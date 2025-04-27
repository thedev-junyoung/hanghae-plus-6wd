//package kr.hhplus.be.server.interfaces.balance.controller;
//
//import kr.hhplus.be.server.regacy.domain.balance.searvice.MockBalanceService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.web.servlet.MockMvc;
//
// 
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.http.MediaType.APPLICATION_JSON;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class BalanceControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private MockBalanceService mockBalanceService;
//
//    @BeforeEach
//    void setUp() {
//        // 각 테스트 실행 전 사용자 1의 잔액을 0으로 초기화
//        mockBalanceService.resetBalance(1L, BigDecimal.ZERO);
//    }
//
//    @Nested
//    @DisplayName("잔액 충전 API")
//    class ChargeBalanceTest {
//
//        @Test
//        @DisplayName("잔액 충전 - 성공")
//        void chargeBalance_success() throws Exception {
//            mockMvc.perform(post("/api/v1/balances/charge")
//                            .contentType(APPLICATION_JSON)
//                            .content("""
//                                {
//                                    "userId": 1,
//                                    "amount": 10000
//                                }
//                            """))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"))
//                    .andExpect(jsonPath("$.data.balance").value(10000));
//        }
//
//        @Test
//        @DisplayName("잔액 충전 - 음수 금액")
//        void chargeBalance_invalidAmount() throws Exception {
//            mockMvc.perform(post("/api/v1/balances/charge")
//                            .contentType(APPLICATION_JSON)
//                            .content("""
//                                {
//                                    "userId": 1,
//                                    "amount": -500
//                                }
//                            """))
//                    .andExpect(status().isBadRequest())
//                    .andExpect(jsonPath("$.status").value("error"));
//        }
//
//        @Test
//        @DisplayName("잔액 충전 - userId 누락")
//        void chargeBalance_missingUserId() throws Exception {
//            mockMvc.perform(post("/api/v1/balances/charge")
//                            .contentType(APPLICATION_JSON)
//                            .content("""
//                                {
//                                    "amount": 10000
//                                }
//                            """))
//                    .andExpect(status().isBadRequest())
//                    .andExpect(jsonPath("$.status").value("error"));
//        }
//
//        @Test
//        @DisplayName("잔액 충전 - amount 누락")
//        void chargeBalance_missingAmount() throws Exception {
//            mockMvc.perform(post("/api/v1/balances/charge")
//                            .contentType(APPLICATION_JSON)
//                            .content("""
//                                {
//                                    "userId": 1
//                                }
//                            """))
//                    .andExpect(status().isBadRequest())
//                    .andExpect(jsonPath("$.status").value("error"));
//        }
//
//        @Test
//        @DisplayName("잔액 충전 - 존재하지 않는 사용자")
//        void chargeBalance_userNotFound() throws Exception {
//            mockMvc.perform(post("/api/v1/balances/charge")
//                            .contentType(APPLICATION_JSON)
//                            .content("""
//                                {
//                                    "userId": 999999,
//                                    "amount": 1000
//                                }
//                            """))
//                    .andExpect(status().isNotFound())
//                    .andExpect(jsonPath("$.status").value("error"));
//        }
//    }
//
//    @Nested
//    @DisplayName("잔액 조회 API")
//    class GetBalanceTest {
//
//        @Test
//        @DisplayName("잔액 조회 - 성공")
//        void getBalance_success() throws Exception {
//            mockMvc.perform(get("/api/v1/balances/1"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"))
//                    .andExpect(jsonPath("$.data.balance").exists());
//        }
//
//        @Test
//        @DisplayName("잔액 조회 - 존재하지 않는 사용자")
//        void getBalance_userNotFound() throws Exception {
//            mockMvc.perform(get("/api/v1/balances/999999"))
//                    .andExpect(status().isNotFound())
//                    .andExpect(jsonPath("$.status").value("error"));
//        }
//    }
//}
