package com.abarrote.abarroteapi.controller;

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
                || !authentication.isAuthenticated()) {

            return "redirect:/login";
        }

        Usuario usuario =
                usuarioService
                        .obtenerEntityPorUsername(
                                authentication.getName()
                        );

        if (usuario.getSucursal() == null) {

            throw new IllegalStateException(
                    "El usuario no tiene una sucursal asignada"
            );
        }

        if (!Boolean.TRUE.equals(
                usuario.getSucursal().getActiva())) {

            throw new IllegalStateException(
                    "La sucursal del usuario está inactiva"
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
                "sucursal",
                usuario.getSucursal().getNombre()
        );

        model.addAttribute(
                "sucursalCodigo",
                usuario.getSucursal().getCodigo()
        );

        return "pos";
    }
}
