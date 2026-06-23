package com.example.project_aichat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY)
    private String phone;

    private String password;

    private String nickname;

    private String avatar;

    private String role;

    private Integer gender;

    private LocalDate birthday;

    private BigDecimal height;

    private BigDecimal weight;

    private BigDecimal targetWeight;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
