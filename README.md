## Spring Cloud Gateway

![png](/_img/msa_v230211.png)

account, item, order service 등 여러 Microservice의 포트 번호를 기억하고 있다면, 각 마이크로서비스의 포트 번호를 이용해 접근하면 된다. 
하지만 다양한 Microservice가 생기고, **Scale-out을 위해 여러 인스턴스를 구동한다면 모두 다른 포트 번호를 할당하고 관리하는 것이 번거로울 것**이다. 
이를 위해 Spring Cloud Gateway를 사용한다.
<br>

Gateway의 포트 번호가 8080번, Item-Service의 포트 번호가 57814번일 경우 다음 2가지 방법으로 Item-Service에 접근할 수 있다.

- http://localhost:8080/item-service
- http://localhost:57814/iterm-service

> microservice를 구별하기 위해 item-service라는 id까지 작성했는데, 생략하는 방법은 아래에 설명한다.

<br>

특정 Microservice를 Scale-out 하는 경우도 마찬가지다. 2개의 Item-Service를 실행하면 중복되지 않는 포트 번호를 사용해야 한다. 
즉, 포트 번호를 일일이 확인하는 방법은 상당히 번거로울 것이다.
<br>

Spring Cloud Gateway를 사용하면 Gateway의 포트 번호로 등록된 모든 마이크로서비스에 쉽게 접근할 수 있다.

<br>

## Route 동록

```yml
# application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: account-service
          uri: lb://ACCOUNT-SERVICE
          predicates:
            - Path=/account-service/**
        - id: item-service
          uri: lb://ITEM-SERVICE
          predicates:
            - Path=/item-service/**
```

```application.yml``` 에 위와 같이 Microservice를 추가하면 된다.