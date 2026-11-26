package com.example.app.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import com.example.app.config.AppProperties;
import com.example.app.domain.attachment.AttachmentEntity;
import com.example.app.domain.attachment.AttachmentMapper;
import com.example.app.domain.channel.ChannelEntity;
import com.example.app.domain.channel.ChannelMapper;
import com.example.app.domain.mention.MessageMentionEntity;
import com.example.app.domain.mention.MessageMentionMapper;
import com.example.app.domain.message.MessageEntity;
import com.example.app.domain.message.MessageMapper;
import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.reaction.ReactionEntity;
import com.example.app.domain.reaction.ReactionMapper;
import com.example.app.domain.user.UserEntity;
import com.example.app.domain.user.UserMapper;
import com.example.app.dto.attachment.AttachmentDTO;
import com.example.app.dto.mention.MentionResponseDTO;
import com.example.app.dto.message.*;
import com.example.app.dto.sync.ReactionSummaryDTO;
import com.example.app.exception.ForbiddenException;
import com.example.app.exception.NotFoundException;
import com.example.app.security.AuthUtil;
import com.example.app.websocket.WebSocketEventPublisher;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @InjectMocks
    private MessageService messageService;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private ChannelMapper channelMapper;

    @Mock
    private MessageMentionMapper mentionMapper;

    @Mock
    private ReactionMapper reactionMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AttachmentMapper attachmentMapper;

    @Mock
    private WebSocketEventPublisher eventPublisher;

    @Mock
    private OperationLogService operationLogService;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private AppProperties props;

    @Mock
    private AppProperties.Message messageProps;

    @Mock
    private HttpServletRequest httpRequest;

    private UserEntity user;

	@BeforeEach
	void setup() {

		user = new UserEntity();
		user.setId(1L);
		user.setUsername("test-user");

		when(authUtil.getTokenFromCookie(any()))
				.thenReturn("token");

		when(authUtil.getUserFromToken(any()))
				.thenReturn(user);
	}

	private void setupCreateMessageSuccess() {

		when(props.getMessage())
				.thenReturn(messageProps);

		when(messageProps.getMaxLength())
				.thenReturn(1000);

		doAnswer(invocation -> {

			MessageEntity message =
					invocation.getArgument(0);

			message.setId(1L);

			return null;

		}).when(messageMapper)
				.insert(any(MessageEntity.class));

		when(mentionMapper.findByMessageId(1L))
				.thenReturn(List.of());
	}

	@Test
	void 新規メッセージ送信() {

		setupCreateMessageSuccess();

		when(messageMapper.findByRequestId("req-001"))
				.thenReturn(Optional.empty());

		MessagePostRequestDTO request =
				new MessagePostRequestDTO(
						"test message",
						null,
						null,
						"req-001"
				);

		MessageSummaryDTO result =
				messageService.createMessage(
						1L,
						httpRequest,
						request
				);

		assertNotNull(result);

		verify(messageMapper)
				.insert(any(MessageEntity.class));
	}

	@Test
	void 同一requestId再送() {

		MessageEntity existing =
				new MessageEntity();

		existing.setId(100L);
		existing.setRequestId("req-001");
		existing.setContent("already created");

		when(messageMapper.findByRequestId("req-001"))
				.thenReturn(Optional.of(existing));

		when(mentionMapper.findByMessageId(100L))
				.thenReturn(List.of());

		MessagePostRequestDTO request =
				new MessagePostRequestDTO(
						"already created",
						null,
						null,
						"req-001"
				);

		MessageSummaryDTO result =
				messageService.createMessage(
						1L,
						httpRequest,
						request
				);

		assertNotNull(result);

		verify(messageMapper, never())
				.insert(any());
	}

	@Test
	void DuplicateKeyException発生時の救済処理() {

		MessageEntity existing =
				new MessageEntity();

		existing.setId(100L);
		existing.setRequestId("req-001");

		when(messageMapper.findByRequestId("req-001"))
				.thenReturn(Optional.empty())
				.thenReturn(Optional.of(existing));

		when(mentionMapper.findByMessageId(100L))
				.thenReturn(List.of());

		doThrow(
				new DuplicateKeyException(
						"uk_messages_request_id"
				)
		).when(messageMapper)
				.insert(any(MessageEntity.class));

		MessagePostRequestDTO request =
				new MessagePostRequestDTO(
						"message",
						null,
						null,
						"req-001"
				);

		MessageSummaryDTO result =
				messageService.createMessage(
						1L,
						httpRequest,
						request
				);

		assertNotNull(result);

		verify(messageMapper)
				.insert(any(MessageEntity.class));
	}

	@Test
	void requestId以外のUNIQUE制約違反() {

		when(messageMapper.findByRequestId("req-001"))
				.thenReturn(Optional.empty())
				.thenReturn(Optional.empty());

		doThrow(
				new DuplicateKeyException(
						"other unique constraint"
				)
		).when(messageMapper)
				.insert(any(MessageEntity.class));

		MessagePostRequestDTO request =
				new MessagePostRequestDTO(
						"message",
						null,
						null,
						"req-001"
				);

		assertThrows(
				DuplicateKeyException.class,
				() -> messageService.createMessage(
						1L,
						httpRequest,
						request
				)
		);
	}

	@Test
	void 存在しない返信先() {

		when(messageMapper.findByRequestId("req-001"))
				.thenReturn(Optional.empty());

		when(messageMapper.findById(999L))
				.thenReturn(Optional.empty());

		MessagePostRequestDTO request =
				new MessagePostRequestDTO(
						"reply message",
						999L,
						null,
						"req-001"
				);

		assertThrows(
				NotFoundException.class,
				() -> messageService.createMessage(
						1L,
						httpRequest,
						request
				)
		);
	}

	@Test
	void 最大文字数超過() {

		when(messageMapper.findByRequestId("req-001"))
				.thenReturn(Optional.empty());

		when(messageProps.getMaxLength())
				.thenReturn(10);

		MessagePostRequestDTO request =
				new MessagePostRequestDTO(
						"12345678901",
						null,
						null,
						"req-001"
				);

		assertThrows(
				IllegalArgumentException.class,
				() -> messageService.createMessage(
						1L,
						httpRequest,
						request
				)
		);
	}

	@Test
	void 存在しないメンションユーザー() {

		when(messageMapper.findByRequestId("req-001"))
				.thenReturn(Optional.empty());

		when(userMapper.findById(999L))
				.thenReturn(Optional.empty());

		MessagePostRequestDTO request =
				new MessagePostRequestDTO(
						"test message",
						null,
						List.of(999L),
						"req-001"
				);

		assertDoesNotThrow(() ->
				messageService.createMessage(
						1L,
						httpRequest,
						request
				)
		);

		verify(mentionMapper, never())
				.insert(any());
	}

	@Test
	void メンション付きメッセージ送信() {

		UserEntity mentionedUser =
				new UserEntity();

		mentionedUser.setId(2L);
		mentionedUser.setUsername("target");

		when(messageMapper.findByRequestId("req-001"))
				.thenReturn(Optional.empty());

		when(userMapper.findById(2L))
				.thenReturn(Optional.of(mentionedUser));

		MessagePostRequestDTO request =
				new MessagePostRequestDTO(
						"hello",
						null,
						List.of(2L),
						"req-001"
				);

		assertDoesNotThrow(() ->
				messageService.createMessage(
						1L,
						httpRequest,
						request
				)
		);

		verify(mentionMapper)
				.insert(any(MessageMentionEntity.class));

		verify(eventPublisher)
				.sendMentionAdded(
						eq(2L),
						anyLong(),
						anyMap()
				);
	}

	

}

