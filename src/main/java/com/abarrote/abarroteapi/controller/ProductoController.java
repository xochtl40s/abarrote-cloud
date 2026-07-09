package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.entity.Producto;
import com.abarrote.abarroteapi.service.ProductoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Producto> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Producto getById(@PathVariable Long id) {
        return service.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    @GetMapping("/codigo/{codigo}")
    public Producto getByCodigo(@PathVariable String codigo) {
        return service.findByCodigoBarras(codigo)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    @PostMapping
    public Producto create(@RequestBody Producto producto) {
        return service.save(producto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteById(id);
    }
}
