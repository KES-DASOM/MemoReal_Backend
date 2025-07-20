package com.dasom.MemoReal.domain.capsule.controller;

import com.dasom.MemoReal.domain.capsule.dto.CapsuleRequestDto;
import com.dasom.MemoReal.domain.capsule.dto.CapsuleResponseDto;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(capsuleService.createCapsule(requestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CapsuleResponseDto> getCapsuleById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(capsuleService.getCapsule(id));
    }

    @GetMapping
    public ResponseEntity<List<CapsuleResponseDto>> getAllCapsules() {
        return ResponseEntity.ok(capsuleService.getAllCapsules());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CapsuleResponseDto> updateCapsule(
            @PathVariable("id") Long id,
            @RequestBody CapsuleRequestDto requestDto
    ) {
        return ResponseEntity.ok(capsuleService.updateCapsule(id, requestDto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCapsule(@PathVariable("id") Long id) {
        capsuleService.deleteCapsule(id);
        return ResponseEntity.noContent().build();
    }
}