// class MessageServiceTest {

//     @InjectMocks
//     private MessageService messageService;

//     @Mock private MessageMapper messageMapper;
//     @Mock private ChannelMapper channelMapper;
//     @Mock private ReactionMapper reactionMapper;
//     @Mock private MessageMentionMapper mentionMapper;
//     @Mock private AttachmentMapper attachmentMapper;

//     @BeforeEach
//     void setup() {
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     @DisplayName("1-1: 添付ファイル結合（getMessages）")
//     void testGetMessages_attachmentMerged() {

//         Long channelId = 10L;

//         // --- チャンネル存在 ---
//         ChannelEntity channel = new ChannelEntity();
//         channel.setId(channelId);
//         channel.setName("general");
//         when(channelMapper.findById(channelId)).thenReturn(Optional.of(channel));

//         // --- メッセージ ---
//         MessageEntity m = new MessageEntity();
//         m.setId(100L);
//         m.setChannelId(channelId);
//         m.setUserId(1L);
//         m.setContent("hello");
//         m.setStatus(1);
//         m.setCreatedAt(LocalDateTime.now());
//         m.setUpdatedAt(LocalDateTime.now());

//         when(messageMapper.findByChannel(channelId))
//                 .thenReturn(List.of(m));

//         // --- reactions（空でOK） ---
//         when(reactionMapper.findByMessageId(100L))
//                 .thenReturn(List.of());

