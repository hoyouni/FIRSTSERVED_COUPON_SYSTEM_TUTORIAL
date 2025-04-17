package com.example.coupon_system_tutorial.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Redis 명령어를 실행한 repository 생성
 */
@Repository
public class CouponCountRepository {

    // Redis 명령어를 실행할 redis templete 변수 생성
    private final RedisTemplate<String, String> redisTemplate;

    // 생성자
    public CouponCountRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Redis incr 명령어 사용을 위한 메소드 생성
    public Long increment() {
        return redisTemplate.opsForValue().increment("coupon_count");
    }
}
