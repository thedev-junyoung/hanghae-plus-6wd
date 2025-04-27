package kr.hhplus.be.server.interfaces.balance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BalanceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    Long userId = 100L;

    @Test
    @DisplayName("잔액 충전 - 성공")
    void charge_success() throws Exception {
        BalanceRequest request = new BalanceRequest(userId, 10_000L, UUID.randomUUID().toString());

        mockMvc.perform(post("/api/v1/balances/charge")
                        .header("X-USER-ID", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId));
    }

    @Test
    @DisplayName("잔액 조회 - 성공")
    void getBalance_success() throws Exception {
        mockMvc.perform(get("/api/v1/balances/" + userId)
                        .header("X-USER-ID", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId));
    }
}
