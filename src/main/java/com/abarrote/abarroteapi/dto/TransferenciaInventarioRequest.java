package com.abarrote.abarroteapi.dto;

public class TransferenciaInventarioRequest {

    private Long sucursalOrigenId;
    private Long sucursalDestinoId;
    private Long productoId;
    private Integer cantidad;
    private String motivo;
    private String observaciones;

    public TransferenciaInventarioRequest() {
    }

    public Long getSucursalOrigenId() {
        return sucursalOrigenId;
    }

    public void setSucursalOrigenId(
            Long sucursalOrigenId) {

        this.sucursalOrigenId = sucursalOrigenId;
    }

    public Long getSucursalDestinoId() {
        return sucursalDestinoId;
    }

    public void setSucursalDestinoId(
            Long sucursalDestinoId) {

        this.sucursalDestinoId = sucursalDestinoId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(
            Long productoId) {

        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(
            Integer cantidad) {

        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(
            String motivo) {

        this.motivo = motivo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones) {

        this.observaciones = observaciones;
    }
}
