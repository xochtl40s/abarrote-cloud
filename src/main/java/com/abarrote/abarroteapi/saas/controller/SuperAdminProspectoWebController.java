package com.abarrote.abarroteapi.saas.controller;

import com.abarrote.abarroteapi.saas.dto.SaasProspectoResponse;
import com.abarrote.abarroteapi.saas.service.SaasProspectoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/super-admin")
public class SuperAdminProspectoWebController {

    private final SaasProspectoService prospectoService;

    public SuperAdminProspectoWebController(
        SaasProspectoService prospectoService
    ) {
        this.prospectoService = prospectoService;
    }

    @GetMapping("/prospectos")
    public String mostrarProspectos(
        @RequestParam(
            name = "registroExitoso",
            required = false
        ) Boolean registroExitoso,
        Model model
    ) {
        List<SaasProspectoResponse> prospectos =
            prospectoService.listarTodos();

        long totalProspectos = prospectos.size();

        long totalNuevos = prospectos
            .stream()
            .filter(prospecto ->
                "NUEVO".equalsIgnoreCase(
                    prospecto.getEstado()
                )
            )
            .count();

        long totalEnProceso = prospectos
            .stream()
            .filter(prospecto ->
                "EN_PROCESO".equalsIgnoreCase(
                    prospecto.getEstado()
                )
            )
            .count();

        long totalActivados = prospectos
            .stream()
            .filter(prospecto ->
                "ACTIVADO".equalsIgnoreCase(
                    prospecto.getEstado()
                )
            )
            .count();

        model.addAttribute("prospectos", prospectos);
        model.addAttribute("totalProspectos", totalProspectos);
        model.addAttribute("totalNuevos", totalNuevos);
        model.addAttribute("totalEnProceso", totalEnProceso);
        model.addAttribute("totalActivados", totalActivados);
        model.addAttribute(
            "registroExitoso",
            Boolean.TRUE.equals(registroExitoso)
        );

        return "super-admin/prospectos";
    }
}
