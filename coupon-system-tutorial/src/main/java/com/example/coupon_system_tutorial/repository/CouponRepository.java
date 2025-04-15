package com.example.coupon_system_tutorial.repository;

import com.example.coupon_system_tutorial.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 발급쿠폰 엔티티 CRUD 를 위한 Repository
 */
public interface CouponRepository extends JpaRepository<Coupon, Long> {

}
