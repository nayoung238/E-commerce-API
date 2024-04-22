package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public SseEmitter subscribe(String orderEventId) {
        SseEmitter sseEmitter = new SseEmitter(5 * 60 * 1000L);
        sseEmitter.onCompletion(() -> {
            log.info("SSE Emitter Completion " + orderEventId);
            sseEmitterMap.remove(orderEventId);
        });
        sseEmitter.onTimeout(() -> {
            log.error("SSE Emitter Timeout");
            sseEmitterMap.remove(orderEventId);
        });
        sseEmitter.onError((t) -> {
            log.error("SSE Emitter Error");
            sseEmitterMap.remove(orderEventId);
        });

        sseEmitterMap.put(orderEventId, sseEmitter);
        log.info("SSE subscription: " + orderEventId);

        /*
            첫 SSE 연결 시 Dummy data 전달
            -> Emitter 생성하고 만료 시간까지 데이터 보내지 않으면
               재연결 요청 시 503 Service Unavailable 발생 및 연결 종료될 수 있음
         */
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("First connection")
                    .id(orderEventId)
                    .data(orderEventId + " -> SSE subscription"));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return sseEmitter;
    }

    public void sendOrderResult(String orderEventId, OrderStatus orderStatus) {
        SseEmitter sseEmitter = sseEmitterMap.get(orderEventId);
        if(sseEmitter != null) {
            try {
                sseEmitter.send(SseEmitter.event()
                                            .id(orderEventId)
                                            .name("Order Status")
                                            .data(objectMapper.writeValueAsString(new EmitterData(orderEventId, orderStatus)), MediaType.APPLICATION_JSON));
                sseEmitter.complete();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        else {
            log.error("SSE Emitter for event Id ("+ orderEventId + ") doesn't exist on this server");
            // TODO: 이벤트 생성
        }
    }

    private static class EmitterData {
        @JsonProperty("Order-Event-Id")
        String orderEventId;
        @JsonProperty("Order-Status")
        String orderStatus;
        EmitterData(String orderEventId, OrderStatus orderStatus) {
            this.orderEventId = orderEventId;
            this.orderStatus = String.valueOf(orderStatus);
        }
    }
}
