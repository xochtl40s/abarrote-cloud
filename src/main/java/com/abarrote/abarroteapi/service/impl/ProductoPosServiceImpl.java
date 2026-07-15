package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.ProductoResponse;
import com.abarrote.abarroteapi.entity.InventarioSucursal;
import com.abarrote.abarroteapi.entity.Producto;
import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.repository.InventarioSucursalRepository;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import com.abarrote.abarroteapi.service.ProductoPosService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductoPosServiceImpl
        implements ProductoPosService {

    private final UsuarioRepository usuarioRepository;

    private final InventarioSucursalRepository
            inventarioSucursalRepository;

    public ProductoPosServiceImpl(
            UsuarioRepository usuarioRepository,
            InventarioSucursalRepository inventarioSucursalRepository) {

        this.usuarioRepository = usuarioRepository;

        this.inventarioSucursalRepository =
                inventarioSucursalRepository;
    }

    @Override
    public List<ProductoResponse> listarProductosDelUsuario(
            String username) {

        Sucursal sucursal =
                obtenerSucursalUsuario(username);

        return inventarioSucursalRepository
                .findBySucursalIdOrderByProductoNombreAsc(
                        sucursal.getId()
                )
                .stream()
                .filter(this::esProductoDisponible)
                .map(this::mapearAResponse)
                .toList();
    }

    @Override
    public List<ProductoResponse> buscarProductosDelUsuario(
            String username,
            String termino) {

        String terminoNormalizado =
                termino != null
                        ? termino.trim().toLowerCase()
                        : "";

        return listarProductosDelUsuario(username)
                .stream()
                .filter(
                        producto ->
                                coincide(
                                        producto,
                                        terminoNormalizado
                                )
                )
                .toList();
    }

    private Sucursal obtenerSucursalUsuario(
            String username) {

        if (username == null
                || username.isBlank()
                || "anonymousUser".equalsIgnoreCase(username)) {

            throw new IllegalStateException(
                    "No existe un usuario autenticado"
            );
        }

        Usuario usuario =
                usuarioRepository
                        .findByUsernameIgnoreCase(
                                username.trim()
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "El usuario autenticado no existe"
                                )
                        );

        if (!Boolean.TRUE.equals(
                usuario.getActivo())) {

            throw new IllegalStateException(
                    "El usuario está inactivo"
            );
        }

        if (usuario.getSucursal() == null) {

            throw new IllegalStateException(
                    "El usuario no tiene una sucursal asignada"
            );
        }

        if (!Boolean.TRUE.equals(
                usuario.getSucursal().getActiva())) {

            throw new IllegalStateException(
                    "La sucursal asignada al usuario está inactiva"
            );
        }

        return usuario.getSucursal();
    }

    private boolean esProductoDisponible(
            InventarioSucursal inventario) {

        if (inventario == null
                || inventario.getProducto() == null) {

            return false;
        }

        Producto producto =
                inventario.getProducto();

        return Boolean.TRUE.equals(
                producto.getActivo()
        );
    }

    private boolean coincide(
            ProductoResponse producto,
            String termino) {

        if (termino.isBlank()) {
            return true;
        }

        String nombre =
                producto.getNombre() != null
                        ? producto.getNombre().toLowerCase()
                        : "";

        String codigo =
                producto.getCodigoBarras() != null
                        ? producto.getCodigoBarras().toLowerCase()
                        : "";

        String categoria =
                producto.getCategoriaNombre() != null
                        ? producto.getCategoriaNombre().toLowerCase()
                        : "";

        return nombre.contains(termino)
                || codigo.contains(termino)
                || categoria.contains(termino);
    }

    private ProductoResponse mapearAResponse(
            InventarioSucursal inventario) {

        Producto producto =
                inventario.getProducto();

        int existencia =
                inventario.getExistencia() != null
                        ? inventario.getExistencia()
                        : 0;

        int stockMinimo =
                inventario.getStockMinimo() != null
                        ? inventario.getStockMinimo()
                        : 0;

        ProductoResponse response =
                new ProductoResponse();

        response.setId(
                producto.getId()
        );

        response.setNombre(
                producto.getNombre()
        );

        response.setCodigoBarras(
                producto.getCodigoBarras()
        );

        response.setDescripcion(
                producto.getDescripcion()
        );

        response.setPrecioCompra(
                producto.getPrecioCompra()
        );

        response.setPrecioVenta(
                producto.getPrecioVenta()
        );

        /*
         * Para el POS, stock representa la existencia
         * de la sucursal del cajero.
         */
        response.setStock(
                existencia
        );

        response.setStockMinimo(
                stockMinimo
        );

        response.setStockBajo(
                existencia <= stockMinimo
        );

        response.setAgotado(
                existencia <= 0
        );

        response.setActivo(
                producto.getActivo()
        );

        if (producto.getCategoria() != null) {

            response.setCategoriaId(
                    producto
                            .getCategoria()
                            .getId()
            );

            response.setCategoriaNombre(
                    producto
                            .getCategoria()
                            .getNombre()
            );
        }

        return response;
    }
}
