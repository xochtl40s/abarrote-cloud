package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.ApiResponse;
import com.abarrote.abarroteapi.dto.ReporteVentasRequest;
import com.abarrote.abarroteapi.dto.ReporteVentasResponse;
import com.abarrote.abarroteapi.service.ReporteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
public class ReporteRestController {

    private final ReporteService reporteService;

    public ReporteRestController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PostMapping("/ventas")
    public ResponseEntity<ApiResponse<ReporteVentasResponse>> generarReporteVentas(
            @Valid @RequestBody ReporteVentasRequest request) {
        ReporteVentasResponse reporte = reporteService.generarReporteVentas(request);
        return ResponseEntity.ok(ApiResponse.ok(reporte));
    }

    @GetMapping("/ventas/dia")
    public ResponseEntity<ApiResponse<BigDecimal>> totalVentasDelDia(
            @RequestParam(required = false) LocalDate fecha) {
        LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();
        BigDecimal total = reporteService.obtenerTotalVentasDelDia(fechaConsulta);
        return ResponseEntity.ok(ApiResponse.ok("Total ventas del " + fechaConsulta, total));
    }

    @GetMapping("/ventas/mes")
    public ResponseEntity<ApiResponse<BigDecimal>> totalVentasDelMes(
            @RequestParam int year,
            @RequestParam int month) {
        BigDecimal total = reporteService.obtenerTotalVentasDelMes(year, month);
        return ResponseEntity.ok(ApiResponse.ok("Total ventas del mes", total));
    }
}
