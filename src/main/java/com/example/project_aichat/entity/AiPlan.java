package com.example.project_aichat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_plan")
public class AiPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String planType;
    private String inputData;
    private String planContent;
    private LocalDateTime createdAt;
}
