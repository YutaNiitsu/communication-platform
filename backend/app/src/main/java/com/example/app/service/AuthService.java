package com.example.app.service;

import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.user.UserEntity;
import com.example.app.domain.user.UserMapper;
import com.example.app.dto.login.LoginRequestDTO;
import com.example.app.dto.login.LoginResponseDTO;
import com.example.app.dto.user.UserResponseDTO;
import com.example.app.exception.UnauthorizedException;
import com.example.app.security.AuthUtil;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;
    private final OperationLogService operationLogService;
    private final AuthUtil authUtil;
    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");

    public LoginResponseDTO login(LoginRequestDTO request, HttpServletResponse response) {

        UserEntity user = userMapper.findByUsername(request.getUsername())
            .orElseGet(() -> {
                appLog.warn("User not found (username={})", request.getUsername());
                throw new UnauthorizedException("ユーザーが存在しません: " + request.getUsername());
            });
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            appLog.warn("Invalid password for user (username={})", request.getUsername());
            throw new UnauthorizedException("パスワードが違います");
        }
        
        String token = jwtProvider.generateToken(user);

        // ★ ResponseCookie を使用して SameSite=Lax を明示的に設定（IPアドレスアクセス時の 403 回避）
        String cookieValue = ResponseCookie.from("token", token)
			.path("/")
			.httpOnly(true)
			.secure(false) 
			//.sameSite("None") // クロスオリジン間でのCookie自動送信を許可
			.maxAge(60 * 60 * 24) // 1日（24時間）
			.build()
            .toString() + "; SameSite=Lax";
        appLog.info("Set-Cookie: " + cookieValue); // デバッグ用にログ出力
        response.addHeader("Set-Cookie", cookieValue);

        // 操作ログ記録
        operationLogService.record(
            user,
            OperationType.login,
            List.of("user_id=" + user.getId(), "username=" + user.getUsername()),
            List.of("ok")
        );

        return LoginResponseDTO.builder()
			.userId(user.getId())
			.username(user.getUsername())
			.role(user.getRole())
			.isActive(user.isActive())
			.build();
    }

	public Map<String, String> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = authUtil.getTokenFromCookie(request);
        UserEntity user = authUtil.getUserFromToken(token);
        Cookie cookie = new Cookie("token", null);
		cookie.setMaxAge(0); // Cookie削除
		cookie.setPath("/");
		response.addCookie(cookie);

        // 操作ログ記録
        operationLogService.record(
            user,
            OperationType.login,
            List.of("user_id=" + user.getId(), "username=" + user.getUsername()),
            List.of("ok")
        );

		return Map.of("message", "Logged out");
	}

	public UserResponseDTO getCurrentUserDTO(String username) {

        UserEntity user = userMapper.findByUsername(username)
                .orElseGet(() -> {
                    appLog.warn("User not found (username={})", username);
                    throw new RuntimeException("User not found");
                });

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.isActive()
        );
    }

}
