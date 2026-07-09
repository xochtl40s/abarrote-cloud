package com.abarrote.abarroteapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
public class VentaRestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarProductos(@RequestParam String q) {
        try {
            // Búsqueda exacta insensible a minúsculas/mayúsculas
            String sql = "SELECT * FROM productos WHERE LOWER(nombre) LIKE LOWER(?)";
            List<Map<String, Object>> filas = jdbcTemplate.queryForList(sql, "%" + q + "%");
            
            List<Map<String, Object>> resultados = new ArrayList<>();
            for (Map<String, Object> fila : filas) {
                Map<String, Object> item = new HashMap<>();
                
                // Mapear ID de forma segura
                Object idObj = fila.getOrDefault("id", fila.get("ID"));
                item.put("id", idObj != null ? idObj.toString() : "0");
                
                // Mapear Nombre
                item.put("nombre", fila.getOrDefault("nombre", "Producto sin nombre").toString());
                
                // Mapear Precio manejando BigDecimals o Doubles de la BD
                Object precioObj = fila.getOrDefault("precio", 0.0);
                double precio = (precioObj instanceof Number) ? ((Number) precioObj).doubleValue() : 0.0;
                item.put("precio", precio);
                
                // TOLERANCIA DE COLUMNAS: Detecta "existencia", "existencias" o "stock" automáticamente
                int stock = 0;
                if (fila.containsKey("existencia") && fila.get("existencia") != null) {
                    stock = ((Number) fila.get("existencia")).intValue();
                } else if (fila.containsKey("existencias") && fila.get("existencias") != null) {
                    stock = ((Number) fila.get("existencias")).intValue();
                } else if (fila.containsKey("stock") && fila.get("stock") != null) {
                    stock = ((Number) fila.get("stock")).intValue();
                }
                item.put("existencia", stock);
                
                resultados.add(item);
            }
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/procesar")
    public ResponseEntity<?> procesarVenta(@RequestBody List<Map<String, Object>> items, Authentication authentication) {
        String cajeroActual = (authentication != null) ? authentication.getName() : "Cajero General";
        try {
            // 1. Validar Stock global antes de alterar nada
            for (Map<String, Object> item : items) {
                String id = item.get("id").toString();
                int cantidad = Integer.parseInt(item.get("cantidad").toString());

                Map<String, Object> prod = jdbcTemplate.queryForMap("SELECT * FROM productos WHERE id = ?", Long.parseLong(id));
                int stockActual = 0;
                if (prod.containsKey("existencia")) stockActual = ((Number) prod.get("existencia")).intValue();
                else if (prod.containsKey("existencias")) stockActual = ((Number) prod.get("existencias")).intValue();
                else if (prod.containsKey("stock")) stockActual = ((Number) prod.get("stock")).intValue();

                if (stockActual < cantidad) {
                    return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", "Stock insuficiente para: " + prod.get("nombre")));
                }
            }

            // 2. Aplicar descuento de inventario real en la Caja
            for (Map<String, Object> item : items) {
                long id = Long.parseLong(item.get("id").toString());
                int cantidad = Integer.parseInt(item.get("cantidad").toString());
                
                try {
                    jdbcTemplate.update("UPDATE productos SET existencia = existencia - ? WHERE id = ?", cantidad, id);
                } catch (Exception e) {
                    try {
                        jdbcTemplate.update("UPDATE productos SET existencias = existencias - ? WHERE id = ?", cantidad, id);
                    } catch (Exception ex) {
                        jdbcTemplate.update("UPDATE productos SET stock = stock - ? WHERE id = ?", cantidad, id);
                    }
                }
            }
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "cajero", cajeroActual));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }
@Autowired
private com.abarrote.abarroteapi.service.VentaService ventaService;


@PostMapping("/registrar")
public ResponseEntity<?> registrarVenta(
        @RequestBody com.abarrote.abarroteapi.dto.VentaRequest request) {

    try {

        var venta = ventaService.registrarVenta(request);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Venta registrada correctamente",
                "idVenta", venta.getId(),
                "total", venta.getTotal()
        ));

    } catch(Exception e) {

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", e.getMessage()
                ));
    }
}
}
