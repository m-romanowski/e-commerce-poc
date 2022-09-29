package dev.marcinromanowski.order;

interface PaymentService {
    PaymentResponse getApprovalLink(Order order);
}

record PaymentResponse(String id, String approvalLink) {

}
