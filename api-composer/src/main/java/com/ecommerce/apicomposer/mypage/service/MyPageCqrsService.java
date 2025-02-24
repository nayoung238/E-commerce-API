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
        Long accountId = Long.valueOf(httpServletRequest.getHeader("X-Account-Id"));

        MyPageResponseDto myPageResponse = findMyPage(accountId);
        if (myPageResponse != null) {
            return myPageResponse;
        }

        myPageResponse = myPageCompositionService.getMyPageDetails(httpServletRequest);
        return saveMyPage(myPageResponse);
    }

    private MyPageResponseDto findMyPage(Long accountId) {
        Query query = new Query(Criteria.where("accountId").is(accountId));
        return mongoTemplate.findOne(query, MyPageResponseDto.class);
    }

    private MyPageResponseDto saveMyPage(MyPageResponseDto myPageResponseDto) {
        return mongoTemplate.save(myPageResponseDto);
    }

    @KafkaListener(topics = KafkaTopicConfig.ORDER_UPDATED_TOPIC)
    private void listenOrderUpdated(ConsumerRecord<Object, OrderUpdatedEvent> record) {
        log.info("Event consumed successfully -> Topic: {}, accountId: {}, OrderStatus: {}",
            record.topic(),
            record.value().accountId(),
            record.value().orderProcessingStatus());

        deleteMyPage(record.value().accountId());
    }

    @KafkaListener(topics = KafkaTopicConfig.COUPON_UPDATED_TOPIC)
    private void listenCouponUpdated(ConsumerRecord<Object, CouponUpdatedEvent> record) {
        log.info("Event consumed successfully -> Topic: {}, accountId: {}, CouponStatus: {}",
            record.topic(),
            record.value().accountId(),
            record.value().couponStatus());

        deleteMyPage(record.value().accountId());
    }

    private DeleteResult deleteMyPage(Long accountId) {
        Query query = new Query(Criteria.where("accountId").is(accountId));
        return mongoTemplate.remove(query, MyPageResponseDto.class);
    }
}
