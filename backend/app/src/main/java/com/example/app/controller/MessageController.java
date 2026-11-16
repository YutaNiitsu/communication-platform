package com.example.app.controller;

import com.example.app.dto.message.*;
import com.example.app.service.MessageService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // 1. GET /channels/{id}/messages
    @GetMapping("/channels/{channelId}/messages")
    public MessageResponseDTO getMessages(
        @PathVariable Long channelId,
        @RequestParam(required = false, name = "message_id") Long messageId,
        @RequestParam(required = false) Integer range
    ) {
        return messageService.getMessages(channelId, messageId, range);
    }

    // 2. POST /channels/{id}/messages
    @PostMapping("/channels/{channelId}/messages")
    public MessageSummaryDTO postMessage(
        @PathVariable Long channelId,
        @RequestBody MessagePostRequestDTO request,
        HttpServletRequest httpRequest
    ) {
        return messageService.createMessage(channelId, httpRequest, request);
    }

    // 3. PUT /messages/{id}
    @PutMapping("/messages/{messageId}")
    public MessageEditResponseDTO update(
            @PathVariable Long messageId,
            @RequestBody MessageEditRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        return messageService.editMessage(messageId, httpRequest, request);
    }

    // 4. DELETE /messages/{id}
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<?> delete(
            @PathVariable Long messageId,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(messageService.deleteMessage(messageId, httpRequest));
    }

    @GetMapping("/messages/{id}/replies")
    public List<MessageDTO> getReplies(@PathVariable Long messageId) {
        return messageService.getReplies(messageId);
    }
}
