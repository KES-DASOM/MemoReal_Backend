package com.dasom.MemoReal.domain.user.controller;

import com.dasom.MemoReal.domain.user.dto.JoinDTO;
import com.dasom.MemoReal.domain.user.dto.LoginDTO;
import com.dasom.MemoReal.domain.user.dto.UserDTO;
import com.dasom.MemoReal.domain.user.service.UserService;
import com.dasom.MemoReal.global.jwt.dto.JwtTokenDTO;
import com.dasom.MemoReal.global.security.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 통해 로그인 후 JWT 토큰 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = JwtTokenDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @PostMapping("/login")
    public JwtTokenDTO login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "로그인 요청 DTO (이메일, 비밀번호)", required = true,
                    content = @Content(schema = @Schema(implementation = LoginDTO.class))
            )
            @RequestBody LoginDTO loginDto
    ) {
        String email = loginDto.getEmail();
        String password = loginDto.getPassword();
        return userService.login(email, password);
    }

    @Operation(summary = "회원가입", description = "사용자 정보를 입력받아 회원가입을 처리")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content)
    })
    @PostMapping("/join")
    public ResponseEntity<UserDTO> join(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입 요청 DTO", required = true,
                    content = @Content(schema = @Schema(implementation = JoinDTO.class))
            )
            @RequestBody JoinDTO joinDto
    ) {
        UserDTO savedUserDto = userService.join(joinDto);
        return ResponseEntity.ok(savedUserDto);
    }

    @Operation(summary = "테스트용 토큰 검증", description = "요청자의 JWT 토큰에서 유저 정보를 추출해 닉네임 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "닉네임 반환 성공", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "토큰 없음 또는 잘못된 토큰", content = @Content)
    })
    @PostMapping("/test")
    public String test() {
        return SecurityUtil.getCurrentUsername();
    }
}