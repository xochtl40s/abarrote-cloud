package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.dto.UsuarioRequest;
import com.abarrote.abarroteapi.dto.UsuarioResponse;
import com.abarrote.abarroteapi.entity.Usuario;

import java.util.List;

public interface UsuarioService {

    UsuarioResponse crear(UsuarioRequest request);

    UsuarioResponse actualizar(Long id, UsuarioRequest request);

    UsuarioResponse obtenerPorId(Long id);

    UsuarioResponse obtenerPorUsername(String username);

    List<UsuarioResponse> listarTodos();

    List<UsuarioResponse> listarActivos();

    void eliminar(Long id);

    void cambiarPassword(
            String username,
            String passwordActual,
            String passwordNueva
    );

    void cambiarPasswordPorAdministrador(
            Long usuarioId,
            String passwordNueva
    );

    Usuario obtenerEntityPorId(Long id);

    Usuario obtenerEntityPorUsername(String username);
}
