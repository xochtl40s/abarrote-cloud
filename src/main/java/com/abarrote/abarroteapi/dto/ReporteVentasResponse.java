package com.abarrote.abarroteapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReporteVentasResponse {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer totalVentas;
    private BigDecimal montoTotal;
    private List<VentaResumen> ventas;
    private List<ProductoTop> productosMasVendidos;
    private List<ResumenCajero> resumenPorCajero;

    public static class VentaResumen {
        private Long ventaId;
        private String fecha;
        private String cajero;
        private BigDecimal total;
        private Integer cantidadProductos;

        public Long getVentaId() { return ventaId; }
        public void setVentaId(Long ventaId) { this.ventaId = ventaId; }
        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }
        public String getCajero() { return cajero; }
        public void setCajero(String cajero) { this.cajero = cajero; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public Integer getCantidadProductos() { return cantidadProductos; }
        public void setCantidadProductos(Integer cantidadProductos) { this.cantidadProductos = cantidadProductos; }
    }

    public static class ProductoTop {
        private String nombre;
        private Integer cantidadVendida;
        private BigDecimal montoTotal;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public Integer getCantidadVendida() { return cantidadVendida; }
        public void setCantidadVendida(Integer cantidadVendida) { this.cantidadVendida = cantidadVendida; }
        public BigDecimal getMontoTotal() { return montoTotal; }
        public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
    }

    public static class ResumenCajero {
        private String cajero;
        private Integer cantidadVentas;
        private BigDecimal montoTotal;
        private List<ProductoCajero> productosVendidos;

        public String getCajero() { return cajero; }
        public void setCajero(String cajero) { this.cajero = cajero; }
        public Integer getCantidadVentas() { return cantidadVentas; }
        public void setCantidadVentas(Integer cantidadVentas) { this.cantidadVentas = cantidadVentas; }
        public BigDecimal getMontoTotal() { return montoTotal; }
        public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
        public List<ProductoCajero> getProductosVendidos() { return productosVendidos; }
        public void setProductosVendidos(List<ProductoCajero> productosVendidos) { this.productosVendidos = productosVendidos; }
    }

    public static class ProductoCajero {
        private String nombreProducto;
        private Integer cantidad;
        private BigDecimal monto;

        public String getNombreProducto() { return nombreProducto; }
        public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal monto) { this.monto = monto; }
    }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public Integer getTotalVentas() { return totalVentas; }
    public void setTotalVentas(Integer totalVentas) { this.totalVentas = totalVentas; }
    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
    public List<VentaResumen> getVentas() { return ventas; }
    public void setVentas(List<VentaResumen> ventas) { this.ventas = ventas; }
    public List<ProductoTop> getProductosMasVendidos() { return productosMasVendidos; }
    public void setProductosMasVendidos(List<ProductoTop> productosMasVendidos) { this.productosMasVendidos = productosMasVendidos; }
    public List<ResumenCajero> getResumenPorCajero() { return resumenPorCajero; }
    public void setResumenPorCajero(List<ResumenCajero> resumenPorCajero) { this.resumenPorCajero = resumenPorCajero; }
}
