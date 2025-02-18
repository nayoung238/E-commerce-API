package com.ecommerce.couponservice.domain.coupon.service;

import com.ecommerce.couponservice.domain.coupon.Coupon;
import com.ecommerce.couponservice.domain.coupon.dto.CouponRegisterRequestDto;
import com.ecommerce.couponservice.domain.coupon.repo.CouponRepository;
import com.ecommerce.couponservice.internalevent.service.InternalEventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class CouponManagementServiceUnitTest {

    @InjectMocks
    private CouponManagementService couponManagementService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private InternalEventService internalEventService;

    @DisplayName("쿠폰 발급에 성공하면 결과 이벤트를 생성한다.")
    @Test
    void shouldPublishEventWhenCouponIssuedSuccessfully () {
        // setup(data)
        long couponId = 1L;
        long accountId = 1L;
        Coupon coupon = createTestCoupon();

        // setup(expectations)
        when(couponRepository.findById(couponId)).thenReturn(Optional.ofNullable(coupon));
        doNothing().when(internalEventService).publishInternalEvent(any());

        // exercise
        couponManagementService.issueCouponInDatabase(couponId, accountId);

        // verify
        verify(internalEventService, times(1))
                .publishInternalEvent(any());
    }

    private Coupon createTestCoupon() {
        CouponRegisterRequestDto request = CouponRegisterRequestDto.of("coupon-name", 1L, BigDecimal.valueOf(20.0), 10L);
        return Coupon.of(request);
    }
}
