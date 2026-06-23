package com.example.project_aichat.service;

import com.example.project_aichat.dto.request.ProfileUpdateRequest;
import com.example.project_aichat.entity.User;

public interface UserService {
    void changePassword(Long userId, String oldPassword, String newPassword);

    User getProfile(Long userId);

    void updateProfile(Long userId, ProfileUpdateRequest request);
}
