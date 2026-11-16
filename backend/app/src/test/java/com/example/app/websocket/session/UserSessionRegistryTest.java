// package com.example.app.websocket.session;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.web.socket.WebSocketSession;

// import java.util.Set;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.Mockito.mock;

// class UserSessionRegistryTest {

//     private UserSessionRegistry registry;

//     @BeforeEach
//     void setUp() {
//         registry = new UserSessionRegistry();
//     }

//     @Test
//     void testRegister() {
//         // Given
//         Long userId = 1L;
//         WebSocketSession sessionA = mock(WebSocketSession.class);

//         // When
//         registry.register(userId, sessionA);

//         // Then: sessions[1] に sessionA が登録され、セッションが取得できること
//         assertThat(registry.getSession(userId)).isEqualTo(sessionA);
//         assertThat(registry.getSessions(userId)).contains(sessionA);
//     }

//     @Test
//     void testUnregisterLatestSession() {
//         // Given: 登録済み状態
//         Long userId = 1L;
//         WebSocketSession sessionA = mock(WebSocketSession.class);
//         registry.register(userId, sessionA);
//         registry.subscribe(userId, 10L); // 購読も追加しておく

//         // When: 一致するセッションでunregister呼び出し
//         registry.unregister(userId, sessionA);

//         // Then: sessions から削除され、subscriptions もクリアされること
//         assertThat(registry.getSession(userId)).isNull();
//         assertThat(registry.getSubscribedChannels(userId)).isEmpty();
//     }

//     @Test
//     void testOldSessionDoesNotUnregisterNewSession() {
//         // Given: sessionB（新）が登録されている状態に、古い sessionA（古）の切断イベントが来たケース
//         Long userId = 1L;
//         WebSocketSession sessionA = mock(WebSocketSession.class);
//         WebSocketSession sessionB = mock(WebSocketSession.class);

//         registry.register(userId, sessionA); // 古いセッション登録
//         registry.register(userId, sessionB); // 新しいセッション登録（複数接続）
//         registry.subscribe(userId, 10L);     // 購読保持

//         // When: 古いセッションの unregister が呼ばれる
//         registry.unregister(userId, sessionA);

//         // Then: セッションは sessionB のまま維持され、subscriptions も保持されること
//         assertThat(registry.getSession(userId)).isEqualTo(sessionB);
//         assertThat(registry.getSubscribedChannels(userId)).contains(10L);
//     }

//     @Test
//     void testSubscribe() {
//         // Given
//         Long userId = 1L;
//         registry.register(userId, mock(WebSocketSession.class));

//         // When
//         registry.subscribe(userId, 10L);

//         // Then: 購読セットに channelId (10) が追加されること
//         assertThat(registry.getSubscribedChannels(userId)).contains(10L);
//     }

//     @Test
//     void testUnsubscribe() {
//         // Given
//         Long userId = 1L;
//         registry.register(userId, mock(WebSocketSession.class));
//         registry.subscribe(userId, 10L);
//         registry.subscribe(userId, 20L); // 複数購読状態 {10, 20}

//         // When: channelId 10 を解除
//         registry.unsubscribe(userId, 10L);

//         // Then: 20 のみが残ること
//         Set<Long> channels = registry.getSubscribedChannels(userId);
//         assertThat(channels).containsExactly(20L);
//     }

//     @Test
//     void testGetUsersSubscribedTo() {
//         // Given: ユーザー1と2がチャンネルを購読している状態
//         registry.subscribe(1L, 10L);
//         registry.subscribe(2L, 10L);
//         registry.subscribe(2L, 20L);

//         // When: チャンネル10を購読しているユーザー一覧を取得
//         Set<Long> users = registry.getUsersSubscribedTo(10L);

//         // Then: ユーザー1と2が返されること
//         assertThat(users).containsExactlyInAnyOrder(1L, 2L);
//     }
// }