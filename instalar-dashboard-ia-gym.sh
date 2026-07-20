#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT="/home/tonatiuh/commerce-cloud"
JAVA_BASE="src/main/java/com/abarrote/abarroteapi"
GYM_BASE="$JAVA_BASE/gym"
TEMPLATES="src/main/resources/templates/gym"

TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP=".backups/dashboard-ia-gym-${TIMESTAMP}"

mostrar_error() {
    echo
    echo "============================================================"
    echo " ERROR INSTALANDO DASHBOARD IA GYM"
    echo "============================================================"
    echo "Línea: $1"
    echo "Comando: $2"
}

trap 'mostrar_error "$LINENO" "$BASH_COMMAND"' ERR

cd "$PROJECT"

echo "============================================================"
echo " COMMERCE CLOUD - GYM CLOUD"
echo " Dashboard analítico + Asistente IA"
echo "============================================================"

test -f pom.xml
test -f "$GYM_BASE/domain/ClienteGym.java"
test -f "$GYM_BASE/domain/Membresia.java"
test -f "$GYM_BASE/domain/GymProducto.java"
test -f "$GYM_BASE/repository/ClienteGymRepository.java"
test -f "$GYM_BASE/repository/MembresiaRepository.java"
test -f "$GYM_BASE/repository/GymProductoRepository.java"
test -f "$GYM_BASE/web/GymCloudController.java"
test -f "$TEMPLATES/dashboard.html"

mkdir -p \
    "$BACKUP" \
    "$GYM_BASE/dto" \
    "$GYM_BASE/service" \
    "$GYM_BASE/web" \
    "$TEMPLATES"

cp "$GYM_BASE/repository/ClienteGymRepository.java" \
   "$BACKUP/ClienteGymRepository.java.bak"

cp "$GYM_BASE/repository/MembresiaRepository.java" \
   "$BACKUP/MembresiaRepository.java.bak"

cp "$GYM_BASE/repository/GymProductoRepository.java" \
   "$BACKUP/GymProductoRepository.java.bak"

cp "$GYM_BASE/web/GymCloudController.java" \
   "$BACKUP/GymCloudController.java.bak"

cp "$TEMPLATES/dashboard.html" \
   "$BACKUP/dashboard.html.bak"

echo
echo "[1/8] Ampliando ClienteGymRepository..."

cat > "$GYM_BASE/repository/ClienteGymRepository.java" <<'EOF'
package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.ClienteGym;
import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClienteGymRepository
    extends JpaRepository<ClienteGym, Long> {

    List<ClienteGym> findAllByTenantIdOrderByNombreAsc(
        Long tenantId
    );

    Optional<ClienteGym> findByIdAndTenantId(
        Long id,
        Long tenantId
    );

    Optional<ClienteGym>
        findByTenantIdAndNumeroClienteIgnoreCase(
            Long tenantId,
            String numeroCliente
        );

    boolean existsByTenantIdAndNumeroClienteIgnoreCase(
        Long tenantId,
        String numeroCliente
    );

    long countByTenantId(Long tenantId);

    /*
     * Clientes que no tienen ninguna membresía activa
     * y vigente para el tenant autenticado.
     */
    @EntityGraph(attributePaths = {})
    @Query("""
        SELECT c
          FROM ClienteGym c
         WHERE c.tenant.id = :tenantId
           AND c.estado = com.abarrote.abarroteapi.gym.domain.EstadoClienteGym.ACTIVO
           AND NOT EXISTS (
                SELECT m.id
                  FROM Membresia m
                 WHERE m.tenant.id = :tenantId
                   AND m.cliente.id = c.id
                   AND m.estado = :estado
                   AND m.fechaFin >= :hoy
           )
         ORDER BY c.nombre ASC, c.apellidoPaterno ASC
        """)
    List<ClienteGym> buscarSinMembresiaActiva(
        @Param("tenantId")
        Long tenantId,

        @Param("estado")
        EstadoMembresia estado,

        @Param("hoy")
        LocalDate hoy
    );
}
EOF

echo
echo "[2/8] Ampliando MembresiaRepository..."

