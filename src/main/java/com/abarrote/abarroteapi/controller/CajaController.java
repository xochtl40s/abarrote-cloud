package com.abarrote.abarroteapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CajaController {

    @GetMapping("/pos/caja")
    public String mostrarCaja(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("cajeroNombre", authentication.getName());
            return "caja";
        }
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String cerrarSesionExplicito(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
