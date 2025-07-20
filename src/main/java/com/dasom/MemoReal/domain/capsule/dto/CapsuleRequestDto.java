package com.dasom.MemoReal.domain.capsule.dto;

import com.dasom.MemoReal.domain.capsule.type.CapsuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapsuleRequestDto {
    private String title;
    private CapsuleType type;
    private String content;
    private LocalDate openDate;
}