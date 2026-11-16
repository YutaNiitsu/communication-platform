package com.example.app.service;

import com.example.app.domain.messageTemplate.MessageTemplateCategoryEntity;
import com.example.app.domain.messageTemplate.MessageTemplateCategoryMapper;
import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.user.UserEntity;
import com.example.app.domain.user.UserMapper;
import com.example.app.dto.messageTemplate.*;
import com.example.app.exception.NotFoundException;
import com.example.app.security.AuthUtil;
import com.example.app.exception.ConflictException;
import com.example.app.exception.ForbiddenException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageTemplateCategoryService {

    private final MessageTemplateCategoryMapper categoryMapper;
    private final JwtProvider jwtProvider;
    private final OperationLogService operationLogService;
    private final AuthUtil authUtil;
    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");

    // --- CREATE ---
    public MessageTemplateCategoryResponseDTO createCategory(
            MessageTemplateCategoryCreateRequestDTO req,
            HttpServletRequest httpReq
    ) {
        String token = jwtProvider.extractTokenFromCookie(httpReq);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        // isShared=true → 管理者のみ作成可能
        if (req.isShared() && requestUser.getRole() != 0) {
            appLog.warn("Only admin can create shared categories");
            throw new ForbiddenException("Only admin can create shared categories");
        }

        if (categoryMapper.existsByName(req.name())) {
            appLog.warn("Name already exists: {}", req.name());
            throw new ConflictException("Name already exists");
        }

        MessageTemplateCategoryEntity category = new MessageTemplateCategoryEntity();
        category.setUserId(requestUser.getId());
        category.setName(req.name());
        category.setShared(req.isShared());

        categoryMapper.insert(category);

        // 操作ログ記録
        operationLogService.record(
            requestUser,
            OperationType.message_template_category_created,
            List.of("category_id=" + category.getId()),
            List.of("name=" + category.getName(), "isShared=" + category.isShared())
        );

        return toResponse(category);
    }

    // --- UPDATE ---
    public MessageTemplateCategoryResponseDTO updateCategory(
            Long id,
            MessageTemplateCategoryEditRequestDTO req,
            HttpServletRequest httpReq
    ) {
        String token = jwtProvider.extractTokenFromCookie(httpReq);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        MessageTemplateCategoryEntity category = categoryMapper.findById(id)
                .orElseGet(() -> {
                    appLog.warn("Category not found: {}", id);
                    throw new NotFoundException("Category not found");
                });

        // isShared=true → 管理者のみ編集可能
        if (category.isShared() && requestUser.getRole() != 0) {
            appLog.warn("Only admin can edit shared categories");
            throw new ForbiddenException("Only admin can edit shared categories");
        }

        // isShared=false → 作成者本人のみ編集可能
        if (!category.isShared() &&
            !category.getUserId().equals(requestUser.getId())) {
            appLog.warn("Access denied");
            throw new ForbiddenException("Access denied");
        }

        if (categoryMapper.existsByName(req.name())) {
            appLog.warn("Name already exists: {}", req.name());
            throw new ConflictException("Name already exists");
        }

        category.setName(req.name());
        category.setShared(req.isShared());
        category.onUpdate();

        categoryMapper.insert(category);

        // 操作ログ記録
        operationLogService.record(
            requestUser,
            OperationType.message_template_category_updated,
            List.of("category_id=" + category.getId()),
            List.of("name=" + category.getName(), "isShared=" + category.isShared())
        );

        return toResponse(category);
    }

    // --- DELETE ---
    public void deleteCategory(Long id, HttpServletRequest httpReq) {
        String token = jwtProvider.extractTokenFromCookie(httpReq);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        MessageTemplateCategoryEntity category = categoryMapper.findById(id)
                .orElseGet(() -> {
                    appLog.warn("Category not found: {}", id);
                    throw new NotFoundException("Category not found");
                });

        // isShared=true → 管理者のみ削除可能
        if (category.isShared() && requestUser.getRole() != 0) {
            appLog.warn("Only admin can delete shared categories");
            throw new ForbiddenException("Only admin can delete shared categories");
        }

        // isShared=false → 作成者本人のみ削除可能
        if (!category.isShared() &&
            !category.getUserId().equals(requestUser.getId())) {
            appLog.warn("Access denied");
            throw new ForbiddenException("Access denied");
        }

        // 操作ログ記録
        operationLogService.record(
            requestUser,
            OperationType.message_template_category_deleted,
            List.of("category_id=" + category.getId()),
            List.of("ok")
        );

        categoryMapper.deleteById(category.getId());
    }

    // --- GET ONE ---
    public MessageTemplateCategoryResponseDTO getCategory(Long id, HttpServletRequest httpReq) {
        String token = jwtProvider.extractTokenFromCookie(httpReq);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        MessageTemplateCategoryEntity category = categoryMapper.findById(id)
                .orElseGet(() -> {
                    appLog.warn("Category not found: {}", id);
                    throw new NotFoundException("Category not found");
                });

        // isShared=false → 作成者本人のみ閲覧可能
        if (!category.isShared() &&
            !category.getUserId().equals(requestUser.getId())) {
            appLog.warn("Access denied");
            throw new ForbiddenException("Access denied");
        }

        return toResponse(category);
    }

    // --- GET LIST ---
    public List<MessageTemplateCategoryResponseDTO> getCategories(
            Boolean shared,
            HttpServletRequest httpReq
    ) {
        String token = jwtProvider.extractTokenFromCookie(httpReq);
        UserEntity requestUser = authUtil.getUserFromToken(token);

        List<MessageTemplateCategoryEntity> list;

        if (shared != null && shared) {
            // isShared=true → 全員閲覧可能
            list = categoryMapper.findByIsShared(true);
        } else if (shared != null && !shared) {
            // isShared=false → 本人のみ
            if (requestUser.getRole() == 1) {
                list = categoryMapper.findByUserIdAndIsShared(requestUser.getId(), false);
            } else {
                appLog.warn("No MessageTemplateCategory found");
                throw new NotFoundException("No MessageTemplateCategory found");
            }
            
        } else {
            appLog.warn("No MessageTemplateCategory found");
            throw new NotFoundException("No MessageTemplateCategory found");
        }

        return list.stream().map(this::toResponse).toList();
    }

    // --- DTO変換 ---
    private MessageTemplateCategoryResponseDTO toResponse(MessageTemplateCategoryEntity c) {
        return new MessageTemplateCategoryResponseDTO(
                c.getId(),
                c.getUserId(),
                c.getName(),
                c.isShared(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
