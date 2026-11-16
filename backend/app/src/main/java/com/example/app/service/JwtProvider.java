package com.example.app.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.domain.user.UserEntity;
import com.example.app.exception.UnauthorizedException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtProvider {

    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");

    @Value("${jwt.secret}")
    private String secret;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 有効期限（例：1日）
    private final long EXPIRATION = 1000 * 60 * 60 * 24;

    public String generateToken(UserEntity user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("user_id", user.getId())  // ★ WebSocket で必要
                .claim("role", user.getRole())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getKey())
                .compact();
    }

    // トークンからユーザー名を取得
    public String getUsernameFromToken(String token) {
        if (token == null || token.isBlank()) {
            appLog.warn("Token is empty");
            throw new UnauthorizedException("Token is empty");
        }

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

        } catch (Exception e) {
            appLog.warn("Invalid or expired token");
            throw new UnauthorizedException("Invalid or expired token");
        }
    }

    // トークンからユーザーIDを取得
    public Long getUserIdFromToken(String token) {
        if (token == null || token.isBlank()) {
            appLog.warn("Token is empty");
            throw new UnauthorizedException("Token is empty");
        }

        try {
            
            Long userId = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("user_id", Long.class);

            if (userId == null) {
                appLog.warn("user_id claim missing");
                throw new UnauthorizedException("user_id claim missing");
            }

            return userId;

        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            appLog.warn("Invalid or expired token");
            throw new UnauthorizedException("Invalid or expired token");
        }
    }

    // トークンからユーザー権限を取得
    public int getRoleFromToken(String token) {
        if (token == null || token.isBlank()) {
            appLog.warn("Token is empty");
            throw new UnauthorizedException("Token is empty");
        }

        try {
            
            Integer role = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("role", Integer.class);

            if (role == null) {
                appLog.warn("role claim missing");
                throw new UnauthorizedException("role claim missing");
            }

            return role;

        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            appLog.warn("Invalid or expired token");
            throw new UnauthorizedException("Invalid or expired token");
        }
    }

    
    // HttpServletRequest からトークンを取得
    public String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            appLog.warn("Cookie not found");
            throw new UnauthorizedException("Cookie not found");
        }

        for (Cookie cookie : request.getCookies()) {
            if ("token".equals(cookie.getName())) {
                String value = cookie.getValue();
                if (value == null || value.isBlank()) {
                    appLog.warn("Authentication token is empty");
                    throw new UnauthorizedException("Authentication token is empty");
                }
                return value;
            }
        }

        appLog.warn("Authentication token not found");
        throw new UnauthorizedException("Authentication token not found");
    }


    // WebSocketSession からトークンを取得
    public String extractTokenFromCookie(WebSocketSession session) {

        List<String> cookieHeaders = session.getHandshakeHeaders().get("Cookie");

        // Cookie ヘッダー自体が無い
        if (cookieHeaders == null || cookieHeaders.isEmpty()) {
            appLog.warn("Cookie not found");
            throw new UnauthorizedException("Cookie not found");
        }

        // Cookie ヘッダーを走査
        for (String cookieHeader : cookieHeaders) {
            String[] cookies = cookieHeader.split("; ");

            for (String cookie : cookies) {

                // token= が存在するか
                if (cookie.startsWith("token=")) {
                    String value = cookie.substring("token=".length());

                    if (value == null || value.isBlank()) {
                        appLog.warn("Authentication token is empty");
                        throw new UnauthorizedException("Authentication token is empty");
                    }

                    return value;
                }
            }
        }

        // token が見つからなかった場合
        appLog.warn("Authentication token not found");
        throw new UnauthorizedException("Authentication token not found");
    }

}
