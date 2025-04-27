package kr.hhplus.be.server.interfaces.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderItem;
import kr.hhplus.be.server.domain.order.OrderRepository;
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
class PaymentControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private static final Long USER_ID = 100L;

    @Autowired
    private OrderRepository orderRepository;

    private String setupOrder() {
        // 단일 상품 주문 아이템 구성
        OrderItem item = OrderItem.of(1L, 1, 270, Money.wons(1000L));
        Order order = Order.create(100L, List.of(item), Money.wons(1000L));
        return orderRepository.save(order).getId();
    }
    @Test
    @DisplayName("결제 성공")
    void requestPayment_success() throws Exception {
        String orderId = setupOrder();
        PaymentRequest request = new PaymentRequest(orderId, USER_ID, "BALANCE", 1000L);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", String.valueOf(USER_ID))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.paymentId").exists())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("잔액 부족 - 결제 실패")
    void requestPayment_insufficientBalance() throws Exception {
        String orderId = setupOrder();
        PaymentRequest request = new PaymentRequest(orderId, USER_ID, "BALANCE", 99999999L);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", String.valueOf(USER_ID))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("잔액이 부족합니다."));
    }
}
