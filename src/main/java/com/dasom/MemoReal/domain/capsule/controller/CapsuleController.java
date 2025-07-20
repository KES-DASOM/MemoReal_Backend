package com.dasom.MemoReal.domain.capsule.controller;

import com.dasom.MemoReal.domain.capsule.dto.CapsuleDto.CapsuleRequestDto;
import com.dasom.MemoReal.domain.capsule.dto.CapsuleDto.CapsuleResponseDto;
import com.dasom.MemoReal.domain.capsule.service.CapsuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/capsules")
public class CapsuleController {

    private final CapsuleService capsuleService;

    @PostMapping
    public ResponseEntity<CapsuleResponseDto> createCapsule(
            @RequestBody CapsuleRequestDto requestDto
    ) {
        CapsuleResponseDto responseDto = capsuleService.createCapsule(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CapsuleResponseDto> getCapsuleById(@PathVariable("id") Long id) {
        CapsuleResponseDto responseDto = capsuleService.getCapsule(id);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<CapsuleResponseDto>> getAllCapsules() {
        List<CapsuleResponseDto> capsules = capsuleService.getAllCapsules();
        return ResponseEntity.ok(capsules);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CapsuleResponseDto> updateCapsule(
            @PathVariable("id") Long id,
            @RequestBody CapsuleRequestDto requestDto
    ) {
        CapsuleResponseDto responseDto = capsuleService.updateCapsule(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCapsule(@PathVariable("id") Long id) {
        capsuleService.deleteCapsule(id);
        return ResponseEntity.noContent().build();
    }
}