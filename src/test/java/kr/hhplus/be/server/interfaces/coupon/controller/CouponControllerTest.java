/*
package kr.hhplus.be.server.domain.coupon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.regacy.domain.coupon.dto.request.CreateCouponRequest;
import kr.hhplus.be.server.regacy.domain.coupon.dto.request.IssueCouponRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("쿠폰 생성 API 테스트")
    class CreateCouponTest {

        @Test
        @DisplayName("쿠폰 생성 성공")
        void createCoupon_success() throws Exception {
            // given
            CreateCouponRequest request = CreateCouponRequest.builder()
                    .code("SUMMER2025")
                    .type("PERCENTAGE")
                    .discountRate(10)
                    .totalQuantity(100)
                    .validFrom(LocalDateTime.now())
                    .validUntil(LocalDateTime.now().plusDays(30))
                    .build();

            // when & then
            mockMvc.perform(post("/api/v1/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.couponType").value("PERCENTAGE"))
                    .andExpect(jsonPath("$.data.discountRate").value(10))
                    .andExpect(jsonPath("$.data.issuedAt").isNotEmpty());
        }

        @Test
        @DisplayName("중복된 쿠폰 코드로 생성 시 실패")
        void createCoupon_duplicateCode() throws Exception {
            // given
            CreateCouponRequest request1 = CreateCouponRequest.builder()
                    .code("DUPLICATE_TEST")
                    .type("PERCENTAGE")
                    .discountRate(10)
                    .totalQuantity(100)
                    .validFrom(LocalDateTime.now())
                    .validUntil(LocalDateTime.now().plusDays(30))
                    .build();

            CreateCouponRequest request2 = CreateCouponRequest.builder()
                    .code("DUPLICATE_TEST") // 같은 코드
                    .type("PERCENTAGE")
                    .discountRate(20)
                    .totalQuantity(50)
                    .validFrom(LocalDateTime.now())
                    .validUntil(LocalDateTime.now().plusDays(30))
                    .build();

            // when & then
            // 첫 번째 요청 (성공)
            mockMvc.perform(post("/api/v1/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isOk());

            // 두 번째 요청 (실패)
            mockMvc.perform(post("/api/v1/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        @DisplayName("잘못된 유효기간으로 생성 시 실패")
        void createCoupon_invalidDateRange() throws Exception {
            // given
            CreateCouponRequest request = CreateCouponRequest.builder()
                    .code("INVALID_DATE_TEST")
                    .type("PERCENTAGE")
                    .discountRate(10)
                    .totalQuantity(100)
                    .validFrom(LocalDateTime.now().plusDays(30)) // 시작일이 종료일보다 늦음
                    .validUntil(LocalDateTime.now())
                    .build();

            // when & then
            mockMvc.perform(post("/api/v1/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("쿠폰 발급 API 테스트")
    class IssueCouponTest {

        @Test
        @DisplayName("쿠폰 발급 성공")
        void issueCoupon_success() throws Exception {
            // given
            // 먼저 쿠폰 생성
            CreateCouponRequest createRequest = CreateCouponRequest.builder()
                    .code("ISSUE_TEST")
                    .type("FIXED")
                    .discountRate(5000)
                    .totalQuantity(10)
                    .validFrom(LocalDateTime.now())
                    .validUntil(LocalDateTime.now().plusDays(30))
                    .build();

            mockMvc.perform(post("/api/v1/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk());

            // 쿠폰 발급 요청
            IssueCouponRequest issueRequest = IssueCouponRequest.builder()
                    .userId(1L)
                    .couponCode("ISSUE_TEST")
                    .build();

            // when & then
            mockMvc.perform(post("/api/v1/coupons/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(issueRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.userId").value(1))
                    .andExpect(jsonPath("$.data.couponType").value("FIXED"))
                    .andExpect(jsonPath("$.data.discountRate").value(5000))
                    .andExpect(jsonPath("$.data.userCouponId").isNotEmpty());
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰 코드로 발급 시 실패")
        void issueCoupon_notFoundCoupon() throws Exception {
            // given
            IssueCouponRequest request = IssueCouponRequest.builder()
                    .userId(1L)
                    .couponCode("NOT_EXISTING_CODE")
                    .build();

            // when & then
            mockMvc.perform(post("/api/v1/coupons/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("쿠폰 목록 조회 API 테스트")
    class GetUserCouponsTest {

        @Test
        @DisplayName("사용자 쿠폰 목록 조회 성공")
        void getUserCoupons_success() throws Exception {
            // given
            Long userId = 1L;

            // 먼저 쿠폰을 생성하고 발급
            CreateCouponRequest createRequest = CreateCouponRequest.builder()
                    .code("LIST_TEST")
                    .type("PERCENTAGE")
                    .discountRate(15)
                    .totalQuantity(10)
                    .validFrom(LocalDateTime.now())
                    .validUntil(LocalDateTime.now().plusDays(30))
                    .build();

            mockMvc.perform(post("/api/v1/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk());

            IssueCouponRequest issueRequest = IssueCouponRequest.builder()
                    .userId(userId)
                    .couponCode("LIST_TEST")
                    .build();

            mockMvc.perform(post("/api/v1/coupons/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(issueRequest)))
                    .andExpect(status().isOk());

            // when & then
            mockMvc.perform(get("/api/v1/coupons/user/{userId}", userId)
                            .param("status", "ALL"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.coupons", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.data.coupons[0].userId").value(userId))
                    .andExpect(jsonPath("$.data.coupons[0].couponType").isNotEmpty())
                    .andExpect(jsonPath("$.data.coupons[0].discountRate").isNotEmpty());
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 쿠폰 목록 조회 시 실패")
        void getUserCoupons_userNotFound() throws Exception {
            // given
            Long nonExistingUserId = 999999L;

            // when & then
            mockMvc.perform(get("/api/v1/coupons/user/{userId}", nonExistingUserId)
                            .param("status", "ALL"))
                    .andDo(print())
                    .andExpect(status().isNotFound())  // 여기서 404 응답이 반환되어야 함
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }


        @Test
        @DisplayName("상태별 필터링 조회 테스트 - UNUSED")
        void getUserCoupons_filterByStatusUnused() throws Exception {
            // given
            Long userId = 1L;

            // when & then
            mockMvc.perform(get("/api/v1/coupons/user/{userId}", userId)
                            .param("status", "UNUSED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"));
        }
    }

    @Nested
    @DisplayName("전체 쿠폰 흐름 E2E 테스트")
    class CouponE2ETest {

        @Test
        @DisplayName("쿠폰 생성 → 발급 → 조회 전체 플로우 테스트")
        void couponFullFlowTest() throws Exception {
            // given
            String couponCode = "E2E_TEST_" + System.currentTimeMillis();
            Long userId = 1L;

            // Step 1: 쿠폰 생성
            CreateCouponRequest createRequest = CreateCouponRequest.builder()
                    .code(couponCode)
                    .type("PERCENTAGE")
                    .discountRate(20)
                    .totalQuantity(5)
                    .validFrom(LocalDateTime.now())
                    .validUntil(LocalDateTime.now().plusDays(30))
                    .build();

            mockMvc.perform(post("/api/v1/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.couponType").value("PERCENTAGE"))
                    .andExpect(jsonPath("$.data.discountRate").value(20));

            // Step 2: 쿠폰 발급
            IssueCouponRequest issueRequest = IssueCouponRequest.builder()
                    .userId(userId)
                    .couponCode(couponCode)
                    .build();

            mockMvc.perform(post("/api/v1/coupons/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(issueRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andExpect(jsonPath("$.data.couponType").value("PERCENTAGE"))
                    .andExpect(jsonPath("$.data.discountRate").value(20));

            // Step 3: 쿠폰 목록 조회 및 검증
            mockMvc.perform(get("/api/v1/coupons/user/{userId}", userId)
                            .param("status", "UNUSED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.coupons", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.data.coupons[?(@.discountRate==20)]").exists());
        }
    }
}*/
