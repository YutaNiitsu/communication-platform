package com.example.app.service;

import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.user.UserEntity;
import com.example.app.domain.user.UserMapper;
import com.example.app.dto.user.*;
import com.example.app.exception.ConflictException;
import com.example.app.exception.NotFoundException;
import com.example.app.security.AuthUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

class UserServiceTest {

    private JwtProvider jwtProvider;
    private UserMapper userMapper;
    private OperationLogService operationLogService;
    private PasswordEncoder passwordEncoder;
    private AuthUtil authUtil;
    private HttpServletRequest httpReq;

    private UserService service;

    @BeforeEach
    void setup() {
        jwtProvider = mock(JwtProvider.class);
        userMapper = mock(UserMapper.class);
        operationLogService = mock(OperationLogService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authUtil = mock(AuthUtil.class);
        httpReq = mock(HttpServletRequest.class);

        service = new UserService(jwtProvider, userMapper, operationLogService, passwordEncoder, authUtil);

        lenient().when(jwtProvider.extractTokenFromCookie(httpReq)).thenReturn("token");
    }

    // ---------------------------------------------------------
    // 1. getUser (2 ケース)
    // ---------------------------------------------------------

    @Test
    @DisplayName("1-1: getUser - 正常")
    void testGetUser_normal() {
        UserEntity u = new UserEntity();
        u.setId(10L);
        u.setUsername("alice");
        u.setRole(1);
        u.setActive(true);

        when(userMapper.findById(10L)).thenReturn(Optional.of(u));

        UserResponseDTO res = service.getUser(10L, httpReq);

        assertThat(res.userId()).isEqualTo(10L);
        assertThat(res.username()).isEqualTo("alice");
    }

    @Test
    @DisplayName("1-2: getUser - ユーザー不存在 → NotFoundException")
    void testGetUser_notFound() {
        when(userMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUser(999L, httpReq))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // ---------------------------------------------------------
    // 2. getAllUsers (2 ケース)
    // ---------------------------------------------------------

    @Test
    @DisplayName("2-1: getAllUsers - 正常（複数）")
    void testGetAllUsers_normal() {
        UserEntity u1 = new UserEntity();
        u1.setId(1L);
        u1.setUsername("alice");

        UserEntity u2 = new UserEntity();
        u2.setId(2L);
        u2.setUsername("bob");

        when(userMapper.findAll()).thenReturn(List.of(u1, u2));

        List<UserResponseDTO> res = service.getAllUsers(httpReq);

        assertThat(res).hasSize(2);
    }

    @Test
    @DisplayName("2-2: getAllUsers - 空リスト")
    void testGetAllUsers_empty() {
        when(userMapper.findAll()).thenReturn(List.of());

        List<UserResponseDTO> res = service.getAllUsers(httpReq);

        assertThat(res).isEmpty();
    }

    // ---------------------------------------------------------
    // 3. createUser (6 ケース)
    // ---------------------------------------------------------

    @Test
    @DisplayName("3-1: createUser - 正常（管理者）")
    void testCreateUser_normal() {

        UserEntity admin = new UserEntity();
        admin.setId(99L);
        admin.setRole(0);
        when(authUtil.getUserFromToken("token")).thenReturn(admin);

        UserCreateRequestDTO dto = new UserCreateRequestDTO("alice", "pass", 1, true);

        when(userMapper.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hashed");

        doAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            u.setId(10L);
            return null;
        }).when(userMapper).insert(any());

        UserResponseDTO res = service.createUser(dto, httpReq);

        assertThat(res.userId()).isEqualTo(10L);
        assertThat(res.username()).isEqualTo("alice");
    }

    @Test
    @DisplayName("3-2: createUser - dto=null → IllegalArgumentException")
    void testCreateUser_dtoNull() {
        assertThatThrownBy(() -> service.createUser(null, httpReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Request body is required");
    }

    @Test
    @DisplayName("3-3: createUser - username=null → IllegalArgumentException")
    void testCreateUser_usernameNull() {
        UserCreateRequestDTO dto = new UserCreateRequestDTO(null, "pass", 1, true);

        assertThatThrownBy(() -> service.createUser(dto, httpReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username is required");
    }

    @Test
    @DisplayName("3-4: createUser - password=null → IllegalArgumentException")
    void testCreateUser_passwordNull() {
        UserCreateRequestDTO dto = new UserCreateRequestDTO("alice", null, 1, true);

        assertThatThrownBy(() -> service.createUser(dto, httpReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password is required");
    }

    @Test
    @DisplayName("3-5: createUser - username 重複 → ConflictException")
    void testCreateUser_usernameExists() {

        UserEntity admin = new UserEntity();
        admin.setId(99L);
        admin.setRole(0);
        when(authUtil.getUserFromToken("token")).thenReturn(admin);

        UserCreateRequestDTO dto = new UserCreateRequestDTO("alice", "pass", 1, true);

        when(userMapper.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(dto, httpReq))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    @DisplayName("3-6: createUser - 管理者以外禁止 → ForbiddenException")
    void testCreateUser_notAdmin() {

        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setRole(1);
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        UserCreateRequestDTO dto = new UserCreateRequestDTO("alice", "pass", 1, true);

        doThrow(new com.example.app.exception.ForbiddenException("Access denied"))
                .when(authUtil).requireAdmin(user);

        assertThatThrownBy(() -> service.createUser(dto, httpReq))
                .isInstanceOf(com.example.app.exception.ForbiddenException.class)
                .hasMessageContaining("Access denied");
    }

    // ---------------------------------------------------------
    // 4. updateUser (5 ケース)
    // ---------------------------------------------------------

    @Test
    @DisplayName("4-1: updateUser - 正常（管理者）")
    void testUpdateUser_normal() {

        UserEntity admin = new UserEntity();
        admin.setId(99L);
        admin.setRole(0);
        when(authUtil.getUserFromToken("token")).thenReturn(admin);

        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("old");
        user.setRole(1);
        user.setActive(true);

        when(userMapper.findById(10L)).thenReturn(Optional.of(user));
        when(userMapper.existsByUsername("new")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hashed");

        UserUpdateRequestDTO dto = new UserUpdateRequestDTO("new", "pass", 2, false);

        UserResponseDTO res = service.updateUser(10L, dto, httpReq);

        assertThat(res.username()).isEqualTo("new");
        assertThat(res.role()).isEqualTo(2);
        assertThat(res.isActive()).isFalse();
    }

    @Test
    @DisplayName("4-2: updateUser - dto=null → IllegalArgumentException")
    void testUpdateUser_dtoNull() {
        assertThatThrownBy(() -> service.updateUser(10L, null, httpReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Request body is required");
    }

    @Test
    @DisplayName("4-3: updateUser - 管理者以外禁止 → ForbiddenException")
    void testUpdateUser_notAdmin() {

        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setRole(1);
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        UserUpdateRequestDTO dto = new UserUpdateRequestDTO("new", "pass", 1, true);

        doThrow(new com.example.app.exception.ForbiddenException("Access denied"))
                .when(authUtil).requireAdmin(user);

        assertThatThrownBy(() -> service.updateUser(10L, dto, httpReq))
                .isInstanceOf(com.example.app.exception.ForbiddenException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    @DisplayName("4-4: updateUser - ユーザー不存在 → NotFoundException")
    void testUpdateUser_notFound() {

        UserEntity admin = new UserEntity();
        admin.setId(99L);
        admin.setRole(0);
        when(authUtil.getUserFromToken("token")).thenReturn(admin);

        when(userMapper.findById(999L)).thenReturn(Optional.empty());

        UserUpdateRequestDTO dto = new UserUpdateRequestDTO("new", "pass", 1, true);

        assertThatThrownBy(() -> service.updateUser(999L, dto, httpReq))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("4-5: updateUser - username 重複 → ConflictException")
    void testUpdateUser_usernameExists() {

        UserEntity admin = new UserEntity();
        admin.setId(99L);
        admin.setRole(0);
        when(authUtil.getUserFromToken("token")).thenReturn(admin);

        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("old");
        user.setRole(1);
        user.setActive(true);

        when(userMapper.findById(10L)).thenReturn(Optional.of(user));
        when(userMapper.existsByUsername("new")).thenReturn(true);

        UserUpdateRequestDTO dto = new UserUpdateRequestDTO("new", "pass", 1, true);

        assertThatThrownBy(() -> service.updateUser(10L, dto, httpReq))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username already exists");
    }

    // ---------------------------------------------------------
    // 5. deleteUser (2 ケース)
    // ---------------------------------------------------------

    @Test
    @DisplayName("5-1: deleteUser - 正常（管理者）")
    void testDeleteUser_normal() {

        UserEntity admin = new UserEntity();
        admin.setId(99L);
        admin.setRole(0);
        when(authUtil.getUserFromToken("token")).thenReturn(admin);

        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("alice");

        when(userMapper.findById(10L)).thenReturn(Optional.of(user));

        String res = service.deleteUser(10L, httpReq);

        assertThat(res).isEqualTo("ok");
        verify(userMapper).deleteById(10L);
    }

    @Test
    @DisplayName("5-2: deleteUser - ユーザー不存在 → NotFoundException")
    void testDeleteUser_notFound() {

        UserEntity admin = new UserEntity();
        admin.setId(99L);
        admin.setRole(0);
        when(authUtil.getUserFromToken("token")).thenReturn(admin);

        when(userMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteUser(999L, httpReq))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
