package com.dasom.MemoReal.domain.capsule.dto;

import lombok.Getter;

import java.util.Date;

public class CapsuleSelectResponseDto {

    @Getter

    private Long capsuleId;
    private String capsuleType;
    private String capsuleTitle;
    private String capsuleContent;
    private Date openTime;

    public CapsuleSelectResponseDto(Long capsuleId) {
        this.capsuleId = capsuleId;
    }
}
