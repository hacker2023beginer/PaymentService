package com.study.paymentservice.repository;

import com.study.paymentservice.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByOrderId(String orderId);

    List<Payment> findByUserId(String userId);

    List<Payment> findByStatus(String status);

    List<Payment> findByUserIdAndTimestampBetween(
            String userId,
            Instant start,
            Instant end
    );

    List<Payment> findByTimestampBetween(
            Instant start,
            Instant end
    );

    Payment getByOrderId(String orderId);
}
