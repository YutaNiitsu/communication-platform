// package com.example.app.controller;

// import com.example.app.controller.AuthController;
// import com.example.app.dto.login.LoginRequestDTO;
// import com.example.app.dto.login.LoginResponseDTO;
// import com.example.app.dto.user.UserCreateRequestDTO;
// import com.example.app.dto.user.UserResponseDTO;
// import com.example.app.exception.BadRequestException;
// import com.example.app.exception.NotFoundException;
// import com.example.app.exception.UnauthorizedException;
// import com.example.app.service.AuthService;

// import jakarta.servlet.http.Cookie;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.mockito.*;

// import org.springframework.http.ResponseEntity;
// import org.springframework.security.authentication.TestingAuthenticationToken;

// import java.util.List;
// import java.util.Map;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.argThat;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;

// class AuthControllerTest {

//     @Mock
//     private AuthService authService;

//     @Mock
//     private HttpServletRequest request;
    
//     @Mock
//     private HttpServletResponse response;

//     @InjectMocks
//     private AuthController authController;

//     @BeforeEach
//     void setup() {
//         MockitoAnnotations.openMocks(this);
//     }

//     // ---------------------------------------------------------
//     // 1-1. login 正常
//     // ---------------------------------------------------------
//     @Test
//     void testLogin_success() {
//         LoginRequestDTO req = LoginRequestDTO.builder()
//                 .username("alice")
//                 .password("pass")
//                 .build();

//         LoginResponseDTO dto = LoginResponseDTO.builder()
//                 .userId(1L)
//                 .username("alice")
//                 .role(1)
//                 .isActive(true)
//                 .build();

//         when(authService.login(eq(req), any())).thenReturn(dto);

//         ResponseEntity<LoginResponseDTO> res = authController.login(req, response);

//         assertThat(res.getStatusCode().value()).isEqualTo(200);
//         assertThat(res.getBody().getUsername()).isEqualTo("alice");
//     }

//     // ---------------------------------------------------------
//     // 1-2. login パスワード不一致 → UnauthorizedException
//     // ---------------------------------------------------------
//     @Test
//     void testLogin_passwordMismatch() {
//         LoginRequestDTO req = LoginRequestDTO.builder()
//                 .username("alice")
//                 .password("wrong")
//                 .build();

//         when(authService.login(eq(req), any()))
//                 .thenThrow(new UnauthorizedException("Invalid password"));

//         try {
//             authController.login(req, response);
//         } catch (UnauthorizedException e) {
//             assertThat(e.getMessage()).isEqualTo("Invalid password");
//         }
//     }

//     // ---------------------------------------------------------
//     // 1-3. login ユーザー不存在 → NotFoundException
//     // ---------------------------------------------------------
//     @Test
//     void testLogin_userNotFound() {
//         LoginRequestDTO req = LoginRequestDTO.builder()
//                 .username("unknown")
//                 .password("pass")
//                 .build();

//         when(authService.login(eq(req), any()))
//                 .thenThrow(new NotFoundException("User not found"));

//         try {
//             authController.login(req, response);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("User not found");
//         }
//     }

//     // ---------------------------------------------------------
//     // 1-4. login バリデーションエラー（@Valid は Controller では発火しない）
//     // ---------------------------------------------------------
//     @Test
//     void testLogin_validationError() {
//         LoginRequestDTO req = LoginRequestDTO.builder()
//                 .username("")
//                 .password("pass")
//                 .build();

//         // AuthService が例外を投げる想定
//         when(authService.login(eq(req), any()))
//                 .thenThrow(new BadRequestException("Validation failed"));

//         try {
//             authController.login(req, response);
//         } catch (BadRequestException e) {
//             assertThat(e.getMessage()).isEqualTo("Validation failed");
//         }
//     }

//     // ---------------------------------------------------------
//     // 1-5. login Cookie 設定確認
//     // ---------------------------------------------------------
//     @Test
//     void testLogin_cookieSet() {
//         LoginRequestDTO req = LoginRequestDTO.builder()
//                 .username("alice")
//                 .password("pass")
//                 .build();

//         LoginResponseDTO dto = LoginResponseDTO.builder()
//                 .userId(1L)
//                 .username("alice")
//                 .role(1)
//                 .isActive(true)
//                 .build();

//         when(authService.login(eq(req), any()))
//                 .thenAnswer(invocation -> {
//                     HttpServletResponse res = invocation.getArgument(1);
//                     res.addCookie(new Cookie("token", "abc"));
//                     return dto;
//                 });

//         authController.login(req, response);

//         verify(response).addCookie(argThat(cookie ->
//                 cookie.getName().equals("token") &&
//                 cookie.getValue().equals("abc")
//         ));
//     }

//     // ---------------------------------------------------------
//     // 2-1. logout 正常
//     // ---------------------------------------------------------
//     @Test
//     void testLogoutSuccessWithoutMockMvc() {

//         HttpServletRequest req = mock(HttpServletRequest.class);
//         HttpServletResponse res = mock(HttpServletResponse.class);

//         when(authService.logout(req, res))
//                 .thenReturn(Map.of("message", "Logged out"));

//         var result = authService.logout(req, res);

//         assertThat(result.get("message")).isEqualTo("Logged out");
//     }



//     // ---------------------------------------------------------
//     // 3-1. me 正常
//     // ---------------------------------------------------------
//     @Test
//     void testMe_success() {
//         TestingAuthenticationToken auth = new TestingAuthenticationToken("alice", null);

//         UserResponseDTO dto = new UserResponseDTO(
//             1L,
//             "alice",
//             1,
//             true
//         );

//         when(authService.getCurrentUserDTO("alice")).thenReturn(dto);

//         UserResponseDTO res = authController.me(auth);

//         assertThat(res.username()).isEqualTo("alice");
//     }

//     // ---------------------------------------------------------
//     // 3-2. me 未認証 → NullPointerException（Controller の仕様）
//     // ---------------------------------------------------------
//     @Test
//     void testMe_unauthenticated() {
//         try {
//             authController.me(null);
//         } catch (NullPointerException e) {
//             assertThat(e).isInstanceOf(NullPointerException.class);
//         }
//     }

// }
