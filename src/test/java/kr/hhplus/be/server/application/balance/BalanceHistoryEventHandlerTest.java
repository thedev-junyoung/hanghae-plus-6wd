package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.domain.balance.BalanceHistory;
import kr.hhplus.be.server.domain.balance.BalanceHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.mockito.Mockito.*;

class BalanceHistoryEventHandlerTest {

    private BalanceHistoryRepository balanceHistoryRepository;
    private BalanceHistoryEventHandler handler;

    @BeforeEach
    void setUp() {
        balanceHistoryRepository = mock(BalanceHistoryRepository.class);
        handler = new BalanceHistoryEventHandler(balanceHistoryRepository);
    }

    @Test
    @DisplayName("이벤트 수신 시 중복되지 않았다면 기록을 저장한다")
    void handle_shouldSave_ifNotDuplicate() {
        // given
        BalanceChargedEvent event = new BalanceChargedEvent(1L, 10000L, "테스트충전", "REQ-1234");
        when(balanceHistoryRepository.existsByRequestId("REQ-1234")).thenReturn(false);

        // when
        handler.handle(event);

        // then
        verify(balanceHistoryRepository).save(any(BalanceHistory.class));
    }

    @Test
    @DisplayName("중복된 requestId면 저장을 건너뛴다")
    void handle_shouldSkip_ifDuplicate() {
        // given
        BalanceChargedEvent event = new BalanceChargedEvent(1L, 10000L, "중복충전", "REQ-1234");
        when(balanceHistoryRepository.existsByRequestId("REQ-1234")).thenReturn(true);

        // when
        handler.handle(event);

        // then
        verify(balanceHistoryRepository, never()).save(any());
    }
}
