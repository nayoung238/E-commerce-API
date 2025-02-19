package com.ecommerce.apigatewayservice.service.mypage;

import com.ecommerce.apigatewayservice.service.mypage.dto.MyPageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageCQRSService {

    private final MyPageCompositionService myPageCompositionService;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public Mono<ServerResponse> getMyPageDetails(ServerRequest serverRequest) {
        Long accountId = Long.valueOf(serverRequest.pathVariable("accountId"));

        return isExistMyPage(accountId)
                .flatMap(exists -> exists
                        ? findMyPage(accountId)
                        : createAndSaveMyPage(serverRequest))
                .flatMap(myPageResponseDto -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(myPageResponseDto))
                .doOnError(error -> log.error("Error", error));
    }

    private Mono<Boolean> isExistMyPage(Long accountId) {
        Query query = new Query(Criteria.where("accountId").is(accountId));
        return reactiveMongoTemplate
                .exists(query, MyPageResponseDto.class)
                .doOnNext(exists -> log.info("MyPage {} for accountId: {}", exists ? "exists" : "doesn't exist", accountId));
    }

    private Mono<MyPageResponseDto> findMyPage(Long accountId) {
        Query query = new Query(Criteria.where("accountId").is(accountId));
        return reactiveMongoTemplate
                .findOne(query, MyPageResponseDto.class)
                .doOnNext(myPageResponseDto -> log.info("Found MyPage for accountId: {}", myPageResponseDto.accountId()));
    }

    private Mono<MyPageResponseDto> createAndSaveMyPage(ServerRequest serverRequest) {
        return myPageCompositionService
                .getMyPageDetails(serverRequest)
                .flatMap(this::saveMyPage)
                .doOnNext(myPageResponseDto -> log.info("Created and saved myPage for accountId: {}", myPageResponseDto.accountId()));
    }

    private Mono<MyPageResponseDto> saveMyPage(MyPageResponseDto myPageResponseDto) {
        return reactiveMongoTemplate.save(myPageResponseDto);
    }
}
