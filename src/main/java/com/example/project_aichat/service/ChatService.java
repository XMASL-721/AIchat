package com.example.project_aichat.service;

import reactor.core.publisher.Flux;

public interface ChatService {
    Flux<String> streamChat(Long conversationId, String message, Long userId);
}
