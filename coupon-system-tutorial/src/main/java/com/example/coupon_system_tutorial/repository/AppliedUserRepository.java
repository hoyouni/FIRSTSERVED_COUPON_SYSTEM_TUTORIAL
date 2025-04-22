package com.example.coupon_system_tutorial.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Set 관리용 레포지토리
 * ex ) '홍길동' 이라는 유저가 쿠폰 발급 요청한 경우
 * sadd applied_user 홍길동 >> 구문 실행
 * return 1 (성공)
 * sadd applied_user 홍길동 >> 구문 실행
 * return 0 (실패)
 * 이미 Set 자료구조에 담긴 데이터는 중복 허용되지 않으므로 0 을 리턴함
 */
@Repository
public class AppliedUserRepository {

    // Redis 명령어 수행을 위한 변수 선언
    private final RedisTemplate<String, String> redisTemplate;

    public AppliedUserRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Set 에 데이터를 담기위한 메소드
    public Long add(Long userId) {
        return redisTemplate.opsForSet().add("applied_user", userId.toString());
    }
}
