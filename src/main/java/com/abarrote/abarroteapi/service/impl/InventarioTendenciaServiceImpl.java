package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.InventarioTendenciaResponse;
import com.abarrote.abarroteapi.entity.InventarioSucursal;
import com.abarrote.abarroteapi.entity.MovimientoInventario;
import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.repository.InventarioSucursalRepository;
import com.abarrote.abarroteapi.repository.MovimientoInventarioRepository;
import com.abarrote.abarroteapi.repository.SucursalRepository;
import com.abarrote.abarroteapi.service.InventarioTendenciaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class InventarioTendenciaServiceImpl
        implements InventarioTendenciaService {

    private static final int DIAS_MINIMOS = 2;

    private static final int DIAS_MAXIMOS = 30;

    private final SucursalRepository sucursalRepository;

    private final InventarioSucursalRepository
            inventarioSucursalRepository;

    private final MovimientoInventarioRepository
            movimientoInventarioRepository;

    public InventarioTendenciaServiceImpl(
            SucursalRepository sucursalRepository,
            InventarioSucursalRepository inventarioSucursalRepository,
            MovimientoInventarioRepository movimientoInventarioRepository) {

        this.sucursalRepository =
                sucursalRepository;

        this.inventarioSucursalRepository =
                inventarioSucursalRepository;

        this.movimientoInventarioRepository =
                movimientoInventarioRepository;
    }

    @Override
    public InventarioTendenciaResponse
    obtenerTendenciaUltimosDias(
            int numeroDias) {

        int dias =
                normalizarNumeroDias(
                        numeroDias
                );

        LocalDate hoy =
                LocalDate.now();

        LocalDate fechaInicial =
                hoy.minusDays(
                        dias - 1L
                );

        List<Sucursal> sucursales =
                sucursalRepository
                        .findByActivaTrueOrderByNombreAsc();

        Map<Long, Sucursal> sucursalesPorId =
                new LinkedHashMap<>();

        Map<Long, Integer> existenciasActuales =
                new LinkedHashMap<>();

        for (Sucursal sucursal : sucursales) {

            sucursalesPorId.put(
                    sucursal.getId(),
                    sucursal
            );

            existenciasActuales.put(
                    sucursal.getId(),
                    0
            );
        }

        cargarExistenciasActuales(
                existenciasActuales
        );

        LocalDateTime inicioConsulta =
                fechaInicial.atStartOfDay();

        LocalDateTime finConsulta =
                hoy.atTime(
                        LocalTime.MAX
                );

        List<MovimientoInventario> movimientos =
                movimientoInventarioRepository
                        .findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(
                                inicioConsulta,
                                finConsulta
                        );

        movimientos.sort(
                Comparator.comparing(
                        MovimientoInventario
                                ::getFechaMovimiento
                ).reversed()
        );

        Map<Long, List<Integer>> valoresPorSucursal =
                inicializarValores(
                        sucursales
                );

        Map<Long, Integer> estadoReconstruido =
                new LinkedHashMap<>(
                        existenciasActuales
                );

        int indiceMovimiento = 0;

        /*
         * Recorremos desde hoy hacia atrás.
         *
         * Para obtener el cierre de un día anterior,
         * revertimos los movimientos realizados después
         * del final de ese día.
         */
        for (
                int desplazamiento = 0;
                desplazamiento < dias;
                desplazamiento++
        ) {

            LocalDate fecha =
                    hoy.minusDays(
                            desplazamiento
                    );

            LocalDateTime finDelDia =
                    fecha.atTime(
                            LocalTime.MAX
                    );

            while (
                    indiceMovimiento
                    < movimientos.size()
            ) {

                MovimientoInventario movimiento =
                        movimientos.get(
                                indiceMovimiento
                        );

                if (
                        !movimiento
                                .getFechaMovimiento()
                                .isAfter(
                                        finDelDia
                                )
                ) {
                    break;
                }

                revertirMovimiento(
                        estadoReconstruido,
                        movimiento
                );

                indiceMovimiento++;
            }

            for (Sucursal sucursal : sucursales) {

                int valor =
                        valorSeguro(
                                estadoReconstruido.get(
                                        sucursal.getId()
                                )
                        );

                /*
                 * Como estamos recorriendo hacia atrás,
                 * insertamos al principio para que la salida
                 * final quede en orden cronológico.
                 */
                valoresPorSucursal
                        .get(
                                sucursal.getId()
                        )
                        .add(
                                0,
                                Math.max(
                                        valor,
                                        0
                                )
                        );
            }
        }

        return construirRespuesta(
                fechaInicial,
                dias,
                sucursales,
                valoresPorSucursal
        );
    }

    private void cargarExistenciasActuales(
            Map<Long, Integer> existenciasActuales) {

        List<InventarioSucursal> inventarios =
                inventarioSucursalRepository
                        .findAll();

        for (InventarioSucursal inventario
                : inventarios) {

            if (
                    inventario.getSucursal() == null
                    || inventario
                    .getSucursal()
                    .getId() == null
            ) {
                continue;
            }

            Long sucursalId =
                    inventario
                            .getSucursal()
                            .getId();

            if (
                    !existenciasActuales
                            .containsKey(
                                    sucursalId
                            )
            ) {
                continue;
            }

            int existencia =
                    valorSeguro(
                            inventario.getExistencia()
                    );

            existenciasActuales.compute(
                    sucursalId,
                    (id, acumulado) ->
                            valorSeguro(acumulado)
                                    + existencia
            );
        }
    }

    private Map<Long, List<Integer>>
    inicializarValores(
            List<Sucursal> sucursales) {

        Map<Long, List<Integer>> resultado =
                new LinkedHashMap<>();

        for (Sucursal sucursal : sucursales) {

            resultado.put(
                    sucursal.getId(),
                    new ArrayList<>()
            );
        }

        return resultado;
    }

    private void revertirMovimiento(
            Map<Long, Integer> estado,
            MovimientoInventario movimiento) {

        if (
                movimiento.getSucursalOrigen() != null
                && movimiento
                .getSucursalOrigen()
                .getId() != null
        ) {

            Long sucursalOrigenId =
                    movimiento
                            .getSucursalOrigen()
                            .getId();

            if (
                    estado.containsKey(
                            sucursalOrigenId
                    )
            ) {

                int variacionOrigen =
                        valorSeguro(
                                movimiento
                                        .getExistenciaOrigenNueva()
                        )
                        - valorSeguro(
                                movimiento
                                        .getExistenciaOrigenAnterior()
                        );

                estado.compute(
                        sucursalOrigenId,
                        (id, actual) ->
                                valorSeguro(actual)
                                        - variacionOrigen
                );
            }
        }

        if (
                movimiento.getSucursalDestino() != null
                && movimiento
                .getSucursalDestino()
                .getId() != null
        ) {

            Long sucursalDestinoId =
                    movimiento
                            .getSucursalDestino()
                            .getId();

            if (
                    estado.containsKey(
                            sucursalDestinoId
                    )
            ) {

                int variacionDestino =
                        valorSeguro(
                                movimiento
                                        .getExistenciaDestinoNueva()
                        )
                        - valorSeguro(
                                movimiento
                                        .getExistenciaDestinoAnterior()
                        );

                estado.compute(
                        sucursalDestinoId,
                        (id, actual) ->
                                valorSeguro(actual)
                                        - variacionDestino
                );
            }
        }
    }

    private InventarioTendenciaResponse
    construirRespuesta(
            LocalDate fechaInicial,
            int dias,
            List<Sucursal> sucursales,
            Map<Long, List<Integer>> valoresPorSucursal) {

        InventarioTendenciaResponse respuesta =
                new InventarioTendenciaResponse();

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern(
                        "dd MMM",
                        new Locale(
                                "es",
                                "MX"
                        )
                );

        List<String> etiquetas =
                new ArrayList<>();

        for (int indice = 0; indice < dias; indice++) {

            etiquetas.add(
                    fechaInicial
                            .plusDays(indice)
                            .format(formato)
                            .replace(".", "")
            );
        }

        respuesta.setEtiquetas(
                etiquetas
        );

        List<InventarioTendenciaResponse.SerieSucursal>
                series =
                new ArrayList<>();

        for (Sucursal sucursal : sucursales) {

            InventarioTendenciaResponse.SerieSucursal serie =
                    new InventarioTendenciaResponse
                            .SerieSucursal();

            serie.setSucursalId(
                    sucursal.getId()
            );

            serie.setCodigo(
                    sucursal.getCodigo()
            );

            serie.setNombre(
                    sucursal.getNombre()
            );

            serie.setValores(
                    valoresPorSucursal.getOrDefault(
                            sucursal.getId(),
                            List.of()
                    )
            );

            series.add(
                    serie
            );
        }

        respuesta.setSeries(
                series
        );

        return respuesta;
    }

    private int normalizarNumeroDias(
            int numeroDias) {

        if (numeroDias < DIAS_MINIMOS) {
            return DIAS_MINIMOS;
        }

        return Math.min(
                numeroDias,
                DIAS_MAXIMOS
        );
    }

    private int valorSeguro(
            Integer valor) {

        return valor != null
                ? valor
                : 0;
    }
}