//         // --- mentions（空でOK） ---
//         when(mentionMapper.findByMessageId(100L))
//                 .thenReturn(List.of());

//         // --- attachments ---
//         AttachmentEntity a1 = new AttachmentEntity();
//         a1.setId(1L);
//         a1.setMessageId(100L);
//         a1.setOriginalFileName("file1.pdf");
//         a1.setStoredFileName("stored1.pdf");
//         a1.setFileUrl("/files/stored1.pdf");
//         a1.setThumbnailUrl(null);
//         a1.setFileType("pdf");
//         a1.setFileSize(1234L);
//         a1.setCreatedAt(LocalDateTime.now());

//         AttachmentEntity a2 = new AttachmentEntity();
//         a2.setId(2L);
//         a2.setMessageId(100L);
//         a2.setOriginalFileName("image.png");
//         a2.setStoredFileName("stored2.png");
//         a2.setFileUrl("/files/stored2.png");
//         a2.setThumbnailUrl("/thumb/stored2.png");
//         a2.setFileType("png");
//         a2.setFileSize(9999L);
//         a2.setCreatedAt(LocalDateTime.now());

//         when(attachmentMapper.findByMessageId(100L))
//                 .thenReturn(List.of(a1, a2));

//         // --- hasMore 判定 ---
//         when(messageMapper.existsPreviousMessages(channelId, 100L))
//                 .thenReturn(false);

//         // --- 実行 ---
//         MessageResponseDTO response = messageService.getMessages(channelId, null, null);

//         // --- 検証 ---
//         assertThat(response.hasMore()).isFalse();
//         assertThat(response.messages()).hasSize(1);

//         MessageSummaryDTO summary = response.messages().get(0);
//         List<AttachmentDTO> attachments = summary.attachments();

//         assertThat(attachments).hasSize(2);

//         assertThat(attachments)
//                 .extracting("originalFileName")
//                 .containsExactlyInAnyOrder("file1.pdf", "image.png");

//         assertThat(attachments)
//                 .extracting("fileUrl")
//                 .contains("/files/stored1.pdf", "/files/stored2.png");

//         assertThat(attachments)
//                 .extracting("thumbnailUrl")
//                 .contains(null, "/thumb/stored2.png");
//     }
// }

// package com.example.app.service;

// import com.example.app.domain.channel.ChannelEntity;
// import com.example.app.domain.channel.ChannelMapper;
// import com.example.app.domain.message.MessageEntity;
// import com.example.app.domain.message.MessageMapper;
// import com.example.app.domain.operationLog.OperationType;
// import com.example.app.domain.reaction.ReactionMapper;
// import com.example.app.domain.mention.MessageMentionMapper;
// import com.example.app.domain.user.UserEntity;
// import com.example.app.domain.user.UserMapper;
// import com.example.app.dto.message.*;
// import com.example.app.exception.*;
// import com.example.app.security.AuthUtil;
// import com.example.app.websocket.WebSocketEventPublisher;

