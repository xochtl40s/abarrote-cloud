package com.abarrote.abarroteapi.saas.service.impl;

import com.abarrote.abarroteapi.saas.dto.SaasPlanResponse;
import com.abarrote.abarroteapi.saas.entity.SaasPlan;
import com.abarrote.abarroteapi.saas.repository.SaasPlanRepository;
import com.abarrote.abarroteapi.saas.service.SaasPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SaasPlanServiceImpl implements SaasPlanService {

    private final SaasPlanRepository saasPlanRepository;

    public SaasPlanServiceImpl(
        SaasPlanRepository saasPlanRepository
    ) {
        this.saasPlanRepository = saasPlanRepository;
    }

    @Override
    public List<SaasPlanResponse> listarPlanesActivos() {
        return saasPlanRepository
            .findByActivoTrueOrderByOrdenVisualAscNombreAsc()
            .stream()
            .map(SaasPlanResponse::desdeEntidad)
            .toList();
    }

    @Override
    public List<SaasPlanResponse> listarTodos() {
        return saasPlanRepository
            .findAllByOrderByOrdenVisualAscNombreAsc()
            .stream()
            .map(SaasPlanResponse::desdeEntidad)
            .toList();
    }

    @Override
    public SaasPlanResponse obtenerPorCodigo(String codigo) {
        String codigoNormalizado = normalizarCodigo(codigo);

        SaasPlan plan = saasPlanRepository
            .findByCodigoIgnoreCase(codigoNormalizado)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No existe un plan SaaS con código: "
                    + codigoNormalizado
            ));

        return SaasPlanResponse.desdeEntidad(plan);
    }

    @Override
    public long contarPlanesActivos() {
        return saasPlanRepository.countByActivoTrue();
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El código del plan es obligatorio"
            );
        }

        return codigo.trim().toUpperCase();
    }
}
