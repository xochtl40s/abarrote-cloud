package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.SucursalComparativoResponse;
import com.abarrote.abarroteapi.dto.SucursalDetalleResponse;
import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.service.SucursalComparativoService;
import com.abarrote.abarroteapi.service.SucursalDetalleService;
import com.abarrote.abarroteapi.service.SucursalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SucursalComparativoServiceImpl
        implements SucursalComparativoService {

    private final SucursalService sucursalService;

    private final SucursalDetalleService
            sucursalDetalleService;

    public SucursalComparativoServiceImpl(
            SucursalService sucursalService,
            SucursalDetalleService sucursalDetalleService) {

        this.sucursalService = sucursalService;

        this.sucursalDetalleService =
                sucursalDetalleService;
    }

    @Override
    public List<SucursalComparativoResponse>
    obtenerComparativoSucursalesActivas() {

        List<SucursalComparativoResponse> resultado =
                new ArrayList<>();

        List<Sucursal> sucursales =
                sucursalService.listarActivas();

        for (Sucursal sucursal : sucursales) {

            SucursalDetalleResponse detalle =
                    sucursalDetalleService.obtenerDetalle(
                            sucursal.getId()
                    );

            SucursalComparativoResponse item =
                    new SucursalComparativoResponse();

            item.setSucursalId(
                    sucursal.getId()
            );

            item.setCodigo(
                    sucursal.getCodigo()
            );

            item.setNombre(
                    sucursal.getNombre()
            );

            item.setNumeroVentas(
                    valorSeguro(
                            detalle.getNumeroVentas()
                    )
            );

            item.setTotalVentas(
                    valorSeguro(
                            detalle.getTotalVentas()
                    )
            );

            item.setUnidadesInventario(
                    valorSeguro(
                            detalle.getUnidadesTotales()
                    )
            );

            item.setProductosDiferentes(
                    valorSeguro(
                            detalle.getProductosDiferentes()
                    )
            );

            item.setValorInventario(
                    valorSeguro(
                            detalle.getValorInventario()
                    )
            );

            resultado.add(item);
        }

        calcularPorcentajes(resultado);

        resultado.sort(
                Comparator.comparing(
                        SucursalComparativoResponse
                                ::getTotalVentas
                ).reversed()
        );

        return resultado;
    }

    private void calcularPorcentajes(
            List<SucursalComparativoResponse> sucursales) {

        BigDecimal ventaMayor =
                sucursales.stream()
                        .map(
                                SucursalComparativoResponse
                                        ::getTotalVentas
                        )
                        .max(
                                Comparator.naturalOrder()
                        )
                        .orElse(
                                BigDecimal.ZERO
                        );

        BigDecimal inventarioMayor =
                sucursales.stream()
                        .map(
                                SucursalComparativoResponse
                                        ::getValorInventario
                        )
                        .max(
                                Comparator.naturalOrder()
                        )
                        .orElse(
                                BigDecimal.ZERO
                        );

        int unidadesMayor =
                sucursales.stream()
                        .mapToInt(
                                SucursalComparativoResponse
                                        ::getUnidadesInventario
                        )
                        .max()
                        .orElse(0);

        for (SucursalComparativoResponse sucursal
                : sucursales) {

            sucursal.setPorcentajeVentas(
                    calcularPorcentaje(
                            sucursal.getTotalVentas(),
                            ventaMayor
                    )
            );

            sucursal.setPorcentajeInventario(
                    calcularPorcentaje(
                            sucursal.getValorInventario(),
                            inventarioMayor
                    )
            );

            sucursal.setPorcentajeUnidades(
                    calcularPorcentaje(
                            sucursal.getUnidadesInventario(),
                            unidadesMayor
                    )
            );
        }
    }

    private int calcularPorcentaje(
            BigDecimal valor,
            BigDecimal valorMayor) {

        if (valor == null
                || valorMayor == null
                || valorMayor.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            return 0;
        }

        return valor
                .multiply(
                        BigDecimal.valueOf(100)
                )
                .divide(
                        valorMayor,
                        0,
                        RoundingMode.HALF_UP
                )
                .intValue();
    }

    private int calcularPorcentaje(
            Integer valor,
            Integer valorMayor) {

        if (valor == null
                || valorMayor == null
                || valorMayor <= 0) {

            return 0;
        }

        return BigDecimal
                .valueOf(valor)
                .multiply(
                        BigDecimal.valueOf(100)
                )
                .divide(
                        BigDecimal.valueOf(valorMayor),
                        0,
                        RoundingMode.HALF_UP
                )
                .intValue();
    }

    private BigDecimal valorSeguro(
            BigDecimal valor) {

        return valor != null
                ? valor
                : BigDecimal.ZERO;
    }

    private int valorSeguro(
            Integer valor) {

        return valor != null
                ? valor
                : 0;
    }
}
