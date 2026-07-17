package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.dto.CategoriaRequest;
import com.abarrote.abarroteapi.entity.Categoria;
import java.util.List;

public interface CategoriaService {
    Categoria crear(CategoriaRequest request);
    Categoria actualizar(Long id, CategoriaRequest request);
    Categoria obtenerPorId(Long id);
    List<Categoria> listarTodas();
    List<Categoria> listarTodasConProductos();
    void eliminar(Long id);
}
