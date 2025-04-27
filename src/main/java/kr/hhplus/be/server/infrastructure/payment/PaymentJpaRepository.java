package kr.hhplus.be.server.infrastructure.payment;

import kr.hhplus.be.server.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);

}
