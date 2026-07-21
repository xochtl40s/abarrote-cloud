package com.abarrote.abarroteapi.restaurante.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class RestauranteController {

    private final JdbcTemplate jdbcTemplate;

    public RestauranteController(
            JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/restaurante/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model) {

        ContextoRestaurante contexto =
                cargarContexto(authentication);

        validarRol(
                contexto,
                "ADMIN"
        );

        Long tenantId =
                contexto.tenantId();

        Integer totalMesas =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM restaurante_mesa
                        WHERE tenant_id = ?
                          AND activa = TRUE
                        """,
                        Integer.class,
                        tenantId
                );

        Integer mesasOcupadas =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM restaurante_mesa
                        WHERE tenant_id = ?
                          AND estado = 'OCUPADA'
                          AND activa = TRUE
                        """,
                        Integer.class,
                        tenantId
                );

        Integer pedidosAbiertos =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM restaurante_pedido
                        WHERE tenant_id = ?
                          AND estado IN (
                              'ABIERTO',
                              'EN_PREPARACION',
                              'LISTO',
                              'ENTREGADO'
                          )
                        """,
                        Integer.class,
                        tenantId
                );

        Integer platillosDisponibles =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM restaurante_platillo
                        WHERE tenant_id = ?
                          AND activo = TRUE
                          AND disponible = TRUE
                        """,
                        Integer.class,
                        tenantId
                );

        BigDecimal ventasDia =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COALESCE(
                            SUM(total),
                            0
                        )
                        FROM restaurante_pedido
                        WHERE tenant_id = ?
                          AND estado = 'CERRADO'
                          AND fecha_cierre::date =
                              CURRENT_DATE
                        """,
                        BigDecimal.class,
                        tenantId
                );

        Integer ticketsDia =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM restaurante_pedido
                        WHERE tenant_id = ?
                          AND estado = 'CERRADO'
                          AND fecha_cierre::date =
                              CURRENT_DATE
                        """,
                        Integer.class,
                        tenantId
                );

        BigDecimal ticketPromedio =
                BigDecimal.ZERO;

        if (ticketsDia != null
                && ticketsDia > 0
                && ventasDia != null) {

            ticketPromedio =
                    ventasDia.divide(
                            BigDecimal.valueOf(ticketsDia),
                            2,
                            java.math.RoundingMode.HALF_UP
                    );
        }

        List<Map<String, Object>> mesas =
                jdbcTemplate.queryForList(
                        """
                        SELECT
                            id,
                            numero,
                            nombre,
                            capacidad,
                            estado
                        FROM restaurante_mesa
                        WHERE tenant_id = ?
                          AND activa = TRUE
                        ORDER BY numero
                        """,
                        tenantId
                );

        List<Map<String, Object>> platillos =
                jdbcTemplate.queryForList(
                        """
                        SELECT
                            p.id,
                            p.codigo,
                            p.nombre,
                            p.precio,
                            p.disponible,
                            c.nombre AS categoria
                        FROM restaurante_platillo p
                        JOIN restaurante_categoria c
                          ON c.id = p.categoria_id
                        WHERE p.tenant_id = ?
                          AND p.activo = TRUE
                        ORDER BY
                            c.orden_visual,
                            p.nombre
                        """,
                        tenantId
                );

        model.addAttribute(
                "tenantNombre",
                contexto.tenantNombre()
        );

        model.addAttribute(
                "usuarioNombre",
                contexto.usuarioNombre()
        );

        model.addAttribute(
                "totalMesas",
                totalMesas
        );

        model.addAttribute(
                "mesasOcupadas",
                mesasOcupadas
        );

        model.addAttribute(
                "pedidosAbiertos",
                pedidosAbiertos
        );

        model.addAttribute(
                "platillosDisponibles",
                platillosDisponibles
        );

        model.addAttribute(
                "ventasDia",
                ventasDia
        );

        model.addAttribute(
                "ticketsDia",
                ticketsDia
        );

        model.addAttribute(
                "ticketPromedio",
                ticketPromedio
        );

        model.addAttribute(
                "mesas",
                mesas
        );

        model.addAttribute(
                "platillos",
                platillos
        );

        return "restaurante/dashboard";
    }

    @GetMapping("/restaurante/mesero")
    public String mesero(
            Authentication authentication,
            Model model) {

        ContextoRestaurante contexto =
                cargarContexto(authentication);

        if (!"MESERO".equalsIgnoreCase(
                contexto.rol()
        )
                && !"ADMIN".equalsIgnoreCase(
                contexto.rol()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene permisos de mesero"
            );
        }

        Long tenantId =
                contexto.tenantId();

        List<Map<String, Object>> mesas =
                jdbcTemplate.queryForList(
                        """
                        SELECT
                            id,
                            numero,
                            nombre,
                            capacidad,
                            estado
                        FROM restaurante_mesa
                        WHERE tenant_id = ?
                          AND activa = TRUE
                        ORDER BY numero
                        """,
                        tenantId
                );

        List<Map<String, Object>> categorias =
                jdbcTemplate.queryForList(
                        """
                        SELECT
                            id,
                            nombre,
                            descripcion
                        FROM restaurante_categoria
                        WHERE tenant_id = ?
                          AND activa = TRUE
                        ORDER BY
                            orden_visual,
                            nombre
                        """,
                        tenantId
                );

        List<Map<String, Object>> platillos =
                jdbcTemplate.queryForList(
                        """
                        SELECT
                            p.id,
                            p.codigo,
                            p.nombre,
                            p.descripcion,
                            p.precio,
                            p.categoria_id,
                            c.nombre AS categoria
                        FROM restaurante_platillo p
                        JOIN restaurante_categoria c
                          ON c.id = p.categoria_id
                        WHERE p.tenant_id = ?
                          AND p.activo = TRUE
                          AND p.disponible = TRUE
                        ORDER BY
                            c.orden_visual,
                            p.nombre
                        """,
                        tenantId
                );

        model.addAttribute(
                "tenantNombre",
                contexto.tenantNombre()
        );

        model.addAttribute(
                "usuarioNombre",
                contexto.usuarioNombre()
        );

        model.addAttribute(
                "mesas",
                mesas
        );

        model.addAttribute(
                "categorias",
                categorias
        );

        model.addAttribute(
                "platillos",
                platillos
        );

        return "restaurante/mesero";
    }

    private ContextoRestaurante cargarContexto(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Sesión no válida"
            );
        }

        List<ContextoRestaurante> resultados =
                jdbcTemplate.query(
                        """
                        SELECT
                            u.id AS usuario_id,
                            u.nombre AS usuario_nombre,
                            u.username,
                            u.rol,
                            u.tenant_id,
                            t.nombre AS tenant_nombre,
                            t.slug AS tenant_slug,
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
                        ) -> new ContextoRestaurante(
                                resultSet.getLong(
                                        "usuario_id"
                                ),
                                resultSet.getString(
                                        "usuario_nombre"
                                ),
                                resultSet.getString(
                                        "username"
                                ),
                                resultSet.getString(
                                        "rol"
                                ),
                                resultSet.getLong(
                                        "tenant_id"
                                ),
                                resultSet.getString(
                                        "tenant_nombre"
                                ),
                                resultSet.getString(
                                        "tenant_slug"
                                ),
                                resultSet.getString(
                                        "tipo_negocio"
                                )
                        ),
                        authentication.getName()
                );

        if (resultados.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuario sin tenant válido"
            );
        }

        ContextoRestaurante contexto =
                resultados.get(0);

        if (!"RESTAURANTE".equalsIgnoreCase(
                contexto.tipoNegocio()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no pertenece a Restaurante Cloud"
            );
        }

        return contexto;
    }

    private void validarRol(
            ContextoRestaurante contexto,
            String rolRequerido) {

        if (!rolRequerido.equalsIgnoreCase(
                contexto.rol()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene el rol requerido"
            );
        }
    }

    private record ContextoRestaurante(
            Long usuarioId,
            String usuarioNombre,
            String username,
            String rol,
            Long tenantId,
            String tenantNombre,
            String tenantSlug,
            String tipoNegocio) {
    }
}
