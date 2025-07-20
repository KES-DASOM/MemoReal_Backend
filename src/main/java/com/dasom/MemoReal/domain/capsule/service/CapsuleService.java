package com.dasom.MemoReal.domain.capsule.service;

import com.dasom.MemoReal.domain.capsule.dto.CapsuleCreateRequestDto;
import com.dasom.MemoReal.domain.capsule.dto.CapsuleUpdateRequestDto;
import com.dasom.MemoReal.domain.capsule.entity.Capsule;
import com.dasom.MemoReal.domain.capsule.repository.CapsuleRepository;
import com.dasom.MemoReal.exception.CustomException;
import com.dasom.MemoReal.exception.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CapsuleService {

    private final CapsuleRepository capsuleRepository;

    public Capsule createCapsule(CapsuleCreateRequestDto dto, String mediaCid) {
        Capsule capsule = new Capsule();
        capsule.setCapsuleType(dto.getCapsuleType());
        capsule.setTitle(dto.getTitle());
        capsule.setContent(dto.getContent());
        capsule.setOpenTime(dto.getOpenTime());
        capsule.setMediaCid(mediaCid);

        return capsuleRepository.save(capsule);
    }


    public Capsule selectCapsuleById(Long id) {
        return capsuleRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteCapsule(Long capsuleId) {
        Capsule capsule = capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAPSULE_NOT_FOUND));

        capsuleRepository.delete(capsule);
    }

    @Transactional
    public void updateCapsule(Long id, CapsuleUpdateRequestDto dto, String newCid) {
        Capsule capsule = capsuleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("캡슐 없음"));

        capsule.setCapsuleType(dto.getCapsuleType());
        capsule.setTitle(dto.getTitle());
        capsule.setContent(dto.getContent());
        capsule.setOpenTime(dto.getOpenTime());

        if (newCid != null) {
            capsule.setMediaCid(newCid); // 새로운 이미지로 대체
        }

        capsuleRepository.save(capsule);
    }
}
