package com.ecommerce.apicomposer.mypage.service;

import com.ecommerce.apicomposer.common.exception.CustomException;
import com.ecommerce.apicomposer.common.exception.ErrorCode;
import com.ecommerce.apicomposer.common.service.AuthServiceClient;
import com.ecommerce.apicomposer.common.service.CouponServiceClient;
import com.ecommerce.apicomposer.common.service.OrderServiceClient;
import com.ecommerce.apicomposer.mypage.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageCompositionService {

    private final AuthServiceClient authServiceClient;
    private final OrderServiceClient orderServiceClient;
    private final CouponServiceClient couponServiceClient;

    private static final long TIMEOUT = 5L;

    public MyPageResponseDto getMyPageDetails(HttpServletRequest httpServletRequest) {
        try {
            UserResponseDto user = findUserAsync(httpServletRequest).get();
            List<OrderSimpleDto> orderList = findOrderListAsync(httpServletRequest).get();
            List<CouponResponseDto> couponList = findCouponListAsync(httpServletRequest).get();
            return MyPageResponseDto.of(user, orderList, couponList);
        } catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException(e);
		}
    }

    private CompletableFuture<UserResponseDto> findUserAsync(HttpServletRequest httpServletRequest) {
        Long userId = Long.valueOf(httpServletRequest.getHeader("X-User-Id"));

        return CompletableFuture
            .supplyAsync(() -> authServiceClient.findUser(userId))
            .orTimeout(TIMEOUT, TimeUnit.SECONDS)
            .exceptionally(ex -> {
                log.error("Error fetching user: {}", ex.getMessage());
                throw new CustomException(ErrorCode.USER_SERVICE_UNAVAILABLE);
            });
    }

    private CompletableFuture<List<OrderSimpleDto>> findOrderListAsync(HttpServletRequest httpServletRequest) {
        Long userId = Long.valueOf(httpServletRequest.getHeader("X-User-Id"));

        return CompletableFuture
            .supplyAsync(() -> orderServiceClient.findOrderList(userId))
            .orTimeout(TIMEOUT, TimeUnit.SECONDS)
            .exceptionally(ex -> {
                log.error("Error fetching order list: {}", ex.getMessage());
                return Collections.emptyList();
            });
    }

    private CompletableFuture<List<CouponResponseDto>> findCouponListAsync(HttpServletRequest httpServletRequest) {
        Long userId = Long.valueOf(httpServletRequest.getHeader("X-User-Id"));

        return CompletableFuture
            .supplyAsync(() -> couponServiceClient.findCouponList(userId))
            .orTimeout(TIMEOUT, TimeUnit.SECONDS)
            .exceptionally(ex -> {
                log.error("Error fetching coupon list: {}", ex.getMessage());
                return Collections.emptyList();
            });
    }
}
