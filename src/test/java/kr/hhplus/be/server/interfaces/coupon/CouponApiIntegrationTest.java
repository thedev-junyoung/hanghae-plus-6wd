package kr.hhplus.be.server.interfaces.coupon;

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


import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class CouponApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("한정 수량 쿠폰 발급 - 성공")
    void limitedIssueCoupon_success() throws Exception {
        // given
        Long userId = ThreadLocalRandom.current().nextLong(10_000_000, 99_999_999);

        String couponCode = "TESTONLY1000";  // <-- 존재하는 쿠폰 코드여야 함
        CouponRequest request = new CouponRequest(userId, couponCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-USER-ID", String.valueOf(userId));
        HttpEntity<CouponRequest> entity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/coupons/limited-issue", entity, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 응답 JSON 파싱
        CustomApiResponse<CouponResponse> parsed = objectMapper.readValue(
                response.getBody(),
                new TypeReference<CustomApiResponse<CouponResponse>>() {}
        );

        // assertions
        assertThat(parsed.getStatus()).isEqualTo("success");
        assertThat(parsed.getData()).isNotNull();
        assertThat(parsed.getData().getUserId()).isEqualTo(userId);
        assertThat(parsed.getData().getCouponType()).isNotBlank();
    }
}
