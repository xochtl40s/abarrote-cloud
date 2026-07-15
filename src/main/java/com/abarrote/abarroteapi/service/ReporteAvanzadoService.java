package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.dto.ReporteAvanzadoResponse;

import java.time.LocalDate;

public interface ReporteAvanzadoService {

    ReporteAvanzadoResponse generarReporte(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Long usuarioId
    );
}
