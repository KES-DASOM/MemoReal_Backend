package com.dasom.MemoReal.domain.capsule.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter // 테스트 시 필요
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Capsule {

    // 캡슐 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long capsuleId;

    // 캡슐 종류
    private String capsuleType;

    // 제목
    private String title;

    // 내용
    private String content;

    // 캡슐 오픈일
    private Date openTime;

    // IPFS CID 필드 ( 이미지 / 동영상 )
    private String mediaCid;

    // 생성자 (id 제외)
    public Capsule(String capsuleType, String title, String content, Date openTime) {
        this.capsuleType = capsuleType;
        this.title = title;
        this.content = content;
        this.openTime = openTime;
    }
}
