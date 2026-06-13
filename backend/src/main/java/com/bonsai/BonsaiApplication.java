package com.bonsai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BonsaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BonsaiApplication.class, args);
    }

}
