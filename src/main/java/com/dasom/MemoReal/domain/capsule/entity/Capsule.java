package com.dasom.MemoReal.domain.capsule.entity;

import com.dasom.MemoReal.domain.capsule.dto.CapsuleRequestDto;
import com.dasom.MemoReal.domain.capsule.dto.CapsuleResponseDto;
import com.dasom.MemoReal.domain.capsule.dto.MediaDto;
import com.dasom.MemoReal.domain.capsule.type.CapsuleType;
import com.dasom.MemoReal.domain.user.dto.UserDTO;
import com.dasom.MemoReal.domain.user.entity.User;
import com.dasom.MemoReal.global.exception.CustomException;
import com.dasom.MemoReal.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "capsules")
@EntityListeners(AuditingEntityListener.class)
@Builder
public class Capsule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private CapsuleType type;

    private String content;

    @Column(nullable = false)
    private LocalDate openDate;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "capsule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Media> medias = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static Capsule toEntity(CapsuleRequestDto dto, User user) {
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return Capsule.builder()
                .title(dto.getTitle())
                .type(dto.getType())
                .content(dto.getContent())
                .openDate(dto.getOpenDate())
                .user(user)
                .build();
    }

    public static CapsuleResponseDto toDto(Capsule capsule) {
        List<MediaDto> mediaDtos = (capsule.getMedias() != null) ?
                capsule.getMedias().stream()
                        .map(Media::toDto)
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        return CapsuleResponseDto.builder()
                .id(capsule.getId())
                .title(capsule.getTitle())
                .type(capsule.getType())
                .content(capsule.getContent())
                .openDate(capsule.getOpenDate())
                .createdAt(capsule.getCreatedAt())
                .updatedAt(capsule.getUpdatedAt())
                .medias(mediaDtos)
                .user(UserDTO.toDto(capsule.getUser()))
                .build();
    }

    public void update(CapsuleRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.type = requestDto.getType();
        this.content = requestDto.getContent();
        this.openDate = requestDto.getOpenDate();
    }
}