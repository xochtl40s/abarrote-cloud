package com.abarrote.abarroteapi.config;

import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomSuccessHandler
    implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {

        HttpSession session = request.getSession(true);

        if (
            authentication.getPrincipal()
                instanceof CommerceUserPrincipal principal
        ) {
            guardarContextoEnSesion(
                session,
                principal
            );

            if (principal.esGym()) {
                response.sendRedirect(
                    request.getContextPath()
                        + "/gym/dashboard"
                );
                return;
            }

            if (principal.tieneRol("CAJERO")) {
                response.sendRedirect(
                    request.getContextPath()
                        + "/pos"
                );
                return;
            }

            response.sendRedirect(
                request.getContextPath()
                    + "/admin"
            );

            return;
        }

        /*
         * Compatibilidad temporal por si algún proceso interno
         * todavía crea un User estándar de Spring Security.
         */
        boolean esAdministrador =
            authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        boolean esCajero =
            authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_CAJERO"::equals);

        if (esAdministrador) {
            response.sendRedirect(
                request.getContextPath()
                    + "/admin"
            );
            return;
        }

        if (esCajero) {
            response.sendRedirect(
                request.getContextPath()
                    + "/pos"
            );
            return;
        }

        response.sendRedirect(
            request.getContextPath()
                + "/login?sinDestino"
        );
    }

    private void guardarContextoEnSesion(
        HttpSession session,
        CommerceUserPrincipal principal
    ) {
        session.setAttribute(
            "usuarioId",
            principal.getUsuarioId()
        );

        session.setAttribute(
            "usuarioNombre",
            principal.getNombre()
        );

        session.setAttribute(
            "tenantId",
            principal.getTenantId()
        );

        session.setAttribute(
            "tenantSlug",
            principal.getTenantSlug()
        );

        session.setAttribute(
            "tenantNombre",
            principal.getTenantNombre()
        );

        session.setAttribute(
            "tipoNegocio",
            principal.getTipoNegocio()
        );

        session.setAttribute(
            "sucursalId",
            principal.getSucursalId()
        );
    }
}
