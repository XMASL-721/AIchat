package com.example.project_aichat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Article;
import com.example.project_aichat.mapper.ArticleMapper;
import com.example.project_aichat.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;

    @Override
    public PageResponse<Article> listAll(int page, int size, String keyword, String category) {
        Page<Article> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(StringUtils.hasText(category), Article::getCategory, category)
                .and(StringUtils.hasText(keyword), w ->
                        w.like(Article::getTitle, keyword)
                         .or().like(Article::getSummary, keyword)
                         .or().like(Article::getTags, keyword))
                .orderByDesc(Article::getCreatedAt);
        return PageResponse.of(articleMapper.selectPage(pageParam, wrapper));
    }

    @Override
    public Article getById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        return article;
    }

    @Override
    public void incrementViewCount(Long id) {
        articleMapper.incrementViewCount(id);
    }
}
