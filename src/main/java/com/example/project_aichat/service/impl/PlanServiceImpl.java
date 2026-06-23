package com.example.project_aichat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.AiPlan;
import com.example.project_aichat.entity.User;
import com.example.project_aichat.mapper.AiPlanMapper;
import com.example.project_aichat.mapper.UserMapper;
import com.example.project_aichat.service.PlanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final AiPlanMapper aiPlanMapper;
    private final UserMapper userMapper;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Override
    public Flux<String> generatePlan(Long userId, String planType, Map<String, Object> inputData) {
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Flux.error(new RuntimeException("用户不存在"));
        }

        // 计算年龄
        int age = 0;
        if (user.getBirthday() != null) {
            age = Period.between(user.getBirthday(), LocalDate.now()).getYears();
        }

        // 构建提示词
        String prompt = buildPrompt(user, planType, inputData, age);

        // 调用 AI 生成计划
        Flux<String> planStream = chatClient.prompt()
                .system("你是一个专业的健身和营养规划师。请根据用户的个人信息和目标，生成详细的周计划。" +
                        "计划应该包含每日的餐食安排和训练安排，要具体到食物名称、份量、训练动作、组数等。" +
                        "使用 Markdown 格式输出，包含清晰的标题、列表和表格。")
                .user(prompt)
                .stream()
                .content();

        // 收集完整内容并保存到数据库
        StringBuilder fullContent = new StringBuilder();
        return planStream.doOnNext(fullContent::append)
                .doOnComplete(() -> {
                    try {
                        AiPlan plan = new AiPlan();
                        plan.setUserId(userId);
                        plan.setPlanType(planType);
                        plan.setInputData(objectMapper.writeValueAsString(inputData));
                        plan.setPlanContent(fullContent.toString());
                        aiPlanMapper.insert(plan);
                    } catch (Exception e) {
                        // 记录错误但不中断流
                    }
                });
    }

    private String buildPrompt(User user, String planType, Map<String, Object> inputData, int age) {
        StringBuilder sb = new StringBuilder();
        sb.append("请为我生成一份").append(getPlanTypeName(planType)).append("。\n\n");
        sb.append("【个人信息】\n");
        sb.append("- 性别：").append(user.getGender() != null && user.getGender() == 1 ? "男" : "女").append("\n");
        sb.append("- 年龄：").append(age > 0 ? age + "岁" : "未知").append("\n");
        sb.append("- 身高：").append(user.getHeight() != null ? user.getHeight() + "cm" : "未知").append("\n");
        sb.append("- 当前体重：").append(user.getWeight() != null ? user.getWeight() + "kg" : "未知").append("\n");
        sb.append("- 目标体重：").append(user.getTargetWeight() != null ? user.getTargetWeight() + "kg" : "未知").append("\n");

        if (inputData.containsKey("goal")) {
            sb.append("- 主要目标：").append(inputData.get("goal")).append("\n");
        }
        if (inputData.containsKey("duration")) {
            sb.append("- 计划周期：").append(inputData.get("duration")).append("\n");
        }
        if (inputData.containsKey("preferences")) {
            sb.append("- 偏好/限制：").append(inputData.get("preferences")).append("\n");
        }

        sb.append("\n【要求】\n");
        sb.append("1. 生成详细的每日计划（周一至周日）\n");
        sb.append("2. 每日包含3餐+加餐的完整餐食安排\n");
        sb.append("3. 每日包含训练安排（如有）\n");
        sb.append("4. 标注每餐的热量和主要营养素\n");
        sb.append("5. 使用表格形式展示每日计划\n");
        sb.append("6. 在计划后提供注意事项和建议\n");

        return sb.toString();
    }

    private String getPlanTypeName(String planType) {
        return switch (planType) {
            case "diet" -> "减脂饮食计划";
            case "workout" -> "健身训练计划";
            case "combined" -> "综合减脂计划（饮食+训练）";
            default -> "个性化计划";
        };
    }

    @Override
    public PageResponse<AiPlan> getUserPlans(Long userId, int page, int size) {
        Page<AiPlan> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AiPlan> wrapper = new LambdaQueryWrapper<AiPlan>()
                .eq(AiPlan::getUserId, userId)
                .orderByDesc(AiPlan::getCreatedAt);
        Page<AiPlan> result = aiPlanMapper.selectPage(pageParam, wrapper);
        return PageResponse.of(result);
    }

    @Override
    public AiPlan getPlanById(Long planId, Long userId) {
        AiPlan plan = aiPlanMapper.selectById(planId);
        if (plan == null) {
            throw new RuntimeException("计划不存在");
        }
        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此计划");
        }
        return plan;
    }

    @Override
    public void deletePlan(Long planId, Long userId) {
        AiPlan plan = getPlanById(planId, userId);
        aiPlanMapper.deleteById(plan.getId());
    }
}
