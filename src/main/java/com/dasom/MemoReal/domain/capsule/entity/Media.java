package com.dasom.MemoReal.domain.capsule.entity;

import com.dasom.MemoReal.domain.capsule.dto.MediaDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "medias")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cid; // IPFS CID
    private String originalFileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capsule_id")
    private Capsule capsule;

    public static MediaDto toDto(Media media) {
        if (media == null) {
            return MediaDto.builder().build();
        }

        return MediaDto.builder()
                .id(media.getId())
                .cid(media.getCid())
                .originalFileName(media.getOriginalFileName())
                .build();
    }

}