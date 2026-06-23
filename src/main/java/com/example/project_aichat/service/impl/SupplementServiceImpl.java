package com.example.project_aichat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.project_aichat.dto.response.PageResponse;
import com.example.project_aichat.entity.Supplement;
import com.example.project_aichat.mapper.SupplementMapper;
import com.example.project_aichat.service.SupplementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SupplementServiceImpl implements SupplementService {

    private final SupplementMapper supplementMapper;

    @Override
    public PageResponse<Supplement> listAll(int page, int size, String keyword, String category) {
        Page<Supplement> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Supplement> wrapper = new LambdaQueryWrapper<Supplement>()
                .eq(StringUtils.hasText(category), Supplement::getCategory, category)
                .and(StringUtils.hasText(keyword), w ->
                        w.like(Supplement::getName, keyword)
                         .or().like(Supplement::getDescription, keyword))
                .orderByAsc(Supplement::getCategory, Supplement::getId);
        return PageResponse.of(supplementMapper.selectPage(pageParam, wrapper));
    }

    @Override
    public Supplement getById(Long id) {
        Supplement supplement = supplementMapper.selectById(id);
        if (supplement == null) {
            throw new RuntimeException("补剂不存在");
        }
        return supplement;
    }
}
