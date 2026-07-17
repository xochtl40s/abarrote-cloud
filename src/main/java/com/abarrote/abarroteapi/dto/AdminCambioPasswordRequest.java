package com.abarrote.abarroteapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminCambioPasswordRequest {

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(
            min = 4,
            message = "La nueva contraseña debe tener al menos 4 caracteres"
    )
    private String passwordNueva;

    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmarPassword;

    public AdminCambioPasswordRequest() {
    }

    public AdminCambioPasswordRequest(
            String passwordNueva,
            String confirmarPassword) {

        this.passwordNueva = passwordNueva;
        this.confirmarPassword = confirmarPassword;
    }

    public String getPasswordNueva() {
        return passwordNueva;
    }

    public void setPasswordNueva(String passwordNueva) {
        this.passwordNueva = passwordNueva;
    }

    public String getConfirmarPassword() {
        return confirmarPassword;
    }

    public void setConfirmarPassword(String confirmarPassword) {
        this.confirmarPassword = confirmarPassword;
    }
}
