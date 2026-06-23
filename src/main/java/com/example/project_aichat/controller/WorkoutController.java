package com.example.project_aichat.controller;

import com.example.project_aichat.dto.response.ApiResponse;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Workout;
import com.example.project_aichat.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    @GetMapping
    public ApiResponse<PageResponse<Workout>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return ApiResponse.success(workoutService.listAll(page, size, keyword, category));
    }

    @GetMapping("/{id}")
    public ApiResponse<Workout> get(@PathVariable Long id) {
        return ApiResponse.success(workoutService.getById(id));
    }

    @GetMapping("/random")
    public ApiResponse<Workout> getRandom() {
        return ApiResponse.success(workoutService.getRandom());
    }
}