cat > "$GYM_BASE/repository/MembresiaRepository.java" <<'EOF'
package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;
import com.abarrote.abarroteapi.gym.domain.Membresia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MembresiaRepository
    extends JpaRepository<Membresia, Long> {

    @EntityGraph(
        attributePaths = {
            "cliente",
            "plan"
        }
    )
    Optional<Membresia> findByIdAndTenantId(
        Long id,
        Long tenantId
    );

    @EntityGraph(
        attributePaths = {
            "cliente",
            "plan"
        }
    )
    List<Membresia>
        findAllByTenantIdAndClienteIdOrderByFechaInicioDesc(
            Long tenantId,
            Long clienteId
        );

    @EntityGraph(
        attributePaths = {
            "cliente",
            "plan"
        }
    )
    List<Membresia>
        findAllByTenantIdOrderByFechaCreacionDesc(
            Long tenantId
        );

    List<Membresia>
        findAllByTenantIdAndEstadoAndFechaFinBefore(
            Long tenantId,
            EstadoMembresia estado,
            LocalDate fecha
        );

    @EntityGraph(
        attributePaths = {
            "cliente",
            "plan"
        }
    )
    List<Membresia>
        findAllByTenantIdAndEstadoAndFechaFinBetweenOrderByFechaFinAsc(
            Long tenantId,
            EstadoMembresia estado,
            LocalDate fechaInicial,
            LocalDate fechaFinal
        );

    long countByTenantIdAndEstado(
        Long tenantId,
        EstadoMembresia estado
    );

    long countByTenantIdAndFechaFinBeforeAndEstado(
        Long tenantId,
        LocalDate fecha,
        EstadoMembresia estado
    );
}
EOF

echo
echo "[3/8] Ampliando GymProductoRepository..."

cat > "$GYM_BASE/repository/GymProductoRepository.java" <<'EOF'
package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.GymProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GymProductoRepository
    extends JpaRepository<GymProducto, Long> {

    List<GymProducto>
        findAllByTenantIdAndActivoTrueOrderByNombreAsc(
            Long tenantId
        );

    Optional<GymProducto> findByIdAndTenantId(
        Long id,
        Long tenantId
    );

    boolean existsByTenantIdAndCodigoIgnoreCase(
        Long tenantId,
        String codigo
    );

    long countByTenantIdAndActivoTrue(
        Long tenantId
    );

    long countByTenantIdAndActivoTrueAndExistenciaLessThanEqual(
        Long tenantId,
        Integer existencia
    );

    @Query("""
        SELECT p
          FROM GymProducto p
         WHERE p.tenant.id = :tenantId
           AND p.activo = true
           AND p.existencia <= p.stockMinimo
         ORDER BY p.existencia ASC, p.nombre ASC
        """)
    List<GymProducto> buscarProductosStockBajo(
        @Param("tenantId")
        Long tenantId
    );
}
EOF

echo
echo "[4/8] Creando servicio analítico..."

cat > "$GYM_BASE/service/GymDashboardService.java" <<'EOF'
package com.abarrote.abarroteapi.gym.service;

import com.abarrote.abarroteapi.gym.domain.ClienteGym;
import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;
import com.abarrote.abarroteapi.gym.domain.GymProducto;
import com.abarrote.abarroteapi.gym.domain.Membresia;
import com.abarrote.abarroteapi.gym.repository.ClienteGymRepository;
import com.abarrote.abarroteapi.gym.repository.GymProductoRepository;
import com.abarrote.abarroteapi.gym.repository.MembresiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GymDashboardService {

    private final ClienteGymRepository clienteRepository;

    private final MembresiaRepository membresiaRepository;

    private final GymProductoRepository productoRepository;

    public GymDashboardService(
        ClienteGymRepository clienteRepository,
        MembresiaRepository membresiaRepository,
        GymProductoRepository productoRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.membresiaRepository = membresiaRepository;
        this.productoRepository = productoRepository;
    }

    public List<Membresia> proximasAVencer(
        Long tenantId
    ) {
        LocalDate hoy = LocalDate.now();

        return membresiaRepository
            .findAllByTenantIdAndEstadoAndFechaFinBetweenOrderByFechaFinAsc(
                tenantId,
                EstadoMembresia.ACTIVA,
                hoy,
                hoy.plusDays(5)
            );
    }

    public List<ClienteGym> clientesSinMembresia(
        Long tenantId
    ) {
        return clienteRepository
            .buscarSinMembresiaActiva(
                tenantId,
                EstadoMembresia.ACTIVA,
                LocalDate.now()
            );
    }

    public List<GymProducto> productosStockBajo(
        Long tenantId
    ) {
        return productoRepository
            .buscarProductosStockBajo(tenantId);
    }

    /*
     * Conteo de membresías que vencen:
     *
     * Hoy, mañana, en 2, 3, 4 y 5 días.
     */
    public List<Long> conteoVencimientosPorDia(
        Long tenantId
    ) {
        LocalDate hoy = LocalDate.now();

        List<Membresia> membresias =
            proximasAVencer(tenantId);

        List<Long> resultado =
            new ArrayList<>();

        for (int dia = 0; dia <= 5; dia++) {
            LocalDate fecha = hoy.plusDays(dia);

            long total = membresias
                .stream()
                .filter(
                    membresia ->
                        fecha.equals(
                            membresia.getFechaFin()
                        )
                )
                .count();

            resultado.add(total);
        }

        return resultado;
    }

    public long totalClientes(
        Long tenantId
    ) {
        return clienteRepository.countByTenantId(
            tenantId
        );
    }

    public long membresiasActivas(
        Long tenantId
    ) {
        return membresiaRepository
            .countByTenantIdAndEstado(
                tenantId,
                EstadoMembresia.ACTIVA
            );
    }

    public long clientesSinMembresiaTotal(
        Long tenantId
    ) {
        return clientesSinMembresia(
            tenantId
        ).size();
    }
}
EOF

