package com.abarrote.abarroteapi.saas.controller;

import com.abarrote.abarroteapi.saas.dto.SaasProspectoResponse;
import com.abarrote.abarroteapi.saas.service.SaasPlanService;
import com.abarrote.abarroteapi.saas.service.SaasProspectoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/super-admin")
public class SuperAdminDashboardController {

    private final SaasPlanService planService;
    private final SaasProspectoService prospectoService;

    public SuperAdminDashboardController(
            SaasPlanService planService,
            SaasProspectoService prospectoService
    ) {
        this.planService = planService;
        this.prospectoService = prospectoService;
    }

    @GetMapping({"", "/"})
    public String dashboard(Model model) {

        List<SaasProspectoResponse> prospectos =
                prospectoService.listarTodos();

        long totalProspectos = prospectos.size();

        long prospectosNuevos = contarPorEstado(
                prospectos,
                "NUEVO"
        );

        long prospectosEnProceso = contarPorEstado(
                prospectos,
                "EN_PROCESO"
        );

        long empresasActivadas = contarPorEstado(
                prospectos,
                "ACTIVADO"
        );

        List<SaasProspectoResponse> prospectosRecientes =
                prospectos
                        .stream()
                        .limit(5)
                        .toList();

        model.addAttribute(
                "planesActivos",
                planService.contarPlanesActivos()
        );

        model.addAttribute(
                "totalProspectos",
                totalProspectos
        );

        model.addAttribute(
                "prospectosNuevos",
                prospectosNuevos
        );

        model.addAttribute(
                "prospectosEnProceso",
                prospectosEnProceso
        );

        model.addAttribute(
                "empresasActivadas",
                empresasActivadas
        );

        model.addAttribute(
                "prospectosRecientes",
                prospectosRecientes
        );

        return "super-admin/dashboard";
    }

    private long contarPorEstado(
            List<SaasProspectoResponse> prospectos,
            String estado
    ) {
        return prospectos
                .stream()
                .filter(prospecto ->
                        estado.equalsIgnoreCase(
                                prospecto.getEstado()
                        )
                )
                .count();
    }
}
