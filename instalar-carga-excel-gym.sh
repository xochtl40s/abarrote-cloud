#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT="/home/tonatiuh/commerce-cloud"
JAVA_BASE="src/main/java/com/abarrote/abarroteapi"
GYM_BASE="$JAVA_BASE/gym"
TEMPLATES="src/main/resources/templates/gym"
POM="pom.xml"

TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP=".backups/carga-excel-gym-${TIMESTAMP}"

mostrar_error() {
    echo
    echo "============================================================"
    echo " ERROR INSTALANDO CARGA EXCEL GYM"
    echo "============================================================"
    echo "Línea: $1"
    echo "Comando: $2"
}

trap 'mostrar_error "$LINENO" "$BASH_COMMAND"' ERR

cd "$PROJECT"

echo "============================================================"
echo " COMMERCE CLOUD - GYM CLOUD"
echo " Carga masiva Excel"
echo " Productos + Clientes + Membresías + Pagos"
echo "============================================================"

test -f "$POM"
test -f "$GYM_BASE/service/GymComercialService.java"
test -f "$GYM_BASE/service/GymService.java"
test -f "$GYM_BASE/repository/PlanMembresiaRepository.java"
test -f "$TEMPLATES/productos.html"
test -f "$TEMPLATES/clientes.html"

mkdir -p \
    "$BACKUP" \
    "$GYM_BASE/service" \
    "$GYM_BASE/web"

cp "$POM" "$BACKUP/pom.xml.bak"
cp "$TEMPLATES/productos.html" \
   "$BACKUP/productos.html.bak"
cp "$TEMPLATES/clientes.html" \
   "$BACKUP/clientes.html.bak"

echo
echo "[1/6] Agregando Apache POI..."

python3 <<'PY'
from pathlib import Path

pom_path = Path("pom.xml")
pom = pom_path.read_text(encoding="utf-8")

if "<artifactId>poi-ooxml</artifactId>" not in pom:
    dependency = """
        <!-- Lectura y generación de archivos Excel para Gym Cloud -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
            <version>5.3.0</version>
        </dependency>
"""

    marker = "</dependencies>"

    if marker not in pom:
        raise SystemExit(
            "ERROR: no se encontró </dependencies> en pom.xml"
        )

    pom = pom.replace(
        marker,
        dependency + "\n    " + marker,
        1
    )

    pom_path.write_text(
        pom,
        encoding="utf-8"
    )

    print("Apache POI agregado.")
else:
    print("Apache POI ya estaba agregado.")
PY

echo
echo "[2/6] Creando resultado de importación..."

cat > "$GYM_BASE/service/GymExcelImportResult.java" <<'EOF'
package com.abarrote.abarroteapi.gym.service;

import java.util.ArrayList;
import java.util.List;

public class GymExcelImportResult {

    private int procesados;

    private int importados;

    private int rechazados;

    private final List<String> errores =
        new ArrayList<>();

    public void procesado() {
        procesados++;
    }

    public void importado() {
        importados++;
    }

    public void rechazado(
        int fila,
        String mensaje
    ) {
        rechazados++;

        if (errores.size() < 15) {
            errores.add(
                "Fila " + fila + ": " + mensaje
            );
        }
    }

    public int getProcesados() {
        return procesados;
    }

    public int getImportados() {
        return importados;
    }

    public int getRechazados() {
        return rechazados;
    }

    public List<String> getErrores() {
        return errores;
    }

    public String getResumen() {
        return "Procesados: " + procesados
            + ", importados: " + importados
            + ", rechazados: " + rechazados;
    }
}
EOF

echo
echo "[3/6] Creando servicio Excel..."

cat > "$GYM_BASE/service/GymExcelService.java" <<'EOF'
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
EOF

echo
echo "[4/6] Creando controlador de carga Excel..."

cat > "$GYM_BASE/web/GymExcelController.java" <<'EOF'
package com.abarrote.abarroteapi.gym.web;

