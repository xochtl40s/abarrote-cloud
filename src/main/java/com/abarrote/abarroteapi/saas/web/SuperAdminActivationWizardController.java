package com.abarrote.abarroteapi.saas.web;

import com.abarrote.abarroteapi.saas.entity.SaasProspecto;
import com.abarrote.abarroteapi.saas.repository.SaasProspectoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/super-admin/prospectos")
public class SuperAdminActivationWizardController {

    private final SaasProspectoRepository prospectoRepository;
    private final JdbcTemplate jdbcTemplate;

    public SuperAdminActivationWizardController(
            SaasProspectoRepository prospectoRepository,
            JdbcTemplate jdbcTemplate) {

        this.prospectoRepository = prospectoRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/{folio}/activar")
    public String mostrarAsistente(
            @PathVariable String folio,
            Model model) {

        SaasProspecto prospecto = prospectoRepository
                .findByFolioIgnoreCase(folio)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No existe el prospecto con folio: " + folio
                        )
                );

        String planCodigo =
                obtenerPlanCodigo(
                        prospecto.getPlanSolicitadoId()
                );

        String planNombre =
                obtenerPlanNombre(
                        prospecto.getPlanSolicitadoId()
                );

        boolean activado =
                prospecto.getTenantId() != null
                        || "ACTIVADO".equalsIgnoreCase(
                                prospecto.getEstado()
                        );

        model.addAttribute(
                "prospecto",
                prospecto
        );

        model.addAttribute(
                "planCodigo",
                planCodigo
        );

        model.addAttribute(
                "planNombre",
                planNombre
        );

        model.addAttribute(
                "activado",
                activado
        );

        return "super-admin/prospecto-activar";
    }

    private String obtenerPlanCodigo(Long planId) {

        if (planId == null) {
            return "BASICO";
        }

        return jdbcTemplate.query(
                """
                SELECT codigo
                FROM saas_plan
                WHERE id = ?
                """,
                resultSet ->
                        resultSet.next()
                                ? resultSet.getString("codigo")
                                : "BASICO",
                planId
        );
    }

    private String obtenerPlanNombre(Long planId) {

        if (planId == null) {
            return "Plan Básico";
        }

        return jdbcTemplate.query(
                """
                SELECT nombre
                FROM saas_plan
                WHERE id = ?
                """,
                resultSet ->
                        resultSet.next()
                                ? resultSet.getString("nombre")
                                : "Plan Básico",
                planId
        );
    }
}
