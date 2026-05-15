package com.study.paymentservice.kafka.producer;

import com.study.paymentservice.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    private static final String TOPIC = "payment-events";

    public void send(PaymentEvent event) {
        kafkaTemplate.send(TOPIC, event.getOrderId(), event);
    }
}
