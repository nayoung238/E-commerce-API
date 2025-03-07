package com.ecommerce.couponservice.couponlog.service;

import com.ecommerce.couponservice.common.exception.CustomException;
import com.ecommerce.couponservice.common.exception.ErrorCode;
import com.ecommerce.couponservice.coupon.entity.Coupon;
import com.ecommerce.couponservice.coupon.repository.CouponRepository;
import com.ecommerce.couponservice.couponlog.dto.CouponLogResponse;
import com.ecommerce.couponservice.couponlog.entity.CouponLog;
import com.ecommerce.couponservice.couponlog.repository.CouponLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponLogService {

	private final CouponRepository couponRepository;
	private final CouponLogRepository couponLogRepository;

	@Transactional
	public void saveCouponLog(Long couponId, Long userId) {
		Coupon coupon = couponRepository.findById(couponId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COUPON));

		CouponLog couponLog = CouponLog.builder()
			.coupon(coupon)
			.userId(userId)
			.build();

		couponLogRepository.save(couponLog);
		couponRepository.save(coupon);
	}

	public List<CouponLogResponse> findAllCouponLogs(Long userId) {
		return couponLogRepository.findAllByUserId(userId)
			.stream()
			.map(CouponLogResponse::of)
			.toList();
	}
}
