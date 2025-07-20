package com.dasom.MemoReal.domain.capsule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class CapsuleUpdateRequestDto {

    // 캡슐 ID는 따로 요청하지 않습니다. * URL을 통해 어떤 ID를 수정할지 알 수 있기 떄문에
    // private Long capsuleId;

    private String capsuleType;
    private String title;
    private String content;
    private Date openTime;
}
