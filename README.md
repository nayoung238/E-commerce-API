## MSA with Spring Cloud Project: Account service

![](/_img/account_service_status_230224.png)

- 사용자 정보를 가져올 때 해당 사용자가 주문한 모든 주문 정보를 가져오는 방식
- ```account-service```와 ```order-service``` 연결: [RestTemplate](#resttemplate) test
- ```account-service```와 ```order-service``` 연결: [FeignClient](#feignclient) 사용
- 장애 처리: [Resilience4J-CircuitBreaker](#resilience4j-circuitbreaker)
- 분산 추적: [Zipkin](#zipkin)

<br>

## RestTemplate

- commit: https://github.com/evelyn82ny/MSA-account-service/commit/066d2bf38adf4151bbae5e2a2155ea00cce27a6b

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

- 현재는 getProperty를 Account-Service의 ```application.yml```에 작성했지만,
- 앞으로 Config-server를 적용해 관리할 예정이다.

위와 같이 작성하면 Account-service와 Order-service가 서로 통신하는 상태가 된다.
아이디가 1인 사용자의 주문을 여러개 생성하고 ```http://localhost:8080/account-service/account/1``` 을 작성하면 다음과 같이 정상적으로 결과를 가져올 수 있다.

![](/_img/connect_to_order_service_result.png)

Order-Service를 여러 instance로 테스트한 결과, 주문을 생성할 때는 여러 instance가 **Round-Robin** 방식으로 주문을 처리하고 사용자 정보를 얻기 위해 사용자의 모든 주문 정보를 가져올 때 문제없이 모든 주문 정보를 가져오는 것을 확인했다.

<br>

## FeignClient

- commit: https://github.com/evelyn82ny/MSA-account-service/commit/db2e6a31271f7d545eaaab0cb33fbc0aa113d5af

```java
@FeignClient(name = "order-service")
public interface OrderServiceClient {

    @GetMapping("/{accountId}/orders")
    List<OrderResponse> getOrders(@PathVariable Long accountId);
}
```
AccountServiceApplication에 ```@EnableFeignClients``` 을 추가하고, Order-service에서 사용할 API를 interface로 생성한다.

```java
@Slf4j
public class AccountServiceImpl implements AccountService {

    
    private final AccountRepository accountRepository;
    private final OrderServiceClient orderServiceClient;

    @Override
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id).orElseThrow();
        AccountResponse response = AccountResponse.fromAccountEntity(account);

        response.setOrders(orderServiceClient.getOrders(id));
        return response;
    }
}
```

RestTemplate을 사용하지 않고 FeignClient를 사용해 Microserive를 연결한 결과, 사용자 정보를 가져올 때 모든 주문 정보를 정상적으로 가져왔다.

<br>

## Resilience4J-CircuitBreaker

- commit: https://github.com/evelyn82ny/MSA-account-service/commit/4aea0318738b39f95fbe40d41ffc496759e17cec

```java
try {
    responseList = orderServiceClient.getOrders(id);
} catch (FeignException e) {
    log.error(e.getMessage());
}
response.setOrders(responseList);
```

```order-service``` 의 문제로 사용자의 주문 정보를 가져오지 못하는 장애 상황을 대비해 ```try-catch``` 사용했는데, **Resilience4J-CircuitBreaker** 을 적용했다.

```java
CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
List<OrderResponse> orders = circuitBreaker.run(() -> orderServiceClient.getOrders(id),
                                                    throwable -> new ArrayList<>());

response.setOrders(orders);
```
위와 같이 변경했으며, 관련 Config는 [Resilience4JConfig.java](https://github.com/evelyn82ny/MSA-account-service/blob/master/src/main/java/com/nayoung/accountservice/config/Resilience4JConfig.java) 에 작성했다.

<br>

## zipkin

### GET: 사용자 정보 및 주문 정보

![/_img/zipkin_account_and_order_result.png]

### account-service

- INFO [account-service, **f58ef567290011f3**,f58ef567290011f3] 88990 --- [o-auto-1-exec-2] c.n.a.web.AccountController
- INFO [account-service, **f58ef567290011f3**,f58ef567290011f3] 88990 --- [o-auto-1-exec-2] c.n.a.domain.AccountServiceImpl

### order-service

- INFO [order-service, **f58ef567290011f3**,f57c31766e7232d7] 90365 --- [o-auto-1-exec-2] c.n.orderservice.web.OrderController
- INFO [order-service, **f58ef567290011f3**,f57c31766e7232d7] 90365 --- [o-auto-1-exec-2] c.n.o.domain.OrderServiceImpl

<br>

### GET: 사용자 정보 및 주문 정보 & order-service에서 오류 발생하는 경우

![/_img/zipkin_account_and_order_error_result.png]

### account-service

- INFO [account-service, **3e54d58f93d7ffd6**,3e54d58f93d7ffd6] 56389 --- [o-auto-1-exec-4] c.n.a.web.AccountController
- INFO [account-service, **3e54d58f93d7ffd6**,3e54d58f93d7ffd6] 56389 --- [o-auto-1-exec-4] c.n.a.domain.AccountServiceImpl

### order-service

- INFO [order-service, **3e54d58f93d7ffd6**,ea7fd2ee14df6be8] 67120 --- [o-auto-1-exec-2] c.n.orderservice.web.OrderController     :
- ERROR [order-service, **3e54d58f93d7ffd6**,ea7fd2ee14df6be8] 67120 --- [o-auto-1-exec-2] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.lang.Exception: 장애 발생] with root cause