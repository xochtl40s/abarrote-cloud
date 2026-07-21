package com.abarrote.abarroteapi.saas.service;

import com.abarrote.abarroteapi.saas.dto.SaasProspectoRequest;
import com.abarrote.abarroteapi.saas.dto.SaasProspectoResponse;

import java.util.List;

public interface SaasProspectoService {

    SaasProspectoResponse registrar(
        SaasProspectoRequest request
    );

    List<SaasProspectoResponse> listarTodos();

    SaasProspectoResponse obtenerPorFolio(String folio);

    long contarNuevos();
}