import com.abarrote.abarroteapi.gym.service.GymExcelImportResult;
import com.abarrote.abarroteapi.gym.service.GymExcelService;
import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GymExcelController {

    private static final String XLSX =
        "application/vnd.openxmlformats-officedocument"
            + ".spreadsheetml.sheet";

    private final GymExcelService excelService;

    public GymExcelController(
        GymExcelService excelService
    ) {
        this.excelService = excelService;
    }

    @GetMapping("/gym/plantillas/productos")
    public ResponseEntity<byte[]>
        descargarProductos(
            @AuthenticationPrincipal
            CommerceUserPrincipal principal
        ) {

        validarGym(principal);

        return descargar(
            excelService.plantillaProductos(),
            "GymCloud_Productos_Muestra.xlsx"
        );
    }

    @GetMapping("/gym/plantillas/clientes")
    public ResponseEntity<byte[]>
        descargarClientes(
            @AuthenticationPrincipal
            CommerceUserPrincipal principal
        ) {

        validarGym(principal);

        return descargar(
            excelService.plantillaClientes(),
            "GymCloud_Clientes_Muestra.xlsx"
        );
    }

    @PostMapping("/gym/productos/importar")
    public String importarProductos(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,

        @RequestParam("archivo")
        MultipartFile archivo,

        RedirectAttributes redirect
    ) {
        validarGym(principal);

        try {
            GymExcelImportResult result =
                excelService.importarProductos(
                    archivo,
                    principal.getTenantId()
                );

            redirect.addFlashAttribute(
                "excelExito",
                result.getResumen()
            );

            redirect.addFlashAttribute(
                "excelErrores",
                result.getErrores()
            );

        } catch (Exception exception) {
            redirect.addFlashAttribute(
                "excelError",
                exception.getMessage()
            );
        }

        return "redirect:/gym/productos";
    }

    @PostMapping("/gym/clientes/importar")
    public String importarClientes(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,

        @RequestParam("archivo")
        MultipartFile archivo,

        RedirectAttributes redirect
    ) {
        validarGym(principal);

        try {
            GymExcelImportResult result =
                excelService.importarClientes(
                    archivo,
                    principal.getTenantId(),
                    principal.getTenantSlug()
                );

            redirect.addFlashAttribute(
                "excelExito",
                result.getResumen()
            );

            redirect.addFlashAttribute(
                "excelErrores",
                result.getErrores()
            );

        } catch (Exception exception) {
            redirect.addFlashAttribute(
                "excelError",
                exception.getMessage()
            );
        }

        return "redirect:/gym/clientes";
    }

    private ResponseEntity<byte[]> descargar(
        byte[] contenido,
        String filename
    ) {
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\""
                    + filename
                    + "\""
            )
            .contentType(
                MediaType.parseMediaType(XLSX)
            )
            .body(contenido);
    }

    private void validarGym(
        CommerceUserPrincipal principal
    ) {
        if (
            principal == null
                || !principal.esGym()
        ) {
            throw new IllegalArgumentException(
                "Acceso exclusivo de Gym Cloud"
            );
        }
    }
}
EOF

echo
echo "[5/6] Agregando botones a las pantallas..."

python3 <<'PY'
from pathlib import Path

