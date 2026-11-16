package com.example.app.service;

import com.example.app.domain.messageTemplate.MessageTemplateEntity;
import com.example.app.domain.messageTemplate.MessageTemplateMapper;
import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.messageTemplate.MessageTemplateCategoryEntity;
import com.example.app.domain.messageTemplate.MessageTemplateCategoryMapper;
import com.example.app.domain.user.UserEntity;
import com.example.app.dto.messageTemplate.*;
import com.example.app.exception.NotFoundException;
import com.example.app.security.AuthUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import com.example.app.exception.ForbiddenException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageTemplateService {
    private final JwtProvider jwtProvider;
    private final OperationLogService operationLogService;
    private final MessageTemplateMapper messageTemplateMapper;
    private final MessageTemplateCategoryMapper categoryMapper;
    private final AuthUtil authUtil;
    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");

    // --- CREATE ---
    public MessageTemplateResponseDTO createTemplate(MessageTemplateCreateRequestDTO req, HttpServletRequest httpReq) {
        if (req == null) {
            appLog.warn("Request body is required");
            throw new IllegalArgumentException("Request body is required");
        }
        if (req.title() == null || req.title().isBlank()) {
            appLog.warn("Title is required");
            throw new IllegalArgumentException("Title is required");
        }
        if (req.content() == null || req.content().isBlank()) {
            appLog.warn("Content is required");
            throw new IllegalArgumentException("Content is required");
        }
        if (req.categoryId() == null) {
            appLog.warn("CategoryId is required");
            throw new IllegalArgumentException("CategoryId is required");
        }

        String token = jwtProvider.extractTokenFromCookie(httpReq);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        if (req.isShared() && requestUser.getRole() != 0) {
            appLog.warn("Only admin can create shared templates");
            throw new ForbiddenException("Only admin can create shared templates");
        }

        MessageTemplateCategoryEntity category = categoryMapper.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        MessageTemplateEntity template = new MessageTemplateEntity();
        template.setUserId(requestUser.getId());
        template.setTitle(req.title());
        template.setContent(req.content());
        template.setCategoryId(category.getId());
        template.setShared(req.isShared());

        messageTemplateMapper.insert(template);

        operationLogService.record(
            requestUser,
            OperationType.message_template_created,
            List.of("template_id=" + template.getId()),
            List.of("title=" + template.getTitle(), "isShared=" + template.isShared())
        );

        return toResponse(template);
    }

    
    // --- UPDATE ---
    public MessageTemplateResponseDTO updateTemplate(Long id, MessageTemplateEditRequestDTO req, HttpServletRequest httpReq) {
        if (req == null) {
            appLog.warn("Request body is required");
            throw new IllegalArgumentException("Request body is required");
        }
        if (req.title() == null || req.title().isBlank()) {
            appLog.warn("Title is required");
            throw new IllegalArgumentException("Title is required");
        }
        if (req.content() == null || req.content().isBlank()) {
            appLog.warn("Content is required");
            throw new IllegalArgumentException("Content is required");
        }
        if (req.categoryId() == null) {
            appLog.warn("CategoryId is required");
            throw new IllegalArgumentException("CategoryId is required");
        }

        String token = jwtProvider.extractTokenFromCookie(httpReq);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        MessageTemplateEntity template = messageTemplateMapper.findById(id)
                .orElseGet(() -> {
                    appLog.warn("Template not found: {}", id);
                    throw new NotFoundException("Template not found");
                });

        if (template.isShared() && requestUser.getRole() != 0) {
            appLog.warn("Only admin can edit shared templates");
            throw new ForbiddenException("Only admin can edit shared templates");
        }

        if (!template.isShared() &&
            !template.getUserId().equals(requestUser.getId()) &&
            requestUser.getRole() != 0) {
            appLog.warn("Access denied");
            throw new ForbiddenException("Access denied");
        }

        MessageTemplateCategoryEntity category = categoryMapper.findById(req.categoryId())
                .orElseGet(() -> {
                    appLog.warn("Category not found: {}", req.categoryId());
                    throw new NotFoundException("Category not found");
                });

        template.setTitle(req.title());
        template.setContent(req.content());
        template.setCategoryId(category.getId());
        template.setShared(req.isShared());
        template.onUpdate();

        messageTemplateMapper.update(template);

        operationLogService.record(
            requestUser,
            OperationType.message_template_updated,
            List.of("template_id=" + template.getId()),
            List.of("title=" + template.getTitle(), "isShared=" + template.isShared())
        );

        return toResponse(template);
    }


    // --- DELETE ---
    public void deleteTemplate(Long id, HttpServletRequest httpReq) {
        String token = jwtProvider.extractTokenFromCookie(httpReq);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        MessageTemplateEntity template = messageTemplateMapper.findById(id)
                .orElseGet(() -> {
                    appLog.warn("Template not found: {}", id);
                    throw new NotFoundException("Template not found");
                });

        // isShared=true → 管理者のみ削除可能
        if (template.isShared() && requestUser.getRole() != 0) {
            appLog.warn("Only admin can delete shared templates");
            throw new ForbiddenException("Only admin can delete shared templates");
        }

        // isShared=false → 作成者本人 or admin のみ削除可能
        if (!template.isShared() &&
            !template.getUserId().equals(requestUser.getId()) &&
            requestUser.getRole() != 0) {
            appLog.warn("Access denied");
            throw new ForbiddenException("Access denied");
        }

        messageTemplateMapper.deleteById(template.getId());

        // 操作ログ記録
        operationLogService.record(
            requestUser,
            OperationType.message_template_deleted,
            List.of("template_id=" + template.getId()),
            List.of("ok")
        );
    }


    // --- GET ONE ---
    public MessageTemplateResponseDTO getTemplate(Long id, HttpServletRequest httpReq) {
        String token = jwtProvider.extractTokenFromCookie(httpReq);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        MessageTemplateEntity template = messageTemplateMapper.findById(id)
                .orElseGet(() -> {
                    appLog.warn("Template not found: {}", id);
                    throw new NotFoundException("Template not found");
                });

        // isShared=false → 作成者本人 or admin のみ閲覧可能
        if (!template.isShared() &&
            !template.getUserId().equals(requestUser.getId()) &&
            requestUser.getRole() != 0) {
            appLog.warn("Access denied");
            throw new ForbiddenException("Access denied");
        }

        return toResponse(template);
    }


    // --- GET LIST ---
    public List<MessageTemplateResponseDTO> getTemplates(Boolean shared, HttpServletRequest httpReq) {
        String token = jwtProvider.extractTokenFromCookie(httpReq);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        List<MessageTemplateEntity> list;

        if (shared != null && shared) {
            // isShared=true → 全員閲覧可能
            list = messageTemplateMapper.findByIsShared(true);
        } else if (shared != null && !shared) {
            // isShared=false → 本人 or admin のみ
            if (requestUser.getRole() == 0) {
                list = messageTemplateMapper.findByIsShared(false);
            } else if (requestUser.getRole() == 1) {
                list = messageTemplateMapper.findByUserIdAndIsShared(requestUser.getId(), false);
            } else {
                appLog.warn("No MessageTemplate found");
                throw new NotFoundException("No MessageTemplate found");
            }
            
        } else {
            appLog.warn("No MessageTemplate found");
            throw new NotFoundException("No MessageTemplate found");
        }

        return list.stream().map(this::toResponse).toList();
    }


    // --- DTO変換 ---
    private MessageTemplateResponseDTO toResponse(MessageTemplateEntity t) {
        return new MessageTemplateResponseDTO(
                t.getId(),
                t.getUserId(),
                t.getTitle(),
                t.getContent(),
                t.getCategoryId(),
                t.isShared(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
