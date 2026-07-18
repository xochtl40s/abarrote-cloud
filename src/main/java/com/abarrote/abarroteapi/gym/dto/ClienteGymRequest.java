package com.abarrote.abarroteapi.gym.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ClienteGymRequest(

    @NotBlank
    @Size(max = 30)
    String numeroCliente,

    @NotBlank
    @Size(max = 100)
    String nombre,

    @Size(max = 100)
    String apellidoPaterno,

    @Size(max = 100)
    String apellidoMaterno,

    @Size(max = 30)
    String telefono,

    @Email
    @Size(max = 150)
    String email,

    LocalDate fechaNacimiento,

    @Size(max = 150)
    String contactoEmergencia,

    @Size(max = 30)
    String telefonoEmergencia
) {
}
