package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.service.ProductoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pos")
public class PosController {

    private final ProductoService productoService;

    public PosController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public String puntoDeVenta(Model model, Authentication authentication) {
        model.addAttribute("cajero", authentication.getName());
        return "pos";
    }
}
