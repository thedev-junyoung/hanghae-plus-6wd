package kr.hhplus.be.server.interfaces.order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.dto.CustomApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class OrderApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("주문 생성 - 성공")
    void createOrder_success() throws Exception {
        // given
        Long userId = 100L;
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest(1L, 1, 270); // 재고 존재해야 함
        OrderRequest request = new OrderRequest(userId, List.of(item), null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-USER-ID", String.valueOf(userId)); // 필요하다면

        HttpEntity<OrderRequest> entity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/orders", entity, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CustomApiResponse<OrderResponse> parsed = objectMapper.readValue(
                response.getBody(),
                new TypeReference<CustomApiResponse<OrderResponse>>() {}
        );

        assertThat(parsed.getStatus()).isEqualTo("success");
        assertThat(parsed.getData().getUserId()).isEqualTo(userId);
        assertThat(parsed.getData().getItems()).isNotEmpty();
        assertThat(parsed.getData().getStatus()).isEqualTo("CREATED");
    }
}
