package dev.marcinromanowski.order;

import dev.marcinromanowski.order.dto.ApprovalLinkDto;
import dev.marcinromanowski.order.dto.OrderDto;
import dev.marcinromanowski.order.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;

    public Mono<ApprovalLinkDto> newOrder(OrderDto orderDto) {
        return orderService.processOrder(orderDto)
                .map(ApprovalLinkDto::new);
    }

    public Mono<Void> paymentSucceeded(PaymentDto paymentDto) {
        return orderService.paymentSucceeded(paymentDto.paymentId());
    }

    public Mono<Void> paymentCanceled(PaymentDto paymentDto) {
        return orderService.paymentCanceled(paymentDto.paymentId());
    }

}
