package com.ecommerce.itemservice.item.api;

import com.ecommerce.itemservice.item.dto.request.ItemRegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("[상품 생성 성공 테스트] 상품 생성 시 상품 상세 반환")
    void item_creation_success_test () throws Exception {
        ItemRegisterRequest request = ItemRegisterRequest.builder()
                .name("apple")
                .stock(10L)
                .price(1000L)
                .build();

        mockMvc.perform(
                post("/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value(request.name()))
            .andExpect(jsonPath("$.stock").value(comparesEqualTo(request.stock().intValue())))
            .andExpect(jsonPath("$.price").value(request.price()))
            .andDo(print());
    }

    @Test
    @DisplayName("[상품 생성 실패 테스트] 상품 생성 시 아이템명 입력 필수")
    void item_creation_failed_test_when_name_null () throws Exception {
        ItemRegisterRequest request = ItemRegisterRequest.builder()
            .name(null)
            .stock(10L)
            .price(1000L)
            .build();

        mockMvc.perform(
                post("/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("아이템명은 필수입니다."))
            .andDo(print());
    }
}