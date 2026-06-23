package com.example.project_aichat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("workout")
public class Workout {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private String exercises;
    private Integer duration;
    private Integer calories;
    private String level;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
