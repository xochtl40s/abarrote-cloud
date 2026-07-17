package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.dto.ProductoRequest;
import com.abarrote.abarroteapi.dto.ProductoResponse;
import com.abarrote.abarroteapi.entity.Producto;

import java.util.List;

public interface ProductoService {

    ProductoResponse crear(
            ProductoRequest request
    );

    ProductoResponse actualizar(
            Long id,
            ProductoRequest request
    );

    ProductoResponse obtenerPorId(
            Long id
    );

    ProductoResponse obtenerPorCodigoBarras(
            String codigoBarras
    );

    List<ProductoResponse> listarTodos();

    List<ProductoResponse> buscarPorNombre(
            String nombre
    );

    List<ProductoResponse> listarStockBajo();

    List<ProductoResponse> listarAgotados();

    long contarStockBajo();

    long contarAgotados();

    void eliminar(
            Long id
    );

    Producto obtenerEntityPorId(
            Long id
    );
}
