//package kr.hhplus.be.server.domain.product.controller;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class ProductControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Nested
//    @DisplayName("상품 목록 조회 API")
//    class GetProductsTest {
//
//        @Test
//        @DisplayName("기본 페이징으로 상품 목록 조회 성공")
//        void getProducts_withDefaultPaging_success() throws Exception {
//            mockMvc.perform(get("/api/v1/products"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"))
//                    .andExpect(jsonPath("$.data.products").isArray())
//                    .andExpect(jsonPath("$.data.pagination").exists())
//                    .andExpect(jsonPath("$.data.pagination.totalElements").exists());
//        }
//
//        @Test
//        @DisplayName("사용자 지정 페이징으로 상품 목록 조회 성공")
//        void getProducts_withCustomPaging_success() throws Exception {
//            mockMvc.perform(get("/api/v1/products")
//                            .param("page", "0")
//                            .param("size", "10")
//                            .param("sort", "price,desc"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"))
//                    .andExpect(jsonPath("$.data.products").isArray())
//                    .andExpect(jsonPath("$.data.pagination").exists());
//        }
//
//        @Test
//        @DisplayName("유효하지 않은 페이지 파라미터로 요청시 기본값으로 처리")
//        void getProducts_withInvalidPageParam_usesDefaultValue() throws Exception {
//            mockMvc.perform(get("/api/v1/products")
//                            .param("page", "-1")
//                            .param("size", "10"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"));
//        }
//    }
//
//    @Nested
//    @DisplayName("상품 상세 조회 API")
//    class GetProductTest {
//
//        @Test
//        @DisplayName("상품 ID로 상세 조회 성공")
//        void getProduct_success() throws Exception {
//            // 기존 MockProductService에 있는 ID 사용
//            Long productId = 1L;
//
//            mockMvc.perform(get("/api/v1/products/{productId}", productId))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"))
//                    .andExpect(jsonPath("$.data.product").exists())
//                    .andExpect(jsonPath("$.data.product.id").exists())
//                    .andExpect(jsonPath("$.data.product.name").exists())
//                    .andExpect(jsonPath("$.data.product.price").exists());
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 ID로도 Mock 서비스는 데이터 반환")
//        void getProduct_withNonExistentId_returnsData() throws Exception {
//            // Mock 서비스는 존재하지 않는 ID에도 응답 생성
//            Long nonExistingId = 9999L;
//
//            mockMvc.perform(get("/api/v1/products/{productId}", nonExistingId))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"))
//                    .andExpect(jsonPath("$.data.product").exists())
//                    .andExpect(jsonPath("$.data.product.id").value(nonExistingId));
//        }
//    }
//
//    @Nested
//    @DisplayName("인기 상품 조회 API")
//    class GetPopularProductsTest {
//
//        @Test
//        @DisplayName("인기 상품 조회 성공")
//        void getPopularProducts_success() throws Exception {
//            mockMvc.perform(get("/api/v1/products/popular"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"))
//                    .andExpect(jsonPath("$.data.products").isArray())
//                    .andExpect(jsonPath("$.data.periodStart").exists())
//                    .andExpect(jsonPath("$.data.periodEnd").exists());
//        }
//    }
//}