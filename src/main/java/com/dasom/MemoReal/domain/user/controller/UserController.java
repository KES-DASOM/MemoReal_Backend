package com.dasom.MemoReal.domain.user.controller;

import com.dasom.MemoReal.domain.user.dto.JoinDTO;
import com.dasom.MemoReal.domain.user.dto.LoginDTO;
import com.dasom.MemoReal.domain.user.dto.UserDTO;
import com.dasom.MemoReal.domain.user.service.UserService;
import com.dasom.MemoReal.global.jwt.dto.JwtTokenDTO;
import com.dasom.MemoReal.global.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public JwtTokenDTO login(@RequestBody LoginDTO loginDto) {
        String email = loginDto.getEmail();
        String password = loginDto.getPassword();
        JwtTokenDTO jwtToken = userService.login(email, password);
        return jwtToken;
    }

    @PostMapping("/join")
    public ResponseEntity<UserDTO> join(@RequestBody JoinDTO joinDto) {
        UserDTO savedUserDto = userService.join(joinDto);
        return ResponseEntity.ok(savedUserDto);
    }


    // 임시 테스트 토큰이 있는 사용자가 요청 시, 사용자의 닉네임 반환
    @PostMapping("/test")
    public String test() {
        return SecurityUtil.getCurrentUsername();
    }
}
