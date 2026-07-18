package com.abarrote.abarroteapi.gym.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PlanMembresiaRequest(

    @NotBlank
    @Size(max = 100)
    String nombre,

    @Size(max = 500)
    String descripcion,

    @NotNull
    @Min(1)
    Integer duracionDias,

    @NotNull
    @DecimalMin("0.01")
    BigDecimal precio
) {
}
