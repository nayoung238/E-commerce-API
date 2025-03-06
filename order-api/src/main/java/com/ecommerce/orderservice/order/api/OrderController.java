package com.ecommerce.orderservice.order.api;

import com.ecommerce.orderservice.auth.entity.UserPrincipal;
import com.ecommerce.orderservice.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.order.dto.OrderSimpleDto;
import com.ecommerce.orderservice.order.service.OrderCreationService;
import com.ecommerce.orderservice.order.service.OrderQueryService;
import com.ecommerce.orderservice.order.dto.OrderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "OrderController", description = "주문 생성 및 주문 목록 제공")
@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderCreationService orderCreationService;
    private final OrderQueryService orderQueryService;

    @Operation(summary = "주문 생성", description = "주문 생성 시 주문 상태는 대기 (최종 상태가 확정되면 별도 알림)")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "주문 생성 성공", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(responseCode = "400", description = "조건을 위반한 데이터 입력시 주문 실패", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생", content = @Content(schema = @Schema(implementation = Exception.class)))
    })
    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid OrderRequestDto orderRequestDto) {
        OrderDto response = orderCreationService.create(orderRequestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @Operation(summary = "주문 목록 조회", description = "Cursor-based Pagination 가능, cursorOrderId 생략 시 최신 주문 목록 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 목록 조회 완료", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(responseCode = "400", description = "조건을 위반한 데이터 입력시 주문 실패", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생", content = @Content(schema = @Schema(implementation = Exception.class)))
    })
    @GetMapping(value = {"/", "/cursor/{cursorOrderId}", })
    public ResponseEntity<?> getOrders(@PathVariable(required = false) @Valid @Positive(message = "주문 커서 아이디는 1 이상이어야 합니다.") Long cursorOrderId,
                                       @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<OrderSimpleDto> response = orderQueryService.findOrdersByUserIdAndOrderId(userPrincipal.getId(), cursorOrderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "주문 단건 조회", description = "Order Id 주문 정보 리턴")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "최신 주문 데이터 조회 완료", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(responseCode = "400", description = "조건을 위반한 데이터 입력시 주문 실패", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(responseCode = "404", description = "주문이 존재하지 않는 경우", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생", content = @Content(schema = @Schema(implementation = Exception.class)))
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable @Valid @Positive(message = "Order Id는 1 이상이어야 합니다.") Long orderId,
                                          @AuthenticationPrincipal UserPrincipal userPrincipal) {
        OrderDto response = orderQueryService.findOrderById(orderId, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/health-check")
    public ResponseEntity<?> healthCheck() {
        log.info("health check...");
        return ResponseEntity.ok("health check...");
    }
}
