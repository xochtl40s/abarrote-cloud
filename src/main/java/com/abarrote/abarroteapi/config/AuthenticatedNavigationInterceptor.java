package com.abarrote.abarroteapi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collection;

@Component
public class AuthenticatedNavigationInterceptor
        implements HandlerInterceptor {

    private static final String LAST_PATH_ATTRIBUTE =
            "commerce.last.authenticated.path";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        aplicarEncabezadosNoCache(response);

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        boolean autenticado =
                estaAutenticado(authentication);

        String path =
                request.getRequestURI();

        /*
         * Un usuario que conserva una sesión válida nunca debe
         * volver a visualizar el formulario de login.
         */
        if ("/login".equals(path)
                && autenticado) {

            String destino =
                    obtenerDestino(
                            request,
                            authentication
                    );

            response.sendRedirect(
                    request.getContextPath()
                            + destino
            );

            return false;
        }

        /*
         * Conservamos la última pantalla HTML visitada.
         * No almacenamos APIs, recursos estáticos ni acciones POST.
         */
        if (autenticado
                && "GET".equalsIgnoreCase(
                        request.getMethod()
                )
                && esRutaNavegable(path)) {

            HttpSession session =
                    request.getSession(true);

            session.setAttribute(
                    LAST_PATH_ATTRIBUTE,
                    path
            );
        }

        return true;
    }

    private boolean estaAutenticado(
            Authentication authentication) {

        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication
                    instanceof AnonymousAuthenticationToken);
    }

    private String obtenerDestino(
            HttpServletRequest request,
            Authentication authentication) {

        HttpSession session =
                request.getSession(false);

        if (session != null) {
            Object ultimaRuta =
                    session.getAttribute(
                            LAST_PATH_ATTRIBUTE
                    );

            if (ultimaRuta instanceof String ruta
                    && esDestinoSeguro(ruta)) {

                return ruta;
            }
        }

        Collection<? extends GrantedAuthority> authorities =
                authentication.getAuthorities();

        if (tieneRol(
                authorities,
                "ROLE_SUPER_ADMIN"
        )) {
            return "/super-admin";
        }

        /*
         * Para ADMIN se utiliza /admin como ruta segura de
         * recuperación. CustomSuccessHandler continúa siendo
         * responsable de la redirección inicial por vertical.
         */
        if (tieneRol(
                authorities,
                "ROLE_ADMIN"
        )) {
            return "/admin";
        }

        return "/";
    }

    private boolean tieneRol(
            Collection<? extends GrantedAuthority> authorities,
            String rol) {

        return authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(rol::equals);
    }

    private boolean esRutaNavegable(
            String path) {

        if (path == null
                || path.isBlank()) {
            return false;
        }

        return !path.equals("/login")
                && !path.equals("/logout")
                && !path.equals("/error")
                && !path.startsWith("/css/")
                && !path.startsWith("/js/")
                && !path.startsWith("/images/")
                && !path.startsWith("/webjars/")
                && !path.startsWith("/favicon")
                && !path.startsWith("/api/");
    }

    private boolean esDestinoSeguro(
            String path) {

        return path.startsWith("/")
                && !path.startsWith("//")
                && !path.equals("/login")
                && !path.equals("/logout")
                && !path.equals("/error");
    }

    private void aplicarEncabezadosNoCache(
            HttpServletResponse response) {

        response.setHeader(
                "Cache-Control",
                "no-store, no-cache, must-revalidate, "
                        + "max-age=0, private"
        );

        response.setHeader(
                "Pragma",
                "no-cache"
        );

        response.setHeader(
                "Expires",
                "0"
        );
    }
}
