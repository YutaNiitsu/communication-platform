package com.example.app.websocket;

import com.example.app.domain.attachment.AttachmentEntity;
import com.example.app.websocket.session.UserSessionRegistry;
import com.example.app.dto.attachment.AttachmentDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class WebSocketEventPublisherTest {

    @InjectMocks
    private WebSocketEventPublisher publisher;

    @Mock
    private UserSessionRegistry sessionRegistry;

    @Mock private WebSocketSession user1Tab1;
    @Mock private WebSocketSession user1Tab2;
    @Mock private WebSocketSession user2Tab1;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------------------------------------------------------
    // 1. publishAttachmentAdded 正常系
    // ---------------------------------------------------------
    @Test
    @DisplayName("1-1: 添付追加イベント送信成功（publishAttachmentAdded）")
    void testPublishAttachmentAdded_success() throws Exception {

        Long channelId = 10L;

        AttachmentDTO attachment = new AttachmentDTO(
            1L,
            100L,
            "file.pdf",
            "stored.pdf",
            "/files/stored.pdf",
            null,
            "pdf",
            1234L,
            LocalDateTime.now()
        );

        // --- 購読ユーザー ---
        when(sessionRegistry.getUsersSubscribedTo(channelId))
                .thenReturn(Set.of(1L, 2L));

        // --- ユーザー1は2タブ ---
        when(sessionRegistry.getSessions(1L))
                .thenReturn(Set.of(user1Tab1, user1Tab2));

        // --- ユーザー2は1タブ ---
        when(sessionRegistry.getSessions(2L))
                .thenReturn(Set.of(user2Tab1));

        // --- 全セッション open ---
        when(user1Tab1.isOpen()).thenReturn(true);
        when(user1Tab2.isOpen()).thenReturn(true);
        when(user2Tab1.isOpen()).thenReturn(true);

        // --- 実行 ---
        publisher.publishAttachmentAdded(channelId, attachment);

        // --- JSON キャプチャ ---
        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);

        verify(user1Tab1, times(1)).sendMessage(captor.capture());
        verify(user1Tab2, times(1)).sendMessage(any(TextMessage.class));
        verify(user2Tab1, times(1)).sendMessage(any(TextMessage.class));

        String json = captor.getValue().getPayload();

        // --- JSON 内容検証 ---
        assertThat(json).contains("\"event\":\"attachment_added\"");
        assertThat(json).contains("\"channel_id\":10");
        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"original_file_name\":\"file.pdf\"");
        assertThat(json).contains("\"file_url\":\"/files/stored.pdf\"");
    }

    // ---------------------------------------------------------
    // 2. publishAttachmentRemoved 正常系
    // ---------------------------------------------------------
    @Test
    @DisplayName("2-1: 添付削除イベント送信成功（publishAttachmentRemoved）")
    void testPublishAttachmentRemoved_success() throws Exception {

        Long channelId = 10L;
        Long messageId = 100L;
        Long attachmentId = 200L;
        String reason = "user_deleted";

        // --- 購読ユーザー ---
        when(sessionRegistry.getUsersSubscribedTo(channelId))
                .thenReturn(Set.of(1L, 2L));

        // --- ユーザー1は2タブ ---
        when(sessionRegistry.getSessions(1L))
                .thenReturn(Set.of(user1Tab1, user1Tab2));

        // --- ユーザー2は1タブ ---
        when(sessionRegistry.getSessions(2L))
                .thenReturn(Set.of(user2Tab1));

        // --- 全セッション open ---
        when(user1Tab1.isOpen()).thenReturn(true);
        when(user1Tab2.isOpen()).thenReturn(true);
        when(user2Tab1.isOpen()).thenReturn(true);

        // --- 実行 ---
        publisher.publishAttachmentRemoved(channelId, messageId, attachmentId, reason);

        // --- JSON キャプチャ ---
        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);

        verify(user1Tab1, times(1)).sendMessage(captor.capture());
        verify(user1Tab2, times(1)).sendMessage(any(TextMessage.class));
        verify(user2Tab1, times(1)).sendMessage(any(TextMessage.class));

        String json = captor.getValue().getPayload();

        // --- JSON 内容検証 ---
        assertThat(json).contains("\"event\":\"attachment_removed\"");
        assertThat(json).contains("\"channel_id\":10");
        assertThat(json).contains("\"message_id\":100");
        assertThat(json).contains("\"attachment_id\":200");
        assertThat(json).contains("\"reason\":\"user_deleted\"");
    }
}


