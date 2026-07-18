package com.abarrote.abarroteapi.gym.dto;

import com.abarrote.abarroteapi.gym.domain.MetodoPagoGym;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoMembresiaResponse(
    Long id,
    Long membresiaId,
    BigDecimal importe,
    MetodoPagoGym metodoPago,
    String referencia,
    LocalDateTime fechaPago
) {
}
