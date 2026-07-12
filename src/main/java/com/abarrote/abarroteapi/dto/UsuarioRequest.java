package com.abarrote.abarroteapi.dto;

import jakarta.validation.constraints.*;

public class UsuarioRequest {
    @NotBlank
    @Size(min = 2, max = 100)
    private String nombre;

    @NotBlank
    @Size(min = 4, max = 20)
    private String username;

    @Size(min = 4)
    private String password;

    @NotBlank
    @Pattern(regexp = "ADMIN|CAJERO")
    private String rol;

    private Boolean activo = true;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
