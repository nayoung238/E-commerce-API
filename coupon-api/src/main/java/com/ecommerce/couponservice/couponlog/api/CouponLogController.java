package com.ecommerce.couponservice.couponlog.api;

import com.ecommerce.couponservice.couponlog.dto.CouponLogResponseDto;
import com.ecommerce.couponservice.couponlog.service.CouponLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Coupon Log", description = "쿠폰 발급 기록")
@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons/log")
public class CouponLogController {

	private final CouponLogService couponLogService;

	@Operation(summary = "쿠폰 목록 조회", description = "HTTP 헤더에 X-Account-Id 추가해주세요")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "쿠푼 목록 조회 성공", content = @Content(schema = @Schema(implementation = List.class))),
	})
	@GetMapping
	public ResponseEntity<?> findAllCouponLogs(HttpServletRequest httpServletRequest) {
		Long accountId = Long.valueOf(httpServletRequest.getHeader("X-Account-Id"));
		List<CouponLogResponseDto> response = couponLogService.findAllCouponLogs(accountId);
		return ResponseEntity.ok(response);
	}
}
