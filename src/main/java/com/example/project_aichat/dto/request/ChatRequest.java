package com.example.project_aichat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatRequest {
    @NotNull(message = "会话 ID 不能为空")
    private Long conversationId;

    @NotBlank(message = "消息内容不能为空")
    private String message;
}
