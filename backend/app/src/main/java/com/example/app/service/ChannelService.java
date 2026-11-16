package com.example.app.service;

import com.example.app.domain.channel.ChannelEntity;
import com.example.app.domain.channel.ChannelMapper;
import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.channel.ChannelMemberEntity;
import com.example.app.domain.channel.ChannelMemberMapper;
import com.example.app.domain.user.UserEntity;
import com.example.app.domain.user.UserMapper;
import com.example.app.dto.channel.ChannelCreateRequestDTO;
import com.example.app.dto.channel.ChannelEditRequestDTO;
import com.example.app.dto.channel.ChannelEditResponseDTO;
import com.example.app.dto.channel.ChannelResponseDTO;
import com.example.app.dto.channel.JoinChannelRequestDTO;
import com.example.app.dto.channel.LeaveChannelRequestDTO;
import com.example.app.dto.channel.ChannelMemberResponseDTO;
import com.example.app.exception.*;
import com.example.app.security.AuthUtil;
import com.example.app.websocket.WebSocketEventPublisher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelMapper channelMapper;
    private final ChannelMemberMapper channelMemberMapper;
    private final WebSocketEventPublisher eventPublisher;
    private final OperationLogService operationLogService;
    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;
    private final AuthUtil authUtil;
    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");

    // 1. GET /channels
    public List<ChannelResponseDTO> findAll() {
        return channelMapper.findAll().stream()
                .map(c -> new ChannelResponseDTO(
                        c.getId(),
                        c.getName(),
                        c.getStatus(),
                        c.getCreatedAt()
                ))
                .toList();
    }

    // 2. GET /channels/{id}
    public ChannelResponseDTO findById(Long id) {
        ChannelEntity channel = channelMapper.findById(id)
                .orElseGet(() -> {
				appLog.warn("Channel not found (channel_id={})", id);
                throw new NotFoundException("Channel not found");
			});

        return new ChannelResponseDTO(
                channel.getId(),
                channel.getName(),
                channel.getStatus(),
                channel.getCreatedAt()
        );
    }

    // 3. POST /channels
    public ChannelResponseDTO create(ChannelCreateRequestDTO request, HttpServletRequest httpRequest) {
        String token = authUtil.getTokenFromCookie(httpRequest);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        if (request.name() == null || request.name().isBlank()) {
            appLog.warn("Channel name is required");
            throw new BadRequestException("Channel name is required");
        }

        if (channelMapper.existsByName(request.name())) {
            appLog.warn("Channel name already exists");
            throw new ConflictException("Channel name already exists");
        }

        ChannelEntity channel = new ChannelEntity();
        channel.setName(request.name());

        channelMapper.insert(channel);

        eventPublisher.sendChannelCreated(channel.getId(), Map.of(
                "name", channel.getName(),
                "status", channel.getStatus()
        ));

        // 操作ログ記録
        operationLogService.record(
            requestUser,
            OperationType.channel_created,
            List.of("channel_id=" + channel.getId()),
            List.of("name=" + channel.getName(), "created_at=" + channel.getCreatedAt())
        );

        return new ChannelResponseDTO(
                channel.getId(),
                channel.getName(),
                channel.getStatus(),
                channel.getCreatedAt()
        );
    }

    // 4. POST /channels/{id}/join
    public ChannelMemberResponseDTO join(Long channelId, HttpServletRequest request, JoinChannelRequestDTO joinRequest) {
        String token = authUtil.getTokenFromCookie(request);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        ChannelEntity channel = channelMapper.findById(channelId)
                .orElseGet(() -> {
				appLog.warn("Channel not found (channel_id={})", channelId);
                throw new NotFoundException("Channel not found");
			});
        UserEntity user = userMapper.findById(joinRequest.user_id())
                .orElseGet(() -> {
                    appLog.warn("User not found (user_id={})", joinRequest.user_id());
                    throw new RuntimeException("User not found");
                });

        ChannelMemberEntity member = channelMemberMapper.findByChannelAndUser(channelId, user.getId())
            .orElse(null);
        if (member != null) {
            appLog.warn("Already joined");
            throw new ConflictException("Already joined");
        }

        member = new ChannelMemberEntity();
        member.setChannelId(channel.getId());
        member.setUserId(user.getId());

        channelMemberMapper.insert(member);

        eventPublisher.sendChannelJoined(channelId, Map.of(
                "channel_id", channelId,
                "user_id", joinRequest.user_id()
        ));

        // 操作ログ記録
        operationLogService.record(
            requestUser,
            OperationType.channel_join,
            List.of("channel_id=" + member.getId()),
            List.of("ok")
        );

        return new ChannelMemberResponseDTO(
                member.getId(),
                user.getUsername(),
                user.getRole(),
                user.isActive(),
                member.getJoinedAt()
        );
    }

    
    // 5. POST /channels/{id}/leave
    public void leave(Long channelId, HttpServletRequest request, LeaveChannelRequestDTO leaveRequest) {
        String token = authUtil.getTokenFromCookie(request);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        ChannelEntity channel = channelMapper.findById(channelId)
                .orElseGet(() -> {
				appLog.warn("Channel not found (channel_id={})", channelId);
                throw new NotFoundException("Channel not found");
			});

        UserEntity user = userMapper.findById(leaveRequest.user_id())
                .orElseGet(() -> {
                    appLog.warn("User not found (user_id={})", leaveRequest.user_id());
                    throw new RuntimeException("User not found");
                });

        ChannelMemberEntity member = channelMemberMapper.findByChannelAndUser(channelId, user.getId())
            .orElse(null);
        if (member == null) {
            appLog.warn("Not joined");
            throw new NotFoundException("Not joined");
        }

        channelMemberMapper.deleteByChannelAndUser(channelId, user.getId());

        // 操作ログ記録
        operationLogService.record(
            requestUser,
            OperationType.channel_leave,
            List.of("channel_id=" + channel.getId()),
            List.of("ok")
        );

        eventPublisher.sendChannelLeft(channelId, leaveRequest.user_id());
        
    }

    // 6. DELETE /channels/{id}
    public String delete(Long channelId, HttpServletRequest request) {
        String token = authUtil.getTokenFromCookie(request);
        UserEntity requestUser = authUtil.getUserFromToken(token);
        authUtil.requireAdmin(requestUser);

        ChannelEntity channel = channelMapper.findById(channelId)
            .orElseGet(() -> {
				appLog.warn("Channel not found (channel_id={})", channelId);
                throw new NotFoundException("Channel not found");
			});

        channelMemberMapper.deleteByChannel(channelId);

        channelMapper.deleteById(channelId);

        // 操作ログ記録
        operationLogService.record(
            requestUser,
            OperationType.channel_deleted,
            List.of("channel_id=" + channel.getId()),
            List.of("ok")
        );

        eventPublisher.sendChannelDeleted(channelId);

        return "ok";
    }

    // 7. GET /channels/{id}/members
    public List<ChannelMemberResponseDTO> getMembers(Long channelId) {

    ChannelEntity channel = channelMapper.findById(channelId)
            .orElseGet(() -> {
				appLog.warn("Channel not found (channel_id={})", channelId);
                throw new NotFoundException("Channel not found");
			});

    return channelMemberMapper.findByChannel(channelId).stream()
            .map(member -> {
                UserEntity user = userMapper.findById(member.getUserId())
                    .orElse(null);
                if (user == null) {
                    throw new NotFoundException("User not found");
                }

                return new ChannelMemberResponseDTO(
                        member.getId(),
                        user.getUsername(),
                        user.getRole(),
                        user.isActive(),
                        member.getJoinedAt()
                );
            })
            .toList();
    }


    public ChannelEditResponseDTO edit(Long id, ChannelEditRequestDTO request, HttpServletRequest httpRequest) {
        String token = jwtProvider.extractTokenFromCookie(httpRequest);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        ChannelEntity channel = channelMapper.findById(id)
                .orElseGet(() -> {
				appLog.warn("Channel not found (channel_id={})", id);
                throw new NotFoundException("Channel not found");
			});

        if (channel.getStatus() == 2) { // 2: 削除済み
            appLog.warn("Channel has been deleted");
            throw new NotFoundException("Channel has been deleted");
        }

        if (request.name() == null || request.name().isBlank()) {
            appLog.warn("Channel name is required");
            throw new BadRequestException("Channel name is required");
        }

        if (channelMapper.existsByNameExceptId(id, request.name())) {
            appLog.warn("Channel name already exists");
            throw new ConflictException("Channel name already exists");
        }

        List<String> result = new ArrayList<>();
        String oldName = channel.getName();
        int oldStatus = channel.getStatus();

        if (request.name() != null && !request.name().equals(oldName)){
            channel.setName(request.name());
            result.add("name=old:" + oldName + ",new:" + request.name());
        }
        if (request.status() != oldStatus){
            channel.setStatus(request.status());
            result.add("status=old:" + String.valueOf(oldStatus) + ",new:" + String.valueOf(request.status()));
        }
        

        channelMapper.update(channel);

        eventPublisher.sendChannelUpdated(channel.getId(), Map.of(
                "name", channel.getName(),
                "status", channel.getStatus()
        ));

        // 操作ログ記録
        operationLogService.record(
            requestUser,
            OperationType.channel_updated,
            List.of("channel_id=" + channel.getId()),
            result
        );

        return new ChannelEditResponseDTO(
                channel.getId(),
                channel.getName(),
                channel.getStatus()
        );
    }
}