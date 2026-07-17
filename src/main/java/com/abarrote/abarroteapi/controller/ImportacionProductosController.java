package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.importacion.ImportacionProductosResultado;
import com.abarrote.abarroteapi.service.importacion.ImportacionProductosExcelService;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ImportacionProductosController {

    private static final String[] ENCABEZADOS = {
            "codigo_barras",
            "nombre",
            "descripcion",
            "categoria",
            "precio_compra",
            "precio_venta",
            "existencia",
            "stock_minimo",
            "sucursal_codigo",
            "activo"
    };

    private final ImportacionProductosExcelService importacionService;

    public ImportacionProductosController(
            ImportacionProductosExcelService importacionService) {

        this.importacionService =
                importacionService;
    }

    @PostMapping(
            value = "/importar-excel",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> importarExcel(
            @RequestParam("archivo")
            MultipartFile archivo) {

        try {

            ImportacionProductosResultado resultado =
                    importacionService.importar(
                            archivo
                    );

            Map<String, Object> respuesta =
                    new LinkedHashMap<>();

            respuesta.put(
                    "success",
                    true
            );

            respuesta.put(
                    "message",
                    "Importación realizada correctamente"
            );

            respuesta.put(
                    "data",
                    resultado
            );

            return ResponseEntity.ok(
                    respuesta
            );

        } catch (IllegalArgumentException error) {

            Map<String, Object> respuesta =
                    new LinkedHashMap<>();

            respuesta.put(
                    "success",
                    false
            );

            respuesta.put(
                    "message",
                    error.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(
                            respuesta
                    );

        } catch (Exception error) {

            Map<String, Object> respuesta =
                    new LinkedHashMap<>();

            respuesta.put(
                    "success",
                    false
            );

            respuesta.put(
                    "message",
                    "No fue posible importar el archivo. Revisa los datos y vuelve a intentarlo."
            );

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            respuesta
                    );
        }
    }

    @GetMapping(
            value = "/plantilla-excel",
            produces =
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    public ResponseEntity<byte[]> descargarPlantilla()
            throws Exception {

        try (
                Workbook workbook =
                        new XSSFWorkbook();

                ByteArrayOutputStream salida =
                        new ByteArrayOutputStream()
        ) {

            Sheet productos =
                    workbook.createSheet(
                            "Productos"
                    );

            Sheet instrucciones =
                    workbook.createSheet(
                            "Instrucciones"
                    );

            CellStyle encabezadoStyle =
                    workbook.createCellStyle();

            Font encabezadoFont =
                    workbook.createFont();

            encabezadoFont.setBold(
                    true
            );

            encabezadoStyle.setFont(
                    encabezadoFont
            );

            Row encabezado =
                    productos.createRow(
                            0
                    );

            for (
                    int columna = 0;
                    columna < ENCABEZADOS.length;
                    columna++
            ) {

                encabezado
                        .createCell(
                                columna
                        )
                        .setCellValue(
                                ENCABEZADOS[columna]
                        );

                encabezado
                        .getCell(
                                columna
                        )
                        .setCellStyle(
                                encabezadoStyle
                        );
            }

            crearFilaEjemplo(
                    productos,
                    1,
                    "750100000001",
                    "Coca-Cola 600 ml",
                    "Refresco botella 600 ml",
                    "Bebidas",
                    14.00,
                    19.00,
                    48,
                    12,
                    "MAT",
                    "SI"
            );

            crearFilaEjemplo(
                    productos,
                    2,
                    "750100000002",
                    "Arroz 1 kg",
                    "Arroz empacado",
                    "Abarrotes",
                    28.00,
                    38.00,
                    25,
                    6,
                    "MAT",
                    "SI"
            );

            productos.createFreezePane(
                    0,
                    1
            );

            int[] anchos = {
                    20,
                    30,
                    35,
                    20,
                    16,
                    16,
                    14,
                    14,
                    18,
                    12
            };

            for (
                    int columna = 0;
                    columna < anchos.length;
                    columna++
            ) {

                productos.setColumnWidth(
                        columna,
                        anchos[columna] * 256
                );
            }

            instrucciones
                    .createRow(
                            0
                    )
                    .createCell(
                            0
                    )
                    .setCellValue(
                            "INSTRUCCIONES PARA CARGA MASIVA"
                    );

            instrucciones
                    .createRow(
                            2
                    )
                    .createCell(
                            0
                    )
                    .setCellValue(
                            "1. No cambies los nombres de los encabezados."
                    );

            instrucciones
                    .createRow(
                            3
                    )
                    .createCell(
                            0
                    )
                    .setCellValue(
                            "2. El código de barras identifica de forma única al producto."
                    );

            instrucciones
                    .createRow(
                            4
                    )
                    .createCell(
                            0
                    )
                    .setCellValue(
                            "3. La existencia reemplaza el inventario de la sucursal indicada."
                    );

            instrucciones
                    .createRow(
                            5
                    )
                    .createCell(
                            0
                    )
                    .setCellValue(
                            "4. La categoría se crea automáticamente si todavía no existe."
                    );

            instrucciones
                    .createRow(
                            6
                    )
                    .createCell(
                            0
                    )
                    .setCellValue(
                            "5. La sucursal debe existir y estar activa."
                    );

            instrucciones
                    .createRow(
                            7
                    )
                    .createCell(
                            0
                    )
                    .setCellValue(
                            "6. Para la sucursal Matriz utiliza el código MAT."
                    );

            instrucciones
                    .createRow(
                            8
                    )
                    .createCell(
                            0
                    )
                    .setCellValue(
                            "7. activo acepta SI o NO."
                    );

            instrucciones.setColumnWidth(
                    0,
                    100 * 256
            );

            workbook.write(
                    salida
            );

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
            );

            headers.setContentDisposition(
                    ContentDisposition
                            .attachment()
                            .filename(
                                    "plantilla_carga_productos.xlsx",
                                    StandardCharsets.UTF_8
                            )
                            .build()
            );

            return new ResponseEntity<>(
                    salida.toByteArray(),
                    headers,
                    HttpStatus.OK
            );
        }
    }

    private void crearFilaEjemplo(
            Sheet sheet,
            int numeroFila,
            String codigoBarras,
            String nombre,
            String descripcion,
            String categoria,
            double precioCompra,
            double precioVenta,
            int existencia,
            int stockMinimo,
            String sucursal,
            String activo) {

        Row fila =
                sheet.createRow(
                        numeroFila
                );

        fila.createCell(0)
                .setCellValue(
                        codigoBarras
                );

        fila.createCell(1)
                .setCellValue(
                        nombre
                );

        fila.createCell(2)
                .setCellValue(
                        descripcion
                );

        fila.createCell(3)
                .setCellValue(
                        categoria
                );

        fila.createCell(4)
                .setCellValue(
                        precioCompra
                );

        fila.createCell(5)
                .setCellValue(
                        precioVenta
                );

        fila.createCell(6)
                .setCellValue(
                        existencia
                );

        fila.createCell(7)
                .setCellValue(
                        stockMinimo
                );

        fila.createCell(8)
                .setCellValue(
                        sucursal
                );

        fila.createCell(9)
                .setCellValue(
                        activo
                );
    }
}
