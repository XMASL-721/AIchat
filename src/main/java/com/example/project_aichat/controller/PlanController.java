package com.example.project_aichat.controller;

import com.example.project_aichat.dto.response.ApiResponse;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.AiPlan;
import com.example.project_aichat.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generatePlan(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        String planType = (String) request.get("planType");
        Map<String, Object> inputData = (Map<String, Object>) request.get("inputData");

        return planService.generatePlan(userId, planType, inputData)
                .map(token -> "data: " + token + "\n\n")
                .concatWithValues("data: [DONE]\n\n");
    }

    @GetMapping
    public ApiResponse<PageResponse<AiPlan>> getUserPlans(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        PageResponse<AiPlan> plans = planService.getUserPlans(userId, page, size);
        return ApiResponse.success(plans);
    }

    @GetMapping("/{id}")
    public ApiResponse<AiPlan> getPlan(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        AiPlan plan = planService.getPlanById(id, userId);
        return ApiResponse.success(plan);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePlan(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        planService.deletePlan(id, userId);
        return ApiResponse.success();
    }
}
