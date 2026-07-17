package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.dto.ReporteVentasRequest;

public interface ReportePdfService {

    byte[] generarReporteVentasPdf(
            ReporteVentasRequest request
    );
}
