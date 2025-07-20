package com.dasom.MemoReal.domain.capsule.controller;

import com.dasom.MemoReal.domain.capsule.dto.CapsuleCreateRequestDto;
import com.dasom.MemoReal.domain.capsule.dto.CapsuleUpdateRequestDto;
import com.dasom.MemoReal.domain.capsule.entity.Capsule;
import com.dasom.MemoReal.domain.capsule.service.CapsuleService;
import com.dasom.MemoReal.domain.ipfs.service.IpfsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/capsules")
@RequiredArgsConstructor
public class CapsuleController {

    private final CapsuleService capsuleService;
    private final IpfsService ipfsService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Capsule> createCapsule(
            @RequestPart("data") String data,
            // 수동 데이터 입력
            // {"capsuleType":"일반","title":"제목","content":"내용","openTime":"2025-07-20T14:03:42.989Z"}
            @RequestPart(value = "media", required = false) MultipartFile mediaFile
    ) throws JsonProcessingException {
        CapsuleCreateRequestDto dto = objectMapper.readValue(data, CapsuleCreateRequestDto.class);

        String cid = (mediaFile != null && !mediaFile.isEmpty()) ? ipfsService.uploadFile(mediaFile) : null;

        Capsule saved = capsuleService.createCapsule(dto, cid);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Capsule> getCapsuleById(@PathVariable Long id) {
        Capsule capsule = capsuleService.selectCapsuleById(id);
        return (capsule != null)
                ? ResponseEntity.ok(capsule)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCapsule(@PathVariable Long id) {
        capsuleService.deleteCapsule(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateCapsule(
            @PathVariable("id") Long id,
            @RequestPart("data") String data,
            // 수동 데이터 입력
            // {"capsuleType":"일반","title":"제목","content":"내용","openTime":"2025-07-20T14:03:42.989Z"}
            @RequestPart(value = "media", required = false) MultipartFile mediaFile
    ) throws JsonProcessingException {

        CapsuleUpdateRequestDto dto = objectMapper.readValue(data, CapsuleUpdateRequestDto.class);

        String newCid = null;
        if (mediaFile != null && !mediaFile.isEmpty()) {
            newCid = ipfsService.uploadFile(mediaFile);
        }

        capsuleService.updateCapsule(id, dto, newCid);
        return ResponseEntity.ok().build();
    }
}