def patch_template(path_string, title_text, download_url, upload_url):
    path = Path(path_string)
    html = path.read_text(encoding="utf-8")

    marker = f"<h1>{title_text}</h1>"

    if marker not in html:
        raise SystemExit(
            f"ERROR: no se encontró el título esperado en {path}"
        )

    if upload_url in html:
        print(f"{path}: botones ya instalados.")
        return

    block = f"""
<div class="excel-panel">

    <div>
        <h2>📊 Carga masiva desde Excel</h2>

        <p>
            Descarga la plantilla, llena los datos
            y carga el archivo en formato .xlsx.
        </p>
    </div>

    <div class="excel-actions">

        <a
            class="excel-download"
            href="{download_url}">
            ⬇ Descargar Excel de muestra
        </a>

        <form
            class="excel-form"
            method="post"
            enctype="multipart/form-data"
            action="{upload_url}">

            <input
                type="file"
                name="archivo"
                accept=".xlsx"
                required>

            <button type="submit">
                ⬆ Cargar Excel
            </button>

        </form>

    </div>

</div>

<div
    class="excel-message excel-success"
    th:if="${{excelExito}}">

    <strong th:text="${{excelExito}}"></strong>

    <ul
        th:if="${{excelErrores != null
            and !excelErrores.isEmpty()}}">

        <li
            th:each="error : ${{excelErrores}}"
            th:text="${{error}}">
        </li>

    </ul>

</div>

<div
    class="excel-message excel-error"
    th:if="${{excelError}}"
    th:text="${{excelError}}">
</div>
"""

    style_marker = "</style>"

    styles = """
        .excel-panel {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 1rem;
            margin: 1rem 0;
            padding: 1.2rem;
            border: 1px solid #6d28d9;
            border-radius: 14px;
            background: rgba(109, 40, 217, 0.12);
        }

        .excel-panel h2 {
            margin: 0 0 0.35rem 0;
        }

        .excel-panel p {
            margin: 0;
            color: #cbd5e1;
        }

        .excel-actions {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 0.7rem;
        }

        .excel-download {
            display: inline-block;
            padding: 0.75rem 1rem;
            border-radius: 8px;
            color: white;
            text-decoration: none;
            font-weight: 700;
            background: #6d28d9;
        }

        .excel-form {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            width: auto;
            margin: 0;
            padding: 0;
            background: transparent;
        }

        .excel-form input[type="file"] {
            max-width: 260px;
        }

        .excel-message {
            margin: 1rem 0;
            padding: 1rem;
            border-radius: 10px;
        }

        .excel-success {
            color: #bbf7d0;
            border: 1px solid #16a34a;
            background: rgba(22, 163, 74, 0.14);
        }

        .excel-error {
            color: #fecaca;
            border: 1px solid #dc2626;
            background: rgba(220, 38, 38, 0.14);
        }

        @media (max-width: 800px) {
            .excel-panel {
                display: block;
            }

            .excel-actions {
                margin-top: 1rem;
            }

            .excel-form {
                display: block;
            }

            .excel-form input,
            .excel-form button {
                width: 100%;
                margin: 0.3rem 0;
            }
        }
"""

    html = html.replace(
        style_marker,
        styles + "\n" + style_marker,
        1
    )

    html = html.replace(
        marker,
        marker + "\n" + block,
        1
    )

    path.write_text(
        html,
        encoding="utf-8"
    )

    print(f"{path}: botones instalados.")

patch_template(
    "src/main/resources/templates/gym/productos.html",
    "Productos de Gym Cloud",
    "/gym/plantillas/productos",
    "/gym/productos/importar"
)

patch_template(
    "src/main/resources/templates/gym/clientes.html",
    "Clientes del gimnasio",
    "/gym/plantillas/clientes",
    "/gym/clientes/importar"
)
PY

echo
echo "[6/6] Validando y compilando..."

grep -RniE \
    '^(<<<<<<<|=======|>>>>>>>)' \
    "$GYM_BASE" \
    "$TEMPLATES" \
    && {
        echo "ERROR: existen conflictos Git."
        exit 1
    } || true

git diff --check

mvn clean compile

echo
echo "============================================================"
echo " CARGA EXCEL GYM INSTALADA"
echo "============================================================"

echo
echo "Rutas nuevas:"
echo "  GET  /gym/plantillas/productos"
echo "  POST /gym/productos/importar"
echo "  GET  /gym/plantillas/clientes"
echo "  POST /gym/clientes/importar"

echo
echo "Respaldo:"
echo "  $BACKUP"

echo
echo "Estado Git:"
git status --short
