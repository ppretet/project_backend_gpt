package com.example.gpt.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class Truck {
    private static final int WINDOW_SIZE_IN_SECONDS = 60;
    private static final int MAX_REQUESTS = 40;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    public boolean is_valid(String username){
        //System.out.println(redisTemplate);
        long currentTime = System.currentTimeMillis();
        String key = "rate_limit:" + username;
        long windowStartTime = currentTime - (WINDOW_SIZE_IN_SECONDS * 1000);
        redisTemplate.opsForZSet().add(key,String.valueOf(currentTime),currentTime);
        redisTemplate.expire(key, WINDOW_SIZE_IN_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStartTime);
        Long count = redisTemplate.opsForZSet().zCard(key);
        //限流
        return count == null || count < MAX_REQUESTS;
    }
}