package com.example.project_aichat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Meal;
import com.example.project_aichat.entity.UserFavorite;
import com.example.project_aichat.entity.Workout;
import com.example.project_aichat.mapper.MealMapper;
import com.example.project_aichat.mapper.UserFavoriteMapper;
import com.example.project_aichat.mapper.WorkoutMapper;
import com.example.project_aichat.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserFavoriteMapper userFavoriteMapper;
    private final MealMapper mealMapper;
    private final WorkoutMapper workoutMapper;

    @Override
    public UserFavorite addFavorite(Long userId, String targetType, Long targetId) {
        // 检查是否已收藏
        if (isFavorite(userId, targetType, targetId)) {
            throw new RuntimeException("已收藏过该内容");
        }

        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setTargetType(targetType);
        favorite.setTargetId(targetId);
        userFavoriteMapper.insert(favorite);
        return favorite;
    }

    @Override
    public void removeFavorite(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTargetType, targetType)
                .eq(UserFavorite::getTargetId, targetId);
        userFavoriteMapper.delete(wrapper);
    }

    @Override
    public PageResponse<UserFavorite> getUserFavorites(Long userId, String targetType, int page, int size) {
        Page<UserFavorite> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(targetType != null && !targetType.isEmpty(), UserFavorite::getTargetType, targetType)
                .orderByDesc(UserFavorite::getCreatedAt);
        Page<UserFavorite> result = userFavoriteMapper.selectPage(pageParam, wrapper);
        return PageResponse.of(result);
    }

    @Override
    public boolean isFavorite(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTargetType, targetType)
                .eq(UserFavorite::getTargetId, targetId);
        return userFavoriteMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Map<String, Object> getFavoriteStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 统计食谱收藏数
        long mealCount = userFavoriteMapper.selectCount(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getTargetType, "meal")
        );

        // 统计训练收藏数
        long workoutCount = userFavoriteMapper.selectCount(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getTargetType, "workout")
        );

        stats.put("mealCount", mealCount);
        stats.put("workoutCount", workoutCount);
        stats.put("totalCount", mealCount + workoutCount);

        return stats;
    }
}
