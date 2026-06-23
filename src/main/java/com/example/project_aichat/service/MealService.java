package com.example.project_aichat.service;

import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Meal;

public interface MealService {
    PageResponse<Meal> listAll(int page, int size, String keyword);
    Meal getById(Long id);
    Meal getRandom();
}
