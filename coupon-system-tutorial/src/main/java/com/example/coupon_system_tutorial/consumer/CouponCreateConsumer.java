package com.example.coupon_system_tutorial.consumer;

import com.example.coupon_system_tutorial.domain.Coupon;
import com.example.coupon_system_tutorial.repository.CouponRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Topic 에 전송된 데이터를 가져오기 위한 Consumer 클래스
 */
@Component
public class CouponCreateConsumer {

    // 컨슈머에서 쿠폰 발급 하기 위한 쿠폰발급 엔티티 DI
    private final CouponRepository couponRepository;

    public CouponCreateConsumer(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    // 데이터를 가져오기 위한 리스너 메소드 추가 + 쿠폰발급 엔티티에 행 추가하는 기능 추가
    @KafkaListener(topics = "coupon_create", groupId = "group_1")
    public void listener(Long userId) {
        couponRepository.save(new Coupon(userId));
    }
}
