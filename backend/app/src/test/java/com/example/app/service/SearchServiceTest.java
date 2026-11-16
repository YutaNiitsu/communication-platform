// package com.example.app.service;

// import com.example.app.domain.message.MessageMapper;
// import com.example.app.domain.operationLog.OperationType;
// import com.example.app.domain.user.UserEntity;
// import com.example.app.dto.message.MessageDTO;
// import com.example.app.dto.search.SearchRequestDTO;
// import com.example.app.dto.search.SearchResponseDTO;
// import com.example.app.exception.BadRequestException;
// import com.example.app.exception.NotFoundException;
// import com.example.app.security.AuthUtil;

// import jakarta.servlet.http.HttpServletRequest;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;

// import static org.assertj.core.api.Assertions.*;
// import static org.mockito.Mockito.*;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.List;

// class SearchServiceTest {

//     private JwtProvider jwtProvider;
//     private OperationLogService operationLogService;
//     private MessageMapper messageMapper;
//     private AuthUtil authUtil;
//     private SearchService searchService;
//     private HttpServletRequest request;

//     @BeforeEach
//     void setup() {
//         jwtProvider = mock(JwtProvider.class);
//         operationLogService = mock(OperationLogService.class);
//         messageMapper = mock(MessageMapper.class);
//         authUtil = mock(AuthUtil.class);

//         searchService = new SearchService(jwtProvider, operationLogService, messageMapper, authUtil);
//         request = mock(HttpServletRequest.class);
//     }

//     // ---------------------------------------------------------
//     // 1. 正常：ユーザー存在
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("1-1: 正常（ユーザー存在）")
//     void testSearch_normal() {

//         // token → user
//         when(jwtProvider.extractTokenFromCookie(request)).thenReturn("token");
//         UserEntity user = new UserEntity();
//         user.setId(1L);
//         when(authUtil.getUserFromToken("token")).thenReturn(user);

//         // 検索結果
//         MessageDTO m = new MessageDTO(
//                 10L, 1L, 1L, null, "hello", 0,
//                 LocalDateTime.now(), LocalDateTime.now()
//         );
//         when(messageMapper.searchMessages(any(), any(), any(), any(), any()))
//                 .thenReturn(List.of(m));

//         SearchRequestDTO req = new SearchRequestDTO("hello", 1L, 1L, null, null);

//         List<SearchResponseDTO> result = searchService.search(req, request);

//         assertThat(result).hasSize(1);
//         assertThat(result.get(0).content()).isEqualTo("hello");

//         verify(operationLogService).record(eq(user), eq(OperationType.search), anyList(), anyList());
//     }

//     // ---------------------------------------------------------
//     // 2. 異常：ユーザー不存在
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("1-2: 異常（ユーザー不存在）")
//     void testSearch_userNotFound() {

//         when(jwtProvider.extractTokenFromCookie(request)).thenReturn("token");
//         when(authUtil.getUserFromToken("token"))
//                 .thenThrow(new NotFoundException("User not found"));

//         SearchRequestDTO req = new SearchRequestDTO("hello", 1L, 999L, null, null);

//         assertThatThrownBy(() -> searchService.search(req, request))
//                 .isInstanceOf(NotFoundException.class)
//                 .hasMessageContaining("User not found");
//     }

//     // ---------------------------------------------------------
//     // 3. 異常：req=null
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("1-3: 異常（req=null）")
//     void testSearch_reqNull() {

//         when(jwtProvider.extractTokenFromCookie(request)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenReturn(new UserEntity());

//         assertThatThrownBy(() -> searchService.search(null, request))
//                 .isInstanceOf(BadRequestException.class)
//                 .hasMessageContaining("Search request is required");
//     }

//     // ---------------------------------------------------------
//     // 4. 異常：keyword=""
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("1-4: 異常（keyword=\"\"）")
//     void testSearch_keywordBlank() {

//         when(jwtProvider.extractTokenFromCookie(request)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenReturn(new UserEntity());

//         SearchRequestDTO req = new SearchRequestDTO("", 1L, 1L, null, null);

//         assertThatThrownBy(() -> searchService.search(req, request))
//                 .isInstanceOf(BadRequestException.class)
//                 .hasMessageContaining("Keyword must not be blank");
//     }

//     // ---------------------------------------------------------
//     // 5. 異常：from > to
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("1-5: 異常（from > to）")
//     void testSearch_invalidDateRange() {

//         when(jwtProvider.extractTokenFromCookie(request)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenReturn(new UserEntity());

//         LocalDate from = LocalDate.of(2024, 2, 1);
//         LocalDate to = LocalDate.of(2024, 1, 1);

//         SearchRequestDTO req = new SearchRequestDTO("hello", 1L, 1L, from, to);

//         assertThatThrownBy(() -> searchService.search(req, request))
//                 .isInstanceOf(BadRequestException.class)
//                 .hasMessageContaining("Invalid date range: from > to");
//     }

//     // ---------------------------------------------------------
//     // 6. 異常：検索結果0件
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("1-6: 異常（検索結果0件）")
//     void testSearch_noResults() {

//         when(jwtProvider.extractTokenFromCookie(request)).thenReturn("token");
//         when(authUtil.getUserFromToken("token")).thenReturn(new UserEntity());

//         when(messageMapper.searchMessages(any(), any(), any(), any(), any()))
//                 .thenReturn(List.of()); // 0件

//         SearchRequestDTO req = new SearchRequestDTO("none", 1L, 1L, null, null);

//         assertThatThrownBy(() -> searchService.search(req, request))
//                 .isInstanceOf(NotFoundException.class)
//                 .hasMessageContaining("No messages found");
//     }
// }
