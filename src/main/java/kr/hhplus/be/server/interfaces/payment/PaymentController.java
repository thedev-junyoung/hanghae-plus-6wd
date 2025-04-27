package kr.hhplus.be.server.interfaces.payment;

import kr.hhplus.be.server.application.payment.*;
import kr.hhplus.be.server.common.dto.CustomApiResponse;
import kr.hhplus.be.server.domain.payment.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentAPI {

    private final PaymentFacadeService paymentFacadeService;

    @Override
    public ResponseEntity<CustomApiResponse<PaymentResponse>> requestPayment(PaymentRequest request) {
        PaymentResult paymentResult = paymentFacadeService.requestPayment(request.toCommand());
        return ResponseEntity.ok(CustomApiResponse.success(PaymentResponse.from(paymentResult)));
    }
}