// package com.example.app.websocket;

// import com.example.app.dto.message.MessageSummaryDTO;
// import com.example.app.websocket.session.UserSessionRegistry;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.springframework.boot.test.system.CapturedOutput;
// import org.springframework.boot.test.system.OutputCaptureExtension;
// import org.springframework.test.util.ReflectionTestUtils;
// import org.springframework.web.socket.TextMessage;
// import org.springframework.web.socket.WebSocketSession;
// import org.springframework.messaging.MessageDeliveryException;

// import java.time.LocalDateTime;
// import java.util.*;

// import static org.assertj.core.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// @ExtendWith(OutputCaptureExtension.class)
// class WebSocketEventPublisherTest {

//     private UserSessionRegistry sessionRegistry;
//     private WebSocketEventPublisher publisher;

//     @BeforeEach
//     void setUp() {
//         sessionRegistry = mock(UserSessionRegistry.class);
//         publisher = new WebSocketEventPublisher(sessionRegistry);
//     }

//     // ---------------------------------------------------------
//     // 1. sendToUser 正常系
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("sendToUser - 正常: JSON変換成功 & 全openセッション送信")
//     void testSendToUser_jsonSuccess() throws Exception {
//         WebSocketSession s1 = mock(WebSocketSession.class);
//         WebSocketSession s2 = mock(WebSocketSession.class);

//         when(s1.isOpen()).thenReturn(true);
//         when(s2.isOpen()).thenReturn(true);

//         Set<WebSocketSession> sessions = new LinkedHashSet<>();
//         sessions.add(s1);
//         sessions.add(s2);

//         when(sessionRegistry.getSessions(10L)).thenReturn(sessions);

//         publisher.sendPong(10L, 12345L);

//         verify(s1).sendMessage(any(TextMessage.class));
//         verify(s2).sendMessage(any(TextMessage.class));
//     }

//     @Test
//     @DisplayName("sendToUser - 正常: session=null はスキップ")
//     void testSendToUser_sessionNull() throws Exception {
//         WebSocketSession s2 = mock(WebSocketSession.class);
//         when(s2.isOpen()).thenReturn(true);

//         Set<WebSocketSession> sessions = new LinkedHashSet<>();
//         sessions.add(null);
//         sessions.add(s2);

//         when(sessionRegistry.getSessions(10L)).thenReturn(sessions);

//         publisher.sendPong(10L, 12345L);

//         verify(s2).sendMessage(any(TextMessage.class));
//     }

//     @Test
//     @DisplayName("sendToUser - 正常: session closed はスキップ")
//     void testSendToUser_sessionClosed() throws Exception {
//         WebSocketSession s1 = mock(WebSocketSession.class);
//         when(s1.isOpen()).thenReturn(false);

//         Set<WebSocketSession> sessions = new LinkedHashSet<>();
//         sessions.add(s1);

//         when(sessionRegistry.getSessions(10L)).thenReturn(sessions);

//         publisher.sendPong(10L, 12345L);

//         verify(s1, never()).sendMessage(any());
//     }

//     @Test
//     @DisplayName("sendToUser - 正常: Unexpected Exception でも他セッション継続")
//     void testSendToUser_unexpectedException() throws Exception {
//         WebSocketSession s1 = mock(WebSocketSession.class);
//         WebSocketSession s2 = mock(WebSocketSession.class);

//         when(s1.isOpen()).thenReturn(true);
//         when(s2.isOpen()).thenReturn(true);

//         doThrow(new RuntimeException("unexpected")).when(s1).sendMessage(any());

//         Set<WebSocketSession> sessions = new LinkedHashSet<>();
//         sessions.add(s1);
//         sessions.add(s2);

//         when(sessionRegistry.getSessions(10L)).thenReturn(sessions);

//         publisher.sendPong(10L, 12345L);

//         verify(s2).sendMessage(any(TextMessage.class));
//     }

//     // ---------------------------------------------------------
//     // 1. sendToUser 異常系（ログ検証）
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("sendToUser - 異常: JSON変換失敗 → JSON serialization error ログ")
//     void testSendToUser_jsonError(CapturedOutput output) throws Exception {
//         WebSocketEventPublisher spyPublisher = spy(publisher);

