package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.ReporteVentasRequest;
import com.abarrote.abarroteapi.service.ReportePdfService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reportes")
public class ReportePdfController {

    private final ReportePdfService reportePdfService;

    public ReportePdfController(
            ReportePdfService reportePdfService) {

        this.reportePdfService =
                reportePdfService;
    }

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> generarPdf(
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaInicio,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaFin) {

        ReporteVentasRequest request =
                new ReporteVentasRequest();

        request.setFechaInicio(
                fechaInicio
        );

        request.setFechaFin(
                fechaFin
        );

        byte[] pdf =
                reportePdfService
                        .generarReporteVentasPdf(
                                request
                        );

        String nombreArchivo =
                "reporte-ventas-"
                        + fechaInicio
                        + "-a-"
                        + fechaFin
                        + ".pdf";

        ContentDisposition disposition =
                ContentDisposition
                        .attachment()
                        .filename(
                                nombreArchivo,
                                StandardCharsets.UTF_8
                        )
                        .build();

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_PDF
        );

        headers.setContentDisposition(
                disposition
        );

        headers.setContentLength(
                pdf.length
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdf);
    }
}
