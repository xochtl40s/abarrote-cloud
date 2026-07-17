package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.ProductoRequest;
import com.abarrote.abarroteapi.dto.ProductoResponse;
import com.abarrote.abarroteapi.entity.Categoria;
import com.abarrote.abarroteapi.entity.Producto;
import com.abarrote.abarroteapi.repository.CategoriaRepository;
import com.abarrote.abarroteapi.repository.ProductoRepository;
import com.abarrote.abarroteapi.service.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoServiceImpl
        implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoServiceImpl(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public ProductoResponse crear(
            ProductoRequest request) {

        validarCodigoBarrasDuplicado(
                null,
                request.getCodigoBarras()
        );

        Producto producto = new Producto();

        mapearRequestAEntity(
                request,
                producto
        );

        Producto guardado =
                productoRepository.save(producto);

        return mapearAResponse(guardado);
    }

    @Override
    public ProductoResponse actualizar(
            Long id,
            ProductoRequest request) {

        Producto producto =
                productoRepository
                        .findById(id)
                        .orElseThrow(() ->
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
                productoRepository.save(producto);

        return mapearAResponse(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(
            Long id) {

        Producto producto =
                productoRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Producto no encontrado"
                                )
                        );

        return mapearAResponse(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorCodigoBarras(
            String codigoBarras) {

        Producto producto =
                productoRepository
                        .findByCodigoBarras(codigoBarras)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Producto no encontrado"
                                )
                        );

        if (!Boolean.TRUE.equals(producto.getActivo())) {

            throw new RuntimeException(
                    "El producto se encuentra inactivo"
            );
        }

        return mapearAResponse(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodos() {

        return productoRepository
                .findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
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
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarStockBajo() {

        return productoRepository
                .encontrarProductosConStockBajo()
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarAgotados() {

        return productoRepository
                .encontrarProductosAgotados()
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
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
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Producto no encontrado"
                                )
                        );

        producto.setActivo(false);

        productoRepository.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerEntityPorId(
            Long id) {

        return productoRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Producto no encontrado"
                        )
                );
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
                .findByCodigoBarras(codigoLimpio)
                .ifPresent(productoExistente -> {

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
                });
    }

    private void mapearRequestAEntity(
            ProductoRequest request,
            Producto producto) {

        producto.setNombre(
                request.getNombre().trim()
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

        producto.setStock(
                request.getStock()
        );

        producto.setStockMinimo(
                request.getStockMinimo() != null
                        ? request.getStockMinimo()
                        : 5
        );

        producto.setActivo(true);

        if (request.getCategoriaId() == null) {

            producto.setCategoria(null);

        } else {

            Categoria categoria =
                    categoriaRepository
                            .findById(
                                    request.getCategoriaId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Categoría no encontrada"
                                    )
                            );

            producto.setCategoria(categoria);
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
