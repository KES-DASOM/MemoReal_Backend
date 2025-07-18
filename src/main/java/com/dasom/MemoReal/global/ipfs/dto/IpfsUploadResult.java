package com.dasom.MemoReal.global.ipfs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IpfsUploadResult {
    private final String hash;
    private final String fileName;
    private final long fileSize;
}
