package com.example.app.controller;

import com.example.app.dto.login.LoginRequestDTO;
import com.example.app.dto.login.LoginResponseDTO;
import com.example.app.dto.user.UserResponseDTO;
import com.example.app.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.app.dto.login.LoginRequestDTO;
import com.example.app.dto.login.LoginResponseDTO;
import com.example.app.dto.message.MessagePostRequestDTO;
import com.example.app.dto.message.MessageDTO;
import com.example.app.dto.user.UserResponseDTO;
import com.example.app.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody @Valid LoginRequestDTO request,
            HttpServletResponse response
    ) {
        LoginResponseDTO dto = authService.login(request, response);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.logout(request, response));
    }
    
    @GetMapping("/me")
    public UserResponseDTO me(Authentication auth) {
        String username = auth.getName();
        return authService.getCurrentUserDTO(username);
    }

}
