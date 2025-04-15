package com.example.coupon_system_tutorial.service;

import com.example.coupon_system_tutorial.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    /**
     * Test 1) 쿠폰 정상 발급 여부
     * 1) 현재 발급된 쿠폰 수량을 확인
     * 2) 발급된 쿠폰 수량이 100개 미만인 경우 쿠폰 발행 (테이블 행 추가)
     */
    @Test
    public void applyOnce() {
        applyService.apply(1L);
        long count = couponRepository.count();
        assertEquals(1, count);
    }

    /**
     * Test 2-1) 동시에 요청이 여러개가 들어오는 경우 쿠폰 정상 발급 여부
     * 테스트 결과 기대한 값 100건과 실제 발급 수량(100건 초과) 이 상이함
     * 분명 서비스 로직에서 카운트 세서 100 건 초과되면 행추가 하지 말라고 했는데..?
     * why?
     * Race condition 이 발생하였기 때문
     * Race condition 이란, 두 개 이상의 스레드가 공유 데이터(쿠폰발급 엔티티)에 엑세스를 하고 동시에 작업을 하려고 할 때 발생하는 문제
     * 예시를 들어보면
     * [우리가 예상한 시나리오]
     * 1) 스레드 1 이라는 친구가 총 쿠폰 발행 수량을 확인 : 99개
     * 2) 1번에 의해 총 쿠폰 발행 수량이 100개가 되지 않았으므로 스레드 1 은 쿠폰 발행 진행
     * 3) 스레드 2 라는 친구가 총 쿠폰 발행 수량을 확인 : 100개
     * 4) 3번에 의해 총 쿠폰 발행 수량이 100개가 되었으므로 스레드 2 는 쿠폰을 발행하지 않음
     * [실제 동작한 시나리오]
     * 1) 스레드 1 이라는 친구가 총 쿠폰 발행 수량을 확인 : 99개
     * 2) 스레드 2 라는 친구가 총 쿠폰 발행 수량을 확인 : 99개
     * 3) 1번에 의해 총 쿠폰 발행 수량이 100개가 되지 않았으므로 스레드 1 은 쿠폰 발행 진행 (100개가 생성됨)
     * 4) 2번에 의해 총 쿠폰 발행 수량이 100개가 되지 않았으므로 스레드 2 도 쿠폰 발행 진행 (101개가 생성됨)
     */
    @Test
    public void applyMultiple() throws InterruptedException {
        // 천 개의 요청을 보낸다고 가정
        int threadCnt = 1000;

        // ExecutorService : 병렬작업을 간단하게 할 수 있도록 하는 자바 API
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        /**
         *  모든 요청이 끝날때까지 기다려야하므로 CountDownLatch 사용
         *  CountDownLatch : 다른 스레드에서 수행하는 작업을 기다리도록 도와주는 클래스
         */
        CountDownLatch countDownLatch = new CountDownLatch(threadCnt);

        // 반복문을 사용하여 threadCnt 만큼의 요청보냄
        for(int i = 0; i < threadCnt; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    applyService.apply(userId);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        // 모든 수행이 완료되면 기대값인 100과 동일한지 확인
        long resultCnt = couponRepository.count();

        // 좌측 : 기대값 , 우측 : 실제 발행된 쿠폰 수량
        assertEquals(100, resultCnt);

    }
}