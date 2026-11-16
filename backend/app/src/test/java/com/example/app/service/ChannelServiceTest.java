package com.example.app.service;

import com.example.app.domain.channel.*;
import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.user.UserEntity;
import com.example.app.domain.user.UserMapper;
import com.example.app.dto.channel.*;
import com.example.app.exception.*;
import com.example.app.security.AuthUtil;
import com.example.app.websocket.WebSocketEventPublisher;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class ChannelServiceTest {

    private ChannelMapper channelMapper;
    private ChannelMemberMapper channelMemberMapper;
    private WebSocketEventPublisher eventPublisher;
    private OperationLogService operationLogService;
    private JwtProvider jwtProvider;
    private UserMapper userMapper;
    private AuthUtil authUtil;

    private ChannelService channelService;
    private HttpServletRequest request;

    @BeforeEach
    void setup() {
        channelMapper = mock(ChannelMapper.class);
        channelMemberMapper = mock(ChannelMemberMapper.class);
        eventPublisher = mock(WebSocketEventPublisher.class);
        operationLogService = mock(OperationLogService.class);
        jwtProvider = mock(JwtProvider.class);
        userMapper = mock(UserMapper.class);
        authUtil = mock(AuthUtil.class);

        channelService = new ChannelService(
                channelMapper,
                channelMemberMapper,
                eventPublisher,
                operationLogService,
                jwtProvider,
                userMapper,
                authUtil
        );

        request = mock(HttpServletRequest.class);
    }

    // ---------------------------------------------------------
    // 1. findAll()
    // ---------------------------------------------------------

    @Test
    @DisplayName("1-1: チャンネル一覧取得（複数件）")
    void testFindAll_normal() {
        ChannelEntity c1 = new ChannelEntity();
        c1.setId(1L);
        c1.setName("general");

        ChannelEntity c2 = new ChannelEntity();
        c2.setId(2L);
        c2.setName("random");

        when(channelMapper.findAll()).thenReturn(List.of(c1, c2));

        List<ChannelResponseDTO> result = channelService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("general");
    }

    @Test
    @DisplayName("1-2: チャンネル一覧取得（0件）")
    void testFindAll_empty() {
        when(channelMapper.findAll()).thenReturn(List.of());

        List<ChannelResponseDTO> result = channelService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("1-3: DTO変換確認")
    void testFindAll_dtoMapping() {
        ChannelEntity c = new ChannelEntity();
        c.setId(1L);
        c.setName("general");

        when(channelMapper.findAll()).thenReturn(List.of(c));

        List<ChannelResponseDTO> result = channelService.findAll();

        assertThat(result.get(0).name()).isEqualTo("general");
    }

    // ---------------------------------------------------------
    // 2. findById()
    // ---------------------------------------------------------

    @Test
    @DisplayName("2-1: 正常取得")
    void testFindById_normal() {
        ChannelEntity c = new ChannelEntity();
        c.setId(1L);
        c.setName("general");

        when(channelMapper.findById(1L)).thenReturn(Optional.of(c));

        ChannelResponseDTO dto = channelService.findById(1L);

        assertThat(dto.name()).isEqualTo("general");
    }

    @Test
    @DisplayName("2-2: 存在しないID → NotFoundException")
    void testFindById_notFound() {
        when(channelMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.findById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    // ---------------------------------------------------------
    // 3. create()
    // ---------------------------------------------------------

    @Test
    @DisplayName("3-1: 正常作成")
    void testCreate_normal() {
        UserEntity user = new UserEntity();
        user.setId(10L);

        when(authUtil.getTokenFromCookie(request)).thenReturn("token");
        when(authUtil.getUserFromToken("token")).thenReturn(user);
        when(channelMapper.existsByName("general")).thenReturn(false);

        ChannelEntity saved = new ChannelEntity();
        saved.setId(1L);
        saved.setName("general");

        doAnswer(inv -> {
            ChannelEntity c = inv.getArgument(0);
            c.setId(1L);
            return null;
        }).when(channelMapper).insert(any());

        ChannelCreateRequestDTO req = new ChannelCreateRequestDTO("general");

        ChannelResponseDTO dto = channelService.create(req, request);

        assertThat(dto.name()).isEqualTo("general");
        verify(operationLogService).record(eq(user), eq(OperationType.channel_created), anyList(), anyList());
    }

    @Test
    @DisplayName("3-2: name=null → BadRequestException")
    void testCreate_nameNull() {
        ChannelCreateRequestDTO req = new ChannelCreateRequestDTO(null);

        assertThatThrownBy(() -> channelService.create(req, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("3-3: name=\"\" → BadRequestException")
    void testCreate_nameEmpty() {
        ChannelCreateRequestDTO req = new ChannelCreateRequestDTO("");

        assertThatThrownBy(() -> channelService.create(req, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("3-4: name 重複 → ConflictException")
    void testCreate_nameDuplicate() {
        when(channelMapper.existsByName("general")).thenReturn(true);

        ChannelCreateRequestDTO req = new ChannelCreateRequestDTO("general");

        assertThatThrownBy(() -> channelService.create(req, request))
                .isInstanceOf(ConflictException.class);
    }

    // ---------------------------------------------------------
    // 4. join()
    // ---------------------------------------------------------

    @Test
    @DisplayName("4-1: 正常参加")
    void testJoin_normal() {
        UserEntity requestUser = new UserEntity();
        requestUser.setId(99L);

        when(authUtil.getTokenFromCookie(request)).thenReturn("token");
        when(authUtil.getUserFromToken("token")).thenReturn(requestUser);

        ChannelEntity channel = new ChannelEntity();
        channel.setId(1L);

        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("alice");

        when(channelMapper.findById(1L)).thenReturn(Optional.of(channel));
        when(userMapper.findById(10L)).thenReturn(Optional.of(user));
        when(channelMemberMapper.findByChannelAndUser(1L, 10L)).thenReturn(Optional.empty());

        doAnswer(inv -> {
            ChannelMemberEntity m = inv.getArgument(0);
            m.setId(100L);
            return null;
        }).when(channelMemberMapper).insert(any());

        JoinChannelRequestDTO reqDto = new JoinChannelRequestDTO(10L);

        ChannelMemberResponseDTO dto = channelService.join(1L, request, reqDto);

        assertThat(dto.username()).isEqualTo("alice");
        verify(eventPublisher).sendChannelJoined(eq(1L), anyMap());
    }

    @Test
    @DisplayName("4-2: チャンネル不存在 → NotFoundException")
    void testJoin_channelNotFound() {
        when(channelMapper.findById(999L)).thenReturn(Optional.empty());

        JoinChannelRequestDTO reqDto = new JoinChannelRequestDTO(10L);

        assertThatThrownBy(() -> channelService.join(999L, request, reqDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("4-3: ユーザー不存在 → RuntimeException")
    void testJoin_userNotFound() {
        ChannelEntity channel = new ChannelEntity();
        channel.setId(1L);

        when(channelMapper.findById(1L)).thenReturn(Optional.of(channel));
        when(userMapper.findById(999L)).thenReturn(Optional.empty());

        JoinChannelRequestDTO reqDto = new JoinChannelRequestDTO(999L);

        assertThatThrownBy(() -> channelService.join(1L, request, reqDto))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("4-4: すでに参加済み → ConflictException")
    void testJoin_alreadyJoined() {
        ChannelEntity channel = new ChannelEntity();
        channel.setId(1L);

        UserEntity user = new UserEntity();
        user.setId(10L);

        when(channelMapper.findById(1L)).thenReturn(Optional.of(channel));
        when(userMapper.findById(10L)).thenReturn(Optional.of(user));

        ChannelMemberEntity member = new ChannelMemberEntity();
        member.setId(100L);

        when(channelMemberMapper.findByChannelAndUser(1L, 10L)).thenReturn(Optional.of(member));

        JoinChannelRequestDTO reqDto = new JoinChannelRequestDTO(10L);

        assertThatThrownBy(() -> channelService.join(1L, request, reqDto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("4-5: WebSocket送信確認")
    void testJoin_websocket() {
        UserEntity requestUser = new UserEntity();
        requestUser.setId(99L);

        when(authUtil.getTokenFromCookie(request)).thenReturn("token");
        when(authUtil.getUserFromToken("token")).thenReturn(requestUser);

        ChannelEntity channel = new ChannelEntity();
        channel.setId(1L);

        UserEntity user = new UserEntity();
        user.setId(10L);

        when(channelMapper.findById(1L)).thenReturn(Optional.of(channel));
        when(userMapper.findById(10L)).thenReturn(Optional.of(user));
        when(channelMemberMapper.findByChannelAndUser(1L, 10L)).thenReturn(Optional.empty());

        doAnswer(inv -> {
            ChannelMemberEntity m = inv.getArgument(0);
            m.setId(100L);
            return null;
        }).when(channelMemberMapper).insert(any());

        JoinChannelRequestDTO reqDto = new JoinChannelRequestDTO(10L);

        channelService.join(1L, request, reqDto);

        verify(eventPublisher).sendChannelJoined(eq(1L), anyMap());
    }

    // ---------------------------------------------------------
    // 5. leave()
    // ---------------------------------------------------------

    @Test
    @DisplayName("5-1: 正常退出")
    void testLeave_normal() {
        UserEntity requestUser = new UserEntity();
        requestUser.setId(99L);

        when(authUtil.getTokenFromCookie(request)).thenReturn("token");
        when(authUtil.getUserFromToken("token")).thenReturn(requestUser);

        ChannelEntity channel = new ChannelEntity();
        channel.setId(1L);

        UserEntity user = new UserEntity();
        user.setId(10L);

        ChannelMemberEntity member = new ChannelMemberEntity();
        member.setId(100L);

        when(channelMapper.findById(1L)).thenReturn(Optional.of(channel));
        when(userMapper.findById(10L)).thenReturn(Optional.of(user));
        when(channelMemberMapper.findByChannelAndUser(1L, 10L)).thenReturn(Optional.of(member));

        LeaveChannelRequestDTO reqDto = new LeaveChannelRequestDTO(10L);

        channelService.leave(1L, request, reqDto);

        verify(channelMemberMapper).deleteByChannelAndUser(1L, 10L);
        verify(eventPublisher).sendChannelLeft(1L, 10L);
    }

    @Test
    @DisplayName("5-2: チャンネル不存在 → NotFoundException")
    void testLeave_channelNotFound() {
        when(channelMapper.findById(999L)).thenReturn(Optional.empty());

        LeaveChannelRequestDTO reqDto = new LeaveChannelRequestDTO(10L);

        assertThatThrownBy(() -> channelService.leave(999L, request, reqDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("5-3: ユーザー不存在 → RuntimeException")
    void testLeave_userNotFound() {
        ChannelEntity channel = new ChannelEntity();
        channel.setId(1L);

        when(channelMapper.findById(1L)).thenReturn(Optional.of(channel));
        when(userMapper.findById(999L)).thenReturn(Optional.empty());

        LeaveChannelRequestDTO reqDto = new LeaveChannelRequestDTO(999L);

        assertThatThrownBy(() -> channelService.leave(1L, request, reqDto))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("5-4: 未参加 → NotFoundException")
    void testLeave_notJoined() {
        ChannelEntity channel = new ChannelEntity();
        channel.setId(1L);

        UserEntity user = new UserEntity();
        user.setId(10L);

        when(channelMapper.findById(1L)).thenReturn(Optional.of(channel));
        when(userMapper.findById(10L)).thenReturn(Optional.of(user));
        when(channelMemberMapper.findByChannelAndUser(1L, 10L)).thenReturn(Optional.empty());

        LeaveChannelRequestDTO reqDto = new LeaveChannelRequestDTO(10L);

        assertThatThrownBy(() -> channelService.leave(1L, request, reqDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("5-5: WebSocket送信確認")
    void testLeave_websocket() {
        UserEntity requestUser = new UserEntity();
        requestUser.setId(99L);

        when(authUtil.getTokenFromCookie(request)).thenReturn("token");
        when(authUtil.getUserFromToken("token")).thenReturn(requestUser);

        ChannelEntity channel = new ChannelEntity();
        channel.setId(1L);

        UserEntity user = new UserEntity();
        user.setId(10L);

        ChannelMemberEntity member = new ChannelMemberEntity();
        member.setId(100L);

        when(channelMapper.findById(1L)).thenReturn(Optional.of(channel));
        when(userMapper.findById(10L)).thenReturn(Optional.of(user));
        when(channelMemberMapper.findByChannelAndUser(1L, 10L)).thenReturn(Optional.of(member));

        LeaveChannelRequestDTO reqDto = new LeaveChannelRequestDTO(10L);

        channelService.leave(1L, request, reqDto);

        verify(eventPublisher).sendChannelLeft(1L, 10L);
    }

    // ---------------------------------------------------------
    // 6. delete()
    // ---------------------------------------------------------

    @Test
    @DisplayName("6-1: 正常削除（admin）")
    void testDelete_normal() {
        UserEntity admin = new UserEntity();
        admin.setId(99L);

        when(authUtil.getTokenFromCookie(request)).thenReturn("token");
        when(authUtil.getUserFromToken("token")).thenReturn(admin);

        doNothing().when(authUtil).requireAdmin(admin);

        ChannelEntity channel = new ChannelEntity();
        channel.setId(1L);

        when(channelMapper.findById(1L)).thenReturn(Optional.of(channel));

        String result = channelService.delete(1L, request);

        assertThat(result).isEqualTo("ok");
        verify(channelMemberMapper).deleteByChannel(1L);
        verify(channelMapper).deleteById(1L);
        verify(eventPublisher).sendChannelDeleted(1L);
    }

    @Test
    @DisplayName("6-2: 権限なし → ForbiddenException")
    void testDelete_forbidden() {
        UserEntity user = new UserEntity();
        user.setId(99L);

        when(authUtil.getTokenFromCookie(request)).thenReturn("token");
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        doThrow(new ForbiddenException("not admin")).when(authUtil).requireAdmin(user);

        assertThatThrownBy(() -> channelService.delete(1L, request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("6-3: チャンネル不存在 → NotFoundException")
    void testDelete_notFound() {
        UserEntity admin = new UserEntity();
        admin.setId(99L);

        when(authUtil.getTokenFromCookie(request)).thenReturn("token");
        when(authUtil.getUserFromToken("token")).thenReturn(admin);

        doNothing().when(authUtil).requireAdmin(admin);

        when(channelMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.delete(999L, request))
                .isInstanceOf(NotFoundException.class);
    }

    // ---------------------------------------------------------
    // 7. getMembers()
    // ---------------------------------------------------------

    @Test
    @DisplayName("7-1: 正常取得")
    void testGetMembers_normal() {
        ChannelEntity channel = new ChannelEntity();
        channel.setId(1L);

        ChannelMemberEntity m1 = new ChannelMemberEntity();
        m1.setId(100L);
        m1.setUserId(10L);

        UserEntity u1 = new UserEntity();
        u1.setId(10L);
        u1.setUsername("alice");

        when(channelMapper.findById(1L)).thenReturn(Optional.of(channel));
        when(channelMemberMapper.findByChannel(1L)).thenReturn(List.of(m1));
        when(userMapper.findById(10L)).thenReturn(Optional.of(u1));

        List<ChannelMemberResponseDTO> result = channelService.getMembers(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("alice");
    }

    @Test
    @DisplayName("7-2: チャンネル不存在 → NotFoundException")
    void testGetMembers_channelNotFound() {
        when(channelMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.getMembers(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("7-3: メンバー0件 → 空リスト")
    void testGetMembers_empty() {

        // チャンネルは存在する
        ChannelEntity channel = new ChannelEntity();
        channel.setId(1L);

        when(channelMapper.findById(1L)).thenReturn(Optional.of(channel));

        // メンバーは0件
        when(channelMemberMapper.findByChannel(1L)).thenReturn(List.of());

        // 実行
        List<ChannelMemberResponseDTO> result = channelService.getMembers(1L);

        // 検証
        assertThat(result).isEmpty();
    }
}