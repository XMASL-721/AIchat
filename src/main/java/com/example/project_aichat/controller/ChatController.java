package com.example.project_aichat.controller;

import com.example.project_aichat.dto.request.ChatRequest;
import com.example.project_aichat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * 核心对话控制器。
 * 使用 Spring MVC 原生 SseEmitter 实现 SSE 流式输出。
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /** SSE 超时 5 分钟 */
    private static final long SSE_TIMEOUT = 300_000L;

    @PostMapping("/stream")
    public SseEmitter streamChat(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        chatService.streamChat(request.getConversationId(), request.getMessage(), userId)
                .subscribe(
                        token -> send(emitter, token),
                        error -> {
                            send(emitter, "[ERROR]" + error.getMessage());
                            emitter.complete();
                        },
                        () -> {
                            send(emitter, "[DONE]");
                            emitter.complete();
                        }
                );

        emitter.onTimeout(() -> {
            send(emitter, "[ERROR]AI 响应超时，请稍后重试");
            emitter.complete();
        });

        return emitter;
    }

    private void send(SseEmitter emitter, String data) {
        try {
            emitter.send(data);
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
