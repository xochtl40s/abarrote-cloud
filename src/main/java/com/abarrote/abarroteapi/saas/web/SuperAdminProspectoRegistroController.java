package com.abarrote.abarroteapi.saas.web;

import com.abarrote.abarroteapi.saas.dto.SaasProspectoRequest;
import com.abarrote.abarroteapi.saas.dto.SaasProspectoResponse;
import com.abarrote.abarroteapi.saas.service.SaasProspectoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/super-admin/prospectos")
public class SuperAdminProspectoRegistroController {

    private final SaasProspectoService saasProspectoService;

    public SuperAdminProspectoRegistroController(
            SaasProspectoService saasProspectoService) {

        this.saasProspectoService = saasProspectoService;
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {

        if (!model.containsAttribute("prospecto")) {
            SaasProspectoRequest request =
                    new SaasProspectoRequest();

            request.setTipoNegocio("RESTAURANTE");
            request.setPlanCodigo("PRO");
            request.setNumeroMesas(1);

            model.addAttribute(
                    "prospecto",
                    request
            );
        }

        return "super-admin/prospecto-form";
    }

    @PostMapping("/registrar")
    public String registrar(
            @ModelAttribute("prospecto")
            SaasProspectoRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        normalizar(request);

        String error = validar(request);

        if (error != null) {
            model.addAttribute(
                    "error",
                    error
            );

            return "super-admin/prospecto-form";
        }

        try {
            SaasProspectoResponse creado =
                    saasProspectoService.registrar(request);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Prospecto registrado correctamente. Folio: "
                            + creado.getFolio()
            );

            return "redirect:/super-admin/prospectos";

        } catch (IllegalArgumentException exception) {

            model.addAttribute(
                    "error",
                    exception.getMessage()
            );

            return "super-admin/prospecto-form";

        } catch (Exception exception) {

            model.addAttribute(
                    "error",
                    "No fue posible registrar el prospecto. "
                            + "Verifica la información e inténtalo nuevamente."
            );

            return "super-admin/prospecto-form";
        }
    }

    private void normalizar(
            SaasProspectoRequest request) {

        request.setNombreNegocio(
                limpiar(request.getNombreNegocio())
        );

        request.setPropietario(
                limpiar(request.getPropietario())
        );

        request.setCorreo(
                limpiar(request.getCorreo()).toLowerCase()
        );

        request.setWhatsapp(
                limpiar(request.getWhatsapp())
        );

        request.setCiudad(
                limpiar(request.getCiudad())
        );

        request.setTipoNegocio(
                limpiar(request.getTipoNegocio()).toUpperCase()
        );

        request.setPlanCodigo(
                limpiar(request.getPlanCodigo()).toUpperCase()
        );

        request.setObservaciones(
                limpiar(request.getObservaciones())
        );
    }

    private String validar(
            SaasProspectoRequest request) {

        if (request.getNombreNegocio().isBlank()) {
            return "El nombre del negocio es obligatorio.";
        }

        if (request.getPropietario().isBlank()) {
            return "El nombre del propietario es obligatorio.";
        }

        if (request.getCorreo().isBlank()) {
            return "El correo es obligatorio.";
        }

        if (!request.getCorreo().matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )) {
            return "El correo no tiene un formato válido.";
        }

        if (request.getWhatsapp().isBlank()) {
            return "El número de WhatsApp es obligatorio.";
        }

        String whatsappNumerico =
                request.getWhatsapp().replaceAll("\\D", "");

        if (whatsappNumerico.length() < 10
                || whatsappNumerico.length() > 15) {

            return "WhatsApp debe contener entre 10 y 15 dígitos.";
        }

        request.setWhatsapp(whatsappNumerico);

        if (request.getCiudad().isBlank()) {
            return "La ciudad es obligatoria.";
        }

        if (!esTipoNegocioValido(
                request.getTipoNegocio()
        )) {
            return "El tipo de negocio seleccionado no es válido.";
        }

        if (!esPlanValido(
                request.getPlanCodigo()
        )) {
            return "El plan seleccionado no es válido.";
        }

        if ("RESTAURANTE".equals(
                request.getTipoNegocio()
        )) {

            if (request.getNumeroMesas() == null
                    || request.getNumeroMesas() < 1) {

                return "Para un restaurante debes indicar al menos una mesa.";
            }

            if (request.getNumeroMesas() > 500) {
                return "El número de mesas no puede ser mayor a 500.";
            }

        } else {
            request.setNumeroMesas(null);
        }

        return null;
    }

    private boolean esTipoNegocioValido(
            String tipoNegocio) {

        return "RESTAURANTE".equals(tipoNegocio)
                || "ABARROTES".equals(tipoNegocio)
                || "GYM".equals(tipoNegocio);
    }

    private boolean esPlanValido(
            String planCodigo) {

        return "BASICO".equals(planCodigo)
                || "PRO".equals(planCodigo)
                || "ENTERPRISE".equals(planCodigo);
    }

    private String limpiar(String valor) {

        return valor == null
                ? ""
                : valor.trim();
    }
}
