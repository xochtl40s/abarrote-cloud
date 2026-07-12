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
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public ProductoResponse crear(ProductoRequest request) {
        if (request.getCodigoBarras() != null && !request.getCodigoBarras().isBlank()
                && productoRepository.existsByCodigoBarras(request.getCodigoBarras())) {
            throw new RuntimeException("Ya existe un producto con ese codigo de barras");
        }
        Producto producto = new Producto();
        mapearRequestAEntity(request, producto);
        return mapearAResponse(productoRepository.save(producto));
    }

    @Override
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        if (request.getCodigoBarras() != null && !request.getCodigoBarras().isBlank()
                && !request.getCodigoBarras().equals(producto.getCodigoBarras())
                && productoRepository.existsByCodigoBarras(request.getCodigoBarras())) {
            throw new RuntimeException("Ya existe un producto con ese codigo de barras");
        }
        mapearRequestAEntity(request, producto);
        return mapearAResponse(productoRepository.save(producto));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        return mapearAResponse(productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado")));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorCodigoBarras(String codigoBarras) {
        return mapearAResponse(productoRepository.findByCodigoBarras(codigoBarras)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodos() {
        return productoRepository.findByActivoTrue().stream()
                .map(this::mapearAResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre).stream()
                .map(this::mapearAResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarStockBajo() {
        return productoRepository.findByStockLessThanEqualAndActivoTrue(5).stream()
                .map(this::mapearAResponse).collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerEntityPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    private void mapearRequestAEntity(ProductoRequest request, Producto producto) {
        producto.setNombre(request.getNombre());
        producto.setCodigoBarras(request.getCodigoBarras());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioCompra(request.getPrecioCompra());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setStock(request.getStock());
        producto.setStockMinimo(request.getStockMinimo());
        producto.setActivo(true);
        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
            producto.setCategoria(categoria);
        }
    }

    private ProductoResponse mapearAResponse(Producto producto) {
        ProductoResponse response = new ProductoResponse();
        response.setId(producto.getId());
        response.setNombre(producto.getNombre());
        response.setCodigoBarras(producto.getCodigoBarras());
        response.setDescripcion(producto.getDescripcion());
        response.setPrecioVenta(producto.getPrecioVenta());
        response.setStock(producto.getStock());
        response.setStockMinimo(producto.getStockMinimo());
        response.setStockBajo(producto.getStockBajo());
        response.setActivo(producto.getActivo());
        if (producto.getCategoria() != null) {
            response.setCategoriaNombre(producto.getCategoria().getNombre());
        }
        return response;
    }
}
