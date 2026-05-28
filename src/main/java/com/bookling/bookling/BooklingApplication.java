package com.bookling.bookling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BooklingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BooklingApplication.class, args);
    }

    // 외부 API 통신을 위한 RestTemplate 장치 등록
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}