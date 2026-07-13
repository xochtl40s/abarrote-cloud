package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.ReporteVentasRequest;
import com.abarrote.abarroteapi.dto.ReporteVentasResponse;
import com.abarrote.abarroteapi.service.ReportePdfService;
import com.abarrote.abarroteapi.service.ReporteService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
public class ReportePdfServiceImpl implements ReportePdfService {

    private static final float MARGEN_IZQUIERDO = 45;
    private static final float MARGEN_DERECHO = 45;
    private static final float MARGEN_SUPERIOR = 50;
    private static final float MARGEN_INFERIOR = 50;

    private static final float ANCHO_PAGINA =
            PDRectangle.A4.getWidth();

    private static final float ANCHO_UTIL =
            ANCHO_PAGINA
                    - MARGEN_IZQUIERDO
                    - MARGEN_DERECHO;

    private static final Locale LOCALE_MEXICO =
            Locale.forLanguageTag("es-MX");

    private static final DateTimeFormatter FORMATO_ARCHIVO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");

    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReporteService reporteService;
    private final Path rutaBaseReportes;

    private final PDType1Font fuenteNormal =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

    private final PDType1Font fuenteNegrita =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

    public ReportePdfServiceImpl(
            ReporteService reporteService,
            @Value("${abarrote.reportes.ruta}")
            String rutaBaseReportes) {

        this.reporteService = reporteService;

        this.rutaBaseReportes =
                Paths.get(rutaBaseReportes)
                        .toAbsolutePath()
                        .normalize();
    }

