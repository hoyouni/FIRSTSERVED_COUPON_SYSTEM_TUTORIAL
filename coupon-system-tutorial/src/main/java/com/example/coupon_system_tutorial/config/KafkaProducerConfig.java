package com.example.coupon_system_tutorial.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Topic 에 데이터를 전송하기 위한 Producer 관련 설정 클래스
 */
@Configuration
public class KafkaProducerConfig {

    /**
     * Producer 인스턴스를 생성하는데에 필요한 설정값들을 설정
     * 스프링에서는 손쉽게 설정값들을 설정할 수 있도록 ProducerFactory 라는 인터페이스를 제공
     */
    @Bean
    public ProducerFactory<String, Long> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * kafka Topic 에 데이터를 전송하기 위해 사용할 kafka Templete 생성
     */
    @Bean
    public KafkaTemplate<String, Long> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


}
