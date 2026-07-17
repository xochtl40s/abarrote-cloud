package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.ItemVentaRequest;
import com.abarrote.abarroteapi.dto.VentaRequest;
import com.abarrote.abarroteapi.entity.DetalleVenta;
import com.abarrote.abarroteapi.entity.InventarioSucursal;
import com.abarrote.abarroteapi.entity.Producto;
import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.entity.Venta;
import com.abarrote.abarroteapi.repository.InventarioSucursalRepository;
import com.abarrote.abarroteapi.repository.ProductoRepository;
import com.abarrote.abarroteapi.repository.SucursalRepository;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import com.abarrote.abarroteapi.repository.VentaRepository;
import com.abarrote.abarroteapi.service.VentaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class VentaServiceImpl
        implements VentaService {

    private static final String CODIGO_SUCURSAL_MATRIZ =
            "MAT";

    private final VentaRepository ventaRepository;

    private final ProductoRepository productoRepository;

    private final UsuarioRepository usuarioRepository;

    private final SucursalRepository sucursalRepository;

    private final InventarioSucursalRepository
            inventarioSucursalRepository;

    public VentaServiceImpl(
            VentaRepository ventaRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository,
            SucursalRepository sucursalRepository,
            InventarioSucursalRepository
                    inventarioSucursalRepository) {

        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.sucursalRepository = sucursalRepository;

        this.inventarioSucursalRepository =
                inventarioSucursalRepository;
    }

    @Override
    public Venta guardar(
            Venta venta) {

        if (venta == null) {
            throw new IllegalArgumentException(
                    "La venta es obligatoria"
            );
        }

        if (venta.getFechaHora() == null) {
            venta.setFechaHora(
                    LocalDateTime.now()
            );
        }

        return ventaRepository.save(
                venta
        );
    }

    @Override
    public Venta registrarVenta(
            VentaRequest request) {

        validarSolicitud(request);

        Usuario usuario =
                obtenerUsuarioAutenticado();

        Sucursal sucursal =
                obtenerSucursalOperacion(usuario);

        Venta venta =
                crearVenta(
                        usuario,
                        sucursal
                );

        BigDecimal total =
                BigDecimal.ZERO;

        for (ItemVentaRequest item
                : request.getItems()) {

            validarItem(item);

            Producto producto =
                    productoRepository
                            .findById(
                                    item.getProductoId()
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Producto no encontrado: "
                                                    + item.getProductoId()
                                    )
                            );

            if (!Boolean.TRUE.equals(
                    producto.getActivo())) {

                throw new IllegalArgumentException(
                        "El producto "
                                + producto.getNombre()
                                + " está inactivo"
                );
            }

            InventarioSucursal inventario =
                    inventarioSucursalRepository
                            .buscarParaActualizar(
                                    sucursal.getId(),
                                    producto.getId()
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "El producto "
                                                    + producto.getNombre()
                                                    + " no está registrado en "
                                                    + sucursal.getNombre()
                                    )
                            );

            Integer existenciaDisponible =
                    inventario.getExistencia() != null
                            ? inventario.getExistencia()
                            : 0;

            if (existenciaDisponible
                    < item.getCantidad()) {

                throw new IllegalArgumentException(
                        "Stock insuficiente en "
                                + sucursal.getNombre()
                                + ": "
                                + producto.getNombre()
                                + " (disponible: "
                                + existenciaDisponible
                                + ", solicitado: "
                                + item.getCantidad()
                                + ")"
                );
            }

            BigDecimal precioVenta =
                    producto.getPrecioVenta();

            if (precioVenta == null) {
                throw new IllegalStateException(
                        "El producto "
                                + producto.getNombre()
                                + " no tiene precio de venta"
                );
            }

            BigDecimal subtotal =
                    precioVenta.multiply(
                            BigDecimal.valueOf(
                                    item.getCantidad()
                            )
                    );

            DetalleVenta detalle =
                    new DetalleVenta();

            detalle.setProducto(producto);
            detalle.setCantidad(
                    item.getCantidad()
            );
            detalle.setPrecioUnitario(
                    precioVenta
            );
            detalle.setSubtotal(
                    subtotal
            );

            venta.agregarDetalle(detalle);

            /*
             * Existencia física de la sucursal.
             */
            inventario.disminuirExistencia(
                    item.getCantidad()
            );

            inventarioSucursalRepository.save(
                    inventario
            );

            /*
             * Producto.stock permanece temporalmente como
             * inventario consolidado de todas las sucursales.
             *
             * Una transferencia no lo modifica, pero una venta sí.
             */
            int stockConsolidado =
                    producto.getStock() != null
                            ? producto.getStock()
                            : 0;

            if (stockConsolidado
                    < item.getCantidad()) {

                throw new IllegalStateException(
                        "Inconsistencia en inventario consolidado para "
                                + producto.getNombre()
                                + ". Revisa la distribución por sucursal."
                );
            }

            producto.setStock(
                    stockConsolidado
                            - item.getCantidad()
            );

            productoRepository.save(
                    producto
            );

            total = total.add(subtotal);
        }

        venta.setTotal(total);

        return ventaRepository.save(
                venta
        );
    }

    private Venta crearVenta(
            Usuario usuario,
            Sucursal sucursal) {

        Venta venta =
                new Venta();

        venta.setFechaHora(
                LocalDateTime.now()
        );

        venta.setUsuario(usuario);
        venta.setSucursal(sucursal);

        venta.setEstado(
                Venta.EstadoVenta.COMPLETADA
        );

        venta.setTotal(
                BigDecimal.ZERO
        );

        return venta;
    }

    private Usuario obtenerUsuarioAutenticado() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username =
                authentication != null
                        ? authentication.getName()
                        : null;

        if (username == null
                || username.isBlank()
                || "anonymousUser".equalsIgnoreCase(
                        username
                )) {

            throw new IllegalStateException(
                    "No existe un usuario autenticado para registrar la venta"
            );
        }

        return usuarioRepository
                .findByUsernameIgnoreCase(
                        username
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Usuario no encontrado: "
                                        + username
                        )
                );
    }

    private Sucursal obtenerSucursalOperacion(
            Usuario usuario) {

        if (usuario.getSucursal() != null) {

            if (!Boolean.TRUE.equals(
                    usuario
                            .getSucursal()
                            .getActiva())) {

                throw new IllegalStateException(
                        "La sucursal asignada al usuario se encuentra inactiva"
                );
            }

            return usuario.getSucursal();
        }

        /*
         * Compatibilidad para usuarios históricos.
         * El inicializador también los asignará a MAT.
         */
        return sucursalRepository
                .findByCodigoIgnoreCase(
                        CODIGO_SUCURSAL_MATRIZ
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "No existe la sucursal matriz MAT"
                        )
                );
    }

    private void validarSolicitud(
            VentaRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud de venta es obligatoria"
            );
        }

        if (request.getItems() == null
                || request.getItems().isEmpty()) {

            throw new IllegalArgumentException(
                    "La venta debe contener al menos un producto"
            );
        }
    }

    private void validarItem(
            ItemVentaRequest item) {

        if (item == null) {
            throw new IllegalArgumentException(
                    "Existe un producto inválido en la venta"
            );
        }

        if (item.getProductoId() == null) {
            throw new IllegalArgumentException(
                    "El identificador del producto es obligatorio"
            );
        }

        if (item.getCantidad() == null
                || item.getCantidad() <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad del producto debe ser mayor que cero"
            );
        }
    }
}
