package com.sig.todaysnews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableRedisRepositories
public class TodaysNewsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodaysNewsApplication.class, args);
    }

}
