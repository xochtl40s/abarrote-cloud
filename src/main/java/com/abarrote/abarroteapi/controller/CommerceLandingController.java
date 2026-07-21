package com.abarrote.abarroteapi.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommerceLandingController {

    /*
     * Número de WhatsApp en formato internacional:
     *
     * 52  = México
     * 1   = prefijo usado para número móvil
     *
     * No se deben incluir:
     * - signo +
     * - espacios
     * - guiones
     * - paréntesis
     */
    private static final String WHATSAPP_NUMBER =
            "5215539862218";

    private static final String WHATSAPP_BASE_URL =
            "https://wa.me/";

    @GetMapping({
            "/",
            "/commerce",
            "/commerce-cloud"
    })
    public String mostrarLanding(
            Model model) {

        model.addAttribute(
                "whatsappGeneral",
                construirEnlaceWhatsApp(
                        """
                        Hola, me interesa conocer Commerce Cloud.

                        Quiero solicitar una demostración y recibir información sobre los planes disponibles.
                        """
                )
        );

        model.addAttribute(
                "whatsappAbarrotes",
                construirEnlaceWhatsApp(
                        """
                        Hola, me interesa Abarrotes Cloud.

                        Quiero solicitar una demostración para conocer el punto de venta, inventario, corte de caja y reportes.
                        """
                )
        );

        model.addAttribute(
                "whatsappRestaurante",
                construirEnlaceWhatsApp(
                        """
                        Hola, me interesa Restaurante Cloud.

                        Quiero solicitar una demostración para conocer el control de mesas, pedidos, meseros, cuentas y corte del día.
                        """
                )
        );

        model.addAttribute(
                "whatsappGym",
                construirEnlaceWhatsApp(
                        """
                        Hola, me interesa Gym Cloud.

                        Quiero solicitar una demostración para conocer el registro de clientes, membresías, pagos y reportes.
                        """
                )
        );

        return "commerce-landing";
    }

    private String construirEnlaceWhatsApp(
            String mensaje) {

        String mensajeNormalizado =
                mensaje
                        .strip()
                        .replace(
                                "\r\n",
                                "\n"
                        );

        String mensajeCodificado =
                URLEncoder.encode(
                                mensajeNormalizado,
                                StandardCharsets.UTF_8
                        )
                        .replace(
                                "+",
                                "%20"
                        );

        return WHATSAPP_BASE_URL
                + WHATSAPP_NUMBER
                + "?text="
                + mensajeCodificado;
    }
}
