package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.ProductoRequest;
import com.abarrote.abarroteapi.dto.ProductoResponse;
import com.abarrote.abarroteapi.entity.Categoria;
import com.abarrote.abarroteapi.entity.InventarioSucursal;
import com.abarrote.abarroteapi.entity.Producto;
import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.repository.CategoriaRepository;
import com.abarrote.abarroteapi.repository.InventarioSucursalRepository;
import com.abarrote.abarroteapi.repository.ProductoRepository;
import com.abarrote.abarroteapi.repository.SucursalRepository;
import com.abarrote.abarroteapi.service.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoServiceImpl
        implements ProductoService {

    private static final String CODIGO_SUCURSAL_MATRIZ =
            "MAT";

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    private final InventarioSucursalRepository
            inventarioSucursalRepository;

    private final SucursalRepository sucursalRepository;

    public ProductoServiceImpl(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            InventarioSucursalRepository
                    inventarioSucursalRepository,
            SucursalRepository sucursalRepository) {

        this.productoRepository =
                productoRepository;

        this.categoriaRepository =
                categoriaRepository;

        this.inventarioSucursalRepository =
                inventarioSucursalRepository;

        this.sucursalRepository =
                sucursalRepository;
    }

    @Override
    public ProductoResponse crear(
            ProductoRequest request) {

        validarRequest(
                request
        );

        validarCodigoBarrasDuplicado(
                null,
                request.getCodigoBarras()
        );

        Producto producto =
                new Producto();

        mapearRequestAEntity(
                request,
                producto
        );

        /*
         * Primero guardamos el producto para obtener
         * su identificador.
         */
        Producto guardado =
                productoRepository.save(
                        producto
                );

        productoRepository.flush();

        /*
         * Regla de negocio:
         *
         * Un producto creado manualmente desde Administración
         * se registra inicialmente en la sucursal MAT.
         *
         * Esto permite que el POS de la matriz lo vea
         * inmediatamente.
         */
        sincronizarInventarioMatriz(
                guardado,
                request.getStock(),
                request.getStockMinimo()
        );

        recalcularStockTotal(
                guardado
        );

        return mapearAResponse(
                guardado
        );
    }

    @Override
    public ProductoResponse actualizar(
            Long id,
            ProductoRequest request) {

        validarRequest(
                request
        );

        Producto producto =
                productoRepository
                        .findById(
                                id
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Producto no encontrado"
                                        )
                        );

        validarCodigoBarrasDuplicado(
                producto,
                request.getCodigoBarras()
        );

        mapearRequestAEntity(
                request,
                producto
        );

        Producto actualizado =
                productoRepository.save(
                        producto
                );

        productoRepository.flush();

        /*
         * El stock capturado en la pantalla de producto
         * representa la existencia de la sucursal MAT.
         */
        sincronizarInventarioMatriz(
                actualizado,
                request.getStock(),
                request.getStockMinimo()
        );

        recalcularStockTotal(
                actualizado
        );

        return mapearAResponse(
                actualizado
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(
            Long id) {

        Producto producto =
                productoRepository
                        .findById(
                                id
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Producto no encontrado"
                                        )
                        );

        return mapearAResponse(
                producto
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorCodigoBarras(
            String codigoBarras) {

        Producto producto =
                productoRepository
                        .findByCodigoBarras(
                                codigoBarras
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Producto no encontrado"
                                        )
                        );

        if (!Boolean.TRUE.equals(
                producto.getActivo())) {

            throw new RuntimeException(
                    "El producto se encuentra inactivo"
            );
        }

        return mapearAResponse(
                producto
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodos() {

        return productoRepository
                .findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(
                        this::mapearAResponse
                )
                .collect(
                        Collectors.toList()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarPorNombre(
            String nombre) {

        String termino =
                nombre == null
                        ? ""
                        : nombre.trim();

        return productoRepository
                .findByNombreContainingIgnoreCaseAndActivoTrue(
                        termino
                )
                .stream()
                .map(
                        this::mapearAResponse
                )
                .collect(
                        Collectors.toList()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarStockBajo() {

        return productoRepository
                .encontrarProductosConStockBajo()
                .stream()
                .map(
                        this::mapearAResponse
                )
                .collect(
                        Collectors.toList()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarAgotados() {

        return productoRepository
                .encontrarProductosAgotados()
                .stream()
                .map(
                        this::mapearAResponse
                )
                .collect(
                        Collectors.toList()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public long contarStockBajo() {

        return productoRepository
                .contarProductosConStockBajo();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarAgotados() {

        return productoRepository
                .contarProductosAgotados();
    }

    @Override
    public void eliminar(
            Long id) {

        Producto producto =
                productoRepository
                        .findById(
                                id
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Producto no encontrado"
                                        )
                        );

        producto.setActivo(
                false
        );

        productoRepository.save(
                producto
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerEntityPorId(
            Long id) {

        return productoRepository
                .findById(
                        id
                )
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "Producto no encontrado"
                                )
                );
    }

    private void sincronizarInventarioMatriz(
            Producto producto,
            Integer existenciaSolicitada,
            Integer stockMinimoSolicitado) {

        Sucursal matriz =
                obtenerSucursalMatriz();

        InventarioSucursal inventario =
                inventarioSucursalRepository
                        .findBySucursalIdAndProductoId(
                                matriz.getId(),
                                producto.getId()
                        )
                        .orElseGet(
                                () -> {

                                    InventarioSucursal nuevo =
                                            new InventarioSucursal();

                                    nuevo.setSucursal(
                                            matriz
                                    );

                                    nuevo.setProducto(
                                            producto
                                    );

                                    return nuevo;
                                }
                        );

        inventario.setExistencia(
                existenciaSolicitada != null
                        ? existenciaSolicitada
                        : 0
        );

        inventario.setStockMinimo(
                stockMinimoSolicitado != null
                        ? stockMinimoSolicitado
                        : 5
        );

        inventarioSucursalRepository.save(
                inventario
        );

        inventarioSucursalRepository.flush();
    }

    private Sucursal obtenerSucursalMatriz() {

        Sucursal matriz =
                sucursalRepository
                        .findByCodigoIgnoreCase(
                                CODIGO_SUCURSAL_MATRIZ
                        )
                        .orElse(
                                null
                        );

        if (matriz != null) {

            if (!Boolean.TRUE.equals(
                    matriz.getActiva())) {

                throw new IllegalStateException(
                        "La sucursal MAT se encuentra inactiva"
                );
            }

            return matriz;
        }

        List<Sucursal> sucursalesActivas =
                sucursalRepository
                        .findByActivaTrueOrderByNombreAsc();

        if (sucursalesActivas.size() == 1) {
            return sucursalesActivas.get(
                    0
            );
        }

        throw new IllegalStateException(
                "No existe una sucursal activa con código MAT. "
                        + "Configura la sucursal matriz antes de crear productos."
        );
    }

    private void recalcularStockTotal(
            Producto producto) {

        Long existenciaTotal =
                inventarioSucursalRepository
                        .obtenerExistenciaTotalProducto(
                                producto.getId()
                        );

        int stockTotal;

        try {

            stockTotal =
                    existenciaTotal == null
                            ? 0
                            : Math.toIntExact(
                                    existenciaTotal
                            );

        } catch (ArithmeticException exception) {

            throw new IllegalStateException(
                    "La existencia total del producto excede el límite permitido",
                    exception
            );
        }

        producto.setStock(
                stockTotal
        );

        productoRepository.save(
                producto
        );

        productoRepository.flush();
    }

    private void validarRequest(
            ProductoRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos del producto son obligatorios"
            );
        }

        if (request.getNombre() == null
                || request.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre del producto es obligatorio"
            );
        }

        if (request.getPrecioCompra() == null) {

            throw new IllegalArgumentException(
                    "El precio de compra es obligatorio"
            );
        }

        if (request.getPrecioVenta() == null) {

            throw new IllegalArgumentException(
                    "El precio de venta es obligatorio"
            );
        }

        if (request.getStock() == null
                || request.getStock() < 0) {

            throw new IllegalArgumentException(
                    "El stock debe ser igual o mayor que cero"
            );
        }
    }

    private void validarCodigoBarrasDuplicado(
            Producto productoActual,
            String codigoBarras) {

        if (codigoBarras == null
                || codigoBarras.isBlank()) {

            return;
        }

        String codigoLimpio =
                codigoBarras.trim();

        productoRepository
                .findByCodigoBarras(
                        codigoLimpio
                )
                .ifPresent(
                        productoExistente -> {

                            boolean esMismoProducto =
                                    productoActual != null
                                            && productoExistente
                                            .getId()
                                            .equals(
                                                    productoActual.getId()
                                            );

                            if (!esMismoProducto) {

                                throw new RuntimeException(
                                        "Ya existe un producto con ese código de barras"
                                );
                            }
                        }
                );
    }

    private void mapearRequestAEntity(
            ProductoRequest request,
            Producto producto) {

        producto.setNombre(
                request.getNombre()
                        .trim()
        );

        producto.setCodigoBarras(
                limpiarTextoOpcional(
                        request.getCodigoBarras()
                )
        );

        producto.setDescripcion(
                limpiarTextoOpcional(
                        request.getDescripcion()
                )
        );

        producto.setPrecioCompra(
                request.getPrecioCompra()
        );

        producto.setPrecioVenta(
                request.getPrecioVenta()
        );

        /*
         * Este valor será recalculado usando la suma
         * de inventario_sucursal.
         */
        producto.setStock(
                request.getStock() != null
                        ? request.getStock()
                        : 0
        );

        producto.setStockMinimo(
                request.getStockMinimo() != null
                        ? request.getStockMinimo()
                        : 5
        );

        producto.setActivo(
                true
        );

        if (request.getCategoriaId() == null) {

            producto.setCategoria(
                    null
            );

        } else {

            Categoria categoria =
                    categoriaRepository
                            .findById(
                                    request.getCategoriaId()
                            )
                            .orElseThrow(
                                    () ->
                                            new RuntimeException(
                                                    "Categoría no encontrada"
                                            )
                            );

            producto.setCategoria(
                    categoria
            );
        }
    }

    private String limpiarTextoOpcional(
            String texto) {

        if (texto == null
                || texto.isBlank()) {

            return null;
        }

        return texto.trim();
    }

    private ProductoResponse mapearAResponse(
            Producto producto) {

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

        response.setStock(
                producto.getStock()
        );

        response.setStockMinimo(
                producto.getStockMinimo()
        );

        response.setStockBajo(
                producto.getStockBajo()
        );

        response.setAgotado(
                producto.getAgotado()
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
