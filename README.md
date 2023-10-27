## E-commerce project: Account service

![](/_img/architecture_231027.png)
<br>

### 사용 기술

- Spring Boot, Spring Cloud
- MySql, Redis
- Resilience 4J: CircuitBreaker, Retry
<br>

## Order-service로부터 사용자 주문 내역 가져오기: OpenFeign 사용

```java
@FeignClient(name = "order-service")
public interface OrderServiceClient {

    @Retry(name = Resilience4JConfig.ORDER_LIST_RETRY_NAME)
    @GetMapping("/orders/{customerAccountId}/{cursorOrderId}")
    List<OrderDto> getOrders(@PathVariable Long customerAccountId, @PathVariable Long cursorOrderId);
}
```
- AccountServiceApplication에 ```@EnableFeignClients``` 추가
- Order-service의 API를 interface로 작성

<br>

## OpenFeign 장애 대응: Resilience4J CircuitBreaker

```java
CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
List<OrderDto> orderDtos = circuitBreaker.run(() -> orderServiceClient.getOrders(id, cursorOrderId),
                                                throwable -> new ArrayList<>());
```

- 일정 기준 동안 응답받지 못하면 Empty List Return
- Configuration: https://github.com/nayoung8142/Account-service/blob/master/src/main/java/com/nayoung/accountservice/openfeign/Resilience4JConfig.java

<br>

## Order-service로부터 사용자 주문 내역 가져오기: RestTemplate 사용

- commit: https://github.com/nayoung8142/Account-service/commit/066d2bf38adf4151bbae5e2a2155ea00cce27a6b

```java
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;  
    private final RestTemplate restTemplate;
    private final Environment environment;

    @Override
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id).orElseThrow();
        AccountResponse response = AccountResponse.fromAccountEntity(account);

        String orderUrl = String.format(environment.getProperty("order_service.url"), id);

        ResponseEntity<List<OrderResponse>> orderResponse =
                restTemplate.exchange(orderUrl, HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<OrderResponse>>() {
                        });

        response.setOrders(orderResponse.getBody());
        return response;
    }
}
```

```yaml
order_service:
  url: http://127.0.0.1:8080/order-service/%s/orders
```

![](/_img/connect_to_order_service_result.png)

- ```http://127.0.0.1:8080/account-service/account/1``` 작성 시 위와 같이 주문 목록 가져옴