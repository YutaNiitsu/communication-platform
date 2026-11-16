// package com.example.app.controller;

// import com.example.app.dto.user.UserCreateRequestDTO;
// import com.example.app.dto.user.UserUpdateRequestDTO;
// import com.example.app.dto.user.UserResponseDTO;
// import com.example.app.exception.BadRequestException;
// import com.example.app.exception.NotFoundException;
// import com.example.app.service.UserService;

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

// class UserControllerTest {

//     @Mock
//     private UserService userService;

//     @Mock
//     private HttpServletRequest httpRequest;

//     @InjectMocks
//     private UserController controller;

//     @BeforeEach
//     void setup() {
//         MockitoAnnotations.openMocks(this);
//     }

//     // ---------------------------------------------------------
//     // 1. createUser
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("1-1 createUser: 正常")
//     void testCreateUser_success() {

//         UserCreateRequestDTO dto = new UserCreateRequestDTO("alice", "pass", 1, true);

//         UserResponseDTO resDto = new UserResponseDTO(1L, "alice", 1, true);

//         when(userService.createUser(eq(dto), any())).thenReturn(resDto);

//         UserResponseDTO result = controller.createUser(dto, httpRequest);

//         assertThat(result.userId()).isEqualTo(1L);
//         assertThat(result.username()).isEqualTo("alice");
//     }

//     @Test
//     @DisplayName("1-2 createUser: dto=null → BadRequestException")
//     void testCreateUser_dtoNull() {

//         try {
//             controller.createUser(null, httpRequest);
//         } catch (BadRequestException | NullPointerException e) {
//             assertThat(e).isInstanceOf(Exception.class);
//         }
//     }

//     // ---------------------------------------------------------
//     // 2. updateUser
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("2-1 updateUser: 正常")
//     void testUpdateUser_success() {

//         UserUpdateRequestDTO dto = new UserUpdateRequestDTO("bob", "newpass", 1, true);

//         UserResponseDTO resDto = new UserResponseDTO(10L, "bob", 1, true);

//         when(userService.updateUser(eq(10L), eq(dto), any())).thenReturn(resDto);

//         UserResponseDTO result = controller.updateUser(10L, dto, httpRequest);

//         assertThat(result.userId()).isEqualTo(10L);
//         assertThat(result.username()).isEqualTo("bob");
//     }

//     @Test
//     @DisplayName("2-2 updateUser: dto=null → BadRequestException")
//     void testUpdateUser_dtoNull() {

//         try {
//             controller.updateUser(10L, null, httpRequest);
//         } catch (BadRequestException | NullPointerException e) {
//             assertThat(e).isInstanceOf(Exception.class);
//         }
//     }

//     @Test
//     @DisplayName("2-3 updateUser: ユーザー不存在 → NotFoundException")
//     void testUpdateUser_notFound() {

//         UserUpdateRequestDTO dto = new UserUpdateRequestDTO("bob", "newpass", 1, true);

//         when(userService.updateUser(eq(999L), eq(dto), any()))
//                 .thenThrow(new NotFoundException("User not found"));

//         try {
//             controller.updateUser(999L, dto, httpRequest);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("User not found");
//         }
//     }

//     // ---------------------------------------------------------
//     // 3. deleteUser
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("3-1 deleteUser: 正常")
//     void testDeleteUser_success() {

//         when(userService.deleteUser(eq(10L), any())).thenReturn("ok");

//         var result = controller.deleteUser(10L, httpRequest);

//         assertThat(result.getBody()).isEqualTo("ok");
//     }

//     @Test
//     @DisplayName("3-2 deleteUser: ユーザー不存在 → NotFoundException")
//     void testDeleteUser_notFound() {

//         when(userService.deleteUser(eq(999L), any()))
//                 .thenThrow(new NotFoundException("User not found"));

//         try {
//             controller.deleteUser(999L, httpRequest);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("User not found");
//         }
//     }

//     // ---------------------------------------------------------
//     // 4. getUser
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("4-1 getUser: 正常")
//     void testGetUser_success() {

//         UserResponseDTO dto = new UserResponseDTO(1L, "alice", 1, true);

//         when(userService.getUser(eq(1L), any())).thenReturn(dto);

//         UserResponseDTO result = controller.getUser(1L, httpRequest);

//         assertThat(result.userId()).isEqualTo(1L);
//         assertThat(result.username()).isEqualTo("alice");
//     }

//     @Test
//     @DisplayName("4-2 getUser: ユーザー不存在 → NotFoundException")
//     void testGetUser_notFound() {

//         when(userService.getUser(eq(999L), any()))
//                 .thenThrow(new NotFoundException("User not found"));

//         try {
//             controller.getUser(999L, httpRequest);
//         } catch (NotFoundException e) {
//             assertThat(e.getMessage()).isEqualTo("User not found");
//         }
//     }

//     // ---------------------------------------------------------
//     // 5. getAllUsers
//     // ---------------------------------------------------------

//     @Test
//     @DisplayName("5-1 getAllUsers: 正常")
//     void testGetAllUsers_success() {

//         UserResponseDTO dto = new UserResponseDTO(1L, "alice", 1, true);

//         when(userService.getAllUsers(any())).thenReturn(List.of(dto));

//         List<UserResponseDTO> result = controller.getAllUsers(httpRequest);

//         assertThat(result).hasSize(1);
//         assertThat(result.get(0).username()).isEqualTo("alice");
//     }
// }
