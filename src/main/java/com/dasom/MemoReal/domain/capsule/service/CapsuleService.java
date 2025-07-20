package com.dasom.MemoReal.domain.capsule.service;

import com.dasom.MemoReal.domain.capsule.dto.CapsuleRequestDto;
import com.dasom.MemoReal.domain.capsule.dto.CapsuleResponseDto;
import com.dasom.MemoReal.domain.capsule.entity.Capsule;
import com.dasom.MemoReal.domain.capsule.repository.CapsuleRepository;
import com.dasom.MemoReal.domain.user.entity.User;
import com.dasom.MemoReal.domain.user.repository.UserRepository;
import com.dasom.MemoReal.global.exception.CustomException;
import com.dasom.MemoReal.global.exception.ErrorCode;
import com.dasom.MemoReal.global.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

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

    /**
     * 새로운 캡슐을 생성합니다.
     */
    public CapsuleResponseDto createCapsule(CapsuleRequestDto requestDto) {
        String currentUserEmail = SecurityUtil.getCurrentUsername(); // CustomException(UNAUTHORIZED) 발생 가능
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Capsule capsule = Capsule.toEntity(requestDto, currentUser);
        Capsule savedCapsule = capsuleRepository.save(capsule);
        return Capsule.toDto(savedCapsule);
    }

    /**
     * 특정 ID의 캡슐을 조회합니다.
     */
    @Transactional(readOnly = true)
    public CapsuleResponseDto getCapsule(Long id) {
        String currentUserEmail = SecurityUtil.getCurrentUsername(); // CustomException(UNAUTHORIZED) 발생 가능
        Capsule capsule = findCapsuleById(id);

        if (!capsule.getUser().getEmail().equals(currentUserEmail)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return Capsule.toDto(capsule);
    }

    /**
     * 현재 사용자가 소유한 모든 캡슐을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<CapsuleResponseDto> getAllCapsules() {
        String currentUserEmail = SecurityUtil.getCurrentUsername(); // CustomException(UNAUTHORIZED) 발생 가능
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return capsuleRepository.findByUser(currentUser).stream()
                .map(Capsule::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 캡슐을 수정합니다.
     */
    public CapsuleResponseDto updateCapsule(Long id, CapsuleRequestDto requestDto) {
        String currentUserEmail = SecurityUtil.getCurrentUsername(); // CustomException(UNAUTHORIZED) 발생 가능
        Capsule capsule = findCapsuleById(id);

        if (!capsule.getUser().getEmail().equals(currentUserEmail)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        capsule.update(requestDto); // requestDto 객체 자체를 전달합니다.

        Capsule savedCapsule = capsuleRepository.save(capsule);
        return Capsule.toDto(savedCapsule);
    }

    /**
     * 특정 캡슐을 삭제합니다.
     */
    public void deleteCapsule(Long id) {
        String currentUserEmail = SecurityUtil.getCurrentUsername(); // CustomException(UNAUTHORIZED) 발생 가능
        Capsule capsule = findCapsuleById(id);

        if (!capsule.getUser().getEmail().equals(currentUserEmail)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        capsuleRepository.delete(capsule);
    }

    /**
     * ID를 통해 캡슐을 찾고, 없을 경우 예외를 발생시킵니다.
     */
    private Capsule findCapsuleById(Long id) {
        return capsuleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CAPSULE_NOT_FOUND));
    }
}