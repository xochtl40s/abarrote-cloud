package com.abarrote.abarroteapi.dto;

import java.time.LocalDateTime;

public class MovimientoInventarioResponse {

    private Long id;
    private String folio;

    private String producto;
    private String codigoBarras;

    private String sucursalOrigen;
    private String sucursalDestino;

    private Integer cantidad;

    private Integer existenciaOrigenAnterior;
    private Integer existenciaOrigenNueva;

    private Integer existenciaDestinoAnterior;
    private Integer existenciaDestinoNueva;

    private String usuarioResponsable;
    private String motivo;
    private String estado;

    private LocalDateTime fechaMovimiento;

    public MovimientoInventarioResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id) {

        this.id = id;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(
            String folio) {

        this.folio = folio;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(
            String producto) {

        this.producto = producto;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(
            String codigoBarras) {

        this.codigoBarras = codigoBarras;
    }

    public String getSucursalOrigen() {
        return sucursalOrigen;
    }

    public void setSucursalOrigen(
            String sucursalOrigen) {

        this.sucursalOrigen = sucursalOrigen;
    }

    public String getSucursalDestino() {
        return sucursalDestino;
    }

    public void setSucursalDestino(
            String sucursalDestino) {

        this.sucursalDestino = sucursalDestino;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(
            Integer cantidad) {

        this.cantidad = cantidad;
    }

    public Integer getExistenciaOrigenAnterior() {
        return existenciaOrigenAnterior;
    }

    public void setExistenciaOrigenAnterior(
            Integer existenciaOrigenAnterior) {

        this.existenciaOrigenAnterior =
                existenciaOrigenAnterior;
    }

    public Integer getExistenciaOrigenNueva() {
        return existenciaOrigenNueva;
    }

    public void setExistenciaOrigenNueva(
            Integer existenciaOrigenNueva) {

        this.existenciaOrigenNueva =
                existenciaOrigenNueva;
    }

    public Integer getExistenciaDestinoAnterior() {
        return existenciaDestinoAnterior;
    }

    public void setExistenciaDestinoAnterior(
            Integer existenciaDestinoAnterior) {

        this.existenciaDestinoAnterior =
                existenciaDestinoAnterior;
    }

    public Integer getExistenciaDestinoNueva() {
        return existenciaDestinoNueva;
    }

    public void setExistenciaDestinoNueva(
            Integer existenciaDestinoNueva) {

        this.existenciaDestinoNueva =
                existenciaDestinoNueva;
    }

    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }

    public void setUsuarioResponsable(
            String usuarioResponsable) {

        this.usuarioResponsable =
                usuarioResponsable;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(
            String motivo) {

        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(
            String estado) {

        this.estado = estado;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(
            LocalDateTime fechaMovimiento) {

        this.fechaMovimiento = fechaMovimiento;
    }
}
