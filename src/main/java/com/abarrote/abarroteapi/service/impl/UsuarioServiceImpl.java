package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.UsuarioRequest;
import com.abarrote.abarroteapi.dto.UsuarioResponse;
import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.repository.SucursalRepository;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import com.abarrote.abarroteapi.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class UsuarioServiceImpl
        implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private final SucursalRepository sucursalRepository;

    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            SucursalRepository sucursalRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.sucursalRepository = sucursalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioResponse crear(
            UsuarioRequest request) {

        validarRequest(request);

        validarUsernameDisponible(
                request.getUsername(),
                null
        );

        if (request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "La contraseña es obligatoria"
            );
        }

        Sucursal sucursal =
                resolverSucursal(
                        request.getSucursalId()
                );

        Usuario usuario =
                new Usuario();

        usuario.setNombre(
                request.getNombre().trim()
        );

        usuario.setUsername(
                request.getUsername().trim()
        );

        usuario.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        usuario.setRol(
                request.getRol()
                        .trim()
                        .toUpperCase()
        );

        usuario.setActivo(
                request.getActivo() != null
                        ? request.getActivo()
                        : true
        );

        usuario.setSucursal(
                sucursal
        );

        return mapearAResponse(
                usuarioRepository.save(usuario)
        );
    }

    @Override
    public UsuarioResponse actualizar(
            Long id,
            UsuarioRequest request) {

        validarRequest(request);

        Usuario usuario =
                obtenerEntityPorId(id);

        validarUsernameDisponible(
                request.getUsername(),
                id
        );

        Sucursal sucursal =
                resolverSucursal(
                        request.getSucursalId()
                );

        usuario.setNombre(
                request.getNombre().trim()
        );

        usuario.setUsername(
                request.getUsername().trim()
        );

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {

            usuario.setPassword(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );
        }

        usuario.setRol(
                request.getRol()
                        .trim()
                        .toUpperCase()
        );

        usuario.setActivo(
                request.getActivo() != null
                        ? request.getActivo()
                        : true
        );

        usuario.setSucursal(
                sucursal
        );

        return mapearAResponse(
                usuarioRepository.save(usuario)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(
            Long id) {

        return mapearAResponse(
                obtenerEntityPorId(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorUsername(
            String username) {

        return mapearAResponse(
                obtenerEntityPorUsername(
                        username
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {

        return usuarioRepository
                .findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                Usuario::getNombre,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .map(this::mapearAResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarActivos() {

        return usuarioRepository
                .findAll()
                .stream()
                .filter(
                        usuario ->
                                Boolean.TRUE.equals(
                                        usuario.getActivo()
                                )
                )
                .sorted(
                        Comparator.comparing(
                                Usuario::getNombre,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .map(this::mapearAResponse)
                .toList();
    }

    @Override
    public void eliminar(
            Long id) {

        Usuario usuario =
                obtenerEntityPorId(id);

        usuarioRepository.delete(
                usuario
        );
    }

    @Override
    public void cambiarPassword(
            String username,
            String passwordActual,
            String passwordNueva) {

        Usuario usuario =
                obtenerEntityPorUsername(
                        username
                );

        if (!passwordEncoder.matches(
                passwordActual,
                usuario.getPassword())) {

            throw new IllegalArgumentException(
                    "La contraseña actual es incorrecta"
            );
        }

        validarPasswordNueva(
                passwordNueva
        );

        if (passwordEncoder.matches(
                passwordNueva,
                usuario.getPassword())) {

            throw new IllegalArgumentException(
                    "La nueva contraseña debe ser diferente de la actual"
            );
        }

        usuario.setPassword(
                passwordEncoder.encode(
                        passwordNueva
                )
        );

        usuarioRepository.save(
                usuario
        );
    }

    @Override
    public void cambiarPasswordPorAdministrador(
            Long usuarioId,
            String passwordNueva) {

        Usuario usuario =
                obtenerEntityPorId(
                        usuarioId
                );

        validarPasswordNueva(
                passwordNueva
        );

        usuario.setPassword(
                passwordEncoder.encode(
                        passwordNueva
                )
        );

        usuarioRepository.save(
                usuario
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerEntityPorId(
            Long id) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "El identificador del usuario es obligatorio"
            );
        }

        return usuarioRepository
                .findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Usuario no encontrado"
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerEntityPorUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            throw new IllegalArgumentException(
                    "El username es obligatorio"
            );
        }

        return usuarioRepository
                .findByUsernameIgnoreCase(
                        username.trim()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Usuario no encontrado"
                        )
                );
    }

    private Sucursal resolverSucursal(
            Long sucursalIdSolicitada) {

        List<Sucursal> sucursalesActivas =
                sucursalRepository
                        .findByActivaTrueOrderByNombreAsc();

        if (sucursalesActivas.isEmpty()) {

            throw new IllegalStateException(
                    "No existe ninguna sucursal activa"
            );
        }

        /*
         * Regla de negocio:
         *
         * Cuando solamente existe una sucursal activa,
         * se asigna automáticamente.
         */
        if (sucursalesActivas.size() == 1) {

            return sucursalesActivas.get(0);
        }

        /*
         * Cuando existen varias sucursales,
         * la selección es obligatoria.
         */
        if (sucursalIdSolicitada == null) {

            throw new IllegalArgumentException(
                    "Selecciona la sucursal del usuario"
            );
        }

        Sucursal sucursal =
                sucursalRepository
                        .findById(
                                sucursalIdSolicitada
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "La sucursal seleccionada no existe"
                                )
                        );

        if (!Boolean.TRUE.equals(
                sucursal.getActiva())) {

            throw new IllegalArgumentException(
                    "La sucursal seleccionada está inactiva"
            );
        }

        return sucursal;
    }

    private void validarUsernameDisponible(
            String username,
            Long usuarioIdActual) {

        String usernameNormalizado =
                username.trim();

        usuarioRepository
                .findByUsernameIgnoreCase(
                        usernameNormalizado
                )
                .ifPresent(
                        usuarioExistente -> {

                            boolean perteneceAOtroUsuario =
                                    usuarioIdActual == null
                                            || !usuarioExistente
                                            .getId()
                                            .equals(
                                                    usuarioIdActual
                                            );

                            if (perteneceAOtroUsuario) {

                                throw new IllegalArgumentException(
                                        "Ya existe un usuario con ese username"
                                );
                            }
                        }
                );
    }

    private void validarRequest(
            UsuarioRequest request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "La información del usuario es obligatoria"
            );
        }

        if (request.getNombre() == null
                || request.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio"
            );
        }

        if (request.getUsername() == null
                || request.getUsername().isBlank()) {

            throw new IllegalArgumentException(
                    "El username es obligatorio"
            );
        }

        if (request.getRol() == null
                || request.getRol().isBlank()) {

            throw new IllegalArgumentException(
                    "El rol es obligatorio"
            );
        }

        String rol =
                request.getRol()
                        .trim()
                        .toUpperCase();

        if (!rol.equals("ADMIN")
                && !rol.equals("CAJERO")) {

            throw new IllegalArgumentException(
                    "El rol debe ser ADMIN o CAJERO"
            );
        }
    }

    private void validarPasswordNueva(
            String passwordNueva) {

        if (passwordNueva == null
                || passwordNueva.isBlank()) {

            throw new IllegalArgumentException(
                    "La nueva contraseña es obligatoria"
            );
        }

        if (passwordNueva.length() < 4) {

            throw new IllegalArgumentException(
                    "La nueva contraseña debe tener al menos 4 caracteres"
            );
        }
    }

    private UsuarioResponse mapearAResponse(
            Usuario usuario) {

        UsuarioResponse response =
                new UsuarioResponse();

        response.setId(
                usuario.getId()
        );

        response.setNombre(
                usuario.getNombre()
        );

        response.setUsername(
                usuario.getUsername()
        );

        response.setRol(
                usuario.getRol()
        );

        response.setActivo(
                usuario.getActivo()
        );

        if (usuario.getSucursal() != null) {

            response.setSucursalId(
                    usuario
                            .getSucursal()
                            .getId()
            );

            response.setSucursalCodigo(
                    usuario
                            .getSucursal()
                            .getCodigo()
            );

            response.setSucursalNombre(
                    usuario
                            .getSucursal()
                            .getNombre()
            );
        }

        return response;
    }
}
