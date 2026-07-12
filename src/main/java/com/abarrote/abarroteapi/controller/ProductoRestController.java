package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.ApiResponse;
import com.abarrote.abarroteapi.dto.ProductoRequest;
import com.abarrote.abarroteapi.dto.ProductoResponse;
import com.abarrote.abarroteapi.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    private final ProductoService productoService;

    public ProductoRestController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductoResponse>> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse creado = productoService.crear(request);
        return ResponseEntity.ok(ApiResponse.ok("Producto creado exitosamente", creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> actualizar(@PathVariable Long id, 
                                                                   @Valid @RequestBody ProductoRequest request) {
        ProductoResponse actualizado = productoService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Producto actualizado exitosamente", actualizado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productoService.obtenerPorId(id)));
    }

    @GetMapping("/codigo/{codigoBarras}")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtenerPorCodigoBarras(@PathVariable String codigoBarras) {
        return ResponseEntity.ok(ApiResponse.ok(productoService.obtenerPorCodigoBarras(codigoBarras)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok(productoService.listarTodos()));
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> buscarPorNombre(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(productoService.buscarPorNombre(q)));
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listarStockBajo() {
        return ResponseEntity.ok(ApiResponse.ok(productoService.listarStockBajo()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Producto eliminado exitosamente", null));
    }
}
