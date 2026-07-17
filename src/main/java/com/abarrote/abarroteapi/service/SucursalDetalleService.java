package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.dto.SucursalDetalleResponse;

public interface SucursalDetalleService {

    SucursalDetalleResponse obtenerDetalle(
            Long sucursalId
    );
}
