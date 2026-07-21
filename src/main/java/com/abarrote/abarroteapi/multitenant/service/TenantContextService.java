package com.abarrote.abarroteapi.multitenant.service;

import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Fuente única y confiable del tenant autenticado.
 *
 * Nunca se debe aceptar tenantId desde formularios, parámetros
 * de URL o cuerpos JSON para operaciones administrativas.
 */
@Service
public class TenantContextService {

    public CommerceUserPrincipal principalActual() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                    instanceof CommerceUserPrincipal principal)) {

            throw new AccessDeniedException(
                    "No existe una sesión autenticada válida."
            );
        }

        if (principal.getTenantId() == null) {
            throw new AccessDeniedException(
                    "El usuario autenticado no tiene tenant."
            );
        }

        return principal;
    }

    public Long tenantIdActual() {
        return principalActual().getTenantId();
    }

    public Long usuarioIdActual() {
        return principalActual().getUsuarioId();
    }

    public boolean esSuperAdmin() {
        return principalActual().tieneRol("SUPER_ADMIN");
    }

    public void validarTenant(Long tenantIdRecurso) {

        Long tenantIdActual =
                tenantIdActual();

        if (tenantIdRecurso == null
                || !tenantIdActual.equals(
                        tenantIdRecurso
                )) {

            throw new AccessDeniedException(
                    "No tienes permiso para acceder "
                            + "a recursos de otro negocio."
            );
        }
    }
}