// import jakarta.servlet.http.HttpServletRequest;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;

// import static org.assertj.core.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyList;
// import static org.mockito.ArgumentMatchers.anyLong;
// import static org.mockito.ArgumentMatchers.anyMap;
// import static org.mockito.ArgumentMatchers.argThat;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;

// import java.time.LocalDateTime;
// import java.util.*;

// class MessageServiceTest {

//     private MessageMapper messageMapper;
//     private ChannelMapper channelMapper;
//     private WebSocketEventPublisher eventPublisher;
//     private MessageMentionMapper mentionMapper;
//     private ReactionMapper reactionMapper;
//     private UserMapper userMapper;
//     private OperationLogService operationLogService;
//     private AuthUtil authUtil;

//     private MessageService messageService;
//     private MessageService spyService;
//     private HttpServletRequest httpRequest;

//     @BeforeEach
//     void setup() {
//         userMapper = mock(UserMapper.class);
//         messageMapper = mock(MessageMapper.class);
//         channelMapper = mock(ChannelMapper.class);
//         eventPublisher = mock(WebSocketEventPublisher.class);
//         operationLogService = mock(OperationLogService.class);
//         mentionMapper = mock(MessageMentionMapper.class);
//         reactionMapper = mock(ReactionMapper.class);
//         authUtil = mock(AuthUtil.class);

//         messageService = new MessageService(
//                 messageMapper,
//                 channelMapper,
//                 eventPublisher,
//                 mentionMapper,
//                 reactionMapper,
//                 userMapper,
//                 operationLogService,
//                 authUtil
//         );

//         spyService = spy(messageService);
//         httpRequest = mock(HttpServletRequest.class);
//     }

//     // getToken と getRequestUser を固定する
//     // private void mockTokenAndUser() {
//     //     doReturn("validToken").when(spyService).getToken(any());
//     //     when(jwtProvider.getUserIdFromToken("validToken")).thenReturn(1L);

//     //     UserEntity user = new UserEntity();
//     //     user.setId(1L);
//     //     user.setUsername("yuta");
//     //     user.setRole(0); // admin
//     //     user.setActive(true);

//     //     when(userMapper.findById(1L)).thenReturn(Optional.of(user));
//     // }
//     @Test
//     @DisplayName("1-1: メッセージ一覧取得（複数件）")
//     void testGetMessages_normal() {

//         // --- チャンネル存在 ---
//         ChannelEntity channel = new ChannelEntity();
//         channel.setId(1L);
//         when(channelMapper.findById(1L)).thenReturn(Optional.of(channel));

//         // --- メッセージ2件（status=0） ---
//         MessageEntity m1 = new MessageEntity();
//         m1.setId(10L);
//         m1.setChannelId(1L);
//         m1.setStatus(0);

//         MessageEntity m2 = new MessageEntity();
//         m2.setId(11L);
//         m2.setChannelId(1L);
//         m2.setStatus(0);

//         when(messageMapper.findByChannel(1L)).thenReturn(List.of(m1, m2));

//         // reactions / mentions は空でOK
//         when(reactionMapper.findByMessageId(anyLong())).thenReturn(List.of());
//         when(mentionMapper.findByMessageId(anyLong())).thenReturn(List.of());

//         // hasMore 判定用
//         when(messageMapper.existsPreviousMessages(1L, 11L)).thenReturn(false);

//         MessageResponseDTO result = messageService.getMessages(1L, null, null);

//         assertThat(result.messages()).hasSize(2);
//         assertThat(result.messages().get(0).message().id()).isEqualTo(10L);
//         assertThat(result.messages().get(1).message().id()).isEqualTo(11L);
//         assertThat(result.hasMore()).isFalse();
//     }

