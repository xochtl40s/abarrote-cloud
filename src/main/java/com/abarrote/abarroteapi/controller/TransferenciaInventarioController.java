package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.MovimientoInventarioResponse;
import com.abarrote.abarroteapi.dto.TransferenciaInventarioRequest;
import com.abarrote.abarroteapi.service.TransferenciaInventarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/inventario/transferencias")
public class TransferenciaInventarioController {

    private final TransferenciaInventarioService
            transferenciaInventarioService;

    public TransferenciaInventarioController(
            TransferenciaInventarioService
                    transferenciaInventarioService) {

        this.transferenciaInventarioService =
                transferenciaInventarioService;
    }

    @GetMapping
    public String mostrarPantalla(
            Model model) {

        if (!model.containsAttribute(
                "transferenciaRequest")) {

            model.addAttribute(
                    "transferenciaRequest",
                    new TransferenciaInventarioRequest()
            );
        }

        model.addAttribute(
                "sucursales",
                transferenciaInventarioService
                        .listarSucursalesActivas()
        );

        model.addAttribute(
                "inventarios",
                transferenciaInventarioService
                        .listarInventarioCompleto()
        );

        model.addAttribute(
                "movimientos",
                transferenciaInventarioService
                        .listarMovimientos()
        );

        model.addAttribute(
                "activePage",
                "inventario"
        );

        return "admin/transferencias-inventario";
    }

    @PostMapping
    public String transferir(
            @ModelAttribute
            TransferenciaInventarioRequest
                    transferenciaRequest,

            Authentication authentication,

            RedirectAttributes redirectAttributes) {

        try {

            String usuario =
                    authentication != null
                            ? authentication.getName()
                            : "usuario-desconocido";

            MovimientoInventarioResponse movimiento =
                    transferenciaInventarioService
                            .transferir(
                                    transferenciaRequest,
                                    usuario
                            );

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Transferencia aplicada correctamente. Folio: "
                            + movimiento.getFolio()
            );

        } catch (RuntimeException exception) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    exception.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "transferenciaRequest",
                    transferenciaRequest
            );
        }

        return "redirect:/admin/inventario/transferencias";
    }
}
