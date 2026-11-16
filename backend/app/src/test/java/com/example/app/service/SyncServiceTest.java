package com.example.app.service;

import com.example.app.domain.attachment.AttachmentEntity;
import com.example.app.domain.attachment.AttachmentMapper;
import com.example.app.domain.mention.MessageMentionEntity;
import com.example.app.domain.mention.MessageMentionMapper;
import com.example.app.domain.message.MessageEntity;
import com.example.app.domain.message.MessageMapper;
import com.example.app.domain.reaction.ReactionEntity;
import com.example.app.domain.reaction.ReactionMapper;
import com.example.app.dto.sync.SyncRequestDTO;
import com.example.app.dto.message.MessageSummaryDTO;
import com.example.app.websocket.WebSocketEventPublisher;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SyncServiceTest {

    @InjectMocks
    private SyncService syncService;

    @Mock private MessageMapper messageMapper;
    @Mock private ReactionMapper reactionMapper;
    @Mock private MessageMentionMapper mentionMapper;
    @Mock private AttachmentMapper attachmentMapper;
    @Mock private WebSocketEventPublisher eventPublisher;

    private final Long userId = 99L;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------------------------------------------------------
    // 正常 1: 差分メッセージあり
    // ---------------------------------------------------------
    @Test
    @DisplayName("1-1: 差分メッセージあり → results に差分が入り sendSyncResult が呼ばれる")
    void testSync_normal_diffExists() {

        SyncRequestDTO req = new SyncRequestDTO(List.of(10L), 500L);

        MessageEntity m = new MessageEntity();
        m.setId(501L);
        m.setChannelId(10L);
        m.setUserId(1L);
        m.setContent("new message");
        m.setStatus(1);
        m.setCreatedAt(LocalDateTime.now());
        m.setUpdatedAt(LocalDateTime.now());

        when(messageMapper.findByChannelIdAndIdGreaterThan(10L, 500L))
                .thenReturn(List.of(m));

        when(reactionMapper.findByMessageId(501L)).thenReturn(List.of());
        when(mentionMapper.findByMessageId(501L)).thenReturn(List.of());
        when(attachmentMapper.findByMessageId(501L)).thenReturn(List.of());

        syncService.sync(userId, req);

        ArgumentCaptor<List<MessageSummaryDTO>> captor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher, times(1)).sendSyncResult(eq(userId), captor.capture());

        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).message().content()).isEqualTo("new message");
    }

    // ---------------------------------------------------------
    // 正常 2: 差分メッセージが複数
    // ---------------------------------------------------------
    @Test
    @DisplayName("1-2: 差分メッセージが複数 → DTO が複数生成される")
    void testSync_normal_multipleDiff() {

        SyncRequestDTO req = new SyncRequestDTO(List.of(10L), 500L);

        List<MessageEntity> diffMessages = new ArrayList<>();
        for (long i = 501; i <= 510; i++) {
            MessageEntity m = new MessageEntity();
            m.setId(i);
            m.setChannelId(10L);
            m.setUserId(1L);
            m.setContent("msg" + i);
            m.setStatus(1);
            m.setCreatedAt(LocalDateTime.now());
            m.setUpdatedAt(LocalDateTime.now());
            diffMessages.add(m);
        }

        when(messageMapper.findByChannelIdAndIdGreaterThan(10L, 500L))
                .thenReturn(diffMessages);

        when(reactionMapper.findByMessageId(anyLong())).thenReturn(List.of());
        when(mentionMapper.findByMessageId(anyLong())).thenReturn(List.of());
        when(attachmentMapper.findByMessageId(anyLong())).thenReturn(List.of());

        syncService.sync(userId, req);

        ArgumentCaptor<List<MessageSummaryDTO>> captor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).sendSyncResult(eq(userId), captor.capture());

        assertThat(captor.getValue()).hasSize(10);
    }

    // ---------------------------------------------------------
    // 正常 3: 差分 0 件
    // ---------------------------------------------------------
    @Test
    @DisplayName("1-3: 差分なし → 空リストが sendSyncResult に渡される")
    void testSync_normal_noDiff() {

        SyncRequestDTO req = new SyncRequestDTO(List.of(10L), 500L);

        when(messageMapper.findByChannelIdAndIdGreaterThan(10L, 500L))
                .thenReturn(List.of());

        syncService.sync(userId, req);

        ArgumentCaptor<List<MessageSummaryDTO>> captor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).sendSyncResult(eq(userId), captor.capture());

        assertThat(captor.getValue()).isEmpty();
    }

    // ---------------------------------------------------------
    // 異常 4: req が null
    // ---------------------------------------------------------
    @Test
    @DisplayName("2-1: req=null → IllegalArgumentException")
    void testSync_error_reqNull() {
        assertThatThrownBy(() -> syncService.sync(userId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SyncRequestDTO is required");
    }

    // ---------------------------------------------------------
    // 異常 5: channelIds が null
    // ---------------------------------------------------------
    @Test
    @DisplayName("2-2: channelIds=null → IllegalArgumentException")
    void testSync_error_channelIdsNull() {

        SyncRequestDTO req = new SyncRequestDTO(null, 500L);

        assertThatThrownBy(() -> syncService.sync(userId, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("channelIds is required");
    }

    // ---------------------------------------------------------
    // 異常 6: channelIds が空
    // ---------------------------------------------------------
    @Test
    @DisplayName("2-3: channelIds=[] → IllegalArgumentException")
    void testSync_error_channelIdsEmpty() {

        SyncRequestDTO req = new SyncRequestDTO(List.of(), 500L);

        assertThatThrownBy(() -> syncService.sync(userId, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("channelIds is required");
    }

    // ---------------------------------------------------------
    // 異常 7: lastMessageId が null
    // ---------------------------------------------------------
    @Test
    @DisplayName("2-4: lastMessageId=null → IllegalArgumentException")
    void testSync_error_lastMessageIdNull() {

        SyncRequestDTO req = new SyncRequestDTO(List.of(10L), null);

        assertThatThrownBy(() -> syncService.sync(userId, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("lastMessageId is required");
    }
}

// package com.example.app.service;

// import com.example.app.domain.message.MessageEntity;
// import com.example.app.domain.message.MessageMapper;
// import com.example.app.domain.mention.MessageMentionEntity;
// import com.example.app.domain.mention.MessageMentionMapper;
// import com.example.app.domain.reaction.ReactionEntity;
// import com.example.app.domain.reaction.ReactionMapper;
// import com.example.app.dto.message.MessageSummaryDTO;
// import com.example.app.dto.sync.*;
// import com.example.app.websocket.WebSocketEventPublisher;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.mockito.ArgumentCaptor;

// import jakarta.servlet.http.HttpServletRequest;

// import static org.assertj.core.api.Assertions.*;
// import static org.mockito.Mockito.*;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// class SyncServiceTest {

//     private MessageMapper messageMapper;
//     private ReactionMapper reactionMapper;
//     private MessageMentionMapper mentionMapper;
//     private WebSocketEventPublisher eventPublisher;
//     private SyncService syncService;

//     @BeforeEach
//     void setup() {
//         messageMapper = mock(MessageMapper.class);
//         reactionMapper = mock(ReactionMapper.class);
//         mentionMapper = mock(MessageMentionMapper.class);
//         eventPublisher = mock(WebSocketEventPublisher.class);

//         syncService = new SyncService(messageMapper, reactionMapper, mentionMapper, eventPublisher);
//     }

//     // ---------------------------------------------------------
//     // 1. 正常：差分メッセージ同期成功
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("1-1: 正常 - 差分メッセージ同期成功")
//     void testSync_normal() {

//         Long userId = 1L;
//         SyncRequestDTO req = new SyncRequestDTO(List.of(10L), 500L);

//         // DB に id=501〜510 が存在する想定
//         MessageEntity m1 = new MessageEntity();
//         m1.setId(501L);
//         m1.setChannelId(10L);
//         m1.setUserId(2L);
//         m1.setContent("hello");
//         m1.setStatus(0);
//         m1.setCreatedAt(LocalDateTime.now());
//         m1.setUpdatedAt(LocalDateTime.now());

//         when(messageMapper.findByChannelIdAndIdGreaterThan(10L, 500L))
//                 .thenReturn(List.of(m1));

//         when(reactionMapper.findByMessageId(501L)).thenReturn(List.of());
//         when(mentionMapper.findByMessageId(501L)).thenReturn(List.of());

//         syncService.sync(userId, req);

//         ArgumentCaptor<List<MessageSummaryDTO>> captor = ArgumentCaptor.forClass(List.class);
//         verify(eventPublisher).sendSyncResult(eq(userId), captor.capture());

//         List<MessageSummaryDTO> results = captor.getValue();
//         assertThat(results).hasSize(1);
//         assertThat(results.get(0).message().id()).isEqualTo(501L);
//     }

//     // ---------------------------------------------------------
//     // 2. 異常：req=null
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("1-2: 異常 - req=null")
//     void testSync_reqNull() {

//         assertThatThrownBy(() -> syncService.sync(1L, null))
//                 .isInstanceOf(IllegalArgumentException.class)
//                 .hasMessageContaining("SyncRequestDTO is required");
//     }

//     // ---------------------------------------------------------
//     // 3. 異常：channelIds=null
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("1-3: 異常 - channelIds=null")
//     void testSync_channelIdsNull() {

//         SyncRequestDTO req = new SyncRequestDTO(null, 500L);

//         assertThatThrownBy(() -> syncService.sync(1L, req))
//                 .isInstanceOf(IllegalArgumentException.class)
//                 .hasMessageContaining("channelIds is required");
//     }

//     // ---------------------------------------------------------
//     // 4. 異常：lastMessageId=null
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("1-4: 異常 - lastMessageId=null")
//     void testSync_lastMessageIdNull() {

//         SyncRequestDTO req = new SyncRequestDTO(List.of(10L), null);

//         assertThatThrownBy(() -> syncService.sync(1L, req))
//                 .isInstanceOf(IllegalArgumentException.class)
//                 .hasMessageContaining("lastMessageId is required");
//     }
// }
