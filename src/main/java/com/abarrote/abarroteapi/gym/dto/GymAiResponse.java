package com.abarrote.abarroteapi.gym.dto;

import java.util.List;

public record GymAiResponse(
    String respuesta,
    String categoria,
    List<String> detalles
) {
}
