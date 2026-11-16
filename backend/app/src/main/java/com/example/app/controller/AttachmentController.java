package com.example.app.controller;

import com.example.app.domain.attachment.*;
import com.example.app.dto.attachment.AttachmentDTO;
import com.example.app.service.AttachmentService;
import com.example.app.service.LocalFileStorage;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final LocalFileStorage fileStorage;

    /**
     * 添付ファイルアップロード
     */
    @PostMapping("/messages/{messageId}/attachments")
    public ResponseEntity<AttachmentDTO> upload(
        @PathVariable Long messageId,
        @RequestParam("file") MultipartFile file,
        HttpServletRequest httpRequest
    ) {
        AttachmentDTO attachment = attachmentService.uploadAttachment(messageId, file, httpRequest);
        return ResponseEntity.ok(attachment);
    }

    /**
     * 添付ファイルダウンロード
     */
    @GetMapping("/files/download/{attachmentId}")
    public ResponseEntity<Resource> getFile(@PathVariable Long attachmentId) {
        return attachmentService.downloadAttachment(attachmentId);
    }

    /**
     * サムネイルダウンロード
     */
    @GetMapping("/thumbnails/{storedFileName}")
    public ResponseEntity<Resource> getThumbnail(@PathVariable String storedFileName) {
        Resource resource = fileStorage.getThumbnail(storedFileName);

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG) // PNGなら変更
            .body(resource);
    }

    /**
     * 添付ファイル削除
     */
    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<?> delete(
        @PathVariable Long attachmentId,
        HttpServletRequest httpRequest
    ) {
        attachmentService.deleteAttachment(attachmentId, httpRequest);
        return ResponseEntity.ok().build();
    }

    
}
