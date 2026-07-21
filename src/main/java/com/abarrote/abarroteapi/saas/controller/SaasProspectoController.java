package com.abarrote.abarroteapi.saas.controller;

import com.abarrote.abarroteapi.saas.dto.SaasProspectoRequest;
import com.abarrote.abarroteapi.saas.dto.SaasProspectoResponse;
import com.abarrote.abarroteapi.saas.service.SaasProspectoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/saas/prospectos")
public class SaasProspectoController {

    private final SaasProspectoService prospectoService;

    public SaasProspectoController(
        SaasProspectoService prospectoService
    ) {
        this.prospectoService = prospectoService;
    }

    @PostMapping
    public ResponseEntity<SaasProspectoResponse> registrar(
        @RequestBody SaasProspectoRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(prospectoService.registrar(request));
    }

    @GetMapping
    public ResponseEntity<List<SaasProspectoResponse>>
    listarTodos() {

        return ResponseEntity.ok(
            prospectoService.listarTodos()
        );
    }

    @GetMapping("/{folio}")
    public ResponseEntity<SaasProspectoResponse> obtener(
        @PathVariable String folio
    ) {
        return ResponseEntity.ok(
            prospectoService.obtenerPorFolio(folio)
        );
    }

    @GetMapping("/resumen/nuevos")
    public ResponseEntity<Map<String, Object>> resumenNuevos() {
        Map<String, Object> respuesta = new LinkedHashMap<>();

        respuesta.put(
            "prospectosNuevos",
            prospectoService.contarNuevos()
        );

        respuesta.put("estado", "OPERATIVO");

        return ResponseEntity.ok(respuesta);
    }
}
