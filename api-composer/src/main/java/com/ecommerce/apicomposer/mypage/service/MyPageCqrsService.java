package com.ecommerce.apicomposer.mypage.service;

import com.ecommerce.apicomposer.common.config.KafkaTopicConfig;
import com.ecommerce.apicomposer.mypage.dto.CouponUpdatedEvent;
import com.ecommerce.apicomposer.mypage.dto.MyPageResponseDto;
import com.ecommerce.apicomposer.mypage.dto.OrderUpdatedEvent;
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

    public MyPageResponseDto getMyPage(HttpServletRequest httpServletRequest) {
        Long userId = Long.valueOf(httpServletRequest.getHeader("X-User-Id"));

        MyPageResponseDto myPageResponse = findMyPage(userId);
        if (myPageResponse != null) {
            return myPageResponse;
        }

        myPageResponse = myPageCompositionService.getMyPageDetails(httpServletRequest);
        return saveMyPage(myPageResponse);
    }

    private MyPageResponseDto findMyPage(Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        return mongoTemplate.findOne(query, MyPageResponseDto.class);
    }

    private MyPageResponseDto saveMyPage(MyPageResponseDto myPageResponseDto) {
        return mongoTemplate.save(myPageResponseDto);
    }

    @KafkaListener(topics = KafkaTopicConfig.ORDER_UPDATED_TOPIC)
    private void listenOrderUpdated(ConsumerRecord<Object, OrderUpdatedEvent> record) {
        log.info("Event consumed successfully -> Topic: {}, userId: {}, OrderStatus: {}",
            record.topic(),
            record.value().userId(),
            record.value().orderProcessingStatus());

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
        return mongoTemplate.remove(query, MyPageResponseDto.class);
    }
}
