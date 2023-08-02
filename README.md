## MSA with Spring Cloud: Item-Service

![](/_img/architecture_230801.png)
![](/_img/dbdiagram_230422.png)

- [프로젝트 요약](#프로젝트-요약)
- [repository](#repository)
- [Kafka Consumer](#kafka-consumer) 설정

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

## Kafka Consumer

- commit: https://github.com/nayoung8142/Item-service/commit/c8400d33c564f144f3177b909513fe1df288989e

```json
// OrderRequest
{
    "itemId": "2",
    "quantity": "5",
    "unitPrice": "1000",
    "totalPrice": "5000",
    "accountId": "1"
}

// jsonInString
{"itemId":2,"quantity":5,"unitPrice":1000,"totalPrice":5000,"accountId":2}
```
- ```order-service``` 로부터 사용자 요청이 String 타입으로 변환되고 Kafka를 통해 메시지 수신

```java
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ItemRepository itemRepository;

    @KafkaListener(topics = "update-stock-topic")
    public void updateStock(String kafkaMessage) {
        Map<Object, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Item item = itemRepository.findById(Long.parseLong(String.valueOf(map.get("itemId")))).orElseThrow();
        item.updateStock(Long.parseLong(String.valueOf(map.get("quantity"))));
        itemRepository.save(item);
    }
}
```
- ```order-service```는 받은 요청을 String 타입로 변환해 ```update-stock-topic``` 이라는 topic에 전달
- ```{"itemId":2,"quantity":5,"unitPrice":1000,"totalPrice":5000,"accountId":2}``` 라는 메시지를 받으면 해당되는 아이템의 재고 감소 작업을 진행
- *예외처리 추가할 예정*


### 결과

![png](/_img/result_of_stock_reduction.png)

- ```order-service```에서 주문을 생성하면 ```item-service```에서 해당되는 아이템의 재고를 감소 시킴
- 100개의 재고가 존재했던 상품에 대해 5개를 주문하면 정상적으로 감소시키는 것을 확인