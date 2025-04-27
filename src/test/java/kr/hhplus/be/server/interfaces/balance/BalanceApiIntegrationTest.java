package kr.hhplus.be.server.interfaces.balance;

import kr.hhplus.be.server.common.dto.CustomApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class BalanceApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final Long USER_ID = 100L;

    @Test
    @DisplayName("잔액 충전 후 조회 통합 테스트")
    void balanceChargeAndGetIntegrationTest() {
        // given
        String requestId = UUID.randomUUID().toString();
        BalanceRequest request = new BalanceRequest(USER_ID, 10_000L, requestId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-USER-ID", String.valueOf(USER_ID));

        HttpEntity<BalanceRequest> entity = new HttpEntity<>(request, headers);

        // when - 충전
        ResponseEntity<CustomApiResponse> chargeResponse = restTemplate.postForEntity(
                "/api/v1/balances/charge", entity, CustomApiResponse.class);

        // then
        assertThat(chargeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // when - 조회
        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        ResponseEntity<CustomApiResponse> getResponse = restTemplate.exchange(
                "/api/v1/balances/" + USER_ID,
                HttpMethod.GET,
                getEntity,
                CustomApiResponse.class
        );

        // then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
