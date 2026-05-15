package com.study.paymentservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class PaymentRequestDto {

    @NotBlank
    private String orderId;

    @NotBlank
    private String userId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal paymentAmount;
}
