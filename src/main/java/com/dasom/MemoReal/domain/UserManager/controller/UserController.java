package com.dasom.MemoReal.domain.UserManager.controller;

import com.dasom.MemoReal.domain.UserManager.dto.*;
import com.dasom.MemoReal.domain.UserManager.entity.User;
import com.dasom.MemoReal.domain.UserManager.service.UserService;
import com.dasom.MemoReal.global.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public CommonResponse<?> register(@RequestBody UserRegisterRequest request) {
        User user = userService.register(request);
        return new CommonResponse<>(true, user);
    }

    @PostMapping("/login")
    public CommonResponse<?> login(@RequestBody LoginDTO.Request request) {
        User user = userService.login(request.getEmail(), request.getPassword());

        String token = JwtUtil.generateToken(user.getUsername());
        LoginDTO.Response response = new LoginDTO.Response(token);

        return new CommonResponse<>(true, response);
    }

    @PutMapping("/update")
    public CommonResponse<?> updateUserInfo(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> updates) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        String email = JwtUtil.extractEmail(token);

        User updatedUser = userService.updateUserInfoByMap(email, updates);

        return new CommonResponse<>(true, updatedUser);
    }
}
