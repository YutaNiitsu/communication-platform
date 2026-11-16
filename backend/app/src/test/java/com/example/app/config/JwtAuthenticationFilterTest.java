// package com.example.app.config;

// import com.example.app.config.JwtAuthenticationFilter;
// import com.example.app.service.JwtProvider;

// import jakarta.servlet.http.Cookie;

// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;
// import org.springframework.mock.web.MockFilterChain;
// import org.springframework.mock.web.MockHttpServletRequest;
// import org.springframework.mock.web.MockHttpServletResponse;
// import org.springframework.security.core.context.SecurityContextHolder;

// import static org.assertj.core.api.Assertions.assertThat;

// class JwtAuthenticationFilterTest {

//     private final JwtProvider jwtProvider = Mockito.mock(JwtProvider.class);
//     private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtProvider);

//     @AfterEach
//     void clearContext() {
//         SecurityContextHolder.clearContext();
//     }

//     // ---------------------------------------------------------
//     // 1. 有効 JWT → 認証成功
//     // ---------------------------------------------------------
//     @Test
//     void testJwtAuth_success() throws Exception {
//         MockHttpServletRequest request = new MockHttpServletRequest();
//         request.setCookies(new Cookie("token", "valid-token"));

//         Mockito.when(jwtProvider.getUsernameFromToken("valid-token"))
//                 .thenReturn("alice");

//         MockHttpServletResponse response = new MockHttpServletResponse();
//         MockFilterChain chain = new MockFilterChain();

//         filter.doFilter(request, response, chain);

//         var auth = SecurityContextHolder.getContext().getAuthentication();
//         assertThat(auth).isNotNull();
//         assertThat(auth.getName()).isEqualTo("alice");
//     }

//     // ---------------------------------------------------------
//     // 2. Cookie 無し → 認証失敗
//     // ---------------------------------------------------------
//     @Test
//     void testJwtAuth_noCookie() throws Exception {
//         MockHttpServletRequest request = new MockHttpServletRequest();
//         MockHttpServletResponse response = new MockHttpServletResponse();
//         MockFilterChain chain = new MockFilterChain();

//         filter.doFilter(request, response, chain);

//         assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
//     }

//     // ---------------------------------------------------------
//     // 3. JWT 期限切れ → 認証失敗
//     // ---------------------------------------------------------
//     @Test
//     void testJwtAuth_expired() throws Exception {
//         MockHttpServletRequest request = new MockHttpServletRequest();
//         request.setCookies(new Cookie("token", "expired-token"));

//         Mockito.when(jwtProvider.getUsernameFromToken("expired-token"))
//                 .thenThrow(new RuntimeException("expired"));

//         MockHttpServletResponse response = new MockHttpServletResponse();
//         MockFilterChain chain = new MockFilterChain();

//         filter.doFilter(request, response, chain);

//         assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
//     }

//     // ---------------------------------------------------------
//     // 4. JWT 改ざん → 認証失敗
//     // ---------------------------------------------------------
//     @Test
//     void testJwtAuth_invalidSignature() throws Exception {
//         MockHttpServletRequest request = new MockHttpServletRequest();
//         request.setCookies(new Cookie("token", "invalid-token"));

//         Mockito.when(jwtProvider.getUsernameFromToken("invalid-token"))
//                 .thenThrow(new RuntimeException("invalid signature"));

//         MockHttpServletResponse response = new MockHttpServletResponse();
//         MockFilterChain chain = new MockFilterChain();

//         filter.doFilter(request, response, chain);

//         assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
//     }

//     // ---------------------------------------------------------
//     // 5. SecurityContext がセットされるか
//     // ---------------------------------------------------------
//     @Test
//     void testJwtAuth_securityContextSet() throws Exception {
//         MockHttpServletRequest request = new MockHttpServletRequest();
//         request.setCookies(new Cookie("token", "valid-token"));

//         Mockito.when(jwtProvider.getUsernameFromToken("valid-token"))
//                 .thenReturn("alice");

//         MockHttpServletResponse response = new MockHttpServletResponse();
//         MockFilterChain chain = new MockFilterChain();

//         filter.doFilter(request, response, chain);

//         var auth = SecurityContextHolder.getContext().getAuthentication();
//         assertThat(auth).isNotNull();
//         assertThat(auth.getName()).isEqualTo("alice");
//     }

//     // ---------------------------------------------------------
//     // 6. authorities が emptyList であること
//     // ---------------------------------------------------------
//     @Test
//     void testJwtAuth_authoritiesEmpty() throws Exception {
//         MockHttpServletRequest request = new MockHttpServletRequest();
//         request.setCookies(new Cookie("token", "valid-token"));

//         Mockito.when(jwtProvider.getUsernameFromToken("valid-token"))
//                 .thenReturn("alice");

//         MockHttpServletResponse response = new MockHttpServletResponse();
//         MockFilterChain chain = new MockFilterChain();

//         filter.doFilter(request, response, chain);

//         var auth = SecurityContextHolder.getContext().getAuthentication();
//         assertThat(auth.getAuthorities()).isEmpty();
//     }

//     // ---------------------------------------------------------
//     // 7. フィルタチェーンが必ず実行される
//     // ---------------------------------------------------------
//     @Test
//     void testFilterChainAlwaysContinues() throws Exception {
//         MockHttpServletRequest request = new MockHttpServletRequest();
//         request.setCookies(new Cookie("token", "valid-token"));

//         Mockito.when(jwtProvider.getUsernameFromToken("valid-token"))
//                 .thenReturn("alice");

//         MockHttpServletResponse response = new MockHttpServletResponse();
//         MockFilterChain chain = Mockito.spy(new MockFilterChain());

//         filter.doFilter(request, response, chain);

//         Mockito.verify(chain, Mockito.times(1))
//                 .doFilter(request, response);
//     }
// }
