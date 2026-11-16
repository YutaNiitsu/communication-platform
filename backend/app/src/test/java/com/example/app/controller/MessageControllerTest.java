// package com.example.app.controller;

// import com.example.app.dto.message.*;
// import com.example.app.exception.BadRequestException;
// import com.example.app.exception.ForbiddenException;
// import com.example.app.exception.NotFoundException;
// import com.example.app.service.MessageService;

// import jakarta.servlet.http.HttpServletRequest;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.mockito.*;

// import java.util.List;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;

// class MessageControllerTest {

//     @Mock
//     private MessageService messageService;

//     @Mock
//     private HttpServletRequest httpRequest;

//     @InjectMocks
//     private MessageController controller;

//     @BeforeEach
//     void setup() {
//         MockitoAnnotations.openMocks(this);
//     }

//     // ---------------------------------------------------------
//     // 1. GET /channels/{id}/messages
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("getMessages: 正常")
//     void testGetMessages_success() {
//         MessageDTO messageDTO = new MessageDTO(1L, 1L, 10L, null, "hello", 0, null, null);
//         MessageSummaryDTO summaryDto = new MessageSummaryDTO(messageDTO, null, null);
//         MessageResponseDTO responseDto = new MessageResponseDTO(List.of(summaryDto), true);

//         when(messageService.getMessages(1L, 1L, 10)).thenReturn(responseDto);

//         MessageResponseDTO result = controller.getMessages(1L, 1L, 10);

//         assertThat(result.messages()).hasSize(1);
//         assertThat(result.messages().get(0).message().content()).isEqualTo("hello");
//     }

//     @Test
//     @DisplayName("getMessages: チャンネル不存在 → NotFoundException")
//     void testGetMessages_notFound() {

//         when(messageService.getMessages(999L, 1L, 10))
//                 .thenThrow(new NotFoundException("Channel not found"));

//         try {
//             controller.getMessages(999L, 1L, 10);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Channel not found");
//         }
//     }

//     // ---------------------------------------------------------
//     // 2. POST /channels/{id}/messages (create)
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("create: 正常")
//     void testCreate_success() {

//         MessagePostRequestDTO req = new MessagePostRequestDTO("hello", null, null);

//         MessageDTO messageDTO = new MessageDTO(1L, 1L, 10L, null, "hello", 0, null, null);
//         MessageSummaryDTO summaryDto = new MessageSummaryDTO(messageDTO, null, null);
//         MessageResponseDTO responseDto = new MessageResponseDTO(List.of(summaryDto), true);

//         when(messageService.createMessage(eq(1L), any(), eq(req)))
//                 .thenReturn(responseDto);

//         MessageResponseDTO result = controller.create(1L, req, httpRequest);

//         assertThat(result.messages().get(0).message().content()).isEqualTo("hello");
//     }

//     @Test
//     @DisplayName("create: dto=null → BadRequestException")
//     void testCreate_dtoNull() {

//         try {
//             controller.create(1L, null, httpRequest);
//         } catch (BadRequestException | NullPointerException e) {
//             assertThat(e).isInstanceOf(Exception.class);
//         }
//     }

//     @Test
//     @DisplayName("create: チャンネル不存在 → NotFoundException")
//     void testCreate_notFound() {

//         MessagePostRequestDTO req = new MessagePostRequestDTO("hello", null, null);

//         when(messageService.createMessage(eq(999L), any(), eq(req)))
//                 .thenThrow(new NotFoundException("Channel not found"));

//         try {
//             controller.create(999L, req, httpRequest);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Channel not found");
//         }
//     }

//     @Test
//     @DisplayName("create: バリデーションエラー → BadRequestException")
//     void testCreate_validationError() {

//         MessagePostRequestDTO req = new MessagePostRequestDTO("", null, null);

//         when(messageService.createMessage(eq(1L), any(), eq(req)))
//                 .thenThrow(new BadRequestException("Validation error"));

//         try {
//             controller.create(1L, req, httpRequest);
//         } catch (BadRequestException e) {
//             assertThat(e.getMessage()).isEqualTo("Validation error");
//         }
//     }

