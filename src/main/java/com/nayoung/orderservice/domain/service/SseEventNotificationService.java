package com.nayoung.orderservice.domain.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.orderservice.domain.OrderItemStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service @Slf4j
@RequiredArgsConstructor
public class SseEventNotificationService {

    private final Map<String, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SseEmitter subscribe(String key) {
        SseEmitter sseEmitter = new SseEmitter(5 * 60 * 1000L);
        sseEmitter.onCompletion(() -> {
            log.info("SSE Emitter Completion " + key);
            sseEmitterMap.remove(key);
        });
        sseEmitter.onTimeout(() -> {
            log.error("SSE Emitter Timeout");
            sseEmitterMap.remove(key);
        });
        sseEmitter.onError((t) -> {
            log.error("SSE Emitter Error");
            sseEmitterMap.remove(key);
        });

        sseEmitterMap.put(key, sseEmitter);
        log.info("SSE subscription: " + key);

        /*
            첫 SSE 연결 시 Dummy data 전달
            -> Emitter 생성하고 만료 시간까지 데이터 보내지 않으면
               재연결 요청 시 503 Service Unavailable 발생 및 연결 종료될 수 있음
         */
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("First connection")
                    .id(key)
                    .data(key + " -> SSE subscription"));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return sseEmitter;
    }

    public void sendOrderResult(String key, OrderItemStatus orderStatus) {
        SseEmitter sseEmitter = sseEmitterMap.get(key);
        if(sseEmitter != null) {
            try {
                sseEmitter.send(SseEmitter.event()
                                            .id(key)
                                            .name("Order Status")
                                            .data(objectMapper.writeValueAsString(new EmitterData(key, orderStatus)), MediaType.APPLICATION_JSON));
                sseEmitter.complete();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        else {
            log.error("SSE Emitter for event Id ("+ key + ") doesn't exist on this server");
            // TODO: 이벤트 생성
        }
    }

    private static class EmitterData {
        @JsonProperty("Order event Id")
        String orderEventId;
        @JsonProperty("Order Status")
        String orderStatus;
        EmitterData(String key, OrderItemStatus orderStatus) {
            this.orderEventId = key;
            this.orderStatus = String.valueOf(orderStatus);
        }
    }
}
