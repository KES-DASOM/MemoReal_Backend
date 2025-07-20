package com.dasom.MemoReal.domain.user.ipfs.config;

import io.ipfs.api.IPFS;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IpfsConfig {

    @Bean
    public IPFS ipfs() {
        // 로컬 IPFS 노드 (기본 포트 5001)
        return new IPFS("/ip4/127.0.0.1/tcp/5001");
    }
}
