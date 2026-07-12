package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.dto.ReporteVentasRequest;
import com.abarrote.abarroteapi.dto.ReporteVentasResponse;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface ReporteService {
    ReporteVentasResponse generarReporteVentas(ReporteVentasRequest request);
    BigDecimal obtenerTotalVentasDelDia(LocalDate fecha);
    BigDecimal obtenerTotalVentasDelMes(int year, int month);
}
