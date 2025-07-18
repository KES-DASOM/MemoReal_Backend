package com.dasom.MemoReal.domain.user.controller;

import com.dasom.MemoReal.domain.user.dto.JoinDTO;
import com.dasom.MemoReal.domain.user.dto.LoginDTO;
import com.dasom.MemoReal.domain.user.dto.UserDTO;
import com.dasom.MemoReal.domain.user.service.UserService;
import com.dasom.MemoReal.global.jwt.dto.JwtTokenDTO;
import com.dasom.MemoReal.global.security.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtTokenDTO> login(@RequestBody LoginDTO loginDto) {
        JwtTokenDTO token = userService.login(loginDto.getEmail(), loginDto.getPassword());
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "회원가입", description = "회원가입 정보를 입력받아 회원으로 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류")
    })
    @PostMapping("/join")
    public ResponseEntity<UserDTO> join(@RequestBody JoinDTO joinDto) {
        UserDTO user = userService.join(joinDto);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "토큰 테스트", description = "JWT 토큰에서 현재 사용자 닉네임을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "토큰 없음 또는 잘못된 토큰")
    })
    @PostMapping("/test")
    public ResponseEntity<String> getCurrentUsername() {
        String username = SecurityUtil.getCurrentUsername();
        return ResponseEntity.ok(username);
    }
}
