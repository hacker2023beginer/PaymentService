package com.study.paymentservice.kafka.event;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    private String paymentId;
    private String orderId;
    private String userId;
    private String status;
}
