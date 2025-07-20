package com.dasom.MemoReal.domain.capsule.controller;

import com.dasom.MemoReal.domain.capsule.dto.CapsuleRequestDto;
import com.dasom.MemoReal.domain.capsule.dto.CapsuleResponseDto;
import com.dasom.MemoReal.domain.capsule.service.CapsuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "타임캡슐 생성", description = "새 타임캡슐을 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "타임캡슐 생성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류")
    })
    @PostMapping
    public ResponseEntity<CapsuleResponseDto> createCapsule(
            @RequestBody CapsuleRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(capsuleService.createCapsule(requestDto));
    }

    @Operation(summary = "타임캡슐 단건 조회", description = "ID기반 특정 타임캡슐 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "타임캡슐 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 타임캡슐이 존재하지 않음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CapsuleResponseDto> getCapsuleById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(capsuleService.getCapsule(id));
    }

    @Operation(summary = "모든 타임캡슐 조회", description = "모든 타임캡슐 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "타임캡슐 목록 조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<CapsuleResponseDto>> getAllCapsules() {
        return ResponseEntity.ok(capsuleService.getAllCapsules());
    }

    @Operation(summary = "타임캡슐 수정", description = "ID기반 타임캡슐 정보 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "타임캡슐 수정 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 타임캡슐이 존재하지 않음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CapsuleResponseDto> updateCapsule(
            @PathVariable("id") Long id,
            @RequestBody CapsuleRequestDto requestDto
    ) {
        return ResponseEntity.ok(capsuleService.updateCapsule(id, requestDto));
    }

    @Operation(summary = "타임캡슐 삭제", description = "ID기반 타임캡슐 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "타임캡슐 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 타임캡슐이 존재하지 않음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCapsule(@PathVariable("id") Long id) {
        capsuleService.deleteCapsule(id);
        return ResponseEntity.noContent().build();
    }
}