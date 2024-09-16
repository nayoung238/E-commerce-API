## E-commerce side project: Item-service

![](/_img/e_commerce_240825.png)
240825 기준
<br>

## 트러블슈팅 리스트

### Kafka Streams Window Aggregations 적용해 DB I/O 최소화
![](/_img/kafka-streams-window-aggregations.png)

- 여러 요청의 집계 결과를 DB에 한 번에 반영해 **DB I/O 최소화**
- Tumbling Window 사용 → 이벤트 중복 집계 방지
- Suppress operator 사용 → window의 intermediate 결과 emit 방지
- [Window Aggregations 코드](https://github.com/imzero238/Item-service/blob/master/src/main/java/com/ecommerce/itemservice/kafka/config/streams/StockAggregationTopology.java)
- Window Results 제어 [포스팅](https://medium.com/@im_zero/kafka-streams%EC%9D%98-window-results-%EC%BB%A8%ED%8A%B8%EB%A1%A4%ED%95%98%EA%B8%B0-3c20c360cf02)

