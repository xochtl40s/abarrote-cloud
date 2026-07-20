package com.abarrote.abarroteapi.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommerceLandingController {

    private final String whatsapp;
    private final String nombreComercial;

    public CommerceLandingController(
            @Value("${commerce.marketing.whatsapp:5215512345678}")
            String whatsapp,
            @Value("${commerce.marketing.nombre:Commerce Cloud}")
            String nombreComercial) {

        this.whatsapp = limpiarTelefono(whatsapp);
        this.nombreComercial = nombreComercial;
    }

    @GetMapping({
            "/",
            "/inicio",
            "/commerce-cloud",
            "/productos"
    })
    public String mostrarPortalComercial(Model model) {

        model.addAttribute("nombreComercial", nombreComercial);

        model.addAttribute(
                "whatsappGeneral",
                crearWhatsappUrl(
                        "Hola, me interesa conocer Commerce Cloud y solicitar una demostración."
                )
        );

        model.addAttribute(
                "whatsappAbarrotes",
                crearWhatsappUrl(
                        "Hola, me interesa una demostración de Abarrotes Cloud para mi negocio."
                )
        );

        model.addAttribute(
                "whatsappGym",
                crearWhatsappUrl(
                        "Hola, me interesa una demostración de Gym Cloud para administrar mi gimnasio."
                )
        );

        model.addAttribute(
                "whatsappRestaurante",
                crearWhatsappUrl(
                        "Hola, me interesa una demostración de Restaurante Cloud para mi restaurante."
                )
        );

        return "commerce-landing";
    }

    private String crearWhatsappUrl(String mensaje) {
        String mensajeCodificado =
                URLEncoder.encode(mensaje, StandardCharsets.UTF_8);

        return "https://wa.me/" + whatsapp + "?text=" + mensajeCodificado;
    }

    private String limpiarTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return "5215512345678";
        }

        return telefono.replaceAll("[^0-9]", "");
    }
}
