package kr.hhplus.be.server.infrastructure.payment;

import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    @Override
    public void save(Payment payment) {
        jpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        return jpaRepository.findByOrderId(orderId);
    }
}
