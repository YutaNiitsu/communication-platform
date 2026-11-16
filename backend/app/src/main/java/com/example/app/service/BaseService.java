// package com.example.app.service;

// import org.springframework.stereotype.Service;

// import com.example.app.domain.user.UserEntity;
// import com.example.app.domain.user.UserMapper;
// import com.example.app.exception.ForbiddenException;

// import jakarta.servlet.http.HttpServletRequest;
// import lombok.AccessLevel;
// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
// public class BaseService {
//     protected final JwtProvider jwtProvider;
//     protected final UserMapper userMapper;
//     protected final OperationLogService operationLogService;
    
//     protected String getToken(HttpServletRequest request){
//         return jwtProvider.extractTokenFromCookie(request);
//     }

//     protected UserEntity getRequestUser(String token){
//         Long UserId = jwtProvider.getUserIdFromToken(token);
//         UserEntity user = userMapper.findById(UserId)
//                 .orElseThrow(() -> new RuntimeException("User not found"));
//         return user;
//     }

//     protected void isAdmin(String token){
//         int role = jwtProvider.getRoleFromToken(token);
//         if (role != 0) {
//             throw new ForbiddenException("Access denied");
//         }
//     }
// }