    @Override
    public byte[] generarReporteVentasPdf(
            ReporteVentasRequest request) {

        validarRangoFechas(request);

        ReporteVentasResponse reporte =
                reporteService.generarReporteVentas(request);

        try (
                PDDocument documento = new PDDocument();
                ByteArrayOutputStream salida =
                        new ByteArrayOutputStream()
        ) {

            EscritorPdf escritor =
                    new EscritorPdf(documento);

            escribirEncabezado(
                    escritor,
                    reporte
            );

            escribirResumenGeneral(
                    escritor,
                    reporte
            );

            escribirResumenCajeros(
                    escritor,
                    reporte
            );

            escribirTopProductos(
                    escritor,
                    reporte
            );

            escribirDetalleVentas(
                    escritor,
                    reporte
            );

            escritor.escribirPieFinal();
            escritor.cerrar();

            documento.save(salida);

            byte[] contenido =
                    salida.toByteArray();

            guardarCopiaLocal(
                    contenido,
                    request
            );

            return contenido;

        } catch (IOException exception) {

            throw new RuntimeException(
                    "No fue posible generar el reporte PDF: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    private void validarRangoFechas(
            ReporteVentasRequest request) {

        if (request == null) {

            throw new RuntimeException(
                    "La solicitud del reporte es obligatoria"
            );
        }

        if (request.getFechaInicio() == null
                || request.getFechaFin() == null) {

            throw new RuntimeException(
                    "Las fechas de inicio y fin son obligatorias"
            );
        }

        if (request.getFechaInicio()
                .isAfter(request.getFechaFin())) {

            throw new RuntimeException(
                    "La fecha inicial no puede ser posterior a la fecha final"
            );
        }
    }

    private void escribirEncabezado(
            EscritorPdf escritor,
            ReporteVentasResponse reporte)
            throws IOException {

        escritor.escribirTitulo(
                "ABARROTE CLOUD"
        );

        escritor.escribirSubtitulo(
                "Reporte de ventas"
        );

        escritor.escribirTexto(
                "Periodo: "
                        + formatearFecha(
                                reporte.getFechaInicio()
                        )
                        + " al "
                        + formatearFecha(
                                reporte.getFechaFin()
                        )
        );

        escritor.escribirTexto(
                "Generado: "
                        + LocalDateTime
                        .now()
                        .format(FORMATO_FECHA_HORA)
        );

        escritor.escribirSeparador();
        escritor.saltarLinea(4);
    }

    private void escribirResumenGeneral(
            EscritorPdf escritor,
            ReporteVentasResponse reporte)
            throws IOException {

        escritor.escribirSeccion(
                "RESUMEN GENERAL"
        );

        escritor.escribirTextoNegrita(
                "Total de ventas: "
                        + valorEntero(
                                reporte.getTotalVentas()
                        )
        );

        escritor.escribirTextoNegrita(
                "Monto total: "
                        + formatearMoneda(
                                reporte.getMontoTotal()
                        )
        );

        escritor.saltarLinea(8);
    }

    private void escribirResumenCajeros(
            EscritorPdf escritor,
            ReporteVentasResponse reporte)
            throws IOException {

        escritor.escribirSeccion(
                "RESUMEN POR CAJERO"
        );

        if (reporte.getResumenPorCajero() == null
                || reporte.getResumenPorCajero().isEmpty()) {

            escritor.escribirTexto(
                    "No existen ventas por cajero en el periodo."
            );

            escritor.saltarLinea(8);

            return;
        }

        for (
                ReporteVentasResponse.ResumenCajero cajero
                : reporte.getResumenPorCajero()
        ) {

            escritor.verificarEspacio(60);

            escritor.escribirTextoNegrita(
                    textoSeguro(cajero.getCajero())
                            + " | "
                            + valorEntero(
                                    cajero.getCantidadVentas()
                            )
                            + " ventas | "
                            + formatearMoneda(
                                    cajero.getMontoTotal()
                            )
            );

            if (cajero.getProductosVendidos() != null) {

                for (
                        ReporteVentasResponse.ProductoCajero producto
                        : cajero.getProductosVendidos()
                ) {

                    escritor.escribirTextoIndentado(
                            textoSeguro(
                                    producto.getNombreProducto()
                            )
                                    + " - "
                                    + valorEntero(
                                            producto.getCantidad()
                                    )
                                    + " piezas - "
                                    + formatearMoneda(
                                            producto.getMonto()
                                    )
                    );
                }
            }

            escritor.saltarLinea(5);
        }

        escritor.saltarLinea(4);
    }

    private void escribirTopProductos(
            EscritorPdf escritor,
            ReporteVentasResponse reporte)
            throws IOException {

        escritor.escribirSeccion(
                "PRODUCTOS MAS VENDIDOS"
        );

        if (reporte.getProductosMasVendidos() == null
                || reporte.getProductosMasVendidos().isEmpty()) {

            escritor.escribirTexto(
                    "No existen productos vendidos en el periodo."
            );

            escritor.saltarLinea(8);

            return;
        }

        int posicion = 1;

        for (
                ReporteVentasResponse.ProductoTop producto
                : reporte.getProductosMasVendidos()
        ) {

            escritor.verificarEspacio(22);

            escritor.escribirTexto(
                    posicion
                            + ". "
                            + textoSeguro(
                                    producto.getNombre()
                            )
                            + " | "
                            + valorEntero(
                                    producto.getCantidadVendida()
                            )
                            + " piezas | "
                            + formatearMoneda(
                                    producto.getMontoTotal()
                            )
            );

            posicion++;
        }

        escritor.saltarLinea(10);
    }

    private void escribirDetalleVentas(
            EscritorPdf escritor,
            ReporteVentasResponse reporte)
            throws IOException {

        escritor.escribirSeccion(
                "DETALLE DE VENTAS"
        );

        if (reporte.getVentas() == null
                || reporte.getVentas().isEmpty()) {

            escritor.escribirTexto(
                    "No existen ventas completadas en el periodo."
            );

            return;
        }

        escritor.escribirEncabezadoTabla();

        for (
                ReporteVentasResponse.VentaResumen venta
                : reporte.getVentas()
        ) {

            escritor.verificarEspacio(24);

            escritor.escribirFilaVenta(
                    "#" + venta.getVentaId(),
                    textoSeguro(venta.getFecha()),
                    textoSeguro(venta.getCajero()),
                    String.valueOf(
                            valorEntero(
                                    venta.getCantidadProductos()
                            )
                    ),
                    formatearMoneda(
                            venta.getTotal()
                    )
            );
        }
    }

    private void guardarCopiaLocal(
            byte[] contenido,
            ReporteVentasRequest request)
            throws IOException {

        LocalDateTime ahora =
                LocalDateTime.now();

        String nombreMes =
                ahora
                        .getMonth()
                        .getDisplayName(
                                TextStyle.FULL,
                                LOCALE_MEXICO
                        )
                        .toLowerCase(LOCALE_MEXICO);

        String carpetaMes =
                String.format(
                        "%02d-%s",
                        ahora.getMonthValue(),
                        nombreMes
                );

        Path carpetaDestino =
                rutaBaseReportes
                        .resolve(
                                String.valueOf(
                                        ahora.getYear()
                                )
                        )
                        .resolve(carpetaMes);

        Files.createDirectories(
                carpetaDestino
        );

        String nombreArchivo =
                "reporte-ventas-"
                        + request.getFechaInicio()
                        + "-a-"
                        + request.getFechaFin()
                        + "-"
                        + ahora.format(FORMATO_ARCHIVO)
                        + ".pdf";

        Path archivoDestino =
                carpetaDestino.resolve(
                        nombreArchivo
                );

        Files.write(
                archivoDestino,
                contenido
        );

        System.out.println(
                "PDF guardado correctamente en: "
                        + archivoDestino
        );
    }

    private String formatearMoneda(
            BigDecimal valor) {

        NumberFormat formato =
                NumberFormat.getCurrencyInstance(
                        LOCALE_MEXICO
                );

        return formato.format(
                valor != null
                        ? valor
                        : BigDecimal.ZERO
        );
    }

    private String formatearFecha(
            LocalDate fecha) {

        if (fecha == null) {
            return "-";
        }

        return fecha.format(
                FORMATO_FECHA
        );
    }

    private int valorEntero(
            Integer valor) {

        return valor != null
                ? valor
                : 0;
    }

    private String textoSeguro(
            String texto) {

        return texto != null
                ? texto
                : "";
    }

    private class EscritorPdf {

        private final PDDocument documento;

        private PDPage pagina;
        private PDPageContentStream contenido;

        private float posicionY;
        private int numeroPagina;

        private EscritorPdf(
                PDDocument documento)
                throws IOException {

            this.documento = documento;

            nuevaPagina();
        }

        private void nuevaPagina()
                throws IOException {

            if (contenido != null) {

                escribirNumeroPagina();

                contenido.close();
            }

            pagina =
                    new PDPage(
                            PDRectangle.A4
                    );

            documento.addPage(pagina);

            contenido =
                    new PDPageContentStream(
                            documento,
                            pagina
                    );

            posicionY =
                    pagina
                            .getMediaBox()
                            .getHeight()
                            - MARGEN_SUPERIOR;

            numeroPagina++;
        }

        private void cerrar()
                throws IOException {

            if (contenido != null) {

                escribirNumeroPagina();

                contenido.close();

                contenido = null;
            }
        }

        private void escribirTitulo(
                String texto)
                throws IOException {

            verificarEspacio(35);

            escribirLinea(
                    texto,
                    fuenteNegrita,
                    20,
                    MARGEN_IZQUIERDO
            );

            posicionY -= 5;
        }

        private void escribirSubtitulo(
                String texto)
                throws IOException {

            verificarEspacio(28);

            escribirLinea(
                    texto,
                    fuenteNegrita,
                    15,
                    MARGEN_IZQUIERDO
            );

            posicionY -= 4;
        }

        private void escribirSeccion(
                String texto)
                throws IOException {

            verificarEspacio(35);

            posicionY -= 3;

            establecerColorRelleno(
                    15,
                    52,
                    96
            );

            contenido.addRect(
                    MARGEN_IZQUIERDO,
                    posicionY - 15,
                    ANCHO_UTIL,
                    22
            );

            contenido.fill();

            establecerColorRelleno(
                    255,
                    255,
                    255
            );

            escribirLineaSinMover(
                    texto,
                    fuenteNegrita,
                    11,
                    MARGEN_IZQUIERDO + 8,
                    posicionY - 9
            );

            establecerColorRelleno(
                    0,
                    0,
                    0
            );

            posicionY -= 29;
        }

        private void escribirTexto(
                String texto)
                throws IOException {

            verificarEspacio(18);

            escribirLinea(
                    limpiarTexto(texto),
                    fuenteNormal,
                    9,
                    MARGEN_IZQUIERDO
            );
        }

        private void escribirTextoNegrita(
                String texto)
                throws IOException {

            verificarEspacio(18);

            escribirLinea(
                    limpiarTexto(texto),
                    fuenteNegrita,
                    10,
                    MARGEN_IZQUIERDO
            );
        }

        private void escribirTextoIndentado(
                String texto)
                throws IOException {

            verificarEspacio(16);

            escribirLinea(
                    "- " + limpiarTexto(texto),
                    fuenteNormal,
                    8,
                    MARGEN_IZQUIERDO + 18
            );
        }

        private void escribirSeparador()
                throws IOException {

            establecerColorBorde(
                    100,
                    116,
                    139
            );

            contenido.moveTo(
                    MARGEN_IZQUIERDO,
                    posicionY
            );

            contenido.lineTo(
                    ANCHO_PAGINA
                            - MARGEN_DERECHO,
                    posicionY
            );

            contenido.stroke();

            establecerColorBorde(
                    0,
                    0,
                    0
            );

            posicionY -= 6;
        }

        private void escribirEncabezadoTabla()
                throws IOException {

            verificarEspacio(28);

            establecerColorRelleno(
                    226,
                    232,
                    240
            );

            contenido.addRect(
                    MARGEN_IZQUIERDO,
                    posicionY - 14,
                    ANCHO_UTIL,
                    20
            );

            contenido.fill();

            establecerColorRelleno(
                    15,
                    23,
                    42
            );

            escribirLineaSinMover(
                    "ID",
                    fuenteNegrita,
                    8,
                    50,
                    posicionY - 8
            );

            escribirLineaSinMover(
                    "FECHA",
                    fuenteNegrita,
                    8,
                    100,
                    posicionY - 8
            );

            escribirLineaSinMover(
                    "CAJERO",
                    fuenteNegrita,
                    8,
                    180,
                    posicionY - 8
            );

            escribirLineaSinMover(
                    "PRODUCTOS",
                    fuenteNegrita,
                    8,
                    345,
                    posicionY - 8
            );

            escribirLineaSinMover(
                    "TOTAL",
                    fuenteNegrita,
                    8,
                    430,
                    posicionY - 8
            );

            establecerColorRelleno(
                    0,
                    0,
                    0
            );

            posicionY -= 24;
        }

        private void escribirFilaVenta(
                String id,
                String fecha,
                String cajero,
                String productos,
                String total)
                throws IOException {

            String cajeroCorto =
                    limitarTexto(
                            limpiarTexto(cajero),
                            25
                    );

            escribirLineaSinMover(
                    limpiarTexto(id),
                    fuenteNormal,
                    8,
                    50,
                    posicionY
            );

            escribirLineaSinMover(
                    limpiarTexto(fecha),
                    fuenteNormal,
                    8,
                    100,
                    posicionY
            );

            escribirLineaSinMover(
                    cajeroCorto,
                    fuenteNormal,
                    8,
                    180,
                    posicionY
            );

            escribirLineaSinMover(
                    limpiarTexto(productos),
                    fuenteNormal,
                    8,
                    365,
                    posicionY
            );

            escribirLineaSinMover(
                    limpiarTexto(total),
                    fuenteNormal,
                    8,
                    430,
                    posicionY
            );

            posicionY -= 17;
        }

        private void escribirLinea(
                String texto,
                PDType1Font fuente,
                float tamano,
                float posicionX)
                throws IOException {

            escribirLineaSinMover(
                    texto,
                    fuente,
                    tamano,
                    posicionX,
                    posicionY
            );

            posicionY -= tamano + 5;
        }

        private void escribirLineaSinMover(
                String texto,
                PDType1Font fuente,
                float tamano,
                float posicionX,
                float coordenadaY)
                throws IOException {

            contenido.beginText();

            contenido.setFont(
                    fuente,
                    tamano
            );

            contenido.newLineAtOffset(
                    posicionX,
                    coordenadaY
            );

            contenido.showText(
                    limpiarTexto(texto)
            );

            contenido.endText();
        }

        private void verificarEspacio(
                float espacioNecesario)
                throws IOException {

            if (
                    posicionY - espacioNecesario
                            < MARGEN_INFERIOR
            ) {

                nuevaPagina();
            }
        }

        private void saltarLinea(
                float espacio) {

            posicionY -= espacio;
        }

        private void escribirNumeroPagina()
                throws IOException {

            String texto =
                    "Pagina "
                            + numeroPagina;

            contenido.beginText();

            contenido.setFont(
                    fuenteNormal,
                    8
            );

            contenido.newLineAtOffset(
                    ANCHO_PAGINA
                            - MARGEN_DERECHO
                            - 45,
                    25
            );

            contenido.showText(texto);

            contenido.endText();
        }

        private void escribirPieFinal()
                throws IOException {

            verificarEspacio(35);

            posicionY -= 10;

            escribirSeparador();

            escribirTexto(
                    "Documento generado automaticamente por Abarrote Cloud."
            );
        }

        private void establecerColorRelleno(
                int rojo,
                int verde,
                int azul)
                throws IOException {

            contenido.setNonStrokingColor(
                    convertirColor(rojo),
                    convertirColor(verde),
                    convertirColor(azul)
            );
        }

        private void establecerColorBorde(
                int rojo,
                int verde,
                int azul)
                throws IOException {

            contenido.setStrokingColor(
                    convertirColor(rojo),
                    convertirColor(verde),
                    convertirColor(azul)
            );
        }

        private float convertirColor(
                int valor) {

            if (valor < 0) {
                return 0.0f;
            }

            if (valor > 255) {
                return 1.0f;
            }

            return valor / 255.0f;
        }

        private String limitarTexto(
                String texto,
                int longitudMaxima) {

            if (texto == null) {
                return "";
            }

            if (texto.length()
                    <= longitudMaxima) {

                return texto;
            }

            return texto.substring(
                    0,
                    longitudMaxima - 3
            ) + "...";
        }

        private String limpiarTexto(
                String texto) {

            if (texto == null) {
                return "";
            }

            return texto
                    .replace("–", "-")
                    .replace("—", "-")
                    .replace("“", "\"")
                    .replace("”", "\"")
                    .replace("‘", "'")
                    .replace("’", "'")
                    .replace("á", "a")
                    .replace("é", "e")
                    .replace("í", "i")
                    .replace("ó", "o")
                    .replace("ú", "u")
                    .replace("Á", "A")
                    .replace("É", "E")
                    .replace("Í", "I")
                    .replace("Ó", "O")
                    .replace("Ú", "U")
                    .replace("ñ", "n")
                    .replace("Ñ", "N")
                    .replace("\n", " ")
                    .replace("\r", " ");
        }
    }
}
