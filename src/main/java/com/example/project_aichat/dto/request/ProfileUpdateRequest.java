package com.example.project_aichat.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProfileUpdateRequest {
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private BigDecimal height;
    private BigDecimal weight;
    private BigDecimal targetWeight;
}
