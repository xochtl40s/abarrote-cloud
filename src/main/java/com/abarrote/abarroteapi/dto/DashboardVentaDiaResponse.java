package com.abarrote.abarroteapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DashboardVentaDiaResponse {

    private LocalDate fecha;
    private String etiqueta;
    private BigDecimal total;
    private Integer numeroVentas;
    private Integer porcentaje;

    public DashboardVentaDiaResponse() {
    }

    public DashboardVentaDiaResponse(
            LocalDate fecha,
            String etiqueta,
            BigDecimal total,
            Integer numeroVentas,
            Integer porcentaje) {

        this.fecha = fecha;
        this.etiqueta = etiqueta;
        this.total = total;
        this.numeroVentas = numeroVentas;
        this.porcentaje = porcentaje;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Integer getNumeroVentas() {
        return numeroVentas;
    }

    public void setNumeroVentas(Integer numeroVentas) {
        this.numeroVentas = numeroVentas;
    }

    public Integer getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(Integer porcentaje) {
        this.porcentaje = porcentaje;
    }
}
