package com.example.project_aichat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Workout;
import com.example.project_aichat.mapper.WorkoutMapper;
import com.example.project_aichat.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class WorkoutServiceImpl implements WorkoutService {

    private final WorkoutMapper workoutMapper;

    @Override
    public PageResponse<Workout> listAll(int page, int size, String keyword, String category) {
        Page<Workout> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Workout> wrapper = new LambdaQueryWrapper<Workout>()
                .eq(StringUtils.hasText(category), Workout::getCategory, category)
                .and(StringUtils.hasText(keyword), w ->
                        w.like(Workout::getName, keyword).or().like(Workout::getExercises, keyword))
                .orderByAsc(Workout::getId);
        return PageResponse.of(workoutMapper.selectPage(pageParam, wrapper));
    }

    @Override
    public Workout getById(Long id) {
        Workout w = workoutMapper.selectById(id);
        if (w == null) throw new RuntimeException("训练计划不存在");
        return w;
    }

    @Override
    public Workout getRandom() {
        LambdaQueryWrapper<Workout> wrapper = new LambdaQueryWrapper<Workout>()
                .last("ORDER BY RAND() LIMIT 1");
        return workoutMapper.selectOne(wrapper);
    }
}
