package com.abarrote.abarroteapi.service.importacion;

import com.abarrote.abarroteapi.dto.importacion.ImportacionProductosResultado;
import com.abarrote.abarroteapi.entity.Categoria;
import com.abarrote.abarroteapi.entity.InventarioSucursal;
import com.abarrote.abarroteapi.entity.Producto;
import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.repository.CategoriaRepository;
import com.abarrote.abarroteapi.repository.InventarioSucursalRepository;
import com.abarrote.abarroteapi.repository.ProductoRepository;
import com.abarrote.abarroteapi.repository.SucursalRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ImportacionProductosExcelService {

    private static final Set<String> ENCABEZADOS_OBLIGATORIOS =
            Set.of(
                    "codigo_barras",
                    "nombre",
                    "categoria",
                    "precio_compra",
                    "precio_venta",
                    "existencia",
                    "stock_minimo",
                    "sucursal_codigo",
                    "activo"
            );

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final SucursalRepository sucursalRepository;
    private final InventarioSucursalRepository inventarioRepository;

    public ImportacionProductosExcelService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            SucursalRepository sucursalRepository,
            InventarioSucursalRepository inventarioRepository) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.sucursalRepository = sucursalRepository;
        this.inventarioRepository = inventarioRepository;
    }

    @Transactional
    public ImportacionProductosResultado importar(
            MultipartFile archivo) {

        validarArchivo(archivo);

        List<FilaProductoExcel> filas =
                leerYValidarArchivo(archivo);

        ImportacionProductosResultado resultado =
                new ImportacionProductosResultado();

        resultado.setFilasLeidas(
                filas.size()
        );

        Map<String, Categoria> categoriasCache =
                new HashMap<>();

        Map<String, Sucursal> sucursalesCache =
                new HashMap<>();

        Set<Long> productosARecalcular =
                new LinkedHashSet<>();

        Map<Long, Producto> productosProcesados =
                new HashMap<>();

        for (FilaProductoExcel fila : filas) {

            Categoria categoria =
                    obtenerOCrearCategoria(
                            fila.categoria(),
                            categoriasCache,
                            resultado
                    );

            Sucursal sucursal =
                    obtenerSucursal(
                            fila.sucursalCodigo(),
                            sucursalesCache
                    );

            Optional<Producto> productoExistente =
                    productoRepository.findByCodigoBarras(
                            fila.codigoBarras()
                    );

            Producto producto;

            if (productoExistente.isPresent()) {

                producto = productoExistente.get();

                resultado
                        .incrementarProductosActualizados();

            } else {

                producto = new Producto();
                producto.setCodigoBarras(
                        fila.codigoBarras()
                );
                producto.setStock(0);

                resultado
                        .incrementarProductosNuevos();
            }

            producto.setNombre(
                    fila.nombre()
            );

            producto.setDescripcion(
                    fila.descripcion()
            );

            producto.setPrecioCompra(
                    fila.precioCompra()
            );

            producto.setPrecioVenta(
                    fila.precioVenta()
            );

            producto.setStockMinimo(
                    fila.stockMinimo()
            );

            producto.setCategoria(
                    categoria
            );

            producto.setActivo(
                    fila.activo()
            );

            if (producto.getStock() == null) {
                producto.setStock(0);
            }

            producto =
                    productoRepository.save(
                            producto
                    );

            Optional<InventarioSucursal> inventarioExistente =
                    inventarioRepository
                            .findBySucursalIdAndProductoId(
                                    sucursal.getId(),
                                    producto.getId()
                            );

            InventarioSucursal inventario;

            if (inventarioExistente.isPresent()) {

                inventario =
                        inventarioExistente.get();

                resultado
                        .incrementarInventariosActualizados();

            } else {

                inventario =
                        new InventarioSucursal();

                inventario.setSucursal(
                        sucursal
                );

                inventario.setProducto(
                        producto
                );

                resultado
                        .incrementarInventariosCreados();
            }

            inventario.setExistencia(
                    fila.existencia()
            );

            inventario.setStockMinimo(
                    fila.stockMinimo()
            );

            inventarioRepository.save(
                    inventario
            );

            productosARecalcular.add(
                    producto.getId()
            );

            productosProcesados.put(
                    producto.getId(),
                    producto
            );
        }

        inventarioRepository.flush();

        for (Long productoId : productosARecalcular) {

            Long existenciaTotal =
                    inventarioRepository
                            .obtenerExistenciaTotalProducto(
                                    productoId
                            );

            Producto producto =
                    productosProcesados.get(
                            productoId
                    );

            producto.setStock(
                    existenciaTotal == null
                            ? 0
                            : Math.toIntExact(
                                    existenciaTotal
                            )
            );

            productoRepository.save(
                    producto
            );
        }

        productoRepository.flush();

        resultado.agregarMensaje(
                "La carga fue procesada correctamente."
        );

        resultado.agregarMensaje(
                "El campo existencia reemplaza la existencia actual de la sucursal indicada."
        );

        resultado.agregarMensaje(
                "El stock general del producto fue recalculado con la suma de todas las sucursales."
        );

        return resultado;
    }

    private List<FilaProductoExcel> leerYValidarArchivo(
            MultipartFile archivo) {

        List<FilaProductoExcel> filas =
                new ArrayList<>();

        List<String> errores =
                new ArrayList<>();

        Set<String> clavesArchivo =
                new HashSet<>();

        try (
                InputStream entrada =
                        archivo.getInputStream();

                Workbook workbook =
                        WorkbookFactory.create(
                                entrada
                        )
        ) {

            Sheet sheet =
                    workbook.getSheet(
                            "Productos"
                    );

            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            if (sheet == null) {
                throw new IllegalArgumentException(
                        "El archivo no contiene hojas"
                );
            }

            Row filaEncabezados =
                    sheet.getRow(
                            sheet.getFirstRowNum()
                    );

            if (filaEncabezados == null) {
                throw new IllegalArgumentException(
                        "El archivo no contiene encabezados"
                );
            }

            Map<String, Integer> columnas =
                    leerEncabezados(
                            filaEncabezados
                    );

            validarEncabezados(
                    columnas
            );

            DataFormatter formatter =
                    new DataFormatter(
                            Locale.US
                    );

            for (
                    int numeroFila =
                    filaEncabezados.getRowNum() + 1;

                    numeroFila <= sheet.getLastRowNum();

                    numeroFila++
            ) {

                Row row =
                        sheet.getRow(
                                numeroFila
                        );

                if (filaVacia(
                        row,
                        formatter)) {

                    continue;
                }

                try {

                    FilaProductoExcel fila =
                            convertirFila(
                                    row,
                                    columnas,
                                    formatter
                            );

                    String clave =
                            fila.codigoBarras()
                                    + "|"
                                    + fila.sucursalCodigo();

                    if (!clavesArchivo.add(
                            clave)) {

                        throw new IllegalArgumentException(
                                "Código de barras y sucursal duplicados dentro del Excel"
                        );
                    }

                    filas.add(
                            fila
                    );

                } catch (Exception error) {

                    errores.add(
                            "Fila "
                                    + (numeroFila + 1)
                                    + ": "
                                    + error.getMessage()
                    );
                }
            }

        } catch (IllegalArgumentException error) {

            throw error;

        } catch (Exception error) {

            throw new IllegalArgumentException(
                    "No fue posible leer el archivo Excel: "
                            + error.getMessage(),
                    error
            );
        }

        if (filas.isEmpty() && errores.isEmpty()) {
            throw new IllegalArgumentException(
                    "El archivo no contiene productos"
            );
        }

        if (!errores.isEmpty()) {

            StringBuilder mensaje =
                    new StringBuilder(
                            "El Excel contiene errores. No se guardó ningún producto."
                    );

            int limite =
                    Math.min(
                            errores.size(),
                            25
                    );

            for (int i = 0; i < limite; i++) {

                mensaje.append("\n")
                        .append(errores.get(i));
            }

            if (errores.size() > limite) {

                mensaje.append("\n")
                        .append("Existen ")
                        .append(
                                errores.size() - limite
                        )
                        .append(
                                " errores adicionales."
                        );
            }

            throw new IllegalArgumentException(
                    mensaje.toString()
            );
        }

        return filas;
    }

    private FilaProductoExcel convertirFila(
            Row row,
            Map<String, Integer> columnas,
            DataFormatter formatter) {

        String codigoBarras =
                textoObligatorio(
                        row,
                        columnas,
                        "codigo_barras",
                        formatter
                );

        String nombre =
                textoObligatorio(
                        row,
                        columnas,
                        "nombre",
                        formatter
                );

        String descripcion =
                textoOpcional(
                        row,
                        columnas,
                        "descripcion",
                        formatter
                );

        String categoria =
                textoObligatorio(
                        row,
                        columnas,
                        "categoria",
                        formatter
                );

        BigDecimal precioCompra =
                decimalObligatorio(
                        row,
                        columnas,
                        "precio_compra",
                        formatter
                );

        BigDecimal precioVenta =
                decimalObligatorio(
                        row,
                        columnas,
                        "precio_venta",
                        formatter
                );

        Integer existencia =
                enteroObligatorio(
                        row,
                        columnas,
                        "existencia",
                        formatter
                );

        Integer stockMinimo =
                enteroObligatorio(
                        row,
                        columnas,
                        "stock_minimo",
                        formatter
                );

        String sucursalCodigo =
                textoObligatorio(
                        row,
                        columnas,
                        "sucursal_codigo",
                        formatter
                ).toUpperCase(
                        Locale.ROOT
                );

        String activoTexto =
                textoObligatorio(
                        row,
                        columnas,
                        "activo",
                        formatter
                );

        if (codigoBarras.length() > 50) {
            throw new IllegalArgumentException(
                    "codigo_barras excede 50 caracteres"
            );
        }

        if (nombre.length() > 100) {
            throw new IllegalArgumentException(
                    "nombre excede 100 caracteres"
            );
        }

        if (descripcion != null
                && descripcion.length() > 255) {

            throw new IllegalArgumentException(
                    "descripcion excede 255 caracteres"
            );
        }

        if (precioCompra.compareTo(
                BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "precio_compra no puede ser negativo"
            );
        }

        if (precioVenta.compareTo(
                BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "precio_venta no puede ser negativo"
            );
        }

        if (precioVenta.compareTo(
                precioCompra) < 0) {

            throw new IllegalArgumentException(
                    "precio_venta no puede ser menor que precio_compra"
            );
        }

        if (existencia < 0) {
            throw new IllegalArgumentException(
                    "existencia no puede ser negativa"
            );
        }

        if (stockMinimo < 0) {
            throw new IllegalArgumentException(
                    "stock_minimo no puede ser negativo"
            );
        }

        Boolean activo =
                convertirActivo(
                        activoTexto
                );

        Sucursal sucursal =
                sucursalRepository
                        .findByCodigoIgnoreCase(
                                sucursalCodigo
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "No existe la sucursal "
                                                        + sucursalCodigo
                                        )
                        );

        if (!Boolean.TRUE.equals(
                sucursal.getActiva())) {

            throw new IllegalArgumentException(
                    "La sucursal "
                            + sucursalCodigo
                            + " está inactiva"
            );
        }

        return new FilaProductoExcel(
                codigoBarras.trim(),
                nombre.trim(),
                descripcion,
                categoria.trim(),
                precioCompra,
                precioVenta,
                existencia,
                stockMinimo,
                sucursalCodigo,
                activo
        );
    }

    private Categoria obtenerOCrearCategoria(
            String nombre,
            Map<String, Categoria> cache,
            ImportacionProductosResultado resultado) {

        String clave =
                nombre.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        Categoria categoriaCache =
                cache.get(
                        clave
                );

        if (categoriaCache != null) {
            return categoriaCache;
        }

        Optional<Categoria> categoriaExistente =
                categoriaRepository
                        .findByNombreIgnoreCase(
                                nombre.trim()
                        );

        Categoria categoria;

        if (categoriaExistente.isPresent()) {

            categoria =
                    categoriaExistente.get();

        } else {

            categoria =
                    new Categoria();

            categoria.setNombre(
                    nombre.trim()
            );

            categoria.setDescripcion(
                    "Categoría creada automáticamente durante una importación Excel"
            );

            categoria =
                    categoriaRepository.save(
                            categoria
                    );

            resultado
                    .incrementarCategoriasCreadas();
        }

        cache.put(
                clave,
                categoria
        );

        return categoria;
    }

    private Sucursal obtenerSucursal(
            String codigo,
            Map<String, Sucursal> cache) {

        String clave =
                codigo.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        Sucursal sucursalCache =
                cache.get(
                        clave
                );

        if (sucursalCache != null) {
            return sucursalCache;
        }

        Sucursal sucursal =
                sucursalRepository
                        .findByCodigoIgnoreCase(
                                clave
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "No existe la sucursal "
                                                        + clave
                                        )
                        );

        if (!Boolean.TRUE.equals(
                sucursal.getActiva())) {

            throw new IllegalArgumentException(
                    "La sucursal "
                            + clave
                            + " está inactiva"
            );
        }

        cache.put(
                clave,
                sucursal
        );

        return sucursal;
    }

    private Map<String, Integer> leerEncabezados(
            Row row) {

        Map<String, Integer> columnas =
                new HashMap<>();

        DataFormatter formatter =
                new DataFormatter(
                        Locale.US
                );

        for (
                Cell cell : row
        ) {

            String encabezado =
                    normalizarEncabezado(
                            formatter.formatCellValue(
                                    cell
                            )
                    );

            if (!encabezado.isBlank()) {

                columnas.put(
                        encabezado,
                        cell.getColumnIndex()
                );
            }
        }

        return columnas;
    }

    private void validarEncabezados(
            Map<String, Integer> columnas) {

        Set<String> faltantes =
                new LinkedHashSet<>(
                        ENCABEZADOS_OBLIGATORIOS
                );

        faltantes.removeAll(
                columnas.keySet()
        );

        if (!faltantes.isEmpty()) {

            throw new IllegalArgumentException(
                    "Faltan las columnas obligatorias: "
                            + String.join(
                                    ", ",
                                    faltantes
                            )
            );
        }
    }

    private String textoObligatorio(
            Row row,
            Map<String, Integer> columnas,
            String columna,
            DataFormatter formatter) {

        String valor =
                leerTexto(
                        row,
                        columnas,
                        columna,
                        formatter
                );

        if (valor == null
                || valor.isBlank()) {

            throw new IllegalArgumentException(
                    columna
                            + " es obligatorio"
            );
        }

        return valor.trim();
    }

    private String textoOpcional(
            Row row,
            Map<String, Integer> columnas,
            String columna,
            DataFormatter formatter) {

        if (!columnas.containsKey(
                columna)) {

            return null;
        }

        String valor =
                leerTexto(
                        row,
                        columnas,
                        columna,
                        formatter
                );

        if (valor == null
                || valor.isBlank()) {

            return null;
        }

        return valor.trim();
    }

    private String leerTexto(
            Row row,
            Map<String, Integer> columnas,
            String columna,
            DataFormatter formatter) {

        Integer indice =
                columnas.get(
                        columna
                );

        if (indice == null) {
            return null;
        }

        Cell cell =
                row.getCell(
                        indice,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
                );

        if (cell == null) {
            return null;
        }

        return formatter
                .formatCellValue(
                        cell
                )
                .trim();
    }

    private BigDecimal decimalObligatorio(
            Row row,
            Map<String, Integer> columnas,
            String columna,
            DataFormatter formatter) {

        String texto =
                textoObligatorio(
                        row,
                        columnas,
                        columna,
                        formatter
                );

        try {

            String limpio =
                    texto
                            .replace("$", "")
                            .replace("MXN", "")
                            .replace("mxn", "")
                            .replace(" ", "")
                            .trim();

            if (limpio.contains(",")
                    && limpio.contains(".")) {

                limpio =
                        limpio.replace(
                                ",",
                                ""
                        );

            } else if (
                    limpio.contains(",")
            ) {

                limpio =
                        limpio.replace(
                                ",",
                                "."
                        );
            }

            return new BigDecimal(
                    limpio
            );

        } catch (Exception error) {

            throw new IllegalArgumentException(
                    columna
                            + " debe ser un número válido"
            );
        }
    }

    private Integer enteroObligatorio(
            Row row,
            Map<String, Integer> columnas,
            String columna,
            DataFormatter formatter) {

        BigDecimal valor =
                decimalObligatorio(
                        row,
                        columnas,
                        columna,
                        formatter
                );

        try {

            return valor.intValueExact();

        } catch (ArithmeticException error) {

            throw new IllegalArgumentException(
                    columna
                            + " debe ser un número entero"
            );
        }
    }

    private Boolean convertirActivo(
            String valor) {

        String normalizado =
                valor.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        return switch (
                normalizado
        ) {

            case "SI",
                 "SÍ",
                 "TRUE",
                 "1",
                 "ACTIVO" -> true;

            case "NO",
                 "FALSE",
                 "0",
                 "INACTIVO" -> false;

            default ->
                    throw new IllegalArgumentException(
                            "activo debe contener SI o NO"
                    );
        };
    }

    private boolean filaVacia(
            Row row,
            DataFormatter formatter) {

        if (row == null) {
            return true;
        }

        for (
                Cell cell : row
        ) {

            if (!formatter
                    .formatCellValue(
                            cell
                    )
                    .trim()
                    .isEmpty()) {

                return false;
            }
        }

        return true;
    }

    private String normalizarEncabezado(
            String valor) {

        String sinAcentos =
                Normalizer.normalize(
                                valor == null
                                        ? ""
                                        : valor,
                                Normalizer.Form.NFD
                        )
                        .replaceAll(
                                "\\p{M}",
                                ""
                        );

        return sinAcentos
                .trim()
                .toLowerCase(
                        Locale.ROOT
                )
                .replaceAll(
                        "[^a-z0-9]+",
                        "_"
                )
                .replaceAll(
                        "^_+|_+$",
                        ""
                );
    }

    private void validarArchivo(
            MultipartFile archivo) {

        if (archivo == null
                || archivo.isEmpty()) {

            throw new IllegalArgumentException(
                    "Selecciona un archivo Excel"
            );
        }

        String nombre =
                archivo.getOriginalFilename();

        if (nombre == null
                || !(
                nombre.toLowerCase(
                                Locale.ROOT
                        )
                        .endsWith(
                                ".xlsx"
                        )
                        || nombre.toLowerCase(
                                Locale.ROOT
                        )
                        .endsWith(
                                ".xls"
                        )
        )) {

            throw new IllegalArgumentException(
                    "El archivo debe tener extensión .xlsx o .xls"
            );
        }
    }

    private record FilaProductoExcel(
            String codigoBarras,
            String nombre,
            String descripcion,
            String categoria,
            BigDecimal precioCompra,
            BigDecimal precioVenta,
            Integer existencia,
            Integer stockMinimo,
            String sucursalCodigo,
            Boolean activo
    ) {
    }
}
