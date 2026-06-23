package com.example.project_aichat.controller;

import com.example.project_aichat.dto.response.ApiResponse;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.UserFavorite;
import com.example.project_aichat.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public ApiResponse<UserFavorite> addFavorite(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        String targetType = (String) request.get("targetType");
        Long targetId = Long.parseLong(request.get("targetId").toString());
        UserFavorite favorite = favoriteService.addFavorite(userId, targetType, targetId);
        return ApiResponse.success(favorite);
    }

    @DeleteMapping
    public ApiResponse<Void> removeFavorite(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        favoriteService.removeFavorite(userId, targetType, targetId);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<PageResponse<UserFavorite>> getUserFavorites(
            @RequestParam(required = false) String targetType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        PageResponse<UserFavorite> favorites = favoriteService.getUserFavorites(userId, targetType, page, size);
        return ApiResponse.success(favorites);
    }

    @GetMapping("/check")
    public ApiResponse<Boolean> isFavorite(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @org.springframework.lang.Nullable Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ApiResponse.success(false);
        }
        Long userId = Long.parseLong(authentication.getName());
        boolean isFav = favoriteService.isFavorite(userId, targetType, targetId);
        return ApiResponse.success(isFav);
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getFavoriteStats(
            @org.springframework.lang.Nullable Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ApiResponse.success(Map.of("mealCount", 0, "workoutCount", 0, "totalCount", 0));
        }
        Long userId = Long.parseLong(authentication.getName());
        Map<String, Object> stats = favoriteService.getFavoriteStats(userId);
        return ApiResponse.success(stats);
    }
}
