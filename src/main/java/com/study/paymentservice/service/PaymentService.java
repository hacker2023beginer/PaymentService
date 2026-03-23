package com.study.paymentservice.service;

import com.study.paymentservice.entity.Payment;
import com.study.paymentservice.exception.PaymentServiceException;
import com.study.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Payment create(Payment payment) {
        payment.setTimestamp(Instant.now());
        return paymentRepository.save(payment);
    }

    public List<Payment> getPayments(String userId, String orderId, String status) {

        if (userId != null) {
            return paymentRepository.findByUserId(userId);
        }

        if (orderId != null) {
            return paymentRepository.findByOrderId(orderId);
        }

        if (status != null) {
            return paymentRepository.findByStatus(status);
        }

        throw new PaymentServiceException("At least one parameter must be provided");
    }

    public BigDecimal getUserSum(String userId, Instant from, Instant to) {
        return paymentRepository
                .findByUserIdAndTimestampBetween(userId, from, to)
                .stream()
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalSum(Instant from, Instant to) {
        return paymentRepository
                .findByTimestampBetween(from, to)
                .stream()
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
