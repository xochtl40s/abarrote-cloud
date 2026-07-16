package com.abarrote.abarroteapi.dto;

import java.math.BigDecimal;

public class ResumenCajeroResponse {

    private String cajero;

    private String username;

    private String sucursal;

    private Integer tickets;

    private Integer unidades;

    private BigDecimal total;

    public ResumenCajeroResponse() {
    }

    public ResumenCajeroResponse(
            String cajero,
            String username,
            String sucursal,
            Integer tickets,
            Integer unidades,
            BigDecimal total) {

        this.cajero = cajero;
        this.username = username;
        this.sucursal = sucursal;
        this.tickets = tickets;
        this.unidades = unidades;
        this.total = total;
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

    public Integer getTickets() {
        return tickets;
    }

    public void setTickets(
            Integer tickets) {

        this.tickets = tickets;
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
}
