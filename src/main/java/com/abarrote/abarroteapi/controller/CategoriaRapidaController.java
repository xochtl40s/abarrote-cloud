package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.entity.Categoria;
import com.abarrote.abarroteapi.repository.CategoriaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaRapidaController {

    private final CategoriaRepository categoriaRepository;

    public CategoriaRapidaController(
            CategoriaRepository categoriaRepository) {

        this.categoriaRepository =
                categoriaRepository;
    }

    @PostMapping("/rapida")
    @Transactional
    public ResponseEntity<Map<String, Object>> crearRapida(
            @RequestBody
            Map<String, String> request) {

        String nombre =
                limpiar(
                        request.get("nombre")
                );

        String descripcion =
                limpiar(
                        request.get("descripcion")
                );

        if (nombre == null
                || nombre.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            respuestaError(
                                    "El nombre de la categoría es obligatorio"
                            )
                    );
        }

        if (nombre.length() > 100) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            respuestaError(
                                    "El nombre no puede exceder 100 caracteres"
                            )
                    );
        }

        if (descripcion != null
                && descripcion.length() > 255) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            respuestaError(
                                    "La descripción no puede exceder 255 caracteres"
                            )
                    );
        }

        Categoria categoriaExistente =
                categoriaRepository
                        .findByNombreIgnoreCase(
                                nombre
                        )
                        .orElse(
                                null
                        );

        if (categoriaExistente != null) {

            Map<String, Object> respuesta =
                    respuestaCategoria(
                            categoriaExistente,
                            false
                    );

            respuesta.put(
                    "message",
                    "La categoría ya existía y fue seleccionada"
            );

            return ResponseEntity.ok(
                    respuesta
            );
        }

        Categoria categoria =
                new Categoria();

        categoria.setNombre(
                normalizarNombre(
                        nombre
                )
        );

        categoria.setDescripcion(
                descripcion == null
                        || descripcion.isBlank()
                        ? "Categoría creada desde el alta de productos"
                        : descripcion
        );

        Categoria guardada =
                categoriaRepository.save(
                        categoria
                );

        Map<String, Object> respuesta =
                respuestaCategoria(
                        guardada,
                        true
                );

        respuesta.put(
                "message",
                "Categoría creada correctamente"
        );

        return ResponseEntity.ok(
                respuesta
        );
    }

    private Map<String, Object> respuestaCategoria(
            Categoria categoria,
            boolean creada) {

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "id",
                categoria.getId()
        );

        data.put(
                "nombre",
                categoria.getNombre()
        );

        data.put(
                "descripcion",
                categoria.getDescripcion()
        );

        Map<String, Object> respuesta =
                new LinkedHashMap<>();

        respuesta.put(
                "success",
                true
        );

        respuesta.put(
                "creada",
                creada
        );

        respuesta.put(
                "data",
                data
        );

        return respuesta;
    }

    private Map<String, Object> respuestaError(
            String mensaje) {

        Map<String, Object> respuesta =
                new LinkedHashMap<>();

        respuesta.put(
                "success",
                false
        );

        respuesta.put(
                "message",
                mensaje
        );

        return respuesta;
    }

    private String limpiar(
            String valor) {

        if (valor == null) {
            return null;
        }

        String limpio =
                valor.trim();

        return limpio.isEmpty()
                ? null
                : limpio;
    }

    private String normalizarNombre(
            String nombre) {

        String limpio =
                nombre.trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );

        if (limpio.isEmpty()) {
            return limpio;
        }

        return limpio.substring(
                        0,
                        1
                )
                .toUpperCase(
                        Locale.forLanguageTag(
                                "es-MX"
                        )
                )
                + limpio.substring(
                        1
                );
    }
}
