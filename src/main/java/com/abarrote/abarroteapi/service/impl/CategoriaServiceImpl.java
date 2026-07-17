package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.CategoriaRequest;
import com.abarrote.abarroteapi.entity.Categoria;
import com.abarrote.abarroteapi.repository.CategoriaRepository;
import com.abarrote.abarroteapi.service.CategoriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public Categoria crear(CategoriaRequest request) {
        if (categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new RuntimeException("Ya existe una categoria con ese nombre");
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        return categoriaRepository.save(categoria);
    }

    @Override
    public Categoria actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
        if (!request.getNombre().equalsIgnoreCase(categoria.getNombre())
                && categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new RuntimeException("Ya existe una categoria con ese nombre");
        }
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarTodasConProductos() {
        List<Categoria> categorias = categoriaRepository.findAll();
        // Forzar carga de productos (Lazy Loading)
        for (Categoria c : categorias) {
            c.getProductos().size();
        }
        return categorias;
    }

    @Override
    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
        categoriaRepository.delete(categoria);
    }
}
