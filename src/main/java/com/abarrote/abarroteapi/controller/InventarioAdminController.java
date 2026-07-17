package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.InventarioSucursalResponse;
import com.abarrote.abarroteapi.dto.InventarioTendenciaResponse;
import com.abarrote.abarroteapi.dto.SucursalComparativoResponse;
import com.abarrote.abarroteapi.service.InventarioTendenciaService;
import com.abarrote.abarroteapi.service.SucursalComparativoService;
import com.abarrote.abarroteapi.service.TransferenciaInventarioService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/inventario")
public class InventarioAdminController {

    private static final int DIAS_TENDENCIA = 7;

    private final TransferenciaInventarioService
            transferenciaInventarioService;

    private final SucursalComparativoService
            sucursalComparativoService;

    private final InventarioTendenciaService
            inventarioTendenciaService;

    private final ObjectMapper objectMapper;

    public InventarioAdminController(
            TransferenciaInventarioService
                    transferenciaInventarioService,
            SucursalComparativoService
                    sucursalComparativoService,
            InventarioTendenciaService
                    inventarioTendenciaService,
            ObjectMapper objectMapper) {

        this.transferenciaInventarioService =
                transferenciaInventarioService;

        this.sucursalComparativoService =
                sucursalComparativoService;

        this.inventarioTendenciaService =
                inventarioTendenciaService;

        this.objectMapper =
                objectMapper;
    }

    @GetMapping
    public String mostrarInventario(
            Model model) {

        List<InventarioSucursalResponse> inventarios =
                transferenciaInventarioService
                        .listarInventarioCompleto();

        List<SucursalComparativoResponse>
                comparativoSucursales =
                sucursalComparativoService
                        .obtenerComparativoSucursalesActivas();

        InventarioTendenciaResponse tendencia =
                inventarioTendenciaService
                        .obtenerTendenciaUltimosDias(
                                DIAS_TENDENCIA
                        );

        int productosRegistrados =
                inventarios.size();

        int unidadesTotales =
                inventarios.stream()
                        .mapToInt(
                                inventario ->
                                        valorSeguro(
                                                inventario
                                                        .getExistencia()
                                        )
                        )
                        .sum();

        long productosStockBajo =
                inventarios.stream()
                        .filter(
                                inventario -> {

                                    int existencia =
                                            valorSeguro(
                                                    inventario
                                                            .getExistencia()
                                            );

                                    int stockMinimo =
                                            valorSeguro(
                                                    inventario
                                                            .getStockMinimo()
                                            );

                                    return existencia > 0
                                            && existencia
                                            <= stockMinimo;
                                }
                        )
                        .count();

        long productosAgotados =
                inventarios.stream()
                        .filter(
                                inventario ->
                                        valorSeguro(
                                                inventario
                                                        .getExistencia()
                                        ) <= 0
                        )
                        .count();

        BigDecimal valorTotalInventario =
                comparativoSucursales.stream()
                        .map(
                                SucursalComparativoResponse
                                        ::getValorInventario
                        )
                        .filter(
                                valor ->
                                        valor != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        model.addAttribute(
                "inventarios",
                inventarios
        );

        model.addAttribute(
                "sucursales",
                transferenciaInventarioService
                        .listarSucursalesActivas()
        );

        model.addAttribute(
                "comparativoSucursales",
                comparativoSucursales
        );

        model.addAttribute(
                "productosRegistrados",
                productosRegistrados
        );

        model.addAttribute(
                "unidadesTotales",
                unidadesTotales
        );

        model.addAttribute(
                "productosStockBajo",
                productosStockBajo
        );

        model.addAttribute(
                "productosAgotados",
                productosAgotados
        );

        model.addAttribute(
                "valorTotalInventario",
                valorTotalInventario
        );

        model.addAttribute(
                "totalSucursales",
                comparativoSucursales.size()
        );

        model.addAttribute(
                "inventarioTrendJson",
                convertirAJson(
                        tendencia
                )
        );

        model.addAttribute(
                "activePage",
                "inventario"
        );

        return "admin/inventario";
    }

    private String convertirAJson(
            InventarioTendenciaResponse tendencia) {

        try {

            return objectMapper
                    .writeValueAsString(
                            tendencia
                    );

        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "No fue posible construir la gráfica "
                            + "de inventario por sucursal",
                    exception
            );
        }
    }

    private int valorSeguro(
            Integer valor) {

        return valor != null
                ? valor
                : 0;
    }
}
