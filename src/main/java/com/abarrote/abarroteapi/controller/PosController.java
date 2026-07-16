package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pos")
public class PosController {

    private final UsuarioService usuarioService;

    public PosController(
            UsuarioService usuarioService) {

        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String puntoDeVenta(
            Model model,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equalsIgnoreCase(
                        authentication.getName()
                )) {

            return "redirect:/login?sesionRequerida";
        }

        Usuario usuario =
                usuarioService
                        .obtenerEntityPorUsername(
                                authentication.getName()
                        );

        if (!Boolean.TRUE.equals(
                usuario.getActivo())) {

            throw new IllegalStateException(
                    "El usuario se encuentra inactivo"
            );
        }

        Sucursal sucursal =
                usuario.getSucursal();

        if (sucursal == null) {

            throw new IllegalStateException(
                    "El usuario no tiene una sucursal asignada"
            );
        }

        if (!Boolean.TRUE.equals(
                sucursal.getActiva())) {

            throw new IllegalStateException(
                    "La sucursal asignada al usuario está inactiva"
            );
        }

        model.addAttribute(
                "cajero",
                usuario.getUsername()
        );

        model.addAttribute(
                "nombreCajero",
                usuario.getNombre()
        );

        model.addAttribute(
                "sucursalId",
                sucursal.getId()
        );

        model.addAttribute(
                "sucursal",
                sucursal.getNombre()
        );

        model.addAttribute(
                "sucursalCodigo",
                sucursal.getCodigo()
        );

        return "pos";
    }
}
