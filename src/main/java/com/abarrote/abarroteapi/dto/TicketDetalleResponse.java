package com.abarrote.abarroteapi.dto;

import java.math.BigDecimal;

public class TicketDetalleResponse {

    private String producto;

    private Integer cantidad;

    private BigDecimal precioUnitario;

    private BigDecimal subtotal;

    public TicketDetalleResponse() {
    }

    public TicketDetalleResponse(
            String producto,
            Integer cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal) {

        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(
            String producto) {

        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(
            Integer cantidad) {

        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(
            BigDecimal precioUnitario) {

        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(
            BigDecimal subtotal) {

        this.subtotal = subtotal;
    }
}
