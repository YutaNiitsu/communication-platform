package com.example.app.service;

import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.user.UserEntity;
import com.example.app.domain.user.UserMapper;
import com.example.app.dto.user.UserCreateRequestDTO;
import com.example.app.dto.user.UserUpdateRequestDTO;
import com.example.app.dto.user.UserResponseDTO;
import com.example.app.exception.ConflictException;
import com.example.app.exception.NotFoundException;
import com.example.app.security.AuthUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;
    private final OperationLogService operationLogService;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtil authUtil;
    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");

    public UserResponseDTO getUser(Long id, HttpServletRequest request) {
        UserEntity user = userMapper.findById(id)
                .orElseGet(() -> {
                    appLog.warn("User not found (user_id={})", id);
                    throw new NotFoundException("User not found");
                });

        return toDTO(user);
    }

    public List<UserResponseDTO> getAllUsers(HttpServletRequest request) {
        return userMapper.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public UserResponseDTO createUser(UserCreateRequestDTO dto, HttpServletRequest request) {
        if (dto == null) {
            appLog.warn("Request body is required");
            throw new IllegalArgumentException("Request body is required");
        }
        if (dto.username() == null || dto.username().isBlank()) {
            appLog.warn("Username is required");
            throw new IllegalArgumentException("Username is required");
        }
        if (dto.password() == null || dto.password().isBlank()) {
            appLog.warn("Password is required");
            throw new IllegalArgumentException("Password is required");
        }

        String token = jwtProvider.extractTokenFromCookie(request);
        UserEntity requestUser = authUtil.getUserFromToken(token);
        authUtil.requireAdmin(requestUser);

        if (userMapper.existsByUsername(dto.username())) {
            appLog.warn("Username already exists");
            throw new ConflictException("Username already exists");
        }

        UserEntity user = new UserEntity();
        user.setUsername(dto.username());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRole(dto.role());
        user.setActive(dto.isActive());

        userMapper.insert(user);

        operationLogService.record(
            requestUser,
            OperationType.user_created,
            List.of("user_id=" + user.getId()),
            List.of(
                "username=" + user.getUsername(),
                "role=" + user.getRole(),
                "active=" + user.isActive()
            )
        );

        return toDTO(user);
    }


    public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO dto, HttpServletRequest request) {
        if (dto == null) {
            appLog.warn("Request body is required");
            throw new IllegalArgumentException("Request body is required");
        }

        String token = jwtProvider.extractTokenFromCookie(request);
        UserEntity requestUser = authUtil.getUserFromToken(token);
        authUtil.requireAdmin(requestUser);

        UserEntity user = userMapper.findById(id)
                .orElseGet(() -> {
                    appLog.warn("User not found (user_id={})", id);
                    throw new NotFoundException("User not found");
                });

        List<String> result = new ArrayList<>();
        String oldName = user.getUsername();
        int oldRole = user.getRole();
        boolean oldActive = user.isActive();

        if (userMapper.existsByUsername(dto.username())) {
            appLog.warn("Username already exists");
            throw new ConflictException("Username already exists");
        }

        if (dto.username() != null && !dto.username().isBlank() && !dto.username().equals(oldName)) {
            user.setUsername(dto.username());
            result.add("username=old:" + oldName + ",new:" + dto.username());
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.password()));
            result.add("password=updated");
        }

        if (dto.role() != oldRole) {
            user.setRole(dto.role());
            result.add("role=old:" + oldRole + ",new:" + dto.role());
        }

        if (dto.isActive() != null && dto.isActive() != oldActive) {
            user.setActive(dto.isActive());
            result.add("is_active=old:" + oldActive + ",new:" + dto.isActive());
        }

        userMapper.update(user);

        operationLogService.record(
            requestUser,
            OperationType.user_updated,
            List.of("user_id=" + user.getId()),
            result
        );

        return toDTO(user);
    }


    public String deleteUser(Long id, HttpServletRequest request) {
        String token = jwtProvider.extractTokenFromCookie(request);
        UserEntity requestUser = authUtil.getUserFromToken(token);
        authUtil.requireAdmin(requestUser);

        UserEntity user = userMapper.findById(id)
                .orElseGet(() -> {
                    appLog.warn("User not found (user_id={})", id);
                    throw new NotFoundException("User not found");
                });

        // 操作ログ記録
        operationLogService.record(
            requestUser,
            OperationType.user_deleted,
            List.of("user_id=" + user.getId(), "username=" + user.getUsername()),
            List.of("ok")
        );

        userMapper.deleteById(user.getId());

        return "ok";
    }

    private UserResponseDTO toDTO(UserEntity u) {
        return new UserResponseDTO(
                u.getId(),
                u.getUsername(),
                u.getRole(),
                u.isActive()
        );
    }
}
