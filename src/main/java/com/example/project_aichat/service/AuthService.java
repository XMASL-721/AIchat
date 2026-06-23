package com.example.project_aichat.service;

import com.example.project_aichat.dto.request.LoginRequest;
import com.example.project_aichat.dto.request.RegisterRequest;
import com.example.project_aichat.dto.response.LoginResponse;

public interface AuthService {
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}
