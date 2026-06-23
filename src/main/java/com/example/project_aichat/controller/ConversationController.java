package com.example.project_aichat.controller;

import com.example.project_aichat.dto.response.ApiResponse;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Conversation;
import com.example.project_aichat.entity.Message;
import com.example.project_aichat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 获取历史会话列表（分页）
     */
    @GetMapping
    public ApiResponse<PageResponse<Conversation>> listConversations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        PageResponse<Conversation> result = conversationService.listConversations(userId, page, size);
        return ApiResponse.success(result);
    }

    /**
     * 新建会话
     */
    @PostMapping
    public ApiResponse<Conversation> createConversation(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Conversation conv = conversationService.createConversation(userId);
        return ApiResponse.success(conv);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteConversation(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        conversationService.deleteConversation(id, userId);
        return ApiResponse.success();
    }

    /**
     * 获取会话的消息历史（分页）
     */
    @GetMapping("/{id}/messages")
    public ApiResponse<PageResponse<Message>> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        PageResponse<Message> result = conversationService.getMessages(id, userId, page, size);
        return ApiResponse.success(result);
    }
}
