package com.example.project_aichat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.project_aichat.chat.DbChatMemory;
import com.example.project_aichat.entity.Conversation;
import com.example.project_aichat.entity.Message;
import com.example.project_aichat.mapper.ConversationMapper;
import com.example.project_aichat.mapper.MessageMapper;
import com.example.project_aichat.service.ChatService;
import com.example.project_aichat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final DbChatMemory dbChatMemory;
    private final ConversationService conversationService;

    /** 上下文窗口大小：保留最近 10 条对话 */
    private static final int CONTEXT_WINDOW_SIZE = 10;

    @Override
    public Flux<String> streamChat(Long conversationId, String userMessage, Long userId) {
        // 1. 验证会话归属
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) {
            return Flux.error(new RuntimeException("会话不存在"));
        }
        if (!conv.getUserId().equals(userId)) {
            return Flux.error(new RuntimeException("无权操作此会话"));
        }

        // 2. 保存用户消息到数据库
        Message userMsgEntity = new Message();
        userMsgEntity.setConversationId(conversationId);
        userMsgEntity.setRole("user");
        userMsgEntity.setContent(userMessage);
        messageMapper.insert(userMsgEntity);

        // 3. 判断是否首次对话（用于异步生成标题）
        Long msgCount = messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
        );
        boolean isFirstMessage = "新对话".equals(conv.getTitle()) && msgCount <= 1;

        // 4. 构建 Advisor：自动从 DB 注入历史上下文
        MessageChatMemoryAdvisor advisor = new MessageChatMemoryAdvisor(
                dbChatMemory,
                conversationId.toString(),
                CONTEXT_WINDOW_SIZE
        );

        // 5. 调用 Spring AI 流式接口
        Flux<String> aiStream = chatClient.prompt()
                .system("你是「小V」，一个专业的健身塑形和减脂餐制作专家。\n" +
                        "【你的身份设定】\n" +
                        "你的名字是小V。你精通健身训练、形体塑造、减脂增肌，同时也是减脂餐制作的大师。\n" +
                        "全程以小V的身份和口吻说话，自称「小V」，用第一人称回复用户。\n" +
                        "比如开头可以说「小V建议你…」「小V来帮你解答…」「根据小V的经验…」等。\n" +
                        "如果用户问你是谁，回答：「我是小V，你的专属健身塑形和减脂餐专家~」\n\n" +
                        "【你可以回答的范围】\n" +
                        "1. 减脂餐的食材清单、做法步骤和烹饪技巧\n" +
                        "2. 健身训练建议、塑形方法和运动计划\n" +
                        "3. 减脂增肌期间的营养搭配和饮食计划\n" +
                        "4. 卡路里计算和热量控制建议\n" +
                        "5. 健康饮食和减肥相关咨询\n\n" +
                        "【重要规则】\n" +
                        "1. 如果用户想吃的菜不在你的菜谱库里，你可以根据用户描述的口味、食材或需求，\n" +
                        "   自己创作生成新的减脂餐食谱，包括菜名、食材清单和详细做法步骤。\n" +
                        "2. 如果用户提问与健身/减脂餐/健康饮食完全无关的内容（如政治、科技、娱乐等），\n" +
                        "   请友好地回复：「抱歉，小V是健身和减脂餐专家，换个相关话题试试吧😊」\n" +
                        "请用简洁、友好、专业的中文回答。回答应清晰有条理，适当使用分段。")
                .user(userMessage)
                .advisors(advisor)
                .stream()
                .content();

        // 6. 将流式结果保存到数据库，返回包装后的 Flux
        return Flux.create(emitter -> {
            StringBuilder fullContent = new StringBuilder();

            aiStream.subscribe(
                    token -> {
                        fullContent.append(token);
                        emitter.next(token);
                    },
                    error -> {
                        emitter.error(error);
                    },
                    () -> {
                        // 7. 流结束，保存 AI 完整回复到数据库
                        if (fullContent.length() > 0) {
                            Message aiMsgEntity = new Message();
                            aiMsgEntity.setConversationId(conversationId);
                            aiMsgEntity.setRole("assistant");
                            aiMsgEntity.setContent(fullContent.toString());
                            messageMapper.insert(aiMsgEntity);
                        }

                        // 8. 首次对话异步生成标题
                        if (isFirstMessage) {
                            conversationService.generateTitle(conversationId, userMessage);
                        }

                        emitter.complete();
                    }
            );
        });
    }
}
