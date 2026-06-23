package com.example.project_aichat.controller;

import com.example.project_aichat.dto.response.ApiResponse;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Supplement;
import com.example.project_aichat.service.SupplementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supplements")
@RequiredArgsConstructor
public class SupplementController {

    private final SupplementService supplementService;

    @GetMapping
    public ApiResponse<PageResponse<Supplement>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return ApiResponse.success(supplementService.listAll(page, size, keyword, category));
    }

    @GetMapping("/{id}")
    public ApiResponse<Supplement> get(@PathVariable Long id) {
        return ApiResponse.success(supplementService.getById(id));
    }
}
