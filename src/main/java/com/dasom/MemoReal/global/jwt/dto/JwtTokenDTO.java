package com.dasom.MemoReal.global.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class JwtTokenDTO {
    private String grantType;
    private String accessToken;
    private String refreshToken;
}
