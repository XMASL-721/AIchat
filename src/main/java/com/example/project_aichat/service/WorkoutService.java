package com.example.project_aichat.service;

import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Workout;

public interface WorkoutService {
    PageResponse<Workout> listAll(int page, int size, String keyword, String category);
    Workout getById(Long id);
    Workout getRandom();
}
