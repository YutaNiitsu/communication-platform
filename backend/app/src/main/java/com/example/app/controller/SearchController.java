package com.example.app.controller;

import com.example.app.dto.search.SearchRequestDTO;
import com.example.app.dto.search.SearchResponseDTO;
import com.example.app.service.SearchService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * GET /search
     * メッセージ検索API
     */
    @GetMapping("/search")
    public List<SearchResponseDTO> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, name = "channel_id") Long channelId,
            @RequestParam(required = false, name = "user_id") Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request
    ) {
        SearchRequestDTO req = new SearchRequestDTO(
                keyword,
                channelId,
                userId,
                from,
                to
        );

        return searchService.search(req, request);
    }
}