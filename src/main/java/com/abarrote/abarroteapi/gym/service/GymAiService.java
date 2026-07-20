package com.abarrote.abarroteapi.gym.service;

import com.abarrote.abarroteapi.gym.domain.ClienteGym;
import com.abarrote.abarroteapi.gym.domain.GymProducto;
import com.abarrote.abarroteapi.gym.domain.GymVenta;
import com.abarrote.abarroteapi.gym.domain.Membresia;
import com.abarrote.abarroteapi.gym.dto.GymAiResponse;
import com.abarrote.abarroteapi.gym.repository.ClienteGymRepository;
import com.abarrote.abarroteapi.gym.repository.GymProductoRepository;
import com.abarrote.abarroteapi.gym.repository.GymVentaRepository;
import com.abarrote.abarroteapi.gym.repository.MembresiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class GymAiService {

    private final GymDashboardService dashboardService;

    private final ClienteGymRepository clienteRepository;

    private final MembresiaRepository membresiaRepository;

    private final GymProductoRepository productoRepository;

    private final GymVentaRepository ventaRepository;

    public GymAiService(
        GymDashboardService dashboardService,
        ClienteGymRepository clienteRepository,
        MembresiaRepository membresiaRepository,
        GymProductoRepository productoRepository,
        GymVentaRepository ventaRepository
    ) {
        this.dashboardService = dashboardService;
        this.clienteRepository = clienteRepository;
        this.membresiaRepository = membresiaRepository;
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
    }

    public GymAiResponse responder(
        Long tenantId,
        String pregunta
    ) {
        String consulta = normalizar(pregunta);

        if (
            contiene(
                consulta,
                "stock",
                "inventario",
                "existencia",
                "agotado",
                "productos bajos"
            )
        ) {
            return responderStock(tenantId);
        }

        if (
            contiene(
                consulta,
                "cinco dias",
                "5 dias",
                "proximos",
                "proximo",
                "vencer",
                "vencimiento",
                "pagar pronto"
            )
        ) {
            return responderProximosVencimientos(
                tenantId
            );
        }

        if (
            contiene(
                consulta,
                "sin membresia",
                "no tienen membresia",
                "inactivos",
                "membresia vencida",
                "sin pagar"
            )
        ) {
            return responderSinMembresia(
                tenantId
            );
        }

        if (
            contiene(
                consulta,
                "venta",
                "ventas",
                "ingreso",
                "vendido",
                "facturacion"
            )
        ) {
            return responderVentas(tenantId);
        }

        if (
            contiene(
                consulta,
                "producto",
                "productos"
            )
        ) {
            return responderProductos(tenantId);
        }

        if (
            contiene(
                consulta,
                "cliente",
                "clientes",
                "socios",
                "usuarios del gym"
            )
        ) {
            return responderClientes(tenantId);
        }

        if (
            contiene(
                consulta,
                "membresias activas",
                "membresia activa",
                "cuantas membresias"
            )
        ) {
            return responderMembresias(tenantId);
        }

        return responderResumen(tenantId);
    }

    private GymAiResponse responderResumen(
        Long tenantId
    ) {
        long clientes =
            dashboardService.totalClientes(tenantId);

        long activas =
            dashboardService.membresiasActivas(
                tenantId
            );

        int sinMembresia =
            dashboardService
                .clientesSinMembresia(tenantId)
                .size();

        int proximas =
            dashboardService
                .proximasAVencer(tenantId)
                .size();

        int stockBajo =
            dashboardService
                .productosStockBajo(tenantId)
                .size();

        List<String> detalles = List.of(
            "Clientes registrados: " + clientes,
            "Membresías activas: " + activas,
            "Clientes sin membresía activa: "
                + sinMembresia,
            "Membresías que vencen en 5 días: "
                + proximas,
            "Productos con stock bajo: "
                + stockBajo
        );

        return new GymAiResponse(
            "Este es el resumen operativo actual del gimnasio.",
            "RESUMEN",
            detalles
        );
    }

    private GymAiResponse responderStock(
        Long tenantId
    ) {
        List<GymProducto> productos =
            dashboardService
                .productosStockBajo(tenantId);

        List<String> detalles = productos
            .stream()
            .limit(15)
            .map(
                producto ->
                    producto.getCodigo()
                        + " - "
                        + producto.getNombre()
                        + ": existencia "
                        + producto.getExistencia()
                        + ", mínimo "
                        + producto.getStockMinimo()
            )
            .toList();

        String respuesta = productos.isEmpty()
            ? "No hay productos con stock bajo."
            : "Encontré "
                + productos.size()
                + " productos que requieren reposición.";

        return new GymAiResponse(
            respuesta,
            "STOCK_BAJO",
            detalles
        );
    }

    private GymAiResponse responderProximosVencimientos(
        Long tenantId
    ) {
        List<Membresia> membresias =
            dashboardService
                .proximasAVencer(tenantId);

        LocalDate hoy = LocalDate.now();

        List<String> detalles = membresias
            .stream()
            .limit(20)
            .map(
                membresia -> {
                    long dias = ChronoUnit.DAYS.between(
                        hoy,
                        membresia.getFechaFin()
                    );

                    return nombreCliente(
                        membresia.getCliente()
                    )
                        + " - "
                        + membresia.getPlan().getNombre()
                        + " - vence "
                        + membresia.getFechaFin()
                        + " - faltan "
                        + dias
                        + " días";
                }
            )
            .toList();

        String respuesta = membresias.isEmpty()
            ? "No existen membresías que venzan durante los próximos cinco días."
            : "Hay "
                + membresias.size()
                + " membresías que vencen entre hoy y los próximos cinco días.";

        return new GymAiResponse(
            respuesta,
            "PROXIMOS_VENCIMIENTOS",
            detalles
        );
    }

    private GymAiResponse responderSinMembresia(
        Long tenantId
    ) {
        List<ClienteGym> clientes =
            dashboardService
                .clientesSinMembresia(tenantId);

        List<String> detalles = clientes
            .stream()
            .limit(20)
            .map(
                cliente ->
                    cliente.getNumeroCliente()
                        + " - "
                        + nombreCliente(cliente)
                        + " - "
                        + valor(cliente.getTelefono())
            )
            .toList();

        String respuesta = clientes.isEmpty()
            ? "Todos los clientes activos tienen una membresía vigente."
            : "Hay "
                + clientes.size()
                + " clientes activos sin membresía vigente.";

        return new GymAiResponse(
            respuesta,
            "SIN_MEMBRESIA",
            detalles
        );
    }

    private GymAiResponse responderVentas(
        Long tenantId
    ) {
        List<GymVenta> ventas =
            ventaRepository
                .findTop50ByTenantIdOrderByFechaVentaDesc(
                    tenantId
                );

        BigDecimal total = ventas
            .stream()
            .map(GymVenta::getTotal)
            .reduce(
                BigDecimal.ZERO,
                BigDecimal::add
            );

        List<String> detalles = ventas
            .stream()
            .limit(10)
            .map(
                venta ->
                    "Venta #"
                        + venta.getId()
                        + " - $"
                        + venta.getTotal()
                        + " - "
                        + venta.getMetodoPago()
                        + " - "
                        + venta.getFechaVenta()
            )
            .toList();

        return new GymAiResponse(
            "Las últimas "
                + ventas.size()
                + " ventas suman $"
                + total
                + ".",
            "VENTAS",
            detalles
        );
    }

    private GymAiResponse responderProductos(
        Long tenantId
    ) {
        List<GymProducto> productos =
            productoRepository
                .findAllByTenantIdAndActivoTrueOrderByNombreAsc(
                    tenantId
                );

        List<String> detalles = productos
            .stream()
            .limit(15)
            .map(
                producto ->
                    producto.getCodigo()
                        + " - "
                        + producto.getNombre()
                        + " - $"
                        + producto.getPrecio()
                        + " - existencia "
                        + producto.getExistencia()
            )
            .toList();

        return new GymAiResponse(
            "Actualmente existen "
                + productos.size()
                + " productos activos.",
            "PRODUCTOS",
            detalles
        );
    }

    private GymAiResponse responderClientes(
        Long tenantId
    ) {
        List<ClienteGym> clientes =
            clienteRepository
                .findAllByTenantIdOrderByNombreAsc(
                    tenantId
                );

        List<String> detalles = clientes
            .stream()
            .limit(15)
            .map(
                cliente ->
                    cliente.getNumeroCliente()
                        + " - "
                        + nombreCliente(cliente)
                        + " - "
                        + cliente.getEstado()
            )
            .toList();

        return new GymAiResponse(
            "El gimnasio tiene "
                + clientes.size()
                + " clientes registrados.",
            "CLIENTES",
            detalles
        );
    }

    private GymAiResponse responderMembresias(
        Long tenantId
    ) {
        long activas =
            dashboardService
                .membresiasActivas(tenantId);

        List<Membresia> membresias =
            membresiaRepository
                .findAllByTenantIdOrderByFechaCreacionDesc(
                    tenantId
                );

        List<String> detalles = membresias
            .stream()
            .filter(
                membresia ->
                    membresia.getEstado()
                        == com.abarrote.abarroteapi.gym.domain.EstadoMembresia.ACTIVA
            )
            .limit(15)
            .map(
                membresia ->
                    nombreCliente(
                        membresia.getCliente()
                    )
                        + " - "
                        + membresia.getPlan().getNombre()
                        + " - vence "
                        + membresia.getFechaFin()
            )
            .toList();

        return new GymAiResponse(
            "Actualmente existen "
                + activas
                + " membresías activas.",
            "MEMBRESIAS_ACTIVAS",
            detalles
        );
    }

    private boolean contiene(
        String texto,
        String... palabras
    ) {
        for (String palabra : palabras) {
            if (
                texto.contains(
                    normalizar(palabra)
                )
            ) {
                return true;
            }
        }

        return false;
    }

    private String normalizar(String texto) {
        String base = texto == null
            ? ""
            : texto.toLowerCase(Locale.ROOT);

        return Normalizer
            .normalize(
                base,
                Normalizer.Form.NFD
            )
            .replaceAll("\\p{M}", "")
            .trim();
    }

    private String nombreCliente(
        ClienteGym cliente
    ) {
        List<String> partes =
            new ArrayList<>();

        agregar(partes, cliente.getNombre());
        agregar(
            partes,
            cliente.getApellidoPaterno()
        );
        agregar(
            partes,
            cliente.getApellidoMaterno()
        );

        return String.join(" ", partes);
    }

    private void agregar(
        List<String> partes,
        String valor
    ) {
        if (
            valor != null
                && !valor.isBlank()
        ) {
            partes.add(valor.trim());
        }
    }

    private String valor(String texto) {
        return texto == null || texto.isBlank()
            ? "sin teléfono"
            : texto;
    }
}
