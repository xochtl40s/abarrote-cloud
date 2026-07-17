package com.abarrote.abarroteapi.dto;

import jakarta.validation.constraints.*;

public class CategoriaRequest {
    @NotBlank
    @Size(min = 2, max = 50)
    private String nombre;

    @Size(max = 255)
    private String descripcion;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
