## MSA with Spring Cloud: Order-service

![](/_img/architecture_230228.png)

- [Kafka Producer](#kafka-producer) 설정
- 분산 추적: [Zipkin](#zipkin)

<br>

## Kafka Producer

- commit: https://github.com/evelyn82ny/MSA-order-service/commit/af415548fc1cba87ca20ee5ba2d734092fcfb8a7

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

<br>

## Zipkin

### POST: 주문 생성 및 재고 감소

![/_img/zipkin_order_and_item_result.png]

### order-service

- INFO [order-service, **bb6eed24344b0ec8**,bb6eed24344b0ec8] 90365 --- [o-auto-1-exec-5] c.n.orderservice.web.OrderController
- INFO [order-service, **bb6eed24344b0ec8**,bb6eed24344b0ec8] 90365 --- [o-auto-1-exec-5] c.n.o.domain.OrderServiceImpl

### item-service

- INFO [item-service, **bb6eed24344b0ec8**,5532ac75ee856649] 88957 --- [ntainer#0-0-C-1] c.n.i.messagequeue.KafkaConsumer

같은 Trace를 사용하지만, 작업마다 Span이 다른 것을 확인할 수 있다.