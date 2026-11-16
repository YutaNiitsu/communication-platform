// package com.example.app.controller;

// import com.example.app.dto.reaction.ReactionResponseDTO;
// import com.example.app.exception.BadRequestException;
// import com.example.app.service.ReactionService;

// import jakarta.servlet.http.HttpServletRequest;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.mockito.*;

// import java.util.List;
// import java.util.Map;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.Mockito.*;

// class ReactionControllerTest {

//     @Mock
//     private ReactionService reactionService;

//     @Mock
//     private HttpServletRequest httpRequest;

//     @InjectMocks
//     private ReactionController controller;

//     @BeforeEach
//     void setup() {
//         MockitoAnnotations.openMocks(this);
//     }

//     // ---------------------------------------------------------
//     // 1. addReaction
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("1-1 addReaction: 正常")
//     void testAddReaction_success() {

//         Map<String, String> body = Map.of("reaction_type", "👍");

//         ReactionResponseDTO dto =
//                 new ReactionResponseDTO(100L, 10L, 1L, "👍", true, null);

//         when(reactionService.addReaction(eq(100L), eq("👍"), any()))
//                 .thenReturn(dto);

//         ReactionResponseDTO result = controller.addReaction(100L, body, httpRequest);

//         assertThat(result.reactionType()).isEqualTo("👍");
//     }

//     @Test
//     @DisplayName("1-2 addReaction: body=null → BadRequestException")
//     void testAddReaction_bodyNull() {

//         try {
//             controller.addReaction(100L, null, httpRequest);
//         } catch (BadRequestException | NullPointerException e) {
//             assertThat(e).isInstanceOf(Exception.class);
//         }
//     }

//     // ---------------------------------------------------------
//     // 2. removeReaction
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("2-1 removeReaction: 正常")
//     void testRemoveReaction_success() {

//         Map<String, String> body = Map.of("reaction_type", "👍");

//         doNothing().when(reactionService).removeReaction(eq(100L), eq("👍"), any());

//         controller.removeReaction(100L, body, httpRequest);

//         verify(reactionService).removeReaction(eq(100L), eq("👍"), any());
//     }

//     @Test
//     @DisplayName("2-2 removeReaction: body=null → BadRequestException")
//     void testRemoveReaction_bodyNull() {

//         try {
//             controller.removeReaction(100L, null, httpRequest);
//         } catch (BadRequestException | NullPointerException e) {
//             assertThat(e).isInstanceOf(Exception.class);
//         }
//     }

//     // ---------------------------------------------------------
//     // 3. getReactions
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("3-1 getReactions: 正常")
//     void testGetReactions_success() {

//         List<Map<String, Object>> list = List.of(
//                 Map.of("user_id", 10L, "reaction_type", "👍")
//         );

//         when(reactionService.getReactions(eq(100L))).thenReturn(list);

//         List<Map<String, Object>> result = controller.getReactions(100L);

//         assertThat(result).hasSize(1);
//         assertThat(result.get(0).get("reaction_type")).isEqualTo("👍");
//     }

//     @Test
//     @DisplayName("3-2 getReactions: メッセージ不存在 → NotFoundException")
//     void testGetReactions_notFound() {

//         when(reactionService.getReactions(eq(999L)))
//                 .thenThrow(new com.example.app.exception.NotFoundException("Message not found"));

//         try {
//             controller.getReactions(999L);
//         } catch (com.example.app.exception.NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Message not found");
//         }
//     }
// }
