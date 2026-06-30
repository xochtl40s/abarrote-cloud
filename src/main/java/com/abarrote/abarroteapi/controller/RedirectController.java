package com.abarrote.abarroteapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RedirectController {

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login"; // Buscará el archivo login.html
    }
}
