package kr.hhplus.be.server.interfaces.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderItem;
import kr.hhplus.be.server.domain.order.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CouponControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long USER_ID = 100L;

    @Autowired
    private CouponRepository couponRepository;


    @Test
    @DisplayName("한정 쿠폰 발급 - 성공")
    void limitedIssueCoupon_success() throws Exception {
        // ✅ Step 1: 고유한 쿠폰 코드 생성
        String uniqueCode = "TEST-" + System.currentTimeMillis();

        // ✅ Step 2: 테스트용 쿠폰을 먼저 DB에 저장
        Coupon coupon = Coupon.createLimitedFixed(
                uniqueCode,
                1000,               // discountAmount
                5,                  // totalQuantity
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(10)
        );
        couponRepository.save(coupon);
        CouponRequest request = new CouponRequest(100L, uniqueCode);

        mockMvc.perform(post("/api/v1/coupons/limited-issue")
                        .header("X-USER-ID", String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(100L))
                .andExpect(jsonPath("$.data.couponType").value("FIXED")); // 예상 타입
    }
}
