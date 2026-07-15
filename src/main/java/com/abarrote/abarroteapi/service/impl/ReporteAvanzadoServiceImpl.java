package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.ReporteAvanzadoResponse;
import com.abarrote.abarroteapi.entity.Categoria;
import com.abarrote.abarroteapi.entity.DetalleVenta;
import com.abarrote.abarroteapi.entity.Producto;
import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.entity.Venta;
import com.abarrote.abarroteapi.repository.VentaRepository;
import com.abarrote.abarroteapi.service.ReporteAvanzadoService;
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
public class ReporteAvanzadoServiceImpl
        implements ReporteAvanzadoService {

    private final VentaRepository ventaRepository;

    public ReporteAvanzadoServiceImpl(
            VentaRepository ventaRepository) {

        this.ventaRepository = ventaRepository;
    }

    @Override
    public ReporteAvanzadoResponse generarReporte(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Long usuarioId) {

        validarFechas(fechaInicio, fechaFin);

        List<Venta> ventas;

        if (usuarioId == null) {

            ventas =
                    ventaRepository
                            .findByFechaHoraBetweenOrderByFechaHoraDesc(
                                    fechaInicio.atStartOfDay(),
                                    fechaFin.atTime(LocalTime.MAX)
                            );

        } else {

            ventas =
                    ventaRepository
                            .findByUsuarioIdAndFechaHoraBetweenOrderByFechaHoraDesc(
                                    usuarioId,
                                    fechaInicio.atStartOfDay(),
                                    fechaFin.atTime(LocalTime.MAX)
                            );
        }

        List<Venta> ventasCompletadas =
                ventas.stream()
                        .filter(venta ->
                                venta.getEstado()
                                        == Venta.EstadoVenta.COMPLETADA
                        )
                        .toList();

        ReporteAvanzadoResponse response =
                new ReporteAvanzadoResponse();

        response.setFechaInicio(fechaInicio);
        response.setFechaFin(fechaFin);
        response.setNumeroVentas(ventasCompletadas.size());

        construirResumenGeneral(
                response,
                ventasCompletadas
        );

        response.setVentasPorProducto(
                construirVentasPorProducto(
                        ventasCompletadas
                )
        );

        response.setVentasPorCategoria(
                construirVentasPorCategoria(
                        ventasCompletadas
                )
        );

        response.setVentasPorCajero(
                construirVentasPorCajero(
                        ventasCompletadas
                )
        );

        return response;
    }

    private void construirResumenGeneral(
            ReporteAvanzadoResponse response,
            List<Venta> ventas) {

        BigDecimal ventasNetas =
                ventas.stream()
                        .map(Venta::getTotal)
                        .filter(total -> total != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        int articulosVendidos =
                ventas.stream()
                        .flatMap(venta ->
                                venta.getDetalles().stream()
                        )
                        .map(DetalleVenta::getCantidad)
                        .filter(cantidad -> cantidad != null)
                        .mapToInt(Integer::intValue)
                        .sum();

        BigDecimal costoMercancia =
                ventas.stream()
                        .flatMap(venta ->
                                venta.getDetalles().stream()
                        )
                        .map(this::calcularCostoDetalle)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal beneficioBruto =
                ventasNetas.subtract(costoMercancia);

        BigDecimal margenBruto =
                calcularPorcentaje(
                        beneficioBruto,
                        ventasNetas
                );

        BigDecimal ventaPromedio =
                ventas.isEmpty()
                        ? BigDecimal.ZERO
                        : ventasNetas.divide(
                                BigDecimal.valueOf(
                                        ventas.size()
                                ),
                                2,
                                RoundingMode.HALF_UP
                        );

        response.setArticulosVendidos(
                articulosVendidos
        );

        response.setVentasNetas(
                ventasNetas
        );

        response.setCostoMercancia(
                costoMercancia
        );

        response.setBeneficioBruto(
                beneficioBruto
        );

        response.setMargenBruto(
                margenBruto
        );

        response.setVentaPromedio(
                ventaPromedio
        );
    }

    private List<ReporteAvanzadoResponse.VentaProducto>
    construirVentasPorProducto(
            List<Venta> ventas) {

        Map<Long, AcumuladorProducto> acumuladores =
                new LinkedHashMap<>();

        for (Venta venta : ventas) {

            for (DetalleVenta detalle : venta.getDetalles()) {

                Producto producto =
                        detalle.getProducto();

                if (producto == null
                        || producto.getId() == null) {
                    continue;
                }

                AcumuladorProducto acumulador =
                        acumuladores.computeIfAbsent(
                                producto.getId(),
                                id ->
                                        new AcumuladorProducto(
                                                producto
                                        )
                        );

                acumulador.agregar(detalle);
            }
        }

        List<ReporteAvanzadoResponse.VentaProducto> resultado =
                new ArrayList<>();

        for (AcumuladorProducto acumulador
                : acumuladores.values()) {

            resultado.add(
                    acumulador.convertir()
            );
        }

        resultado.sort(
                Comparator.comparing(
                        ReporteAvanzadoResponse
                                .VentaProducto
                                ::getVentasNetas
                ).reversed()
        );

        return resultado;
    }

    private List<ReporteAvanzadoResponse.VentaCategoria>
    construirVentasPorCategoria(
            List<Venta> ventas) {

        Map<String, AcumuladorCategoria> acumuladores =
                new LinkedHashMap<>();

        for (Venta venta : ventas) {

            for (DetalleVenta detalle : venta.getDetalles()) {

                Producto producto =
                        detalle.getProducto();

                Categoria categoria =
                        producto != null
                                ? producto.getCategoria()
                                : null;

                String clave =
                        categoria != null
                                && categoria.getId() != null
                                ? "ID-" + categoria.getId()
                                : "SIN-CATEGORIA";

                AcumuladorCategoria acumulador =
                        acumuladores.computeIfAbsent(
                                clave,
                                id ->
                                        new AcumuladorCategoria(
                                                categoria
                                        )
                        );

                acumulador.agregar(detalle);
            }
        }

        List<ReporteAvanzadoResponse.VentaCategoria> resultado =
                new ArrayList<>();

        for (AcumuladorCategoria acumulador
                : acumuladores.values()) {

            resultado.add(
                    acumulador.convertir()
            );
        }

        resultado.sort(
                Comparator.comparing(
                        ReporteAvanzadoResponse
                                .VentaCategoria
                                ::getVentasNetas
                ).reversed()
        );

        return resultado;
    }

    private List<ReporteAvanzadoResponse.VentaCajero>
    construirVentasPorCajero(
            List<Venta> ventas) {

        Map<Long, AcumuladorCajero> acumuladores =
                new LinkedHashMap<>();

        for (Venta venta : ventas) {

            Usuario usuario =
                    venta.getUsuario();

            if (usuario == null
                    || usuario.getId() == null) {
                continue;
            }

            AcumuladorCajero acumulador =
                    acumuladores.computeIfAbsent(
                            usuario.getId(),
                            id ->
                                    new AcumuladorCajero(
                                            usuario
                                    )
                    );

            acumulador.agregar(venta);
        }

        List<ReporteAvanzadoResponse.VentaCajero> resultado =
                new ArrayList<>();

        for (AcumuladorCajero acumulador
                : acumuladores.values()) {

            resultado.add(
                    acumulador.convertir()
            );
        }

        resultado.sort(
                Comparator.comparing(
                        ReporteAvanzadoResponse
                                .VentaCajero
                                ::getVentasNetas
                ).reversed()
        );

        return resultado;
    }

    private BigDecimal calcularCostoDetalle(
            DetalleVenta detalle) {

        if (detalle == null
                || detalle.getProducto() == null
                || detalle.getCantidad() == null) {

            return BigDecimal.ZERO;
        }

        BigDecimal precioCompra =
                detalle.getProducto()
                        .getPrecioCompra();

        if (precioCompra == null) {
            return BigDecimal.ZERO;
        }

        return precioCompra.multiply(
                BigDecimal.valueOf(
                        detalle.getCantidad()
                )
        );
    }

    private BigDecimal obtenerSubtotal(
            DetalleVenta detalle) {

        if (detalle == null
                || detalle.getSubtotal() == null) {

            return BigDecimal.ZERO;
        }

        return detalle.getSubtotal();
    }

    private BigDecimal calcularPorcentaje(
            BigDecimal numerador,
            BigDecimal denominador) {

        if (numerador == null
                || denominador == null
                || denominador.compareTo(
                        BigDecimal.ZERO
                ) == 0) {

            return BigDecimal.ZERO;
        }

        return numerador
                .multiply(
                        BigDecimal.valueOf(100)
                )
                .divide(
                        denominador,
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private void validarFechas(
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        if (fechaInicio == null
                || fechaFin == null) {

            throw new RuntimeException(
                    "Las fechas inicial y final son obligatorias"
            );
        }

        if (fechaInicio.isAfter(fechaFin)) {

            throw new RuntimeException(
                    "La fecha inicial no puede ser posterior a la fecha final"
            );
        }
    }

    private class AcumuladorProducto {

        private final Long productoId;
        private final String producto;
        private final String categoria;

        private int cantidad;

        private BigDecimal ventas =
                BigDecimal.ZERO;

        private BigDecimal costo =
                BigDecimal.ZERO;

        private AcumuladorProducto(
                Producto producto) {

            this.productoId =
                    producto.getId();

            this.producto =
                    producto.getNombre();

            this.categoria =
                    producto.getCategoria() != null
                            ? producto
                            .getCategoria()
                            .getNombre()
                            : "Sin categoría";
        }

        private void agregar(
                DetalleVenta detalle) {

            cantidad +=
                    detalle.getCantidad() != null
                            ? detalle.getCantidad()
                            : 0;

            ventas =
                    ventas.add(
                            obtenerSubtotal(detalle)
                    );

            costo =
                    costo.add(
                            calcularCostoDetalle(detalle)
                    );
        }

        private ReporteAvanzadoResponse.VentaProducto
        convertir() {

            ReporteAvanzadoResponse.VentaProducto item =
                    new ReporteAvanzadoResponse.VentaProducto();

            BigDecimal beneficio =
                    ventas.subtract(costo);

            item.setProductoId(productoId);
            item.setProducto(producto);
            item.setCategoria(categoria);
            item.setArticulosVendidos(cantidad);
            item.setVentasNetas(ventas);
            item.setCostoMercancia(costo);
            item.setBeneficioBruto(beneficio);

            item.setMargenBruto(
                    calcularPorcentaje(
                            beneficio,
                            ventas
                    )
            );

            return item;
        }
    }

    private class AcumuladorCategoria {

        private final Long categoriaId;
        private final String categoria;

        private int cantidad;

        private BigDecimal ventas =
                BigDecimal.ZERO;

        private BigDecimal costo =
                BigDecimal.ZERO;

        private AcumuladorCategoria(
                Categoria categoria) {

            this.categoriaId =
                    categoria != null
                            ? categoria.getId()
                            : null;

            this.categoria =
                    categoria != null
                            ? categoria.getNombre()
                            : "Sin categoría";
        }

        private void agregar(
                DetalleVenta detalle) {

            cantidad +=
                    detalle.getCantidad() != null
                            ? detalle.getCantidad()
                            : 0;

            ventas =
                    ventas.add(
                            obtenerSubtotal(detalle)
                    );

            costo =
                    costo.add(
                            calcularCostoDetalle(detalle)
                    );
        }

        private ReporteAvanzadoResponse.VentaCategoria
        convertir() {

            ReporteAvanzadoResponse.VentaCategoria item =
                    new ReporteAvanzadoResponse.VentaCategoria();

            BigDecimal beneficio =
                    ventas.subtract(costo);

            item.setCategoriaId(categoriaId);
            item.setCategoria(categoria);
            item.setArticulosVendidos(cantidad);
            item.setVentasNetas(ventas);
            item.setCostoMercancia(costo);
            item.setBeneficioBruto(beneficio);

            item.setMargenBruto(
                    calcularPorcentaje(
                            beneficio,
                            ventas
                    )
            );

            return item;
        }
    }

    private class AcumuladorCajero {

        private final Long usuarioId;
        private final String cajero;

        private int ventasRealizadas;
        private int articulosVendidos;

        private BigDecimal ventas =
                BigDecimal.ZERO;

        private BigDecimal costo =
                BigDecimal.ZERO;

        private AcumuladorCajero(
                Usuario usuario) {

            this.usuarioId =
                    usuario.getId();

            this.cajero =
                    usuario.getNombre();
        }

        private void agregar(
                Venta venta) {

            ventasRealizadas++;

            ventas =
                    ventas.add(
                            venta.getTotal() != null
                                    ? venta.getTotal()
                                    : BigDecimal.ZERO
                    );

            for (DetalleVenta detalle
                    : venta.getDetalles()) {

                articulosVendidos +=
                        detalle.getCantidad() != null
                                ? detalle.getCantidad()
                                : 0;

                costo =
                        costo.add(
                                calcularCostoDetalle(detalle)
                        );
            }
        }

        private ReporteAvanzadoResponse.VentaCajero
        convertir() {

            ReporteAvanzadoResponse.VentaCajero item =
                    new ReporteAvanzadoResponse.VentaCajero();

            BigDecimal beneficio =
                    ventas.subtract(costo);

            BigDecimal promedio =
                    ventasRealizadas == 0
                            ? BigDecimal.ZERO
                            : ventas.divide(
                                    BigDecimal.valueOf(
                                            ventasRealizadas
                                    ),
                                    2,
                                    RoundingMode.HALF_UP
                            );

            item.setUsuarioId(usuarioId);
            item.setCajero(cajero);
            item.setNumeroVentas(ventasRealizadas);
            item.setArticulosVendidos(
                    articulosVendidos
            );
            item.setVentasNetas(ventas);
            item.setCostoMercancia(costo);
            item.setBeneficioBruto(beneficio);
            item.setVentaPromedio(promedio);

            return item;
        }
    }
}
