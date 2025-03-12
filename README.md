## E-commerce API

트래픽이 많은 상황에서 **사용자 응답 개선**과 **한정된 리소스를 효율적으로 사용하는 방법**을 찾기 위한 이커머스 프로젝트

![](_img/e_commerce_241129.png)

기술
- Spring Boot, Spring Data JPA, Security
- Spring Cloud Gateway, Eureka
- Kafka, Kafka Streams
- MySQL, MongoDB
- Redis, Redisson, Redis stream
- OpenFeign
- Resilience4J CircuitBreaker + Retry
<br>

기능
- [x] 회원 관련 API
  - [JwtAuthenticationFilter](https://github.com/nayoung238/E-commerce-API/blob/main/auth-api/src/main/java/com/ecommerce/auth/common/config/JwtAuthenticationFilter.java#L17) 및 [UserPrincipal](https://github.com/nayoung238/E-commerce-API/blob/main/auth-api/src/main/java/com/ecommerce/auth/auth/entity/UserPrincipal.java#L18C14-L18C27)로 사용자 인증
- [x] 주문 관련 API
- [x] 상품 관련 API
- [x] 쿠폰 관련 API

<br>

## 📚 List of Refactoring Tasks

### MSA 내부 호출 설계

![](/_img/service-discovery.png)

- 이슈 발생
  - 마이크로서비스가 API Gateway 역으로 호출 (내부망 → DMZ 호출)
  - API Gateway 역할 모호
  - 경로 증가 → 응답 속도 지연
- 해결
  - 마이크로서비스 간 직접 통신 (OpenFeign, Kafka 활용)
  - Service Discovery(Eureka)를 통해 다른 마이크로서비스 주소 조회
- 성과
  - API Gateway는 클라이언트 요청을 적절한 서비스로 전달하는 역할만 수행
- 포스팅 [DMZ 영역의 API Gateway와 내부 Microservices 분리](https://medium.com/@nayoung238/dmz-%EC%98%81%EC%97%AD%EC%9D%98-api-gateway%EC%99%80-%EB%82%B4%EB%B6%80-microservices-%EB%B6%84%EB%A6%AC-dcd2048bf0d7)

<br>

### API Composition

![](/_img/api-composition.png)

- 이슈 발생
  - 특정 서비스가 다른 서비스의 도메인까지 고려해야 하는 문제 발생
- 해결
  - ~~서비스 진입점인 API Gateway에서~~ [API 조합](https://github.com/nayoung238/E-commerce-API/blob/main/api-composer/src/main/java/com/ecommerce/apicomposer/mypage/service/MyPageCompositionService.java#L36)해 응답(파생 데이터) 생성
    - Webflux 구현으로 유지보수 힘듦 → API composer 서비스로 이동
  - 응답 데이터 Empty ~~Mono~~ 발생 시 중요도가 낮은 데이터는 제외하고 응답
- 새로운 문제 발생
  - 요청마다 여러 서비스 호출(네트워크 비용 증가) 및 인메모리 조인 발생 → CQRS 패턴으로 해결
- 포스팅 [API Composition Pattern](https://medium.com/@nayoung238/api-composition-pattern-f220523ca761)

<br>

### CQRS Pattern

![](/_img/cqrs-pattern.png)
- 이슈 발생
  - API 조합 시 여러 서비스 호출로 네트워크 비용 증가
- 해결
  - ~~API Gateway~~ API composer에서 [생성한 파생 데이터 → MongoDB에 캐싱](https://github.com/nayoung238/E-commerce-API/blob/main/api-composer/src/main/java/com/ecommerce/apicomposer/mypage/service/MyPageCqrsService.java#L26)
  - 원천 데이터 변경 시 기능에 따라 파생 데이터 수정 or 제거
- 성과
  - 요청마다 인메모리 조인 발생하지 않음
  - 캐싱된 데이터 사용 → 네트워크 비용 절감
- 포스팅 [Command and Query Responsibility Segregation (CQRS) pattern](https://medium.com/@nayoung238/command-and-query-responsibility-segregation-cqrs-pattern-674876273ec5)

<br>

### Kafka Streams Window Aggregations 적용해 DB I/O 최소화

![](/_img/kafka-streams-window-aggregations.png)

- 이슈 발생
  - 한정된 DB 커넥션을 요청마다 사용
- 해결
  - 카프카 스트림즈 [윈도우 합계 토폴로지](https://github.com/nayoung238/E-commerce-API/blob/main/item-api/src/main/java/com/ecommerce/itemservice/kafka/config/StockAggregationTopology.java#L41) 설계
  - 여러 요청의 집계 결과를 DB 일괄 반영해 **DB I/O 최소화**
  - Tumbling Window 사용 → 이벤트 중복 집계 방지
  - Suppress operator 사용 → window 최종 결과만 emit
- 새로운 문제 발생
  - **drop 되는 이벤트 발생 → 정합성 깨짐 (적용 실패)**
- 포스팅 [Kafka Streams Aggregations - Window Results 컨트롤하기](https://medium.com/@nayoung238/kafka-streams%EC%9D%98-window-results-%EC%BB%A8%ED%8A%B8%EB%A1%A4%ED%95%98%EA%B8%B0-3c20c360cf02)

<br>

### Transactional Outbox Pattern으로 선형성 보장

![](/_img/transactional_outbox_pattern.png)

- 이슈 발생
  - DB에 insert 되지 못한 요청의 결과 이벤트가 먼저 도착 → DB 업데이트 불가
- 원인
  - DB와 Kafka 간 처리 속도 차이
- 해결
  - [Transactional Outbox 패턴](https://github.com/nayoung238/E-commerce-API/blob/main/order-api/src/main/java/com/ecommerce/orderservice/internalevent/InternalEventListener.java#L28) 적용
  - DB insert 된 데이터만 Kafka Event 발생
- 새로운 문제 발생
  - DB insert 작업과 Kafka Event 발행 작업이 직렬화 되어 TPS 저하 → KStream-KTable Join 해결 시도
- 포스팅 [Transactional Outbox Pattern으로 선형성 보장하기](https://medium.com/@nayoung238/transactional-outbox-pattern%EC%9C%BC%EB%A1%9C-%EC%9D%B4%EB%B2%A4%ED%8A%B8-%EB%B0%9C%ED%96%89-%EB%B3%B4%EC%9E%A5%ED%95%98%EA%B8%B0-0f2e045b2e50)

<br>

### Kafka Streams Join으로 DB I/O 최소화 (10.2s → 1.7s)

![](/_img/kstream_ktable_join.png)

- 이슈 발생
  - Transactional Outbox 패턴 적용 시 DB insert 작업과 Kafka Event 발행 작업 직렬화 → TPS 저하
- 해결
  - [KStream-KTable Join](https://github.com/nayoung238/E-commerce-API/blob/main/order-api/src/main/java/com/ecommerce/orderservice/kafka/config/streams/KStreamKTableJoinConfig.java#L83)으로 최종 결과를 DB insert (DB I/O 감소)
- 성과
  - Transactional Outbox 패턴 대비 처리 속도 **83% 개선 (10.2s → 1.7s)**
- 새로운 문제 발생
  - 데이터 실시간 접근 어려움
  - 내부 상태 관리 필요 → Tombstone 레코드 설정해 해결
- 포스팅 [KStream-KTable Join 적용 실패기 — 성능 83% 개선](https://medium.com/@nayoung238/kstream-ktable-join-%EC%A0%81%EC%9A%A9-%EC%8B%A4%ED%8C%A8%EA%B8%B0-f7b8bfa11e42)


<br>

### Lua script로 Redis Streams 이벤트 발행 (2.73s -> 0.495s)

![](/_img/redis_streams_vs_kafka.png)

- 이슈 발생
  - Redis 트랜잭션 내 여러 명령어 각각 Redis 서버로 전송 → 네트워크 비용 증가
- 원인
  - Redis 트랜잭션 구현 시 [SessionCallback](https://github.com/nayoung238/E-commerce-API/blob/main/coupon-api/src/main/java/com/ecommerce/couponservice/redis/manager/CouponStockRedisManager.java#L55) 사용
- 해결
  - [Lua script](https://github.com/nayoung238/E-commerce-API/blob/main/coupon-api/src/main/java/com/ecommerce/couponservice/redis/manager/CouponStockRedisManager.java#L121)로 여러 작업 명령어 일괄 전송 및 원자적 처리 → 네트워크 비용 절감
- 성과
  - SessionCallback 대비 처리 속도 **82% 개선 (2.73s -> 0.495s)**
- 포스팅 [쿠폰 발급을 위한 Redis Streams + Lua Script 적용기](https://medium.com/@nayoung238/%EC%BF%A0%ED%8F%B0-%EB%B0%9C%EA%B8%89%EC%9D%84-%EC%9C%84%ED%95%9C-redis-streams-lua-script-%EC%A0%81%EC%9A%A9%EA%B8%B0-5f3dc4d02b2c)

<br>

### Redisson + Optimistic Lock으로 동시성 제어

![](/_img/redisson_optimistic_lock.png)

- 이슈 발생
  - DB 락 획득 과정에서 병목 현상 발생
- 원인
  - DB 락 획득을 위해 DB 커넥션 필요
  - 다른 세션이 먼저 DB 락 획득한 경우, DB 커넥션 점유한 상태에서 DB 락 해제 대기
- 해결
  - [Redisson(Distributed Lock) 획득](https://github.com/nayoung238/E-commerce-API/blob/main/item-api/src/main/java/com/ecommerce/itemservice/item/service/StockUpdateByRedissonServiceImpl.java#L28)한 트랜잭션만 DB 접근
  - 트랜잭션보다 Redisson Lease time 먼저 종료되는 상황 대비 → **Optimistic Lock 추가 사용**해 데이터 정합성 유지
- 성과
  - DB 부하 감소

<br>

### Resilience4J CircuitBreaker + Retry 설정

![](/_img/circuit-breaker-retry.png)

- 이슈 발생
  - 이미 다운된, 응답 지연이 발생하는 서비스 재호출 → 응답 지연 가능성 높음
- 해결
  - Resilience4J CircuitBreaker 모듈 추가해 해당 API 상태 확인 → [API Gateway](https://github.com/nayoung238/E-commerce-API/blob/main/api-gateway/src/main/resources/application.yml#L58) 설정, [OpenFeign](https://github.com/nayoung238/E-commerce-API/blob/main/order-api/src/main/java/com/ecommerce/orderservice/openfeign/ItemServiceClient.java#L28) 설정
  - Retry 모듈에 [Exponential Backoff and Jitter 전략](https://github.com/nayoung238/E-commerce-API/blob/main/order-api/src/main/java/com/ecommerce/orderservice/common/config/Resilience4jRetryConfig.java#L19) 설정 (재시도로 인한 네트워크 혼잡 방지)
- 포스팅
  - [Spring Cloud Gateway - Circuit Breaker, Time Limiter](https://medium.com/@nayoung238/spring-cloud-gateway-circuit-breaker-time-limiter-5e3c26a62b4c)
  - [Resilience4J Retry, CircuitBreaker 적용 및 Exponential Backoff and Jitter 재시도 요청 분산시키기](https://medium.com/@nayoung238/resilience4j-retry-circuitbreaker-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0-a60d06a46c54)