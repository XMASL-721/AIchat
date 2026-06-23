package com.example.project_aichat.service;

import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Article;

public interface ArticleService {
    PageResponse<Article> listAll(int page, int size, String keyword, String category);
    Article getById(Long id);
    void incrementViewCount(Long id);
}
