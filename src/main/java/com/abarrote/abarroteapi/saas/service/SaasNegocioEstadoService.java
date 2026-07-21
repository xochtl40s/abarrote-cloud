package com.abarrote.abarroteapi.saas.service;

public interface SaasNegocioEstadoService {

    String alternarEstado(
            String folio,
            Long superAdminUsuarioId
    );
}
