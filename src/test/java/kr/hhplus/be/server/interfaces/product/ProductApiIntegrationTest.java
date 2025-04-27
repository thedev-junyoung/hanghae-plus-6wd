package kr.hhplus.be.server.interfaces.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductApiIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("상품 목록 조회 성공")
    void getProducts() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", "100");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/products?page=0&size=5",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("상품 상세 조회 성공")
    void getProductDetail() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", "100");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/products/1?size=260",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("인기 상품 조회 성공")
    void getPopularProducts() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", "100");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/products/popular?days=3&limit=5",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
} 