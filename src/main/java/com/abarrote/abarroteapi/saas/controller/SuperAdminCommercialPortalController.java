package com.abarrote.abarroteapi.saas.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/super-admin")
public class SuperAdminCommercialPortalController {

    private final JdbcTemplate jdbcTemplate;

    public SuperAdminCommercialPortalController(
            JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/resumen")
    public String resumen(Model model) {

        model.addAttribute(
                "totalProspectos",
                contar(
                    """
                    SELECT COUNT(*)
                    FROM saas_prospecto
                    WHERE estado <> 'ELIMINADO'
                    """
                )
        );

        model.addAttribute(
                "prospectosNuevos",
                contar(
                    """
                    SELECT COUNT(*)
                    FROM saas_prospecto
                    WHERE estado = 'NUEVO'
                    """
                )
        );

        model.addAttribute(
                "negociosActivos",
                contar(
                    """
                    SELECT COUNT(*)
                    FROM saas_prospecto
                    WHERE estado = 'ACTIVADO'
                    """
                )
        );

        model.addAttribute(
                "negociosSuspendidos",
                contar(
                    """
                    SELECT COUNT(*)
                    FROM saas_prospecto
                    WHERE estado = 'EN_PROCESO'
                      AND tenant_id IS NOT NULL
                    """
                )
        );

        model.addAttribute(
                "usuariosActivos",
                contar(
                    """
                    SELECT COUNT(*)
                    FROM usuario
                    WHERE activo = TRUE
                    """
                )
        );

        model.addAttribute(
                "ultimosNegocios",
                jdbcTemplate.queryForList(
                    """
                    SELECT
                        p.folio,
                        p.nombre_negocio,
                        p.tipo_negocio,
                        p.estado,
                        p.fecha_creacion,
                        t.slug,
                        t.plan
                    FROM saas_prospecto p
                    LEFT JOIN tenant t
                           ON t.id = p.tenant_id
                    WHERE p.estado <> 'ELIMINADO'
                    ORDER BY p.fecha_creacion DESC
                    LIMIT 10
                    """
                )
        );

        return "super-admin/resumen";
    }

    @GetMapping("/planes")
    public String planes(Model model) {

        List<Map<String, Object>> planes =
                jdbcTemplate.queryForList(
                    """
                    SELECT
                        id,
                        codigo,
                        nombre,
                        descripcion,
                        precio_mensual,
                        precio_anual,
                        max_usuarios,
                        max_sucursales,
                        max_mesas,
                        permite_reportes_avanzados,
                        permite_asistente_ia,
                        permite_exportacion_excel,
                        permite_soporte_prioritario,
                        dias_prueba,
                        activo,
                        orden_visual
                    FROM saas_plan
                    ORDER BY orden_visual, id
                    """
                );

        model.addAttribute(
                "planes",
                planes
        );

        return "super-admin/planes";
    }

    @GetMapping("/empresas")
    public String empresas(Model model) {

        model.addAttribute(
                "empresas",
                jdbcTemplate.queryForList(
                    """
                    SELECT
                        t.id,
                        t.nombre,
                        t.slug,
                        t.tipo_negocio,
                        t.estado,
                        t.plan,
                        t.fecha_alta,
                        t.fecha_vencimiento,
                        t.activo,
                        COUNT(u.id) AS total_usuarios
                    FROM tenant t
                    LEFT JOIN usuario u
                           ON u.tenant_id = t.id
                    WHERE t.tipo_negocio <> 'PLATFORM'
                    GROUP BY
                        t.id,
                        t.nombre,
                        t.slug,
                        t.tipo_negocio,
                        t.estado,
                        t.plan,
                        t.fecha_alta,
                        t.fecha_vencimiento,
                        t.activo
                    ORDER BY t.fecha_alta DESC
                    """
                )
        );

        return "super-admin/empresas";
    }

    @GetMapping("/suscripciones")
    public String suscripciones(Model model) {

        model.addAttribute(
                "suscripciones",
                jdbcTemplate.queryForList(
                    """
                    SELECT
                        s.*,
                        t.nombre AS tenant_nombre,
                        t.slug AS tenant_slug
                    FROM saas_suscripcion s
                    LEFT JOIN tenant t
                           ON t.id = s.tenant_id
                    ORDER BY s.id DESC
                    """
                )
        );

        return "super-admin/suscripciones";
    }

    @GetMapping("/bitacora")
    public String bitacora(Model model) {

        model.addAttribute(
                "eventos",
                jdbcTemplate.queryForList(
                    """
                    SELECT
                        p.folio,
                        p.nombre_negocio,
                        p.estado,
                        p.observaciones,
                        p.fecha_creacion,
                        p.fecha_actualizacion,
                        t.slug AS tenant_slug,
                        t.tipo_negocio
                    FROM saas_prospecto p
                    LEFT JOIN tenant t
                           ON t.id = p.tenant_id
                    ORDER BY p.fecha_actualizacion DESC
                    LIMIT 100
                    """
                )
        );

        return "super-admin/bitacora";
    }

    private long contar(String sql) {

        Long valor =
                jdbcTemplate.queryForObject(
                        sql,
                        Long.class
                );

        return valor == null
                ? 0L
                : valor;
    }
}
