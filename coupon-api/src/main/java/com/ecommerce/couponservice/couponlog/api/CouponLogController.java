package com.ecommerce.couponservice.couponlog.api;

import com.ecommerce.couponservice.auth.entity.UserPrincipal;
import com.ecommerce.couponservice.auth.jwt.JwtUtil;
import com.ecommerce.couponservice.common.exception.CustomException;
import com.ecommerce.couponservice.common.exception.ErrorCode;
import com.ecommerce.couponservice.couponlog.dto.CouponLogResponse;
import com.ecommerce.couponservice.couponlog.service.CouponLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Coupon Log", description = "쿠폰 발급 기록")
@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons/log")
public class CouponLogController {

	private final CouponLogService couponLogService;
	private final JwtUtil jwtUtil;

	@Operation(summary = "쿠폰 목록 조회")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "쿠푼 목록 조회 성공", content = @Content(schema = @Schema(implementation = List.class))),
		@ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(implementation = CustomException.class)))
	})
	@GetMapping
	public ResponseEntity<?> findAllCouponLogs(@RequestHeader("Authorization") String authorization,
											   @AuthenticationPrincipal UserPrincipal userPrincipal) {

		Long userId = jwtUtil.getUserIdFromRequestHeader(authorization);
		if (!userId.equals(userPrincipal.getId())) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}

		List<CouponLogResponse> response = couponLogService.findAllCouponLogs(userId);
		return ResponseEntity.ok(response);
	}
}
