package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.DashboardProductoTopResponse;
import com.abarrote.abarroteapi.dto.DashboardVentaDiaResponse;
import com.abarrote.abarroteapi.dto.ProductoResponse;
import com.abarrote.abarroteapi.dto.ReporteVentasRequest;
import com.abarrote.abarroteapi.dto.ReporteVentasResponse;
import com.abarrote.abarroteapi.entity.Venta;
import com.abarrote.abarroteapi.repository.DetalleVentaRepository;
import com.abarrote.abarroteapi.repository.VentaRepository;
import com.abarrote.abarroteapi.service.CategoriaService;
import com.abarrote.abarroteapi.service.ProductoService;
import com.abarrote.abarroteapi.service.ReporteService;
import com.abarrote.abarroteapi.service.SucursalService;
import com.abarrote.abarroteapi.service.UsuarioService;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductoService productoService;

    private final UsuarioService usuarioService;

    private final CategoriaService categoriaService;

    private final ReporteService reporteService;

    private final SucursalService sucursalService;

    private final VentaRepository ventaRepository;

    private final DetalleVentaRepository
            detalleVentaRepository;

    public AdminController(
            ProductoService productoService,
            UsuarioService usuarioService,
            CategoriaService categoriaService,
            ReporteService reporteService,
            SucursalService sucursalService,
            VentaRepository ventaRepository,
            DetalleVentaRepository detalleVentaRepository) {

        this.productoService = productoService;

        this.usuarioService = usuarioService;

        this.categoriaService = categoriaService;

        this.reporteService = reporteService;

        this.sucursalService = sucursalService;

        this.ventaRepository = ventaRepository;

        this.detalleVentaRepository =
                detalleVentaRepository;
    }

    // ============================================================
    // DASHBOARD
    // ============================================================

    @GetMapping
    public String dashboard(
            Model model) {

        LocalDate hoy =
                LocalDate.now();

        BigDecimal totalVentasHoy =
                valorSeguro(
                        reporteService
                                .obtenerTotalVentasDelDia(
                                        hoy
                                )
                );

        List<ProductoResponse> productosActivos =
                productoService.listarTodos();

        List<ProductoResponse> productosStockBajo =
                productoService.listarStockBajo();

        List<ProductoResponse> productosAgotados =
                productoService.listarAgotados();

        List<Venta> ventasHoy =
                ventaRepository
                        .findByFechaHoraBetweenOrderByFechaHoraDesc(
                                hoy.atStartOfDay(),
                                hoy.atTime(
                                        LocalTime.MAX
                                )
                        )
                        .stream()
                        .filter(
                                venta ->
                                        venta.getEstado()
                                                == Venta
                                                .EstadoVenta
                                                .COMPLETADA
                        )
                        .toList();

        List<DashboardVentaDiaResponse>
                ventasUltimosSieteDias =
                construirVentasUltimosSieteDias(
                        hoy
                );

        List<DashboardProductoTopResponse>
                productosMasVendidos =
                obtenerProductosMasVendidos(
                        hoy
                );

        BigDecimal totalUltimosSieteDias =
                ventasUltimosSieteDias
                        .stream()
                        .map(
                                DashboardVentaDiaResponse
                                        ::getTotal
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        int operacionesUltimosSieteDias =
                ventasUltimosSieteDias
                        .stream()
                        .mapToInt(
                                DashboardVentaDiaResponse
                                        ::getNumeroVentas
                        )
                        .sum();

        model.addAttribute(
                "totalVentasHoy",
                totalVentasHoy
        );

        model.addAttribute(
                "numeroVentasHoy",
                ventasHoy.size()
        );

        model.addAttribute(
                "totalProductos",
                productosActivos.size()
        );

        model.addAttribute(
                "cantidadStockBajo",
                productoService.contarStockBajo()
        );

        model.addAttribute(
                "cantidadAgotados",
                productoService.contarAgotados()
        );

        model.addAttribute(
                "totalUsuarios",
                usuarioService
                        .listarActivos()
                        .size()
        );

        model.addAttribute(
                "productosStockBajo",
                productosStockBajo
        );

        model.addAttribute(
                "productosAgotados",
                productosAgotados
        );

        model.addAttribute(
                "ventasUltimosSieteDias",
                ventasUltimosSieteDias
        );

        model.addAttribute(
                "productosMasVendidos",
                productosMasVendidos
        );

        model.addAttribute(
                "totalUltimosSieteDias",
                totalUltimosSieteDias
        );

        model.addAttribute(
                "operacionesUltimosSieteDias",
                operacionesUltimosSieteDias
        );

        model.addAttribute(
                "activePage",
                "dashboard"
        );

        return "admin/dashboard";
    }

    // ============================================================
    // PRODUCTOS
    // ============================================================

    @GetMapping("/productos")
    public String listarProductos(
            Model model) {

        model.addAttribute(
                "productos",
                productoService.listarTodos()
        );

        model.addAttribute(
                "categorias",
                categoriaService.listarTodas()
        );

        model.addAttribute(
                "activePage",
                "productos"
        );

        return "admin/productos";
    }

    // ============================================================
    // USUARIOS
    // ============================================================

    @GetMapping("/usuarios")
    public String listarUsuarios(
            Model model) {

        model.addAttribute(
                "usuarios",
                usuarioService.listarTodos()
        );

        /*
         * Se envían las sucursales activas para el selector
         * del formulario de alta y edición de usuarios.
         *
         * Si únicamente existe una, el formulario la muestra
         * bloqueada y el servicio la asigna automáticamente.
         */
        model.addAttribute(
                "sucursalesActivas",
                sucursalService.listarActivas()
        );

        model.addAttribute(
                "activePage",
                "usuarios"
        );

        return "admin/usuarios";
    }

    // ============================================================
    // CATEGORÍAS
    // ============================================================

    @GetMapping("/categorias")
    public String listarCategorias(
            Model model) {

        model.addAttribute(
                "categorias",
                categoriaService
                        .listarTodasConProductos()
        );

        model.addAttribute(
                "totalProductos",
                productoService
                        .listarTodos()
                        .size()
        );

        model.addAttribute(
                "productosStockBajo",
                productoService
                        .listarStockBajo()
                        .size()
        );

        model.addAttribute(
                "activePage",
                "categorias"
        );

        return "admin/categorias";
    }

    // ============================================================
    // REPORTES
    // ============================================================

    @GetMapping("/reportes")
    public String reportes(
            Model model) {

        model.addAttribute(
                "fechaInicio",
                LocalDate.now()
                        .minusDays(7)
        );

        model.addAttribute(
                "fechaFin",
                LocalDate.now()
        );

        model.addAttribute(
                "activePage",
                "reportes"
        );

        return "admin/reportes";
    }

    @PostMapping("/reportes")
    public String generarReporte(
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaInicio,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaFin,

            Model model) {

        ReporteVentasRequest request =
                new ReporteVentasRequest();

        request.setFechaInicio(
                fechaInicio
        );

        request.setFechaFin(
                fechaFin
        );

        ReporteVentasResponse reporte =
                reporteService
                        .generarReporteVentas(
                                request
                        );

        model.addAttribute(
                "reporte",
                reporte
        );

        model.addAttribute(
                "fechaInicio",
                fechaInicio
        );

        model.addAttribute(
                "fechaFin",
                fechaFin
        );

        model.addAttribute(
                "activePage",
                "reportes"
        );

        return "admin/reportes";
    }

    // ============================================================
    // CORTE DE CAJA
    // ============================================================

    @GetMapping("/corte")
    public String corteCaja(
            Model model) {

        LocalDate hoy =
                LocalDate.now();

        BigDecimal totalHoy =
                valorSeguro(
                        reporteService
                                .obtenerTotalVentasDelDia(
                                        hoy
                                )
                );

        List<Venta> ventasHoy =
                ventaRepository
                        .findByFechaHoraBetweenOrderByFechaHoraDesc(
                                hoy.atStartOfDay(),
                                hoy.atTime(
                                        LocalTime.MAX
                                )
                        );

        model.addAttribute(
                "fecha",
                hoy
        );

        model.addAttribute(
                "totalVentas",
                totalHoy
        );

        model.addAttribute(
                "numeroVentas",
                ventasHoy.size()
        );

        model.addAttribute(
                "ventas",
                ventasHoy
        );

        model.addAttribute(
                "activePage",
                "corte"
        );

        return "admin/corte";
    }

    // ============================================================
    // MÉTODOS PRIVADOS DEL DASHBOARD
    // ============================================================

    private List<DashboardVentaDiaResponse>
    construirVentasUltimosSieteDias(
            LocalDate hoy) {

        LocalDate fechaInicial =
                hoy.minusDays(6);

        LocalDateTime inicio =
                fechaInicial.atStartOfDay();

        LocalDateTime fin =
                hoy.atTime(
                        LocalTime.MAX
                );

        List<Venta> ventas =
                ventaRepository
                        .findByFechaHoraBetweenOrderByFechaHoraDesc(
                                inicio,
                                fin
                        )
                        .stream()
                        .filter(
                                venta ->
                                        venta.getEstado()
                                                == Venta
                                                .EstadoVenta
                                                .COMPLETADA
                        )
                        .toList();

        Map<LocalDate, List<Venta>>
                ventasPorFecha =
                ventas.stream()
                        .collect(
                                Collectors.groupingBy(
                                        venta ->
                                                venta
                                                        .getFechaHora()
                                                        .toLocalDate()
                                )
                        );

        List<DashboardVentaDiaResponse> resultado =
                new ArrayList<>();

        for (int i = 0; i < 7; i++) {

            LocalDate fecha =
                    fechaInicial.plusDays(i);

            List<Venta> ventasDelDia =
                    ventasPorFecha.getOrDefault(
                            fecha,
                            List.of()
                    );

            BigDecimal totalDelDia =
                    ventasDelDia
                            .stream()
                            .map(Venta::getTotal)
                            .filter(
                                    total ->
                                            total != null
                            )
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            String nombreDia =
                    fecha
                            .getDayOfWeek()
                            .getDisplayName(
                                    TextStyle.SHORT,
                                    new Locale(
                                            "es",
                                            "MX"
                                    )
                            );

            String etiqueta =
                    nombreDia
                            .substring(0, 1)
                            .toUpperCase()
                            + nombreDia.substring(1)
                            + " "
                            + fecha.getDayOfMonth();

            DashboardVentaDiaResponse dato =
                    new DashboardVentaDiaResponse();

            dato.setFecha(
                    fecha
            );

            dato.setEtiqueta(
                    etiqueta
            );

            dato.setTotal(
                    totalDelDia
            );

            dato.setNumeroVentas(
                    ventasDelDia.size()
            );

            dato.setPorcentaje(0);

            resultado.add(
                    dato
            );
        }

        BigDecimal mayorVenta =
                resultado
                        .stream()
                        .map(
                                DashboardVentaDiaResponse
                                        ::getTotal
                        )
                        .max(
                                Comparator.naturalOrder()
                        )
                        .orElse(
                                BigDecimal.ZERO
                        );

        for (DashboardVentaDiaResponse dato
                : resultado) {

            int porcentaje;

            if (mayorVenta.compareTo(
                    BigDecimal.ZERO
            ) == 0) {

                porcentaje = 0;

            } else {

                porcentaje =
                        dato.getTotal()
                                .multiply(
                                        BigDecimal
                                                .valueOf(100)
                                )
                                .divide(
                                        mayorVenta,
                                        0,
                                        RoundingMode.HALF_UP
                                )
                                .intValue();
            }

            dato.setPorcentaje(
                    porcentaje
            );
        }

        return resultado;
    }

    private List<DashboardProductoTopResponse>
    obtenerProductosMasVendidos(
            LocalDate hoy) {

        LocalDateTime inicio =
                hoy.minusDays(29)
                        .atStartOfDay();

        LocalDateTime fin =
                hoy.atTime(
                        LocalTime.MAX
                );

        List<Object[]> resultados =
                detalleVentaRepository
                        .buscarProductosMasVendidos(
                                inicio,
                                fin,
                                PageRequest.of(
                                        0,
                                        5
                                )
                        );

        List<DashboardProductoTopResponse>
                productos =
                new ArrayList<>();

        int posicion = 1;

        for (Object[] fila : resultados) {

            Long productoId =
                    ((Number) fila[0])
                            .longValue();

            String nombre =
                    String.valueOf(
                            fila[1]
                    );

            Long cantidad =
                    ((Number) fila[2])
                            .longValue();

            BigDecimal importe =
                    fila[3] instanceof BigDecimal
                            ? (BigDecimal) fila[3]
                            : new BigDecimal(
                                    fila[3].toString()
                            );

            productos.add(
                    new DashboardProductoTopResponse(
                            productoId,
                            nombre,
                            cantidad,
                            importe,
                            posicion
                    )
            );

            posicion++;
        }

        return productos;
    }

    private BigDecimal valorSeguro(
            BigDecimal valor) {

        return valor != null
                ? valor
                : BigDecimal.ZERO;
    }
}
