package com.example.app.security;
// package com.example.app.security;

// import com.example.app.exception.InvalidJwtException;
// import com.example.app.security.JwtUtil;

// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.security.Keys;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.test.util.ReflectionTestUtils;

// import java.nio.charset.StandardCharsets;
// import java.security.Key;
// import java.time.Instant;
// import java.util.Date;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertThrows;

// class JwtUtilTest {

//     private JwtUtil jwtUtil;
//     private final String TEST_SECRET = "my-super-secret-key-256bit-long-1234";
//     private Key key;

//     @BeforeEach
//     void setup() {
//         jwtUtil = new JwtUtil();
// 		ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
//         key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
//     }

//     private String createJwt(Object userId, Date exp, Key signingKey) {
//         return Jwts.builder()
//                 .claim("user_id", userId)
//                 .setExpiration(exp)
//                 .signWith(signingKey, SignatureAlgorithm.HS256)
//                 .compact();
//     }

//     // ---------------------------------------------------------
//     // 1. 正常な JWT の user_id 抽出
//     // ---------------------------------------------------------
//     @Test
//     void testExtractUserId_success() {
//         String jwt = createJwt(100L, Date.from(Instant.now().plusSeconds(3600)), key);

//         Long result = jwtUtil.extractUserId(jwt);

//         assertThat(result).isEqualTo(100L);
//     }

//     // ---------------------------------------------------------
//     // 2. 改ざん JWT（署名不正）
//     // ---------------------------------------------------------
//     @Test
//     void testExtractUserId_tamperedSignature() {
//         Key otherKey = Keys.hmacShaKeyFor("differentSecretKeyForTampering123456".getBytes(StandardCharsets.UTF_8));
//         String jwt = createJwt(100L, Date.from(Instant.now().plusSeconds(3600)), otherKey);

//         InvalidJwtException ex = assertThrows(InvalidJwtException.class, () -> jwtUtil.extractUserId(jwt));
//         assertThat(ex.getMessage()).isEqualTo("JWT signature validation failed");
//     }

//     // ---------------------------------------------------------
//     // 3. 期限切れ JWT
//     // ---------------------------------------------------------
//     @Test
//     void testExtractUserId_expired() {
//         String jwt = createJwt(100L, Date.from(Instant.now().minusSeconds(3600)), key);

//         InvalidJwtException ex = assertThrows(InvalidJwtException.class, () -> jwtUtil.extractUserId(jwt));
//         assertThat(ex.getMessage()).isEqualTo("JWT is expired");
//     }

//     // ---------------------------------------------------------
//     // 4. 秘密鍵が違う JWT
//     // ---------------------------------------------------------
//     @Test
//     void testExtractUserId_wrongSecret() {
//         Key wrongKey = Keys.hmacShaKeyFor("wrongSecretKeyForTesting123456789".getBytes(StandardCharsets.UTF_8));
//         String jwt = createJwt(100L, Date.from(Instant.now().plusSeconds(3600)), wrongKey);

//         InvalidJwtException ex = assertThrows(InvalidJwtException.class, () -> jwtUtil.extractUserId(jwt));
//         assertThat(ex.getMessage()).isEqualTo("JWT signature validation failed");
//     }

//     // ---------------------------------------------------------
//     // 5. JWT 形式不正（ただの文字列）
//     // ---------------------------------------------------------
//     @Test
//     void testExtractUserId_malformed() {
//         InvalidJwtException ex = assertThrows(InvalidJwtException.class, () -> jwtUtil.extractUserId("abc"));
//         assertThat(ex.getMessage()).isEqualTo("JWT is malformed");
//     }

//     // ---------------------------------------------------------
//     // 6. user_id が存在しない
//     // ---------------------------------------------------------
//     @Test
//     void testExtractUserId_missingUserId() {
//         String jwt = Jwts.builder()
//                 .setExpiration(Date.from(Instant.now().plusSeconds(3600)))
//                 .signWith(key, SignatureAlgorithm.HS256)
//                 .compact();

//         InvalidJwtException ex = assertThrows(InvalidJwtException.class, () -> jwtUtil.extractUserId(jwt));
//         assertThat(ex.getMessage()).isEqualTo("JWT does not contain user_id");
//     }

//     // ---------------------------------------------------------
//     // 7. user_id が Number 以外（String）
//     // ---------------------------------------------------------
//     @Test
//     void testExtractUserId_userIdNotLong() {
//         String jwt = createJwt("abc", Date.from(Instant.now().plusSeconds(3600)), key);

//         InvalidJwtException ex = assertThrows(InvalidJwtException.class, () -> jwtUtil.extractUserId(jwt));
//         assertThat(ex.getMessage()).isEqualTo("user_id must be a number");
//     }

//     // ---------------------------------------------------------
//     // 8. token=null
//     // ---------------------------------------------------------
//     @Test
//     void testExtractUserId_nullToken() {
//         InvalidJwtException ex = assertThrows(InvalidJwtException.class, () -> jwtUtil.extractUserId(null));
//         assertThat(ex.getMessage()).isEqualTo("JWT token is null or empty");
//     }

//     // ---------------------------------------------------------
//     // 9. token=""（空文字）
//     // ---------------------------------------------------------
//     @Test
//     void testExtractUserId_emptyToken() {
//         InvalidJwtException ex = assertThrows(InvalidJwtException.class, () -> jwtUtil.extractUserId(""));
//         assertThat(ex.getMessage()).isEqualTo("JWT token is null or empty");
//     }

//     // ---------------------------------------------------------
//     // 10. token=" "（空白）
//     // ---------------------------------------------------------
//     @Test
//     void testExtractUserId_blankToken() {
//         InvalidJwtException ex = assertThrows(InvalidJwtException.class, () -> jwtUtil.extractUserId(" "));
//         assertThat(ex.getMessage()).isEqualTo("JWT token is null or empty");
//     }
// }
