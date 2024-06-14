package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class RedisMsRedissionApplication {

    public static void main(String[] args) {
        try {
            log.info("开始启动。。。");
            SpringApplication.run(RedisMsRedissionApplication.class, args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
