package com.abarrote.abarroteapi.multitenant.dto;

import com.abarrote.abarroteapi.multitenant.domain.TipoNegocio;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class TenantRequest {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(
        min = 3,
        max = 150,
        message = "El nombre debe contener entre 3 y 150 caracteres"
    )
    private String nombre;

    @NotBlank(message = "El slug es obligatorio")
    @Size(
        min = 3,
        max = 80,
        message = "El slug debe contener entre 3 y 80 caracteres"
    )
    @Pattern(
        regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
        message = "El slug solo puede contener letras minúsculas, números y guiones"
    )
    private String slug;

    @NotNull(message = "El tipo de negocio es obligatorio")
    private TipoNegocio tipoNegocio;

    @NotBlank(message = "El plan es obligatorio")
    @Size(
        max = 30,
        message = "El plan no puede exceder 30 caracteres"
    )
    private String plan;

    @FutureOrPresent(
        message = "La fecha de vencimiento no puede estar en el pasado"
    )
    private LocalDate fechaVencimiento;

    public TenantRequest() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public TipoNegocio getTipoNegocio() {
        return tipoNegocio;
    }

    public void setTipoNegocio(TipoNegocio tipoNegocio) {
        this.tipoNegocio = tipoNegocio;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
}
