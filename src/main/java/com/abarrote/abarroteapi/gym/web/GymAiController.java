package com.abarrote.abarroteapi.gym.web;

import com.abarrote.abarroteapi.gym.dto.GymAiRequest;
import com.abarrote.abarroteapi.gym.dto.GymAiResponse;
import com.abarrote.abarroteapi.gym.service.GymAiService;
import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GymAiController {

    private final GymAiService gymAiService;

    public GymAiController(
        GymAiService gymAiService
    ) {
        this.gymAiService = gymAiService;
    }

    @GetMapping("/gym/asistente")
    public String asistente(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        validarGym(principal);

        model.addAttribute(
            "principal",
            principal
        );

        return "gym/asistente";
    }

    @PostMapping("/gym/api/asistente")
    @ResponseBody
    public ResponseEntity<GymAiResponse>
        responder(
            @AuthenticationPrincipal
            CommerceUserPrincipal principal,

            @Valid
            @RequestBody GymAiRequest request
        ) {

        validarGym(principal);

        return ResponseEntity.ok(
            gymAiService.responder(
                principal.getTenantId(),
                request.pregunta()
            )
        );
    }

    private void validarGym(
        CommerceUserPrincipal principal
    ) {
        if (
            principal == null
                || !principal.esGym()
        ) {
            throw new IllegalArgumentException(
                "Acceso exclusivo de Gym Cloud"
            );
        }
    }
}
