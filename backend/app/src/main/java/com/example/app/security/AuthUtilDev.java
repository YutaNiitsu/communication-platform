package com.example.app.security;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.app.domain.user.UserEntity;
import com.example.app.domain.user.UserMapper;
import com.example.app.service.JwtProvider;

import jakarta.servlet.http.HttpServletRequest;

@Component
@Profile("dev")
@Primary
public class AuthUtilDev extends AuthUtil {

    public AuthUtilDev(JwtProvider jwtProvider, UserMapper userMapper) {
        super(jwtProvider, userMapper);
    }

    @Override
    public String getTokenFromCookie(HttpServletRequest request){
        return "dev-token"; // 開発時は固定のトークンを返す
    }

    @Override
    public Long getUserIdFromToken(String token) {
        return 1L; // 開発時は常に userId=1 として扱う
    }

    @Override
    public UserEntity getUserFromToken(String token) {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("dev-user");
        user.setRole(0); // admin 権限
        user.setActive(true);
        return user;
    }
}
