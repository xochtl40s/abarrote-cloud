package com.abarrote.abarroteapi.multitenant.dto;

import com.abarrote.abarroteapi.multitenant.domain.EstadoTenant;
import com.abarrote.abarroteapi.multitenant.domain.TipoNegocio;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TenantResponse {

    private Long id;
    private String nombre;
    private String slug;
    private TipoNegocio tipoNegocio;
    private EstadoTenant estado;
    private String plan;
    private LocalDateTime fechaAlta;
    private LocalDate fechaVencimiento;
    private boolean activo;

    public TenantResponse() {
    }

    public TenantResponse(
        Long id,
        String nombre,
        String slug,
        TipoNegocio tipoNegocio,
        EstadoTenant estado,
        String plan,
        LocalDateTime fechaAlta,
        LocalDate fechaVencimiento,
        boolean activo
    ) {
        this.id = id;
        this.nombre = nombre;
        this.slug = slug;
        this.tipoNegocio = tipoNegocio;
        this.estado = estado;
        this.plan = plan;
        this.fechaAlta = fechaAlta;
        this.fechaVencimiento = fechaVencimiento;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSlug() {
        return slug;
    }

    public TipoNegocio getTipoNegocio() {
        return tipoNegocio;
    }

    public EstadoTenant getEstado() {
        return estado;
    }

    public String getPlan() {
        return plan;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public boolean isActivo() {
        return activo;
    }
}
