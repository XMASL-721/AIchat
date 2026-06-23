package com.example.project_aichat.service;

import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Conversation;
import com.example.project_aichat.entity.Message;

public interface ConversationService {
    Conversation createConversation(Long userId);

    PageResponse<Conversation> listConversations(Long userId, int page, int size);

    void deleteConversation(Long id, Long userId);

    PageResponse<Message> getMessages(Long conversationId, Long userId, int page, int size);

    void generateTitle(Long conversationId, String firstMessage);
}
