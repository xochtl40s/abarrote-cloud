package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.entity.Sucursal;

import java.util.List;

public interface SucursalService {

    List<Sucursal> listarTodas();

    List<Sucursal> listarActivas();

    Sucursal obtenerPorId(Long id);

    Sucursal crear(Sucursal sucursal);

    Sucursal actualizar(
            Long id,
            Sucursal sucursal
    );

    void cerrarSucursal(
            Long id,
            String usuarioResponsable
    );
}
