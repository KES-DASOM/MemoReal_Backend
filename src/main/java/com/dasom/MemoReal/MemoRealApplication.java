package com.dasom.MemoReal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
// createAt, updatedAt 자동 빌드를 위한 어노테이션
@EnableJpaAuditing
public class MemoRealApplication {

	public static void main(String[] args) {
		SpringApplication.run(MemoRealApplication.class, args);
	}

}
