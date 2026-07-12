package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.ApiResponse;
import com.abarrote.abarroteapi.dto.VentaRequest;
import com.abarrote.abarroteapi.dto.VentaResponse;
import com.abarrote.abarroteapi.entity.Venta;
import com.abarrote.abarroteapi.service.ProductoService;
import com.abarrote.abarroteapi.service.VentaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ventas")
public class VentaRestController {

    private final VentaService ventaService;
    private final ProductoService productoService;

    public VentaRestController(VentaService ventaService, ProductoService productoService) {
        this.ventaService = ventaService;
        this.productoService = productoService;
    }

    /**
     * Buscar productos por nombre para agregar a la venta (POS)
     */
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<com.abarrote.abarroteapi.dto.ProductoResponse>>> buscarProductos(
            @RequestParam String q) {
        List<com.abarrote.abarroteapi.dto.ProductoResponse> productos = productoService.buscarPorNombre(q);
        return ResponseEntity.ok(ApiResponse.ok(productos));
    }

    /**
     * Registrar una nueva venta (proceso completo del POS)
     */
    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<VentaResponse>> registrarVenta(
            @RequestBody VentaRequest request,
            Authentication authentication) {

        Venta venta = ventaService.registrarVenta(request);
        return ResponseEntity.ok(ApiResponse.ok("Venta registrada correctamente", mapearAResponse(venta)));
    }

    /**
     * Sincronizar ventas pendientes desde modo offline (batch)
     */
    @PostMapping("/sincronizar")
    public ResponseEntity<ApiResponse<SincronizacionResponse>> sincronizarVentas(
            @RequestBody List<VentaRequest> ventasPendientes,
            Authentication authentication) {

        List<Long> ventasRegistradas = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        for (int i = 0; i < ventasPendientes.size(); i++) {
            try {
                Venta venta = ventaService.registrarVenta(ventasPendientes.get(i));
                ventasRegistradas.add(venta.getId());
            } catch (Exception e) {
                errores.add("Venta " + (i + 1) + ": " + e.getMessage());
            }
        }

        SincronizacionResponse response = new SincronizacionResponse();
        response.setTotalRecibidas(ventasPendientes.size());
        response.setTotalRegistradas(ventasRegistradas.size());
        response.setIdsRegistradas(ventasRegistradas);
        response.setErrores(errores);

        String mensaje = ventasRegistradas.size() + " de " + ventasPendientes.size() + " ventas sincronizadas";
        return ResponseEntity.ok(ApiResponse.ok(mensaje, response));
    }

    // Clase interna para respuesta de sincronización
    public static class SincronizacionResponse {
        private int totalRecibidas;
        private int totalRegistradas;
        private List<Long> idsRegistradas;
        private List<String> errores;

        public int getTotalRecibidas() { return totalRecibidas; }
        public void setTotalRecibidas(int totalRecibidas) { this.totalRecibidas = totalRecibidas; }
        public int getTotalRegistradas() { return totalRegistradas; }
        public void setTotalRegistradas(int totalRegistradas) { this.totalRegistradas = totalRegistradas; }
        public List<Long> getIdsRegistradas() { return idsRegistradas; }
        public void setIdsRegistradas(List<Long> idsRegistradas) { this.idsRegistradas = idsRegistradas; }
        public List<String> getErrores() { return errores; }
        public void setErrores(List<String> errores) { this.errores = errores; }
    }

    private VentaResponse mapearAResponse(Venta venta) {
        VentaResponse response = new VentaResponse();
        response.setId(venta.getId());
        response.setFecha(venta.getFecha());
        response.setTotal(venta.getTotal());
        response.setCajero(venta.getUsuario() != null ? venta.getUsuario().getNombre() : "N/A");
        response.setEstado(venta.getEstado().name());

        List<VentaResponse.DetalleVentaResponse> detalles = venta.getDetalles().stream()
                .map(d -> {
                    VentaResponse.DetalleVentaResponse dr = new VentaResponse.DetalleVentaResponse();
                    dr.setProductoNombre(d.getProducto().getNombre());
                    dr.setCantidad(d.getCantidad());
                    dr.setPrecioUnitario(d.getPrecioUnitario());
                    dr.setSubtotal(d.getSubtotal());
                    return dr;
                })
                .collect(Collectors.toList());

        response.setDetalles(detalles);
        return response;
    }
}
