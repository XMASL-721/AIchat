package com.example.project_aichat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("meal")
public class Meal {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String ingredients;

    private String steps;

    private String imageUrl;

    private Integer calories;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
