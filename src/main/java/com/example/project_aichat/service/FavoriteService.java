package com.example.project_aichat.service;

import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Meal;
import com.example.project_aichat.entity.UserFavorite;
import com.example.project_aichat.entity.Workout;

import java.util.Map;

public interface FavoriteService {
    UserFavorite addFavorite(Long userId, String targetType, Long targetId);
    void removeFavorite(Long userId, String targetType, Long targetId);
    PageResponse<UserFavorite> getUserFavorites(Long userId, String targetType, int page, int size);
    boolean isFavorite(Long userId, String targetType, Long targetId);
    Map<String, Object> getFavoriteStats(Long userId);
}
