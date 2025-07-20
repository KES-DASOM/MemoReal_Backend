package com.dasom.MemoReal.domain.capsule.dto;

import com.dasom.MemoReal.domain.capsule.type.CapsuleType;
import com.dasom.MemoReal.domain.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapsuleResponseDto {
    private Long id;
    private String title;
    private CapsuleType type;
    private String content;
    private LocalDate openDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MediaDto> medias;

    private UserDTO user;  // 회원 정보 추가
}