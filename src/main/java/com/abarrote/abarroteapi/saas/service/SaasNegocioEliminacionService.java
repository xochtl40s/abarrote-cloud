package com.abarrote.abarroteapi.saas.service;

public interface SaasNegocioEliminacionService {

    void eliminarNegocio(
            String folio,
            Long superAdminUsuarioId
    );
}
