package com.dasom.MemoReal.domain.capsule.repository;

import com.dasom.MemoReal.domain.capsule.entity.Capsule;
import com.dasom.MemoReal.domain.user.entity.User; // User 엔티티 import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // 스프링 빈으로 등록
public interface CapsuleRepository extends JpaRepository<Capsule, Long> {
    // JpaRepository를 상속받으면 save, findById, findAll, delete 등의 기본적인 CRUD 메서드가 자동으로 제공됩니다.
    // 추가적인 쿼리 메서드가 필요하다면 여기에 선언할 수 있습니다.

    // 특정 User에 속하는 모든 Capsule을 조회하기 위한 메서드 추가
    // Spring Data JPA가 메서드 이름 규칙에 따라 자동으로 쿼리를 생성합니다.
    List<Capsule> findByUser(User user);
}