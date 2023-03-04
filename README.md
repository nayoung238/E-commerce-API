## MSA with Spring Cloud: Item-Service

![](/_img/architecture_230228.png)

- [Kafka Consumer](#kafka-consumer) 설정
- 분산 추적: [Zipkin](#zipkin)

<br>

## Kafka Consumer

- commit: https://github.com/evelyn82ny/MSA-item-service/commit/c8400d33c564f144f3177b909513fe1df288989e

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

<br>

## Zipkin

### POST: 주문 생성 및 재고 감소

![](/_img/zipkin_order_and_item_result.png)

### order-service

- INFO [order-service, **bb6eed24344b0ec8**,bb6eed24344b0ec8] 90365 --- [o-auto-1-exec-5] c.n.orderservice.web.OrderController
- INFO [order-service, **bb6eed24344b0ec8**,bb6eed24344b0ec8] 90365 --- [o-auto-1-exec-5] c.n.o.domain.OrderServiceImpl

### item-service

- INFO [item-service, **bb6eed24344b0ec8**,5532ac75ee856649] 88957 --- [ntainer#0-0-C-1] c.n.i.messagequeue.KafkaConsumer

같은 Trace를 사용하지만, 작업마다 Span이 다른 것을 확인할 수 있다.