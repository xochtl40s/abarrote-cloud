package com.abarrote.abarroteapi.saas.service;

import com.abarrote.abarroteapi.saas.dto.SaasPlanResponse;

import java.util.List;

public interface SaasPlanService {

    List<SaasPlanResponse> listarPlanesActivos();

    List<SaasPlanResponse> listarTodos();

    SaasPlanResponse obtenerPorCodigo(String codigo);

    long contarPlanesActivos();
}
