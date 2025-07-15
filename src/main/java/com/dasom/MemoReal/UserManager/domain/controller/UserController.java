package com.dasom.MemoReal.UserManager.domain.controller;

import com.dasom.MemoReal.UserManager.domain.dto.*;
import com.dasom.MemoReal.UserManager.domain.entity.User;
import com.dasom.MemoReal.UserManager.domain.service.UserService;
import com.dasom.MemoReal.UserManager.global.util.JwtUtil;
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
        try {
            User user = userService.register(request);
            return new CommonResponse<>(false, user);
        } catch (IllegalStateException e) {
            return new CommonResponse<>(true, e.getMessage());
        } catch (Exception e) {
            return new CommonResponse<>(true, "회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public CommonResponse<?> login(@RequestBody Login.Request request) {
        try {
            User user = userService.login(request.getEmail(), request.getPassword());

            if (user == null) {
                return new CommonResponse<>(true, "이메일 또는 비밀번호가 일치하지 않습니다.");
            }

            String token = JwtUtil.generateToken(user.getUsername());
            Login.Response response = new Login.Response(token);

            return new CommonResponse<>(false, response);

        } catch (Exception e) {
            return new CommonResponse<>(true, "로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    @PutMapping("/update")
    public CommonResponse<?> updateUserInfo(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> updates) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            String email = JwtUtil.extractEmail(token);

            User updatedUser = userService.updateUserInfoByMap(email, updates);

            return new CommonResponse<>(false, updatedUser);
        } catch (IllegalStateException e) {
            return new CommonResponse<>(true, e.getMessage());
        } catch (Exception e) {
            return new CommonResponse<>(true, "유저 정보 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
