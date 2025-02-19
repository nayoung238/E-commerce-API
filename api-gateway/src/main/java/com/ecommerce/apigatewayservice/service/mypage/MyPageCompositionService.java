package com.ecommerce.apigatewayservice.service.mypage;

import com.ecommerce.apigatewayservice.service.mypage.dto.AccountResponseDto;
import com.ecommerce.apigatewayservice.service.mypage.dto.MyPageResponseDto;
import com.ecommerce.apigatewayservice.service.mypage.dto.OrderListDto;
import com.ecommerce.apigatewayservice.service.reactiveloadbalancer.ReactiveLoadBalancerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageCompositionService {

    private final ReactiveLoadBalancerService reactiveLoadBalancerService;
    private final WebClient.Builder webClientBuilder;

    public Mono<MyPageResponseDto> getMyPageDetails(ServerRequest serverRequest) {
        Mono<AccountResponseDto> account = getAccount(serverRequest)
                .doOnError(throwable -> log.error("Exception thrown by getAccount method: {}", throwable.getMessage()))
                .onErrorResume(throwable -> Mono.empty())
                .switchIfEmpty(Mono.error(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)));

        Mono<OrderListDto> orderList = getOrderList(serverRequest)
                .doOnError(throwable -> log.error("Exception thrown by getOrderList method: {}", throwable.getMessage()))
                .onErrorResume(throwable -> Mono.just(OrderListDto.emptyInstance()));

        return Mono.zip(account, orderList)
                .map(tuple -> MyPageResponseDto.of(tuple.getT1(), tuple.getT2()));
    }

    private Mono<AccountResponseDto> getAccount(ServerRequest serverRequest) {
        Long accountId = Long.valueOf(serverRequest.pathVariable("accountId"));

        return reactiveLoadBalancerService.chooseInstance("ACCOUNT-SERVICE")
                .flatMap(i -> {
                    String url = String.format("http://%s:%d", i.getHost(), i.getPort());
                    return webClientBuilder.baseUrl(url).build()
                            .get()
                            .uri("/accounts/{accountId}", accountId)
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(AccountResponseDto.class);
                });
    }

    private Mono<OrderListDto> getOrderList(ServerRequest serverRequest) {
        Long accountId = Long.valueOf(serverRequest.pathVariable("accountId"));
        Optional<Long> cursorOrderId = serverRequest.pathVariables().containsKey("cursorOrderId")
                ? Optional.of(Long.parseLong(serverRequest.pathVariable("cursorOrderId")))
                : Optional.empty();

        return reactiveLoadBalancerService.chooseInstance("ORDER-SERVICE")
                .flatMap(i -> {
                    String url = String.format("http://%s:%d", i.getHost(), i.getPort());
                    return webClientBuilder.baseUrl(url).build()
                            .get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/orders/{accountId}")
                                    .path(cursorOrderId.isPresent() ? "/{cursorOrderId}" : "")
                                    .build(accountId, cursorOrderId.orElse(null))
                            )
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(OrderListDto.class);
                });
    }
}
