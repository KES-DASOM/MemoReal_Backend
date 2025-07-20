package com.dasom.MemoReal.domain.capsule.repository;

import com.dasom.MemoReal.domain.capsule.entity.Capsule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CapsuleRepository extends JpaRepository<Capsule, Long> {

    // MySQL과 연동

}
