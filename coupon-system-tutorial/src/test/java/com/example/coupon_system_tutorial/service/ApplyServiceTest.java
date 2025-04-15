package com.example.coupon_system_tutorial.service;

import com.example.coupon_system_tutorial.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 쿠폰발급 로직 테스트 로직 작성
 */
@SpringBootTest
class ApplyServiceTest {

    // 쿠폰발급 서비스 로직
    @Autowired
    private ApplyService applyService;

    // 쿠폰발급 CRUD 를 위한 repository
    @Autowired
    private CouponRepository couponRepository;

    // Test 1) 쿠폰 정상 발급 여부
    @Test
    public void applyOnce() {
        applyService.apply(1L);
        long count = couponRepository.count();
        assertEquals(1, count);
    }
}