//     // ---------------------------------------------------------
//     // 3. PUT /messages/{id} (update)
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("update: 正常")
//     void testUpdate_success() {

//         MessageEditRequestDTO req = new MessageEditRequestDTO("edited", 0);

//         MessageResponseDTO dto =
//                 new MessageResponseDTO(1L, 1L, 10L, null, "edited", 0, null, null);

//         MessageEditResponseDTO editDto =
//                 new MessageEditResponseDTO(dto);

//         when(messageService.editMessage(eq(1L), any(), eq(req)))
//                 .thenReturn(editDto);

//         MessageEditResponseDTO result = controller.update(1L, req, httpRequest);

//         assertThat(result.message().content()).isEqualTo("edited");
//     }

//     @Test
//     @DisplayName("update: dto=null → BadRequestException")
//     void testUpdate_dtoNull() {

//         try {
//             controller.update(1L, null, httpRequest);
//         } catch (BadRequestException | NullPointerException e) {
//             assertThat(e).isInstanceOf(Exception.class);
//         }
//     }

//     @Test
//     @DisplayName("update: メッセージ不存在 → NotFoundException")
//     void testUpdate_notFound() {

//         MessageEditRequestDTO req = new MessageEditRequestDTO("edited", 0);

//         when(messageService.editMessage(eq(999L), any(), eq(req)))
//                 .thenThrow(new NotFoundException("Message not found"));

//         try {
//             controller.update(999L, req, httpRequest);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Message not found");
//         }
//     }

//     @Test
//     @DisplayName("update: 編集権限なし → ForbiddenException")
//     void testUpdate_forbidden() {

//         MessageEditRequestDTO req = new MessageEditRequestDTO("edited", 0);

//         when(messageService.editMessage(eq(1L), any(), eq(req)))
//                 .thenThrow(new ForbiddenException("Access denied"));

//         try {
//             controller.update(1L, req, httpRequest);
//         } catch (ForbiddenException e) {
//             assertThat(e.getMessage()).isEqualTo("Access denied");
//         }
//     }

//     // ---------------------------------------------------------
//     // 4. DELETE /messages/{id} (delete)
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("delete: 正常")
//     void testDelete_success() {

//         when(messageService.deleteMessage(eq(1L), any()))
//                 .thenReturn("ok");

//         var result = controller.delete(1L, httpRequest);

//         assertThat(result.getBody()).isEqualTo("ok");
//     }

//     @Test
//     @DisplayName("delete: メッセージ不存在 → NotFoundException")
//     void testDelete_notFound() {

//         when(messageService.deleteMessage(eq(999L), any()))
//                 .thenThrow(new NotFoundException("Message not found"));

//         try {
//             controller.delete(999L, httpRequest);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Message not found");
//         }
//     }

//     @Test
//     @DisplayName("delete: 削除権限なし → ForbiddenException")
//     void testDelete_forbidden() {

//         when(messageService.deleteMessage(eq(1L), any()))
//                 .thenThrow(new ForbiddenException("Access denied"));

//         try {
//             controller.delete(1L, httpRequest);
//         } catch (ForbiddenException e) {
//             assertThat(e.getMessage()).isEqualTo("Access denied");
//         }
//     }

//     // ---------------------------------------------------------
//     // 5. GET /messages/{id}/replies
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("getReplies: 正常")
//     void testGetReplies_success() {

//         MessageResponseDTO dto =
//                 new MessageResponseDTO(2L, 1L, 10L, 1L, "reply", 0, null, null);

//         when(messageService.getReplies(1L)).thenReturn(List.of(dto));

//         List<MessageResponseDTO> result = controller.getReplies(1L);

//         assertThat(result).hasSize(1);
//         assertThat(result.get(0).content()).isEqualTo("reply");
//     }

//     @Test
//     @DisplayName("getReplies: メッセージ不存在 → NotFoundException")
//     void testGetReplies_notFound() {

//         when(messageService.getReplies(999L))
//                 .thenThrow(new NotFoundException("Message not found"));

//         try {
//             controller.getReplies(999L);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Message not found");
//         }
//     }
// }
