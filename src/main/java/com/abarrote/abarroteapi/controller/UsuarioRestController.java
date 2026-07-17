package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.AdminCambioPasswordRequest;
import com.abarrote.abarroteapi.dto.ApiResponse;
import com.abarrote.abarroteapi.dto.CambioPasswordRequest;
import com.abarrote.abarroteapi.dto.UsuarioRequest;
import com.abarrote.abarroteapi.dto.UsuarioResponse;
import com.abarrote.abarroteapi.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    private final UsuarioService usuarioService;

    public UsuarioRestController(
            UsuarioService usuarioService) {

        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> crear(
            @Valid @RequestBody UsuarioRequest request) {

        UsuarioResponse creado =
                usuarioService.crear(request);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Usuario creado exitosamente",
                        creado
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request) {

        UsuarioResponse actualizado =
                usuarioService.actualizar(id, request);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Usuario actualizado exitosamente",
                        actualizado
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.ok(
                        usuarioService.obtenerPorId(id)
                )
        );
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerPorUsername(
            @PathVariable String username) {

        return ResponseEntity.ok(
                ApiResponse.ok(
                        usuarioService.obtenerPorUsername(username)
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listarTodos() {

        return ResponseEntity.ok(
                ApiResponse.ok(
                        usuarioService.listarTodos()
                )
        );
    }

    @GetMapping("/activos")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listarActivos() {

        return ResponseEntity.ok(
                ApiResponse.ok(
                        usuarioService.listarActivos()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id) {

        usuarioService.eliminar(id);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Usuario eliminado exitosamente",
                        null
                )
        );
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<ApiResponse<Void>> cambiarPassword(
            @Valid @RequestBody CambioPasswordRequest request,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "No existe una sesión autenticada"
            );
        }

        if (!request.getPasswordNueva()
                .equals(request.getConfirmarPassword())) {

            throw new RuntimeException(
                    "La nueva contraseña y su confirmación no coinciden"
            );
        }

        usuarioService.cambiarPassword(
                authentication.getName(),
                request.getPasswordActual(),
                request.getPasswordNueva()
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Contraseña cambiada exitosamente",
                        null
                )
        );
    }

    @PostMapping("/{id}/cambiar-password")
    public ResponseEntity<ApiResponse<Void>>
    cambiarPasswordPorAdministrador(
            @PathVariable Long id,
            @Valid @RequestBody AdminCambioPasswordRequest request,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "No existe una sesión autenticada"
            );
        }

        boolean esAdministrador =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority
                                        .getAuthority()
                                        .equals("ROLE_ADMIN")
                        );

        if (!esAdministrador) {

            throw new RuntimeException(
                    "Solamente un administrador puede cambiar la contraseña de otro usuario"
            );
        }

        if (!request.getPasswordNueva()
                .equals(request.getConfirmarPassword())) {

            throw new RuntimeException(
                    "La nueva contraseña y su confirmación no coinciden"
            );
        }

        usuarioService.cambiarPasswordPorAdministrador(
                id,
                request.getPasswordNueva()
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Contraseña del usuario cambiada exitosamente",
                        null
                )
        );
    }
}
