package org.example.pingpong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.example.pingpong")
public class PingPongApplication {
    public static void main(String[] args) {
        SpringApplication.run(PingPongApplication.class, args);
    }
}

