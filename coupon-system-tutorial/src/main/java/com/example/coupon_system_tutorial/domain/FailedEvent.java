package com.example.coupon_system_tutorial.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * 쿠폰 발급에 실패했을 때 담아둘 엔티티
 */
@Entity
public class FailedEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;    // 쿠폰이 발급되어야 하는 유저 아이디

    public FailedEvent() {
    }

    public FailedEvent(Long userId) {
        this.userId = userId;
    }
}
