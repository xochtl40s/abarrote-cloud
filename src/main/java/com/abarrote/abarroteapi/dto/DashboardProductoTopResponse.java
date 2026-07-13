package com.abarrote.abarroteapi.dto;

import java.math.BigDecimal;

public class DashboardProductoTopResponse {

    private Long productoId;
    private String nombre;
    private Long cantidadVendida;
    private BigDecimal importeVendido;
    private Integer posicion;

    public DashboardProductoTopResponse() {
    }

    public DashboardProductoTopResponse(
            Long productoId,
            String nombre,
            Long cantidadVendida,
            BigDecimal importeVendido,
            Integer posicion) {

        this.productoId = productoId;
        this.nombre = nombre;
        this.cantidadVendida = cantidadVendida;
        this.importeVendido = importeVendido;
        this.posicion = posicion;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(Long cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public BigDecimal getImporteVendido() {
        return importeVendido;
    }

    public void setImporteVendido(BigDecimal importeVendido) {
        this.importeVendido = importeVendido;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }
}
