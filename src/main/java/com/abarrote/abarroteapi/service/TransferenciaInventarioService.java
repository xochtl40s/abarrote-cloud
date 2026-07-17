package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.dto.InventarioSucursalResponse;
import com.abarrote.abarroteapi.dto.MovimientoInventarioResponse;
import com.abarrote.abarroteapi.dto.TransferenciaInventarioRequest;
import com.abarrote.abarroteapi.entity.Sucursal;

import java.util.List;

public interface TransferenciaInventarioService {

    MovimientoInventarioResponse transferir(
            TransferenciaInventarioRequest request,
            String usuarioResponsable
    );

    List<Sucursal> listarSucursalesActivas();

    List<InventarioSucursalResponse>
    listarInventarioCompleto();

    List<MovimientoInventarioResponse>
    listarMovimientos();
}