echo
echo "[5/8] Creando Asistente IA Gym..."

cat > "$GYM_BASE/dto/GymAiRequest.java" <<'EOF'
package com.abarrote.abarroteapi.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GymAiRequest(

    @NotBlank
    @Size(max = 500)
    String pregunta
) {
}
EOF

cat > "$GYM_BASE/dto/GymAiResponse.java" <<'EOF'
package com.abarrote.abarroteapi.gym.dto;

import java.util.List;

public record GymAiResponse(
    String respuesta,
    String categoria,
    List<String> detalles
) {
}
EOF

cat > "$GYM_BASE/service/GymAiService.java" <<'EOF'
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
EOF

echo
echo "[6/8] Creando controlador IA..."

cat > "$GYM_BASE/web/GymAiController.java" <<'EOF'
package com.abarrote.abarroteapi.gym.web;

import com.abarrote.abarroteapi.gym.dto.GymAiRequest;
import com.abarrote.abarroteapi.gym.dto.GymAiResponse;
import com.abarrote.abarroteapi.gym.service.GymAiService;
import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GymAiController {

    private final GymAiService gymAiService;

    public GymAiController(
        GymAiService gymAiService
    ) {
        this.gymAiService = gymAiService;
    }

    @GetMapping("/gym/asistente")
    public String asistente(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        validarGym(principal);

        model.addAttribute(
            "principal",
            principal
        );

        return "gym/asistente";
    }

    @PostMapping("/gym/api/asistente")
    @ResponseBody
    public ResponseEntity<GymAiResponse>
        responder(
            @AuthenticationPrincipal
            CommerceUserPrincipal principal,

            @Valid
            @RequestBody GymAiRequest request
        ) {

        validarGym(principal);

        return ResponseEntity.ok(
            gymAiService.responder(
                principal.getTenantId(),
                request.pregunta()
            )
        );
    }

    private void validarGym(
        CommerceUserPrincipal principal
    ) {
        if (
            principal == null
                || !principal.esGym()
        ) {
            throw new IllegalArgumentException(
                "Acceso exclusivo de Gym Cloud"
            );
        }
    }
}
EOF

echo
echo "[7/8] Actualizando GymCloudController..."

python3 <<'PY'
from pathlib import Path

path = Path(
    "src/main/java/com/abarrote/abarroteapi/gym/web/GymCloudController.java"
)

text = path.read_text(encoding="utf-8")

if "GymDashboardService" not in text:
    text = text.replace(
        "import com.abarrote.abarroteapi.gym.service.GymComercialService;",
        """import com.abarrote.abarroteapi.gym.service.GymComercialService;
import com.abarrote.abarroteapi.gym.service.GymDashboardService;"""
    )

    text = text.replace(
        "private final GymComercialService comercialService;",
        """private final GymComercialService comercialService;

    private final GymDashboardService dashboardService;"""
    )

    text = text.replace(
        """public GymCloudController(
        GymComercialService comercialService,
        GymService gymService,""",
        """public GymCloudController(
        GymComercialService comercialService,
        GymDashboardService dashboardService,
        GymService gymService,"""
    )

    text = text.replace(
        """this.comercialService = comercialService;
        this.gymService = gymService;""",
        """this.comercialService = comercialService;
        this.dashboardService = dashboardService;
        this.gymService = gymService;"""
    )

old = """        model.addAttribute(
            "membresiasVencidas",
            membresiaRepository
                .countByTenantIdAndFechaFinBeforeAndEstado(
                    tenantId,
                    LocalDate.now(),
                    EstadoMembresia.ACTIVA
                )
        );

        return "gym/dashboard";"""

