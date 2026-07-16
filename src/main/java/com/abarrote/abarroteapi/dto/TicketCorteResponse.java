package com.abarrote.abarroteapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketCorteResponse {

    private Long id;

    private LocalDateTime fechaHora;

    private String cajero;

    private String username;

    private String sucursal;

    private String sucursalCodigo;

    private String estado;

    private Integer partidas;

    private Integer unidades;

    private BigDecimal total;

    private List<TicketDetalleResponse> detalles =
            new ArrayList<>();

    public TicketCorteResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id) {

        this.id = id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(
            LocalDateTime fechaHora) {

        this.fechaHora = fechaHora;
    }

    public String getCajero() {
        return cajero;
    }

    public void setCajero(
            String cajero) {

        this.cajero = cajero;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(
            String username) {

        this.username = username;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(
            String sucursal) {

        this.sucursal = sucursal;
    }

    public String getSucursalCodigo() {
        return sucursalCodigo;
    }

    public void setSucursalCodigo(
            String sucursalCodigo) {

        this.sucursalCodigo = sucursalCodigo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(
            String estado) {

        this.estado = estado;
    }

    public Integer getPartidas() {
        return partidas;
    }

    public void setPartidas(
            Integer partidas) {

        this.partidas = partidas;
    }

    public Integer getUnidades() {
        return unidades;
    }

    public void setUnidades(
            Integer unidades) {

        this.unidades = unidades;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(
            BigDecimal total) {

        this.total = total;
    }

    public List<TicketDetalleResponse> getDetalles() {
        return detalles;
    }

    public void setDetalles(
            List<TicketDetalleResponse> detalles) {

        this.detalles =
                detalles != null
                        ? detalles
                        : new ArrayList<>();
    }
}
