package com.example.app.service;

import com.example.app.domain.user.UserEntity;
import com.example.app.exception.UnauthorizedException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    private final String VALID_SECRET = "12345678901234567890123456789012"; // 32byte
    private final String SHORT_SECRET = "short";

    @BeforeEach
    void setup() {
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "secret", VALID_SECRET);
    }

    // ---------------------------------------------------------
    // 1. generateToken()
    // ---------------------------------------------------------

    @Test
    @DisplayName("1-1: トークン生成成功")
    void testGenerateToken_normal() {
        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("yuta");
        user.setRole(1);

        String token = jwtProvider.generateToken(user);

        assertThat(token).isNotNull();
        assertThat(jwtProvider.getUsernameFromToken(token)).isEqualTo("yuta");
        assertThat(jwtProvider.getUserIdFromToken(token)).isEqualTo(10L);
        assertThat(jwtProvider.getRoleFromToken(token)).isEqualTo(1);
    }

    @Test
    @DisplayName("1-2: secret が短すぎる → WeakKeyException")
    void testGenerateToken_weakSecret() {
        JwtProvider provider = new JwtProvider();
        ReflectionTestUtils.setField(provider, "secret", SHORT_SECRET);

        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("yuta");
        user.setRole(1);

        assertThatThrownBy(() -> provider.generateToken(user))
                .isInstanceOf(WeakKeyException.class);
    }

    // ---------------------------------------------------------
    // 2. getUsernameFromToken()
    // ---------------------------------------------------------

    @Test
    @DisplayName("2-1: subject 抽出成功")
    void testGetUsername_normal() {
        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("yuta");
        user.setRole(1);

        String token = jwtProvider.generateToken(user);

        assertThat(jwtProvider.getUsernameFromToken(token)).isEqualTo("yuta");
    }

    @Test
    @DisplayName("2-2: トークン改ざん → UnauthorizedException")
    void testGetUsername_tampered() {
        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("yuta");
        user.setRole(1);

        String token = jwtProvider.generateToken(user);
        String tampered = token.substring(0, token.length() - 5) + "xxxxx";

        assertThatThrownBy(() -> jwtProvider.getUsernameFromToken(tampered))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid or expired token");
    }

    @Test
    @DisplayName("2-3: 期限切れ → UnauthorizedException")
    void testGetUsername_expired() {
        Key key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));

        String expiredToken = Jwts.builder()
                .setSubject("yuta")
                .claim("user_id", 10)
                .claim("role", 1)
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> jwtProvider.getUsernameFromToken(expiredToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid or expired token");
    }

    @Test
    @DisplayName("2-4: token=null → UnauthorizedException")
    void testGetUsername_null() {
        assertThatThrownBy(() -> jwtProvider.getUsernameFromToken(null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Token is empty");
    }

    // ---------------------------------------------------------
    // 3. getUserIdFromToken()
    // ---------------------------------------------------------

    @Test
    @DisplayName("3-1: user_id 抽出成功")
    void testGetUserId_normal() {
        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("yuta");
        user.setRole(1);

        String token = jwtProvider.generateToken(user);

        assertThat(jwtProvider.getUserIdFromToken(token)).isEqualTo(10L);
    }

    @Test
    @DisplayName("3-2: user_id claim 欠落 → UnauthorizedException")
    void testGetUserId_missingClaim() {
        Key key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .setSubject("yuta")
                .claim("role", 1)
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> jwtProvider.getUserIdFromToken(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("user_id claim missing");
    }

    @Test
    @DisplayName("3-3: token=null → UnauthorizedException")
    void testGetUserId_null() {
        assertThatThrownBy(() -> jwtProvider.getUserIdFromToken(null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Token is empty");
    }

    @Test
    @DisplayName("3-4: トークン改ざん → UnauthorizedException")
    void testGetUserId_tampered() {
        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("yuta");
        user.setRole(1);

        String token = jwtProvider.generateToken(user);
        String tampered = token.substring(0, token.length() - 5) + "xxxxx";

        assertThatThrownBy(() -> jwtProvider.getUserIdFromToken(tampered))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ---------------------------------------------------------
    // 4. getRoleFromToken()
    // ---------------------------------------------------------

    @Test
    @DisplayName("4-1: role 抽出成功")
    void testGetRole_normal() {
        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("yuta");
        user.setRole(1);

        String token = jwtProvider.generateToken(user);

        assertThat(jwtProvider.getRoleFromToken(token)).isEqualTo(1);
    }

    @Test
    @DisplayName("4-2: role claim 欠落 → UnauthorizedException")
    void testGetRole_missingClaim() {
        Key key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .setSubject("yuta")
                .claim("user_id", 10)
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> jwtProvider.getRoleFromToken(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("role claim missing");
    }

    @Test
    @DisplayName("4-3: token=null → UnauthorizedException")
    void testGetRole_null() {
        assertThatThrownBy(() -> jwtProvider.getRoleFromToken(null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Token is empty");
    }

    @Test
    @DisplayName("4-4: トークン改ざん → UnauthorizedException")
    void testGetRole_tampered() {
        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("yuta");
        user.setRole(1);

        String token = jwtProvider.generateToken(user);
        String tampered = token.substring(0, token.length() - 5) + "xxxxx";

        assertThatThrownBy(() -> jwtProvider.getRoleFromToken(tampered))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ---------------------------------------------------------
    // 5. extractTokenFromCookie(HttpServletRequest)
    // ---------------------------------------------------------

    @Test
    @DisplayName("5-1: token 取得成功")
    void testExtractTokenFromCookie_normal() {
        Cookie cookie = new Cookie("token", "abc123");
        HttpServletRequest req = mock(HttpServletRequest.class);

        when(req.getCookies()).thenReturn(new Cookie[]{cookie});

        assertThat(jwtProvider.extractTokenFromCookie(req)).isEqualTo("abc123");
    }

    @Test
    @DisplayName("5-2: Cookie が無い → UnauthorizedException")
    void testExtractTokenFromCookie_noCookies() {
        HttpServletRequest req = mock(HttpServletRequest.class);

        when(req.getCookies()).thenReturn(null);

        assertThatThrownBy(() -> jwtProvider.extractTokenFromCookie(req))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Cookie not found");
    }

    @Test
    @DisplayName("5-3: token Cookie が無い → UnauthorizedException")
    void testExtractTokenFromCookie_noTokenCookie() {
        Cookie cookie = new Cookie("session", "xyz");
        HttpServletRequest req = mock(HttpServletRequest.class);

        when(req.getCookies()).thenReturn(new Cookie[]{cookie});

        assertThatThrownBy(() -> jwtProvider.extractTokenFromCookie(req))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Authentication token not found");
    }

    @Test
    @DisplayName("5-4: token が空文字 → UnauthorizedException")
    void testExtractTokenFromCookie_emptyToken() {
        Cookie cookie = new Cookie("token", "");
        HttpServletRequest req = mock(HttpServletRequest.class);

        when(req.getCookies()).thenReturn(new Cookie[]{cookie});

        assertThatThrownBy(() -> jwtProvider.extractTokenFromCookie(req))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Authentication token is empty");
    }

    // ---------------------------------------------------------
    // 6. extractTokenFromCookie(WebSocketSession)
    // ---------------------------------------------------------

    @Test
    @DisplayName("6-1: WebSocket Cookie 取得成功")
    void testExtractTokenFromWebSocket_normal() {
        WebSocketSession session = mock(WebSocketSession.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "token=abc123");

        when(session.getHandshakeHeaders()).thenReturn(headers);

        assertThat(jwtProvider.extractTokenFromCookie(session)).isEqualTo("abc123");
    }

    @Test
    @DisplayName("6-2: Cookie ヘッダーが無い → UnauthorizedException")
    void testExtractTokenFromWebSocket_noHeader() {
        WebSocketSession session = mock(WebSocketSession.class);

        HttpHeaders headers = new HttpHeaders(); // 空
        when(session.getHandshakeHeaders()).thenReturn(headers);

        assertThatThrownBy(() -> jwtProvider.extractTokenFromCookie(session))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Cookie not found");
    }

    @Test
    @DisplayName("6-3: token が無い → UnauthorizedException")
    void testExtractTokenFromWebSocket_noToken() {
        WebSocketSession session = mock(WebSocketSession.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "session=xyz");

        when(session.getHandshakeHeaders()).thenReturn(headers);

        assertThatThrownBy(() -> jwtProvider.extractTokenFromCookie(session))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Authentication token not found");
    }

    @Test
    @DisplayName("6-4: token が空文字 → UnauthorizedException")
    void testExtractTokenFromWebSocket_emptyToken() {
        WebSocketSession session = mock(WebSocketSession.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "token=");

        when(session.getHandshakeHeaders()).thenReturn(headers);

        assertThatThrownBy(() -> jwtProvider.extractTokenFromCookie(session))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Authentication token is empty");
    }
}
