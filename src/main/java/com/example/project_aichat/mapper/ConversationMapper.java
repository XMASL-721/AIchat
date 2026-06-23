package com.example.project_aichat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.project_aichat.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
