package com.dasom.MemoReal.domain.capsule.service;

import com.dasom.MemoReal.domain.capsule.dto.CapsuleDto.CapsuleRequestDto;
import com.dasom.MemoReal.domain.capsule.dto.CapsuleDto.CapsuleResponseDto;
import com.dasom.MemoReal.domain.capsule.entity.Capsule;
import com.dasom.MemoReal.domain.capsule.repository.CapsuleRepository;
import com.dasom.MemoReal.domain.user.entity.User;
import com.dasom.MemoReal.domain.user.repository.UserRepository;
import com.dasom.MemoReal.global.exception.CustomException;
import com.dasom.MemoReal.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CapsuleService {

    private final CapsuleRepository capsuleRepository;
    private final UserRepository userRepository;

    // 현재 인증된 사용자 User 엔티티를 가져오는 헬퍼 메서드
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        String userEmail = authentication.getName();

        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // 캡슐 생성
    public CapsuleResponseDto createCapsule(CapsuleRequestDto requestDto) {
        User currentUser = getCurrentUser();

        Capsule capsule = requestDto.toEntity();
        capsule.setUser(currentUser);

        Capsule savedCapsule = capsuleRepository.save(capsule);
        System.out.println("Capsule 저장 완료, ID: " + savedCapsule.getId());
        return CapsuleResponseDto.toDto(savedCapsule);
    }

    // 캡슐 조회 (단일)
    @Transactional(readOnly = true)
    public CapsuleResponseDto getCapsule(Long id) {
        User currentUser = getCurrentUser();
        Capsule capsule = findCapsuleById(id);

        if (!capsule.getUser().getId().equals(currentUser.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return CapsuleResponseDto.toDto(capsule);
    }

    // 모든 캡슐 조회 (현재 로그인된 사용자의 캡슐만 조회)
    @Transactional(readOnly = true)
    public List<CapsuleResponseDto> getAllCapsules() {
        User currentUser = getCurrentUser();

        return capsuleRepository.findByUser(currentUser).stream()
                .map(CapsuleResponseDto::toDto)
                .collect(Collectors.toList());
    }

    // 캡슐 업데이트
    public CapsuleResponseDto updateCapsule(Long id, CapsuleRequestDto requestDto) {
        User currentUser = getCurrentUser();
        Capsule capsule = findCapsuleById(id);

        if (!capsule.getUser().getId().equals(currentUser.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        capsule.update(
                requestDto.getTitle(),
                requestDto.getType(),
                requestDto.getContent(),
                requestDto.getOpenDate()
        );

        Capsule savedCapsule = capsuleRepository.save(capsule);
        return CapsuleResponseDto.toDto(savedCapsule);
    }

    // 캡슐 삭제
    public void deleteCapsule(Long id) {
        User currentUser = getCurrentUser();
        Capsule capsule = findCapsuleById(id);

        if (!capsule.getUser().getId().equals(currentUser.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        capsuleRepository.delete(capsule);
    }

    // ID로 캡슐을 찾는 내부 메서드 (CustomException 사용)
    private Capsule findCapsuleById(Long id) {
        return capsuleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CAPSULE_NOT_FOUND));
    }
}