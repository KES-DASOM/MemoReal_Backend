package com.dasom.MemoReal.domain.Capsule.controller;

import com.dasom.MemoReal.domain.Capsule.dto.ContentUploadRequest;
import com.dasom.MemoReal.domain.Capsule.dto.MetadataDto;
import com.dasom.MemoReal.domain.Capsule.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/content")
public class ContentController {

    private final ContentService contentService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadContent(
            @RequestPart("file") MultipartFile file,
            @RequestPart("metadata") ContentUploadRequest metadata
    ) {
        Long userId = 1L; // TODO: JWT에서 추출 예정
        MetadataDto savedMetadata = contentService.upload(file, metadata, userId);
        return ResponseEntity.ok("정상적으로 업로드 되었습니다. 파일 해시: " + savedMetadata.getFileHash());
    }

    @GetMapping("/{id}") // 메타데이터 id를 받아서 상세정보 조회
    public ResponseEntity<MetadataDto> getMetadata(@PathVariable Long id) {
        MetadataDto dto = contentService.retrieveMetadata(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadFile(@PathVariable Long id) {
        byte[] fileData = contentService.downloadFile(id);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"downloaded_file\"")
                .body(fileData);
    }

    @GetMapping("/user/{userId}") // userid를 기반으로 해당 유저가 등록한 모든 메타데이터 조회
    // Todo: JWT에서 userId 추출해서 사용하게 수정 할 예정
    public ResponseEntity<List<MetadataDto>> getUserContent(@PathVariable Long userId) {
        List<MetadataDto> list = contentService.findAllByUserId(userId);
        return ResponseEntity.ok(list);
    }
    @PatchMapping("/{id}")
    // Todo: JWT에서 userId 추출하여 메타데이터 상 등록된 대상과 같을 경우에만 실행 되게 수정
    public ResponseEntity<String> updateMetadataFields(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        String message = contentService.updateMetadataFields(id, updates);
        return ResponseEntity.ok(message);
    }
    @DeleteMapping("/delete/{metadataId}")
    // Todo: JWT 연동 후 실제 유저 아이디로 변경
    public ResponseEntity<String> deleteContent(@PathVariable Long metadataId) {
        Long userId = 1L; // TODO: JWT 연동 후 실제 유저 아이디로 변경
        contentService.deleteMetadataAndContent(metadataId, userId);
        return ResponseEntity.ok("메타데이터 및 IPFS 컨텐츠가 성공적으로 삭제되었습니다.");
    }
}
