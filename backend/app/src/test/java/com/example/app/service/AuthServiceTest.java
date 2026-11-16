package com.example.app.service;

import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.user.UserEntity;
import com.example.app.domain.user.UserMapper;
import com.example.app.dto.login.LoginRequestDTO;
import com.example.app.dto.login.LoginResponseDTO;
import com.example.app.dto.user.UserResponseDTO;
import com.example.app.exception.UnauthorizedException;
import com.example.app.security.AuthUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private PasswordEncoder passwordEncoder;
    private JwtProvider jwtProvider;
    private UserMapper userMapper;
    private OperationLogService operationLogService;
    private AuthUtil authUtil;
    private AuthService authService;

    private HttpServletResponse response;
    private HttpServletRequest request;

    @BeforeEach
    void setup() {
        passwordEncoder = mock(PasswordEncoder.class);
        jwtProvider = mock(JwtProvider.class);
        userMapper = mock(UserMapper.class);
        operationLogService = mock(OperationLogService.class);
        authUtil = mock(AuthUtil.class);

        authService = new AuthService(
                passwordEncoder,
                jwtProvider,
                userMapper,
                operationLogService,
                authUtil
        );

        response = mock(HttpServletResponse.class);
        request = mock(HttpServletRequest.class);
    }

    // ---------------------------------------------------------
    // 1. login()
    // ---------------------------------------------------------

    @Test
    @DisplayName("1-1 正常ログイン")
    void testLogin_normal() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("alice");
        user.setPasswordHash("hashed");
        user.setRole(0);
        user.setActive(true);

        when(userMapper.findByUsername("alice")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(jwtProvider.generateToken(user)).thenReturn("abc");

        LoginRequestDTO req = new LoginRequestDTO("alice", "pass");

        LoginResponseDTO dto = authService.login(req, response);

        assertThat(dto.getUsername()).isEqualTo("alice");
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), contains("token=abc"));
        verify(operationLogService).record(
                eq(user),
                eq(OperationType.login),
                anyList(),
                anyList()
        );
    }

    @Test
    @DisplayName("1-2 ユーザー不存在 → UnauthorizedException")
    void testLogin_userNotFound() {
        when(userMapper.findByUsername("unknown")).thenReturn(java.util.Optional.empty());

        LoginRequestDTO req = new LoginRequestDTO("unknown", "pass");

        assertThatThrownBy(() -> authService.login(req, response))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("ユーザーが存在しません");
    }

    @Test
    @DisplayName("1-3 パスワード不一致 → UnauthorizedException")
    void testLogin_invalidPassword() {
        UserEntity user = new UserEntity();
        user.setUsername("alice");
        user.setPasswordHash("hashed");

        when(userMapper.findByUsername("alice")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        LoginRequestDTO req = new LoginRequestDTO("alice", "wrong");

        assertThatThrownBy(() -> authService.login(req, response))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("パスワードが違います");
    }

    @Test
    @DisplayName("1-4 JWT 生成成功 → Cookie に token=abc")
    void testLogin_jwtGenerated() {
        UserEntity user = new UserEntity();
        user.setUsername("alice");
        user.setPasswordHash("hashed");

        when(userMapper.findByUsername("alice")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(jwtProvider.generateToken(user)).thenReturn("abc");

        LoginRequestDTO req = new LoginRequestDTO("alice", "pass");

        authService.login(req, response);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), contains("token=abc"));
    }

    @Test
    @DisplayName("1-5 Cookie 設定確認")
    void testLogin_cookieSettings() {
        UserEntity user = new UserEntity();
        user.setUsername("alice");
        user.setPasswordHash("hashed");

        when(userMapper.findByUsername("alice")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(jwtProvider.generateToken(user)).thenReturn("abc");

        LoginRequestDTO req = new LoginRequestDTO("alice", "pass");

        authService.login(req, response);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE),
                argThat(cookie -> cookie.contains("HttpOnly") && cookie.contains("Path=/")));
    }

    @Test
    @DisplayName("1-6 username=null → UnauthorizedException")
    void testLogin_usernameNull() {
        LoginRequestDTO req = new LoginRequestDTO(null, "pass");

        assertThatThrownBy(() -> authService.login(req, response))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("1-7 password=null → UnauthorizedException")
    void testLogin_passwordNull() {
        LoginRequestDTO req = new LoginRequestDTO("alice", null);

        assertThatThrownBy(() -> authService.login(req, response))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ---------------------------------------------------------
    // 2. logout()
    // ---------------------------------------------------------

    @Test
    @DisplayName("2-1 正常ログアウト")
    void testLogout_normal() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("alice");

        when(authUtil.getTokenFromCookie(request)).thenReturn("validToken");
        when(authUtil.getUserFromToken("validToken")).thenReturn(user);

        Map<String, String> result = authService.logout(request, response);

        assertThat(result.get("message")).isEqualTo("Logged out");

        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());
        assertThat(captor.getValue().getMaxAge()).isZero();
    }

    @Test
    @DisplayName("2-2 token が存在しない → UnauthorizedException")
    void testLogout_noToken() {
        when(authUtil.getTokenFromCookie(request)).thenThrow(new UnauthorizedException("Missing token"));

        assertThatThrownBy(() -> authService.logout(request, response))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("2-3 token が不正 → JWT parse error")
    void testLogout_invalidToken() {
        when(authUtil.getTokenFromCookie(request)).thenReturn("invalid");
        when(authUtil.getUserFromToken("invalid")).thenThrow(new RuntimeException("JWT parse error"));

        assertThatThrownBy(() -> authService.logout(request, response))
                .hasMessageContaining("JWT parse error");
    }

    // ---------------------------------------------------------
    // 3. getCurrentUserDTO()
    // ---------------------------------------------------------

    @Test
    @DisplayName("3-1 正常取得")
    void testGetCurrentUserDTO_normal() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("alice");
        user.setRole(0);
        user.setActive(true);

        when(userMapper.findByUsername("alice")).thenReturn(java.util.Optional.of(user));

        UserResponseDTO dto = authService.getCurrentUserDTO("alice");

        assertThat(dto.username()).isEqualTo("alice");
    }

    @Test
    @DisplayName("3-2 ユーザー不存在 → RuntimeException")
    void testGetCurrentUserDTO_notFound() {
        when(userMapper.findByUsername("unknown")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUserDTO("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("3-3 username=null → RuntimeException")
    void testGetCurrentUserDTO_null() {
        assertThatThrownBy(() -> authService.getCurrentUserDTO(null))
                .isInstanceOf(RuntimeException.class);
    }
}