//         ObjectMapper mockMapper = mock(ObjectMapper.class);
//         doThrow(new JsonProcessingException("json error"){}).when(mockMapper).writeValueAsString(any());

//         ReflectionTestUtils.setField(spyPublisher, "objectMapper", mockMapper);

//         when(sessionRegistry.getSessions(10L)).thenReturn(Collections.emptySet());

//         spyPublisher.sendPong(10L, 12345L);

//         assertThat(output).contains("JSON serialization error");
//         assertThat(output).contains("[ADMIN NOTIFY]");
//     }

//     @Test
//     @DisplayName("sendToUser - 異常: MessageDeliveryException → MessageDeliveryException ログ")
//     void testSendToUser_messageDeliveryException(CapturedOutput output) throws Exception {
//         WebSocketSession s1 = mock(WebSocketSession.class);
//         WebSocketSession s2 = mock(WebSocketSession.class);

//         when(s1.isOpen()).thenReturn(true);
//         when(s2.isOpen()).thenReturn(true);

//         doThrow(new MessageDeliveryException("delivery error")).when(s1).sendMessage(any());

//         Set<WebSocketSession> sessions = new LinkedHashSet<>();
//         sessions.add(s1);
//         sessions.add(s2);

//         when(sessionRegistry.getSessions(10L)).thenReturn(sessions);

//         publisher.sendPong(10L, 12345L);

//         assertThat(output).contains("MessageDeliveryException");
//         assertThat(output).contains("[ADMIN NOTIFY]");
//         verify(s2, never()).sendMessage(any());
//     }

//     @Test
//     @DisplayName("sendToUser - 異常: IOException → IOException ログ & 継続")
//     void testSendToUser_ioException(CapturedOutput output) throws Exception {
//         WebSocketSession s1 = mock(WebSocketSession.class);
//         WebSocketSession s2 = mock(WebSocketSession.class);

//         when(s1.isOpen()).thenReturn(true);
//         when(s2.isOpen()).thenReturn(true);

//         doThrow(new io.jsonwebtoken.io.IOException("disconnect")).when(s1).sendMessage(any());

//         Set<WebSocketSession> sessions = new LinkedHashSet<>();
//         sessions.add(s1);
//         sessions.add(s2);

//         when(sessionRegistry.getSessions(10L)).thenReturn(sessions);

//         publisher.sendPong(10L, 12345L);

//         assertThat(output).contains("IOException (client disconnected)");
//         verify(s2).sendMessage(any(TextMessage.class));
//     }

//     @Test
//     @DisplayName("sendToUser - 異常: OutOfMemoryError → OutOfMemoryError ログ & 中断")
//     void testSendToUser_outOfMemoryError(CapturedOutput output) throws Exception {
//         WebSocketSession s1 = mock(WebSocketSession.class);
//         WebSocketSession s2 = mock(WebSocketSession.class);

//         when(s1.isOpen()).thenReturn(true);
//         when(s2.isOpen()).thenReturn(true);

//         doThrow(new OutOfMemoryError("oom")).when(s1).sendMessage(any());

//         Set<WebSocketSession> sessions = new LinkedHashSet<>();
//         sessions.add(s1);
//         sessions.add(s2);

//         when(sessionRegistry.getSessions(10L)).thenReturn(sessions);

//         publisher.sendPong(10L, 12345L);

//         assertThat(output).contains("OutOfMemoryError");
//         assertThat(output).contains("[ADMIN NOTIFY]");
//         verify(s2, never()).sendMessage(any());
//     }

//     // ---------------------------------------------------------
//     // 2. sendToChannel 正常系
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("sendToChannel - 正常: JSON変換成功 & 購読者全員へ送信")
//     void testSendToChannel_jsonSuccess() throws Exception {
//         WebSocketSession s1 = mock(WebSocketSession.class);
//         WebSocketSession s2 = mock(WebSocketSession.class);

//         when(s1.isOpen()).thenReturn(true);
//         when(s2.isOpen()).thenReturn(true);

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L, 2L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s1));
//         when(sessionRegistry.getSessions(2L)).thenReturn(Set.of(s2));

//         publisher.sendReactionRemoved(100L, 200L, 1L, "👍");

//         verify(s1).sendMessage(any(TextMessage.class));
//         verify(s2).sendMessage(any(TextMessage.class));
//     }

