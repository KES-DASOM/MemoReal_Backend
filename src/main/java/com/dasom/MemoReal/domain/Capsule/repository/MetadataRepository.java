package com.dasom.MemoReal.domain.Capsule.repository;

import com.dasom.MemoReal.domain.Capsule.entity.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetadataRepository extends JpaRepository<Metadata, Long> {
    List<Metadata> findAllByUserId(Long userId); // 유저별 메타데이터 조회

}
