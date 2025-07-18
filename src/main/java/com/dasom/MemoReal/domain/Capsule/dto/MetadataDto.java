package com.dasom.MemoReal.domain.Capsule.dto;

import com.dasom.MemoReal.domain.Capsule.entity.Metadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetadataDto {
    private String filename;
    private String contentType; //확장자
    private String title;
    private String description;
    private String uploadedDate;      // yyyy-MM-dd
    private String fileHash;          // 실제 콘텐츠 파일의 IPFS 해시
    private String accessCondition;   // 열람 조건
    private String category;  //컨텐츠 분류 용 카테고리
    private String tags;

    public static MetadataDto fromEntity(Metadata metadata) {
        return new MetadataDto(
                metadata.getFilename(),
                metadata.getContentType(),
                metadata.getTitle(),
                metadata.getDescription(),
                metadata.getUploadedDate() != null ? metadata.getUploadedDate().toString() : null,
                metadata.getIpfsContentHash(),
                metadata.getAccessCondition(),
                metadata.getCategory(),
                metadata.getTags()
        );
    }
}
