package com.ecommerce.itemservice.item.api;

import com.ecommerce.itemservice.item.dto.ItemRegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("local")
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("상품 생성 테스트")
    void 상품_생성_테스트() throws Exception {
        ItemRegisterRequest request = ItemRegisterRequest.builder()
                .name("apple")
                .stock(10L)
                .build();

        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders
                .post("/items/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
        );

        result.andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("item-register",
                        requestFields(
                                fieldWithPath("name").description("상품 이름"),
                                fieldWithPath("stock").description("상품 재고")
                        ),
                        responseFields(
                                fieldWithPath("id").description("상품 ID"),
                                fieldWithPath("name").description("상품 이름"),
                                fieldWithPath("stock").description("상품 재고")
                        ))
                );
    }
}