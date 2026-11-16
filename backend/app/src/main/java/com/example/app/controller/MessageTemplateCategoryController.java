package com.example.app.controller;

import com.example.app.dto.messageTemplate.*;
import com.example.app.service.MessageTemplateCategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message_template_categories")
public class MessageTemplateCategoryController {

    private final MessageTemplateCategoryService categoryService;

    public MessageTemplateCategoryController(MessageTemplateCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // --- CREATE ---
    @PostMapping
    public MessageTemplateCategoryResponseDTO create(
            @RequestBody MessageTemplateCategoryCreateRequestDTO body,
            HttpServletRequest req
    ) {
        return categoryService.createCategory(body, req);
    }

    // --- UPDATE ---
    @PutMapping("/{id}")
    public MessageTemplateCategoryResponseDTO update(
            @PathVariable Long id,
            @RequestBody MessageTemplateCategoryEditRequestDTO body,
            HttpServletRequest req
    ) {
        return categoryService.updateCategory(id, body, req);
    }

    // --- DELETE ---
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id,
            HttpServletRequest req
    ) {
        categoryService.deleteCategory(id, req);
    }

    // --- GET ONE ---
    @GetMapping("/{id}")
    public MessageTemplateCategoryResponseDTO getOne(
            @PathVariable Long id,
            HttpServletRequest req
    ) {
        return categoryService.getCategory(id, req);
    }

    // --- GET LIST ---
    @GetMapping
    public List<MessageTemplateCategoryResponseDTO> list(
            @RequestParam(required = false,  name = "is_shared") Boolean isShared,
            HttpServletRequest req
    ) {
        return categoryService.getCategories(isShared, req);
    }
}
