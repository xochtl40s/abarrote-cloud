package com.abarrote.abarroteapi.dto;

import java.math.BigDecimal;

public class ResumenSucursalCorteResponse {

    private String codigo;

    private String sucursal;

    private Integer tickets;

    private Integer unidades;

    private BigDecimal total;

    public ResumenSucursalCorteResponse() {
    }

    public ResumenSucursalCorteResponse(
            String codigo,
            String sucursal,
            Integer tickets,
            Integer unidades,
            BigDecimal total) {

        this.codigo = codigo;
        this.sucursal = sucursal;
        this.tickets = tickets;
        this.unidades = unidades;
        this.total = total;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(
            String codigo) {

        this.codigo = codigo;
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
