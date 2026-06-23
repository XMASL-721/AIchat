package com.example.project_aichat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 配置。
 * ChatClient.Builder 由 Spring AI AutoConfiguration 自动注入，
 * 这里只需暴露 ChatClient Bean 供业务代码使用。
 */
@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
