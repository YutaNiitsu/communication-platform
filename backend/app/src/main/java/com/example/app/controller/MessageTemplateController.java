package com.example.app.controller;

import com.example.app.dto.messageTemplate.*;
import com.example.app.service.MessageTemplateService;
import com.example.app.service.UserService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/message_templates")
public class MessageTemplateController {

    private final MessageTemplateService messageTemplateService;

    public MessageTemplateController(MessageTemplateService messageTemplateService, UserService userService) {
        this.messageTemplateService = messageTemplateService;
    }

    // --- CREATE ---
    @PostMapping
    public MessageTemplateResponseDTO create(
            HttpServletRequest req,
            @RequestBody MessageTemplateCreateRequestDTO body
    ) {
        return messageTemplateService.createTemplate(body, req);
    }

    // --- UPDATE ---
    @PutMapping("/{id}")
    public MessageTemplateResponseDTO update(
            @PathVariable Long id,
            HttpServletRequest req,
            @RequestBody MessageTemplateEditRequestDTO body
    ) {
        return messageTemplateService.updateTemplate(id, body, req);
    }

    // --- DELETE ---
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id,
            HttpServletRequest req
    ) {
        messageTemplateService.deleteTemplate(id, req);
    }

    // --- GET ONE ---
    @GetMapping("/{id}")
    public MessageTemplateResponseDTO getOne(
        @PathVariable Long id,
        HttpServletRequest req
    ) {
        return messageTemplateService.getTemplate(id, req);
    }

    // --- GET LIST ---
    @GetMapping
    public List<MessageTemplateResponseDTO> list(
            @RequestParam(required = false,  name = "is_shared") Boolean isShared,
            HttpServletRequest req
    ) {
        return messageTemplateService.getTemplates(isShared, req);
    }
}
