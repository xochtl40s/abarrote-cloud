package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.ReporteVentasRequest;
import com.abarrote.abarroteapi.dto.ReporteVentasResponse;
import com.abarrote.abarroteapi.entity.Venta;
import com.abarrote.abarroteapi.repository.VentaRepository;
import com.abarrote.abarroteapi.service.CategoriaService;
import com.abarrote.abarroteapi.service.ProductoService;
import com.abarrote.abarroteapi.service.ReporteService;
import com.abarrote.abarroteapi.service.UsuarioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductoService productoService;
    private final UsuarioService usuarioService;
    private final CategoriaService categoriaService;
    private final ReporteService reporteService;
    private final VentaRepository ventaRepository;

    public AdminController(ProductoService productoService,
                           UsuarioService usuarioService,
                           CategoriaService categoriaService,
                           ReporteService reporteService,
                           VentaRepository ventaRepository) {
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.categoriaService = categoriaService;
        this.reporteService = reporteService;
        this.ventaRepository = ventaRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalVentasHoy", reporteService.obtenerTotalVentasDelDia(LocalDate.now()));
        model.addAttribute("totalProductos", productoService.listarTodos().size());
        model.addAttribute("productosStockBajo", productoService.listarStockBajo().size());
        model.addAttribute("totalUsuarios", usuarioService.listarActivos().size());
        return "admin/dashboard";
            }

    @GetMapping("/productos")
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "admin/productos";
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "admin/usuarios";
    }

    @GetMapping("/categorias")
    public String listarCategorias(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodasConProductos());
        model.addAttribute("totalProductos", productoService.listarTodos().size());
        model.addAttribute("productosStockBajo", productoService.listarStockBajo().size());
        return "admin/categorias";
    }

    @GetMapping("/reportes")
    public String reportes(Model model) {
        model.addAttribute("fechaInicio", LocalDate.now().minusDays(7));
        model.addAttribute("fechaFin", LocalDate.now());
        return "admin/reportes";
    }

    @PostMapping("/reportes")
    public String generarReporte(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {

        ReporteVentasRequest request = new ReporteVentasRequest();
        request.setFechaInicio(fechaInicio);
        request.setFechaFin(fechaFin);

        ReporteVentasResponse reporte = reporteService.generarReporteVentas(request);

        model.addAttribute("reporte", reporte);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        return "admin/reportes";
    }

    @GetMapping("/corte")
    public String corteCaja(Model model) {
        LocalDate hoy = LocalDate.now();
        BigDecimal totalHoy = reporteService.obtenerTotalVentasDelDia(hoy);

        List<Venta> ventasHoy = ventaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(
                hoy.atStartOfDay(), hoy.atTime(java.time.LocalTime.MAX));

        model.addAttribute("fecha", hoy);
        model.addAttribute("totalVentas", totalHoy);
        model.addAttribute("numeroVentas", ventasHoy.size());
        model.addAttribute("ventas", ventasHoy);
        return "admin/corte";
    }
}
