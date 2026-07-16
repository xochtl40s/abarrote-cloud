package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.dto.CorteCajaResponse;

import java.time.LocalDate;

public interface CorteCajaService {

    CorteCajaResponse generarCorte(
            LocalDate fecha
    );
}
