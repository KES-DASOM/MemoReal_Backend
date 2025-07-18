package com.dasom.MemoReal.domain.Capsule.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "metadata")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Metadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ipfsContentHash;
    private String filename;
    private String contentType;
    private String title;
    private String description;
    private LocalDate uploadedDate;
    private String accessCondition;
    private String category;
    private String tags;
    private Long userId;// 추후에 user 테이블과 외래키 연결
}