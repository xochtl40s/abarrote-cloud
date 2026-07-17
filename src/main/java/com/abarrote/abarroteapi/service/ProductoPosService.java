package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.dto.ProductoResponse;

import java.util.List;

public interface ProductoPosService {

    List<ProductoResponse> listarProductosDelUsuario(
            String username
    );

    List<ProductoResponse> buscarProductosDelUsuario(
            String username,
            String termino
    );
}
