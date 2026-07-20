package com.abarrote.abarroteapi.gym.service;

import com.abarrote.abarroteapi.gym.domain.MetodoPagoGym;
import com.abarrote.abarroteapi.gym.domain.PlanMembresia;
import com.abarrote.abarroteapi.gym.dto.ClienteGymRequest;
import com.abarrote.abarroteapi.gym.dto.ClienteGymResponse;
import com.abarrote.abarroteapi.gym.dto.MembresiaRequest;
import com.abarrote.abarroteapi.gym.dto.MembresiaResponse;
import com.abarrote.abarroteapi.gym.dto.PagoMembresiaRequest;
import com.abarrote.abarroteapi.gym.repository.PlanMembresiaRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GymExcelService {

    private final GymComercialService comercialService;

    private final GymService gymService;

    private final PlanMembresiaRepository planRepository;

    private final DataFormatter formatter =
        new DataFormatter(
            new Locale("es", "MX")
        );

    public GymExcelService(
        GymComercialService comercialService,
        GymService gymService,
        PlanMembresiaRepository planRepository
    ) {
        this.comercialService = comercialService;
        this.gymService = gymService;
        this.planRepository = planRepository;
    }

    public GymExcelImportResult importarProductos(
        MultipartFile archivo,
        Long tenantId
    ) {
        validarArchivo(archivo);

        GymExcelImportResult resultado =
            new GymExcelImportResult();

        try (
            InputStream input =
                archivo.getInputStream();

            Workbook workbook =
                WorkbookFactory.create(input)
        ) {
            Sheet sheet = workbook.getSheetAt(0);

            int encabezado =
                localizarEncabezado(
                    sheet,
                    "codigo"
                );

            Map<String, Integer> columnas =
                obtenerColumnas(
                    sheet.getRow(encabezado)
                );

            exigirColumnas(
                columnas,
                "codigo",
                "nombre",
                "precio",
                "existencia",
                "stock_minimo"
            );

            for (
                int i = encabezado + 1;
                i <= sheet.getLastRowNum();
                i++
            ) {
                Row row = sheet.getRow(i);

                if (filaVacia(row)) {
                    continue;
                }

                resultado.procesado();

                try {
                    String codigo =
                        texto(
                            row,
                            columnas.get("codigo")
                        );

                    String nombre =
                        texto(
                            row,
                            columnas.get("nombre")
                        );

                    String categoria =
                        textoOpcional(
                            row,
                            columnas.get("categoria")
                        );

                    String descripcion =
                        textoOpcional(
                            row,
                            columnas.get("descripcion")
                        );

                    BigDecimal precio =
                        decimal(
                            row,
                            columnas.get("precio"),
                            true
                        );

                    BigDecimal costo =
                        decimal(
                            row,
                            columnas.get("costo"),
                            false
                        );

                    Integer existencia =
                        entero(
                            row,
                            columnas.get("existencia"),
                            0
                        );

                    Integer stockMinimo =
                        entero(
                            row,
                            columnas.get("stock_minimo"),
                            0
                        );

                    if (codigo.isBlank()) {
                        throw new IllegalArgumentException(
                            "codigo es obligatorio"
                        );
                    }

                    if (nombre.isBlank()) {
                        throw new IllegalArgumentException(
                            "nombre es obligatorio"
                        );
                    }

                    comercialService.crearProducto(
                        tenantId,
                        codigo,
                        nombre,
                        descripcion,
                        categoria,
                        precio,
                        costo,
                        existencia,
                        stockMinimo
                    );

                    resultado.importado();

                } catch (Exception exception) {
                    resultado.rechazado(
                        i + 1,
                        mensaje(exception)
                    );
                }
            }

            return resultado;

        } catch (Exception exception) {
            throw new IllegalArgumentException(
                "No fue posible leer el Excel: "
                    + mensaje(exception),
                exception
            );
        }
    }

    public GymExcelImportResult importarClientes(
        MultipartFile archivo,
        Long tenantId,
        String tenantSlug
    ) {
        validarArchivo(archivo);

        GymExcelImportResult resultado =
            new GymExcelImportResult();

        List<PlanMembresia> planes =
            planRepository
                .findAllByTenantIdAndActivoTrueOrderByPrecioAsc(
                    tenantId
                );

        try (
            InputStream input =
                archivo.getInputStream();

            Workbook workbook =
                WorkbookFactory.create(input)
        ) {
            Sheet sheet = workbook.getSheetAt(0);

            int encabezado =
                localizarEncabezado(
                    sheet,
                    "numero_cliente"
                );

            Map<String, Integer> columnas =
                obtenerColumnas(
                    sheet.getRow(encabezado)
                );

            exigirColumnas(
                columnas,
                "numero_cliente",
                "nombre"
            );

            for (
                int i = encabezado + 1;
                i <= sheet.getLastRowNum();
                i++
            ) {
                Row row = sheet.getRow(i);

                if (filaVacia(row)) {
                    continue;
                }

                resultado.procesado();

                try {
                    String numeroCliente =
                        texto(
                            row,
                            columnas.get(
                                "numero_cliente"
                            )
                        );

                    String nombre =
                        texto(
                            row,
                            columnas.get("nombre")
                        );

                    if (numeroCliente.isBlank()) {
                        throw new IllegalArgumentException(
                            "numero_cliente es obligatorio"
                        );
                    }

                    if (nombre.isBlank()) {
                        throw new IllegalArgumentException(
                            "nombre es obligatorio"
                        );
                    }

                    ClienteGymRequest request =
                        new ClienteGymRequest(
                            numeroCliente,
                            nombre,
                            textoOpcional(
                                row,
                                columnas.get(
                                    "apellido_paterno"
                                )
                            ),
                            textoOpcional(
                                row,
                                columnas.get(
                                    "apellido_materno"
                                )
                            ),
                            textoOpcional(
                                row,
                                columnas.get("telefono")
                            ),
                            textoOpcional(
                                row,
                                columnas.get("email")
                            ),
                            fecha(
                                row,
                                columnas.get(
                                    "fecha_nacimiento"
                                )
                            ),
                            textoOpcional(
                                row,
                                columnas.get(
                                    "contacto_emergencia"
                                )
                            ),
                            textoOpcional(
                                row,
                                columnas.get(
                                    "telefono_emergencia"
                                )
                            )
                        );

                    ClienteGymResponse cliente =
                        gymService.crearCliente(
                            tenantSlug,
                            request
                        );

                    String nombrePlan =
                        textoOpcional(
                            row,
                            columnas.get("plan")
                        );

                    if (!nombrePlan.isBlank()) {
                        PlanMembresia plan =
                            buscarPlan(
                                planes,
                                nombrePlan
                            );

                        LocalDate fechaInicio =
                            fecha(
                                row,
                                columnas.get(
                                    "fecha_inicio"
                                )
                            );

                        MembresiaResponse membresia =
                            gymService.crearMembresia(
                                tenantSlug,
                                new MembresiaRequest(
                                    cliente.id(),
                                    plan.getId(),
                                    fechaInicio
                                )
                            );

                        String metodo =
                            textoOpcional(
                                row,
                                columnas.get(
                                    "metodo_pago"
                                )
                            );

                        if (!metodo.isBlank()) {
                            MetodoPagoGym metodoPago =
                                MetodoPagoGym.valueOf(
                                    metodo
                                        .trim()
                                        .toUpperCase(
                                            Locale.ROOT
                                        )
                                );

                            gymService.registrarPago(
                                tenantSlug,
                                new PagoMembresiaRequest(
                                    membresia.id(),
                                    plan.getPrecio(),
                                    metodoPago,
                                    textoOpcional(
                                        row,
                                        columnas.get(
                                            "referencia"
                                        )
                                    ),
                                    "Carga masiva Excel"
                                )
                            );
                        }
                    }

                    resultado.importado();

                } catch (Exception exception) {
                    resultado.rechazado(
                        i + 1,
                        mensaje(exception)
                    );
                }
            }

            return resultado;

        } catch (Exception exception) {
            throw new IllegalArgumentException(
                "No fue posible leer el Excel: "
                    + mensaje(exception),
                exception
            );
        }
    }

    public byte[] plantillaProductos() {
        try (
            Workbook workbook =
                new XSSFWorkbook();

            ByteArrayOutputStream output =
                new ByteArrayOutputStream()
        ) {
            Sheet sheet =
                workbook.createSheet("Productos");

            String[] headers = {
                "codigo",
                "nombre",
                "categoria",
                "descripcion",
                "precio",
                "costo",
                "existencia",
                "stock_minimo"
            };

            Row header = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {
                header.createCell(i)
                    .setCellValue(headers[i]);
            }

            Object[][] data = {
                {
                    "AGUA001",
                    "Agua natural 1L",
                    "Bebidas",
                    "Botella de agua natural",
                    20.00,
                    10.00,
                    30,
                    5
                },
                {
                    "PROT001",
                    "Proteína Whey 2 lb",
                    "Suplementos",
                    "Proteína sabor chocolate",
                    850.00,
                    600.00,
                    10,
                    2
                },
                {
                    "SHAK001",
                    "Shaker 700 ml",
                    "Accesorios",
                    "Vaso mezclador",
                    120.00,
                    65.00,
                    15,
                    3
                }
            };

            escribirDatos(
                sheet,
                data
            );

            ajustarColumnas(
                sheet,
                headers.length
            );

            workbook.write(output);

            return output.toByteArray();

        } catch (Exception exception) {
            throw new IllegalStateException(
                "No fue posible generar plantilla",
                exception
            );
        }
    }

    public byte[] plantillaClientes() {
        try (
            Workbook workbook =
                new XSSFWorkbook();

            ByteArrayOutputStream output =
                new ByteArrayOutputStream()
        ) {
            Sheet sheet =
                workbook.createSheet("Clientes");

            String[] headers = {
                "numero_cliente",
                "nombre",
                "apellido_paterno",
                "apellido_materno",
                "telefono",
                "email",
                "fecha_nacimiento",
                "contacto_emergencia",
                "telefono_emergencia",
                "plan",
                "fecha_inicio",
                "metodo_pago",
                "referencia"
            };

            Row header = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {
                header.createCell(i)
                    .setCellValue(headers[i]);
            }

            Object[][] data = {
                {
                    "GYM-000101",
                    "Ana",
                    "López",
                    "Martínez",
                    "5511112233",
                    "ana@example.com",
                    "1993-05-18",
                    "María Martínez",
                    "5522223344",
                    "Mensual",
                    LocalDate.now().toString(),
                    "EFECTIVO",
                    "ALTA-101"
                },
                {
                    "GYM-000102",
                    "Carlos",
                    "Ramírez",
                    "Hernández",
                    "5533334455",
                    "carlos@example.com",
                    "1988-11-02",
                    "Laura Hernández",
                    "5544445566",
                    "Anual",
                    LocalDate.now().toString(),
                    "TRANSFERENCIA",
                    "TRX-102"
                }
            };

            escribirDatos(
                sheet,
                data
            );

            ajustarColumnas(
                sheet,
                headers.length
            );

            workbook.write(output);

            return output.toByteArray();

        } catch (Exception exception) {
            throw new IllegalStateException(
                "No fue posible generar plantilla",
                exception
            );
        }
    }

    private void escribirDatos(
        Sheet sheet,
        Object[][] data
    ) {
        for (int r = 0; r < data.length; r++) {
            Row row = sheet.createRow(r + 1);

            for (
                int c = 0;
                c < data[r].length;
                c++
            ) {
                Object value = data[r][c];

                if (value instanceof Number number) {
                    row.createCell(c)
                        .setCellValue(
                            number.doubleValue()
                        );
                } else {
                    row.createCell(c)
                        .setCellValue(
                            value != null
                                ? value.toString()
                                : ""
                        );
                }
            }
        }
    }

    private void ajustarColumnas(
        Sheet sheet,
        int totalColumnas
    ) {
        for (int i = 0; i < totalColumnas; i++) {
            sheet.autoSizeColumn(i);

            int width = Math.min(
                sheet.getColumnWidth(i) + 1500,
                12000
            );

            sheet.setColumnWidth(i, width);
        }

        sheet.createFreezePane(0, 1);
    }

    private int localizarEncabezado(
        Sheet sheet,
        String primeraColumna
    ) {
        int limite = Math.min(
            sheet.getLastRowNum(),
            10
        );

        for (int i = 0; i <= limite; i++) {
            Row row = sheet.getRow(i);

            if (row == null) {
                continue;
            }

            for (Cell cell : row) {
                if (
                    normalizar(
                        formatter.formatCellValue(cell)
                    ).equals(
                        normalizar(primeraColumna)
                    )
                ) {
                    return i;
                }
            }
        }

        throw new IllegalArgumentException(
            "No se encontró el encabezado "
                + primeraColumna
        );
    }

    private Map<String, Integer> obtenerColumnas(
        Row row
    ) {
        if (row == null) {
            throw new IllegalArgumentException(
                "Fila de encabezados vacía"
            );
        }

        Map<String, Integer> columnas =
            new HashMap<>();

        for (Cell cell : row) {
            String header =
                normalizar(
                    formatter.formatCellValue(cell)
                );

            if (!header.isBlank()) {
                columnas.put(
                    header,
                    cell.getColumnIndex()
                );
            }
        }

        return columnas;
    }

    private void exigirColumnas(
        Map<String, Integer> columnas,
        String... nombres
    ) {
        for (String nombre : nombres) {
            if (!columnas.containsKey(nombre)) {
                throw new IllegalArgumentException(
                    "Falta la columna: " + nombre
                );
            }
        }
    }

    private boolean filaVacia(Row row) {
        if (row == null) {
            return true;
        }

        for (Cell cell : row) {
            if (
                !formatter
                    .formatCellValue(cell)
                    .trim()
                    .isBlank()
            ) {
                return false;
            }
        }

        return true;
    }

    private String texto(
        Row row,
        Integer columna
    ) {
        if (columna == null) {
            return "";
        }

        Cell cell = row.getCell(
            columna,
            Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
        );

        if (cell == null) {
            return "";
        }

        return formatter
            .formatCellValue(cell)
            .trim();
    }

    private String textoOpcional(
        Row row,
        Integer columna
    ) {
        return texto(row, columna);
    }

    private BigDecimal decimal(
        Row row,
        Integer columna,
        boolean requerido
    ) {
        String value =
            texto(row, columna)
                .replace("$", "")
                .replace(",", "")
                .trim();

        if (value.isBlank()) {
            if (requerido) {
                throw new IllegalArgumentException(
                    "valor numérico obligatorio"
                );
            }

            return null;
        }

        return new BigDecimal(value);
    }

    private Integer entero(
        Row row,
        Integer columna,
        int defecto
    ) {
        String value =
            texto(row, columna)
                .replace(",", "")
                .trim();

        if (value.isBlank()) {
            return defecto;
        }

        return new BigDecimal(value).intValueExact();
    }

    private LocalDate fecha(
        Row row,
        Integer columna
    ) {
        if (columna == null) {
            return null;
        }

        Cell cell = row.getCell(
            columna,
            Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
        );

        if (cell == null) {
            return null;
        }

        if (
            cell.getCellType()
                == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell)
        ) {
            return cell
                .getDateCellValue()
                .toInstant()
                .atZone(
                    ZoneId.systemDefault()
                )
                .toLocalDate();
        }

        String value =
            formatter
                .formatCellValue(cell)
                .trim();

        if (value.isBlank()) {
            return null;
        }

        return LocalDate.parse(value);
    }

    private PlanMembresia buscarPlan(
        List<PlanMembresia> planes,
        String nombre
    ) {
        return planes
            .stream()
            .filter(
                plan ->
                    plan.getNombre()
                        .equalsIgnoreCase(
                            nombre.trim()
                        )
            )
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "No existe el plan: " + nombre
                )
            );
    }

    private void validarArchivo(
        MultipartFile archivo
    ) {
        if (
            archivo == null
                || archivo.isEmpty()
        ) {
            throw new IllegalArgumentException(
                "Selecciona un archivo Excel"
            );
        }

        String filename =
            archivo.getOriginalFilename();

        if (
            filename == null
                || !filename
                    .toLowerCase(Locale.ROOT)
                    .endsWith(".xlsx")
        ) {
            throw new IllegalArgumentException(
                "El archivo debe tener extensión .xlsx"
            );
        }
    }

    private String normalizar(String value) {
        return value == null
            ? ""
            : value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace(" ", "_");
    }

    private String mensaje(Throwable throwable) {
        Throwable actual = throwable;

        while (
            actual.getCause() != null
                && actual.getCause() != actual
        ) {
            actual = actual.getCause();
        }

        String message = actual.getMessage();

        return message != null
            ? message
            : actual.getClass().getSimpleName();
    }
}
