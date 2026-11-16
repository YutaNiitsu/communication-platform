// package com.example.app.service;

// import com.example.app.domain.user.UserEntity;
// import com.example.app.domain.user.UserMapper;
// import com.example.app.exception.ForbiddenException;
// import com.example.app.exception.NotFoundException;
// import com.example.app.exception.UnauthorizedException;

// import jakarta.servlet.http.Cookie;
// import jakarta.servlet.http.HttpServletRequest;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;

// import static org.assertj.core.api.Assertions.*;
// import static org.mockito.Mockito.*;

// import java.util.Optional;

// class BaseServiceTest {

//     private JwtProvider jwtProvider;
//     private UserMapper userMapper;
//     private OperationLogService operationLogService;

//     private BaseService baseService;

//     // ★ BaseService は abstract ではないが protected constructor のため
//     //   テスト用のダミーサブクラスを作る
//     static class TestBaseService extends BaseService {
//         public TestBaseService(JwtProvider jwtProvider, UserMapper userMapper, OperationLogService operationLogService) {
//             super(jwtProvider, userMapper, operationLogService);
//         }
//     }

//     @BeforeEach
//     void setup() {
//         jwtProvider = mock(JwtProvider.class);
//         userMapper = mock(UserMapper.class);
//         operationLogService = mock(OperationLogService.class);

//         baseService = new TestBaseService(jwtProvider, userMapper, operationLogService);
//     }

//     // ---------------------------------------------------------
//     // 1. getToken
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("1-1 getToken: 正常取得")
//     void testGetTokenSuccess() {

//         HttpServletRequest req = mock(HttpServletRequest.class);
//         Cookie cookie = new Cookie("token", "abc123");
//         when(req.getCookies()).thenReturn(new Cookie[]{cookie});

//         when(jwtProvider.extractTokenFromCookie(req)).thenReturn("abc123");

//         String token = baseService.getToken(req);

//         assertThat(token).isEqualTo("abc123");
//     }

//     @Test
//     @DisplayName("1-2 getToken: Cookie 無し → UnauthorizedException")
//     void testGetTokenNoCookie() {

//         HttpServletRequest req = mock(HttpServletRequest.class);
//         when(req.getCookies()).thenReturn(null);

//         when(jwtProvider.extractTokenFromCookie(req))
//                 .thenThrow(new UnauthorizedException("Cookie not found"));

//         assertThatThrownBy(() -> baseService.getToken(req))
//                 .isInstanceOf(UnauthorizedException.class)
//                 .hasMessageContaining("Cookie not found");
//     }

//     @Test
//     @DisplayName("1-3 getToken: token 無し → UnauthorizedException")
//     void testGetTokenMissingToken() {

//         HttpServletRequest req = mock(HttpServletRequest.class);
//         Cookie cookie = new Cookie("session", "xyz");
//         when(req.getCookies()).thenReturn(new Cookie[]{cookie});

//         when(jwtProvider.extractTokenFromCookie(req))
//                 .thenThrow(new UnauthorizedException("Authentication token not found"));

//         assertThatThrownBy(() -> baseService.getToken(req))
//                 .isInstanceOf(UnauthorizedException.class)
//                 .hasMessageContaining("Authentication token not found");
//     }

//     // ---------------------------------------------------------
//     // 2. getRequestUser
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("2-1 getRequestUser: 正常取得")
//     void testGetRequestUserSuccess() {

//         when(jwtProvider.getUserIdFromToken("valid")).thenReturn(10L);

//         UserEntity user = new UserEntity();
//         user.setId(10L);
//         user.setUsername("yuta");

//         when(userMapper.findById(10L)).thenReturn(Optional.of(user));

//         UserEntity result = baseService.getRequestUser("valid");

//         assertThat(result.getId()).isEqualTo(10L);
//         assertThat(result.getUsername()).isEqualTo("yuta");
//     }

//     @Test
//     @DisplayName("2-2 getRequestUser: ユーザー不存在 → NotFoundException")
//     void testGetRequestUserNotFound() {

//         when(jwtProvider.getUserIdFromToken("valid")).thenReturn(999L);
//         when(userMapper.findById(999L)).thenReturn(Optional.empty());

//         assertThatThrownBy(() -> baseService.getRequestUser("valid"))
//                 .isInstanceOf(NotFoundException.class)
//                 .hasMessageContaining("User not found");
//     }

//     @Test
//     @DisplayName("2-3 getRequestUser: トークン不正 → UnauthorizedException")
//     void testGetRequestUserInvalidToken() {

//         when(jwtProvider.getUserIdFromToken("invalid"))
//                 .thenThrow(new UnauthorizedException("Invalid or expired token"));

//         assertThatThrownBy(() -> baseService.getRequestUser("invalid"))
//                 .isInstanceOf(UnauthorizedException.class)
//                 .hasMessageContaining("Invalid or expired token");
//     }

//     @Test
//     @DisplayName("2-4 getRequestUser: userId claim 無し → UnauthorizedException")
//     void testGetRequestUserMissingUserIdClaim() {

//         when(jwtProvider.getUserIdFromToken("noUserId"))
//                 .thenThrow(new UnauthorizedException("user_id claim missing"));

//         assertThatThrownBy(() -> baseService.getRequestUser("noUserId"))
//                 .isInstanceOf(UnauthorizedException.class)
//                 .hasMessageContaining("user_id claim missing");
//     }

//     // ---------------------------------------------------------
//     // 3. isAdmin
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("3-1 isAdmin: role=0 → 正常（例外なし）")
//     void testIsAdminSuccess() {

//         when(jwtProvider.getRoleFromToken("admin")).thenReturn(0);

//         baseService.isAdmin("admin"); // 例外が出なければ成功
//     }

//     @Test
//     @DisplayName("3-2 isAdmin: role≠0 → ForbiddenException")
//     void testIsAdminForbidden() {

//         when(jwtProvider.getRoleFromToken("user")).thenReturn(1);

//         assertThatThrownBy(() -> baseService.isAdmin("user"))
//                 .isInstanceOf(ForbiddenException.class)
//                 .hasMessageContaining("Access denied");
//     }

//     @Test
//     @DisplayName("3-3 isAdmin: role claim 無し → UnauthorizedException")
//     void testIsAdminMissingRoleClaim() {

//         when(jwtProvider.getRoleFromToken("noRole"))
//                 .thenThrow(new UnauthorizedException("role claim missing"));

//         assertThatThrownBy(() -> baseService.isAdmin("noRole"))
//                 .isInstanceOf(UnauthorizedException.class)
//                 .hasMessageContaining("role claim missing");
//     }

//     @Test
//     @DisplayName("3-4 isAdmin: role=999 → ForbiddenException")
//     void testIsAdminInvalidRole() {

//         when(jwtProvider.getRoleFromToken("invalidRole")).thenReturn(999);

//         assertThatThrownBy(() -> baseService.isAdmin("invalidRole"))
//                 .isInstanceOf(ForbiddenException.class)
//                 .hasMessageContaining("Access denied");
//     }
// }
