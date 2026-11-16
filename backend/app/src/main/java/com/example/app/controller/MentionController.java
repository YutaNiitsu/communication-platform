package com.example.app.controller;

import com.example.app.dto.user.UserResponseDTO;
import com.example.app.service.MentionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mention_candidates")
public class MentionController {

    private final MentionService mentionService;

    public MentionController(MentionService mentionService) {
        this.mentionService = mentionService;
    }

    @GetMapping
    public List<UserResponseDTO> getCandidates(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long channel_id
    ) {
        return mentionService.getCandidates(query, channel_id);
    }
}
