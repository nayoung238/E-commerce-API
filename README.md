## E-commerce project: Item-service

![](/_img/e_commerce_240218.png)

<br>

## 기능 관련 리뷰

### Kafka Streams 윈도우 집계 적용
![](/_img/kafka-streams-window-aggregations.png)

여러 요청을 모다 DB에 한 번에 반영하기 위해 카프카 스트림즈 윈도우 집계 적용

- Tumbling Window 사용 → 이벤트 중복 집계 방지
- Suppress operator 사용 → window의 intermediate 결과 emit 방지
- 윈도우 집계 [코드](https://github.com/imzero238/Item-service/blob/master/src/main/java/com/ecommerce/itemservice/kafka/service/streams/StockAggregationService.java)
- [리뷰](https://medium.com/@im_zero/kafka-streams%EC%9D%98-window-results-%EC%BB%A8%ED%8A%B8%EB%A1%A4%ED%95%98%EA%B8%B0-3c20c360cf02)

