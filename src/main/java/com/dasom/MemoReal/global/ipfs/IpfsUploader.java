package com.dasom.MemoReal.global.ipfs;

import com.dasom.MemoReal.global.exception.CustomException;
import com.dasom.MemoReal.global.exception.ErrorCode;
import com.dasom.MemoReal.global.ipfs.dto.IpfsUploadResult;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

@Component
public class IpfsUploader {

    public IpfsUploadResult upload(File file) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:5001/api/v0/add",
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String bodyStr = response.getBody();
                if (bodyStr != null && bodyStr.contains("Hash")) {
                    String hash = extractValue(bodyStr, "Hash");
                    String name = extractValue(bodyStr, "Name");
                    String size = extractValue(bodyStr, "Size");
                    return new IpfsUploadResult(hash, name, Long.parseLong(size));
                }
            }
            throw new CustomException(ErrorCode.UPLOAD_FAILED, "IPFS 응답 실패: " + response.getStatusCode());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UPLOAD_FAILED, "IPFS 요청 실패: " + e.getMessage());
        }
    }

    private String extractValue(String json, String key) {
        int start = json.indexOf("\"" + key + "\":\"") + key.length() + 4;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
