//package kr.hhplus.be.server.domain.payment.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.jayway.jsonpath.JsonPath;
//import kr.hhplus.be.server.regacy.domain.balance.service.MockBalanceService;
//import kr.hhplus.be.server.regacy.domain.payment.dto.request.ProcessPaymentRequest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
// 
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class PaymentControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private MockBalanceService balanceService;
//
//    @BeforeEach
//    void setup() {
//        // 모든 테스트 유저 등록
//        balanceService.resetBalance(1L, new BigDecimal("10000"));
//        balanceService.resetBalance(2L, new BigDecimal("10000"));
//        balanceService.resetBalance(3L, new BigDecimal("10000"));
//        balanceService.resetBalance(4L, new BigDecimal("10000"));
//    }
//
//    @Nested
//    @DisplayName("결제 요청 API")
//    class ProcessPaymentTest {
//
//        @Test
//        @DisplayName("결제 요청 성공")
//        void processPayment_success() throws Exception {
//            ProcessPaymentRequest request = new ProcessPaymentRequest(1001L, 1L, new BigDecimal("1000"));
//
//            mockMvc.perform(post("/api/v1/payments")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"))
//                    .andExpect(jsonPath("$.data.paymentId").exists())
//                    .andExpect(jsonPath("$.data.status").value("SUCCESS"));
//        }
//    }
//
//    @Nested
//    @DisplayName("결제 확인 API")
//    class ConfirmPaymentTest {
//
//        @Test
//        @DisplayName("결제 확인 성공")
//        void confirmPayment_success() throws Exception {
//            ProcessPaymentRequest request = new ProcessPaymentRequest(1002L, 2L, new BigDecimal("2000"));
//
//            String response = mockMvc.perform(post("/api/v1/payments")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isOk())
//                    .andReturn().getResponse().getContentAsString();
//
//            String pgTransactionId = JsonPath.read(response, "$.data.pgTransactionId");
//
//            mockMvc.perform(post("/api/v1/payments/confirm")
//                            .param("pgTransactionId", pgTransactionId))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"))
//                    .andExpect(jsonPath("$.data.pgTransactionId").value(pgTransactionId));
//        }
//    }
//
//    @Nested
//    @DisplayName("결제 상태 조회 API")
//    class GetPaymentStatusTest {
//
//        @Test
//        @DisplayName("결제 ID로 상태 조회 성공")
//        void getPaymentStatus_success() throws Exception {
//            ProcessPaymentRequest request = new ProcessPaymentRequest(1003L, 3L, new BigDecimal("3000"));
//
//            String response = mockMvc.perform(post("/api/v1/payments")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isOk())
//                    .andReturn().getResponse().getContentAsString();
//
//            Long paymentId = ((Number) JsonPath.read(response, "$.data.paymentId")).longValue();
//
//            mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"))
//                    .andExpect(jsonPath("$.data.paymentId").value(paymentId));
//        }
//    }
//
//    @Nested
//    @DisplayName("주문 ID로 결제 조회 API")
//    class GetPaymentByOrderIdTest {
//
//        @Test
//        @DisplayName("주문 ID로 결제 조회 성공")
//        void getPaymentByOrderId_success() throws Exception {
//            ProcessPaymentRequest request = new ProcessPaymentRequest(1004L, 4L, new BigDecimal("4000"));
//
//            String response = mockMvc.perform(post("/api/v1/payments")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isOk())
//                    .andReturn().getResponse().getContentAsString();
//
//            Long orderId = ((Number) JsonPath.read(response, "$.data.orderId")).longValue();
//
//            mockMvc.perform(get("/api/v1/payments/order/{orderId}", orderId))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"))
//                    .andExpect(jsonPath("$.data.orderId").value(orderId));
//        }
//    }
//}
