package dev.marcinromanowski.infrastructure.web;

import dev.marcinromanowski.order.OrderFacade;
import dev.marcinromanowski.order.dto.ApprovalLinkDto;
import dev.marcinromanowski.order.dto.OrderDto;
import dev.marcinromanowski.order.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping(name = "/api/v1/checkout")
public class CheckoutController {

    private final OrderFacade orderFacade;

    @PostMapping
    public Mono<ApprovalLinkDto> checkout(@RequestBody OrderDto order) {
        return orderFacade.newOrder(order);
    }

    // TODO: more payments methods support
    //  for now, I just simulated integration with paypal for example purposes
    @PostMapping(value = "/success")
    public Mono<Void> paymentSuccess(@RequestParam String paymentId) {
        return orderFacade.paymentSucceeded(new PaymentDto(paymentId));
    }

    @PostMapping(value = "/cancel")
    public Mono<Void> paymentCancel(@RequestParam String paymentId) {
        return orderFacade.paymentCancel(new PaymentDto(paymentId));
    }

}