//     @Test
//     @DisplayName("1-2: チャンネル不存在 → NotFoundException")
//     void testGetMessages_channelNotFound() {

//         when(channelMapper.findById(999L)).thenReturn(Optional.empty());

//         assertThatThrownBy(() -> messageService.getMessages(999L, null, null))
//                 .isInstanceOf(NotFoundException.class)
//                 .hasMessageContaining("Channel not found");
//     }

//     @Test
//     @DisplayName("2-1: 正常投稿（メッセージ作成＋WebSocket送信＋ログ記録）")
//     void testCreateMessage_normal() {

//         // --- 認証ユーザー ---
//         UserEntity requestUser = new UserEntity();
//         requestUser.setId(99L);
//         requestUser.setUsername("yuta");

//         when(authUtil.getTokenFromCookie(httpRequest)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenReturn(requestUser);

//         // --- チャンネル存在 ---
//         ChannelEntity channel = new ChannelEntity();
//         channel.setId(1L);
//         when(channelMapper.findById(1L)).thenReturn(Optional.of(channel));

//         // --- メッセージ挿入時に ID を付与する ---
//         doAnswer(inv -> {
//             MessageEntity m = inv.getArgument(0);
//             m.setId(100L); // DB が採番したと仮定
//             return null;
//         }).when(messageMapper).insert(any());

//         // --- メンションなし ---
//         when(mentionMapper.findByMessageId(100L)).thenReturn(List.of());

//         MessagePostRequestDTO req = new MessagePostRequestDTO(
//                 "hello world",
//                 null,
//                 null
//         );

//         MessageSummaryDTO result = messageService.createMessage(1L, httpRequest, req);

//         // --- 内容確認 ---
//         assertThat(result.message().id()).isEqualTo(100L);
//         assertThat(result.message().content()).isEqualTo("hello world");

//         // --- WebSocket送信確認 ---
//         verify(eventPublisher).sendMessageCreated(eq(1L), anyMap());

//         // --- 操作ログ確認 ---
//         verify(operationLogService).record(
//                 eq(requestUser),
//                 eq(OperationType.message_created),
//                 anyList(),
//                 anyList()
//         );
//     }

//     @Test
//     @DisplayName("2-2: チャンネル不存在 → NotFoundException")
//     void testCreateMessage_channelNotFound() {

//         when(authUtil.getTokenFromCookie(httpRequest)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenReturn(new UserEntity());

//         when(channelMapper.findById(999L)).thenReturn(Optional.empty());

//         MessagePostRequestDTO req = new MessagePostRequestDTO("hello", null, null);

//         assertThatThrownBy(() -> messageService.createMessage(999L, httpRequest, req))
//                 .isInstanceOf(NotFoundException.class)
//                 .hasMessageContaining("Channel not found");
//     }

//     @Test
//     @DisplayName("2-3: ユーザー不存在 → NotFoundException")
//     void testCreateMessage_userNotFound() {

//         // Cookie → token → userId=999（存在しない）
//         when(authUtil.getTokenFromCookie(httpRequest)).thenReturn("token");

//         UserEntity fakeUser = new UserEntity();
//         fakeUser.setId(999L);
//         when(authUtil.getUserFromToken("token")).thenReturn(fakeUser);

//         // チャンネルは存在する
//         ChannelEntity channel = new ChannelEntity();
//         channel.setId(1L);
//         when(channelMapper.findById(1L)).thenReturn(Optional.of(channel));

//         // メンション先ユーザーが存在しない
//         when(userMapper.findById(999L)).thenReturn(Optional.empty());

//         MessagePostRequestDTO req = new MessagePostRequestDTO(
//                 "hello",
//                 null,
//                 List.of(999L) // 存在しないユーザー
//         );

