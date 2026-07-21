package com.abarrote.abarroteapi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Control central de caché para páginas HTML de Commerce Cloud.
 *
 * Responsabilidades:
 *
 * 1. Evitar que el navegador conserve páginas privadas sensibles.
 * 2. Evitar que las credenciales temporales queden disponibles
 *    mediante el historial del navegador.
 * 3. Permitir que /login siempre llegue al controlador de login.
 *
 * Este interceptor NO decide redirecciones por rol o tenant.
 *
 * La redirección posterior a una autenticación exitosa pertenece
 * exclusivamente a CustomSuccessHandler.
 */
@Component
public class AuthenticatedNavigationInterceptor
        implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        aplicarEncabezadosNoCache(response);

        /*
         * No se conserva ninguna "última ruta".
         *
         * En particular, nunca debemos guardar como destino:
         *
         * /super-admin/prospectos/{folio}/resultado
         *
         * porque esa página contiene credenciales temporales.
         *
         * La ruta /login siempre continúa normalmente.
         */
        return true;
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
