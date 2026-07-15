package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.ApiResponse;
import com.abarrote.abarroteapi.dto.ProductoResponse;
import com.abarrote.abarroteapi.dto.VentaRequest;
import com.abarrote.abarroteapi.dto.VentaResponse;
import com.abarrote.abarroteapi.entity.Venta;
import com.abarrote.abarroteapi.service.ProductoPosService;
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

    private final ProductoPosService
            productoPosService;

    public VentaRestController(
            VentaService ventaService,
            ProductoPosService productoPosService) {

        this.ventaService = ventaService;

        this.productoPosService =
                productoPosService;
    }

    /*
     * Catálogo completo para el POS.
     * Regresa existencias únicamente de la sucursal
     * asignada al usuario autenticado.
     */
    @GetMapping("/productos")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>>
    listarProductosPos(
            Authentication authentication) {

        String username =
                obtenerUsername(authentication);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        productoPosService
                                .listarProductosDelUsuario(
                                        username
                                )
                )
        );
    }

    /*
     * Búsqueda de productos dentro del inventario
     * de la sucursal del cajero.
     */
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>>
    buscarProductos(
            @RequestParam(defaultValue = "")
            String q,

            Authentication authentication) {

        String username =
                obtenerUsername(authentication);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        productoPosService
                                .buscarProductosDelUsuario(
                                        username,
                                        q
                                )
                )
        );
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<VentaResponse>>
    registrarVenta(
            @RequestBody
            VentaRequest request,

            Authentication authentication) {

        obtenerUsername(authentication);

        Venta venta =
                ventaService.registrarVenta(
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Venta registrada correctamente",
                        mapearAResponse(venta)
                )
        );
    }

    /*
     * Sincronización por lote.
     *
     * Una venta rechazada por reglas de negocio no se
     * registra y su error queda informado.
     */
    @PostMapping("/sincronizar")
    public ResponseEntity<ApiResponse<SincronizacionResponse>>
    sincronizarVentas(
            @RequestBody
            List<VentaRequest> ventasPendientes,

            Authentication authentication) {

        obtenerUsername(authentication);

        List<Long> ventasRegistradas =
                new ArrayList<>();

        List<String> errores =
                new ArrayList<>();

        for (int indice = 0;
             indice < ventasPendientes.size();
             indice++) {

            try {

                Venta venta =
                        ventaService.registrarVenta(
                                ventasPendientes.get(indice)
                        );

                ventasRegistradas.add(
                        venta.getId()
                );

            } catch (RuntimeException exception) {

                errores.add(
                        "Venta "
                                + (indice + 1)
                                + ": "
                                + exception.getMessage()
                );
            }
        }

        SincronizacionResponse response =
                new SincronizacionResponse();

        response.setTotalRecibidas(
                ventasPendientes.size()
        );

        response.setTotalRegistradas(
                ventasRegistradas.size()
        );

        response.setIdsRegistradas(
                ventasRegistradas
        );

        response.setErrores(
                errores
        );

        String mensaje =
                ventasRegistradas.size()
                        + " de "
                        + ventasPendientes.size()
                        + " ventas sincronizadas";

        return ResponseEntity.ok(
                ApiResponse.ok(
                        mensaje,
                        response
                )
        );
    }

    private String obtenerUsername(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equalsIgnoreCase(
                        authentication.getName()
                )) {

            throw new IllegalStateException(
                    "No existe una sesión autenticada"
            );
        }

        return authentication.getName();
    }

    private VentaResponse mapearAResponse(
            Venta venta) {

        VentaResponse response =
                new VentaResponse();

        response.setId(
                venta.getId()
        );

        response.setFecha(
                venta.getFecha()
        );

        response.setTotal(
                venta.getTotal()
        );

        response.setCajero(
                venta.getUsuario() != null
                        ? venta.getUsuario().getNombre()
                        : "N/A"
        );

        response.setEstado(
                venta.getEstado().name()
        );

        List<VentaResponse.DetalleVentaResponse> detalles =
                venta.getDetalles()
                        .stream()
                        .map(
                                detalle -> {

                                    VentaResponse.DetalleVentaResponse item =
                                            new VentaResponse
                                                    .DetalleVentaResponse();

                                    item.setProductoNombre(
                                            detalle
                                                    .getProducto()
                                                    .getNombre()
                                    );

                                    item.setCantidad(
                                            detalle.getCantidad()
                                    );

                                    item.setPrecioUnitario(
                                            detalle.getPrecioUnitario()
                                    );

                                    item.setSubtotal(
                                            detalle.getSubtotal()
                                    );

                                    return item;
                                }
                        )
                        .collect(
                                Collectors.toList()
                        );

        response.setDetalles(
                detalles
        );

        return response;
    }

    public static class SincronizacionResponse {

        private int totalRecibidas;
        private int totalRegistradas;

        private List<Long> idsRegistradas;

        private List<String> errores;

        public int getTotalRecibidas() {
            return totalRecibidas;
        }

        public void setTotalRecibidas(
                int totalRecibidas) {

            this.totalRecibidas = totalRecibidas;
        }

        public int getTotalRegistradas() {
            return totalRegistradas;
        }

        public void setTotalRegistradas(
                int totalRegistradas) {

            this.totalRegistradas = totalRegistradas;
        }

        public List<Long> getIdsRegistradas() {
            return idsRegistradas;
        }

        public void setIdsRegistradas(
                List<Long> idsRegistradas) {

            this.idsRegistradas = idsRegistradas;
        }

        public List<String> getErrores() {
            return errores;
        }

        public void setErrores(
                List<String> errores) {

            this.errores = errores;
        }
    }
}
