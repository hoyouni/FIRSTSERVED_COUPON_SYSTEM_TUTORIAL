package com.example.coupon_system_tutorial.service;

import com.example.coupon_system_tutorial.domain.Coupon;
import com.example.coupon_system_tutorial.repository.CouponRepository;
import org.springframework.stereotype.Service;

/**
 * 쿠폰 발급 로직을 위한 Service
 * 선착순 100명에게 할인쿠폰을 제공하는 이벤트 진행
 * [요구사항]
 * 1. 선착순 100명에게만 지급되어야 함
 * 2. 101개 이상 지급이 되면 안된다.
 * 3. 순간적으로 몰리는 트래픽을 버텨내야 한다.
 */
@Service
public class ApplyService {

    // CRUD 로직 작성을 위한 발급쿠폰 repo 필드 생성
    private final CouponRepository couponRepository;

    public ApplyService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public void apply(Long userId) {
        // 현재 발급된 쿠폰 수량 확인
        Long count = couponRepository.count();

        // 발급갯수가 100개 이상이면 발급 안해줌
        if(count > 100) {
            return;
        }

        // 그 외의 경우 쿠폰 엔티티에 행 추가 해줌
        couponRepository.save(new Coupon(userId));
    }
}
