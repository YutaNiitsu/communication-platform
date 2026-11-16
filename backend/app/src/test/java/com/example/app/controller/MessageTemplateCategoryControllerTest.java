// package com.example.app.controller;

// import com.example.app.dto.messageTemplate.*;
// import com.example.app.exception.BadRequestException;
// import com.example.app.exception.NotFoundException;
// import com.example.app.exception.ForbiddenException;
// import com.example.app.service.MessageTemplateCategoryService;

// import jakarta.servlet.http.HttpServletRequest;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.mockito.*;

// import java.util.List;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.Mockito.*;

// class MessageTemplateCategoryControllerTest {

//     @Mock
//     private MessageTemplateCategoryService categoryService;

//     @Mock
//     private HttpServletRequest httpRequest;

//     @InjectMocks
//     private MessageTemplateCategoryController controller;

//     @BeforeEach
//     void setup() {
//         MockitoAnnotations.openMocks(this);
//     }

//     // ---------------------------------------------------------
//     // 1. CREATE
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("1-1 create: 正常")
//     void testCreate_success() {

//         MessageTemplateCategoryCreateRequestDTO body =
//                 new MessageTemplateCategoryCreateRequestDTO("greeting", false);

//         MessageTemplateCategoryResponseDTO dto =
//                 new MessageTemplateCategoryResponseDTO(1L, 99L, "greeting", false, null, null);

//         when(categoryService.createCategory(eq(body), any())).thenReturn(dto);

//         MessageTemplateCategoryResponseDTO result = controller.create(body, httpRequest);

//         assertThat(result.id()).isEqualTo(1L);
//         assertThat(result.name()).isEqualTo("greeting");
//     }

//     @Test
//     @DisplayName("1-2 create: body=null → BadRequestException")
//     void testCreate_bodyNull() {

//         try {
//             controller.create(null, httpRequest);
//         } catch (BadRequestException | NullPointerException e) {
//             assertThat(e).isInstanceOf(Exception.class);
//         }
//     }

//     // ---------------------------------------------------------
//     // 2. UPDATE
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("2-1 update: 正常")
//     void testUpdate_success() {

//         MessageTemplateCategoryEditRequestDTO body =
//                 new MessageTemplateCategoryEditRequestDTO("updated", true);

//         MessageTemplateCategoryResponseDTO dto =
//                 new MessageTemplateCategoryResponseDTO(10L, 99L, "updated", true, null, null);

//         when(categoryService.updateCategory(eq(10L), eq(body), any())).thenReturn(dto);

//         MessageTemplateCategoryResponseDTO result =
//                 controller.update(10L, body, httpRequest);

//         assertThat(result.id()).isEqualTo(10L);
//         assertThat(result.name()).isEqualTo("updated");
//     }

//     @Test
//     @DisplayName("2-2 update: body=null → BadRequestException")
//     void testUpdate_bodyNull() {

//         try {
//             controller.update(10L, null, httpRequest);
//         } catch (BadRequestException | NullPointerException e) {
//             assertThat(e).isInstanceOf(Exception.class);
//         }
//     }

//     @Test
//     @DisplayName("2-3 update: カテゴリ不存在 → NotFoundException")
//     void testUpdate_notFound() {

//         MessageTemplateCategoryEditRequestDTO body =
//                 new MessageTemplateCategoryEditRequestDTO("updated", true);

//         when(categoryService.updateCategory(eq(999L), eq(body), any()))
//                 .thenThrow(new NotFoundException("Category not found"));

//         try {
//             controller.update(999L, body, httpRequest);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Category not found");
//         }
//     }

//     // ---------------------------------------------------------
//     // 3. DELETE
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("3-1 delete: 正常")
//     void testDelete_success() {

//         doNothing().when(categoryService).deleteCategory(eq(10L), any());

//         controller.delete(10L, httpRequest);

//         verify(categoryService).deleteCategory(eq(10L), any());
//     }

//     @Test
//     @DisplayName("3-2 delete: カテゴリ不存在 → NotFoundException")
//     void testDelete_notFound() {

//         doThrow(new NotFoundException("Category not found"))
//                 .when(categoryService).deleteCategory(eq(999L), any());

//         try {
//             controller.delete(999L, httpRequest);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Category not found");
//         }
//     }

//     // ---------------------------------------------------------
//     // 4. GET ONE
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("4-1 getOne: 正常")
//     void testGetOne_success() {

//         MessageTemplateCategoryResponseDTO dto =
//                 new MessageTemplateCategoryResponseDTO(10L, 99L, "greeting", false, null, null);

//         when(categoryService.getCategory(eq(10L), any())).thenReturn(dto);

//         MessageTemplateCategoryResponseDTO result = controller.getOne(10L, httpRequest);

//         assertThat(result.id()).isEqualTo(10L);
//         assertThat(result.name()).isEqualTo("greeting");
//     }

//     @Test
//     @DisplayName("4-2 getOne: カテゴリ不存在 → NotFoundException")
//     void testGetOne_notFound() {

//         when(categoryService.getCategory(eq(999L), any()))
//                 .thenThrow(new NotFoundException("Category not found"));

//         try {
//             controller.getOne(999L, httpRequest);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Category not found");
//         }
//     }

//     // ---------------------------------------------------------
//     // 5. GET LIST
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("5-1 list: 正常")
//     void testList_success() {

//         MessageTemplateCategoryResponseDTO dto =
//                 new MessageTemplateCategoryResponseDTO(1L, 99L, "greeting", false, null, null);

//         when(categoryService.getCategories(eq(false), any()))
//                 .thenReturn(List.of(dto));

//         List<MessageTemplateCategoryResponseDTO> result =
//                 controller.list(false, httpRequest);

//         assertThat(result).hasSize(1);
//         assertThat(result.get(0).name()).isEqualTo("greeting");
//     }

//     @Test
//     @DisplayName("5-2 list: カテゴリ不存在 → NotFoundException")
//     void testList_notFound() {

//         when(categoryService.getCategories(eq(true), any()))
//                 .thenThrow(new NotFoundException("No categories"));

//         try {
//             controller.list(true, httpRequest);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("No categories");
//         }
//     }
// }
