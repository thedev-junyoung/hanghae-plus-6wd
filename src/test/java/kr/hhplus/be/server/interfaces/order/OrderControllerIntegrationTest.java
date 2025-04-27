package kr.hhplus.be.server.interfaces.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long USER_ID = 100L;

    @Test
    @DisplayName("주문 생성 - 성공")
    void createOrder_success() throws Exception {
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest(1L, 1, 270);
        OrderRequest request = new OrderRequest(USER_ID, List.of(item), null);

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-USER-ID", String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.userId").value(USER_ID))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.status").value("CREATED"));
    }

    @Test
    @DisplayName("주문 생성 실패 - 빈 주문 항목 리스트")
    void createOrder_emptyItems_shouldFail() throws Exception {
        OrderRequest request = new OrderRequest(USER_ID, List.of(), null);

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-USER-ID", String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 생성 실패 - 상품 ID 누락")
    void createOrder_missingProductId_shouldFail() throws Exception {
        String invalidJson = """
                {
                    "userId": 100,
                    "items": [{"quantity": 1, "size": 270}],
                    "couponCode": null
                }
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-USER-ID", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 생성 실패 - 존재하지 않는 상품 ID")
    void createOrder_invalidProduct_shouldFail() throws Exception {
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest(999999L, 1, 270); // 존재하지 않는 ID
        OrderRequest request = new OrderRequest(USER_ID, List.of(item), null);

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-USER-ID", String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("주문 생성 실패 - 재고 부족")
    void createOrder_insufficientStock_shouldFail() throws Exception {
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest(1L, 9999, 270); // 수량 과도하게 요청
        OrderRequest request = new OrderRequest(USER_ID, List.of(item), null);

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-USER-ID", String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }
}
