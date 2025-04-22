package com.example.coupon_system_tutorial.consumer;

import com.example.coupon_system_tutorial.domain.Coupon;
import com.example.coupon_system_tutorial.domain.FailedEvent;
import com.example.coupon_system_tutorial.repository.CouponRepository;
import com.example.coupon_system_tutorial.repository.FailedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Topic 에 전송된 데이터를 가져오기 위한 Consumer 클래스
 * 그런데,, Consumer 에서 쿠폰을 발급하다가 에러가 발생하면 ?
 * - 현재 로직상 Consumer 에서 Topic 에 있는 데이터를 가져간 후에 쿠폰을 발급하는 과정에서 에러가 발생한다면
 *   쿠폰은 발급되지 않았는데 발급된 쿠폰의 갯수만 증가하는 문제가 발생할 수 있음
 *   결과적으로 100개보다 적은 수량이 쿠폰이 발급될 수 있음.
 * 방안 ) 쿠폰을 발급하다가 오류가 발생하면 백업 데이터와 로그를 남기도록 하자
 */
@Component
public class CouponCreateConsumer {

    // 컨슈머에서 쿠폰 발급 하기 위한 쿠폰발급 엔티티 DI
    private final CouponRepository couponRepository;

    // 쿠폰 발급 실패 시 데이터 저장을 위한 변수
    private final FailedEventRepository failedEventRepository;

    // 로그 남기기 위한 변수 선언
    private final Logger logger = LoggerFactory.getLogger(CouponCreateConsumer.class);

    public CouponCreateConsumer(CouponRepository couponRepository, FailedEventRepository failedEventRepository) {
        this.couponRepository = couponRepository;
        this.failedEventRepository = failedEventRepository;
    }

    // 데이터를 가져오기 위한 리스너 메소드 추가 + 쿠폰발급 엔티티에 행 추가하는 기능 추가 + 실패 시 로그 남기고 실패한 유저 아이디 데이터 저장
    @KafkaListener(topics = "coupon_create", groupId = "group_1")
    public void listener(Long userId) {

        try {
            couponRepository.save(new Coupon(userId));
        } catch(Exception e) {
            logger.error("failed to create coupon : " + userId);
            failedEventRepository.save(new FailedEvent(userId));
        }

    }
}
