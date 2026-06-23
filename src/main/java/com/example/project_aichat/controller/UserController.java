package com.example.project_aichat.controller;

import com.example.project_aichat.dto.request.PasswordRequest;
import com.example.project_aichat.dto.request.ProfileUpdateRequest;
import com.example.project_aichat.dto.response.ApiResponse;
import com.example.project_aichat.entity.User;
import com.example.project_aichat.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody PasswordRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return ApiResponse.success();
    }

    @GetMapping("/profile")
    public ApiResponse<User> getProfile(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        User user = userService.getProfile(userId);
        return ApiResponse.success(user);
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        userService.updateProfile(userId, request);
        return ApiResponse.success();
    }
}
