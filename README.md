## E-commerce side project: Order-service

![](/_img/e_commerce_240825.png)

skills 
- Spring Boot, Spring JPA
- Kafka, OpenFeign
- Resilience4J CircuitBreaker + Retry
- MySQL
<br>

## 📚 재미있었던 리팩토링 리스트

### Transactional Outbox Pattern으로 선형성 보장

![](/_img/transactional_outbox_pattern.png)
- DB insert 작업과 Kafka Event 발행 작업의 선형성 보장
- DB insert 된 데이터만 Kafka Event 발생해 DB와 Kafka 간 속도 차이 해결
- but, TPS 저하 심각... 🤔 (현재 Debezium 적용 중)
- [포스팅](https://medium.com/@im_zero/transactional-outbox-pattern%EC%9C%BC%EB%A1%9C-%EC%9D%B4%EB%B2%A4%ED%8A%B8-%EB%B0%9C%ED%96%89-%EB%B3%B4%EC%9E%A5%ED%95%98%EA%B8%B0-0f2e045b2e50)
- [커밋](https://github.com/imzero238/Order-service/commit/6d4fcc6f6b19882117ef7f42369ba689ca535034)
<br>

### Kafka Streams Join으로 DB I/O 최소화

![](/_img/kstream_ktable_join.png)

- KStream-KTable Join 작업으로 DB I/O 최소화
- *but, 데이터 실시간 접근 어려움 & task 할당 등 여러 문제 고려해 사용하지 않음 (포스팅에 포함)*
- [포스팅](https://medium.com/@im_zero/kstream-ktable-join-%EC%A0%81%EC%9A%A9-%EC%8B%A4%ED%8C%A8%EA%B8%B0-f7b8bfa11e42)
- [KStream-KTable Join 코드](https://github.com/imzero238/Order-service/blob/master/src/main/java/com/ecommerce/orderservice/kafka/config/streams/KStreamKTableJoinConfig.java)
<br>

### Resilience4J CircuitBreaker + Retry 설정

![](/_img/circuit-breaker-retry.png)

- OpenFeign 사용 메서드에 Resilience4J CircuitBreaker, Retry 모듈 추가해 무한 응답하는 상황 방지
- Exponential Backoff and Jitter 전략 사용
- [포스팅](https://medium.com/@im_zero/resilience4j-retry-circuitbreaker-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0-a60d06a46c54)
- [OpenFeign 코드](https://github.com/imzero238/Order-service/blob/master/src/main/java/com/ecommerce/orderservice/openfeign/ItemServiceClient.java)
- [CircuitBreaker Config](https://github.com/imzero238/Order-service/blob/master/src/main/java/com/ecommerce/orderservice/resilience4j/Resilience4jCircuitBreakerConfig.java)
- [Retry Config](https://github.com/imzero238/Order-service/blob/master/src/main/java/com/ecommerce/orderservice/resilience4j/Resilience4jRetryConfig.java)

