// package com.example.app.controller;

// import com.example.app.dto.search.SearchRequestDTO;
// import com.example.app.dto.search.SearchResponseDTO;
// import com.example.app.exception.BadRequestException;

// import com.example.app.exception.NotFoundException;
// import com.example.app.service.SearchService;

// import jakarta.servlet.http.HttpServletRequest;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.mockito.*;
// import org.springframework.web.client.HttpServerErrorException.InternalServerError;

// import java.time.LocalDate;
// import java.util.List;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;

// class SearchControllerTest {

//     @Mock
//     private SearchService searchService;

//     @Mock
//     private HttpServletRequest httpRequest;

//     @InjectMocks
//     private SearchController controller;

//     @BeforeEach
//     void setup() {
//         MockitoAnnotations.openMocks(this);
//     }

//     // ---------------------------------------------------------
//     // 1. 正常：全パラメータ指定
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("1-1 search: 全パラメータ指定 → 正常")
//     void testSearch_success() {

//         SearchResponseDTO dto =
//                 new SearchResponseDTO(1L, 10L, 99L, null, "hello", 0, null, null);

//         when(searchService.search(any(), any()))
//                 .thenReturn(List.of(dto));

//         List<SearchResponseDTO> result = controller.search(
//                 "hello",
//                 1L,
//                 10L,
//                 LocalDate.of(2024, 1, 1),
//                 LocalDate.of(2024, 1, 31),
//                 httpRequest
//         );

//         assertThat(result).hasSize(1);
//         assertThat(result.get(0).content()).isEqualTo("hello");

//         ArgumentCaptor<SearchRequestDTO> captor = ArgumentCaptor.forClass(SearchRequestDTO.class);
//         verify(searchService).search(captor.capture(), eq(httpRequest));

//         SearchRequestDTO req = captor.getValue();
//         assertThat(req.keyword()).isEqualTo("hello");
//         assertThat(req.channelId()).isEqualTo(1L);
//         assertThat(req.userId()).isEqualTo(10L);
//         assertThat(req.from()).isEqualTo(LocalDate.of(2024, 1, 1));
//         assertThat(req.to()).isEqualTo(LocalDate.of(2024, 1, 31));
//     }

//     // ---------------------------------------------------------
//     // 2. 異常：request_user_id 未指定 → BadRequestException
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("2-1 search: request_user_id 未指定 → BadRequestException")
//     void testSearch_missingRequestUserId() {

//         when(searchService.search(any(), any()))
//                 .thenThrow(new BadRequestException("request_user_id is required"));

//         try {
//             controller.search(
//                     "hello",
//                     null,
//                     null,
//                     null,
//                     null,
//                     httpRequest
//             );
//         } catch (BadRequestException e) {
//             assertThat(e.getMessage()).isEqualTo("request_user_id is required");
//         }
//     }

//     // ---------------------------------------------------------
//     // 3. 異常：ユーザー不存在 → NotFoundException
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("3-1 search: ユーザー不存在 → NotFoundException")
//     void testSearch_userNotFound() {

//         when(searchService.search(any(), any()))
//                 .thenThrow(new NotFoundException("User not found"));

//         try {
//             controller.search(
//                     null,
//                     null,
//                     null,
//                     null,
//                     null,
//                     httpRequest
//             );
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("User not found");
//         }
//     }

//     // ---------------------------------------------------------
//     // 4. 異常：検索結果 0 件 → NotFoundException
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("4-1 search: 検索結果 0 件 → NotFoundException")
//     void testSearch_noResults() {

//         when(searchService.search(any(), any()))
//                 .thenThrow(new NotFoundException("No results"));

//         try {
//             controller.search(
//                     "none",
//                     null,
//                     null,
//                     null,
//                     null,
//                     httpRequest
//             );
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("No results");
//         }
//     }

//     // ---------------------------------------------------------
//     // 5. 異常：バリデーションエラー → BadRequestException
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("5-1 search: バリデーションエラー → BadRequestException")
//     void testSearch_validationError() {

//         when(searchService.search(any(), any()))
//                 .thenThrow(new BadRequestException("Invalid keyword"));

//         try {
//             controller.search(
//                     "",
//                     null,
//                     null,
//                     null,
//                     null,
//                     httpRequest
//             );
//         } catch (BadRequestException e) {
//             assertThat(e.getMessage()).isEqualTo("Invalid keyword");
//         }
//     }

//     // ---------------------------------------------------------
//     // 6. 異常：内部エラー → InternalServerException
//     // ---------------------------------------------------------
//     @Test
//     @DisplayName("6-1 search: 内部エラー → InternalServerException")
//     void testSearch_internalError() {

//         when(searchService.search(any(), any()))
//                 .thenThrow(new RuntimeException("DB failure"));

//         try {
//             controller.search(
//                     "hello",
//                     null,
//                     null,
//                     null,
//                     null,
//                     httpRequest
//             );
//         } catch (RuntimeException e) {
//             assertThat(e.getMessage()).isEqualTo("DB failure");
//         }
//     }
// }
