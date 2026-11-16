package com.example.app.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecureTestController {

    @GetMapping("/secure/test")
    public String secureTest() {
        return "OK";
    }
}
