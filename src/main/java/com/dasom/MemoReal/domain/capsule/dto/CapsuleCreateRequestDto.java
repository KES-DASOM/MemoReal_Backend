package com.dasom.MemoReal.domain.capsule.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CapsuleCreateRequestDto {

    private String capsuleType;
    private String title;
    private String content;
    private Date openTime;
}
