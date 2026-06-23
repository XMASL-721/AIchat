package com.example.project_aichat.controller;

import com.example.project_aichat.dto.response.ApiResponse;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.AiPlan;
import com.example.project_aichat.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    private static final long SSE_TIMEOUT = 300_000L;

    @PostMapping("/generate")
    public SseEmitter generatePlan(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        String planType = (String) request.get("planType");
        Map<String, Object> inputData = (Map<String, Object>) request.get("inputData");

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        planService.generatePlan(userId, planType, inputData)
                .subscribe(
                        token -> send(emitter, token),
                        error -> {
                            send(emitter, "[ERROR]" + error.getMessage());
                            emitter.complete();
                        },
                        () -> {
                            send(emitter, "[DONE]");
                            emitter.complete();
                        }
                );

        emitter.onTimeout(() -> {
            send(emitter, "[ERROR]生成超时，请重试");
            emitter.complete();
        });

        return emitter;
    }

    private void send(SseEmitter emitter, String data) {
        try {
            emitter.send(SseEmitter.event().data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
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
