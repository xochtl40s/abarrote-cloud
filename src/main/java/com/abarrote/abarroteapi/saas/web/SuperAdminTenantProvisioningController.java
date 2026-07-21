package com.abarrote.abarroteapi.saas.web;

import com.abarrote.abarroteapi.saas.dto.TenantProvisioningResult;
import com.abarrote.abarroteapi.saas.service.TenantProvisioningService;
import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping("/super-admin/prospectos")
public class SuperAdminTenantProvisioningController {

    private static final String SESSION_PREFIX =
            "commerce.activation.result.";

    private final TenantProvisioningService provisioningService;

    public SuperAdminTenantProvisioningController(
            TenantProvisioningService provisioningService) {

        this.provisioningService = provisioningService;
    }

    /*
     * POST:
     * ejecuta la transacción, guarda el resultado temporalmente
     * en la sesión autenticada y aplica Post/Redirect/Get.
     */
    @PostMapping("/{folio}/activar")
    public String activar(
            @PathVariable String folio,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Long usuarioId =
                    obtenerUsuarioId(authentication);

            TenantProvisioningResult resultado =
                    provisioningService.activarProspecto(
                            folio,
                            usuarioId
                    );

            session.setAttribute(
                    sessionKey(folio),
                    resultado
            );

            return "redirect:/super-admin/prospectos/"
                    + folio
                    + "/resultado";

        } catch (IllegalArgumentException
                 | IllegalStateException exception) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );

            return "redirect:/super-admin/prospectos/"
                    + folio
                    + "/activar";
        }
    }

    /*
     * GET:
     * solo presenta el resultado existente.
     * No vuelve a ejecutar el aprovisionamiento.
     */
    @GetMapping("/{folio}/resultado")
    public String mostrarResultado(
            @PathVariable String folio,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Object valor =
                session.getAttribute(
                        sessionKey(folio)
                );

        if (!(valor instanceof TenantProvisioningResult resultado)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Las credenciales temporales ya no están "
                            + "disponibles. Genera unas nuevas."
            );

            return "redirect:/super-admin/prospectos/"
                    + folio
                    + "/activar";
        }

        String loginUrl =
                ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/login")
                        .build()
                        .toUriString();

        model.addAttribute(
                "resultado",
                resultado
        );

        model.addAttribute(
                "loginUrl",
                loginUrl
        );

        return "super-admin/empresa-activada";
    }

    /*
     * El Super Admin confirma que ya copió las credenciales.
     * En ese momento se eliminan de la sesión.
     */
    @PostMapping("/{folio}/resultado/finalizar")
    public String finalizarResultado(
            @PathVariable String folio,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        session.removeAttribute(
                sessionKey(folio)
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Activación finalizada correctamente."
        );

        return "redirect:/super-admin/prospectos";
    }

    private Long obtenerUsuarioId(
            Authentication authentication) {

        if (authentication != null
                && authentication.getPrincipal()
                instanceof CommerceUserPrincipal principal) {

            return principal.getUsuarioId();
        }

        return null;
    }

    private String sessionKey(
            String folio) {

        return SESSION_PREFIX
                + folio.toUpperCase();
    }
}
