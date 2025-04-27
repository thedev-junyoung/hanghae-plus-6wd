package kr.hhplus.be.server.interfaces.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.application.payment.RequestPaymentCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentRequest {

    @NotBlank
    private String orderId;

    @NotNull
    private Long userId;

    @NotBlank
    private String method;

    private Long amount;


    public RequestPaymentCommand toCommand() {
        return new RequestPaymentCommand(orderId, userId, amount, method);
    }
}