//         // createMessage は「メンション先ユーザーが存在しない場合は無視する」仕様なので
//         // ここでは例外は発生しない
//         // → しかし「投稿者本人が存在しない」ケースを作る必要がある

//         // 投稿者本人が存在しないケースを作る
//         when(authUtil.getUserFromToken("token")).thenThrow(new NotFoundException("User not found"));

//         assertThatThrownBy(() -> messageService.createMessage(1L, httpRequest, req))
//                 .isInstanceOf(NotFoundException.class)
//                 .hasMessageContaining("User not found");
//     }


//     @Test
//     @DisplayName("3-1: 正常編集（本人）")
//     void testEditMessage_normal_user() {

//         // --- 認証ユーザー（本人） ---
//         UserEntity requestUser = new UserEntity();
//         requestUser.setId(10L);
//         requestUser.setRole(1); // 一般ユーザー

//         when(authUtil.getTokenFromCookie(httpRequest)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenReturn(requestUser);

//         // --- 編集対象メッセージ ---
//         MessageEntity message = new MessageEntity();
//         message.setId(100L);
//         message.setUserId(10L); // 本人
//         message.setChannelId(1L);
//         message.setContent("old");
//         message.setStatus(0);

//         when(messageMapper.findById(100L)).thenReturn(Optional.of(message));

//         MessageEditRequestDTO req = new MessageEditRequestDTO("new content", 0);

//         MessageEditResponseDTO result = messageService.editMessage(100L, httpRequest, req);

//         assertThat(result.message().content()).isEqualTo("new content");

//         verify(messageMapper).update(any());
//         verify(eventPublisher).sendMessageUpdated(eq(1L), anyMap());
//         verify(operationLogService).record(
//                 eq(requestUser),
//                 eq(OperationType.message_updated),
//                 anyList(),
//                 anyList()
//         );
//     }

//     @Test
//     @DisplayName("3-2: 正常編集（admin）")
//     void testEditMessage_normal_admin() {

//         // --- 認証ユーザー（admin） ---
//         UserEntity admin = new UserEntity();
//         admin.setId(99L);
//         admin.setRole(0); // admin

//         when(authUtil.getTokenFromCookie(httpRequest)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenReturn(admin);

//         // --- 編集対象メッセージ（投稿者は別ユーザー） ---
//         MessageEntity message = new MessageEntity();
//         message.setId(100L);
//         message.setUserId(10L); // 投稿者
//         message.setChannelId(1L);
//         message.setContent("old");
//         message.setStatus(0);

//         when(messageMapper.findById(100L)).thenReturn(Optional.of(message));

//         MessageEditRequestDTO req = new MessageEditRequestDTO("admin edit", 0);

//         MessageEditResponseDTO result = messageService.editMessage(100L, httpRequest, req);

//         assertThat(result.message().content()).isEqualTo("admin edit");
//         verify(messageMapper).update(any());
//     }

//     @Test
//     @DisplayName("3-3: 編集履歴保存（before/after）")
//     void testEditMessage_historySaved() {

//         UserEntity requestUser = new UserEntity();
//         requestUser.setId(10L);
//         requestUser.setRole(1);

//         when(authUtil.getTokenFromCookie(httpRequest)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenReturn(requestUser);

//         MessageEntity message = new MessageEntity();
//         message.setId(100L);
//         message.setUserId(10L);
//         message.setChannelId(1L);
//         message.setContent("old content");
//         message.setStatus(0);

//         when(messageMapper.findById(100L)).thenReturn(Optional.of(message));

//         MessageEditRequestDTO req = new MessageEditRequestDTO("new content", 0);

//         messageService.editMessage(100L, httpRequest, req);

//         verify(operationLogService).record(
//                 eq(requestUser),
//                 eq(OperationType.message_updated),
//                 anyList(),
//                 argThat(list -> list.contains("before_content=old content")
//                         && list.contains("after_content=new content"))
//         );
//     }

//     @Test
//     @DisplayName("3-4: メッセージ不存在 → NotFoundException")
//     void testEditMessage_notFound() {

