package com.example.project_aichat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.project_aichat.dto.request.LoginRequest;
import com.example.project_aichat.dto.request.RegisterRequest;
import com.example.project_aichat.dto.response.LoginResponse;
import com.example.project_aichat.entity.User;
import com.example.project_aichat.mapper.UserMapper;
import com.example.project_aichat.security.JwtTokenProvider;
import com.example.project_aichat.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void register(RegisterRequest request) {
        // 1. 校验手机号是否已注册
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone())
        );
        if (count > 0) {
            throw new RuntimeException("手机号已注册");
        }

        // 2. 创建用户
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname("用户" + request.getPhone().substring(7)); // 默认昵称: 用户+手机号后4位
        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone())
        );
        if (user == null) {
            throw new RuntimeException("手机号未注册");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 3. 生成 JWT
        String token = jwtTokenProvider.generateToken(user.getId(), user.getPhone());

        // 4. 返回登录结果
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(), user.getPhone(), user.getNickname(), user.getAvatar(),
                user.getRole() != null ? user.getRole() : "user"
        );
        return new LoginResponse(token, userInfo);
    }
}
