package com.abarrote.abarroteapi.gym.dto;

import java.math.BigDecimal;

public record PlanMembresiaResponse(
    Long id,
    String nombre,
    String descripcion,
    Integer duracionDias,
    BigDecimal precio,
    boolean activo
) {
}
