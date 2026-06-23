package com.example.project_aichat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Conversation;
import com.example.project_aichat.entity.Message;
import com.example.project_aichat.mapper.ConversationMapper;
import com.example.project_aichat.mapper.MessageMapper;
import com.example.project_aichat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final ChatClient chatClient;

    @Override
    public Conversation createConversation(Long userId) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle("新对话");
        conversationMapper.insert(conversation);
        return conversation;
    }

    @Override
    public PageResponse<Conversation> listConversations(Long userId, int page, int size) {
        Page<Conversation> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUserId, userId)
                .orderByDesc(Conversation::getUpdatedAt);

        Page<Conversation> result = conversationMapper.selectPage(pageParam, wrapper);
        return PageResponse.of(result);
    }

    @Override
    public void deleteConversation(Long id, Long userId) {
        // 验证归属
        Conversation conv = conversationMapper.selectById(id);
        if (conv == null) {
            throw new RuntimeException("会话不存在");
        }
        if (!conv.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此会话");
        }
        // 级联删除（外键 ON DELETE CASCADE 会自动删消息）
        conversationMapper.deleteById(id);
    }

    @Override
    public PageResponse<Message> getMessages(Long conversationId, Long userId, int page, int size) {
        // 验证会话归属
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) {
            throw new RuntimeException("会话不存在");
        }
        if (!conv.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看此会话");
        }

        Page<Message> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, conversationId)
                .orderByAsc(Message::getCreatedAt);

        Page<Message> result = messageMapper.selectPage(pageParam, wrapper);
        return PageResponse.of(result);
    }

    @Override
    @Async
    public void generateTitle(Long conversationId, String firstMessage) {
        try {
            // 尝试用 AI 生成标题
            String aiTitle = chatClient.prompt()
                    .system("你是一个会话摘要助手。请为以下用户的第一条消息生成一个简短标题（5-15字），直接返回标题，不要有任何其他内容。")
                    .user(firstMessage)
                    .call()
                    .content();

            if (aiTitle != null && !aiTitle.isBlank()) {
                // 清理 AI 输出（去掉可能的引号）
                aiTitle = aiTitle.replaceAll("[\"\"''\\n]", "").trim();
                if (aiTitle.length() > 50) {
                    aiTitle = aiTitle.substring(0, 50);
                }
                Conversation conv = conversationMapper.selectById(conversationId);
                if (conv != null) {
                    conv.setTitle(aiTitle);
                    conversationMapper.updateById(conv);
                }
            }
        } catch (Exception e) {
            // AI 生成失败时使用默认标题
            String fallback = firstMessage.length() > 20
                    ? firstMessage.substring(0, 20) + "..."
                    : firstMessage;
            Conversation conv = conversationMapper.selectById(conversationId);
            if (conv != null && "新对话".equals(conv.getTitle())) {
                conv.setTitle(fallback);
                conversationMapper.updateById(conv);
            }
        }
    }
}
