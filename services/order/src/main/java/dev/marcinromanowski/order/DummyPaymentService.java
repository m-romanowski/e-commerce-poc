package dev.marcinromanowski.order;

import java.util.UUID;

class DummyPaymentService implements PaymentService {

    @Override
    public PaymentResponse getApprovalLink(Order order) {
        return new PaymentResponse(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

}
