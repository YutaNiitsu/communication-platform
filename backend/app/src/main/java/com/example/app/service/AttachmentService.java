package com.example.app.service;

import com.example.app.config.AppProperties;
import com.example.app.domain.attachment.AttachmentEntity;
import com.example.app.domain.attachment.AttachmentMapper;
import com.example.app.domain.message.MessageEntity;
import com.example.app.domain.message.MessageMapper;
import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.user.UserEntity;
import com.example.app.dto.attachment.AttachmentDTO;
import com.example.app.dto.storage.StorageDeleteResponseDTO;
import com.example.app.exception.ForbiddenException;
import com.example.app.exception.NotFoundException;
import com.example.app.security.AuthUtil;
import com.example.app.websocket.WebSocketEventPublisher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AppProperties props;
    private final StorageUsageChecker usageChecker;
    private final LocalFileStorage fileStorage;
    private final ThumbnailGenerator thumbnailGenerator;
    private final AttachmentMapper attachmentMapper;
    private final MessageMapper messageMapper;
    private final WebSocketEventPublisher eventPublisher;
    private final FileNameGenerator fileNameGenerator;
    private final OperationLogService operationLogService;
    private final AuthUtil authUtil;
    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");


    public AttachmentDTO uploadAttachment(Long messageId, MultipartFile file, HttpServletRequest httpRequest) {
        String token = authUtil.getTokenFromCookie(httpRequest);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        MessageEntity message = messageMapper.findById(messageId)
                .orElseGet(() -> {
						appLog.warn("Message not found (message_id={})", messageId);
						throw new NotFoundException("Message not found");
					});

        // 1. ストレージ使用率チェック
        int usage = usageChecker.getUsagePercent();
 
        if (usage >= props.getStorage().getDeleteThresholdPercent()) {
            appLog.warn("Storage usage exceeded delete threshold: " + usage + "%");
            throw new IllegalArgumentException("Storage usage exceeded delete threshold: " + usage + "%");
        }

        // 2. 拡張子・MIMEチェック
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            appLog.warn("File must have extension: " + originalName);
            throw new IllegalArgumentException("File must have extension");
        }
        String lower = originalName.toLowerCase();

        String ext;
        if (lower.endsWith(".tar.gz")) {
            ext = "tar.gz";
        } else {
            ext = lower.substring(lower.lastIndexOf('.') + 1);
        }

        if (!props.getAttachment().getAllowedExtensions().contains(ext)) {
            appLog.warn("Extension not allowed: " + ext);
            throw new IllegalArgumentException("Extension not allowed: " + ext);
        }

        String mime = file.getContentType();
        if (mime == null || !props.getAttachment().getAllowedMimeTypes().contains(mime)) {
            appLog.warn("MIME not allowed: " + mime);
            throw new IllegalArgumentException("MIME not allowed: " + mime);
        }

        // 3. 容量チェック
        long size = file.getSize();
        long maxFileBytes = props.getAttachment().getMaxFileSizeMb() * 1024L * 1024L;
        if (size > maxFileBytes) {
            appLog.warn("File too large: " + size + " bytes, max allowed: " + maxFileBytes + " bytes");
            throw new IllegalArgumentException("File too large");
        }

        // 4. ランダムファイル名生成
        String storedFileName = fileNameGenerator.generate(ext);

        // 5. 本体保存
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            appLog.error("Failed to read file bytes: " + e.getMessage());
            throw new RuntimeException("Failed to read file", e);
        }
        String fileUrl = fileStorage.save(bytes, storedFileName);

        // 6. サムネイル生成（画像のみ）
        String thumbnailUrl = null;
        if (isImage(ext)) {
            thumbnailUrl = thumbnailGenerator.generate(bytes)
                    .map(thBytes -> {
                        String thumbName = storedFileName + ".thumb.jpg";
                        return fileStorage.saveThumbnail(thBytes, thumbName);
                    })
                    .orElse(null);
        }

        // 7. DB登録
        AttachmentEntity attachment = new AttachmentEntity();
        attachment.setMessageId(messageId);
        attachment.setStoredFileName(storedFileName);
        attachment.setOriginalFileName(originalName);
        attachment.setFileUrl(fileUrl);
        attachment.setThumbnailUrl(thumbnailUrl);
        attachment.setFileType(resolveFileType(ext));
        attachment.setFileSize(size);
        attachment.setCreatedAt(LocalDateTime.now());

        attachmentMapper.insert(attachment);

        AttachmentDTO attachmentDTO = new AttachmentDTO(
                attachment.getId(),
                attachment.getMessageId(),
                attachment.getOriginalFileName(),
                attachment.getStoredFileName(),
                attachment.getFileUrl(),
                attachment.getThumbnailUrl(),
                attachment.getFileType(),
                attachment.getFileSize(),
                attachment.getCreatedAt()
        );

        appLog.info("Uploaded attachment: " + originalName + " (stored as: " + storedFileName + ")" + ",fileType: " + attachment.getFileType() + ", size: " + size + " bytes, messageId: " + messageId);

        // 8. WebSocket通知
        eventPublisher.publishAttachmentAdded(message.getChannelId(), attachmentDTO);

        appLog.info("Published WebSocket event for attachment added: " + attachmentDTO.id() + " to channel: " + message.getChannelId());

        operationLogService.record(
                requestUser,
                OperationType.attachment_added,
                List.of(
                        "message_id=" +
                                messageId
                        ),
                List.of(
                        "attachment_id=" + attachment.getId(),
                        "fileName=" + originalName,
                        "fileType=" + attachment.getFileType(),
                        "fileSize=" + attachment.getFileSize()
                )
        );

        return attachmentDTO;
    }

    public void deleteAttachment(Long attachmentId, HttpServletRequest httpRequest) {
        String token = authUtil.getTokenFromCookie(httpRequest);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        AttachmentEntity attachment = attachmentMapper.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));
        
        MessageEntity message = messageMapper.findById(attachment.getMessageId())
                .orElseGet(() -> {
						appLog.warn("Message not found (message_id={})", attachment.getMessageId());
						throw new NotFoundException("Message not found");
					});
        
        // 投稿者本人 or admin のみ削除可能
        if (!message.getUserId().equals(requestUser.getId()) &&
                requestUser.getRole() != 0) { // Assuming role 0 is admin
            throw new ForbiddenException("Access denied");
        }

        fileStorage.delete(attachment.getStoredFileName());
        appLog.info("Deleted attachment: " + attachment.getOriginalFileName());
        if (attachment.getThumbnailUrl() != null) {
            // thumbnailUrl からファイル名だけ抜く実装でもOK
            String thumbName = attachment.getStoredFileName() + ".thumb.jpg";
            fileStorage.deleteThumbnail(thumbName);
            appLog.info("Deleted thumbnail: " + thumbName);
        }

        attachmentMapper.delete(attachment.getId());

        String reason = "user_deleted";

        eventPublisher.publishAttachmentRemoved(message.getChannelId(), attachment.getMessageId(), attachment.getId(), reason);

        operationLogService.record(
                requestUser,
                OperationType.attachment_removed,
                List.of(
                        "attachment_id=" + attachment.getId()
                        ),
                List.of("ok")
        );
    }

    public StorageDeleteResponseDTO cleanupStorage() {
        int usage = usageChecker.getUsagePercent();
        if (usage < props.getStorage().getDeleteThresholdPercent()) {
            return new StorageDeleteResponseDTO(0, usage);
        }

        int deleted = 0;
        while (usage >= props.getStorage().getWarnThresholdPercent()) {
            List<AttachmentEntity> oldest =
                    attachmentMapper.findOrderByCreatedAtAsc();
            if (oldest.isEmpty()) break;

            for (AttachmentEntity a : oldest) {;
                fileStorage.delete(a.getStoredFileName());
                appLog.info("Deleted attachment: " + a.getOriginalFileName());
                if (a.getThumbnailUrl() != null) {
                    // thumbnailUrl からファイル名だけ抜く実装でもOK
                    String thumbName = a.getStoredFileName() + ".thumb.jpg";
                    fileStorage.deleteThumbnail(thumbName);
                    appLog.info("Deleted thumbnail: " + thumbName);
                }

                attachmentMapper.delete(a.getId());

                String reason = "system_cleanup";
                MessageEntity message = messageMapper.findById(a.getMessageId())
                                            .orElseThrow(() -> new IllegalArgumentException("Not found"));
                eventPublisher.publishAttachmentRemoved(message.getChannelId(), a.getMessageId(), a.getId(), reason);

                deleted++;
            }
            usage = usageChecker.getUsagePercent();
        }

        StorageDeleteResponseDTO response = new StorageDeleteResponseDTO(deleted, usage);
        return response;
    }

    public ResponseEntity<Resource> downloadAttachment(Long attachmentId) {
        AttachmentEntity attachment = attachmentMapper.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));

        Resource resource = fileStorage.get(attachment.getStoredFileName());

        String ext = attachment.getStoredFileName().substring(attachment.getStoredFileName().lastIndexOf('.') + 1).toLowerCase();

        MediaType mediaType = switch (ext) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "png" -> MediaType.IMAGE_PNG;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };

        String disposition = ext.equals("pdf")
                ? "inline"   // PDF はプレビュー表示
                : "attachment"; // その他はダウンロード

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        disposition + "; filename=\"" + UriUtils.encode(attachment.getOriginalFileName(), StandardCharsets.UTF_8) + "\"")
                .body(resource);
    }

    private boolean isImage(String ext) {
        return switch (ext.toLowerCase()) {
            case "jpg", "jpeg", "png", "gif", "webp" -> true;
            default -> false;
        };
    }

    private String resolveFileType(String ext) {
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "webp" -> "image";
            case "pdf" -> "pdf";
            case "docx" -> "word";
            case "xlsx" -> "excel";
            default -> "other";
        };
    }

}
