package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.AsistenteResponse;
import com.abarrote.abarroteapi.service.AsistenteInventarioService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/admin/asistente")
public class AsistenteController {

    private final AsistenteInventarioService
            asistenteInventarioService;

    public AsistenteController(
            AsistenteInventarioService asistenteInventarioService) {

        this.asistenteInventarioService =
                asistenteInventarioService;
    }

    @GetMapping
    public String mostrarAsistente(
            Model model) {

        model.addAttribute(
                "activePage",
                "asistente"
        );

        return "admin/asistente";
    }

    @PostMapping(
            value = "/preguntar",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public AsistenteResponse preguntar(
            @RequestBody
            Map<String, String> request) {

        return asistenteInventarioService
                .responder(
                        request.getOrDefault(
                                "pregunta",
                                ""
                        )
                );
    }
}
