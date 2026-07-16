package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.dto.InventarioTendenciaResponse;

public interface InventarioTendenciaService {

    InventarioTendenciaResponse
    obtenerTendenciaUltimosDias(
            int numeroDias
    );
}

