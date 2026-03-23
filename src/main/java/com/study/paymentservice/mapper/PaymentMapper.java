package com.study.paymentservice.mapper;

import com.study.paymentservice.dto.PaymentRequestDto;
import com.study.paymentservice.dto.PaymentResponseDto;
import com.study.paymentservice.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    Payment toEntity(PaymentRequestDto dto);

    PaymentResponseDto toDto(Payment payment);
}
