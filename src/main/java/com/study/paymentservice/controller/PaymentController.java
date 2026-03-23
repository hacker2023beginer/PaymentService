package com.study.paymentservice.controller;

import com.study.paymentservice.entity.Payment;
import com.study.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public Payment create(@RequestBody Payment payment) {
        return paymentService.create(payment);
    }

    @GetMapping
    public List<Payment> getPayments(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String status
    ) {
        return paymentService.getPayments(userId, orderId, status);
    }

    @GetMapping("/sum/user")
    public BigDecimal getUserSum(
            @RequestParam String userId,
            @RequestParam String from,
            @RequestParam String to
    ) {
        return paymentService.getUserSum(
                userId,
                Instant.parse(from),
                Instant.parse(to)
        );
    }

    @GetMapping("/sum")
    public BigDecimal getTotalSum(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return paymentService.getTotalSum(
                Instant.parse(from),
                Instant.parse(to)
        );
    }
}
