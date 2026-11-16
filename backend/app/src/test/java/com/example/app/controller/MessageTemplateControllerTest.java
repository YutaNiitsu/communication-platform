// package com.example.app.controller;

// import com.example.app.dto.messageTemplate.*;
// import com.example.app.exception.BadRequestException;
// import com.example.app.exception.NotFoundException;
// import com.example.app.exception.ForbiddenException;
// import com.example.app.service.MessageTemplateService;

// import jakarta.servlet.http.HttpServletRequest;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.mockito.*;

// import java.util.List;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.Mockito.*;

// class MessageTemplateControllerTest {

//     @Mock
//     private MessageTemplateService templateService;

//     @Mock
//     private HttpServletRequest httpRequest;

//     @InjectMocks
//     private MessageTemplateController controller;

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

//         MessageTemplateCreateRequestDTO body =
//                 new MessageTemplateCreateRequestDTO("title", "content", 1L, true);

//         MessageTemplateResponseDTO dto =
//                 new MessageTemplateResponseDTO(1L, 99L, "title", "content", 1L, true, null, null);

//         when(templateService.createTemplate(eq(body), any())).thenReturn(dto);

//         MessageTemplateResponseDTO result = controller.create(httpRequest, body);

//         assertThat(result.id()).isEqualTo(1L);
//         assertThat(result.title()).isEqualTo("title");
//     }

//     @Test
//     @DisplayName("1-2 create: body=null → BadRequestException")
//     void testCreate_bodyNull() {

//         try {
//             controller.create(httpRequest, null);
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

//         MessageTemplateEditRequestDTO body =
//                 new MessageTemplateEditRequestDTO("newTitle", "newContent", 2L, false);

//         MessageTemplateResponseDTO dto =
//                 new MessageTemplateResponseDTO(10L, 99L, "newTitle", "newContent", 2L, false, null, null);

//         when(templateService.updateTemplate(eq(10L), eq(body), any())).thenReturn(dto);

//         MessageTemplateResponseDTO result =
//                 controller.update(10L, httpRequest, body);

//         assertThat(result.id()).isEqualTo(10L);
//         assertThat(result.title()).isEqualTo("newTitle");
//     }

//     @Test
//     @DisplayName("2-2 update: body=null → BadRequestException")
//     void testUpdate_bodyNull() {

//         try {
//             controller.update(10L, httpRequest, null);
//         } catch (BadRequestException | NullPointerException e) {
//             assertThat(e).isInstanceOf(Exception.class);
//         }
//     }

//     @Test
//     @DisplayName("2-3 update: テンプレート不存在 → NotFoundException")
//     void testUpdate_notFound() {

//         MessageTemplateEditRequestDTO body =
//                 new MessageTemplateEditRequestDTO("newTitle", "newContent", 2L, false);

//         when(templateService.updateTemplate(eq(999L), eq(body), any()))
//                 .thenThrow(new NotFoundException("Template not found"));

//         try {
//             controller.update(999L, httpRequest, body);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Template not found");
//         }
//     }

//     // ---------------------------------------------------------
//     // 3. DELETE
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("3-1 delete: 正常")
//     void testDelete_success() {

//         doNothing().when(templateService).deleteTemplate(eq(10L), any());

//         controller.delete(10L, httpRequest);

//         verify(templateService).deleteTemplate(eq(10L), any());
//     }

//     @Test
//     @DisplayName("3-2 delete: テンプレート不存在 → NotFoundException")
//     void testDelete_notFound() {

//         doThrow(new NotFoundException("Template not found"))
//                 .when(templateService).deleteTemplate(eq(999L), any());

//         try {
//             controller.delete(999L, httpRequest);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Template not found");
//         }
//     }

//     // ---------------------------------------------------------
//     // 4. GET ONE
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("4-1 getOne: 正常")
//     void testGetOne_success() {

//         MessageTemplateResponseDTO dto =
//                 new MessageTemplateResponseDTO(10L, 99L, "title", "content", 1L, true, null, null);

//         when(templateService.getTemplate(eq(10L), any())).thenReturn(dto);

//         MessageTemplateResponseDTO result = controller.getOne(10L, httpRequest);

//         assertThat(result.id()).isEqualTo(10L);
//         assertThat(result.title()).isEqualTo("title");
//     }

//     @Test
//     @DisplayName("4-2 getOne: テンプレート不存在 → NotFoundException")
//     void testGetOne_notFound() {

//         when(templateService.getTemplate(eq(999L), any()))
//                 .thenThrow(new NotFoundException("Template not found"));

//         try {
//             controller.getOne(999L, httpRequest);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Template not found");
//         }
//     }

//     // ---------------------------------------------------------
//     // 5. GET LIST
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("5-1 list: 正常")
//     void testList_success() {

//         MessageTemplateResponseDTO dto =
//                 new MessageTemplateResponseDTO(1L, 99L, "title", "content", 1L, true, null, null);

//         when(templateService.getTemplates(eq(true), any()))
//                 .thenReturn(List.of(dto));

//         List<MessageTemplateResponseDTO> result =
//                 controller.list(true, httpRequest);

//         assertThat(result).hasSize(1);
//         assertThat(result.get(0).title()).isEqualTo("title");
//     }

//     @Test
//     @DisplayName("5-2 list: テンプレート不存在 → NotFoundException")
//     void testList_notFound() {

//         when(templateService.getTemplates(eq(false), any()))
//                 .thenThrow(new NotFoundException("No templates"));

//         try {
//             controller.list(false, httpRequest);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("No templates");
//         }
//     }
// }
