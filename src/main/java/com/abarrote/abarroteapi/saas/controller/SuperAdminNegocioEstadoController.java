package com.abarrote.abarroteapi.saas.controller;

import com.abarrote.abarroteapi.saas.service.SaasNegocioEstadoService;
import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/super-admin/prospectos")
public class SuperAdminNegocioEstadoController {

    private final SaasNegocioEstadoService estadoService;

    public SuperAdminNegocioEstadoController(
            SaasNegocioEstadoService estadoService) {

        this.estadoService = estadoService;
    }

    @PostMapping("/{folio}/alternar-estado")
    public String alternarEstado(
            @PathVariable String folio,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            CommerceUserPrincipal principal =
                    obtenerSuperAdmin(
                            authentication
                    );

            String mensaje =
                    estadoService.alternarEstado(
                            folio,
                            principal.getUsuarioId()
                    );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    mensaje
            );

        } catch (IllegalArgumentException
                 | IllegalStateException exception) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );
        }

        return "redirect:/super-admin/prospectos";
    }

    private CommerceUserPrincipal obtenerSuperAdmin(
            Authentication authentication) {

        if (authentication == null
                || !(authentication.getPrincipal()
                    instanceof CommerceUserPrincipal principal)
                || !principal.tieneRol("SUPER_ADMIN")) {

            throw new AccessDeniedException(
                    "Esta operación requiere SUPER_ADMIN."
            );
        }

        return principal;
    }
}
