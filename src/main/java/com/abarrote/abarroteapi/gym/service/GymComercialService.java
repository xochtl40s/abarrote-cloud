package com.abarrote.abarroteapi.gym.service;

import com.abarrote.abarroteapi.gym.domain.GymProducto;
import com.abarrote.abarroteapi.gym.domain.GymVenta;
import com.abarrote.abarroteapi.gym.domain.GymVentaDetalle;
import com.abarrote.abarroteapi.gym.dto.GymVentaRequest;
import com.abarrote.abarroteapi.gym.dto.GymVentaResponse;
import com.abarrote.abarroteapi.gym.repository.GymProductoRepository;
import com.abarrote.abarroteapi.gym.repository.GymVentaRepository;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class GymComercialService {

    private final GymProductoRepository productoRepository;

    private final GymVentaRepository ventaRepository;

    private final TenantRepository tenantRepository;

    public GymComercialService(
        GymProductoRepository productoRepository,
        GymVentaRepository ventaRepository,
        TenantRepository tenantRepository
    ) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.tenantRepository = tenantRepository;
    }

    public GymProducto crearProducto(
        Long tenantId,
        String codigo,
        String nombre,
        String descripcion,
        String categoria,
        BigDecimal precio,
        BigDecimal costo,
        Integer existencia,
        Integer stockMinimo
    ) {
        Tenant tenant = obtenerTenant(tenantId);

        String codigoNormalizado =
            codigo.trim().toUpperCase(Locale.ROOT);

        if (
            productoRepository
                .existsByTenantIdAndCodigoIgnoreCase(
                    tenantId,
                    codigoNormalizado
                )
        ) {
            throw new IllegalArgumentException(
                "Ya existe un producto con el código "
                    + codigoNormalizado
            );
        }

        GymProducto producto = new GymProducto();

        producto.setTenant(tenant);
        producto.setCodigo(codigoNormalizado);
        producto.setNombre(nombre.trim());
        producto.setDescripcion(descripcion);
        producto.setCategoria(categoria);
        producto.setPrecio(precio);
        producto.setCosto(costo);
        producto.setExistencia(
            existencia != null ? existencia : 0
        );
        producto.setStockMinimo(
            stockMinimo != null ? stockMinimo : 0
        );
        producto.setActivo(true);

        return productoRepository.save(producto);
    }

    public GymProducto actualizarExistencia(
        Long tenantId,
        Long productoId,
        Integer nuevaExistencia
    ) {
        if (nuevaExistencia == null || nuevaExistencia < 0) {
            throw new IllegalArgumentException(
                "La existencia no puede ser negativa"
            );
        }

        GymProducto producto = productoRepository
            .findByIdAndTenantId(
                productoId,
                tenantId
            )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Producto no encontrado"
                )
            );

        producto.setExistencia(nuevaExistencia);

        return productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public List<GymProducto> listarProductos(
        Long tenantId
    ) {
        return productoRepository
            .findAllByTenantIdAndActivoTrueOrderByNombreAsc(
                tenantId
            );
    }

    public GymVentaResponse registrarVenta(
        Long tenantId,
        String username,
        GymVentaRequest request
    ) {
        Tenant tenant = obtenerTenant(tenantId);

        if (
            request.productos() == null
                || request.productos().isEmpty()
        ) {
            throw new IllegalArgumentException(
                "La venta no contiene productos"
            );
        }

        String metodoPago = request
            .metodoPago()
            .trim()
            .toUpperCase(Locale.ROOT);

        if (
            !List.of(
                "EFECTIVO",
                "TARJETA",
                "TRANSFERENCIA",
                "OTRO"
            ).contains(metodoPago)
        ) {
            throw new IllegalArgumentException(
                "Método de pago no válido"
            );
        }

        GymVenta venta = new GymVenta();

        venta.setTenant(tenant);
        venta.setUsuarioUsername(username);
        venta.setMetodoPago(metodoPago);

        BigDecimal total = BigDecimal.ZERO;

        for (GymVentaRequest.Item item
            : request.productos()) {

            GymProducto producto =
                productoRepository
                    .findByIdAndTenantId(
                        item.productoId(),
                        tenantId
                    )
                    .orElseThrow(
                        () ->
                            new IllegalArgumentException(
                                "Producto no encontrado: "
                                    + item.productoId()
                            )
                    );

            int cantidad = item.cantidad();

            if (cantidad <= 0) {
                throw new IllegalArgumentException(
                    "Cantidad no válida"
                );
            }

            if (producto.getExistencia() < cantidad) {
                throw new IllegalArgumentException(
                    "Existencia insuficiente para "
                        + producto.getNombre()
                );
            }

            BigDecimal subtotal =
                producto
                    .getPrecio()
                    .multiply(
                        BigDecimal.valueOf(cantidad)
                    );

            GymVentaDetalle detalle =
                new GymVentaDetalle();

            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(
                producto.getPrecio()
            );
            detalle.setSubtotal(subtotal);

            venta.agregarDetalle(detalle);

            producto.setExistencia(
                producto.getExistencia() - cantidad
            );

            productoRepository.save(producto);

            total = total.add(subtotal);
        }

        venta.setTotal(total);

        GymVenta guardada =
            ventaRepository.save(venta);

        return new GymVentaResponse(
            guardada.getId(),
            guardada.getTotal(),
            guardada.getMetodoPago(),
            guardada.getFechaVenta()
        );
    }

    @Transactional(readOnly = true)
    public List<GymVenta> listarVentas(
        Long tenantId
    ) {
        return ventaRepository
            .findTop50ByTenantIdOrderByFechaVentaDesc(
                tenantId
            );
    }

    private Tenant obtenerTenant(Long tenantId) {
        return tenantRepository
            .findById(tenantId)
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Tenant no encontrado"
                )
            );
    }
}
