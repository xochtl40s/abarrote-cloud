package com.abarrote.abarroteapi.dto;

import java.math.BigDecimal;

public class SucursalComparativoResponse {

    private Long sucursalId;
    private String codigo;
    private String nombre;

    private Integer numeroVentas;
    private BigDecimal totalVentas;

    private Integer unidadesInventario;
    private Integer productosDiferentes;
    private BigDecimal valorInventario;

    private Integer porcentajeVentas;
    private Integer porcentajeInventario;
    private Integer porcentajeUnidades;

    public SucursalComparativoResponse() {
    }

    public Long getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Long sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getNumeroVentas() {
        return numeroVentas;
    }

    public void setNumeroVentas(Integer numeroVentas) {
        this.numeroVentas = numeroVentas;
    }

    public BigDecimal getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(BigDecimal totalVentas) {
        this.totalVentas = totalVentas;
    }

    public Integer getUnidadesInventario() {
        return unidadesInventario;
    }

    public void setUnidadesInventario(
            Integer unidadesInventario) {

        this.unidadesInventario = unidadesInventario;
    }

    public Integer getProductosDiferentes() {
        return productosDiferentes;
    }

    public void setProductosDiferentes(
            Integer productosDiferentes) {

        this.productosDiferentes = productosDiferentes;
    }

    public BigDecimal getValorInventario() {
        return valorInventario;
    }

    public void setValorInventario(
            BigDecimal valorInventario) {

        this.valorInventario = valorInventario;
    }

    public Integer getPorcentajeVentas() {
        return porcentajeVentas;
    }

    public void setPorcentajeVentas(
            Integer porcentajeVentas) {

        this.porcentajeVentas = porcentajeVentas;
    }

    public Integer getPorcentajeInventario() {
        return porcentajeInventario;
    }

    public void setPorcentajeInventario(
            Integer porcentajeInventario) {

        this.porcentajeInventario = porcentajeInventario;
    }

    public Integer getPorcentajeUnidades() {
        return porcentajeUnidades;
    }

    public void setPorcentajeUnidades(
            Integer porcentajeUnidades) {

        this.porcentajeUnidades = porcentajeUnidades;
    }
}