//         when(authUtil.getTokenFromCookie(httpRequest)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenReturn(new UserEntity());

//         when(messageMapper.findById(999L)).thenReturn(Optional.empty());

//         MessageEditRequestDTO req = new MessageEditRequestDTO("new", 0);

//         assertThatThrownBy(() -> messageService.editMessage(999L, httpRequest, req))
//                 .isInstanceOf(NotFoundException.class)
//                 .hasMessageContaining("Message not found");
//     }

//     @Test
//     @DisplayName("3-5: 権限なし → ForbiddenException")
//     void testEditMessage_forbidden() {

//         // 認証ユーザー（一般ユーザー）
//         UserEntity requestUser = new UserEntity();
//         requestUser.setId(20L); // 投稿者ではない
//         requestUser.setRole(1); // admin でもない

//         when(authUtil.getTokenFromCookie(httpRequest)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenReturn(requestUser);

//         // 編集対象メッセージ（投稿者は別ユーザー）
//         MessageEntity message = new MessageEntity();
//         message.setId(100L);
//         message.setUserId(10L); // 投稿者
//         message.setChannelId(1L);
//         message.setContent("old");
//         message.setStatus(0);

//         when(messageMapper.findById(100L)).thenReturn(Optional.of(message));

//         MessageEditRequestDTO req = new MessageEditRequestDTO("new", 0);

//         assertThatThrownBy(() -> messageService.editMessage(100L, httpRequest, req))
//                 .isInstanceOf(ForbiddenException.class)
//                 .hasMessageContaining("Access denied");
//     }


//     @Test
//     @DisplayName("4-1: 正常削除（本人）")
//     void testDeleteMessage_normal_user() {

//         // --- 認証ユーザー（本人） ---
//         UserEntity requestUser = new UserEntity();
//         requestUser.setId(10L);
//         requestUser.setRole(1); // 一般ユーザー

//         when(authUtil.getTokenFromCookie(httpRequest)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenReturn(requestUser);

//         // --- 削除対象メッセージ（投稿者本人） ---
//         MessageEntity message = new MessageEntity();
//         message.setId(100L);
//         message.setUserId(10L); // 本人
//         message.setChannelId(1L);
//         message.setStatus(0);
//         message.setCreatedAt(LocalDateTime.now().minusDays(1)); // 1日前に作成
//         message.onUpdate();

//         when(messageMapper.findById(100L)).thenReturn(Optional.of(message));

//         String result = messageService.deleteMessage(100L, httpRequest);

//         assertThat(result).isEqualTo("ok");
//         assertThat(message.getStatus()).isEqualTo(2); // 削除済み

//         verify(messageMapper).update(any());
//         verify(eventPublisher).sendMessageDeleted(eq(1L), anyMap());
//         verify(operationLogService).record(eq(requestUser), eq(OperationType.message_deleted), anyList(), anyList());
//     }

//     @Test
//     @DisplayName("4-2: 正常削除（admin）")
//     void testDeleteMessage_normal_admin() {

//         // --- 認証ユーザー（admin） ---
//         UserEntity admin = new UserEntity();
//         admin.setId(99L);
//         admin.setRole(0); // admin

//         when(authUtil.getTokenFromCookie(httpRequest)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenReturn(admin);

//         // --- 削除対象メッセージ（投稿者は別ユーザー） ---
//         MessageEntity message = new MessageEntity();
//         message.setId(100L);
//         message.setUserId(10L);
//         message.setChannelId(1L);
//         message.setStatus(0);
//         message.setCreatedAt(LocalDateTime.now().minusDays(1)); // 1日前に作成
//         message.onUpdate();

//         when(messageMapper.findById(100L)).thenReturn(Optional.of(message));

//         String result = messageService.deleteMessage(100L, httpRequest);

//         assertThat(result).isEqualTo("ok");
//         verify(messageMapper).update(any());
//     }

