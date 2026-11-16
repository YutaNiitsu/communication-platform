package com.example.app.security;

import org.springframework.stereotype.Component;

import com.example.app.domain.user.UserEntity;
import com.example.app.domain.user.UserMapper;
import com.example.app.exception.ForbiddenException;
import com.example.app.exception.NotFoundException;
import com.example.app.exception.UnauthorizedException;
import com.example.app.service.JwtProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthUtil {
    protected final JwtProvider jwtProvider;
    protected final UserMapper userMapper;
    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");

    // ---------------------------------------------------------
    // 1. 認証（Authentication）
    // ---------------------------------------------------------

    public String getTokenFromCookie(HttpServletRequest request){
        return jwtProvider.extractTokenFromCookie(request);
    }

    /** JWT から userId を取り出す（検証込み） */
    public Long getUserIdFromToken(String token) {
        if (token == null || token.isBlank()) {
            appLog.warn("Token is missing or invalid");
            throw new UnauthorizedException("Missing token");
        }

        try {
            return jwtProvider.getUserIdFromToken(token);
        } catch (Exception e) {
            appLog.warn("Invalid token");
            throw new UnauthorizedException("Invalid token");
        }
    }

    /** JWT → UserEntity */
    public UserEntity getUserFromToken(String token) {
        Long userId = getUserIdFromToken(token);

        UserEntity user = userMapper.findById(userId)
            .orElseGet(() -> {
                    appLog.warn("User not found (user_id={})", userId);
                    throw new UnauthorizedException("User not found");
                });

        return user;
    }

    // ---------------------------------------------------------
    // 2. 認可（Authorization）
    // ---------------------------------------------------------

    /** 管理者のみ許可 */
    public void requireAdmin(UserEntity user) {
        if (user.getRole() != 0) {
            appLog.warn("Admin privilege required");
            throw new ForbiddenException("Admin privilege required");
        }
    }

    /** 本人のみ許可 */
    public void requireOwner(Long requestUserId, Long targetUserId) {
        if (!requestUserId.equals(targetUserId)) {
            appLog.warn("You are not the owner");
            throw new ForbiddenException("You are not the owner");
        }
    }

    /** 本人または管理者のみ許可 */
    public void requireOwnerOrAdmin(UserEntity user, Long targetUserId) {
        if (!user.getId().equals(targetUserId) && user.getRole() != 0) {
            appLog.warn("Owner or admin privilege required");
            throw new ForbiddenException("Owner or admin privilege required");
        }
    }

    /** チャンネル管理者チェック（必要なら拡張） */
    public void requireChannelAdmin(Long userId, Long channelAdminId) {
        if (!userId.equals(channelAdminId)) {
            appLog.warn("Channel admin privilege required");
            throw new ForbiddenException("Channel admin privilege required");
        }
    }

    // ---------------------------------------------------------
    // 3. 認証＋認可の組み合わせ
    // ---------------------------------------------------------

    /** token → UserEntity（認証）＋管理者チェック */
    public UserEntity authenticateAdmin(String token) {
        UserEntity user = getUserFromToken(token);
        requireAdmin(user);
        return user;
    }

    /** token → UserEntity（認証）＋本人チェック */
    public UserEntity authenticateOwner(String token, Long targetUserId) {
        UserEntity user = getUserFromToken(token);
        requireOwner(user.getId(), targetUserId);
        return user;
    }

    /** token → UserEntity（認証）＋本人 or 管理者チェック */
    public UserEntity authenticateOwnerOrAdmin(String token, Long targetUserId) {
        UserEntity user = getUserFromToken(token);
        requireOwnerOrAdmin(user, targetUserId);
        return user;
    }
}