new = """        model.addAttribute(
            "membresiasVencidas",
            membresiaRepository
                .countByTenantIdAndFechaFinBeforeAndEstado(
                    tenantId,
                    LocalDate.now(),
                    EstadoMembresia.ACTIVA
                )
        );

        model.addAttribute(
            "proximasAVencer",
            dashboardService.proximasAVencer(
                tenantId
            )
        );

        model.addAttribute(
            "clientesSinMembresia",
            dashboardService.clientesSinMembresia(
                tenantId
            )
        );

        model.addAttribute(
            "productosStockBajo",
            dashboardService.productosStockBajo(
                tenantId
            )
        );

        model.addAttribute(
            "vencimientosDatos",
            dashboardService.conteoVencimientosPorDia(
                tenantId
            )
        );

        model.addAttribute(
            "clientesConMembresia",
            dashboardService.membresiasActivas(
                tenantId
            )
        );

        model.addAttribute(
            "clientesSinMembresiaTotal",
            dashboardService.clientesSinMembresiaTotal(
                tenantId
            )
        );

        return "gym/dashboard";"""

if old not in text:
    raise SystemExit(
        "ERROR: no se encontró el bloque dashboard esperado."
    )

text = text.replace(old, new, 1)

path.write_text(
    text,
    encoding="utf-8"
)

print("GymCloudController actualizado.")
PY

echo
echo "[8/8] Creando dashboard y pantalla IA..."

cat > "$TEMPLATES/dashboard.html" <<'EOF'
<!DOCTYPE html>
<html
    lang="es"
    xmlns:th="http://www.thymeleaf.org">

