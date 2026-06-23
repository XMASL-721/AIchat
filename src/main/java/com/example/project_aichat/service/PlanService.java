package com.example.project_aichat.service;

import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.AiPlan;
import reactor.core.publisher.Flux;

import java.util.Map;

public interface PlanService {
    Flux<String> generatePlan(Long userId, String planType, Map<String, Object> inputData);
    PageResponse<AiPlan> getUserPlans(Long userId, int page, int size);
    AiPlan getPlanById(Long planId, Long userId);
    void deletePlan(Long planId, Long userId);
}
