package com.abarrote.abarroteapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CorteCajaResponse {

    private LocalDate fecha;

    private BigDecimal totalVentas =
            BigDecimal.ZERO;

    private Integer numeroVentas = 0;

    private Integer unidadesVendidas = 0;

    private BigDecimal ticketPromedio =
            BigDecimal.ZERO;

    private List<TicketCorteResponse> tickets =
            new ArrayList<>();

    private List<ResumenCajeroResponse> cajeros =
            new ArrayList<>();

    private List<ResumenSucursalCorteResponse> sucursales =
            new ArrayList<>();

    public CorteCajaResponse() {
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(
            LocalDate fecha) {

        this.fecha = fecha;
    }

    public BigDecimal getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(
            BigDecimal totalVentas) {

        this.totalVentas =
                totalVentas != null
                        ? totalVentas
                        : BigDecimal.ZERO;
    }

    public Integer getNumeroVentas() {
        return numeroVentas;
    }

    public void setNumeroVentas(
            Integer numeroVentas) {

        this.numeroVentas =
                numeroVentas != null
                        ? numeroVentas
                        : 0;
    }

    public Integer getUnidadesVendidas() {
        return unidadesVendidas;
    }

    public void setUnidadesVendidas(
            Integer unidadesVendidas) {

        this.unidadesVendidas =
                unidadesVendidas != null
                        ? unidadesVendidas
                        : 0;
    }

    public BigDecimal getTicketPromedio() {
        return ticketPromedio;
    }

    public void setTicketPromedio(
            BigDecimal ticketPromedio) {

        this.ticketPromedio =
                ticketPromedio != null
                        ? ticketPromedio
                        : BigDecimal.ZERO;
    }

    public List<TicketCorteResponse> getTickets() {
        return tickets;
    }

    public void setTickets(
            List<TicketCorteResponse> tickets) {

        this.tickets =
                tickets != null
                        ? tickets
                        : new ArrayList<>();
    }

    public List<ResumenCajeroResponse> getCajeros() {
        return cajeros;
    }

    public void setCajeros(
            List<ResumenCajeroResponse> cajeros) {

        this.cajeros =
                cajeros != null
                        ? cajeros
                        : new ArrayList<>();
    }

    public List<ResumenSucursalCorteResponse> getSucursales() {
        return sucursales;
    }

    public void setSucursales(
            List<ResumenSucursalCorteResponse> sucursales) {

        this.sucursales =
                sucursales != null
                        ? sucursales
                        : new ArrayList<>();
    }
}
