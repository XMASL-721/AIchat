package com.example.project_aichat.controller;

import com.example.project_aichat.dto.response.ApiResponse;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Meal;
import com.example.project_aichat.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;

    @GetMapping
    public ApiResponse<PageResponse<Meal>> listMeals(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(mealService.listAll(page, size, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<Meal> getMeal(@PathVariable Long id) {
        return ApiResponse.success(mealService.getById(id));
    }

    @GetMapping("/random")
    public ApiResponse<Meal> getRandomMeal() {
        return ApiResponse.success(mealService.getRandom());
    }
}
