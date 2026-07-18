package com.abarrote.abarroteapi.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CommerceUserPrincipal extends User {

    private final Long usuarioId;

    private final String nombre;

    private final Long tenantId;

    private final String tenantSlug;

    private final String tenantNombre;

    private final String tipoNegocio;

    private final Long sucursalId;

    public CommerceUserPrincipal(
        Long usuarioId,
        String nombre,
        String username,
        String password,
        Collection<? extends GrantedAuthority> authorities,
        Long tenantId,
        String tenantSlug,
        String tenantNombre,
        String tipoNegocio,
        Long sucursalId
    ) {
        super(
            username,
            password,
            authorities
        );

        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.tenantId = tenantId;
        this.tenantSlug = tenantSlug;
        this.tenantNombre = tenantNombre;
        this.tipoNegocio = tipoNegocio;
        this.sucursalId = sucursalId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getTenantSlug() {
        return tenantSlug;
    }

    public String getTenantNombre() {
        return tenantNombre;
    }

    public String getTipoNegocio() {
        return tipoNegocio;
    }

    public Long getSucursalId() {
        return sucursalId;
    }

    public boolean esGym() {
        return "GYM".equalsIgnoreCase(tipoNegocio);
    }

    public boolean esAbarrotes() {
        return !esGym();
    }

    public boolean tieneRol(String rol) {
        String autoridadEsperada =
            rol.startsWith("ROLE_")
                ? rol
                : "ROLE_" + rol;

        return getAuthorities()
            .stream()
            .anyMatch(
                authority ->
                    autoridadEsperada.equals(
                        authority.getAuthority()
                    )
            );
    }
}
