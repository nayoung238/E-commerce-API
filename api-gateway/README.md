## Spring Cloud Gateway

![png](/_img/api-gateway.png)

user, item, order service 등 여러 Microservice의 포트 번호를 기억하고 있다면, 각 마이크로서비스의 포트 번호를 이용해 접근하면 된다. 
하지만 다양한 Microservice가 생기고, **Scale-out을 위해 여러 인스턴스를 구동한다면 모두 다른 포트 번호를 할당하고 관리하는 것이 번거로울 것**이다. 
이를 위해 Spring Cloud Gateway를 사용한다.
<br>

Gateway의 포트 번호가 8089번, Item-Service의 포트 번호가 56124번일 경우 다음 2가지 방법으로 Item-Service에 접근할 수 있다.

- ```http://localhost:8089/item-api/...```
- ```http://localhost:56124/item-api/...```

<br>

특정 Microservice를 Scale-out 하는 경우도 마찬가지다. 2개의 Item-Service를 실행하면 중복되지 않는 포트 번호를 사용해야 하고, 포트 번호를 일일이 확인하는 과정은 상당히 번거로울 것이다.
<br>

Spring Cloud Gateway를 사용하면 Gateway의 포트 번호로 등록된 모든 마이크로서비스에 쉽게 접근할 수 있다.

- ```http://localhost:8089/item-api/items/23```
- ```http://localhost:8089/order-api/orders/7```
- ```http://localhost:8089/auth-api/users/9584```

<br>

## Route 등록

Spring Cloud Gateway + Eureka 관련 설정은 https://medium.com/@im_zero/spring-cloud-gateway-eureka-25567532cfcd 에서 자세히 확인할 수 있다.

```yml
# application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: item-service
          uri: lb://ITEM-SERVICE
          predicates:
            - Path=/item-api/**
          filters:
            - RewritePath=/item-api/?(?<segment>.*), /$\{segment}
```
<br>

![png](/_img/eureka_instances.png)

Eureka server에 client로 등록한 Microservice, API Gateway를 실행하면 ```http://localhost:8761```에서 등록된 모든 instance를 확인할 수 있다.
<br>

API Gateway는 8089번 포트 번호로 설정했고, 모든 Microservice는 Random 포트 번호로 설정했기 때문에 실행할 때마다 랜덤하게 할당된다. 포트 번호를 확인하고 싶다면 클릭하거나 마우스 커서를 올려 왼쪽 하단에서 확인할 수 있다.

<br>

## Circuit Breaker 설정

Spring Cloud Gateway + Circuit Breaker 관련 설정은 https://medium.com/@im_zero/spring-cloud-gateway-circuit-breaker-time-limiter-5e3c26a62b4c 에서 자세히 확인할 수 있다.

![png](/_img/api-gateway-circuit-breaker.png)

특정 서버의 장애가 다른 서버로 전파되는 것을 방지하고, 서버 오류에도 클라이언트에게 빠른 응답을 하기 위해 Circuit Breaker를 설정했다.


