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
public class IpfsClient {

    private static final String IPFS_API_BASE_URL = "http://localhost:5001/api/v0";

    /**
     * IPFS에 파일 업로드
     * @param file 업로드할 파일
     * @return 업로드 결과 객체 (해시, 파일명, 크기)
     */
    public IpfsUploadResult upload(File file) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    IPFS_API_BASE_URL + "/add",
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

    /**
     * IPFS 해시로부터 파일 데이터 다운로드
     * @param ipfsHash IPFS 파일 해시
     * @return 파일 데이터 바이트 배열
     */
    public byte[] download(String ipfsHash) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = IPFS_API_BASE_URL + "/cat?arg=" + ipfsHash;

            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new CustomException(ErrorCode.FILE_NOT_FOUND, "IPFS 파일 다운로드 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND, "IPFS 요청 실패: " + e.getMessage());
        }
    }

    // 간단한 JSON 값 파싱
    private String extractValue(String json, String key) {
        int start = json.indexOf("\"" + key + "\":\"") + key.length() + 4;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
