package com.example.project_aichat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("supplement")
public class Supplement {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String emoji;

    private String category;

    private String description;

    private String benefits;

    private String dosage;

    private String timing;

    private String sideEffects;

    private String suitableFor;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
