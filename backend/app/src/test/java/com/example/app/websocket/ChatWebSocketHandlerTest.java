// package com.example.app.websocket;

// import com.example.app.service.JwtProvider;
// import com.example.app.service.SyncService;
// import com.example.app.websocket.session.UserSessionRegistry;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.springframework.web.socket.TextMessage;
// import org.springframework.web.socket.WebSocketSession;

// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// class ChatWebSocketHandlerTest {

//     @Mock
//     private WebSocketEventPublisher eventPublisher;

//     @Mock
//     private SyncService syncService;

//     @Mock
//     private UserSessionRegistry sessionRegistry;

//     @Mock
//     private JwtProvider jwtProvider;

//     @Mock
//     private WebSocketSession session;

//     @InjectMocks
//     private ChatWebSocketHandler webSocketHandler;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//         // 共通の認証モック設定（Cookieからtokenを抽出し、userId=1Lを返す）
//         when(jwtProvider.extractTokenFromCookie(session)).thenReturn("dummy-token");
//         when(jwtProvider.getUserIdFromToken("dummy-token")).thenReturn(1L);
//     }

//     @Test
//     void testHandleSubscribe() throws Exception {
//         // Given: action="subscribe" のJSONメッセージ
//         String payload = "{\"action\":\"subscribe\",\"channel_id\":10}";
//         TextMessage message = new TextMessage(payload);

//         // When
//         webSocketHandler.handleTextMessage(session, message);

//         // Then: UserSessionRegistry の subscribe が正しく呼ばれること
//         verify(sessionRegistry, times(1)).subscribe(1L, 10L);
//     }

//     @Test
//     void testHandleUnsubscribe() throws Exception {
//         // Given: action="unsubscribe" のJSONメッセージ
//         String payload = "{\"action\":\"unsubscribe\",\"channel_id\":10}";
//         TextMessage message = new TextMessage(payload);

//         // When
//         webSocketHandler.handleTextMessage(session, message);

//         // Then: UserSessionRegistry の unsubscribe が正しく呼ばれること
//         verify(sessionRegistry, times(1)).unsubscribe(1L, 10L);
//     }

//     @Test
//     void testHandlePing() throws Exception {
//         // Given: action="ping" のJSONメッセージ
//         String payload = "{\"action\":\"ping\",\"timestamp\":1710000000000}";
//         TextMessage message = new TextMessage(payload);

//         // When
//         webSocketHandler.handleTextMessage(session, message);

//         // Then: WebSocketEventPublisher の sendPong が正しく呼ばれること
//         verify(eventPublisher, times(1)).sendPong(eq(1L), eq(1710000000000L));
//     }

//     @Test
//     void testHandleSync() throws Exception {
//         // Given: action="sync" のJSONメッセージ
//         String payload = "{\"action\":\"sync\",\"channelIds\":[10],\"lastMessageId\":500}";
//         TextMessage message = new TextMessage(payload);

//         // When
//         webSocketHandler.handleTextMessage(session, message);

//         // Then: SyncService の sync が正しく呼ばれること
//         verify(syncService, times(1)).sync(eq(1L), argThat(req -> 
//             req.channelIds().contains(10L) && req.lastMessageId() == 500L
//         ));
//     }
// }