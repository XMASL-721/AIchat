package com.example.project_aichat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserInfo userInfo;

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String phone;
        private String nickname;
        private String avatar;
        private String role;
    }
}
