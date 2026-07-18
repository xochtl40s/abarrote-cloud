package com.abarrote.abarroteapi.gym.web;

import com.abarrote.abarroteapi.gym.repository.ClienteGymRepository;
import com.abarrote.abarroteapi.gym.repository.PlanMembresiaRepository;
import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GymDashboardController {

    private final ClienteGymRepository clienteRepository;

    private final PlanMembresiaRepository planRepository;

    public GymDashboardController(
        ClienteGymRepository clienteRepository,
        PlanMembresiaRepository planRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.planRepository = planRepository;
    }

    @GetMapping("/gym/dashboard")
    public String dashboard(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        if (principal == null) {
            return "redirect:/login?sesionRequerida";
        }

        if (!principal.esGym()) {
            return "redirect:/admin?moduloIncorrecto";
        }

        int totalClientes =
            clienteRepository
                .findAllByTenantIdOrderByNombreAsc(
                    principal.getTenantId()
                )
                .size();

        int totalPlanes =
            planRepository
                .findAllByTenantIdAndActivoTrueOrderByPrecioAsc(
                    principal.getTenantId()
                )
                .size();

        model.addAttribute(
            "usuarioNombre",
            principal.getNombre()
        );

        model.addAttribute(
            "tenantNombre",
            principal.getTenantNombre()
        );

        model.addAttribute(
            "tenantSlug",
            principal.getTenantSlug()
        );

        model.addAttribute(
            "totalClientes",
            totalClientes
        );

        model.addAttribute(
            "totalPlanes",
            totalPlanes
        );

        return "gym/dashboard";
    }

    @GetMapping("/gym")
    public String inicioGym() {
        return "redirect:/gym/dashboard";
    }
}
