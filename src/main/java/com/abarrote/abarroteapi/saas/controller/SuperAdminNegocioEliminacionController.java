package com.abarrote.abarroteapi.saas.controller;

import com.abarrote.abarroteapi.saas.service.SaasNegocioEliminacionService;
import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/super-admin/prospectos")
public class SuperAdminNegocioEliminacionController {

    private final SaasNegocioEliminacionService eliminacionService;

    public SuperAdminNegocioEliminacionController(
            SaasNegocioEliminacionService eliminacionService) {

        this.eliminacionService = eliminacionService;
    }

    @PostMapping("/{folio}/eliminar")
    public String eliminar(
            @PathVariable String folio,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            CommerceUserPrincipal principal =
                    obtenerSuperAdmin(authentication);

            eliminacionService.eliminarNegocio(
                    folio,
                    principal.getUsuarioId()
            );

            /*
             * Elimina posibles credenciales temporales
             * conservadas para ese prospecto.
             */
            session.removeAttribute(
                    "commerce.activation.result."
                            + folio.toUpperCase()
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "El negocio fue eliminado y sus accesos "
                            + "quedaron desactivados."
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
