package com.abarrote.abarroteapi.saas.service;

import com.abarrote.abarroteapi.saas.dto.TenantProvisioningResult;

public interface TenantProvisioningService {

    /*
     * Operación idempotente:
     *
     * - Si el prospecto es NUEVO, crea tenant y administrador.
     * - Si ya fue ACTIVADO, reutiliza el tenant y regenera
     *   únicamente la contraseña temporal del administrador.
     */
    TenantProvisioningResult activarProspecto(
            String folio,
            Long atendidoPorUsuarioId
    );
}
