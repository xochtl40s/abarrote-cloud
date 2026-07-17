package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.ReporteVentasRequest;
import com.abarrote.abarroteapi.dto.ReporteVentasResponse;
import com.abarrote.abarroteapi.entity.Venta;
import com.abarrote.abarroteapi.repository.VentaRepository;
import com.abarrote.abarroteapi.service.ReporteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final VentaRepository ventaRepository;

    public ReporteServiceImpl(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Override
    public ReporteVentasResponse generarReporteVentas(ReporteVentasRequest request) {
        LocalDateTime inicio = request.getFechaInicio().atStartOfDay();
        LocalDateTime fin = request.getFechaFin().atTime(LocalTime.MAX);

        List<Venta> ventas = request.getUsuarioId() != null
                ? ventaRepository.findByUsuarioIdAndFechaHoraBetweenOrderByFechaHoraDesc(request.getUsuarioId(), inicio, fin)
                : ventaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(inicio, fin);

        List<Venta> ventasCompletadas = ventas.stream()
                .filter(v -> v.getEstado() == Venta.EstadoVenta.COMPLETADA)
                .collect(Collectors.toList());

        ReporteVentasResponse response = new ReporteVentasResponse();
        response.setFechaInicio(request.getFechaInicio());
        response.setFechaFin(request.getFechaFin());
        response.setTotalVentas(ventasCompletadas.size());
        response.setMontoTotal(ventasCompletadas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // ===== RESUMEN POR CAJERO CON PRODUCTOS =====
        Map<String, List<Venta>> ventasPorCajero = ventasCompletadas.stream()
                .collect(Collectors.groupingBy(v -> v.getUsuario() != null ? v.getUsuario().getNombre() : "N/A"));

        List<ReporteVentasResponse.ResumenCajero> resumenCajeros = ventasPorCajero.entrySet().stream()
                .map(e -> {
                    ReporteVentasResponse.ResumenCajero rc = new ReporteVentasResponse.ResumenCajero();
                    rc.setCajero(e.getKey());
                    rc.setCantidadVentas(e.getValue().size());
                    rc.setMontoTotal(e.getValue().stream()
                            .map(Venta::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));

                    // Agrupar productos vendidos por este cajero
                    Map<String, ReporteVentasResponse.ProductoCajero> productosMap = e.getValue().stream()
                            .flatMap(v -> v.getDetalles().stream())
                            .collect(Collectors.toMap(
                                    d -> d.getProducto().getNombre(),
                                    d -> {
                                        ReporteVentasResponse.ProductoCajero pc = new ReporteVentasResponse.ProductoCajero();
                                        pc.setNombreProducto(d.getProducto().getNombre());
                                        pc.setCantidad(d.getCantidad());
                                        pc.setMonto(d.getSubtotal());
                                        return pc;
                                    },
                                    (existing, replacement) -> {
                                        existing.setCantidad(existing.getCantidad() + replacement.getCantidad());
                                        existing.setMonto(existing.getMonto().add(replacement.getMonto()));
                                        return existing;
                                    }
                            ));

                    List<ReporteVentasResponse.ProductoCajero> productosOrdenados = productosMap.values().stream()
                            .sorted((a, b) -> b.getCantidad().compareTo(a.getCantidad()))
                            .collect(Collectors.toList());

                    rc.setProductosVendidos(productosOrdenados);
                    return rc;
                })
                .sorted((a, b) -> b.getMontoTotal().compareTo(a.getMontoTotal()))
                .collect(Collectors.toList());
        response.setResumenPorCajero(resumenCajeros);

        List<ReporteVentasResponse.VentaResumen> resumen = ventasCompletadas.stream()
                .map(v -> {
                    ReporteVentasResponse.VentaResumen vr = new ReporteVentasResponse.VentaResumen();
                    vr.setVentaId(v.getId());
                    vr.setFecha(v.getFechaHora().toLocalDate().toString());
                    vr.setCajero(v.getUsuario() != null ? v.getUsuario().getNombre() : "N/A");
                    vr.setTotal(v.getTotal());
                    vr.setCantidadProductos(v.getDetalles().size());
                    return vr;
                }).collect(Collectors.toList());
        response.setVentas(resumen);

        Map<String, Integer> cantidadPorProducto = ventasCompletadas.stream()
                .flatMap(v -> v.getDetalles().stream())
                .collect(Collectors.groupingBy(
                        d -> d.getProducto().getNombre(),
                        Collectors.summingInt(com.abarrote.abarroteapi.entity.DetalleVenta::getCantidad)
                ));

        Map<String, BigDecimal> montoPorProducto = ventasCompletadas.stream()
                .flatMap(v -> v.getDetalles().stream())
                .collect(Collectors.groupingBy(
                        d -> d.getProducto().getNombre(),
                        Collectors.reducing(BigDecimal.ZERO, com.abarrote.abarroteapi.entity.DetalleVenta::getSubtotal, BigDecimal::add)
                ));

        List<ReporteVentasResponse.ProductoTop> topProductos = cantidadPorProducto.entrySet().stream()
                .map(e -> {
                    ReporteVentasResponse.ProductoTop pt = new ReporteVentasResponse.ProductoTop();
                    pt.setNombre(e.getKey());
                    pt.setCantidadVendida(e.getValue());
                    pt.setMontoTotal(montoPorProducto.get(e.getKey()));
                    return pt;
                })
                .sorted((a, b) -> b.getCantidadVendida().compareTo(a.getCantidadVendida()))
                .limit(10)
                .collect(Collectors.toList());
        response.setProductosMasVendidos(topProductos);

        return response;
    }

    @Override
    public BigDecimal obtenerTotalVentasDelDia(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);
        BigDecimal total = ventaRepository.sumTotalVentasPorFechaHora(inicio, fin);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal obtenerTotalVentasDelMes(int year, int month) {
        LocalDate inicio = LocalDate.of(year, month, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        BigDecimal total = ventaRepository.sumTotalVentasPorFechaHora(inicio.atStartOfDay(), fin.atTime(LocalTime.MAX));
        return total != null ? total : BigDecimal.ZERO;
    }
}
