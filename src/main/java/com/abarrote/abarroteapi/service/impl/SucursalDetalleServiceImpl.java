package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.SucursalDetalleResponse;
import com.abarrote.abarroteapi.entity.InventarioSucursal;
import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.entity.Venta;
import com.abarrote.abarroteapi.repository.InventarioSucursalRepository;
import com.abarrote.abarroteapi.repository.SucursalRepository;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import com.abarrote.abarroteapi.repository.VentaRepository;
import com.abarrote.abarroteapi.service.SucursalDetalleService;
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
public class SucursalDetalleServiceImpl
        implements SucursalDetalleService {

    private final SucursalRepository sucursalRepository;

    private final InventarioSucursalRepository
            inventarioSucursalRepository;

    private final UsuarioRepository usuarioRepository;

    private final VentaRepository ventaRepository;

    public SucursalDetalleServiceImpl(
            SucursalRepository sucursalRepository,
            InventarioSucursalRepository inventarioSucursalRepository,
            UsuarioRepository usuarioRepository,
            VentaRepository ventaRepository) {

        this.sucursalRepository = sucursalRepository;

        this.inventarioSucursalRepository =
                inventarioSucursalRepository;

        this.usuarioRepository = usuarioRepository;
        this.ventaRepository = ventaRepository;
    }

    @Override
    public SucursalDetalleResponse obtenerDetalle(
            Long sucursalId) {

        Sucursal sucursal =
                sucursalRepository
                        .findById(sucursalId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "La sucursal solicitada no existe"
                                )
                        );

        LocalDate hoy =
                LocalDate.now();

        List<InventarioSucursal> inventarios =
                inventarioSucursalRepository
                        .findBySucursalIdOrderByProductoNombreAsc(
                                sucursalId
                        );

        List<Venta> ventas =
                ventaRepository
                        .findBySucursalIdAndFechaHoraBetweenOrderByFechaHoraDesc(
                                sucursalId,
                                hoy.minusDays(29).atStartOfDay(),
                                hoy.atTime(LocalTime.MAX)
                        )
                        .stream()
                        .filter(
                                venta ->
                                        venta.getEstado()
                                                == Venta.EstadoVenta.COMPLETADA
                        )
                        .toList();

        List<Usuario> usuariosSucursal =
                usuarioRepository
                        .findAll()
                        .stream()
                        .filter(
                                usuario ->
                                        usuario.getSucursal() != null
                                                && usuario
                                                .getSucursal()
                                                .getId()
                                                .equals(sucursalId)
                        )
                        .sorted(
                                Comparator.comparing(
                                        Usuario::getNombre,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .toList();

        SucursalDetalleResponse response =
                new SucursalDetalleResponse();

        response.setSucursalId(
                sucursal.getId()
        );

        response.setCodigo(
                sucursal.getCodigo()
        );

        response.setNombre(
                sucursal.getNombre()
        );

        response.setDireccion(
                sucursal.getDireccion()
        );

        response.setTelefono(
                sucursal.getTelefono()
        );

        response.setActiva(
                sucursal.getActiva()
        );

        construirInventario(
                response,
                inventarios
        );

        construirVentasGenerales(
                response,
                ventas
        );

        response.setCajeros(
                construirCajeros(
                        usuariosSucursal,
                        ventas
                )
        );

        return response;
    }

    private void construirInventario(
            SucursalDetalleResponse response,
            List<InventarioSucursal> inventarios) {

        List<SucursalDetalleResponse.ProductoStock>
                productos = new ArrayList<>();

        int unidadesTotales = 0;

        BigDecimal valorInventario =
                BigDecimal.ZERO;

        for (InventarioSucursal inventario
                : inventarios) {

            int existencia =
                    inventario.getExistencia() != null
                            ? inventario.getExistencia()
                            : 0;

            BigDecimal precioVenta =
                    inventario
                    .getProducto()
                    .getPrecioVenta() != null
                            ? inventario
                            .getProducto()
                            .getPrecioVenta()
                            : BigDecimal.ZERO;

            BigDecimal valorProducto =
                    precioVenta.multiply(
                            BigDecimal.valueOf(
                                    existencia
                            )
                    );

            SucursalDetalleResponse.ProductoStock item =
                    new SucursalDetalleResponse.ProductoStock();

            item.setProductoId(
                    inventario.getProducto().getId()
            );

            item.setCodigoBarras(
                    inventario
                            .getProducto()
                            .getCodigoBarras()
            );

            item.setProducto(
                    inventario.getProducto().getNombre()
            );

            item.setExistencia(existencia);

            item.setStockMinimo(
                    inventario.getStockMinimo() != null
                            ? inventario.getStockMinimo()
                            : 0
            );

            item.setPrecioVenta(precioVenta);
            item.setValorInventario(valorProducto);

            productos.add(item);

            unidadesTotales += existencia;

            valorInventario =
                    valorInventario.add(
                            valorProducto
                    );
        }

        response.setProductos(productos);
        response.setProductosDiferentes(
                productos.size()
        );
        response.setUnidadesTotales(
                unidadesTotales
        );
        response.setValorInventario(
                valorInventario
        );
    }

    private void construirVentasGenerales(
            SucursalDetalleResponse response,
            List<Venta> ventas) {

        BigDecimal totalVentas =
                ventas.stream()
                        .map(Venta::getTotal)
                        .filter(total -> total != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal promedio =
                ventas.isEmpty()
                        ? BigDecimal.ZERO
                        : totalVentas.divide(
                                BigDecimal.valueOf(
                                        ventas.size()
                                ),
                                2,
                                RoundingMode.HALF_UP
                        );

        response.setNumeroVentas(
                ventas.size()
        );

        response.setTotalVentas(
                totalVentas
        );

        response.setVentaPromedio(
                promedio
        );
    }

    private List<SucursalDetalleResponse.CajeroEstadistica>
    construirCajeros(
            List<Usuario> usuarios,
            List<Venta> ventas) {

        Map<Long, List<Venta>> ventasPorUsuario =
                new LinkedHashMap<>();

        for (Venta venta : ventas) {

            if (venta.getUsuario() == null
                    || venta.getUsuario().getId() == null) {

                continue;
            }

            ventasPorUsuario
                    .computeIfAbsent(
                            venta.getUsuario().getId(),
                            id -> new ArrayList<>()
                    )
                    .add(venta);
        }

        List<SucursalDetalleResponse.CajeroEstadistica>
                resultado = new ArrayList<>();

        for (Usuario usuario : usuarios) {

            List<Venta> ventasUsuario =
                    ventasPorUsuario.getOrDefault(
                            usuario.getId(),
                            List.of()
                    );

            BigDecimal total =
                    ventasUsuario.stream()
                            .map(Venta::getTotal)
                            .filter(valor -> valor != null)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            BigDecimal promedio =
                    ventasUsuario.isEmpty()
                            ? BigDecimal.ZERO
                            : total.divide(
                                    BigDecimal.valueOf(
                                            ventasUsuario.size()
                                    ),
                                    2,
                                    RoundingMode.HALF_UP
                            );

            SucursalDetalleResponse.CajeroEstadistica item =
                    new SucursalDetalleResponse.CajeroEstadistica();

            item.setUsuarioId(
                    usuario.getId()
            );

            item.setNombre(
                    usuario.getNombre()
            );

            item.setUsername(
                    usuario.getUsername()
            );

            item.setRol(
                    usuario.getRol()
            );

            item.setActivo(
                    usuario.getActivo()
            );

            item.setNumeroVentas(
                    ventasUsuario.size()
            );

            item.setTotalVentas(total);
            item.setVentaPromedio(promedio);

            resultado.add(item);
        }

        resultado.sort(
                Comparator.comparing(
                        SucursalDetalleResponse
                                .CajeroEstadistica
                                ::getTotalVentas
                ).reversed()
        );

        return resultado;
    }
}
