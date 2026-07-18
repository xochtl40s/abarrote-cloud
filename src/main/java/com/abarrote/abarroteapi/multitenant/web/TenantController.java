package com.abarrote.abarroteapi.multitenant.web;

import com.abarrote.abarroteapi.multitenant.dto.TenantRequest;
import com.abarrote.abarroteapi.multitenant.dto.TenantResponse;
import com.abarrote.abarroteapi.multitenant.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(
        TenantService tenantService
    ) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<TenantResponse> crear(
        @Valid @RequestBody TenantRequest request
    ) {
        TenantResponse response =
            tenantService.crear(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TenantResponse>> listar() {
        return ResponseEntity.ok(
            tenantService.listar()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> obtenerPorId(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(
            tenantService.obtenerPorId(id)
        );
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<TenantResponse> obtenerPorSlug(
        @PathVariable String slug
    ) {
        return ResponseEntity.ok(
            tenantService.obtenerPorSlug(slug)
        );
    }

    @PatchMapping("/{id}/suspender")
    public ResponseEntity<TenantResponse> suspender(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(
            tenantService.suspender(id)
        );
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<TenantResponse> reactivar(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(
            tenantService.reactivar(id)
        );
    }
}
