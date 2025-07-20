package com.dasom.MemoReal.domain.capsule.dto;

import com.dasom.MemoReal.domain.user.dto.UserDTO;
import com.dasom.MemoReal.domain.capsule.entity.Capsule;
import com.dasom.MemoReal.domain.capsule.entity.Media;
import com.dasom.MemoReal.domain.capsule.type.CapsuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CapsuleDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CapsuleRequestDto {
        private String title;
        private CapsuleType type;
        private String content;
        private LocalDate openDate;

        public Capsule toEntity() {
            return Capsule.builder()
                    .title(title)
                    .type(type)
                    .content(content)
                    .openDate(openDate)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CapsuleResponseDto {
        private Long id;
        private String title;
        private CapsuleType type;
        private String content;
        private LocalDate openDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<MediaDto> medias;

        private UserDTO user;  // 회원 정보 추가

        public static CapsuleResponseDto toDto(Capsule capsule) {
            List<MediaDto> mediaDtos = capsule.getMedia().stream()
                    .map(MediaDto::toDto)
                    .collect(Collectors.toList());

            return CapsuleResponseDto.builder()
                    .id(capsule.getId())
                    .title(capsule.getTitle())
                    .type(capsule.getType())
                    .content(capsule.getContent())
                    .openDate(capsule.getOpenDate())
                    .createdAt(capsule.getCreatedAt())
                    .updatedAt(capsule.getUpdatedAt())
                    .medias(mediaDtos)
                    .user(UserDTO.toDto(capsule.getUser()))  // user 변환 후 세팅
                    .build();
        }
    }
}
