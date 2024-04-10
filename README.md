## E-commerce project: Order-service

![](/_img/e_commerce_240218.png)

<br>

## 기능 관련 리뷰

### Kafka Streams Join으로 주문 생성 이슈 해결

Kafka, DB 간 속도 차이 ➝ KStream-KTable Join으로 해결

- 코드: https://github.com/imzero238/Order-service/blob/master/src/main/java/com/ecommerce/orderservice/kafka/streams/KStreamKTableJoinConfig.java#L80
- 리뷰 링크 추가 예정

### Kafka Streams 내부 상태 관리

Tombstone 레코드 설정으로 내부 상태 관리

- 코드: https://github.com/imzero238/Order-service/blob/master/src/main/java/com/ecommerce/orderservice/kafka/producer/KafkaProducerService.java#L17
- 리뷰: https://medium.com/@im_zero/kafka-streams-internal-state-management-6746c4898e34


### Resilience4J CircuitBreaker + Retry 설정

![](/_img/circuit-breaker-retry.png)

OpenFeign 사용 메서드에 Resilience4J CircuitBreaker, Retry 모듈 추가해 무한 응답하는 상황 방지

- OpenFeign 적용 코드: https://github.com/imzero238/Order-service/blob/master/src/main/java/com/ecommerce/orderservice/openfeign/ItemServiceClient.java
- CircuitBreaker Config 코드: https://github.com/imzero238/Order-service/blob/master/src/main/java/com/ecommerce/orderservice/resilience4j/Resilience4jCircuitBreakerConfig.java
- Retry Config 코드: https://github.com/imzero238/Order-service/blob/master/src/main/java/com/ecommerce/orderservice/resilience4j/Resilience4jRetryConfig.java
- 리뷰: https://medium.com/@im_zero/resilience4j-retry-circuitbreaker-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0-a60d06a46c54

