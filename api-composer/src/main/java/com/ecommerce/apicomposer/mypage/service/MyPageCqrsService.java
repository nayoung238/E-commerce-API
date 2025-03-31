package com.ecommerce.apicomposer.mypage.service;

import com.ecommerce.apicomposer.common.config.KafkaTopicConfig;
import com.ecommerce.apicomposer.mypage.dto.kafka.CouponUpdatedEvent;
import com.ecommerce.apicomposer.mypage.dto.response.MyPageResponse;
import com.ecommerce.apicomposer.mypage.dto.kafka.OrderUpdatedEvent;
import com.mongodb.client.result.DeleteResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageCqrsService {

    private final MyPageCompositionService myPageCompositionService;
    private final MongoTemplate mongoTemplate;

    public MyPageResponse getMyPage(Long userId, HttpServletRequest httpServletRequest) {
        MyPageResponse myPageResponse = findMyPage(userId);
        if (myPageResponse != null) {
            return myPageResponse;
        }

        myPageResponse = myPageCompositionService.getMyPageDetails(httpServletRequest);
        return saveMyPage(myPageResponse);
    }

    private MyPageResponse findMyPage(Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        return mongoTemplate.findOne(query, MyPageResponse.class);
    }

    private MyPageResponse saveMyPage(MyPageResponse myPageResponse) {
        return mongoTemplate.save(myPageResponse);
    }

    @KafkaListener(topics = KafkaTopicConfig.ORDER_UPDATED_TOPIC)
    private void listenOrderUpdated(ConsumerRecord<Object, OrderUpdatedEvent> record) {
        log.info("Event consumed successfully -> Topic: {}, userId: {}, OrderStatus: {}",
            record.topic(),
            record.value().userId(),
            record.value().orderStatus());

        deleteMyPage(record.value().userId());
    }

    @KafkaListener(topics = KafkaTopicConfig.COUPON_UPDATED_TOPIC)
    private void listenCouponUpdated(ConsumerRecord<Object, CouponUpdatedEvent> record) {
        log.info("Event consumed successfully -> Topic: {}, userId: {}, CouponStatus: {}",
            record.topic(),
            record.value().userId(),
            record.value().couponStatus());

        deleteMyPage(record.value().userId());
    }

    private DeleteResult deleteMyPage(Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        return mongoTemplate.remove(query, MyPageResponse.class);
    }
}
