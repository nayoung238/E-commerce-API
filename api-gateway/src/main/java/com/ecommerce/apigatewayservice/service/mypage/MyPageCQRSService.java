package com.ecommerce.apigatewayservice.service.mypage;

import com.ecommerce.apigatewayservice.service.mypage.dto.MyPageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.support.ServerResponseResultHandler;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageCQRSService {

    private final MyPageCompositionService myPageCompositionService;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final ServerResponseResultHandler serverResponseResultHandler;

    public Mono<ServerResponse> getMyPageDetails(ServerRequest serverRequest) {
        Long accountId = Long.valueOf(serverRequest.pathVariable("accountId"));

        return isExistMyPage(accountId)
                .flatMap(exists -> exists
                        ? findMyPage(accountId)
                        : createAndSaveMyPage(serverRequest))
                .flatMap(myPageDto -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(myPageDto))
                .doOnError(error -> log.error("Error"));
    }

    private Mono<Boolean> isExistMyPage(Long accountId) {
        Query query = new Query(Criteria.where("accountId").is(accountId));
        return reactiveMongoTemplate
                .exists(query, MyPageDto.class)
                .doOnNext(exists -> log.info("MyPage {} for accountId: {}", exists ? "exists" : "doesn't exist", accountId));
    }

    private Mono<MyPageDto> findMyPage(Long accountId) {
        Query query = new Query(Criteria.where("accountId").is(accountId));
        return reactiveMongoTemplate
                .findOne(query, MyPageDto.class)
                .doOnNext(myPageDto -> log.info("Found MyPage for accountId: {}", myPageDto.getAccountId()));
    }

    private Mono<MyPageDto> createAndSaveMyPage(ServerRequest serverRequest) {
        return myPageCompositionService
                .getMyPageDetails(serverRequest)
                .flatMap(this::saveMyPage)
                .doOnNext(myPageDto -> log.info("Created and saved myPage for accountId: {}", myPageDto.getAccountId()));
    }

    private Mono<MyPageDto> saveMyPage(MyPageDto myPageDto) {
        return reactiveMongoTemplate.save(myPageDto);
    }
}
