package dev.marcinromanowski.infrastructure.web;

import dev.marcinromanowski.order.OrderFacade;
import dev.marcinromanowski.order.dto.ApprovalLinkDto;
import dev.marcinromanowski.order.dto.OrderDto;
import dev.marcinromanowski.order.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/checkout")
public class CheckoutController {

    private final OrderFacade orderFacade;

    @PostMapping(value = "/new", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ApprovalLinkDto> checkout(@RequestBody OrderDto order) {
        return orderFacade.newOrder(order);
    }

    // TODO: more payments methods support
    //  for now, I just simulated integration with paypal for example purposes
    @PostMapping(value = "/success", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Void> paymentSuccess(@RequestBody PaymentDto payment) {
        return orderFacade.paymentSucceeded(payment);
    }

    @PostMapping(value = "/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Void> paymentCancel(@RequestBody PaymentDto payment) {
        return orderFacade.paymentCanceled(payment);
    }

}
