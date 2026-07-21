package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.UsuarioRequest;
import com.abarrote.abarroteapi.dto.UsuarioResponse;
import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import com.abarrote.abarroteapi.multitenant.service.TenantContextService;
import com.abarrote.abarroteapi.repository.SucursalRepository;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import com.abarrote.abarroteapi.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UsuarioServiceImpl
        implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private final SucursalRepository sucursalRepository;

    private final TenantRepository tenantRepository;

    private final TenantContextService tenantContextService;

    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            SucursalRepository sucursalRepository,
            TenantRepository tenantRepository,
            TenantContextService tenantContextService,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.sucursalRepository = sucursalRepository;
        this.tenantRepository = tenantRepository;
        this.tenantContextService = tenantContextService;
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

        String rolNormalizado =
                request.getRol()
                        .trim()
                        .toUpperCase();

        validarRolAdministrable(
                rolNormalizado
        );

        usuario.setRol(
                rolNormalizado
        );

        usuario.setActivo(
                request.getActivo() != null
                        ? request.getActivo()
                        : true
        );

        usuario.setSucursal(
                sucursal
        );

        usuario.setTenant(
                obtenerTenantActual()
        );

        validarRolAdministrable(
                usuario.getRol()
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

        String rolNormalizado =
                request.getRol()
                        .trim()
                        .toUpperCase();

        validarRolAdministrable(
                rolNormalizado
        );

        usuario.setRol(
                rolNormalizado
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

        Long tenantId =
                tenantContextService.tenantIdActual();

        return usuarioRepository
                .findByTenantIdOrderByNombreAsc(
                        tenantId
                )
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarActivos() {

        Long tenantId =
                tenantContextService.tenantIdActual();

        return usuarioRepository
                .findByTenantIdAndActivoTrueOrderByNombreAsc(
                        tenantId
                )
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

        @Override
    @Transactional
    public List<Sucursal>
            listarSucursalesActivasDelTenantActual() {

        Long tenantId =
                tenantContextService.tenantIdActual();

        List<Sucursal> sucursales =
                sucursalRepository
                        .findByTenantIdAndActivaTrueOrderByNombreAsc(
                                tenantId
                        );

        if (!sucursales.isEmpty()) {
            return sucursales;
        }

        Tenant tenant =
                tenantRepository
                        .findById(tenantId)
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "El tenant autenticado no existe."
                                )
                        );

        Sucursal matriz =
                sucursalRepository
                        .findByTenantIdAndCodigoIgnoreCase(
                                tenantId,
                                "MAT"
                        )
                        .orElseGet(
                                () -> {

                                    Sucursal nueva =
                                            new Sucursal();

                                    nueva.setTenant(tenant);
                                    nueva.setCodigo("MAT");
                                    nueva.setNombre("Sucursal Matriz");
                                    nueva.setActiva(true);

                                    return sucursalRepository
                                            .saveAndFlush(nueva);
                                }
                        );

        /*
         * Autocorrección del ADMIN autenticado:
         *
         * Si el usuario no tenía sucursal o estaba apuntando
         * a una sucursal heredada de otro tenant, se reasigna
         * a la nueva MAT correcta.
         */
        Usuario usuarioActual =
                usuarioRepository
                        .findByIdAndTenantId(
                                tenantContextService.usuarioIdActual(),
                                tenantId
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "No se encontró el usuario "
                                                + "autenticado en su tenant."
                                )
                        );

        boolean necesitaCorreccion =
                usuarioActual.getSucursal() == null
                || usuarioActual.getSucursal().getTenant() == null
                || !tenantId.equals(
                        usuarioActual
                                .getSucursal()
                                .getTenant()
                                .getId()
                );

        if (necesitaCorreccion) {
            usuarioActual.setSucursal(matriz);
            usuarioRepository.saveAndFlush(usuarioActual);
        }

        return List.of(matriz);
    }

    @Override
    public void eliminar(
            Long id) {

        Usuario usuario =
                obtenerEntityPorId(id);

        if (tenantContextService
                .usuarioIdActual()
                .equals(usuario.getId())) {

            throw new IllegalArgumentException(
                    "No puedes eliminar tu propio usuario."
            );
        }

        if ("SUPER_ADMIN".equalsIgnoreCase(
                usuario.getRol()
        )) {
            throw new IllegalArgumentException(
                    "No está permitido administrar "
                            + "usuarios SUPER_ADMIN "
                            + "desde un negocio."
            );
        }

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
                .findByIdAndTenantId(
                        id,
                        tenantContextService.tenantIdActual()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Usuario no encontrado "
                                        + "en tu negocio"
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
                .findByUsernameIgnoreCaseAndTenantId(
                        username.trim(),
                        tenantContextService.tenantIdActual()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Usuario no encontrado "
                                        + "en tu negocio"
                        )
                );
    }

    private Sucursal resolverSucursal(
            Long sucursalIdSolicitada) {

        Long tenantId =
                tenantContextService.tenantIdActual();

        List<Sucursal> sucursalesActivas =
                sucursalRepository
                        .findByTenantIdAndActivaTrueOrderByNombreAsc(
                                tenantId
                        );

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
                        .filter(
                                candidata ->
                                        candidata.getTenant() != null
                                        && tenantId.equals(
                                            candidata
                                                .getTenant()
                                                .getId()
                                        )
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "La sucursal seleccionada "
                                                + "no pertenece a tu negocio"
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

    private Tenant obtenerTenantActual() {

        Long tenantId =
                tenantContextService.tenantIdActual();

        return tenantRepository
                .findById(tenantId)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "El tenant autenticado no existe."
                        )
                );
    }

    private void validarRolAdministrable(
            String rol) {

        if (rol == null || rol.isBlank()) {
            throw new IllegalArgumentException(
                    "El rol es obligatorio."
            );
        }

        String rolNormalizado =
                rol.trim().toUpperCase();

        if ("SUPER_ADMIN".equals(rolNormalizado)) {
            throw new IllegalArgumentException(
                    "Un administrador de negocio no puede "
                            + "crear ni modificar SUPER_ADMIN."
            );
        }

        if (!List.of(
                "ADMIN",
                "CAJERO",
                "MESERO"
        ).contains(rolNormalizado)) {

            throw new IllegalArgumentException(
                    "Rol no permitido para este negocio: "
                            + rolNormalizado
            );
        }
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
