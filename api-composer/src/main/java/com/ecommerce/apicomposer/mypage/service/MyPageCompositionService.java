package com.ecommerce.apicomposer.mypage.service;

import com.ecommerce.apicomposer.common.exception.CustomException;
import com.ecommerce.apicomposer.common.exception.ErrorCode;
import com.ecommerce.apicomposer.common.service.AuthServiceClient;
import com.ecommerce.apicomposer.common.service.CouponServiceClient;
import com.ecommerce.apicomposer.common.service.OrderServiceClient;
import com.ecommerce.apicomposer.mypage.dto.response.CouponLogResponse;
import com.ecommerce.apicomposer.mypage.dto.response.MyPageResponse;
import com.ecommerce.apicomposer.mypage.dto.response.OrderSummaryResponse;
import com.ecommerce.apicomposer.mypage.dto.response.UserResponse;
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

    public MyPageResponse getMyPageDetails(HttpServletRequest httpServletRequest) {
        try {
            UserResponse user = findUserAsync(httpServletRequest).get();
            List<OrderSummaryResponse> orderList = findOrdersAsync(httpServletRequest).get();
            List<CouponLogResponse> couponList = findCouponsAsync(httpServletRequest).get();
            return MyPageResponse.of(user, orderList, couponList);
        } catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException(e);
		}
    }

    private CompletableFuture<UserResponse> findUserAsync(HttpServletRequest httpServletRequest) {
        Long userId = Long.valueOf(httpServletRequest.getHeader("X-User-Id"));

        return CompletableFuture
            .supplyAsync(() -> authServiceClient.findUser(userId))
            .orTimeout(TIMEOUT, TimeUnit.SECONDS)
            .exceptionally(ex -> {
                log.error("Error fetching user: {}", ex.getMessage());
                throw new CustomException(ErrorCode.USER_SERVICE_UNAVAILABLE);
            });
    }

    private CompletableFuture<List<OrderSummaryResponse>> findOrdersAsync(HttpServletRequest httpServletRequest) {
        Long userId = Long.valueOf(httpServletRequest.getHeader("X-User-Id"));

        return CompletableFuture
            .supplyAsync(() -> orderServiceClient.findOrderList(userId))
            .orTimeout(TIMEOUT, TimeUnit.SECONDS)
            .exceptionally(ex -> {
                log.error("Error fetching order list: {}", ex.getMessage());
                return Collections.emptyList();
            });
    }

    private CompletableFuture<List<CouponLogResponse>> findCouponsAsync(HttpServletRequest httpServletRequest) {
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
