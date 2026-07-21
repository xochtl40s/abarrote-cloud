package com.abarrote.abarroteapi.saas.controller;

import com.abarrote.abarroteapi.saas.dto.SaasPlanResponse;
import com.abarrote.abarroteapi.saas.service.SaasPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/saas/planes")
public class SaasPlanController {

    private final SaasPlanService saasPlanService;

    public SaasPlanController(
        SaasPlanService saasPlanService
    ) {
        this.saasPlanService = saasPlanService;
    }

    @GetMapping
    public ResponseEntity<List<SaasPlanResponse>> listarActivos() {
        return ResponseEntity.ok(
            saasPlanService.listarPlanesActivos()
        );
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<SaasPlanResponse> obtenerPorCodigo(
        @PathVariable String codigo
    ) {
        return ResponseEntity.ok(
            saasPlanService.obtenerPorCodigo(codigo)
        );
    }

    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> obtenerResumen() {
        Map<String, Object> respuesta = new LinkedHashMap<>();

        respuesta.put("modulo", "Commerce Cloud SaaS");
        respuesta.put(
            "planesActivos",
            saasPlanService.contarPlanesActivos()
        );
        respuesta.put("estado", "OPERATIVO");

        return ResponseEntity.ok(respuesta);
    }
}
