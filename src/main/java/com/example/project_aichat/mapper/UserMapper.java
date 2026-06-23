package com.example.project_aichat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.project_aichat.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
