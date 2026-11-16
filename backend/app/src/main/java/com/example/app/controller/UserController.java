package com.example.app.controller;

import com.example.app.dto.user.UserCreateRequestDTO;
import com.example.app.dto.user.UserUpdateRequestDTO;
import com.example.app.dto.user.UserResponseDTO;
import com.example.app.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 指定したユーザー情報を取得
    @GetMapping("/{id}")
    public UserResponseDTO getUser(
        @PathVariable Long id,
        HttpServletRequest request
    ) {
        return userService.getUser(id, request);
    }

    // ユーザー一覧を取得
    @GetMapping
    public List<UserResponseDTO> getAllUsers(HttpServletRequest request) {
        return userService.getAllUsers(request);
    }

    // 管理者が新規ユーザーを登録
    @PostMapping
    public UserResponseDTO createUser(@RequestBody UserCreateRequestDTO dto, HttpServletRequest request) {
        return userService.createUser(dto, request);
    }

    // 管理者が既存ユーザー情報を変更
    @PutMapping("/{id}")
    public UserResponseDTO updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequestDTO dto,
            HttpServletRequest request
    ) {
        return userService.updateUser(id, dto, request);
    }

    // 管理者がユーザーを削除
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(userService.deleteUser(id, request));
    }
}