package com.abarrote.abarroteapi.gym.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GymVentaRequest(

    @NotEmpty
    List<@Valid Item> productos,

    @NotNull
    @Size(max = 30)
    String metodoPago
) {

    public record Item(

        @NotNull
        Long productoId,

        @NotNull
        @Positive
        Integer cantidad
    ) {
    }
}