//     @Test
//     @DisplayName("sendToChannel - 正常: session=null はスキップ")
//     void testSendToChannel_sessionNull() throws Exception {
//         WebSocketSession s2 = mock(WebSocketSession.class);
//         when(s2.isOpen()).thenReturn(true);

//         Set<WebSocketSession> sessions = new LinkedHashSet<>();
//         sessions.add(null);
//         sessions.add(s2);

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(sessions);

//         publisher.sendReactionRemoved(100L, 200L, 1L, "👍");

//         verify(s2).sendMessage(any(TextMessage.class));
//     }

//     @Test
//     @DisplayName("sendToChannel - 正常: session closed はスキップ")
//     void testSendToChannel_sessionClosed() throws Exception {
//         WebSocketSession s1 = mock(WebSocketSession.class);
//         when(s1.isOpen()).thenReturn(false);

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s1));

//         publisher.sendReactionRemoved(100L, 200L, 1L, "👍");

//         verify(s1, never()).sendMessage(any());
//     }

//     @Test
//     @DisplayName("sendToChannel - 正常: Unexpected Exception でも他セッション継続")
//     void testSendToChannel_unexpectedException() throws Exception {
//         WebSocketSession s1 = mock(WebSocketSession.class);
//         WebSocketSession s2 = mock(WebSocketSession.class);

//         when(s1.isOpen()).thenReturn(true);
//         when(s2.isOpen()).thenReturn(true);

//         doThrow(new RuntimeException("unexpected")).when(s1).sendMessage(any());

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L, 2L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s1));
//         when(sessionRegistry.getSessions(2L)).thenReturn(Set.of(s2));

//         publisher.sendReactionRemoved(100L, 200L, 1L, "👍");

//         verify(s2).sendMessage(any(TextMessage.class));
//     }

//     // ---------------------------------------------------------
//     // 2. sendToChannel 異常系（ログ検証）
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("sendToChannel - 異常: JSON変換失敗 → JSON serialization error ログ")
//     void testSendToChannel_jsonError(CapturedOutput output) throws Exception {
//         WebSocketEventPublisher spyPublisher = spy(publisher);

//         ObjectMapper mockMapper = mock(ObjectMapper.class);
//         doThrow(new JsonProcessingException("json error"){}).when(mockMapper).writeValueAsString(any());

//         ReflectionTestUtils.setField(spyPublisher, "objectMapper", mockMapper);

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of());

//         spyPublisher.sendMessageCreated(100L, Map.of("id", 1));

//         assertThat(output).contains("JSON serialization error");
//         assertThat(output).contains("[ADMIN NOTIFY]");
//     }

//     @Test
//     @DisplayName("sendToChannel - 異常: MessageDeliveryException → MessageDeliveryException ログ")
//     void testSendToChannel_messageDeliveryException(CapturedOutput output) throws Exception {
//         WebSocketSession s1 = mock(WebSocketSession.class);
//         WebSocketSession s2 = mock(WebSocketSession.class);

//         when(s1.isOpen()).thenReturn(true);
//         when(s2.isOpen()).thenReturn(true);

//         doThrow(new MessageDeliveryException("delivery error")).when(s1).sendMessage(any());

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L, 2L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s1));
//         when(sessionRegistry.getSessions(2L)).thenReturn(Set.of(s2));

//         publisher.sendMessageCreated(100L, Map.of("id", 1));

//         assertThat(output).contains("MessageDeliveryException");
//         assertThat(output).contains("[ADMIN NOTIFY]");
//         verify(s2, never()).sendMessage(any());
//     }

//     @Test
//     @DisplayName("sendToChannel - 異常: IOException → Unexpected error ログ & 継続")
//     void testSendToChannel_ioException(CapturedOutput output) throws Exception {
//         WebSocketSession s1 = mock(WebSocketSession.class);
//         WebSocketSession s2 = mock(WebSocketSession.class);

//         when(s1.isOpen()).thenReturn(true);
//         when(s2.isOpen()).thenReturn(true);

//         doThrow(new io.jsonwebtoken.io.IOException("disconnect")).when(s1).sendMessage(any());

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L, 2L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s1));
//         when(sessionRegistry.getSessions(2L)).thenReturn(Set.of(s2));

//         publisher.sendMessageCreated(100L, Map.of("id", 1));

//         assertThat(output).contains("IOException (client disconnected)");
//         verify(s2).sendMessage(any(TextMessage.class));
//     }

