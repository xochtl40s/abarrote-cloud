package com.abarrote.abarroteapi.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ReporteVentasRequest {
    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaFin;

    private Long usuarioId;

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}
