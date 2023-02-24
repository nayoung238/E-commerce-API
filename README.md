## MSA with Spring Cloud Project: Account service

![](/_img/account_service_status_230224.png)

- 사용자 정보를 가져올 때 해당 사용자가 주문한 모든 주문 정보를 가져오는 방식
- Account-service와 Order-service를 연결하기 위해 **RestTemplate** 또는 **FeignClient** 방식을 이용

<br>

## RestTemplate 방식

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