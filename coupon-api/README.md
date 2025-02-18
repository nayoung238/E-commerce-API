## E-commerce side project: Coupon-service

![](/_img/e_commerce_241129.png)

- Spring Boot, Spring Data JPA
- Spring Cloud G/W, Eureka
- **Redis (Streams, Lua Script)**
- Kafka
- MySQL
<br>

## 📚 Refactoring Log

### Redis Streams 이벤트 발행

![](/_img/redis_streams_vs_kafka.png)

- Lua script로 대기열 제거, 수량 감소, 이벤트 발행 작업 원자적 처리
- 이벤트 발행 정확도 테스트 결과: Redis Streams + Lua script (100%) / Kafka (92.66%)
- https://medium.com/@im_zero/%EC%BF%A0%ED%8F%B0-%EB%B0%9C%EA%B8%89%EC%9D%84-%EC%9C%84%ED%95%9C-redis-streams-lua-script-%EC%A0%81%EC%9A%A9%EA%B8%B0-5f3dc4d02b2c