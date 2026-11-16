package com.example.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.app.domain.messageTemplate.MessageTemplateCategoryEntity;
import com.example.app.domain.messageTemplate.MessageTemplateCategoryMapper;
import com.example.app.domain.messageTemplate.MessageTemplateEntity;
import com.example.app.domain.messageTemplate.MessageTemplateMapper;
import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.user.UserEntity;
import com.example.app.domain.user.UserMapper;
import com.example.app.dto.messageTemplate.MessageTemplateCategoryResponseDTO;
import com.example.app.dto.messageTemplate.MessageTemplateCreateRequestDTO;
import com.example.app.dto.messageTemplate.MessageTemplateEditRequestDTO;
import com.example.app.dto.messageTemplate.MessageTemplateResponseDTO;
import com.example.app.exception.ForbiddenException;
import com.example.app.exception.NotFoundException;
import com.example.app.security.AuthUtil;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class MessageTemplateServiceTest {

    @Mock JwtProvider jwtProvider;
    @Mock OperationLogService operationLogService;
    @Mock MessageTemplateMapper templateMapper;
    @Mock MessageTemplateCategoryMapper categoryMapper;
    @Mock AuthUtil authUtil;
    @Mock HttpServletRequest httpReq;

    MessageTemplateService service;

    @BeforeEach
    void setup() {
        service = new MessageTemplateService(jwtProvider, operationLogService, templateMapper, categoryMapper, authUtil);
        lenient().when(jwtProvider.extractTokenFromCookie(httpReq)).thenReturn("token");
    }

    @Test
    @DisplayName("1-1: 個人テンプレート作成成功")
    void testCreateTemplate_personal() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRole(1); // 一般ユーザー
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        MessageTemplateCategoryEntity cat = new MessageTemplateCategoryEntity();
        cat.setId(10L);
        when(categoryMapper.findById(10L)).thenReturn(Optional.of(cat));

        MessageTemplateCreateRequestDTO req =
                new MessageTemplateCreateRequestDTO("title", "content", 10L, false);

        doAnswer(inv -> {
            MessageTemplateEntity t = inv.getArgument(0);
            t.setId(100L);
            return null;
        }).when(templateMapper).insert(any());

        MessageTemplateResponseDTO res = service.createTemplate(req, httpReq);

        assertThat(res.id()).isEqualTo(100L);
        assertThat(res.isShared()).isFalse();
    }

    @Test
    @DisplayName("1-2: 共有テンプレート作成成功（管理者）")
    void testCreateTemplate_shared_admin() {
        UserEntity admin = new UserEntity();
        admin.setId(1L);
        admin.setRole(0); // admin
        when(authUtil.getUserFromToken("token")).thenReturn(admin);

        MessageTemplateCategoryEntity cat = new MessageTemplateCategoryEntity();
        cat.setId(10L);
        when(categoryMapper.findById(10L)).thenReturn(Optional.of(cat));

        MessageTemplateCreateRequestDTO req =
                new MessageTemplateCreateRequestDTO("title", "content", 10L, true);

        service.createTemplate(req, httpReq);

        verify(templateMapper).insert(any());
    }

    @Test
    @DisplayName("1-3: req=null → IllegalArgumentException")
    void testCreateTemplate_reqNull() {
        assertThatThrownBy(() -> service.createTemplate(null, httpReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Request body is required");
    }

    @Test
    @DisplayName("1-4: title=null → IllegalArgumentException")
    void testCreateTemplate_titleNull() {
        MessageTemplateCreateRequestDTO req =
                new MessageTemplateCreateRequestDTO(null, "content", 10L, false);

        assertThatThrownBy(() -> service.createTemplate(req, httpReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Title is required");
    }

    @Test
    @DisplayName("1-5: content=null → IllegalArgumentException")
    void testCreateTemplate_contentNull() {
        MessageTemplateCreateRequestDTO req =
                new MessageTemplateCreateRequestDTO("title", null, 10L, false);

        assertThatThrownBy(() -> service.createTemplate(req, httpReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Content is required");
    }

    @Test
    @DisplayName("1-6: categoryId=null → IllegalArgumentException")
    void testCreateTemplate_categoryIdNull() {
        MessageTemplateCreateRequestDTO req =
                new MessageTemplateCreateRequestDTO("title", "content", null, false);

        assertThatThrownBy(() -> service.createTemplate(req, httpReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CategoryId is required");
    }

    @Test
    @DisplayName("1-8: 一般ユーザーが共有テンプレート作成 → ForbiddenException")
    void testCreateTemplate_shared_user_forbidden() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRole(1); // 一般
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        MessageTemplateCreateRequestDTO req =
                new MessageTemplateCreateRequestDTO("title", "content", 10L, true);

        assertThatThrownBy(() -> service.createTemplate(req, httpReq))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Only admin can create shared templates");
    }

    // ---------------------------------------------------------
    // 2. updateTemplate
    // ---------------------------------------------------------

    @Test
    @DisplayName("2-1: 個人テンプレート編集成功（本人）")
    void testUpdateTemplate_personal_owner() {

        UserEntity owner = new UserEntity();
        owner.setId(1L);
        owner.setRole(1); // 一般ユーザー
        when(authUtil.getUserFromToken("token")).thenReturn(owner);

        MessageTemplateEntity template = new MessageTemplateEntity();
        template.setId(100L);
        template.setUserId(1L); // 本人
        template.setShared(false);
        template.setTitle("old");
        template.setContent("old-content");
        template.setCategoryId(10L);

        when(templateMapper.findById(100L)).thenReturn(Optional.of(template));

        MessageTemplateCategoryEntity category = new MessageTemplateCategoryEntity();
        category.setId(20L);
        when(categoryMapper.findById(20L)).thenReturn(Optional.of(category));

        MessageTemplateEditRequestDTO req =
                new MessageTemplateEditRequestDTO("new-title", "new-content", 20L, false);

        MessageTemplateResponseDTO res = service.updateTemplate(100L, req, httpReq);

        assertThat(res.title()).isEqualTo("new-title");
        assertThat(res.content()).isEqualTo("new-content");
        verify(templateMapper).update(any());
        verify(operationLogService).record(eq(owner),
                eq(OperationType.message_template_updated), anyList(), anyList());
    }

    @Test
    @DisplayName("2-2: 管理者が共有テンプレート編集成功")
    void testUpdateTemplate_shared_admin() {

        UserEntity admin = new UserEntity();
        admin.setId(99L);
        admin.setRole(0); // admin
        when(authUtil.getUserFromToken("token")).thenReturn(admin);

        MessageTemplateEntity template = new MessageTemplateEntity();
        template.setId(100L);
        template.setUserId(1L);
        template.setShared(true);
        template.setTitle("old");
        template.setContent("old-content");
        template.setCategoryId(10L);

        when(templateMapper.findById(100L)).thenReturn(Optional.of(template));

        MessageTemplateCategoryEntity category = new MessageTemplateCategoryEntity();
        category.setId(20L);
        when(categoryMapper.findById(20L)).thenReturn(Optional.of(category));

        MessageTemplateEditRequestDTO req =
                new MessageTemplateEditRequestDTO("new-title", "new-content", 20L, true);

        service.updateTemplate(100L, req, httpReq);

        verify(templateMapper).update(any());
    }

    @Test
    @DisplayName("2-3: req=null → IllegalArgumentException")
    void testUpdateTemplate_reqNull() {

        assertThatThrownBy(() -> service.updateTemplate(100L, null, httpReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Request body is required");
    }

    @Test
    @DisplayName("2-4: title=null → IllegalArgumentException")
    void testUpdateTemplate_titleNull() {

        MessageTemplateEditRequestDTO req =
                new MessageTemplateEditRequestDTO(null, "content", 10L, false);

        assertThatThrownBy(() -> service.updateTemplate(100L, req, httpReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Title is required");
    }

    @Test
    @DisplayName("2-5: content=null → IllegalArgumentException")
    void testUpdateTemplate_contentNull() {

        MessageTemplateEditRequestDTO req =
                new MessageTemplateEditRequestDTO("title", null, 10L, false);

        assertThatThrownBy(() -> service.updateTemplate(100L, req, httpReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Content is required");
    }

    @Test
    @DisplayName("2-6: categoryId=null → IllegalArgumentException")
    void testUpdateTemplate_categoryIdNull() {

        MessageTemplateEditRequestDTO req =
                new MessageTemplateEditRequestDTO("title", "content", null, false);

        assertThatThrownBy(() -> service.updateTemplate(100L, req, httpReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CategoryId is required");
    }

    @Test
    @DisplayName("2-7: テンプレート不存在 → NotFoundException")
    void testUpdateTemplate_templateNotFound() {

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRole(1);
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        when(templateMapper.findById(999L)).thenReturn(Optional.empty());

        MessageTemplateEditRequestDTO req =
                new MessageTemplateEditRequestDTO("title", "content", 10L, false);

        assertThatThrownBy(() -> service.updateTemplate(999L, req, httpReq))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Template not found");
    }

    @Test
    @DisplayName("2-8: カテゴリ不存在 → NotFoundException")
    void testUpdateTemplate_categoryNotFound() {

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRole(1);
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        MessageTemplateEntity template = new MessageTemplateEntity();
        template.setId(100L);
        template.setUserId(1L);
        template.setShared(false);
        when(templateMapper.findById(100L)).thenReturn(Optional.of(template));

        when(categoryMapper.findById(999L)).thenReturn(Optional.empty());

        MessageTemplateEditRequestDTO req =
                new MessageTemplateEditRequestDTO("title", "content", 999L, false);

        assertThatThrownBy(() -> service.updateTemplate(100L, req, httpReq))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    @DisplayName("2-9: 一般ユーザーが共有テンプレート編集 → ForbiddenException")
    void testUpdateTemplate_shared_user_forbidden() {

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRole(1); // 一般
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        MessageTemplateEntity template = new MessageTemplateEntity();
        template.setId(100L);
        template.setUserId(99L);
        template.setShared(true); // 共有テンプレート
        when(templateMapper.findById(100L)).thenReturn(Optional.of(template));

        MessageTemplateEditRequestDTO req =
                new MessageTemplateEditRequestDTO("title", "content", 10L, true);

        assertThatThrownBy(() -> service.updateTemplate(100L, req, httpReq))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Only admin can edit shared templates");
    }

    @Test
    @DisplayName("2-10: 他人の個人テンプレート編集 → ForbiddenException")
    void testUpdateTemplate_personal_other_forbidden() {

        UserEntity user = new UserEntity();
        user.setId(2L);
        user.setRole(1); // 一般
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        MessageTemplateEntity template = new MessageTemplateEntity();
        template.setId(100L);
        template.setUserId(1L); // owner=1
        template.setShared(false);
        when(templateMapper.findById(100L)).thenReturn(Optional.of(template));

        MessageTemplateEditRequestDTO req =
                new MessageTemplateEditRequestDTO("title", "content", 10L, false);

        assertThatThrownBy(() -> service.updateTemplate(100L, req, httpReq))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Access denied");
    }

    // ---------------------------------------------------------
    // 3. deleteTemplate
    // ---------------------------------------------------------

    @Test
    @DisplayName("3-1: 個人テンプレート削除成功（本人）")
    void testDeleteTemplate_personal_owner() {

        UserEntity owner = new UserEntity();
        owner.setId(1L);
        owner.setRole(1); // 一般ユーザー
        when(authUtil.getUserFromToken("token")).thenReturn(owner);

        MessageTemplateEntity template = new MessageTemplateEntity();
        template.setId(100L);
        template.setUserId(1L); // 本人
        template.setShared(false);

        when(templateMapper.findById(100L)).thenReturn(Optional.of(template));

        service.deleteTemplate(100L, httpReq);

        verify(templateMapper).deleteById(100L);
        verify(operationLogService).record(eq(owner),
                eq(OperationType.message_template_deleted), anyList(), anyList());
    }

    @Test
    @DisplayName("3-2: 管理者が共有テンプレート削除成功")
    void testDeleteTemplate_shared_admin() {

        UserEntity admin = new UserEntity();
        admin.setId(99L);
        admin.setRole(0); // admin
        when(authUtil.getUserFromToken("token")).thenReturn(admin);

        MessageTemplateEntity template = new MessageTemplateEntity();
        template.setId(100L);
        template.setUserId(1L);
        template.setShared(true); // 共有テンプレート

        when(templateMapper.findById(100L)).thenReturn(Optional.of(template));

        service.deleteTemplate(100L, httpReq);

        verify(templateMapper).deleteById(100L);
    }

    @Test
    @DisplayName("3-3: 一般ユーザーが共有テンプレート削除 → ForbiddenException")
    void testDeleteTemplate_shared_user_forbidden() {

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRole(1); // 一般ユーザー
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        MessageTemplateEntity template = new MessageTemplateEntity();
        template.setId(100L);
        template.setUserId(99L);
        template.setShared(true); // 共有テンプレート

        when(templateMapper.findById(100L)).thenReturn(Optional.of(template));

        assertThatThrownBy(() -> service.deleteTemplate(100L, httpReq))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Only admin can delete shared templates");
    }

    @Test
    @DisplayName("3-4: 他人の個人テンプレート削除 → ForbiddenException")
    void testDeleteTemplate_personal_other_forbidden() {

        UserEntity user = new UserEntity();
        user.setId(2L);
        user.setRole(1); // 一般ユーザー
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        MessageTemplateEntity template = new MessageTemplateEntity();
        template.setId(100L);
        template.setUserId(1L); // owner=1
        template.setShared(false);

        when(templateMapper.findById(100L)).thenReturn(Optional.of(template));

        assertThatThrownBy(() -> service.deleteTemplate(100L, httpReq))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    @DisplayName("3-5: テンプレート不存在 → NotFoundException")
    void testDeleteTemplate_notFound() {

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRole(1);
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        when(templateMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteTemplate(999L, httpReq))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Template not found");
    }

    // ---------------------------------------------------------
    // 4. getTemplate
    // ---------------------------------------------------------

    @Test
    @DisplayName("4-1: 共有テンプレート閲覧成功（全員閲覧可能）")
    void testGetTemplate_shared() {

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRole(1); // 一般ユーザー
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        MessageTemplateEntity template = new MessageTemplateEntity();
        template.setId(100L);
        template.setUserId(99L);
        template.setShared(true); // 共有テンプレート

        when(templateMapper.findById(100L)).thenReturn(Optional.of(template));

        MessageTemplateResponseDTO res = service.getTemplate(100L, httpReq);

        assertThat(res.id()).isEqualTo(100L);
        assertThat(res.isShared()).isTrue();
    }

    @Test
    @DisplayName("4-2: 個人テンプレート閲覧成功（本人）")
    void testGetTemplate_personal_owner() {

        UserEntity owner = new UserEntity();
        owner.setId(1L);
        owner.setRole(1);
        when(authUtil.getUserFromToken("token")).thenReturn(owner);

        MessageTemplateEntity template = new MessageTemplateEntity();
        template.setId(100L);
        template.setUserId(1L); // 本人
        template.setShared(false);

        when(templateMapper.findById(100L)).thenReturn(Optional.of(template));

        MessageTemplateResponseDTO res = service.getTemplate(100L, httpReq);

        assertThat(res.userId()).isEqualTo(1L);
        assertThat(res.isShared()).isFalse();
    }

    @Test
    @DisplayName("4-3: 他人の個人テンプレート閲覧 → ForbiddenException")
    void testGetTemplate_personal_other_forbidden() {

        UserEntity user = new UserEntity();
        user.setId(2L); // owner ではない
        user.setRole(1); // 一般ユーザー
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        MessageTemplateEntity template = new MessageTemplateEntity();
        template.setId(100L);
        template.setUserId(1L); // owner=1
        template.setShared(false);

        when(templateMapper.findById(100L)).thenReturn(Optional.of(template));

        assertThatThrownBy(() -> service.getTemplate(100L, httpReq))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    @DisplayName("4-4: テンプレート不存在 → NotFoundException")
    void testGetTemplate_notFound() {

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRole(1);
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        when(templateMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTemplate(999L, httpReq))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Template not found");
    }

    // ---------------------------------------------------------
    // 5. getTemplates
    // ---------------------------------------------------------

    @Test
    @DisplayName("5-1: 共有テンプレート一覧取得（shared=true → 全員閲覧可能）")
    void testGetTemplates_sharedTrue() {

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRole(1); // 一般ユーザー
        when(authUtil.getUserFromToken("token")).thenReturn(user);

        MessageTemplateEntity t1 = new MessageTemplateEntity();
        t1.setId(100L);
        t1.setShared(true);

        when(templateMapper.findByIsShared(true)).thenReturn(List.of(t1));

        List<MessageTemplateResponseDTO> res = service.getTemplates(true, httpReq);

        assertThat(res).hasSize(1);
        assertThat(res.get(0).isShared()).isTrue();
    }

    
}
