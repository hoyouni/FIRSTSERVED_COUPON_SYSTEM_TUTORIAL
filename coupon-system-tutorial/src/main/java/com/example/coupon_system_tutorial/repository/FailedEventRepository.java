package com.example.coupon_system_tutorial.repository;

import com.example.coupon_system_tutorial.domain.FailedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 쿠폰 발급 실패 시 데이터 저장을 위한 레포지토리
 */
public interface FailedEventRepository extends JpaRepository<FailedEvent, Long> {
}
