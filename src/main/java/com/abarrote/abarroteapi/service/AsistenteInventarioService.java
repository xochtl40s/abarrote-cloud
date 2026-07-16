package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.dto.AsistenteResponse;

public interface AsistenteInventarioService {

    AsistenteResponse responder(
            String pregunta
    );
}
