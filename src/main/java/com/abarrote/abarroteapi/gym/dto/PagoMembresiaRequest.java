package com.abarrote.abarroteapi.gym.dto;

import com.abarrote.abarroteapi.gym.domain.MetodoPagoGym;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PagoMembresiaRequest(

    @NotNull
    Long membresiaId,

    @NotNull
    @DecimalMin("0.01")
    BigDecimal importe,

    @NotNull
    MetodoPagoGym metodoPago,

    @Size(max = 100)
    String referencia,

    @Size(max = 500)
    String observaciones
) {
}
