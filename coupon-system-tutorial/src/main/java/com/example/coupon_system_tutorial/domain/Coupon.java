package com.example.coupon_system_tutorial.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * 발급쿠폰 엔티티 생성
 */
@Entity
public class Coupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            // 고유번호

    private Long userId;        // 발급받은 유저 아이디

    // 기본 생성자
    public Coupon() {

    }

    // 발급받은 유저 아이디를 매개변수로 가지는 생성자
    public Coupon(Long userId) {
        this.userId = userId;
    }

    // Id getter
    public Long getId() {
        return id;
    }

}
