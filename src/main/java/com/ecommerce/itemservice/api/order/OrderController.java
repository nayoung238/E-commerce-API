package com.ecommerce.itemservice.api.order;

import com.ecommerce.itemservice.domain.item.service.ItemService;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "OrderController", description = "주문과 관련된 상품 변경 데이터 제공")
@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
@Slf4j
public class OrderController {

    private final ItemService itemService;

    @Operation(summary = "주문에 대한 상품 변경 성공 여부 제공", description = "주문에 대한 상품 변경 작업 결과를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문에 대한 상품 변경 성공 여부", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(responseCode = "400", description = "조건을 위반한 데이터 입력 시 주문 실패", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(responseCode = "404", description = "주문에 대한 상품 변경 작업이 없는 경우", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생", content = @Content(schema = @Schema(implementation = Exception.class)))
    })
    @GetMapping("/processing-result/{orderEventKey}")
    public ResponseEntity<?> findOrderProcessingResult(@PathVariable String orderEventKey) {
        OrderStatus orderItemStatus = itemService.findOrderProcessingStatus(orderEventKey);
        return ResponseEntity.status(HttpStatus.OK).body(orderItemStatus);
    }

    @Operation(summary = "상태 검사")
    @GetMapping("/health-check")
    public ResponseEntity<?> healthCheck() {
        log.info("health check...");
        return ResponseEntity.ok("health check...");
    }
}
