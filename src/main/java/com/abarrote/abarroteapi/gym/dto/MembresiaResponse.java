package com.abarrote.abarroteapi.gym.dto;

import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MembresiaResponse(
    Long id,
    Long clienteId,
    String cliente,
    Long planId,
    String plan,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    BigDecimal precio,
    EstadoMembresia estado
) {
}
