package com.abarrote.abarroteapi.gym.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GymVentaResponse(
    Long ventaId,
    BigDecimal total,
    String metodoPago,
    LocalDateTime fechaVenta
) {
}
