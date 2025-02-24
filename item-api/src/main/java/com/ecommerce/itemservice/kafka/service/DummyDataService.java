package com.ecommerce.itemservice.kafka.service;

import com.ecommerce.itemservice.kafka.config.TopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class DummyDataService {
//
//    private final KafkaProducerService kafkaProducerService;
//
//    @Scheduled(fixedDelay = 15_000, initialDelay = 30_000)
//    private void sendDummyData() {
//        log.info("Dummy data sent: {}", LocalDateTime.now());
//        IntStream.rangeClosed(1, 5).forEach(i -> {
//                    kafkaProducerService.sendMessage(TopicConfig.ITEM_UPDATE_LOG_TOPIC, String.valueOf(1), 0L);
//                }
//        );
//    }
//}