//     @Test
//     @DisplayName("sendToChannel - 異常: OutOfMemoryError → OutOfMemoryError ログ & 中断")
//     void testSendToChannel_outOfMemoryError(CapturedOutput output) throws Exception {
//         WebSocketSession s1 = mock(WebSocketSession.class);
//         WebSocketSession s2 = mock(WebSocketSession.class);

//         when(s1.isOpen()).thenReturn(true);
//         when(s2.isOpen()).thenReturn(true);

//         doThrow(new OutOfMemoryError("oom")).when(s1).sendMessage(any());

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L, 2L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s1));
//         when(sessionRegistry.getSessions(2L)).thenReturn(Set.of(s2));

//         publisher.sendMessageCreated(100L, Map.of("id", 1));

//         assertThat(output).contains("OutOfMemoryError");
//         assertThat(output).contains("[ADMIN NOTIFY]");
//         verify(s2, never()).sendMessage(any());
//     }

//     // ---------------------------------------------------------
//     // 3. 公開API payload 検証
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("sendMessageCreated - event=message_created が含まれる")
//     void testSendMessageCreated_payload() throws Exception {
//         WebSocketSession s = mock(WebSocketSession.class);
//         when(s.isOpen()).thenReturn(true);

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s));

//         Map<String, Object> msg = Map.of("id", 200);

//         publisher.sendMessageCreated(100L, msg);

//         ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
//         verify(s).sendMessage(captor.capture());

//         String json = captor.getValue().getPayload();
//         assertThat(json).contains("\"event\":\"message_created\"");
//         assertThat(json).contains("\"channel_id\":100");
//         assertThat(json).contains("\"id\":200");
//     }

//     @Test
//     @DisplayName("sendMessageUpdated - event=message_updated")
//     void testSendMessageUpdated_payload() throws Exception {
//         WebSocketSession s = mock(WebSocketSession.class);
//         when(s.isOpen()).thenReturn(true);

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s));

//         Map<String, Object> msg = Map.of("id", 200);

//         publisher.sendMessageUpdated(100L, msg);

//         ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
//         verify(s).sendMessage(captor.capture());

//         assertThat(captor.getValue().getPayload()).contains("\"event\":\"message_updated\"");
//     }

//     @Test
//     @DisplayName("sendMessageDeleted - event=message_deleted")
//     void testSendMessageDeleted_payload() throws Exception {
//         WebSocketSession s = mock(WebSocketSession.class);
//         when(s.isOpen()).thenReturn(true);

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s));

//         Map<String, Object> msg = Map.of("id", 200);

//         publisher.sendMessageDeleted(100L, msg);

//         ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
//         verify(s).sendMessage(captor.capture());

//         assertThat(captor.getValue().getPayload()).contains("\"event\":\"message_deleted\"");
//     }

//     @Test
//     @DisplayName("sendMentionAdded - sendToUser が呼ばれる")
//     void testSendMentionAdded_payload() throws Exception {
//         WebSocketSession s = mock(WebSocketSession.class);
//         when(s.isOpen()).thenReturn(true);

//         when(sessionRegistry.getSessions(10L)).thenReturn(Set.of(s));

//         Map<String, Object> mention = Map.of("id", 300);

//         publisher.sendMentionAdded(10L, 200L, mention);

//         ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
//         verify(s).sendMessage(captor.capture());

//         String json = captor.getValue().getPayload();
//         assertThat(json).contains("\"event\":\"mention_created\"");
//         assertThat(json).contains("\"message_id\":200");
//     }

//     @Test
//     @DisplayName("sendReactionAdded - event=reaction_added")
//     void testSendReactionAdded_payload() throws Exception {
//         WebSocketSession s = mock(WebSocketSession.class);
//         when(s.isOpen()).thenReturn(true);

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s));

//         publisher.sendReactionAdded(100L, 200L, 10L, "👍", LocalDateTime.now());

//         ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
//         verify(s).sendMessage(captor.capture());

//         assertThat(captor.getValue().getPayload()).contains("\"event\":\"reaction_added\"");
//     }

//     @Test
//     @DisplayName("sendReactionRemoved - event=reaction_removed")
//     void testSendReactionRemoved_payload() throws Exception {
//         WebSocketSession s = mock(WebSocketSession.class);
//         when(s.isOpen()).thenReturn(true);

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s));

