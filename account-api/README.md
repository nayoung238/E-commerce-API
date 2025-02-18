## E-commerce project: Account service

![](/_img/e_commerce_240218.png)
<br>

## 기능 관련 리뷰

### Resilience4J CircuitBreaker + Retry 설정

![](/_img/circuit-breaker-retry.png)

- OpenFeign & Fallback [코드](https://github.com/imzero238/Account-service/blob/master/src/main/java/com/ecommerce/accountservice/openfeign/OrderServiceClient.java)
- CircuitBreaker Config [코드](https://github.com/imzero238/Account-service/blob/master/src/main/java/com/ecommerce/accountservice/resilience4j/Resilience4jCircuitBreakerConfig.java)
- Retry Config [코드](https://github.com/imzero238/Account-service/blob/master/src/main/java/com/ecommerce/accountservice/resilience4j/Resilience4jRetryConfig.java)
- [리뷰](https://medium.com/@im_zero/resilience4j-retry-circuitbreaker-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0-a60d06a46c54)