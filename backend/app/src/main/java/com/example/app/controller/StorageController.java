package com.example.app.controller;

import com.example.app.dto.storage.StorageDeleteResponseDTO;
import com.example.app.dto.storage.StorageUsageResponseDTO;
import com.example.app.service.AttachmentService;
import com.example.app.service.StorageUsageChecker;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/storages")
@RequiredArgsConstructor
public class StorageController {

    private final StorageUsageChecker usageChecker;
    private final AttachmentService attachmentService;

    /**
     * ストレージ使用率取得 API
     * GET /storages/usage
     */
    @GetMapping("/usage")
    public ResponseEntity<StorageUsageResponseDTO> getUsage() {

        int percent = usageChecker.getUsagePercent();

        StorageUsageResponseDTO response = new StorageUsageResponseDTO(percent);

        return ResponseEntity.ok(response);
    }

    /**
     * ストレージ容量削除（手動実行）
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<StorageDeleteResponseDTO> cleanup() {
        StorageDeleteResponseDTO response = attachmentService.cleanupStorage();
        return ResponseEntity.ok(response);
    }
}
