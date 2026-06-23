package com.example.project_aichat.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.project_aichat.entity.Message;
import com.example.project_aichat.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 message 表的只读 ChatMemory。
 * get() 返回最近 N 条作为上下文；
 * add() 为空——消息已由 ChatServiceImpl 手动保存，避免重复。
 */
@Component
@RequiredArgsConstructor
public class DbChatMemory implements ChatMemory {

    private final MessageMapper messageMapper;

    /** 不重复保存——ChatServiceImpl 已手动 insert */
    @Override
    public void add(String conversationId, List<org.springframework.ai.chat.messages.Message> messages) {
    }

    @Override
    public List<org.springframework.ai.chat.messages.Message> get(String conversationId, int lastN) {
        Long convId = Long.parseLong(conversationId);
        List<Message> entities = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, convId)
                        .orderByDesc(Message::getCreatedAt)
                        .last("LIMIT " + lastN)
        );
        // 反转回时间正序
        Collections.reverse(entities);
        return entities.stream()
                .map(this::toSpringAiMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        Long convId = Long.parseLong(conversationId);
        messageMapper.delete(new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, convId));
    }

    private org.springframework.ai.chat.messages.Message toSpringAiMessage(Message entity) {
        return "user".equals(entity.getRole())
                ? new UserMessage(entity.getContent())
                : new AssistantMessage(entity.getContent());
    }
}
