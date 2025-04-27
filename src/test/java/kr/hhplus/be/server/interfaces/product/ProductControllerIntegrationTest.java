package kr.hhplus.be.server.interfaces.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("상품 목록 조회 성공")
    void getProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products?page=0&size=5&sort=name,asc")
                        .header("X-USER-ID", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.products").isArray());
    }

    @Test
    @DisplayName("상품 상세 조회 성공")
    void getProductDetail() throws Exception {
        mockMvc.perform(get("/api/v1/products/{productId}?size=260", 1)
                        .header("X-USER-ID", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.product").exists());
    }

    @Test
    @DisplayName("인기 상품 조회 성공")
    void getPopularProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products/popular?days=3&limit=5")
                        .header("X-USER-ID", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray());
    }
}
