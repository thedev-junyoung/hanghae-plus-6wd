//package kr.hhplus.be.server.domain.order.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.jayway.jsonpath.JsonPath;
//import kr.hhplus.be.server.regacy.domain.order.dto.request.CreateOrderRequest;
//import kr.hhplus.be.server.regacy.domain.order.dto.request.CreateOrderRequest.OrderItemRequest;
//import kr.hhplus.be.server.regacy.domain.order.dto.request.CreateOrderRequest.ShippingAddress;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//
//import static org.hamcrest.Matchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class OrderControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Nested
//    @DisplayName("주문 생성 API 테스트")
//    class CreateOrderTest {
//
//        @Test
//        @DisplayName("주문 생성 성공")
//        void createOrder_success() throws Exception {
//            CreateOrderRequest request = CreateOrderRequest.builder()
//                    .userId(1L)
//                    .items(List.of(
//                            OrderItemRequest.builder().productId(1001L).quantity(1).size(270).build(),
//                            OrderItemRequest.builder().productId(1002L).quantity(2).size(265).build()
//                    ))
//                    .shippingAddress(ShippingAddress.builder()
//                            .receiverName("홍길동")
//                            .phoneNumber("010-1234-5678")
//                            .zipCode("06164")
//                            .address1("서울 강남구")
//                            .address2("101동 202호")
//                            .memo("부재 시 문 앞에 두세요")
//                            .build())
//                    .build();
//
//            mockMvc.perform(post("/api/v1/orders")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"))
//                    .andExpect(jsonPath("$.data.orderId").exists())
//                    .andExpect(jsonPath("$.data.items", hasSize(2)));
//        }
//
//        @Test
//        @DisplayName("상품 사이즈 누락으로 실패")
//        void createOrder_missingSize() throws Exception {
//            CreateOrderRequest request = CreateOrderRequest.builder()
//                    .userId(1L)
//                    .items(List.of(
//                            OrderItemRequest.builder().productId(1001L).quantity(1).size(null).build()
//                    ))
//                    .build();
//
//            mockMvc.perform(post("/api/v1/orders")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andDo(print())
//                    .andExpect(status().isBadRequest())
//                    .andExpect(jsonPath("$.status").value("error"))
//                    .andExpect(jsonPath("$.message").isNotEmpty());
//        }
//    }
//
//    @Nested
//    @DisplayName("주문 상세 조회 API 테스트")
//    class GetOrderTest {
//
//        @Test
//        @DisplayName("주문 조회 성공")
//        void getOrder_success() throws Exception {
//            mockMvc.perform(get("/api/v1/orders/{orderId}", 1000L))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"))
//                    .andExpect(jsonPath("$.data.orderId").value(1000));
//        }
//    }
//
//    @Nested
//    @DisplayName("주문 취소 API 테스트")
//    class CancelOrderTest {
//
//        @Test
//        @DisplayName("주문 취소 성공")
//        void cancelOrder_success() throws Exception {
//
//            CreateOrderRequest request = CreateOrderRequest.builder()
//                    .userId(2L)
//                    .items(List.of(OrderItemRequest.builder().productId(1010L).quantity(1).size(270).build()))
//                    .build();
//
//            String response = mockMvc.perform(post("/api/v1/orders")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isOk())
//                    .andReturn().getResponse().getContentAsString();
//
//            Long orderId = ((Number) JsonPath.read(response, "$.data.orderId")).longValue();
//            // 취소 요청
//            mockMvc.perform(delete("/api/v1/orders/{orderId}", orderId)
//                            .param("userId", "2"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"))
//                    .andExpect(jsonPath("$.data.status").value("CANCELLED"));
//        }
//
//        @Test
//        @DisplayName("다른 사용자가 주문 취소 시 실패")
//        void cancelOrder_unauthorized() throws Exception {
//            // given: 주문 생성
//            CreateOrderRequest request = CreateOrderRequest.builder()
//                    .userId(1L)
//                    .items(List.of(OrderItemRequest.builder().productId(1010L).quantity(1).size(270).build()))
//                    .build();
//
//            String response = mockMvc.perform(post("/api/v1/orders")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isOk())
//                    .andReturn()
//                    .getResponse()
//                    .getContentAsString();
//
//            Long orderId = ((Number) JsonPath.read(response, "$.data.orderId")).longValue();
//
//
//            // when & then: 잘못된 사용자 ID로 취소 요청
//            mockMvc.perform(delete("/api/v1/orders/{orderId}", orderId)
//                            .param("userId", "9999"))
//                    .andDo(print())
//                    .andExpect(status().isForbidden())  // 비즈니스 예외 처리에서 403으로 매핑했을 경우
//                    .andExpect(jsonPath("$.status").value("error"))
//                    .andExpect(jsonPath("$.message").value("인증되지 않은 접근입니다."));        }
//    }
//}
