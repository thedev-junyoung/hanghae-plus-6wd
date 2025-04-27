package kr.hhplus.be.server.application.payment;


import kr.hhplus.be.server.domain.payment.Payment;

public interface PaymentUseCase {

    Payment recordSuccess(PaymentCommand command);

}
