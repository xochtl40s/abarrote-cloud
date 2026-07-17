package com.abarrote.abarroteapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteAvanzadoResponse {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private Integer numeroVentas;
    private Integer articulosVendidos;

    private BigDecimal ventasNetas;
    private BigDecimal costoMercancia;
    private BigDecimal beneficioBruto;
    private BigDecimal margenBruto;
    private BigDecimal ventaPromedio;

    private List<VentaProducto> ventasPorProducto =
            new ArrayList<>();

    private List<VentaCategoria> ventasPorCategoria =
            new ArrayList<>();

    private List<VentaCajero> ventasPorCajero =
            new ArrayList<>();

    public ReporteAvanzadoResponse() {
    }

    public static class VentaProducto {

        private Long productoId;
        private String producto;
        private String categoria;

        private Integer articulosVendidos;

        private BigDecimal ventasNetas;
        private BigDecimal costoMercancia;
        private BigDecimal beneficioBruto;
        private BigDecimal margenBruto;

        public VentaProducto() {
        }

        public Long getProductoId() {
            return productoId;
        }

        public void setProductoId(Long productoId) {
            this.productoId = productoId;
        }

        public String getProducto() {
            return producto;
        }

        public void setProducto(String producto) {
            this.producto = producto;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public Integer getArticulosVendidos() {
            return articulosVendidos;
        }

        public void setArticulosVendidos(Integer articulosVendidos) {
            this.articulosVendidos = articulosVendidos;
        }

        public BigDecimal getVentasNetas() {
            return ventasNetas;
        }

        public void setVentasNetas(BigDecimal ventasNetas) {
            this.ventasNetas = ventasNetas;
        }

        public BigDecimal getCostoMercancia() {
            return costoMercancia;
        }

        public void setCostoMercancia(BigDecimal costoMercancia) {
            this.costoMercancia = costoMercancia;
        }

        public BigDecimal getBeneficioBruto() {
            return beneficioBruto;
        }

        public void setBeneficioBruto(BigDecimal beneficioBruto) {
            this.beneficioBruto = beneficioBruto;
        }

        public BigDecimal getMargenBruto() {
            return margenBruto;
        }

        public void setMargenBruto(BigDecimal margenBruto) {
            this.margenBruto = margenBruto;
        }
    }

    public static class VentaCategoria {

        private Long categoriaId;
        private String categoria;

        private Integer articulosVendidos;

        private BigDecimal ventasNetas;
        private BigDecimal costoMercancia;
        private BigDecimal beneficioBruto;
        private BigDecimal margenBruto;

        public VentaCategoria() {
        }

        public Long getCategoriaId() {
            return categoriaId;
        }

        public void setCategoriaId(Long categoriaId) {
            this.categoriaId = categoriaId;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public Integer getArticulosVendidos() {
            return articulosVendidos;
        }

        public void setArticulosVendidos(Integer articulosVendidos) {
            this.articulosVendidos = articulosVendidos;
        }

        public BigDecimal getVentasNetas() {
            return ventasNetas;
        }

        public void setVentasNetas(BigDecimal ventasNetas) {
            this.ventasNetas = ventasNetas;
        }

        public BigDecimal getCostoMercancia() {
            return costoMercancia;
        }

        public void setCostoMercancia(BigDecimal costoMercancia) {
            this.costoMercancia = costoMercancia;
        }

        public BigDecimal getBeneficioBruto() {
            return beneficioBruto;
        }

        public void setBeneficioBruto(BigDecimal beneficioBruto) {
            this.beneficioBruto = beneficioBruto;
        }

        public BigDecimal getMargenBruto() {
            return margenBruto;
        }

        public void setMargenBruto(BigDecimal margenBruto) {
            this.margenBruto = margenBruto;
        }
    }

    public static class VentaCajero {

        private Long usuarioId;
        private String cajero;

        private Integer numeroVentas;
        private Integer articulosVendidos;

        private BigDecimal ventasNetas;
        private BigDecimal costoMercancia;
        private BigDecimal beneficioBruto;
        private BigDecimal ventaPromedio;

        public VentaCajero() {
        }

        public Long getUsuarioId() {
            return usuarioId;
        }

        public void setUsuarioId(Long usuarioId) {
            this.usuarioId = usuarioId;
        }

        public String getCajero() {
            return cajero;
        }

        public void setCajero(String cajero) {
            this.cajero = cajero;
        }

        public Integer getNumeroVentas() {
            return numeroVentas;
        }

        public void setNumeroVentas(Integer numeroVentas) {
            this.numeroVentas = numeroVentas;
        }

        public Integer getArticulosVendidos() {
            return articulosVendidos;
        }

        public void setArticulosVendidos(Integer articulosVendidos) {
            this.articulosVendidos = articulosVendidos;
        }

        public BigDecimal getVentasNetas() {
            return ventasNetas;
        }

        public void setVentasNetas(BigDecimal ventasNetas) {
            this.ventasNetas = ventasNetas;
        }

        public BigDecimal getCostoMercancia() {
            return costoMercancia;
        }

        public void setCostoMercancia(BigDecimal costoMercancia) {
            this.costoMercancia = costoMercancia;
        }

        public BigDecimal getBeneficioBruto() {
            return beneficioBruto;
        }

        public void setBeneficioBruto(BigDecimal beneficioBruto) {
            this.beneficioBruto = beneficioBruto;
        }

        public BigDecimal getVentaPromedio() {
            return ventaPromedio;
        }

        public void setVentaPromedio(BigDecimal ventaPromedio) {
            this.ventaPromedio = ventaPromedio;
        }
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getNumeroVentas() {
        return numeroVentas;
    }

    public void setNumeroVentas(Integer numeroVentas) {
        this.numeroVentas = numeroVentas;
    }

    public Integer getArticulosVendidos() {
        return articulosVendidos;
    }

    public void setArticulosVendidos(Integer articulosVendidos) {
        this.articulosVendidos = articulosVendidos;
    }

    public BigDecimal getVentasNetas() {
        return ventasNetas;
    }

    public void setVentasNetas(BigDecimal ventasNetas) {
        this.ventasNetas = ventasNetas;
    }

    public BigDecimal getCostoMercancia() {
        return costoMercancia;
    }

    public void setCostoMercancia(BigDecimal costoMercancia) {
        this.costoMercancia = costoMercancia;
    }

    public BigDecimal getBeneficioBruto() {
        return beneficioBruto;
    }

    public void setBeneficioBruto(BigDecimal beneficioBruto) {
        this.beneficioBruto = beneficioBruto;
    }

    public BigDecimal getMargenBruto() {
        return margenBruto;
    }

    public void setMargenBruto(BigDecimal margenBruto) {
        this.margenBruto = margenBruto;
    }

    public BigDecimal getVentaPromedio() {
        return ventaPromedio;
    }

    public void setVentaPromedio(BigDecimal ventaPromedio) {
        this.ventaPromedio = ventaPromedio;
    }

    public List<VentaProducto> getVentasPorProducto() {
        return ventasPorProducto;
    }

    public void setVentasPorProducto(
            List<VentaProducto> ventasPorProducto) {
        this.ventasPorProducto = ventasPorProducto;
    }

    public List<VentaCategoria> getVentasPorCategoria() {
        return ventasPorCategoria;
    }

    public void setVentasPorCategoria(
            List<VentaCategoria> ventasPorCategoria) {
        this.ventasPorCategoria = ventasPorCategoria;
    }

    public List<VentaCajero> getVentasPorCajero() {
        return ventasPorCajero;
    }

    public void setVentasPorCajero(
            List<VentaCajero> ventasPorCajero) {
        this.ventasPorCajero = ventasPorCajero;
    }
}
