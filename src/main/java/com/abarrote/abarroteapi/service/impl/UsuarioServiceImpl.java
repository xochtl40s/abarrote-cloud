package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.UsuarioRequest;
import com.abarrote.abarroteapi.dto.UsuarioResponse;
import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import com.abarrote.abarroteapi.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioResponse crear(UsuarioRequest request) {

        if (usuarioRepository
                .findByUsernameIgnoreCase(request.getUsername())
                .isPresent()) {

            throw new RuntimeException(
                    "Ya existe un usuario con ese username"
            );
        }

        if (request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new RuntimeException(
                    "La contraseña es obligatoria"
            );
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(request.getNombre().trim());
        usuario.setUsername(request.getUsername().trim());
        usuario.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        usuario.setRol(request.getRol().toUpperCase());
        usuario.setActivo(
                request.getActivo() != null
                        ? request.getActivo()
                        : true
        );

        return mapearAResponse(
                usuarioRepository.save(usuario)
        );
    }

    @Override
    public UsuarioResponse actualizar(
            Long id,
            UsuarioRequest request) {

        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        )
                );

        if (!request.getUsername()
                .equalsIgnoreCase(usuario.getUsername())
                && usuarioRepository
                .findByUsernameIgnoreCase(request.getUsername())
                .isPresent()) {

            throw new RuntimeException(
                    "Ya existe un usuario con ese username"
            );
        }

        usuario.setNombre(request.getNombre().trim());
        usuario.setUsername(request.getUsername().trim());

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {

            usuario.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        usuario.setRol(request.getRol().toUpperCase());
        usuario.setActivo(request.getActivo());

        return mapearAResponse(
                usuarioRepository.save(usuario)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {

        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        )
                );

        return mapearAResponse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorUsername(
            String username) {

        Usuario usuario = usuarioRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        )
                );

        return mapearAResponse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {

        return usuarioRepository
                .findAll()
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarActivos() {

        return usuarioRepository
                .findAll()
                .stream()
                .filter(Usuario::getActivo)
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {

        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        )
                );

        usuarioRepository.delete(usuario);
    }

    @Override
    public void cambiarPassword(
            String username,
            String passwordActual,
            String passwordNueva) {

        Usuario usuario = usuarioRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        )
                );

        if (!passwordEncoder.matches(
                passwordActual,
                usuario.getPassword())) {

            throw new RuntimeException(
                    "La contraseña actual es incorrecta"
            );
        }

        validarPasswordNueva(passwordNueva);

        if (passwordEncoder.matches(
                passwordNueva,
                usuario.getPassword())) {

            throw new RuntimeException(
                    "La nueva contraseña debe ser diferente de la actual"
            );
        }

        usuario.setPassword(
                passwordEncoder.encode(passwordNueva)
        );

        usuarioRepository.save(usuario);
    }

    @Override
    public void cambiarPasswordPorAdministrador(
            Long usuarioId,
            String passwordNueva) {

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        )
                );

        validarPasswordNueva(passwordNueva);

        usuario.setPassword(
                passwordEncoder.encode(passwordNueva)
        );

        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerEntityPorId(Long id) {

        return usuarioRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerEntityPorUsername(
            String username) {

        return usuarioRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        )
                );
    }

    private void validarPasswordNueva(
            String passwordNueva) {

        if (passwordNueva == null
                || passwordNueva.isBlank()) {

            throw new RuntimeException(
                    "La nueva contraseña es obligatoria"
            );
        }

        if (passwordNueva.length() < 4) {

            throw new RuntimeException(
                    "La nueva contraseña debe tener al menos 4 caracteres"
            );
        }
    }

    private UsuarioResponse mapearAResponse(
            Usuario usuario) {

        UsuarioResponse response =
                new UsuarioResponse();

        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setUsername(usuario.getUsername());
        response.setRol(usuario.getRol());
        response.setActivo(usuario.getActivo());

        return response;
    }
}
