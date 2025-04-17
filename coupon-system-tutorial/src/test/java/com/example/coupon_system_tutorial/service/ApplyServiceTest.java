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

    }   // End Test 2-1


    /**
     * Test 2-2) Redis 를 활용하여 동시에 요청이 여러개가 들어오는 경우 쿠폰 정상 발급 여부 체크
     * Race Condition 은 두 개 이상의 스레드에서 공유 자원에 대해 엑세스 할 때 발생하는 문제로
     * 싱글 스레드로 작업한다면 Race Condition 은 발생하지 않을 것임.
     * - 방안 1 : 자바에서 제공하는 synchronized 를 사용
     *  . 서버가 여러대가 된다면 다시 Race Condition 발생할 수 있음
     * - 방안 2 : MySql + Redis 를 활용한 Lock
     *  . 우리가 원하는 건 쿠폰 개수에 대한 정합성인데 Lock 을 활용하여 구현한다면
     *    발급된 쿠폰 개수를 가져오는 것 부터 쿠폰을 생성하는 것 까지 Lock 을 걸어야 함
     *    그렇게 된다면 Lock 을 거는 구간이 길어져 성능상 불이익 발생함
     *    예를 들어서 저장하는 로직까지 2초가 걸리게 된다면 Lock 은 2초 뒤에 풀리게 되고 사용자들은 그만큼 기다려야 함.
     * 선착순 쿠폰 발급의 핵심은 쿠폰 갯수이므로 쿠폰 갯수 체크에 대한 정합성만 관리하면 됨.
     *  - 방안 3 : Redis 를 활용하여 핵심 로직 (쿠폰 갯수 체크 및 increase) 구현
     *   . Redis 에는 incr 이라는 명령어가 존재하고 이 명령어는 key 에 대한 value 를 1 씩 증가시키는 명령어임.
     *     Redis 는 싱글 스레드 기반으로 동작하여 Race Condition 을 해결할 수 있을 뿐만 아니라
     *     incr 명령어는 성능도 굉장히 빠르고 데이터 정합성도 보장됨.
     *     * incr key 에 해당하는 숫자를 1씩 증가시키고 증가된 값을 리턴하는 명령어
     *       exec ) {coupon_count : 0}
     *              git bash > incr coupon_count 실행
     *              {coupon_count : 1}
     *              git bash > incr coupon_count 실행
     *              {coupon_count : 2}
     *              ...
     *     * 참고로 flushAll 은 데이터 초기화 명령어
     * 우리는 방안 3) 을 활용하여 incr 명령어를 활용하여 발급된 쿠폰의 갯수를 제어할 것임.
     * 쿠폰을 발급하기 전에 coupon_count 를 1 증가시키고 리턴되는 값이 100 보다 크다면
     * 이미 100개 이상이 발급되었다는 뜻이므로 더 이상 쿠폰이 발급되면 안됨.
     *
     * [방안 3) 사용 시 발생 가능한 문제점]
     * 문제점 1) 현재 로직은 Redis 를 활용해서 쿠폰의 발급 갯수를 가져온 후에 발급이 가능하다면 RDB 에 저장하는 방식
     *         이 방식은 발급하는 쿠폰의 갯수가 많아지면 RDB 에 부하를 주게 되며 사용하는 RDB 가 다양한 곳에서 사용하는 DB 라면
     *         다른 서비스에도 장애를 초래할 수 있음.
     *          ex) MySql 이 1분 당 100개의 insert 작업만 가능하다고 가정.
     *              - 10 : 00 분에 10,000 개의 쿠폰 생성 요청
     *                10 : 01 분에 주문 생성 요청
     *                10 : 02 분에 회원 가입 요청
     *          이렇게 되면 1분에 100개씩 10,000 개를 생성하려면 100분 이라는 시간이 소요됨.
     *          10 : 01, 10 : 02 에 들어온 요청은 100분 이후에 생성이 됨.
     *          타임 아웃이 없다면 느리게라도 모든 요청이 처리 되겠지만 대부분의 서비스에는 타임아웃 옵션이 설정되어 있고
     *          그러므로 주문 회원가입 뿐만 아니라 일부분의 쿠폰도 생성되지 않는 오류가 발생할 수 있음.
     * 문제점 2) 짧은 시간 내에 많은 요청이 들어오게 된다면 DB 서버의 리소스를 많이 사용하게 되므로 부하가 발생하고
     *         서비스 지연 / 오류로 이어질 수 있음.
     */
    @Test
    public void applyMultipleBasedOnRedis() throws InterruptedException {
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
                    applyService.applyBasedOnRedis(userId);
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

    }   // End Test 2-2


    /**
     * Test 2-3) Redis + kafka 를 활용하여 동시에 요청이 여러개가 들어오는 경우 쿠폰 정상 발급 여부 체크
     *      테스트 전에 kafka 에 대해 간략하게 알아보자면
     *      kafka 는 '분산 이벤트 스트리밍 플랫폼' 으로 이벤트 스트리밍이란 소스에서 목적지까지 이벤트를 실시간으로 스트리밍 하는 것임.
     *      kafka 의 기본 구성은
     *      Producer --> Topic <-- Consumer
     *      로 구성되어 있음.
     *      Topic : Queue 라고 생각하면 됨
     *      Producer : Topic 에 데이터를 삽입할 수 있는 기능을 가짐
     *      Consumer : Topic 에 삽입된 데이터를 가져갈 수 있는 기능을 가짐
     *      그래서 kafka 는
     *      소스에서 (Producer) 에서 목적지까지 (Consumer) 데이터를 실시간으로 스트리밍 할 수 있는 플랫폼이라고 함.
     *  그래서 이번 기능 개발의 목표는!
     *      Producer 를 활용하여 쿠폰을 생성할 유저의 아이디를 Topic 에 넣고
     *      Consumer 를 활용하여 유저의 아이디를 가져와서 쿠폰을 생성 / 변경 하도록 하자.
     *
     *  kafka 를 사용하게 되면 Api 를 통해 직접 쿠폰을 발급할 때에 비해서 처리량을 조절할 수 있고
     *  처리량을 조절함에 따라 데이터베이스의 부하를 줄일 수 있다는 장점이 있음.
     *  다만, 테스트케이스에서 확인 했다시피 쿠폰 생성까지 약간의 텀이 발생된다는 단점이 존재한다.
     */
    @Test
    public void applyMultipleBasedOnKafka() throws InterruptedException {
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
                    applyService.applyBasedOnRedisWithKafka(userId);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        Thread.sleep(10000);

        // 모든 수행이 완료되면 기대값인 100과 동일한지 확인
        long resultCnt = couponRepository.count();

        // 좌측 : 기대값 , 우측 : 실제 발행된 쿠폰 수량
        assertEquals(100, resultCnt);

    }   // End Test 2-3

}