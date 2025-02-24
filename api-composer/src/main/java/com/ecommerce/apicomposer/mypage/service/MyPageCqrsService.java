package com.ecommerce.apicomposer.mypage.service;

import com.ecommerce.apicomposer.mypage.dto.MyPageResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
}
