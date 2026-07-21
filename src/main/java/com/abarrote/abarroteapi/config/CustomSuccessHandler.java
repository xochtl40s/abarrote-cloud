package com.abarrote.abarroteapi.config;

import java.io.IOException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomSuccessHandler
        implements AuthenticationSuccessHandler {

    private final JdbcTemplate jdbcTemplate;

    public CustomSuccessHandler(
            JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        List<UsuarioDestino> destinos =
                jdbcTemplate.query(
                        """
                        SELECT
                            u.rol,
                            t.tipo_negocio
                        FROM usuario u
                        JOIN tenant t
                          ON t.id = u.tenant_id
                        WHERE LOWER(u.username) =
                              LOWER(?)
                          AND u.activo = TRUE
                        """,
                        (
                                resultSet,
                                rowNumber
                        ) -> new UsuarioDestino(
                                resultSet.getString(
                                        "rol"
                                ),
                                resultSet.getString(
                                        "tipo_negocio"
                                )
                        ),
                        authentication.getName()
                );

        if (!destinos.isEmpty()) {

            UsuarioDestino destino =
                    destinos.get(0);

            String tipoNegocio =
                    normalizar(
                            destino.tipoNegocio()
                    );

            String rol =
                    normalizar(
                            destino.rol()
                    );

            /*
             * =====================================================
             * SUPER ADMINISTRADOR GLOBAL DE COMMERCE CLOUD
             * =====================================================
             */
            if ("PLATFORM".equals(tipoNegocio)
                    && "SUPER_ADMIN".equals(rol)) {

                response.sendRedirect(
                        "/super-admin"
                );

                return;
            }

            /*
             * =====================================================
             * RESTAURANTE
             * =====================================================
             */
            if ("RESTAURANTE".equals(tipoNegocio)) {

                if ("MESERO".equals(rol)) {

                    response.sendRedirect(
                            "/restaurante/mesero"
                    );

                    return;
                }

                if ("ADMIN".equals(rol)) {

                    response.sendRedirect(
                            "/restaurante/dashboard"
                    );

                    return;
                }
            }

            /*
             * =====================================================
             * GYM
             * =====================================================
             */
            if ("GYM".equals(tipoNegocio)) {

                response.sendRedirect(
                        "/gym/dashboard"
                );

                return;
            }

            /*
             * =====================================================
             * ABARROTES
             * =====================================================
             */
            if ("ABARROTES".equals(tipoNegocio)) {

                if ("CAJERO".equals(rol)) {

                    response.sendRedirect(
                            "/pos"
                    );

                    return;
                }

                if ("ADMIN".equals(rol)) {

                    response.sendRedirect(
                            "/admin"
                    );

                    return;
                }
            }
        }

        /*
         * =========================================================
         * RESPALDO POR AUTORIDAD
         * =========================================================
         */

        boolean superAdministrador =
                tieneAutoridad(
                        authentication,
                        "ROLE_SUPER_ADMIN"
                );

        boolean administrador =
                tieneAutoridad(
                        authentication,
                        "ROLE_ADMIN"
                );

        boolean cajero =
                tieneAutoridad(
                        authentication,
                        "ROLE_CAJERO"
                );

        boolean mesero =
                tieneAutoridad(
                        authentication,
                        "ROLE_MESERO"
                );

        if (superAdministrador) {

            response.sendRedirect(
                    "/super-admin"
            );

            return;
        }

        if (mesero) {

            response.sendRedirect(
                    "/restaurante/mesero"
            );

            return;
        }

        if (administrador) {

            response.sendRedirect(
                    "/admin"
            );

            return;
        }

        if (cajero) {

            response.sendRedirect(
                    "/pos"
            );

            return;
        }

        response.sendRedirect(
                "/login?error"
        );
    }

    private boolean tieneAutoridad(
            Authentication authentication,
            String autoridad) {

        return authentication
                .getAuthorities()
                .stream()
                .map(
                        GrantedAuthority::getAuthority
                )
                .anyMatch(
                        autoridad::equals
                );
    }

    private String normalizar(
            String valor) {

        if (valor == null) {
            return "";
        }

        String resultado =
                valor
                        .trim()
                        .toUpperCase();

        if (resultado.startsWith("ROLE_")) {

            return resultado.substring(
                    "ROLE_".length()
            );
        }

        return resultado;
    }

    private record UsuarioDestino(
            String rol,
            String tipoNegocio) {
    }
}