//     @Test
//     @DisplayName("4-3: メッセージ不存在 → NotFoundException")
//     void testDeleteMessage_notFound() {

//         when(authUtil.getTokenFromCookie(httpRequest)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenReturn(new UserEntity());

//         when(messageMapper.findById(999L)).thenReturn(Optional.empty());

//         assertThatThrownBy(() -> messageService.deleteMessage(999L, httpRequest))
//                 .isInstanceOf(NotFoundException.class)
//                 .hasMessageContaining("Message not found");
//     }

//     @Test
//     @DisplayName("4-4: ユーザー不存在 → NotFoundException")
//     void testDeleteMessage_userNotFound() {

//         // Cookie → token → user が存在しないケース
//         when(authUtil.getTokenFromCookie(httpRequest)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenThrow(new NotFoundException("User not found"));

//         MessageEntity message = new MessageEntity();
//         message.setId(100L);
//         message.setUserId(10L);
//         message.setChannelId(1L);
//         message.setStatus(0);

//         when(messageMapper.findById(100L)).thenReturn(Optional.of(message));

//         assertThatThrownBy(() -> messageService.deleteMessage(100L, httpRequest))
//                 .isInstanceOf(NotFoundException.class)
//                 .hasMessageContaining("User not found");
//     }

//     @Test
//     @DisplayName("4-5: 権限なし → ForbiddenException")
//     void testDeleteMessage_forbidden() {

//         // 認証ユーザー（一般ユーザー）
//         UserEntity requestUser = new UserEntity();
//         requestUser.setId(20L); // 投稿者ではない
//         requestUser.setRole(1); // admin でもない

//         when(authUtil.getTokenFromCookie(httpRequest)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenReturn(requestUser);

//         // 削除対象メッセージ（投稿者は別ユーザー）
//         MessageEntity message = new MessageEntity();
//         message.setId(100L);
//         message.setUserId(10L); // 投稿者
//         message.setChannelId(1L);
//         message.setStatus(0);

//         when(messageMapper.findById(100L)).thenReturn(Optional.of(message));

//         assertThatThrownBy(() -> messageService.deleteMessage(100L, httpRequest))
//                 .isInstanceOf(ForbiddenException.class)
//                 .hasMessageContaining("Access denied");
//     }


//     @Test
//     @DisplayName("5-1: 返信一覧取得成功")
//     void testGetReplies_normal() {

//         // 親メッセージが存在
//         MessageEntity parent = new MessageEntity();
//         parent.setId(100L);

//         when(messageMapper.findById(100L)).thenReturn(Optional.of(parent));

//         // 返信メッセージを複数返す
//         MessageEntity r1 = new MessageEntity();
//         r1.setId(101L);
//         r1.setReplyToMessageId(100L);

//         MessageEntity r2 = new MessageEntity();
//         r2.setId(102L);
//         r2.setReplyToMessageId(100L);

//         when(messageMapper.findByReplyToMessageId(100L))
//                 .thenReturn(List.of(r1, r2));

//         List<MessageDTO> result = messageService.getReplies(100L);

//         assertThat(result).hasSize(2);
//         assertThat(result.get(0).id()).isEqualTo(101L);
//         assertThat(result.get(1).id()).isEqualTo(102L);
//     }

//     @Test
//     @DisplayName("5-2: 親メッセージ不存在 → NotFoundException")
//     void testGetReplies_notFound() {

//         when(messageMapper.findById(999L)).thenReturn(Optional.empty());

//         assertThatThrownBy(() -> messageService.getReplies(999L))
//                 .isInstanceOf(NotFoundException.class)
//                 .hasMessageContaining("Message not found");
//     }

//     @Test
//     @DisplayName("5-3: messageId=null → NullPointerException")
//     void testGetReplies_null() {

//         assertThatThrownBy(() -> messageService.getReplies(null))
//                 .isInstanceOf(NotFoundException.class);
//     }


// }
