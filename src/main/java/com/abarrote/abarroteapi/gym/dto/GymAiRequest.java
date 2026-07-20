package com.abarrote.abarroteapi.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GymAiRequest(

    @NotBlank
    @Size(max = 500)
    String pregunta
) {
}
