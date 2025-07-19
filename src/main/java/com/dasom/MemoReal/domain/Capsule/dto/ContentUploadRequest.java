package com.dasom.MemoReal.domain.Capsule.dto;

import com.dasom.MemoReal.domain.Capsule.entity.Metadata;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ContentUploadRequest {

    private String title;
    private String description;
    private String accessCondition;
    private String category;
    private String tags;


    public Metadata toEntity(String filename, String contentType, String ipfsContentHash, LocalDate uploadedDate, Long userId) {
        return Metadata.builder()
                .filename(filename)
                .contentType(contentType)
                .title(this.title)
                .description(this.description)
                .accessCondition(this.accessCondition)
                .category(this.category)
                .tags(this.tags)
                .uploadedDate(uploadedDate)
                .ipfsContentHash(ipfsContentHash)
                .userId(userId)
                .build();
    }
}
