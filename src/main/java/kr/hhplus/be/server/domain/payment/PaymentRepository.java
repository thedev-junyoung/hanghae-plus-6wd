package kr.hhplus.be.server.domain.payment;

import java.util.Optional;

public interface PaymentRepository {

    /**
     * 결제 정보를 저장하거나 업데이트한다.
     * 결제 완료 시점 또는 결제 상태 변경이 발생할 때 호출된다.
     */
    void save(Payment payment);

    /**
     * 결제 ID로 결제 정보를 조회한다.
     * 주로 결제 상세 확인이나 이벤트 처리 시 사용된다.
     */
    Optional<Payment> findById(String id);

    Optional<Payment> findByOrderId(String id);
}
