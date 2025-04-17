package com.example.coupon_system_tutorial.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * kafka Templete 을 사용해서 Topic 에 데이터를 전송할 Producer 클래스
 */
@Component
public class CouponCreateProducer {

    private final KafkaTemplate<String, Long> kafkaTemplate;

    public CouponCreateProducer(KafkaTemplate<String, Long> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Topic 에 유저 아이디를 전달하기 위한 메소드
    public void create(Long userId) {
        kafkaTemplate.send("coupon_create", userId);
    }
}
