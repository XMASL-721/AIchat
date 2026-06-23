package com.example.project_aichat.controller;

import com.example.project_aichat.dto.response.ApiResponse;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Article;
import com.example.project_aichat.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ApiResponse<PageResponse<Article>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return ApiResponse.success(articleService.listAll(page, size, keyword, category));
    }

    @GetMapping("/{id}")
    public ApiResponse<Article> get(@PathVariable Long id) {
        Article article = articleService.getById(id);
        articleService.incrementViewCount(id);
        return ApiResponse.success(article);
    }
}
