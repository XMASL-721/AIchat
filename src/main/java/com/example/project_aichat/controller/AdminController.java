package com.example.project_aichat.controller;

import com.example.project_aichat.dto.response.ApiResponse;
import com.example.project_aichat.entity.Article;
import com.example.project_aichat.entity.Meal;
import com.example.project_aichat.entity.Supplement;
import com.example.project_aichat.entity.Workout;
import com.example.project_aichat.mapper.ArticleMapper;
import com.example.project_aichat.mapper.MealMapper;
import com.example.project_aichat.mapper.SupplementMapper;
import com.example.project_aichat.mapper.WorkoutMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final MealMapper mealMapper;
    private final WorkoutMapper workoutMapper;
    private final ArticleMapper articleMapper;
    private final SupplementMapper supplementMapper;

    // ==================== 减脂餐管理 ====================
    @PostMapping("/meals")
    public ApiResponse<Meal> addMeal(@RequestBody Meal meal) {
        mealMapper.insert(meal);
        return ApiResponse.success(meal);
    }

    @PutMapping("/meals/{id}")
    public ApiResponse<Meal> updateMeal(@PathVariable Long id, @RequestBody Meal meal) {
        meal.setId(id);
        mealMapper.updateById(meal);
        return ApiResponse.success(meal);
    }

    @DeleteMapping("/meals/{id}")
    public ApiResponse<Void> deleteMeal(@PathVariable Long id) {
        mealMapper.deleteById(id);
        return ApiResponse.success();
    }

    // ==================== 健身训练管理 ====================
    @PostMapping("/workouts")
    public ApiResponse<Workout> addWorkout(@RequestBody Workout workout) {
        workoutMapper.insert(workout);
        return ApiResponse.success(workout);
    }

    @PutMapping("/workouts/{id}")
    public ApiResponse<Workout> updateWorkout(@PathVariable Long id, @RequestBody Workout workout) {
        workout.setId(id);
        workoutMapper.updateById(workout);
        return ApiResponse.success(workout);
    }

    @DeleteMapping("/workouts/{id}")
    public ApiResponse<Void> deleteWorkout(@PathVariable Long id) {
        workoutMapper.deleteById(id);
        return ApiResponse.success();
    }

    // ==================== 健康资讯管理 ====================
    @PostMapping("/articles")
    public ApiResponse<Article> addArticle(@RequestBody Article article) {
        articleMapper.insert(article);
        return ApiResponse.success(article);
    }

    @PutMapping("/articles/{id}")
    public ApiResponse<Article> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        article.setId(id);
        articleMapper.updateById(article);
        return ApiResponse.success(article);
    }

    @DeleteMapping("/articles/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable Long id) {
        articleMapper.deleteById(id);
        return ApiResponse.success();
    }

    // ==================== 补剂指南管理 ====================
    @PostMapping("/supplements")
    public ApiResponse<Supplement> addSupplement(@RequestBody Supplement supplement) {
        supplementMapper.insert(supplement);
        return ApiResponse.success(supplement);
    }

    @PutMapping("/supplements/{id}")
    public ApiResponse<Supplement> updateSupplement(@PathVariable Long id, @RequestBody Supplement supplement) {
        supplement.setId(id);
        supplementMapper.updateById(supplement);
        return ApiResponse.success(supplement);
    }

    @DeleteMapping("/supplements/{id}")
    public ApiResponse<Void> deleteSupplement(@PathVariable Long id) {
        supplementMapper.deleteById(id);
        return ApiResponse.success();
    }
}
