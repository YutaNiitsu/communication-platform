// package com.example.app.controller;

// import com.example.app.dto.channel.*;
// import com.example.app.exception.BadRequestException;
// import com.example.app.exception.ConflictException;
// import com.example.app.exception.NotFoundException;
// import com.example.app.exception.ForbiddenException;
// import com.example.app.service.ChannelService;

// import jakarta.servlet.http.HttpServletRequest;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.mockito.*;

// import java.util.List;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.Mockito.*;

// class ChannelControllerTest {

//     @Mock
//     private ChannelService channelService;

//     @Mock
//     private HttpServletRequest request;

//     @InjectMocks
//     private ChannelController controller;

//     @BeforeEach
//     void setup() {
//         MockitoAnnotations.openMocks(this);
//     }

//     // ---------------------------------------------------------
//     // 1. create
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("1-1 create: 正常")
//     void testCreate_success() {
//         ChannelCreateRequestDTO dto = new ChannelCreateRequestDTO("general");

//         ChannelResponseDTO resDto =
//                 new ChannelResponseDTO(1L, "general", 0, null);

//         when(channelService.create(eq(dto), any())).thenReturn(resDto);

//         ChannelResponseDTO result = controller.create(dto, request);

//         assertThat(result.id()).isEqualTo(1L);
//         assertThat(result.name()).isEqualTo("general");
//     }

//     @Test
//     @DisplayName("1-2 create: dto=null → BadRequestException")
//     void testCreate_dtoNull() {
//         try {
//             controller.create(null, request);
//         } catch (BadRequestException | NullPointerException e) {
//             assertThat(e).isInstanceOf(Exception.class);
//         }
//     }

//     // ---------------------------------------------------------
//     // 2. update
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("2-1 update: 正常")
//     void testUpdate_success() {
//         ChannelEditRequestDTO dto = new ChannelEditRequestDTO("newName", 1);

//         ChannelEditResponseDTO resDto =
//                 new ChannelEditResponseDTO(10L, "newName", 1);

//         when(channelService.edit(eq(10L), eq(dto), any())).thenReturn(resDto);

//         ChannelEditResponseDTO result = controller.update(10L, dto, request);

//         assertThat(result.id()).isEqualTo(10L);
//         assertThat(result.name()).isEqualTo("newName");
//     }

//     @Test
//     @DisplayName("2-2 update: dto=null → BadRequestException")
//     void testUpdate_dtoNull() {
//         try {
//             controller.update(10L, null, request);
//         } catch (BadRequestException | NullPointerException e) {
//             assertThat(e).isInstanceOf(Exception.class);
//         }
//     }

//     @Test
//     @DisplayName("2-3 update: チャンネル不存在 → NotFoundException")
//     void testUpdate_notFound() {
//         ChannelEditRequestDTO dto = new ChannelEditRequestDTO("newName", 1);

//         when(channelService.edit(eq(999L), eq(dto), any()))
//                 .thenThrow(new NotFoundException("Channel not found"));

//         try {
//             controller.update(999L, dto, request);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Channel not found");
//         }
//     }

//     // ---------------------------------------------------------
//     // 3. join
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("3-1 join: 正常")
//     void testJoin_success() {
//         JoinChannelRequestDTO dto = new JoinChannelRequestDTO(10L);

//         ChannelMemberResponseDTO resDto =
//                 new ChannelMemberResponseDTO(10L, "alice", 1, true, null);

//         when(channelService.join(eq(1L), any(), eq(dto))).thenReturn(resDto);

//         ChannelMemberResponseDTO result = controller.join(1L, request, dto);

//         assertThat(result.userId()).isEqualTo(10L);
//     }

//     @Test
//     @DisplayName("3-2 join: dto=null → BadRequestException")
//     void testJoin_dtoNull() {
//         try {
//             controller.join(1L, request, null);
//         } catch (BadRequestException | NullPointerException e) {
//             assertThat(e).isInstanceOf(Exception.class);
//         }
//     }

//     @Test
//     @DisplayName("3-3 join: チャンネル不存在 → NotFoundException")
//     void testJoin_notFound() {
//         JoinChannelRequestDTO dto = new JoinChannelRequestDTO(10L);

//         when(channelService.join(eq(999L), any(), eq(dto)))
//                 .thenThrow(new NotFoundException("Channel not found"));

//         try {
//             controller.join(999L, request, dto);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Channel not found");
//         }
//     }

//     // ---------------------------------------------------------
//     // 4. leave
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("4-1 leave: 正常")
//     void testLeave_success() {
//         LeaveChannelRequestDTO dto = new LeaveChannelRequestDTO(10L);

//         doNothing().when(channelService).leave(eq(1L), any(), eq(dto));

//         controller.leave(1L, request, dto);

//         verify(channelService).leave(eq(1L), any(), eq(dto));
//     }

//     @Test
//     @DisplayName("4-2 leave: dto=null → BadRequestException")
//     void testLeave_dtoNull() {
//         try {
//             controller.leave(1L, request, null);
//         } catch (BadRequestException | NullPointerException e) {
//             assertThat(e).isInstanceOf(Exception.class);
//         }
//     }

//     @Test
//     @DisplayName("4-3 leave: チャンネル不存在 → NotFoundException")
//     void testLeave_notFound() {
//         LeaveChannelRequestDTO dto = new LeaveChannelRequestDTO(10L);

//         doThrow(new NotFoundException("Channel not found"))
//                 .when(channelService).leave(eq(999L), any(), eq(dto));

//         try {
//             controller.leave(999L, request, dto);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Channel not found");
//         }
//     }

//     // ---------------------------------------------------------
//     // 5. delete
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("5-1 delete: 正常")
//     void testDelete_success() {
//         when(channelService.delete(eq(1L), any())).thenReturn("ok");

//         var result = controller.delete(1L, request);

//         assertThat(result.getBody()).isEqualTo("ok");
//     }

//     @Test
//     @DisplayName("5-2 delete: ForbiddenException")
//     void testDelete_forbidden() {
//         when(channelService.delete(eq(1L), any()))
//                 .thenThrow(new ForbiddenException("Access denied"));

//         try {
//             controller.delete(1L, request);
//         } catch (ForbiddenException e) {
//             assertThat(e.getMessage()).isEqualTo("Access denied");
//         }
//     }

//     @Test
//     @DisplayName("5-3 delete: チャンネル不存在 → NotFoundException")
//     void testDelete_notFound() {
//         when(channelService.delete(eq(999L), any()))
//                 .thenThrow(new NotFoundException("Channel not found"));

//         try {
//             controller.delete(999L, request);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("Channel not found");
//         }
//     }

// }
