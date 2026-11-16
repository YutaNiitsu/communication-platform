package com.example.app.controller;

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

import com.example.app.config.AppProperties;
import com.example.app.dto.config.AppConfigResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/configs")
@RequiredArgsConstructor
public class ConfigController {

    private final AppProperties appProperties;

    @GetMapping
    public ResponseEntity<AppConfigResponseDTO> getConfig() {
        AppConfigResponseDTO response =
            new AppConfigResponseDTO(

                new AppConfigResponseDTO.Message(
                    appProperties.getMessage().getMaxLength(),
                    appProperties.getMessage().getPageRange()
                ),

                new AppConfigResponseDTO.Attachment(
                    appProperties.getAttachment().getAllowedExtensions(),
                    appProperties.getAttachment().getMaxFileCount(),
                    appProperties.getAttachment().getMaxFileSizeMb(),
                    appProperties.getAttachment().getMaxTotalSizeMb()
                ),

                new AppConfigResponseDTO.Storage(
                    appProperties.getStorage().getWarnThresholdPercent(),
                    appProperties.getStorage().getDeleteThresholdPercent()
                )
            );

        return ResponseEntity.ok(response);
    }
}