//         publisher.sendReactionRemoved(100L, 200L, 10L, "👍");

//         ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
//         verify(s).sendMessage(captor.capture());

//         assertThat(captor.getValue().getPayload()).contains("\"event\":\"reaction_removed\"");
//     }

//     @Test
//     @DisplayName("sendChannelCreated - event=channel_created")
//     void testSendChannelCreated_payload() throws Exception {
//         WebSocketSession s = mock(WebSocketSession.class);
//         when(s.isOpen()).thenReturn(true);

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s));

//         publisher.sendChannelCreated(100L, Map.of("id", 1));

//         ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
//         verify(s).sendMessage(captor.capture());

//         assertThat(captor.getValue().getPayload()).contains("\"event\":\"channel_created\"");
//     }

//     @Test
//     @DisplayName("sendChannelUpdated - event=channel_updated")
//     void testSendChannelUpdated_payload() throws Exception {
//         WebSocketSession s = mock(WebSocketSession.class);
//         when(s.isOpen()).thenReturn(true);

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s));

//         publisher.sendChannelUpdated(100L, Map.of("id", 1));

//         ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
//         verify(s).sendMessage(captor.capture());

//         assertThat(captor.getValue().getPayload()).contains("\"event\":\"channel_updated\"");
//     }

//     @Test
//     @DisplayName("sendChannelJoined - event=channel_joined")
//     void testSendChannelJoined_payload() throws Exception {
//         WebSocketSession s = mock(WebSocketSession.class);
//         when(s.isOpen()).thenReturn(true);

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s));

//         publisher.sendChannelJoined(100L, Map.of("id", 1));

//         ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
//         verify(s).sendMessage(captor.capture());

//         assertThat(captor.getValue().getPayload()).contains("\"event\":\"channel_joined\"");
//     }

//     @Test
//     @DisplayName("sendChannelLeft - event=channel_left")
//     void testSendChannelLeft_payload() throws Exception {
//         WebSocketSession s = mock(WebSocketSession.class);
//         when(s.isOpen()).thenReturn(true);

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s));

//         publisher.sendChannelLeft(100L, 10L);

//         ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
//         verify(s).sendMessage(captor.capture());

//         assertThat(captor.getValue().getPayload()).contains("\"event\":\"channel_left\"");
//     }

//     @Test
//     @DisplayName("sendChannelDeleted - event=channel_deleted")
//     void testSendChannelDeleted_payload() throws Exception {
//         WebSocketSession s = mock(WebSocketSession.class);
//         when(s.isOpen()).thenReturn(true);

//         when(sessionRegistry.getUsersSubscribedTo(100L)).thenReturn(Set.of(1L));
//         when(sessionRegistry.getSessions(1L)).thenReturn(Set.of(s));

//         publisher.sendChannelDeleted(100L);

//         ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
//         verify(s).sendMessage(captor.capture());

//         assertThat(captor.getValue().getPayload()).contains("\"event\":\"channel_deleted\"");
//     }

//     @Test
//     @DisplayName("sendPong - event=pong")
//     void testSendPong_payload() throws Exception {
//         WebSocketSession s = mock(WebSocketSession.class);
//         when(s.isOpen()).thenReturn(true);

//         when(sessionRegistry.getSessions(10L)).thenReturn(Set.of(s));

//         publisher.sendPong(10L, 12345L);

//         ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
//         verify(s).sendMessage(captor.capture());

//         assertThat(captor.getValue().getPayload()).contains("\"event\":\"pong\"");
//         assertThat(captor.getValue().getPayload()).contains("\"timestamp\":12345");
//     }

//     @Test
//     @DisplayName("sendSyncResult - event=sync_result")
//     void testSendSyncResult_payload() throws Exception {
//         WebSocketSession s = mock(WebSocketSession.class);
//         when(s.isOpen()).thenReturn(true);

//         when(sessionRegistry.getSessions(10L)).thenReturn(Set.of(s));

//         MessageSummaryDTO summary = new MessageSummaryDTO(null, List.of(), List.of(), List.of());
//         publisher.sendSyncResult(10L, List.of(summary));

//         ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
//         verify(s).sendMessage(captor.capture());

//         assertThat(captor.getValue().getPayload()).contains("\"event\":\"sync_result\"");
//         assertThat(captor.getValue().getPayload()).contains("\"messages\"");
//     }
// }
