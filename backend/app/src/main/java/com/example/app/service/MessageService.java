package com.example.app.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import com.example.app.config.AppProperties;
import com.example.app.domain.attachment.AttachmentEntity;
import com.example.app.domain.attachment.AttachmentMapper;
import com.example.app.domain.channel.ChannelEntity;
import com.example.app.domain.channel.ChannelMapper;
import com.example.app.domain.mention.MessageMentionEntity;
import com.example.app.domain.mention.MessageMentionMapper;
import com.example.app.domain.message.MessageEntity;
import com.example.app.domain.message.MessageMapper;
import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.reaction.ReactionEntity;
import com.example.app.domain.reaction.ReactionMapper;
import com.example.app.domain.user.UserEntity;
import com.example.app.domain.user.UserMapper;
import com.example.app.dto.attachment.AttachmentDTO;
import com.example.app.dto.mention.MentionResponseDTO;
import com.example.app.dto.message.*;
import com.example.app.dto.sync.ReactionSummaryDTO;
import com.example.app.exception.ForbiddenException;
import com.example.app.exception.NotFoundException;
import com.example.app.security.AuthUtil;
import com.example.app.websocket.WebSocketEventPublisher;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MessageService{

    private final MessageMapper messageMapper;
    private final ChannelMapper channelMapper;
    private final WebSocketEventPublisher eventPublisher;
    private final MessageMentionMapper mentionMapper;
    private final ReactionMapper reactionMapper;
    private final UserMapper userMapper;
    private final AttachmentMapper attachmentMapper;
    private final OperationLogService operationLogService;
    private final AuthUtil authUtil;
    private final AppProperties props;
    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");

    // 1. GET /channels/{id}/messages
    public MessageResponseDTO getMessages(Long channelId, Long messageId, Integer range) {

        ChannelEntity channel = channelMapper.findById(channelId)
            .orElseGet(() -> {
				appLog.warn("Channel not found (channel_id={})", channelId);
                throw new NotFoundException("Channel not found");
			});

        List<MessageEntity> messages = messageMapper.findAll();
        if (messageId != null && range != null){
            messages = messageMapper.findPreviousMessages(channelId, messageId, range);
        }
        else if (messageId != null && range == null){
            messages = messageMapper.findAllPreviousMessages(channelId, messageId);
        }
        else if (messageId == null && range != null){
            messages = messageMapper.findLatestMessages(channelId, range);
        }
        else {
            messages = messageMapper.findByChannel(channelId);
        }

        boolean hasMore = Boolean.TRUE.equals(
            messageMapper.existsPreviousMessages(channelId, messages.get(messages.size() - 1).getId()
        	)
        );

        List<MessageSummaryDTO> messageSummary = new ArrayList<>();

        for (MessageEntity m : messages) {

            // --- message DTO ---
            MessageDTO messageDTO = new MessageDTO(
                    m.getId(),
                    m.getChannelId(),
                    m.getUserId(),
                    m.getReplyToMessageId(),
                    m.getRequestId(),
                    m.getContent(),
                    m.getStatus(),
                    m.getCreatedAt(),
                    m.getUpdatedAt()
            );

            // --- reactions ---
            List<ReactionEntity> reactions = reactionMapper.findByMessageId(m.getId());
            Map<String, List<ReactionEntity>> grouped =
                    reactions.stream().collect(Collectors.groupingBy(ReactionEntity::getReactionType));

            List<ReactionSummaryDTO> reactionDTOs = grouped.entrySet().stream()
                    .map(e -> new ReactionSummaryDTO(
                            e.getKey(),
                            e.getValue().size(),
                            e.getValue().stream().map(r -> r.getUserId()).toList()
                    ))
                    .toList();

            // --- mentions ---
            List<MessageMentionEntity> mentions = mentionMapper.findByMessageId(m.getId());
            List<MentionResponseDTO> mentionDTOs = mentions.stream()
                    .map(mm -> new MentionResponseDTO(
                            mm.getId(),
                            mm.getMentionedUserId(),
                            mm.getCreatedAt()
                    ))
                    .toList();
        
			// --- attachments ---
			List<AttachmentEntity> attachments = attachmentMapper.findByMessageId(m.getId());
			List<AttachmentDTO> attachmentDTOs = attachments.stream()
					.map(a -> new AttachmentDTO(
							a.getId(),
							a.getMessageId(),
							a.getOriginalFileName(),
							a.getStoredFileName(),
							a.getFileUrl(),
							a.getThumbnailUrl(),
							a.getFileType(),
							a.getFileSize(),
							a.getCreatedAt()
					))
					.toList();

            // --- 結合 ---
            messageSummary.add(new MessageSummaryDTO(
                    messageDTO,
                    reactionDTOs,
                    mentionDTOs,
					attachmentDTOs
            ));
        }

        return new MessageResponseDTO(messageSummary, hasMore);
    }

    // 2. POST /channels/{id}/messages
    public MessageSummaryDTO createMessage(Long channelId, HttpServletRequest httpRequest, MessagePostRequestDTO request) {
        String token = authUtil.getTokenFromCookie(httpRequest);
        UserEntity requestUser = authUtil.getUserFromToken(token);

		// メッセージの重複リクエストチェック
        MessageEntity existing = messageMapper.findByRequestId( request.requestId())
				.orElse(null);

		if (existing != null) {

			appLog.info(
				"Duplicate request detected. requestId={}",
				request.requestId()
			);

			List<MessageMentionEntity> mentions = mentionMapper.findByMessageId(existing.getId());
        	List<MentionResponseDTO> mentionDTOs = mentions.stream()
                .map(mm -> new MentionResponseDTO(
                        mm.getId(),
                        mm.getMentionedUserId(),
                        mm.getCreatedAt()
                ))
                .toList();

			return new MessageSummaryDTO(
                toResponse(existing),
                new ArrayList<>(),
                mentionDTOs,
				new ArrayList<>()
        	);
		}

        // 文字数制限チェック
        if (request.content().length() > props.getMessage().getMaxLength()) {
            throw new IllegalArgumentException("Message content exceeds the maximum length");
        }

        List<String> target = new ArrayList<>();
        Long replyToMessageId = null;

        MessageEntity message = new MessageEntity();
        message.setChannelId(channelId);
        message.setUserId(requestUser.getId());
		message.setRequestId(request.requestId());
        message.setContent(request.content());
		message.setStatus(0);

        if (request.replyToMessageId() != null) {
            // 返信メッセージの場合
            MessageEntity replyToMessage =
				messageMapper.findById(request.replyToMessageId())
					.orElseGet(() -> {
						appLog.warn("Message not found (message_id={})", request.replyToMessageId());
						throw new NotFoundException("Message not found");
					});

            message.setReplyToMessageId(replyToMessage.getId());

            target.add(
                    "reply_to_message_id=" +
                    message.getReplyToMessageId()
            );

            replyToMessageId =
                    message.getReplyToMessageId();
        }

        try {
			messageMapper.insert(message);
		} catch (DuplicateKeyException e) {
			// メッセージの重複リクエストチェック
			existing = messageMapper.findByRequestId(request.requestId())
				.orElse(null);

			if (existing != null) {
				appLog.info(
					"Duplicate request detected. requestId={}",
					request.requestId()
				);
				List<MessageMentionEntity> mentions = mentionMapper.findByMessageId(existing.getId());
				List<MentionResponseDTO> mentionDTOs = mentions.stream()
					.map(mm -> new MentionResponseDTO(
							mm.getId(),
							mm.getMentionedUserId(),
							mm.getCreatedAt()
					))
					.toList();

				return new MessageSummaryDTO(
					toResponse(existing),
					new ArrayList<>(),
					mentionDTOs,
					new ArrayList<>()
				);
			}

			// それ以外の例外は再スロー
			appLog.info(
				"Unexpected error occurred while inserting message. requestId={}",
				request.requestId()
			);
			throw e;
		}

        target.add(
			"message_id=" + message.getId()
        );

        // メンションされたユーザーIDを保持
        List<Long> mentionedUserIds =
                new ArrayList<>();

        // メンション保存
        if (request.mentionedUserId() != null) {
            for (Long mentionedUserId :
                    request.mentionedUserId()) {

                UserEntity mentionedUser =
                        userMapper.findById(mentionedUserId)
                                .orElse(null);

                // 存在しないユーザーは無視
                if (mentionedUser == null) {
                    continue;
                }

                MessageMentionEntity mention =
                        new MessageMentionEntity();

                mention.setMessageId(message.getId());
                mention.setMentionedUserId(mentionedUser.getId());

                mentionMapper.insert(mention);

                // message_created 用
                mentionedUserIds.add(
                        mentionedUser.getId()
                );

                // メンション WebSocket通知
                Map<String, Object> mentionEventData =
                        new java.util.HashMap<>();

                mentionEventData.put(
                        "id",
                        mention.getId()
                );

                mentionEventData.put(
                        "mentioned_user_id",
                        mention.getMentionedUserId()
                );

                mentionEventData.put(
                        "created_at",
                        mention.getCreatedAt()
                );

                eventPublisher.sendMentionAdded(
                        mention.getMentionedUserId(),
                        message.getId(),
                        mentionEventData
                );

                // メンション操作ログ
                operationLogService.record(
                        requestUser,
                        OperationType.mention_created,
                        List.of(
                                "message_id=" +
                                        message.getId(),
                                "mentioned_user_id=" +
                                        mentionedUser.getId()
                        ),
                        List.of(
                                "username=" +
                                        mentionedUser.getUsername()
                        )
                );
            }
        }

        // message_created WebSocket通知
        Map<String, Object> eventData =
                new java.util.HashMap<>();

        eventData.put("id",message.getId());
        eventData.put("channel_id",message.getChannelId());
        eventData.put("user_id",message.getUserId());
        eventData.put("reply_to_message_id",replyToMessageId);
        eventData.put("content",message.getContent());
        eventData.put("mentioned_user_ids",mentionedUserIds);
        eventData.put("status",message.getStatus());
        eventData.put("created_at",message.getCreatedAt());
        eventData.put("updated_at","");

        eventPublisher.sendMessageCreated(
                channelId,
                eventData
        );

        // メッセージ作成ログ
        operationLogService.record(
                requestUser,
                OperationType.message_created,
                target,
                List.of(
                        "content=" +
                        message.getContent()
                )
        );

		List<MessageMentionEntity> mentions = mentionMapper.findByMessageId(message.getId());
        List<MentionResponseDTO> mentionDTOs = mentions.stream()
                .map(mm -> new MentionResponseDTO(
                        mm.getId(),
                        mm.getMentionedUserId(),
                        mm.getCreatedAt()
                ))
                .toList();

        return new MessageSummaryDTO(
                toResponse(message),
                new ArrayList<>(),
                mentionDTOs,
				new ArrayList<>()
        );
    }

    // 3. PUT /messages/{id}
    public MessageEditResponseDTO editMessage(Long messageId, HttpServletRequest httpRequest, MessageEditRequestDTO request) {
        String token = authUtil.getTokenFromCookie(httpRequest);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        MessageEntity message = messageMapper.findById(messageId)
                .orElseGet(() -> {
						appLog.warn("Message not found (message_id={})", messageId);
						throw new NotFoundException("Message not found");
					});

        if (message.getStatus() == 2) { // 2: 削除済み
            throw new NotFoundException("Message has been deleted");
        }
        
        // 投稿者本人 or admin のみ編集可能
        if (!message.getUserId().equals(requestUser.getId()) &&
                requestUser.getRole() != 0) { // Assuming role 0 is admin
            throw new ForbiddenException("Access denied");
        }

		// 文字数制限チェック
		if (request.content().length() > props.getMessage().getMaxLength()) {
			throw new IllegalArgumentException("Message content exceeds the maximum length");
		}

        String before = message.getContent();
        String after = request.content();

        // メッセージ本体更新
        message.setContent(after);
        message.setStatus(request.status());
        message.onUpdate();
        messageMapper.update(message);

        // WebSocket通知
        Map<String, Object> eventData = new java.util.HashMap<>();
        eventData.put("id", message.getId());
        eventData.put("channel_id", message.getChannelId());
        eventData.put("user_id", message.getUserId());
        eventData.put("reply_to_message_id", message.getReplyToMessageId());
        eventData.put("content", message.getContent());
        eventData.put("status", message.getStatus());
        eventData.put("created_at", message.getCreatedAt());
        eventData.put("updated_at", message.getUpdatedAt());

        eventPublisher.sendMessageUpdated(message.getChannelId(), eventData);

        // 操作ログ記録
        operationLogService.record(
            requestUser,
            OperationType.message_updated,
            List.of("message_id=" + message.getId()),
            List.of("before_content=" + before, "after_content=" + after)
        );

        return new MessageEditResponseDTO(
                toResponse(message)
        );
    }

    // 4. DELETE /messages/{id}
    public String deleteMessage(Long messageId, HttpServletRequest httpRequest) {
        String token = authUtil.getTokenFromCookie(httpRequest);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        MessageEntity message = messageMapper.findById(messageId)
                .orElseGet(() -> {
						appLog.warn("Message not found (message_id={})", messageId);
						throw new NotFoundException("Message not found");
					});

        if (message.getStatus() == 2) { // 2: 削除済み
            throw new NotFoundException("Message has been deleted");
        }
        
        // 投稿者本人 or admin のみ削除可能
        if (!message.getUserId().equals(requestUser.getId()) &&
                requestUser.getRole() != 0) { // Assuming role 0 is admin
            throw new ForbiddenException("Access denied");
        }

        message.setStatus(2); // 2: 削除
        messageMapper.update(message);

        // WebSocket通知
        eventPublisher.sendMessageDeleted(message.getChannelId(), Map.of(
                "id", message.getId(),
                "deleted", true,
                "updated_at", message.getUpdatedAt()
        ));

        // 操作ログ記録
        operationLogService.record(
            requestUser,
            OperationType.message_deleted,
            List.of("message_id=" + message.getId()),
            List.of("ok")
        );
        
        return "ok";
    }

    public List<MessageDTO> getReplies(Long messageId) {
         // 親メッセージ存在チェック
        messageMapper.findById(messageId)
            .orElseGet(() -> {
						appLog.warn("Message not found (message_id={})", messageId);
						throw new NotFoundException("Message not found");
					});

        // 返信一覧取得
        List<MessageEntity> replies = messageMapper.findByReplyToMessageId(messageId);

        return replies.stream()
            .map(this::toResponse)
            .toList();
    }


    // --- DTO 変換 ---
    private MessageDTO toResponse(MessageEntity m) {
        return new MessageDTO(
                m.getId(),
                m.getChannelId(),
                m.getUserId(),
                m.getReplyToMessageId(),
                m.getRequestId(),
                m.getContent(),
                m.getStatus(),
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }

    public boolean hasPrevious(Long channelId, Long lastMessageId) {
        return messageMapper.existsPreviousMessages(channelId, lastMessageId);
    }

}
