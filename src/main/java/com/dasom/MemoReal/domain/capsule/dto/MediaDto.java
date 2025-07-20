package com.dasom.MemoReal.domain.capsule.dto;

import com.dasom.MemoReal.domain.capsule.entity.Media;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaDto {

    private Long id;
    private String cid;
    private String originalFileName;

    public static MediaDto toDto(Media media) {
        String cid = media.getCid();

        return MediaDto.builder()
                .id(media.getId())
                .cid(cid)
                .originalFileName(media.getOriginalFileName())
                .build();
    }
}