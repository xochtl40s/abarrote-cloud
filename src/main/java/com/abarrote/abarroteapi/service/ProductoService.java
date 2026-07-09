package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.entity.Producto;
import com.abarrote.abarroteapi.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<Producto> findAll() {
        return repository.findAll();
    }

    public Optional<Producto> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Producto> findByCodigoBarras(String codigoBarras) {
        return repository.findByCodigoBarras(codigoBarras);
    }

    public Producto save(Producto producto) {
        return repository.save(producto);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

}
