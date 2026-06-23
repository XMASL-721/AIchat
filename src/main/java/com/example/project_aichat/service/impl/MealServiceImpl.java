package com.example.project_aichat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Meal;
import com.example.project_aichat.mapper.MealMapper;
import com.example.project_aichat.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MealServiceImpl implements MealService {

    private final MealMapper mealMapper;

    @Override
    public PageResponse<Meal> listAll(int page, int size, String keyword) {
        Page<Meal> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Meal> wrapper = new LambdaQueryWrapper<Meal>()
                .and(StringUtils.hasText(keyword), w ->
                        w.like(Meal::getName, keyword).or().like(Meal::getIngredients, keyword))
                .orderByAsc(Meal::getId);
        Page<Meal> result = mealMapper.selectPage(pageParam, wrapper);
        return PageResponse.of(result);
    }

    @Override
    public Meal getById(Long id) {
        Meal meal = mealMapper.selectById(id);
        if (meal == null) {
            throw new RuntimeException("减脂餐不存在");
        }
        return meal;
    }

    @Override
    public Meal getRandom() {
        LambdaQueryWrapper<Meal> wrapper = new LambdaQueryWrapper<Meal>()
                .last("ORDER BY RAND() LIMIT 1");
        return mealMapper.selectOne(wrapper);
    }
}
