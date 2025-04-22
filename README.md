# 선착순 쿠폰 발급 이벤트 시스템 로직 개발 
 - 목적
   1)  선착순 쿠폰 발급 이벤트 시스템 로직 개발을 통해 발생할 수 있는 문제점을 찾고 동시성 등의 문제를 해결하고자 함
   2)  Redis 와 Kafka 에 대한 개념과 사용 이유에 대해 학습하며 활용 방안을 습득하고자 함
      
 - 요구사항
   1)  선착순 100명에게 총 100건의 할인 쿠폰 발행
   2)  쿠폰 발행 수량은 100개를 초과할 수 없음
   3)  순간적으로 발생하는 트래픽에 대한 대응 필요
   4)  1인당 최대 쿠폰 발급 갯수는 1개로 제한함 (중복 발급 불가)
      
# Language
 - Java 17
# Database
 - MySQL
# Framework
 - Spring Boot 3
 - JPA (Hibernate)
# Container
 - Docker
# Cache / Message broker
 - Redis
 - Kafka

