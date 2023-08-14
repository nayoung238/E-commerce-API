## MSA with Spring Cloud: Order-service

![](/_img/architecture_230801.png)
![](/_img/dbdiagram_230422.png)

- [프로젝트 요약](#프로젝트-요약)
- [Kafka Producer](#kafka-producer) 설정

<br>

## 프로젝트 요약

### 1. **Monolithic architecture**로 제작한 프로젝트의 **응답 속도를 개선**하기 위한 **MSA 전환 프로젝트**

#### Monolithic architecture 프로젝트에서의 문제
- 주문 생성 및 재고 수정 작업을 멀티스레드 환경에서 테스트한 결과 Deadlock과 Lost Update 현상 발생
- 해당 이슈는 Pessimistic Lock(Exclusive-Lock)을 사용해 해결
  - Exclusive-Lock은 테이블 레벨에 Intention Exclusive(IX) 락과 레코드 레벨에 Next-Key Lock 을 획득
  - 어떤 인덱스를 사용하는가에 따라 X, REC_NOT_GAP(Exclusive Record Lock) 락이 걸릴 수 있음
- 결국 수정을 위해 X-Lock을 획득해야 하며, 모든 요청을 순서대로 처리해 응답 속도가 지연되는 문제 발생 (트래픽 증가 시 전체 시스템에 영향을 미침)
- 이를 해결하기 위해 Order-service와 Item-Service로 분리하고, Order-service에 결과적 일관성 방식을 적용해 응답 속도를 개선하는 것이 목표
<br>

### 2. **Eventual Consistency(결과적 일관성)** 개념을 적용해 응답 속도 개선

![](/_img/eventual_consistency.png)

- Order-service: 최대한 많은 주문을 받기 위해 **Eventual Consistency(결과적 일관성)** 적용
  - 주문 상태를 WAITING 으로 생성한 후, Item-service로 부터 받은 ‘재고 차감 작업’의 결과를 통해 WAITING 상태를 SUCCEED 또는 FAILED 상태로 변경
- Item-service: 정확한 재고 수정을 위해 **실시간 일관성** 사용 (응답 속도보단 정확성에 초점)
<br>

### 3. 마이크로서비스 간 의존성을 낮추기 위해 비동기 메시징 시스템 **Kafka** 사용

- 주문 서비스가 주문 메시지를 발행하면 상품 서비스에서 메시지를 바탕으로 주문 상품에 대한 재고 차감 작업 진행
- 이후 상품 서비스에서 ‘재고 차감 결과’를 이벤트로 발행하면 주문 서비스에서 메시지를 바탕으로 주문 상태를 확정
<br>

- 주문 서비스에서는 일정 시간 내에 ‘재고 처리 결과’ 응답받지 못하면 해당 이벤트를 재시도 대상으로 설정
- 메시지 유실이면 재시도가 적절하지만, **네트워크 지연이라면 재시도로 인해 같은 이벤트가 중복 처리**될 수 있음
- 이를 해결하기 위해 Kafka Idempotent Producer를 사용해도 되지만, 같은 세션에서만 보장되는 기능이므로 사용하지 않음
- **유니크한 주문 ID를 멱등키로 사용**해 상품 서비스에서 재고 차감 작업 전, 해당 이벤트가 이미 처리되었는지 판단해 중복 처리 방지

<br>

### 4. 재고 데이터를 분산 환경에서 동기화하고, Rollback 될 Transaction 줄이기 위해 **Redis 사용**

![](/_img/stock_data_in_redis.png)

- ‘재고 차감’ 이벤트를 받은 Item-service는 Redis에서 재고 데이터를 차감하고, 재고 변동 기록을 insert
  - 재고 차감 이벤트에 대해 재고 변동 기록을 INSERT 하는 작업만 발생하고, DB에서 관리되는 재고 데이터를 실시간으로 갱신하지 않음
  - DB에서 관리되는 재고 데이터를 실시간으로 수정하지 않으므로, X-Lock 획득을 위한 대기 시간이 없어져 응답 속도 개선을 기대할 수 있음
<br>

- 재고 부족으로 실패 처리가 확정인 요청에 대해 ‘재고 차감’ 이벤트를 발생시키지 않으면, Rollback 될 트랜잭션이 감소하므로 Item-Service의 부하가 줄어들 것이라 기대
  - Item-service에서 재고 관리를 위해 사용하는 Redis를 Order-service에서 Read-only 로 사용
  - Order-service는 Redis에서 요청을 처리할 수 있는 재고를 파악한 뒤, 처리할 수 있는 경우에만 ‘재고 차감’ 이벤트를 발생
- 이 경우 재고 데이터를 Order-service(Read-only)와 Item-service(Write-only)에서 같이 사용하므로 마이크로서비스 간 데이터 분리가 제대로 안 된 경우인지 의심됨

<br>

## Repository

- Order Service https://github.com/nayoung8142/Order-service
- Item Service https://github.com/nayoung8142/Item-service
- Coupon Service https://github.com/nayoung8142/Coupon-service
- Account Service https://github.com/nayoung8142/Account-service

<br>

## Kafka Producer

- commit: https://github.com/nayoung8142/Order-service/commit/af415548fc1cba87ca20ee5ba2d734092fcfb8a7

```java
@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String kafkaTopic, OrderRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        kafkaTemplate.send(kafkaTopic, jsonInString);
    }
}
```
- ```OrderRequest``` 객체로 넘어온 요청을 String 타입으로 변환한 뒤 Kafka topic에 전달하기 위한 로직 작성

```json
// OrderRequest
{
    "itemId": "2",
    "quantity": "5",
    "unitPrice": "1000",
    "totalPrice": "5000",
    "accountId": "1"
}

// jsonInString (String Type)
{"itemId":2,"quantity":5,"unitPrice":1000,"totalPrice":5000,"accountId":2}
```
- mapper를 통해 ```OrderRequest``` 를 String 형태로 변경해 토픽에 전달한다.

<br>

```java
public class OrderController {

    private final OrderService orderService;
    private final KafkaProducer kafkaProducer;

    @PostMapping("{accountId}/orders")
    public ResponseEntity<?> create(@PathVariable Long accountId, @RequestBody OrderRequest orderRequest) {
        orderRequest.setAccountId(accountId);
        OrderResponse response = orderService.create(orderRequest);

        /* kafka */
        kafkaProducer.send("update-stock-topic", orderRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```
- 주문 생성 후 재고를 줄이고자 위에서 생성한 send 메소드에  ```update-stock-topic``` 이라는 topic 정보와 request를 전달
- 토픽 이름은 Kafka Consumer인 ```item-service``` 에서 지정


### 결과

![png](/_img/result_of_stock_reduction.png)

- 100개의 재고가 존재했던 상품에 대해 5개를 주문하면 정상적으로 감소되는 것을 확인