package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.ReporteAvanzadoResponse;
import com.abarrote.abarroteapi.service.ReporteAvanzadoService;
import com.abarrote.abarroteapi.service.UsuarioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reportes-avanzados")
public class ReporteAvanzadoController {

    private final ReporteAvanzadoService reporteAvanzadoService;
    private final UsuarioService usuarioService;

    public ReporteAvanzadoController(
            ReporteAvanzadoService reporteAvanzadoService,
            UsuarioService usuarioService) {

        this.reporteAvanzadoService = reporteAvanzadoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String mostrarReporte(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin,

            @RequestParam(required = false)
            Long usuarioId,

            Model model) {

        PeriodoReporte periodo =
                resolverPeriodo(fechaInicio, fechaFin);

        ReporteAvanzadoResponse reporte =
                reporteAvanzadoService.generarReporte(
                        periodo.fechaInicio(),
                        periodo.fechaFin(),
                        usuarioId
                );

        model.addAttribute("reporte", reporte);
        model.addAttribute(
                "fechaInicio",
                periodo.fechaInicio()
        );
        model.addAttribute(
                "fechaFin",
                periodo.fechaFin()
        );
        model.addAttribute("usuarioId", usuarioId);
        model.addAttribute(
                "usuarios",
                usuarioService.listarActivos()
        );
        model.addAttribute(
                "activePage",
                "reportes"
        );

        return "admin/reportes-avanzados";
    }

    @GetMapping("/exportar-csv")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin,

            @RequestParam(required = false)
            Long usuarioId) {

        PeriodoReporte periodo =
                resolverPeriodo(fechaInicio, fechaFin);

        ReporteAvanzadoResponse reporte =
                reporteAvanzadoService.generarReporte(
                        periodo.fechaInicio(),
                        periodo.fechaFin(),
                        usuarioId
                );

        String csv = construirCsv(reporte);

        /*
         * Se agrega BOM UTF-8 para que Excel y LibreOffice
         * reconozcan correctamente acentos y caracteres especiales.
         */
        byte[] contenido =
                ("\uFEFF" + csv)
                        .getBytes(StandardCharsets.UTF_8);

        String nombreArchivo =
                "reporte-avanzado-"
                        + periodo.fechaInicio()
                        + "-a-"
                        + periodo.fechaFin()
                        + ".csv";

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                new MediaType(
                        "text",
                        "csv",
                        StandardCharsets.UTF_8
                )
        );

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(
                                nombreArchivo,
                                StandardCharsets.UTF_8
                        )
                        .build()
        );

        headers.setContentLength(contenido.length);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(contenido);
    }

    private PeriodoReporte resolverPeriodo(
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        LocalDate fechaFinal =
                fechaFin != null
                        ? fechaFin
                        : LocalDate.now();

        LocalDate fechaInicial =
                fechaInicio != null
                        ? fechaInicio
                        : fechaFinal.minusDays(29);

        if (fechaInicial.isAfter(fechaFinal)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final"
            );
        }

        return new PeriodoReporte(
                fechaInicial,
                fechaFinal
        );
    }

    private String construirCsv(
            ReporteAvanzadoResponse reporte) {

        StringBuilder csv = new StringBuilder();

        agregarLinea(
                csv,
                "REPORTE AVANZADO DE VENTAS"
        );

        agregarLinea(
                csv,
                "Fecha inicial",
                reporte.getFechaInicio()
        );

        agregarLinea(
                csv,
                "Fecha final",
                reporte.getFechaFin()
        );

        agregarLinea(csv);

        agregarLinea(
                csv,
                "RESUMEN GENERAL"
        );

        agregarLinea(
                csv,
                "Concepto",
                "Valor"
        );

        agregarLinea(
                csv,
                "Número de ventas",
                reporte.getNumeroVentas()
        );

        agregarLinea(
                csv,
                "Artículos vendidos",
                reporte.getArticulosVendidos()
        );

        agregarLinea(
                csv,
                "Ventas netas",
                formatearDecimal(
                        reporte.getVentasNetas()
                )
        );

        agregarLinea(
                csv,
                "Costo de mercancía",
                formatearDecimal(
                        reporte.getCostoMercancia()
                )
        );

        agregarLinea(
                csv,
                "Beneficio bruto",
                formatearDecimal(
                        reporte.getBeneficioBruto()
                )
        );

        agregarLinea(
                csv,
                "Margen bruto (%)",
                formatearDecimal(
                        reporte.getMargenBruto()
                )
        );

        agregarLinea(
                csv,
                "Venta promedio",
                formatearDecimal(
                        reporte.getVentaPromedio()
                )
        );

        agregarLinea(csv);
        agregarLinea(
                csv,
                "VENTAS POR PRODUCTO"
        );

        agregarLinea(
                csv,
                "Producto",
                "Categoría",
                "Artículos vendidos",
                "Ventas netas",
                "Costo mercancía",
                "Beneficio bruto",
                "Margen bruto (%)"
        );

        for (ReporteAvanzadoResponse.VentaProducto item
                : reporte.getVentasPorProducto()) {

            agregarLinea(
                    csv,
                    item.getProducto(),
                    item.getCategoria(),
                    item.getArticulosVendidos(),
                    formatearDecimal(
                            item.getVentasNetas()
                    ),
                    formatearDecimal(
                            item.getCostoMercancia()
                    ),
                    formatearDecimal(
                            item.getBeneficioBruto()
                    ),
                    formatearDecimal(
                            item.getMargenBruto()
                    )
            );
        }

        agregarLinea(csv);
        agregarLinea(
                csv,
                "VENTAS POR CATEGORÍA"
        );

        agregarLinea(
                csv,
                "Categoría",
                "Artículos vendidos",
                "Ventas netas",
                "Costo mercancía",
                "Beneficio bruto",
                "Margen bruto (%)"
        );

        for (ReporteAvanzadoResponse.VentaCategoria item
                : reporte.getVentasPorCategoria()) {

            agregarLinea(
                    csv,
                    item.getCategoria(),
                    item.getArticulosVendidos(),
                    formatearDecimal(
                            item.getVentasNetas()
                    ),
                    formatearDecimal(
                            item.getCostoMercancia()
                    ),
                    formatearDecimal(
                            item.getBeneficioBruto()
                    ),
                    formatearDecimal(
                            item.getMargenBruto()
                    )
            );
        }

        agregarLinea(csv);
        agregarLinea(
                csv,
                "VENTAS POR CAJERO"
        );

        agregarLinea(
                csv,
                "Cajero",
                "Número de ventas",
                "Artículos vendidos",
                "Ventas netas",
                "Costo mercancía",
                "Beneficio bruto",
                "Venta promedio"
        );

        for (ReporteAvanzadoResponse.VentaCajero item
                : reporte.getVentasPorCajero()) {

            agregarLinea(
                    csv,
                    item.getCajero(),
                    item.getNumeroVentas(),
                    item.getArticulosVendidos(),
                    formatearDecimal(
                            item.getVentasNetas()
                    ),
                    formatearDecimal(
                            item.getCostoMercancia()
                    ),
                    formatearDecimal(
                            item.getBeneficioBruto()
                    ),
                    formatearDecimal(
                            item.getVentaPromedio()
                    )
            );
        }

        return csv.toString();
    }

    private void agregarLinea(
            StringBuilder csv,
            Object... valores) {

        for (int indice = 0;
             indice < valores.length;
             indice++) {

            if (indice > 0) {
                csv.append(',');
            }

            csv.append(
                    escaparCsv(valores[indice])
            );
        }

        csv.append(System.lineSeparator());
    }

    private String escaparCsv(
            Object valor) {

        if (valor == null) {
            return "";
        }

        String texto = valor.toString();

        boolean requiereComillas =
                texto.contains(",")
                        || texto.contains("\"")
                        || texto.contains("\n")
                        || texto.contains("\r");

        texto = texto.replace(
                "\"",
                "\"\""
        );

        if (requiereComillas) {
            return "\"" + texto + "\"";
        }

        return texto;
    }

    private String formatearDecimal(
            BigDecimal valor) {

        return valor != null
                ? valor.toPlainString()
                : "0.00";
    }

    private record PeriodoReporte(
            LocalDate fechaInicio,
            LocalDate fechaFin) {
    }
}
