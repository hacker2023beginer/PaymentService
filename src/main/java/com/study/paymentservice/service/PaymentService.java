package com.study.paymentservice.service;

import com.study.paymentservice.client.RandomNumberClient;
import com.study.paymentservice.dto.PaymentRequestDto;
import com.study.paymentservice.dto.PaymentResponseDto;
import com.study.paymentservice.entity.Payment;
import com.study.paymentservice.kafka.event.PaymentEvent;
import com.study.paymentservice.exception.PaymentServiceException;
import com.study.paymentservice.mapper.PaymentMapper;
import com.study.paymentservice.kafka.producer.PaymentProducer;
import com.study.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.study.paymentservice.entity.PaymentStatus.FAILED;
import static com.study.paymentservice.entity.PaymentStatus.SUCCESS;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentMapper mapper;
    private final RandomNumberClient randomClient;
    private final PaymentRepository paymentRepository;
    private final PaymentProducer producer;

    public PaymentResponseDto create(PaymentRequestDto requestDto) {
        Payment payment = mapper.toEntity(requestDto);

        payment.setTimestamp(Instant.now());

        int random = randomClient.getRandomNumber();

        if (random % 2 == 0) {
            payment.setStatus(SUCCESS.name());
        } else {
            payment.setStatus(FAILED.name());
        }

        Payment saved = paymentRepository.save(payment);

        PaymentEvent event = PaymentEvent.builder()
                .paymentId(saved.getId())
                .orderId(saved.getOrderId())
                .userId(saved.getUserId())
                .status(saved.getStatus())
                .build();

        producer.send(event);
        return mapper.toDto(saved);
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

    public Payment getById(String orderId){
        Payment payment = paymentRepository.getByOrderId(orderId);

        if (payment == null) {
            throw new PaymentServiceException("Payment not found for orderId: " + orderId);
        }

        return payment;
    }
}
