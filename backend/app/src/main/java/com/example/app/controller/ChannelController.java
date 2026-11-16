package com.example.app.controller;

import com.example.app.dto.channel.*;
import com.example.app.service.ChannelService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.app.dto.channel.*;
import com.example.app.service.ChannelService;

import java.util.List;

@RestController
@RequestMapping("/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @GetMapping
    public List<ChannelResponseDTO> findAll() {
        return channelService.findAll();
    }

    @GetMapping("/{id}")
    public ChannelResponseDTO findById(@PathVariable Long id) {
        return channelService.findById(id);
    }

    @PostMapping
    public ChannelResponseDTO create(
        @RequestBody ChannelCreateRequestDTO dto,
        HttpServletRequest request
    ) {
        return channelService.create(dto, request);
    }

    @PostMapping("/{id}/join")
    public ChannelMemberResponseDTO join(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestBody JoinChannelRequestDTO joinRequest
    ) {
        return channelService.join(id, request, joinRequest);
    }

    @PostMapping("/{id}/leave")
    public void leave(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestBody LeaveChannelRequestDTO leaveRequest
    ) {
        channelService.leave(id, request, leaveRequest);
    }

    @PutMapping("/{id}")
    public ChannelEditResponseDTO update(
            @PathVariable Long id,
            @RequestBody ChannelEditRequestDTO dto,
            HttpServletRequest request
    ) {
        return channelService.edit(id, dto, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(channelService.delete(id, request));
    }

    @GetMapping("/{id}/members")
    public List<ChannelMemberResponseDTO> getMembers(@PathVariable Long id) {
        return channelService.getMembers(id);
    }
}