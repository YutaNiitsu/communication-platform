package com.example.app.controller;

import com.example.app.dto.reaction.ReactionResponseDTO;
import com.example.app.service.ReactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
public class ReactionController {

    private final ReactionService reactionService;

    public ReactionController(ReactionService reactionService) {
        this.reactionService = reactionService;
    }

    // POST /messages/{id}/reactions
    @PostMapping("/{id}/reactions")
    public ReactionResponseDTO addReaction(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest req
    ) {
        return reactionService.addReaction(id, body.get("reaction_type"), req);
    }

    // DELETE /messages/{id}/reactions
    @DeleteMapping("/{id}/reactions")
    public void removeReaction(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest req
    ) {
        reactionService.removeReaction(id, body.get("reaction_type"), req);
    }

    // GET /messages/{id}/reactions
    @GetMapping("/{id}/reactions")
    public List<Map<String, Object>> getReactions(@PathVariable Long id) {
        return reactionService.getReactions(id);
    }
}
