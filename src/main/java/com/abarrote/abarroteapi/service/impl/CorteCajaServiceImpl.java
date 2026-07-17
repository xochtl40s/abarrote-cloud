package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.CorteCajaResponse;
import com.abarrote.abarroteapi.dto.ResumenCajeroResponse;
import com.abarrote.abarroteapi.dto.ResumenSucursalCorteResponse;
import com.abarrote.abarroteapi.dto.TicketCorteResponse;
import com.abarrote.abarroteapi.dto.TicketDetalleResponse;
import com.abarrote.abarroteapi.entity.DetalleVenta;
import com.abarrote.abarroteapi.entity.Venta;
import com.abarrote.abarroteapi.repository.VentaRepository;
import com.abarrote.abarroteapi.service.CorteCajaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CorteCajaServiceImpl
        implements CorteCajaService {

    private final VentaRepository ventaRepository;

    public CorteCajaServiceImpl(
            VentaRepository ventaRepository) {

        this.ventaRepository =
                ventaRepository;
    }

    @Override
    public CorteCajaResponse generarCorte(
            LocalDate fecha) {

        LocalDate fechaSegura =
                fecha != null
                        ? fecha
                        : LocalDate.now();

        List<Venta> ventas =
                ventaRepository
                        .buscarCorteConDetalle(
                                fechaSegura.atStartOfDay(),
                                fechaSegura.atTime(
                                        LocalTime.MAX
                                )
                        );

        List<TicketCorteResponse> tickets =
                ventas.stream()
                        .map(this::convertirTicket)
                        .sorted(
                                Comparator.comparing(
                                        TicketCorteResponse
                                                ::getFechaHora
                                ).reversed()
                        )
                        .toList();

        List<TicketCorteResponse> completados =
                tickets.stream()
                        .filter(
                                ticket ->
                                        "COMPLETADA".equals(
                                                ticket.getEstado()
                                        )
                        )
                        .toList();

        BigDecimal total =
                completados.stream()
                        .map(
                                TicketCorteResponse
                                        ::getTotal
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        int unidades =
                completados.stream()
                        .mapToInt(
                                ticket ->
                                        ticket.getUnidades() != null
                                                ? ticket.getUnidades()
                                                : 0
                        )
                        .sum();

        BigDecimal promedio =
                completados.isEmpty()
                        ? BigDecimal.ZERO
                        : total.divide(
                                BigDecimal.valueOf(
                                        completados.size()
                                ),
                                2,
                                RoundingMode.HALF_UP
                        );

        CorteCajaResponse corte =
                new CorteCajaResponse();

        corte.setFecha(
                fechaSegura
        );

        corte.setTickets(
                tickets
        );

        corte.setNumeroVentas(
                completados.size()
        );

        corte.setUnidadesVendidas(
                unidades
        );

        corte.setTotalVentas(
                total
        );

        corte.setTicketPromedio(
                promedio
        );

        corte.setCajeros(
                construirResumenCajeros(
                        completados
                )
        );

        corte.setSucursales(
                construirResumenSucursales(
                        completados
                )
        );

        return corte;
    }

    private TicketCorteResponse convertirTicket(
            Venta venta) {

        TicketCorteResponse ticket =
                new TicketCorteResponse();

        ticket.setId(
                venta.getId()
        );

        ticket.setFechaHora(
                venta.getFechaHora()
        );

        ticket.setTotal(
                venta.getTotal() != null
                        ? venta.getTotal()
                        : BigDecimal.ZERO
        );

        ticket.setEstado(
                venta.getEstado() != null
                        ? venta.getEstado().name()
                        : "SIN_ESTADO"
        );

        if (venta.getUsuario() != null) {

            ticket.setCajero(
                    venta.getUsuario().getNombre()
            );

            ticket.setUsername(
                    venta.getUsuario().getUsername()
            );

        } else {

            ticket.setCajero(
                    "Sin cajero"
            );

            ticket.setUsername(
                    "N/A"
            );
        }

        if (venta.getSucursal() != null) {

            ticket.setSucursal(
                    venta.getSucursal().getNombre()
            );

            ticket.setSucursalCodigo(
                    venta.getSucursal().getCodigo()
            );

        } else {

            ticket.setSucursal(
                    "Sin sucursal"
            );

            ticket.setSucursalCodigo(
                    "N/A"
            );
        }

        List<TicketDetalleResponse> detalles =
                new ArrayList<>();

        int unidades = 0;

        for (DetalleVenta detalle
                : venta.getDetalles()) {

            int cantidad =
                    detalle.getCantidad() != null
                            ? detalle.getCantidad()
                            : 0;

            unidades += cantidad;

            detalles.add(
                    new TicketDetalleResponse(
                            detalle.getProducto() != null
                                    ? detalle
                                            .getProducto()
                                            .getNombre()
                                    : "Producto no disponible",
                            cantidad,
                            detalle.getPrecioUnitario() != null
                                    ? detalle.getPrecioUnitario()
                                    : BigDecimal.ZERO,
                            detalle.getSubtotal() != null
                                    ? detalle.getSubtotal()
                                    : BigDecimal.ZERO
                    )
            );
        }

        ticket.setDetalles(
                detalles
        );

        ticket.setPartidas(
                detalles.size()
        );

        ticket.setUnidades(
                unidades
        );

        return ticket;
    }

    private List<ResumenCajeroResponse>
    construirResumenCajeros(
            List<TicketCorteResponse> tickets) {

        Map<String, Acumulador> resumen =
                new LinkedHashMap<>();

        for (TicketCorteResponse ticket
                : tickets) {

            String clave =
                    ticket.getUsername()
                    + "|"
                    + ticket.getSucursalCodigo();

            Acumulador acumulador =
                    resumen.computeIfAbsent(
                            clave,
                            llave ->
                                    new Acumulador(
                                            ticket.getCajero(),
                                            ticket.getUsername(),
                                            ticket.getSucursal()
                                    )
                    );

            acumulador.tickets++;
            acumulador.unidades +=
                    ticket.getUnidades() != null
                            ? ticket.getUnidades()
                            : 0;

            acumulador.total =
                    acumulador.total.add(
                            ticket.getTotal() != null
                                    ? ticket.getTotal()
                                    : BigDecimal.ZERO
                    );
        }

        return resumen.values()
                .stream()
                .map(
                        acumulador ->
                                new ResumenCajeroResponse(
                                        acumulador.nombre,
                                        acumulador.username,
                                        acumulador.sucursal,
                                        acumulador.tickets,
                                        acumulador.unidades,
                                        acumulador.total
                                )
                )
                .sorted(
                        Comparator.comparing(
                                ResumenCajeroResponse
                                        ::getTotal
                        ).reversed()
                )
                .toList();
    }

    private List<ResumenSucursalCorteResponse>
    construirResumenSucursales(
            List<TicketCorteResponse> tickets) {

        Map<String, AcumuladorSucursal> resumen =
                new LinkedHashMap<>();

        for (TicketCorteResponse ticket
                : tickets) {

            String clave =
                    ticket.getSucursalCodigo();

            AcumuladorSucursal acumulador =
                    resumen.computeIfAbsent(
                            clave,
                            llave ->
                                    new AcumuladorSucursal(
                                            ticket.getSucursalCodigo(),
                                            ticket.getSucursal()
                                    )
                    );

            acumulador.tickets++;
            acumulador.unidades +=
                    ticket.getUnidades() != null
                            ? ticket.getUnidades()
                            : 0;

            acumulador.total =
                    acumulador.total.add(
                            ticket.getTotal() != null
                                    ? ticket.getTotal()
                                    : BigDecimal.ZERO
                    );
        }

        return resumen.values()
                .stream()
                .map(
                        acumulador ->
                                new ResumenSucursalCorteResponse(
                                        acumulador.codigo,
                                        acumulador.sucursal,
                                        acumulador.tickets,
                                        acumulador.unidades,
                                        acumulador.total
                                )
                )
                .sorted(
                        Comparator.comparing(
                                ResumenSucursalCorteResponse
                                        ::getTotal
                        ).reversed()
                )
                .toList();
    }

    private static class Acumulador {

        private final String nombre;

        private final String username;

        private final String sucursal;

        private int tickets;

        private int unidades;

        private BigDecimal total =
                BigDecimal.ZERO;

        private Acumulador(
                String nombre,
                String username,
                String sucursal) {

            this.nombre = nombre;
            this.username = username;
            this.sucursal = sucursal;
        }
    }

    private static class AcumuladorSucursal {

        private final String codigo;

        private final String sucursal;

        private int tickets;

        private int unidades;

        private BigDecimal total =
                BigDecimal.ZERO;

        private AcumuladorSucursal(
                String codigo,
                String sucursal) {

            this.codigo = codigo;
            this.sucursal = sucursal;
        }
    }
}
