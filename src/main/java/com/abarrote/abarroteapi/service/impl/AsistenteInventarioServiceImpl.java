package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.AsistenteResponse;
import com.abarrote.abarroteapi.entity.InventarioSucursal;
import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.repository.InventarioSucursalRepository;
import com.abarrote.abarroteapi.service.AsistenteInventarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AsistenteInventarioServiceImpl
        implements AsistenteInventarioService {

    private static final int FACTOR_OBJETIVO_SURTIDO = 2;

    private final InventarioSucursalRepository
            inventarioSucursalRepository;

    public AsistenteInventarioServiceImpl(
            InventarioSucursalRepository
                    inventarioSucursalRepository) {

        this.inventarioSucursalRepository =
                inventarioSucursalRepository;
    }

    @Override
    public AsistenteResponse responder(
            String pregunta) {

        String preguntaSegura =
                limpiarPregunta(
                        pregunta
                );

        if (preguntaSegura.isBlank()) {

            return respuesta(
                    preguntaSegura,
                    construirAyuda()
            );
        }

        String textoNormalizado =
                normalizar(
                        preguntaSegura
                );

        List<InventarioSucursal> inventarios =
                obtenerInventariosActivos();

        Optional<Sucursal> sucursalDetectada =
                detectarSucursal(
                        textoNormalizado,
                        inventarios
                );

        Intencion intencion =
                detectarIntencion(
                        textoNormalizado,
                        sucursalDetectada
                );

        String contenido =
                switch (intencion) {

                    case INVENTARIO_SUCURSAL ->
                            responderInventarioSucursal(
                                    sucursalDetectada,
                                    inventarios
                            );

                    case INVENTARIO_TOTAL ->
                            responderInventarioTotal(
                                    inventarios
                            );

                    case SUCURSAL_MENOR_INVENTARIO ->
                            responderSucursalMenorInventario(
                                    inventarios
                            );

                    case SUCURSAL_MAYOR_INVENTARIO ->
                            responderSucursalMayorInventario(
                                    inventarios
                            );

                    case PRODUCTOS_POR_SURTIR ->
                            responderProductosPorSurtir(
                                    sucursalDetectada,
                                    inventarios
                            );

                    case PRODUCTOS_AGOTADOS ->
                            responderProductosAgotados(
                                    sucursalDetectada,
                                    inventarios
                            );

                    case STOCK_BAJO ->
                            responderStockBajo(
                                    sucursalDetectada,
                                    inventarios
                            );

                    case PRODUCTOS_CON_EXCEDENTE ->
                            responderProductosConExcedente(
                                    sucursalDetectada,
                                    inventarios
                            );

                    case TRANSFERENCIAS ->
                            responderTransferencias(
                                    sucursalDetectada,
                                    inventarios
                            );

                    case RESUMEN_NEGOCIO ->
                            responderResumenGeneral(
                                    inventarios
                            );

                    case LISTAR_SUCURSALES ->
                            responderSucursales(
                                    inventarios
                            );

                    case AYUDA ->
                            construirAyuda();

                    case DESCONOCIDA ->
                            responderConsultaDesconocida(
                                    preguntaSegura,
                                    sucursalDetectada
                            );
                };

        return respuesta(
                preguntaSegura,
                contenido
        );
    }

    // ============================================================
    // DETECCIÓN DE INTENCIÓN
    // ============================================================

    private Intencion detectarIntencion(
            String texto,
            Optional<Sucursal> sucursalDetectada) {

        if (
                contieneAlguna(
                        texto,
                        "ayuda",
                        "que puedes hacer",
                        "que sabes hacer",
                        "como te uso",
                        "como funciona"
                )
        ) {

            return Intencion.AYUDA;
        }

        if (
                contieneAlguna(
                        texto,
                        "resumen",
                        "estado del negocio",
                        "estado del inventario",
                        "como esta mi negocio",
                        "analiza el inventario",
                        "analisis general"
                )
        ) {

            return Intencion.RESUMEN_NEGOCIO;
        }

        if (
                contieneAlguna(
                        texto,
                        "transferir",
                        "transferencia",
                        "traspasar",
                        "mover mercancia",
                        "mover inventario",
                        "abastecer a otra",
                        "enviar a otra sucursal"
                )
        ) {

            return Intencion.TRANSFERENCIAS;
        }

        if (
                contieneAlguna(
                        texto,
                        "agotados",
                        "agotado",
                        "sin existencia",
                        "sin mercancia",
                        "en cero"
                )
        ) {

            return Intencion.PRODUCTOS_AGOTADOS;
        }

        if (
                contieneAlguna(
                        texto,
                        "stock bajo",
                        "poco inventario",
                        "poca existencia",
                        "por agotarse",
                        "bajo minimo",
                        "debajo del minimo"
                )
        ) {

            return Intencion.STOCK_BAJO;
        }

        if (
                contieneAlguna(
                        texto,
                        "surtir",
                        "resurtir",
                        "reabastecer",
                        "comprar",
                        "faltan",
                        "debo pedir",
                        "debo surtir"
                )
        ) {

            return Intencion.PRODUCTOS_POR_SURTIR;
        }

        if (
                contieneAlguna(
                        texto,
                        "excedente",
                        "sobrante",
                        "sobran",
                        "sobreinventario",
                        "demasiada mercancia"
                )
        ) {

            return Intencion.PRODUCTOS_CON_EXCEDENTE;
        }

        if (
                contieneAlguna(
                        texto,
                        "menos mercancia",
                        "menos inventario",
                        "menor inventario",
                        "tienda esta peor",
                        "sucursal esta peor",
                        "necesita mas mercancia"
                )
        ) {

            return Intencion.SUCURSAL_MENOR_INVENTARIO;
        }

        if (
                contieneAlguna(
                        texto,
                        "mas mercancia",
                        "mas inventario",
                        "mayor inventario",
                        "mejor surtida",
                        "mas surtida"
                )
        ) {

            return Intencion.SUCURSAL_MAYOR_INVENTARIO;
        }

        if (
                contieneAlguna(
                        texto,
                        "cuantas unidades",
                        "cuanta mercancia",
                        "cuanto inventario",
                        "existencia total",
                        "inventario total"
                )
        ) {

            if (sucursalDetectada.isPresent()) {

                return Intencion.INVENTARIO_SUCURSAL;
            }

            return Intencion.INVENTARIO_TOTAL;
        }

        if (
                contieneAlguna(
                        texto,
                        "sucursales",
                        "tiendas",
                        "que tiendas",
                        "lista de sucursales"
                )
        ) {

            return Intencion.LISTAR_SUCURSALES;
        }

        if (
                sucursalDetectada.isPresent()
                && contieneAlguna(
                        texto,
                        "inventario",
                        "mercancia",
                        "unidades",
                        "existencia"
                )
        ) {

            return Intencion.INVENTARIO_SUCURSAL;
        }

        return Intencion.DESCONOCIDA;
    }

    // ============================================================
    // INVENTARIO POR SUCURSAL
    // ============================================================

    private String responderInventarioSucursal(
            Optional<Sucursal> sucursalDetectada,
            List<InventarioSucursal> inventarios) {

        if (sucursalDetectada.isEmpty()) {

            return """
                    No pude identificar la sucursal.

                    Escribe el nombre o código. Por ejemplo:

                    ¿Cuántas unidades hay en la sucursal Matriz?
                    """;
        }

        Sucursal sucursal =
                sucursalDetectada.get();

        List<InventarioSucursal> inventarioSucursal =
                filtrarPorSucursal(
                        inventarios,
                        sucursal
                );

        int unidades =
                sumarExistencias(
                        inventarioSucursal
                );

        long productosDiferentes =
                inventarioSucursal.stream()
                        .filter(
                                inventario ->
                                        inventario.getProducto() != null
                        )
                        .map(
                                inventario ->
                                        inventario
                                                .getProducto()
                                                .getId()
                        )
                        .distinct()
                        .count();

        long agotados =
                inventarioSucursal.stream()
                        .filter(
                                inventario ->
                                        existencia(inventario) <= 0
                        )
                        .count();

        long stockBajo =
                inventarioSucursal.stream()
                        .filter(
                                this::estaEnStockBajo
                        )
                        .count();

        return """
                🏪 %s - %s

                Unidades disponibles: %d
                Productos diferentes: %d
                Productos con stock bajo: %d
                Productos agotados: %d
                """.formatted(
                        textoSeguro(
                                sucursal.getCodigo(),
                                "SIN-CODIGO"
                        ),
                        textoSeguro(
                                sucursal.getNombre(),
                                "Sucursal"
                        ),
                        unidades,
                        productosDiferentes,
                        stockBajo,
                        agotados
                ).trim();
    }

    // ============================================================
    // INVENTARIO TOTAL
    // ============================================================

    private String responderInventarioTotal(
            List<InventarioSucursal> inventarios) {

        int totalUnidades =
                sumarExistencias(
                        inventarios
                );

        Map<Long, List<InventarioSucursal>>
                inventarioPorSucursal =
                agruparPorSucursalId(
                        inventarios
                );

        StringBuilder respuesta =
                new StringBuilder();

        respuesta.append(
                "📦 Inventario total del negocio\n\n"
        );

        respuesta.append(
                "Unidades disponibles: "
        ).append(
                totalUnidades
        ).append(
                "\n"
        );

        respuesta.append(
                "Sucursales activas con inventario: "
        ).append(
                inventarioPorSucursal.size()
        ).append(
                "\n\n"
        );

        inventarioPorSucursal.values()
                .stream()
                .sorted(
                        Comparator.comparing(
                                lista ->
                                        lista
                                                .get(0)
                                                .getSucursal()
                                                .getNombre()
                        )
                )
                .forEach(
                        lista -> {

                            Sucursal sucursal =
                                    lista
                                            .get(0)
                                            .getSucursal();

                            respuesta.append(
                                    "• "
                            ).append(
                                    sucursal.getCodigo()
                            ).append(
                                    " - "
                            ).append(
                                    sucursal.getNombre()
                            ).append(
                                    ": "
                            ).append(
                                    sumarExistencias(
                                            lista
                                    )
                            ).append(
                                    " unidades\n"
                            );
                        }
                );

        return respuesta.toString().trim();
    }

    // ============================================================
    // SUCURSAL MENOR / MAYOR
    // ============================================================

    private String responderSucursalMenorInventario(
            List<InventarioSucursal> inventarios) {

        return obtenerSucursalExtrema(
                inventarios,
                false
        );
    }

    private String responderSucursalMayorInventario(
            List<InventarioSucursal> inventarios) {

        return obtenerSucursalExtrema(
                inventarios,
                true
        );
    }

    private String obtenerSucursalExtrema(
            List<InventarioSucursal> inventarios,
            boolean buscarMayor) {

        Map<Sucursal, Integer> totales =
                construirTotalesPorSucursal(
                        inventarios
                );

        if (totales.isEmpty()) {

            return "No existen datos de inventario "
                    + "para sucursales activas.";
        }

        Comparator<Map.Entry<Sucursal, Integer>>
                comparador =
                Map.Entry.comparingByValue();

        Optional<Map.Entry<Sucursal, Integer>>
                resultado =
                buscarMayor
                        ? totales.entrySet()
                        .stream()
                        .max(comparador)
                        : totales.entrySet()
                        .stream()
                        .min(comparador);

        if (resultado.isEmpty()) {

            return "No fue posible calcular "
                    + "el inventario por sucursal.";
        }

        Sucursal sucursal =
                resultado.get().getKey();

        int unidades =
                resultado.get().getValue();

        String tipo =
                buscarMayor
                        ? "mayor"
                        : "menor";

        return """
                La sucursal con %s inventario es:

                🏪 %s - %s
                Unidades disponibles: %d
                """.formatted(
                        tipo,
                        sucursal.getCodigo(),
                        sucursal.getNombre(),
                        unidades
                ).trim();
    }

    // ============================================================
    // SURTIDO
    // ============================================================

    private String responderProductosPorSurtir(
            Optional<Sucursal> sucursalDetectada,
            List<InventarioSucursal> inventarios) {

        List<InventarioSucursal> candidatos =
                aplicarFiltroSucursal(
                        inventarios,
                        sucursalDetectada
                )
                .stream()
                .filter(
                        inventario ->
                                existencia(inventario)
                                <= stockMinimo(inventario)
                )
                .sorted(
                        comparadorSucursalProducto()
                )
                .toList();

        if (candidatos.isEmpty()) {

            if (sucursalDetectada.isPresent()) {

                return """
                        No encontré productos por surtir en:

                        🏪 %s - %s

                        Todos los productos se encuentran por encima
                        de su stock mínimo.
                        """.formatted(
                                sucursalDetectada
                                        .get()
                                        .getCodigo(),
                                sucursalDetectada
                                        .get()
                                        .getNombre()
                ).trim();
            }

            return """
                    No encontré productos por surtir.

                    Todos los inventarios de las sucursales activas
                    se encuentran por encima de su stock mínimo.
                    """;
        }

        Map<Sucursal, List<InventarioSucursal>>
                porSucursal =
                candidatos.stream()
                        .collect(
                                Collectors.groupingBy(
                                        InventarioSucursal::getSucursal,
                                        LinkedHashMap::new,
                                        Collectors.toList()
                                )
                        );

        StringBuilder respuesta =
                new StringBuilder(
                        "📦 Recomendación de surtido\n\n"
                );

        int totalSugerido = 0;

        for (
                Map.Entry<Sucursal, List<InventarioSucursal>>
                        entrada : porSucursal.entrySet()
        ) {

            Sucursal sucursal =
                    entrada.getKey();

            respuesta.append(
                    "🏪 "
            ).append(
                    sucursal.getCodigo()
            ).append(
                    " - "
            ).append(
                    sucursal.getNombre()
            ).append(
                    "\n"
            );

            for (
                    InventarioSucursal inventario :
                    entrada.getValue()
            ) {

                int existencia =
                        existencia(inventario);

                int minimo =
                        stockMinimo(inventario);

                int objetivo =
                        Math.max(
                                minimo
                                * FACTOR_OBJETIVO_SURTIDO,
                                minimo + 1
                        );

                int surtir =
                        Math.max(
                                objetivo - existencia,
                                1
                        );

                totalSugerido += surtir;

                respuesta.append(
                        "• "
                ).append(
                        inventario
                                .getProducto()
                                .getNombre()
                ).append(
                        ": actual "
                ).append(
                        existencia
                ).append(
                        ", mínimo "
                ).append(
                        minimo
                ).append(
                        ", surtir "
                ).append(
                        surtir
                ).append(
                        "\n"
                );
            }

            respuesta.append("\n");
        }

        respuesta.append(
                "Total sugerido para surtir: "
        ).append(
                totalSugerido
        ).append(
                " unidades."
        );

        return respuesta.toString().trim();
    }

    // ============================================================
    // AGOTADOS
    // ============================================================

    private String responderProductosAgotados(
            Optional<Sucursal> sucursalDetectada,
            List<InventarioSucursal> inventarios) {

        List<InventarioSucursal> agotados =
                aplicarFiltroSucursal(
                        inventarios,
                        sucursalDetectada
                )
                .stream()
                .filter(
                        inventario ->
                                existencia(inventario) <= 0
                )
                .sorted(
                        comparadorSucursalProducto()
                )
                .toList();

        if (agotados.isEmpty()) {

            if (sucursalDetectada.isPresent()) {

                return "No existen productos agotados en "
                        + sucursalDetectada
                        .get()
                        .getNombre()
                        + ".";
            }

            return "No existen productos agotados "
                    + "en las sucursales activas.";
        }

        StringBuilder respuesta =
                new StringBuilder(
                        "🚫 Productos agotados\n\n"
                );

        for (InventarioSucursal inventario
                : agotados) {

            respuesta.append(
                    "• "
            ).append(
                    inventario
                            .getSucursal()
                            .getCodigo()
            ).append(
                    " - "
            ).append(
                    inventario
                            .getSucursal()
                            .getNombre()
            ).append(
                    ": "
            ).append(
                    inventario
                            .getProducto()
                            .getNombre()
            ).append(
                    "\n"
            );
        }

        return respuesta.toString().trim();
    }

    // ============================================================
    // STOCK BAJO
    // ============================================================

    private String responderStockBajo(
            Optional<Sucursal> sucursalDetectada,
            List<InventarioSucursal> inventarios) {

        List<InventarioSucursal> stockBajo =
                aplicarFiltroSucursal(
                        inventarios,
                        sucursalDetectada
                )
                .stream()
                .filter(
                        this::estaEnStockBajo
                )
                .sorted(
                        comparadorSucursalProducto()
                )
                .toList();

        if (stockBajo.isEmpty()) {

            return "No existen productos con stock bajo "
                    + construirSufijoSucursal(
                    sucursalDetectada
            )
                    + ".";
        }

        StringBuilder respuesta =
                new StringBuilder(
                        "⚠️ Productos con stock bajo\n\n"
                );

        for (InventarioSucursal inventario
                : stockBajo) {

            respuesta.append(
                    "• "
            ).append(
                    inventario
                            .getSucursal()
                            .getCodigo()
            ).append(
                    " - "
            ).append(
                    inventario
                            .getProducto()
                            .getNombre()
            ).append(
                    ": "
            ).append(
                    existencia(inventario)
            ).append(
                    " unidades; mínimo "
            ).append(
                    stockMinimo(inventario)
            ).append(
                    "\n"
            );
        }

        return respuesta.toString().trim();
    }

    // ============================================================
    // EXCEDENTES
    // ============================================================

    private String responderProductosConExcedente(
            Optional<Sucursal> sucursalDetectada,
            List<InventarioSucursal> inventarios) {

        List<InventarioSucursal> excedentes =
                aplicarFiltroSucursal(
                        inventarios,
                        sucursalDetectada
                )
                .stream()
                .filter(
                        inventario ->
                                stockMinimo(inventario) > 0
                                && existencia(inventario)
                                >= stockMinimo(inventario) * 3
                )
                .sorted(
                        Comparator
                                .comparingInt(
                                        this::calcularExcedente
                                )
                                .reversed()
                )
                .toList();

        if (excedentes.isEmpty()) {

            return "No encontré productos con excedente "
                    + construirSufijoSucursal(
                    sucursalDetectada
            )
                    + ".";
        }

        StringBuilder respuesta =
                new StringBuilder(
                        "📈 Productos con excedente\n\n"
                );

        for (InventarioSucursal inventario
                : excedentes) {

            respuesta.append(
                    "• "
            ).append(
                    inventario
                            .getSucursal()
                            .getCodigo()
            ).append(
                    " - "
            ).append(
                    inventario
                            .getProducto()
                            .getNombre()
            ).append(
                    ": existencia "
            ).append(
                    existencia(inventario)
            ).append(
                    ", excedente estimado "
            ).append(
                    calcularExcedente(inventario)
            ).append(
                    "\n"
            );
        }

        return respuesta.toString().trim();
    }

    // ============================================================
    // TRANSFERENCIAS
    // ============================================================

    private String responderTransferencias(
            Optional<Sucursal> sucursalDestinoDetectada,
            List<InventarioSucursal> inventarios) {

        List<InventarioSucursal> faltantes =
                aplicarFiltroSucursal(
                        inventarios,
                        sucursalDestinoDetectada
                )
                .stream()
                .filter(
                        inventario ->
                                existencia(inventario)
                                <= stockMinimo(inventario)
                )
                .toList();

        List<SugerenciaTransferencia> sugerencias =
                new ArrayList<>();

        for (InventarioSucursal destino
                : faltantes) {

            Optional<InventarioSucursal> origen =
                    inventarios.stream()
                            .filter(
                                    candidato ->
                                            mismoProducto(
                                                    candidato,
                                                    destino
                                            )
                            )
                            .filter(
                                    candidato ->
                                            !mismaSucursal(
                                                    candidato,
                                                    destino
                                            )
                            )
                            .filter(
                                    candidato ->
                                            calcularExcedente(
                                                    candidato
                                            ) > 0
                            )
                            .max(
                                    Comparator.comparingInt(
                                            this::calcularExcedente
                                    )
                            );

            if (origen.isEmpty()) {
                continue;
            }

            int requerido =
                    Math.max(
                            stockMinimo(destino) * 2
                            - existencia(destino),
                            1
                    );

            int disponible =
                    calcularExcedente(
                            origen.get()
                    );

            int cantidad =
                    Math.min(
                            requerido,
                            disponible
                    );

            if (cantidad <= 0) {
                continue;
            }

            sugerencias.add(
                    new SugerenciaTransferencia(
                            origen.get(),
                            destino,
                            cantidad
                    )
            );
        }

        if (sugerencias.isEmpty()) {

            return """
                    No encontré excedentes suficientes para
                    recomendar transferencias internas.

                    Los productos faltantes deben considerarse
                    para compra o surtido externo.
                    """;
        }

        StringBuilder respuesta =
                new StringBuilder(
                        "🚚 Transferencias sugeridas\n\n"
                );

        for (SugerenciaTransferencia sugerencia
                : sugerencias) {

            respuesta.append(
                    "• Mover "
            ).append(
                    sugerencia.cantidad()
            ).append(
                    " unidades de "
            ).append(
                    sugerencia
                            .destino()
                            .getProducto()
                            .getNombre()
            ).append(
                    "\n  Desde: "
            ).append(
                    sugerencia
                            .origen()
                            .getSucursal()
                            .getCodigo()
            ).append(
                    " - "
            ).append(
                    sugerencia
                            .origen()
                            .getSucursal()
                            .getNombre()
            ).append(
                    "\n  Hacia: "
            ).append(
                    sugerencia
                            .destino()
                            .getSucursal()
                            .getCodigo()
            ).append(
                    " - "
            ).append(
                    sugerencia
                            .destino()
                            .getSucursal()
                            .getNombre()
            ).append(
                    "\n\n"
            );
        }

        respuesta.append(
                "Estas recomendaciones no ejecutan "
                        + "movimientos automáticamente."
        );

        return respuesta.toString().trim();
    }

    // ============================================================
    // RESUMEN GENERAL
    // ============================================================

    private String responderResumenGeneral(
            List<InventarioSucursal> inventarios) {

        int unidades =
                sumarExistencias(
                        inventarios
                );

        long agotados =
                inventarios.stream()
                        .filter(
                                inventario ->
                                        existencia(inventario) <= 0
                        )
                        .count();

        long stockBajo =
                inventarios.stream()
                        .filter(
                                this::estaEnStockBajo
                        )
                        .count();

        long saludables =
                inventarios.stream()
                        .filter(
                                inventario ->
                                        existencia(inventario)
                                        > stockMinimo(inventario)
                        )
                        .count();

        Map<Sucursal, Integer> totales =
                construirTotalesPorSucursal(
                        inventarios
                );

        Optional<Map.Entry<Sucursal, Integer>>
                menor =
                totales.entrySet()
                        .stream()
                        .min(
                                Map.Entry.comparingByValue()
                        );

        StringBuilder respuesta =
                new StringBuilder();

        respuesta.append(
                "📊 Resumen operativo del inventario\n\n"
        );

        respuesta.append(
                "Sucursales activas: "
        ).append(
                totales.size()
        ).append(
                "\n"
        );

        respuesta.append(
                "Unidades disponibles: "
        ).append(
                unidades
        ).append(
                "\n"
        );

        respuesta.append(
                "Registros saludables: "
        ).append(
                saludables
        ).append(
                "\n"
        );

        respuesta.append(
                "Registros con stock bajo: "
        ).append(
                stockBajo
        ).append(
                "\n"
        );

        respuesta.append(
                "Registros agotados: "
        ).append(
                agotados
        ).append(
                "\n"
        );

        menor.ifPresent(
                entrada -> {

                    respuesta.append(
                            "\nAtención prioritaria: "
                    ).append(
                            entrada
                                    .getKey()
                                    .getCodigo()
                    ).append(
                            " - "
                    ).append(
                            entrada
                                    .getKey()
                                    .getNombre()
                    ).append(
                            ", con "
                    ).append(
                            entrada.getValue()
                    ).append(
                            " unidades."
                    );
                }
        );

        return respuesta.toString().trim();
    }

    // ============================================================
    // SUCURSALES
    // ============================================================

    private String responderSucursales(
            List<InventarioSucursal> inventarios) {

        Map<Sucursal, Integer> totales =
                construirTotalesPorSucursal(
                        inventarios
                );

        if (totales.isEmpty()) {

            return "No existen sucursales activas "
                    + "con inventario registrado.";
        }

        StringBuilder respuesta =
                new StringBuilder(
                        "🏪 Sucursales activas\n\n"
                );

        totales.entrySet()
                .stream()
                .sorted(
                        Comparator.comparing(
                                entrada ->
                                        entrada
                                                .getKey()
                                                .getNombre()
                        )
                )
                .forEach(
                        entrada -> {

                            respuesta.append(
                                    "• "
                            ).append(
                                    entrada
                                            .getKey()
                                            .getCodigo()
                            ).append(
                                    " - "
                            ).append(
                                    entrada
                                            .getKey()
                                            .getNombre()
                            ).append(
                                    ": "
                            ).append(
                                    entrada.getValue()
                            ).append(
                                    " unidades\n"
                            );
                        }
                );

        return respuesta.toString().trim();
    }

    // ============================================================
    // RESPUESTA DESCONOCIDA
    // ============================================================

    private String responderConsultaDesconocida(
            String pregunta,
            Optional<Sucursal> sucursalDetectada) {

        StringBuilder respuesta =
                new StringBuilder();

        respuesta.append(
                "No pude relacionar la pregunta con una "
                        + "consulta de inventario disponible.\n\n"
        );

        sucursalDetectada.ifPresent(
                sucursal ->
                        respuesta.append(
                                "Sí identifiqué la sucursal: "
                        ).append(
                                sucursal.getCodigo()
                        ).append(
                                " - "
                        ).append(
                                sucursal.getNombre()
                        ).append(
                                ".\n\n"
                        )
        );

        respuesta.append(
                construirAyuda()
        );

        return respuesta.toString().trim();
    }

    private String construirAyuda() {

        return """
                Puedo responder preguntas como:

                • ¿Cuántas unidades hay en Ecatepec?
                • ¿Cuántas unidades hay en la sucursal Matriz?
                • ¿Cuál sucursal tiene menos mercancía?
                • ¿Cuál sucursal tiene más inventario?
                • ¿Qué productos requiero surtir?
                • ¿Qué productos debo surtir en Ecatepec?
                • ¿Qué productos están agotados?
                • ¿Qué productos tienen stock bajo?
                • ¿Dónde tengo excedentes?
                • ¿Qué mercancía conviene transferir?
                • Hazme un resumen del inventario.
                """;
    }

    // ============================================================
    // DETECCIÓN DINÁMICA DE SUCURSAL
    // ============================================================

    private Optional<Sucursal> detectarSucursal(
            String textoNormalizado,
            List<InventarioSucursal> inventarios) {

        return inventarios.stream()
                .map(
                        InventarioSucursal::getSucursal
                )
                .filter(
                        sucursal ->
                                sucursal != null
                                && sucursal.getId() != null
                )
                .distinct()
                .filter(
                        sucursal -> {

                            String nombre =
                                    normalizar(
                                            sucursal.getNombre()
                                    );

                            String codigo =
                                    normalizar(
                                            sucursal.getCodigo()
                                    );

                            return (
                                    !nombre.isBlank()
                                    && textoNormalizado.contains(
                                            nombre
                                    )
                            )
                            || (
                                    !codigo.isBlank()
                                    && contienePalabraCompleta(
                                            textoNormalizado,
                                            codigo
                                    )
                            );
                        }
                )
                .findFirst();
    }

    // ============================================================
    // CONSULTA Y FILTROS
    // ============================================================

    private List<InventarioSucursal>
    obtenerInventariosActivos() {

        return inventarioSucursalRepository
                .findAll()
                .stream()
                .filter(
                        this::inventarioValido
                )
                .sorted(
                        comparadorSucursalProducto()
                )
                .toList();
    }

    private boolean inventarioValido(
            InventarioSucursal inventario) {

        return inventario != null
                && inventario.getSucursal() != null
                && inventario.getSucursal().getId() != null
                && Boolean.TRUE.equals(
                        inventario
                                .getSucursal()
                                .getActiva()
                )
                && inventario.getProducto() != null
                && inventario.getProducto().getId() != null;
    }

    private List<InventarioSucursal>
    aplicarFiltroSucursal(
            List<InventarioSucursal> inventarios,
            Optional<Sucursal> sucursal) {

        if (sucursal.isEmpty()) {
            return inventarios;
        }

        return filtrarPorSucursal(
                inventarios,
                sucursal.get()
        );
    }

    private List<InventarioSucursal>
    filtrarPorSucursal(
            List<InventarioSucursal> inventarios,
            Sucursal sucursal) {

        return inventarios.stream()
                .filter(
                        inventario ->
                                inventario
                                        .getSucursal()
                                        .getId()
                                        .equals(
                                                sucursal.getId()
                                        )
                )
                .toList();
    }

    private Map<Long, List<InventarioSucursal>>
    agruparPorSucursalId(
            List<InventarioSucursal> inventarios) {

        return inventarios.stream()
                .collect(
                        Collectors.groupingBy(
                                inventario ->
                                        inventario
                                                .getSucursal()
                                                .getId(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        )
                );
    }

    private Map<Sucursal, Integer>
    construirTotalesPorSucursal(
            List<InventarioSucursal> inventarios) {

        Map<Sucursal, Integer> resultado =
                new LinkedHashMap<>();

        for (InventarioSucursal inventario
                : inventarios) {

            resultado.merge(
                    inventario.getSucursal(),
                    existencia(inventario),
                    Integer::sum
            );
        }

        return resultado;
    }

    // ============================================================
    // REGLAS DE INVENTARIO
    // ============================================================

    private boolean estaEnStockBajo(
            InventarioSucursal inventario) {

        int existencia =
                existencia(inventario);

        int minimo =
                stockMinimo(inventario);

        return existencia > 0
                && existencia <= minimo;
    }

    private int calcularExcedente(
            InventarioSucursal inventario) {

        int existencia =
                existencia(inventario);

        int reserva =
                stockMinimo(inventario) * 2;

        return Math.max(
                existencia - reserva,
                0
        );
    }

    private boolean mismoProducto(
            InventarioSucursal primero,
            InventarioSucursal segundo) {

        return primero.getProducto() != null
                && segundo.getProducto() != null
                && primero.getProducto().getId() != null
                && primero
                .getProducto()
                .getId()
                .equals(
                        segundo
                                .getProducto()
                                .getId()
                );
    }

    private boolean mismaSucursal(
            InventarioSucursal primero,
            InventarioSucursal segundo) {

        return primero.getSucursal() != null
                && segundo.getSucursal() != null
                && primero.getSucursal().getId() != null
                && primero
                .getSucursal()
                .getId()
                .equals(
                        segundo
                                .getSucursal()
                                .getId()
                );
    }

    private int sumarExistencias(
            List<InventarioSucursal> inventarios) {

        return inventarios.stream()
                .mapToInt(
                        this::existencia
                )
                .sum();
    }

    private int existencia(
            InventarioSucursal inventario) {

        return inventario.getExistencia() != null
                ? inventario.getExistencia()
                : 0;
    }

    private int stockMinimo(
            InventarioSucursal inventario) {

        return inventario.getStockMinimo() != null
                ? inventario.getStockMinimo()
                : 0;
    }

    // ============================================================
    // UTILIDADES
    // ============================================================

    private Comparator<InventarioSucursal>
    comparadorSucursalProducto() {

        return Comparator
                .comparing(
                        (InventarioSucursal inventario) ->
                                textoSeguro(
                                        inventario
                                                .getSucursal()
                                                .getNombre(),
                                        ""
                                )
                )
                .thenComparing(
                        inventario ->
                                textoSeguro(
                                        inventario
                                                .getProducto()
                                                .getNombre(),
                                        ""
                                )
                );
    }

    private String limpiarPregunta(
            String pregunta) {

        return pregunta != null
                ? pregunta.trim()
                : "";
    }

    private String normalizar(
            String texto) {

        if (texto == null) {
            return "";
        }

        String descompuesto =
                Normalizer.normalize(
                        texto,
                        Normalizer.Form.NFD
                );

        return descompuesto
                .replaceAll(
                        "\\p{M}",
                        ""
                )
                .toLowerCase(
                        Locale.ROOT
                )
                .replaceAll(
                        "[^a-z0-9\\s]",
                        " "
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    private boolean contieneAlguna(
            String texto,
            String... expresiones) {

        for (String expresion
                : expresiones) {

            if (
                    texto.contains(
                            normalizar(expresion)
                    )
            ) {
                return true;
            }
        }

        return false;
    }

    private boolean contienePalabraCompleta(
            String texto,
            String palabra) {

        if (palabra == null || palabra.isBlank()) {
            return false;
        }

        return (
                " " + texto + " "
        ).contains(
                " " + palabra + " "
        );
    }

    private String construirSufijoSucursal(
            Optional<Sucursal> sucursal) {

        return sucursal
                .map(
                        valor ->
                                "en "
                                + valor.getCodigo()
                                + " - "
                                + valor.getNombre()
                )
                .orElse(
                        "en las sucursales activas"
                );
    }

    private String textoSeguro(
            String valor,
            String predeterminado) {

        return valor != null
                && !valor.isBlank()
                ? valor
                : predeterminado;
    }

    private AsistenteResponse respuesta(
            String pregunta,
            String contenido) {

        return new AsistenteResponse(
                pregunta,
                contenido,
                LocalDateTime.now()
        );
    }

    private enum Intencion {

        INVENTARIO_SUCURSAL,

        INVENTARIO_TOTAL,

        SUCURSAL_MENOR_INVENTARIO,

        SUCURSAL_MAYOR_INVENTARIO,

        PRODUCTOS_POR_SURTIR,

        PRODUCTOS_AGOTADOS,

        STOCK_BAJO,

        PRODUCTOS_CON_EXCEDENTE,

        TRANSFERENCIAS,

        RESUMEN_NEGOCIO,

        LISTAR_SUCURSALES,

        AYUDA,

        DESCONOCIDA
    }

    private record SugerenciaTransferencia(
            InventarioSucursal origen,
            InventarioSucursal destino,
            int cantidad) {
    }
}
