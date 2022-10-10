package dev.marcinromanowski.order;

import lombok.val;

import java.util.UUID;

class DummyPaymentService implements PaymentService {

    private static final String LINK = "http://localhost?paymentId=%s";

    @Override
    public PaymentResponse getApprovalLink(Order order) {
        val paymentId = UUID.randomUUID().toString();
        return new PaymentResponse(paymentId, LINK.formatted(paymentId));
    }

}
