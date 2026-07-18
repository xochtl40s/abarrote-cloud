package com.abarrote.abarroteapi.multitenant.service;

import com.abarrote.abarroteapi.multitenant.dto.TenantRequest;
import com.abarrote.abarroteapi.multitenant.dto.TenantResponse;

import java.util.List;

public interface TenantService {

    TenantResponse crear(TenantRequest request);

    TenantResponse obtenerPorId(Long id);

    TenantResponse obtenerPorSlug(String slug);

    List<TenantResponse> listar();

    TenantResponse suspender(Long id);

    TenantResponse reactivar(Long id);
}