<head>
    <meta charset="UTF-8">

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1">

    <title>Gym Cloud</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family:
                "Segoe UI",
                system-ui,
                sans-serif;
            color: #f8fafc;
            background: #09090b;
        }

        .topbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 1rem 2rem;
            border-bottom: 1px solid #27272a;
            background: #18181b;
        }

        .brand {
            color: #c084fc;
            font-size: 1.4rem;
            font-weight: 800;
        }

        .logout {
            color: #fecaca;
            text-decoration: none;
        }

        .layout {
            display: grid;
            grid-template-columns: 230px 1fr;
            min-height: calc(100vh - 70px);
        }

        .sidebar {
            padding: 1.5rem;
            border-right: 1px solid #27272a;
            background: #111113;
        }

        .sidebar a {
            display: block;
            margin-bottom: 0.7rem;
            padding: 0.85rem;
            border-radius: 10px;
            color: #d4d4d8;
            text-decoration: none;
        }

        .sidebar a:hover {
            color: white;
            background: #27272a;
        }

        main {
            min-width: 0;
            padding: 2rem;
        }

        .cards {
            display: grid;
            grid-template-columns:
                repeat(auto-fit, minmax(190px, 1fr));
            gap: 1rem;
            margin-top: 2rem;
        }

        .card,
        .panel {
            padding: 1.35rem;
            border: 1px solid #3f3f46;
            border-radius: 16px;
            background: #18181b;
        }

        .card strong {
            display: block;
            margin-top: 0.6rem;
            color: #c084fc;
            font-size: 2rem;
        }

        .analytics {
            display: grid;
            grid-template-columns:
                repeat(auto-fit, minmax(360px, 1fr));
            gap: 1rem;
            margin-top: 1.4rem;
        }

        .panel h2 {
            margin-top: 0;
            font-size: 1.1rem;
        }

        canvas {
            width: 100%;
            height: 260px;
        }

        .tables {
            display: grid;
            gap: 1rem;
            margin-top: 1.4rem;
        }

        .table-wrapper {
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th,
        td {
            padding: 0.75rem;
            border-bottom: 1px solid #3f3f46;
            text-align: left;
            white-space: nowrap;
        }

        th {
            color: #d8b4fe;
        }

        .danger {
            color: #fca5a5;
        }

        .warning {
            color: #fde68a;
        }

        .empty {
            color: #94a3b8;
        }

        .ai-button {
            display: inline-block;
            margin-top: 1.4rem;
            padding: 0.9rem 1.2rem;
            border-radius: 10px;
            color: white;
            text-decoration: none;
            font-weight: 800;
            background:
                linear-gradient(
                    135deg,
                    #6d28d9,
                    #2563eb
                );
        }

        @media (max-width: 760px) {
            .layout {
                display: block;
            }

            .sidebar {
                border-right: none;
                border-bottom: 1px solid #27272a;
            }

            main {
                padding: 1rem;
            }

            .analytics {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>

<body>

<header class="topbar">

    <div class="brand">
        🏋️ Gym Cloud
    </div>

    <a
        class="logout"
        th:href="@{/logout}">
        Cerrar sesión
    </a>

</header>

<div class="layout">

    <nav class="sidebar">

        <a th:href="@{/gym/dashboard}">
            📊 Dashboard
        </a>

        <a th:href="@{/gym/pos}">
            🧾 Punto de venta
        </a>

        <a th:href="@{/gym/productos}">
            🥤 Productos
        </a>

        <a th:href="@{/gym/ventas}">
            💰 Ventas
        </a>

        <a th:href="@{/gym/clientes}">
            👥 Clientes
        </a>

        <a th:href="@{/gym/membresias}">
            🎫 Membresías y pagos
        </a>

        <a th:href="@{/gym/asistente}">
            🤖 Asistente IA
        </a>

    </nav>

    <main>

        <h1
            th:text="${principal.tenantNombre}">
            Gym
        </h1>

        <p>
            Bienvenido,
            <strong
                th:text="${principal.nombre}">
                Administrador
            </strong>
        </p>

        <a
            class="ai-button"
            th:href="@{/gym/asistente}">
            🤖 Consultar Asistente IA Gym
        </a>

        <section class="cards">

            <article class="card">
                Productos activos
                <strong
                    th:text="${totalProductos}">
                    0
                </strong>
            </article>

            <article class="card">
                Ventas realizadas
                <strong
                    th:text="${totalVentas}">
                    0
                </strong>
            </article>

            <article class="card">
                Clientes registrados
                <strong
                    th:text="${totalClientes}">
                    0
                </strong>
            </article>

            <article class="card">
                Membresías activas
                <strong
                    th:text="${membresiasActivas}">
                    0
                </strong>
            </article>

            <article class="card">
                Vencen en 5 días
                <strong
                    class="warning"
                    th:text="${proximasAVencer.size()}">
                    0
                </strong>
            </article>

            <article class="card">
                Sin membresía activa
                <strong
                    class="danger"
                    th:text="${clientesSinMembresia.size()}">
                    0
                </strong>
            </article>

            <article class="card">
                Productos con stock bajo
                <strong
                    class="danger"
                    th:text="${productosStockBajo.size()}">
                    0
                </strong>
            </article>

        </section>

        <section class="analytics">

            <article class="panel">

                <h2>
                    Membresías que vencen en los próximos 5 días
                </h2>

                <canvas id="graficaVencimientos"></canvas>

            </article>

            <article class="panel">

                <h2>
                    Estado actual de clientes
                </h2>

                <canvas id="graficaMembresias"></canvas>

            </article>

        </section>

        <section class="tables">

            <article class="panel">

                <h2>
                    Próximas membresías a vencer
                </h2>

                <div class="table-wrapper">

                    <table>

                        <thead>
                        <tr>
                            <th>Cliente</th>
                            <th>Plan</th>
                            <th>Vencimiento</th>
                        </tr>
                        </thead>

                        <tbody>

                        <tr
                            th:each="membresia : ${proximasAVencer}">

                            <td>
                                <span
                                    th:text="${membresia.cliente.nombre}">
                                </span>

                                <span
                                    th:text="${membresia.cliente.apellidoPaterno}">
                                </span>
                            </td>

                            <td
                                th:text="${membresia.plan.nombre}">
                            </td>

                            <td
                                class="warning"
                                th:text="${membresia.fechaFin}">
                            </td>

                        </tr>

                        <tr
                            th:if="${proximasAVencer.isEmpty()}">

                            <td
                                colspan="3"
                                class="empty">
                                No hay vencimientos próximos.
                            </td>

                        </tr>

                        </tbody>

                    </table>

                </div>

            </article>

            <article class="panel">

                <h2>
                    Clientes sin membresía activa
                </h2>

                <div class="table-wrapper">

                    <table>

                        <thead>
                        <tr>
                            <th>Número</th>
                            <th>Cliente</th>
                            <th>Teléfono</th>
                        </tr>
                        </thead>

                        <tbody>

                        <tr
                            th:each="cliente : ${clientesSinMembresia}">

                            <td
                                th:text="${cliente.numeroCliente}">
                            </td>

                            <td>
                                <span
                                    th:text="${cliente.nombre}">
                                </span>

                                <span
                                    th:text="${cliente.apellidoPaterno}">
                                </span>
                            </td>

                            <td
                                th:text="${cliente.telefono}">
                            </td>

                        </tr>

                        <tr
                            th:if="${clientesSinMembresia.isEmpty()}">

                            <td
                                colspan="3"
                                class="empty">
                                Todos los clientes tienen membresía vigente.
                            </td>

                        </tr>

                        </tbody>

                    </table>

                </div>

            </article>

            <article class="panel">

                <h2>
                    Productos con stock bajo
                </h2>

                <div class="table-wrapper">

                    <table>

                        <thead>
                        <tr>
                            <th>Código</th>
                            <th>Producto</th>
                            <th>Existencia</th>
                            <th>Mínimo</th>
                        </tr>
                        </thead>

                        <tbody>

                        <tr
                            th:each="producto : ${productosStockBajo}">

                            <td
                                th:text="${producto.codigo}">
                            </td>

                            <td
                                th:text="${producto.nombre}">
                            </td>

                            <td
                                class="danger"
                                th:text="${producto.existencia}">
                            </td>

                            <td
                                th:text="${producto.stockMinimo}">
                            </td>

                        </tr>

                        <tr
                            th:if="${productosStockBajo.isEmpty()}">

                            <td
                                colspan="4"
                                class="empty">
                                No existen productos con stock bajo.
                            </td>

                        </tr>

                        </tbody>

                    </table>

                </div>

            </article>

        </section>

    </main>

</div>

<script th:inline="javascript">

    const vencimientos =
        /*[[${vencimientosDatos}]]*/ [0, 0, 0, 0, 0, 0];

    const clientesConMembresia =
        /*[[${clientesConMembresia}]]*/ 0;

    const clientesSinMembresia =
        /*[[${clientesSinMembresiaTotal}]]*/ 0;

    function prepararCanvas(canvas) {

        const ratio =
            window.devicePixelRatio || 1;

        const width =
            canvas.clientWidth;

        const height = 260;

        canvas.width = width * ratio;
        canvas.height = height * ratio;

        const ctx =
            canvas.getContext('2d');

        ctx.scale(ratio, ratio);

        return {
            ctx,
            width,
            height
        };
    }

    function dibujarBarras(
        canvasId,
        labels,
        values
    ) {
        const canvas =
            document.getElementById(canvasId);

        const {
            ctx,
            width,
            height
        } = prepararCanvas(canvas);

        ctx.clearRect(
            0,
            0,
            width,
            height
        );

        const padding = 38;

        const chartWidth =
            width - padding * 2;

        const chartHeight =
            height - padding * 2;

        const maxValue =
            Math.max(...values, 1);

        const espacio =
            chartWidth / values.length;

        const barWidth =
            Math.max(20, espacio * 0.55);

        ctx.font =
            '12px Segoe UI';

        ctx.textAlign =
            'center';

        values.forEach(
            (value, index) => {

                const barHeight =
                    (value / maxValue)
                    * chartHeight;

                const x =
                    padding
                    + index * espacio
                    + (espacio - barWidth) / 2;

                const y =
                    padding
                    + chartHeight
                    - barHeight;

                const gradient =
                    ctx.createLinearGradient(
                        0,
                        y,
                        0,
                        y + barHeight
                    );

                gradient.addColorStop(
                    0,
                    '#c084fc'
                );

                gradient.addColorStop(
                    1,
                    '#6d28d9'
                );

                ctx.fillStyle =
                    gradient;

                ctx.fillRect(
                    x,
                    y,
                    barWidth,
                    barHeight
                );

                ctx.fillStyle =
                    '#f8fafc';

                ctx.fillText(
                    String(value),
                    x + barWidth / 2,
                    Math.max(
                        y - 8,
                        14
                    )
                );

                ctx.fillStyle =
                    '#a1a1aa';

                ctx.fillText(
                    labels[index],
                    x + barWidth / 2,
                    height - 12
                );
            }
        );
    }

    function dibujarDona(
        canvasId,
        values,
        labels
    ) {
        const canvas =
            document.getElementById(canvasId);

        const {
            ctx,
            width,
            height
        } = prepararCanvas(canvas);

        const centerX =
            width * 0.35;

        const centerY =
            height / 2;

        const radius =
            Math.min(
                width,
                height
            ) * 0.3;

        const total =
            Math.max(
                values.reduce(
                    (sum, value) =>
                        sum + value,
                    0
                ),
                1
            );

        const colors = [
            '#22c55e',
            '#ef4444'
        ];

        let start =
            -Math.PI / 2;

        values.forEach(
            (value, index) => {

                const angle =
                    (value / total)
                    * Math.PI
                    * 2;

                ctx.beginPath();

                ctx.arc(
                    centerX,
                    centerY,
                    radius,
                    start,
                    start + angle
                );

                ctx.arc(
                    centerX,
                    centerY,
                    radius * 0.55,
                    start + angle,
                    start,
                    true
                );

                ctx.closePath();

                ctx.fillStyle =
                    colors[index];

                ctx.fill();

                start += angle;
            }
        );

        ctx.font =
            'bold 18px Segoe UI';

        ctx.textAlign =
            'center';

        ctx.fillStyle =
            '#f8fafc';

        ctx.fillText(
            String(
                values.reduce(
                    (sum, value) =>
                        sum + value,
                    0
                )
            ),
            centerX,
            centerY + 6
        );

        ctx.font =
            '13px Segoe UI';

        labels.forEach(
            (label, index) => {

                const x =
                    width * 0.68;

                const y =
                    centerY
                    - 25
                    + index * 48;

                ctx.fillStyle =
                    colors[index];

                ctx.fillRect(
                    x,
                    y - 12,
                    14,
                    14
                );

                ctx.fillStyle =
                    '#e4e4e7';

                ctx.textAlign =
                    'left';

                ctx.fillText(
                    label
                        + ': '
                        + values[index],
                    x + 22,
                    y
                );
            }
        );
    }

    function renderGraficas() {

        dibujarBarras(
            'graficaVencimientos',
            [
                'Hoy',
                '1 día',
                '2 días',
                '3 días',
                '4 días',
                '5 días'
            ],
            vencimientos
        );

        dibujarDona(
            'graficaMembresias',
            [
                clientesConMembresia,
                clientesSinMembresia
            ],
            [
                'Con membresía activa',
                'Sin membresía activa'
            ]
        );
    }

    window.addEventListener(
        'load',
        renderGraficas
    );

    window.addEventListener(
        'resize',
        renderGraficas
    );

</script>

</body>
</html>
EOF

cat > "$TEMPLATES/asistente.html" <<'EOF'
<!DOCTYPE html>
<html
    lang="es"
    xmlns:th="http://www.thymeleaf.org">

<head>
    <meta charset="UTF-8">

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1">

    <title>Asistente IA | Gym Cloud</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            min-height: 100vh;
            margin: 0;
            padding: 2rem;
            font-family:
                "Segoe UI",
                system-ui,
                sans-serif;
            color: #f8fafc;
            background:
                radial-gradient(
                    circle at top right,
                    rgba(109, 40, 217, 0.30),
                    transparent 35%
                ),
                #09090b;
        }

        a {
            color: #c084fc;
        }

        .container {
            width: min(950px, 100%);
            margin: 0 auto;
        }

        .assistant {
            margin-top: 1.5rem;
            padding: 1.5rem;
            border: 1px solid #3f3f46;
            border-radius: 18px;
            background: #18181b;
        }

        .examples {
            display: flex;
            flex-wrap: wrap;
            gap: 0.6rem;
            margin: 1rem 0;
        }

        .examples button {
            padding: 0.65rem 0.8rem;
            border: 1px solid #6d28d9;
            border-radius: 999px;
            color: #e9d5ff;
            background: rgba(109, 40, 217, 0.15);
            cursor: pointer;
        }

        textarea {
            width: 100%;
            min-height: 110px;
            padding: 1rem;
            border: 1px solid #52525b;
            border-radius: 12px;
            outline: none;
            resize: vertical;
            color: white;
            background: #27272a;
            font-size: 1rem;
        }

        .send {
            margin-top: 0.8rem;
            padding: 0.85rem 1.2rem;
            border: none;
            border-radius: 10px;
            color: white;
            font-weight: 800;
            background:
                linear-gradient(
                    135deg,
                    #7c3aed,
                    #2563eb
                );
            cursor: pointer;
        }

        .response {
            display: none;
            margin-top: 1.4rem;
            padding: 1.2rem;
            border: 1px solid #3f3f46;
            border-radius: 14px;
            background: #111113;
        }

        .response.visible {
            display: block;
        }

        .category {
            display: inline-block;
            margin-bottom: 0.8rem;
            padding: 0.3rem 0.6rem;
            border-radius: 999px;
            color: #ddd6fe;
            background: #5b21b6;
            font-size: 0.75rem;
        }

        li {
            margin-bottom: 0.5rem;
            color: #d4d4d8;
        }

        .error {
            color: #fca5a5;
        }
    </style>
</head>

<body>

<div class="container">

    <a th:href="@{/gym/dashboard}">
        ← Regresar al dashboard
    </a>

    <h1>
        🤖 Asistente IA Gym Cloud
    </h1>

    <p>
        Analiza clientes, membresías, vencimientos,
        ventas e inventario del gimnasio actual.
    </p>

    <section class="assistant">

        <div class="examples">

            <button
                type="button"
                onclick="usarPregunta(
                    'Dame un resumen del gimnasio'
                )">
                Resumen general
            </button>

            <button
                type="button"
                onclick="usarPregunta(
                    '¿Quiénes vencen en los próximos 5 días?'
                )">
                Próximos vencimientos
            </button>

            <button
                type="button"
                onclick="usarPregunta(
                    'Muéstrame clientes sin membresía activa'
                )">
                Sin membresía
            </button>

            <button
                type="button"
                onclick="usarPregunta(
                    '¿Qué productos tienen stock bajo?'
                )">
                Stock bajo
            </button>

            <button
                type="button"
                onclick="usarPregunta(
                    'Dame el resumen de ventas'
                )">
                Ventas
            </button>

        </div>

        <textarea
            id="pregunta"
            placeholder="Ejemplo: ¿Qué clientes deben renovar su membresía durante los próximos cinco días?">
        </textarea>

        <button
            class="send"
            type="button"
            onclick="preguntar()">
            Consultar datos reales
        </button>

        <div
            id="respuesta"
            class="response">

            <span
                id="categoria"
                class="category">
            </span>

            <h2 id="mensaje"></h2>

            <ul id="detalles"></ul>

        </div>

    </section>

