package com.abarrote.abarroteapi.gym.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MembresiaRequest(

    @NotNull
    Long clienteId,

    @NotNull
    Long planId,

    LocalDate fechaInicio
) {
}
