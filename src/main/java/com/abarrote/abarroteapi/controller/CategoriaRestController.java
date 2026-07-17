package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.ApiResponse;
import com.abarrote.abarroteapi.dto.CategoriaRequest;
import com.abarrote.abarroteapi.entity.Categoria;
import com.abarrote.abarroteapi.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaRestController {

    private final CategoriaService categoriaService;

    public CategoriaRestController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Categoria>> crear(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Categoria creada", categoriaService.crear(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Categoria>> actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Categoria actualizada", categoriaService.actualizar(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Categoria>> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(categoriaService.obtenerPorId(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Categoria>>> listarTodas() {
        return ResponseEntity.ok(ApiResponse.ok(categoriaService.listarTodas()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Categoria eliminada", null));
    }
}