</div>

<script>

    function usarPregunta(texto) {
        document.getElementById(
            'pregunta'
        ).value = texto;
    }

    async function preguntar() {

        const pregunta =
            document.getElementById(
                'pregunta'
            ).value.trim();

        const responseBox =
            document.getElementById(
                'respuesta'
            );

        const mensaje =
            document.getElementById(
                'mensaje'
            );

        const categoria =
            document.getElementById(
                'categoria'
            );

        const detalles =
            document.getElementById(
                'detalles'
            );

        if (!pregunta) {
            alert('Escribe una pregunta.');
            return;
        }

        responseBox.classList.add(
            'visible'
        );

        mensaje.className = '';

        mensaje.textContent =
            'Consultando PostgreSQL...';

        categoria.textContent =
            'PROCESANDO';

        detalles.innerHTML = '';

        try {

            const response = await fetch(
                '/gym/api/asistente',
                {
                    method: 'POST',
                    headers: {
                        'Content-Type':
                            'application/json'
                    },
                    body: JSON.stringify({
                        pregunta
                    })
                }
            );

            if (!response.ok) {
                throw new Error(
                    'No fue posible consultar el asistente.'
                );
            }

            const data =
                await response.json();

            categoria.textContent =
                data.categoria;

            mensaje.textContent =
                data.respuesta;

            detalles.innerHTML = '';

            if (
                data.detalles
                && data.detalles.length > 0
            ) {
                data.detalles.forEach(
                    detalle => {

                        const li =
                            document.createElement(
                                'li'
                            );

                        li.textContent =
                            detalle;

                        detalles.appendChild(li);
                    }
                );
            } else {

                const li =
                    document.createElement('li');

                li.textContent =
                    'No se encontraron registros para mostrar.';

                detalles.appendChild(li);
            }

        } catch (error) {

            categoria.textContent =
                'ERROR';

            mensaje.className =
                'error';

            mensaje.textContent =
                error.message;
        }
    }

</script>

</body>
</html>
EOF

echo
echo "Validando conflictos..."

if grep -RniE \
    '^(<<<<<<<|=======|>>>>>>>)' \
    "$GYM_BASE" \
    "$TEMPLATES"; then

    echo "ERROR: existen conflictos Git."
    exit 1
fi

git diff --check

echo
echo "Compilando..."

mvn clean compile

echo
echo "============================================================"
echo " DASHBOARD IA GYM INSTALADO"
echo "============================================================"

echo
echo "Funcionalidades:"
echo "  Gráfica de vencimientos próximos"
echo "  Gráfica con/sin membresía activa"
echo "  Productos con stock bajo"
echo "  Asistente IA conectado a datos reales"
echo
echo "Rutas:"
echo "  /gym/dashboard"
echo "  /gym/asistente"
echo "  /gym/api/asistente"
echo
echo "Respaldo:"
echo "  $BACKUP"
echo
echo "Estado Git:"
git status --short
