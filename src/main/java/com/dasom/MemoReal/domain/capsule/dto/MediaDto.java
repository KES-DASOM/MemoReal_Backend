package com.dasom.MemoReal.domain.capsule.dto;

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

}