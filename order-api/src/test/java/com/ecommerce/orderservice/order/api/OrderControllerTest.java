package com.ecommerce.orderservice.order.api;

import com.ecommerce.orderservice.IntegrationTestSupport;
import com.ecommerce.orderservice.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import com.ecommerce.orderservice.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderControllerTest extends IntegrationTestSupport {

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@AfterEach
	void afterEach() {
		orderRepository.deleteAll();
	}

	@DisplayName("[주문 생성 실패 테스트] 주문 생성 시 userId 필수")
	@Test
	public void order_creation_failed_test_when_userId_null () throws Exception {
		// given
		final Long userId = null;
		final List<Long> itemIds = List.of(1L, 3L);
		OrderRequestDto orderRequestDto = getOrderRequestDto(userId, itemIds);

		// when & then
		mockMvc.perform(
				post("/orders")
					.content(objectMapper.writeValueAsString(orderRequestDto))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isBadRequest())
			.andExpect(content().string(containsString("사용자 아이디는 필수입니다.")))
			.andDo(print());
	}

	@DisplayName("[주문 생성 실패 테스트] 주문 생성 시 userId 필수")
	@Test
	public void order_creation_failed_test_when_order_null () throws Exception {
		// given
		final Long userId = 10L;
		OrderRequestDto orderRequestDto = OrderRequestDto.of(userId, null);

		// when & then
		mockMvc.perform(
				post("/orders")
					.content(objectMapper.writeValueAsString(orderRequestDto))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isBadRequest())
			.andExpect(content().string(containsString("주문 아이템은 필수입니다.")))
			.andDo(print());
	}

	@DisplayName("[주문 상태 확인 테스트] 주문 생성 시 주문 상태는 PROCESSING")
	@Test
	public void order_creation_succeed_test() throws Exception {
		// given
		final Long userId = 10L;
		final List<Long> itemIds = List.of(1L, 3L);
		OrderRequestDto orderRequestDto = getOrderRequestDto(userId, itemIds);

		// when & then
		mockMvc.perform(
			post("/orders")
				.content(objectMapper.writeValueAsString(orderRequestDto))
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isAccepted())
			.andExpect(jsonPath("$.userId").value(userId))
			.andExpect(jsonPath("$.orderProcessingStatus").value(OrderProcessingStatus.PROCESSING.name()))
			.andExpect(jsonPath("$.orderItemDtos").isArray())
			.andExpect(jsonPath("$.orderItemDtos.length()").value(itemIds.size()))
			.andExpect(jsonPath("$.orderItemDtos[*].orderProcessingStatus")
				.value(Matchers.everyItem(Matchers.is(OrderProcessingStatus.PROCESSING.name()))))
			.andDo(print());
	}
}