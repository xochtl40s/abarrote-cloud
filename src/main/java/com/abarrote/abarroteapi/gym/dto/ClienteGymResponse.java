package com.abarrote.abarroteapi.gym.dto;

import com.abarrote.abarroteapi.gym.domain.EstadoClienteGym;

import java.time.LocalDate;

public record ClienteGymResponse(
    Long id,
    String numeroCliente,
    String nombreCompleto,
    String telefono,
    String email,
    LocalDate fechaNacimiento,
    EstadoClienteGym estado
) {
}
