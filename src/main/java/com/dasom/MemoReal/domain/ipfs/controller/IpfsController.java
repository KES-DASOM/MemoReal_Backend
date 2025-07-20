package com.dasom.MemoReal.domain.ipfs.controller;

import com.dasom.MemoReal.domain.ipfs.service.IpfsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ipfs")
@RequiredArgsConstructor
public class IpfsController {

    private final IpfsService ipfsService;

    // 파일 업로드 API 예시 (이미지, 동영상 등)
    @PostMapping("/upload/file")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String cid = ipfsService.uploadFile(file);
            return ResponseEntity.ok(cid);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("IPFS 파일 업로드 실패: " + e.getMessage());
        }
    }
}
