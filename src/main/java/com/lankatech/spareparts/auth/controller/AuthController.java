package com.lankatech.spareparts.auth.controller;

import com.lankatech.spareparts.auth.entity.User;
import com.lankatech.spareparts.auth.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            return ResponseEntity.status(401).build();
        }

        User user = userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow();

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("userId", user.getUserId());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().getRoleName());

        return ResponseEntity.ok(response);
    }
}