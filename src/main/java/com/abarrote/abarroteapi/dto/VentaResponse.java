package com.abarrote.abarroteapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VentaResponse {
    private Long id;
    private LocalDateTime fecha;
    private BigDecimal total;
    private String cajero;
    private String estado;
    private List<DetalleVentaResponse> detalles;

    public static class DetalleVentaResponse {
        private String productoNombre;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;

        public String getProductoNombre() { return productoNombre; }
        public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getCajero() { return cajero; }
    public void setCajero(String cajero) { this.cajero = cajero; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public List<DetalleVentaResponse> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVentaResponse> detalles) { this.detalles = detalles; }
}
