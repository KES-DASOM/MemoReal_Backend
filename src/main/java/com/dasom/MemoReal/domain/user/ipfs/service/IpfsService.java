package com.dasom.MemoReal.domain.user.ipfs.service;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class IpfsService {

    private final IPFS ipfs;

    public IpfsService() {
        this.ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001"); // IPFS 데몬 주소
    }

    public String uploadFile(MultipartFile file) {
        try {
            NamedStreamable.InputStreamWrapper inputStreamWrapper =
                    new NamedStreamable.InputStreamWrapper(file.getInputStream());
            MerkleNode addResult = ipfs.add(inputStreamWrapper).get(0);
            return addResult.hash.toBase58();
        } catch (IOException e) {
            throw new RuntimeException("IPFS 업로드 실패", e);
        }
    }
}