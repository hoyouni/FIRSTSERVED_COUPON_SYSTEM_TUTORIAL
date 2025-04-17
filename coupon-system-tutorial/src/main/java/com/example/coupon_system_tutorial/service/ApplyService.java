package com.example.coupon_system_tutorial.service;

import com.example.coupon_system_tutorial.domain.Coupon;
import com.example.coupon_system_tutorial.repository.CouponCountRepository;
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

    // redis incr 명령어 사용을 위한 변수 생성
    private final CouponCountRepository couponCountRepository;

    public ApplyService(CouponRepository couponRepository, CouponCountRepository couponCountRepository) {
        this.couponRepository = couponRepository;
        this.couponCountRepository = couponCountRepository;
    }

    // Redis 사용 전 쿠폰 발급 로직 (상세 내용은 ApplyServiceTest 클래스 확인)
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

    // Redis 를 활용한 쿠폰 발급 로직 (상세 내용은 ApplyServiceTest 클래스 확인)
    public void applyBasedOnRedis(Long userId) {
        // 쿠폰을 발급하기 전(쿠폰발급 테이블 행 추가 전) 에 현재 발급된 쿠폰의 갯수를 증가 시킴
        Long count = couponCountRepository.increment();

        // 발급갯수가 100개보다 많다면 발급 안해줌
        if(count > 100) {
            return;
        }

        // 그 외의 경우 쿠폰 엔티티에 행 추가 해줌
        couponRepository.save(new Coupon(userId));
    }
}
