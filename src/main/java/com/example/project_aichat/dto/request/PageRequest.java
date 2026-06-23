package com.example.project_aichat.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageRequest {
    @Min(value = 1, message = "页码最小为 1")
    private int page = 1;

    @Min(value = 1, message = "每页条数最小为 1")
    private int size = 20;
}
