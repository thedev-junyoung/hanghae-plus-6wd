package kr.hhplus.be.server.interfaces.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.dto.CustomApiResponse;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderItem;
import kr.hhplus.be.server.domain.order.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentApiIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;


    private String setupOrder() {
        // 단일 상품 주문 아이템 구성
        OrderItem item = OrderItem.of(1L, 1, 270, Money.wons(1000L));
        Order order = Order.create(100L, List.of(item), Money.wons(1000L));
        return orderRepository.save(order).getId();
    }

    @Test
    @DisplayName("결제 요청 성공")
    void requestPayment_success() throws Exception {
        String randomOrderId = setupOrder();

        // 미리 주문 삽입하거나 테스트 환경에서 주문 생성 처리 필요
        PaymentRequest request = new PaymentRequest(randomOrderId, 100L, "BALANCE", 1000L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-USER-ID", "100");

        HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/payments", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CustomApiResponse<PaymentResponse> result = objectMapper.readValue(
                response.getBody(),
                new TypeReference<>() {}
        );

        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getData().getOrderId()).isEqualTo(randomOrderId);
        assertThat(result.getData().getStatus()).isEqualTo("SUCCESS");
    }
}
