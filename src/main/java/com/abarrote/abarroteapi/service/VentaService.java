package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.dto.VentaRequest;
import com.abarrote.abarroteapi.entity.Venta;

public interface VentaService {

    Venta guardar(Venta venta);

    Venta registrarVenta(VentaRequest request);

}
