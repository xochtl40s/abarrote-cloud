package com.abarrote.abarroteapi.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    public UsuarioRestController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> crear(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse creado = usuarioService.crear(request);
        return ResponseEntity.ok(ApiResponse.ok("Usuario creado exitosamente", creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizar(@PathVariable Long id,
                                                                  @Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse actualizado = usuarioService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado exitosamente", actualizado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.obtenerPorId(id)));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerPorUsername(@PathVariable String username) {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.obtenerPorUsername(username)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.listarTodos()));
    }

    @GetMapping("/activos")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listarActivos() {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.listarActivos()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario eliminado exitosamente", null));
    }

    // ===== CAMBIAR CONTRASEÑA =====
    @PostMapping("/cambiar-password")
    public ResponseEntity<ApiResponse<Void>> cambiarPassword(
            @Valid @RequestBody CambioPasswordRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        usuarioService.cambiarPassword(username, request.getPasswordActual(), request.getPasswordNueva());
        return ResponseEntity.ok(ApiResponse.ok("Contraseña cambiada exitosamente", null));
    }
}
