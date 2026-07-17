package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.dto.SucursalComparativoResponse;

import java.util.List;

public interface SucursalComparativoService {

    List<SucursalComparativoResponse>
    obtenerComparativoSucursalesActivas();
}
