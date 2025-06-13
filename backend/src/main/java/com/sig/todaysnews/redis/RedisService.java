package com.sig.todaysnews.redis;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Transactional
@RequiredArgsConstructor
public class RedisService {
    private final Logger logger = LoggerFactory.getLogger(RedisService.class);
    private final StringRedisTemplate stringRedisTemplate;

    public void addRefreshTokenByRedis(String username, String refreshToken, Duration duration) {
        ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
        stringValueOperations.set(username, refreshToken, duration);
    }

    public void deleteRefreshTokenByRedis(String username) {
        stringRedisTemplate.delete(username);
    }

    public String getRefreshTokenByRedis(String username) {
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        return stringStringValueOperations.get(username);
    }

    public void addAccessTokenByRedis(String username, String accessToken, Duration duration) {
        ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
        stringValueOperations.set(accessToken, username, duration);
    }

    public boolean existsAccessTokenInRedis(String accessToken) {
        return stringRedisTemplate.hasKey(accessToken);
    }
}