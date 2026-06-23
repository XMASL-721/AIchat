package com.example.project_aichat.service;

import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Supplement;

public interface SupplementService {
    PageResponse<Supplement> listAll(int page, int size, String keyword, String category);
    Supplement getById(Long id);
}
