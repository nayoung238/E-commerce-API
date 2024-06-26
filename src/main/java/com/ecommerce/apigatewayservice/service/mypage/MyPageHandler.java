package com.ecommerce.apigatewayservice.service.mypage;

import com.ecommerce.apigatewayservice.service.mypage.dto.AccountDto;
import com.ecommerce.apigatewayservice.service.mypage.dto.MyPageDto;
import com.ecommerce.apigatewayservice.service.mypage.dto.OrderListDto;
import com.ecommerce.apigatewayservice.service.loadbalancer.LoadBalancerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

//@Service
//@RequiredArgsConstructor
//@Slf4j
public class MyPageHandler {
//
//    private final WebClient webClient = WebClient.create();
//    private final LoadBalancerService loadBalancerService;
//    private int accountServicePortNumber;
//    private int orderServicePortNumber;
//
//    public Mono<ServerResponse> getMyPageDetails(ServerRequest serverRequest) {
//        setPortNumber();
//
//        Mono<AccountDto> account = getAccount(serverRequest)
//                .doOnError(throwable -> log.error("Exception thrown by getAccount method: {}", throwable.getMessage()))
//                .onErrorResume(throwable -> Mono.empty())
//                .switchIfEmpty(Mono.error(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)));
//
//        Mono<OrderListDto> orderList = getOrderList(serverRequest)
//                .doOnError(throwable -> log.error("Exception thrown by getOrderList method: {}", throwable.getMessage()))
//                .onErrorResume(throwable -> Mono.just(new OrderListDto(null)));
//
//        Mono<MyPageDto> myPage = account.zipWith(orderList)
//                .map(tuple -> {
//                    AccountDto accountDto = tuple.getT1();
//                    OrderListDto orderListDto = tuple.getT2();
//                    return new MyPageDto(accountDto, orderListDto);
//                });
//
//        return ServerResponse
//                .ok()
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(myPage, MyPageDto.class);
//    }
//
//    private void setPortNumber() {
//        Thread thread = new Thread(() -> {
//            accountServicePortNumber = loadBalancerService.getPortNumber("ACCOUNT-SERVICE");
//            orderServicePortNumber = loadBalancerService.getPortNumber("ORDER-SERVICE");
//        });
//        thread.start();
//        try {
//            thread.join();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private Mono<AccountDto> getAccount(ServerRequest serverRequest) {
//        Long userId = Long.valueOf(serverRequest.pathVariable("userId"));
//
//        return webClient
//                .get()
//                .uri("http://localhost:" + accountServicePortNumber + "/accounts/{userId}", userId)
//                .accept(MediaType.APPLICATION_JSON)
//                .retrieve()
////                .onStatus(httpStatusCode -> httpStatusCode != HttpStatus.OK,
////                        clientResponse -> clientResponse.createException()
////                                .flatMap(throwable -> Mono.error(new ResponseStatusException(clientResponse.statusCode(), throwable.getMessage(), throwable.getCause()))))
//                .bodyToMono(AccountDto.class);
////                .onErrorResume(throwable -> Mono.error(new RuntimeException(throwable)));
//    }
//
//    private Mono<OrderListDto> getOrderList(ServerRequest serverRequest) {
//        Long userId = Long.valueOf(serverRequest.pathVariable("userId"));
//        Long cursorOrderId = Long.valueOf(serverRequest.pathVariable("cursorOrderId"));
//
//        if(cursorOrderId == 0) {  // first page
//            return webClient
//                    .get()
//                    .uri("http://localhost:" + orderServicePortNumber + "/orders/{userId}", userId)
//                    .accept(MediaType.APPLICATION_JSON)
//                    .retrieve()
//                    .bodyToMono(OrderListDto.class);
//        } else {
//            return webClient
//                    .get()
//                    .uri("http://localhost:" + orderServicePortNumber + "/orders/{userId}/{cursorOrderId}", userId, cursorOrderId)
//                    .accept(MediaType.APPLICATION_JSON)
//                    .retrieve()
//                    .bodyToMono(OrderListDto.class);
//        }
//    }
}
