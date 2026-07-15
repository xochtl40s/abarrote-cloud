package com.abarrote.abarroteapi.dto;

public class InventarioSucursalResponse {

    private Long inventarioId;

    private Long sucursalId;
    private String sucursalCodigo;
    private String sucursalNombre;

    private Long productoId;
    private String productoNombre;
    private String codigoBarras;

    private Integer existencia;
    private Integer stockMinimo;

    public InventarioSucursalResponse() {
    }

    public Long getInventarioId() {
        return inventarioId;
    }

    public void setInventarioId(
            Long inventarioId) {

        this.inventarioId = inventarioId;
    }

    public Long getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(
            Long sucursalId) {

        this.sucursalId = sucursalId;
    }

    public String getSucursalCodigo() {
        return sucursalCodigo;
    }

    public void setSucursalCodigo(
            String sucursalCodigo) {

        this.sucursalCodigo = sucursalCodigo;
    }

    public String getSucursalNombre() {
        return sucursalNombre;
    }

    public void setSucursalNombre(
            String sucursalNombre) {

        this.sucursalNombre = sucursalNombre;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(
            Long productoId) {

        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(
            String productoNombre) {

        this.productoNombre = productoNombre;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(
            String codigoBarras) {

        this.codigoBarras = codigoBarras;
    }

    public Integer getExistencia() {
        return existencia;
    }

    public void setExistencia(
            Integer existencia) {

        this.existencia = existencia;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(
            Integer stockMinimo) {

        this.stockMinimo = stockMinimo;
    }
}
