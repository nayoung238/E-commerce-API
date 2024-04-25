## E-commerce project: Order-service

![](/_img/e_commerce_240218.png)

<br>

## 기능 관련 리뷰

### Kafka Streams Join으로 주문 생성 이슈 해결

![](/_img/kstream_ktable_join.png)

Kafka, DB 간 속도 차이 ➝ KStream-KTable Join 작업으로 DB 최소화하여 이슈 해결

- KStream-KTable Join [코드](https://github.com/imzero238/Order-service/blob/master/src/main/java/com/ecommerce/orderservice/kafka/streams/KStreamKTableJoinConfig.java#L80)
- Tombstone 레코드 설정으로 내부 상태 관리 [코드](https://github.com/imzero238/Order-service/blob/master/src/main/java/com/ecommerce/orderservice/kafka/producer/KafkaProducerService.java#L17) 맟 [리뷰](https://medium.com/@im_zero/kafka-streams-internal-state-management-6746c4898e34)
- *but, 데이터 접근 어려움 & 이벤트 유실 가능성 등 여러 문제 발생*
<br>

### Transactional Outbox Pattern으로 주문 생성 이슈 해결

![](/_img/transactional_outbox_pattern.png)

- DB 트랜잭션과 카프카 이벤트 발행(내부에서 비동기로 처리) 작업을 원자적으로 처리
- [커밋](https://github.com/imzero238/Order-service/commit/6d4fcc6f6b19882117ef7f42369ba689ca535034) 및 [리뷰](https://medium.com/@im_zero/transactional-outbox-pattern%EC%9C%BC%EB%A1%9C-%EC%9D%B4%EB%B2%A4%ED%8A%B8-%EB%B0%9C%ED%96%89-%EB%B3%B4%EC%9E%A5%ED%95%98%EA%B8%B0-0f2e045b2e50)

<br>

### Resilience4J CircuitBreaker + Retry 설정

![](/_img/circuit-breaker-retry.png)

OpenFeign 사용 메서드에 Resilience4J CircuitBreaker, Retry 모듈 추가해 무한 응답하는 상황 방지

- OpenFeign 적용 [코드](https://github.com/imzero238/Order-service/blob/master/src/main/java/com/ecommerce/orderservice/openfeign/ItemServiceClient.java)
- CircuitBreaker Config [코드](https://github.com/imzero238/Order-service/blob/master/src/main/java/com/ecommerce/orderservice/resilience4j/Resilience4jCircuitBreakerConfig.java)
- Retry Config [코드](https://github.com/imzero238/Order-service/blob/master/src/main/java/com/ecommerce/orderservice/resilience4j/Resilience4jRetryConfig.java)
- [리뷰](https://medium.com/@im_zero/resilience4j-retry-circuitbreaker-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0-a60d06a46c54)

