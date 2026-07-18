#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT="/home/tonatiuh/commerce-cloud"
BASE_PACKAGE_PATH="src/main/java/com/abarrote/abarroteapi"
MULTITENANT_PATH="${BASE_PACKAGE_PATH}/multitenant"
MIGRATION_PATH="src/main/resources/db/migration"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP_PATH=".backups/multitenant-core-${TIMESTAMP}"

mostrar_error() {
    echo
    echo "============================================================"
    echo " ERROR DURANTE LA INSTALACIÓN MULTI-TENANT"
    echo "============================================================"
    echo "Línea: $1"
    echo "Comando: $2"
    echo
}

trap 'mostrar_error "$LINENO" "$BASH_COMMAND"' ERR

echo "============================================================"
echo " COMMERCE CLOUD"
echo " Instalación del núcleo multi-tenant"
echo "============================================================"

if [ ! -d "$PROJECT" ]; then
    echo "ERROR: no existe el proyecto:"
    echo "$PROJECT"
    exit 1
fi

cd "$PROJECT"

echo
echo "Directorio de trabajo:"
pwd

if [ ! -f "pom.xml" ]; then
    echo "ERROR: no se encontró pom.xml en:"
    pwd
    exit 1
fi

if [ ! -d "src/main/java" ]; then
    echo "ERROR: no existe src/main/java"
    exit 1
fi

if ! command -v java >/dev/null 2>&1; then
    echo "ERROR: Java no está instalado."
    exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
    echo "ERROR: Maven no está instalado."
    exit 1
fi

if ! command -v git >/dev/null 2>&1; then
    echo "ERROR: Git no está instalado."
    exit 1
fi

echo
echo "=== RAMA ACTUAL ==="
git branch --show-current

echo
echo "=== ESTADO INICIAL ==="
git status --short

echo
echo "=== PAQUETE PRINCIPAL DETECTADO ==="

if [ ! -d "$BASE_PACKAGE_PATH" ]; then
    echo "ERROR: no existe el paquete esperado:"
    echo "$BASE_PACKAGE_PATH"
    echo
    echo "Paquetes encontrados:"
    find src/main/java -maxdepth 5 -type d | sort
    exit 1
fi

echo "$BASE_PACKAGE_PATH"

echo
echo "=== RESPALDO ==="

mkdir -p "$BACKUP_PATH"

if [ -d "$MULTITENANT_PATH" ]; then
    cp -R "$MULTITENANT_PATH" "$BACKUP_PATH/"
    echo "Respaldado: $MULTITENANT_PATH"
fi

if [ -d "$MIGRATION_PATH" ]; then
    cp -R "$MIGRATION_PATH" "$BACKUP_PATH/"
    echo "Respaldado: $MIGRATION_PATH"
fi

cp pom.xml "$BACKUP_PATH/pom.xml.bak"

echo "Respaldo creado en:"
echo "$BACKUP_PATH"

mkdir -p \
    "$MULTITENANT_PATH/domain" \
    "$MULTITENANT_PATH/dto" \
    "$MULTITENANT_PATH/repository" \
    "$MULTITENANT_PATH/service" \
    "$MULTITENANT_PATH/context" \
    "$MULTITENANT_PATH/web" \
    "$MIGRATION_PATH"

echo
echo "=== CREANDO ENTIDADES ==="

cat > "$MULTITENANT_PATH/domain/TipoNegocio.java" <<'EOF'
package com.abarrote.abarroteapi.multitenant.domain;

public enum TipoNegocio {

    ABARROTES,
    GYM,
    FARMACIA,
    COFFEE_SHOP,
    RESTAURANTE,
    OTRO
}
EOF

cat > "$MULTITENANT_PATH/domain/EstadoTenant.java" <<'EOF'
package com.abarrote.abarroteapi.multitenant.domain;

public enum EstadoTenant {

    ACTIVO,
    SUSPENDIDO,
    VENCIDO,
    CANCELADO
}
EOF

cat > "$MULTITENANT_PATH/domain/Tenant.java" <<'EOF'
package com.abarrote.abarroteapi.multitenant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(
    name = "tenant",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_tenant_slug",
            columnNames = "slug"
        )
    },
    indexes = {
        @Index(
            name = "idx_tenant_slug",
            columnList = "slug"
        ),
        @Index(
            name = "idx_tenant_estado",
            columnList = "estado"
        ),
        @Index(
            name = "idx_tenant_tipo_negocio",
            columnList = "tipo_negocio"
        )
    }
)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "nombre",
        nullable = false,
        length = 150
    )
    private String nombre;

    @Column(
        name = "slug",
        nullable = false,
        length = 80
    )
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "tipo_negocio",
        nullable = false,
        length = 30
    )
    private TipoNegocio tipoNegocio;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "estado",
        nullable = false,
        length = 20
    )
    private EstadoTenant estado;

    @Column(
        name = "plan",
        nullable = false,
        length = 30
    )
    private String plan;

    @Column(
        name = "fecha_alta",
        nullable = false,
        updatable = false
    )
    private LocalDateTime fechaAlta;

    @Column(
        name = "fecha_actualizacion",
        nullable = false
    )
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(
        name = "activo",
        nullable = false
    )
    private boolean activo;

    public Tenant() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();

        fechaAlta = ahora;
        fechaActualizacion = ahora;
        slug = normalizarSlug(slug);

        if (estado == null) {
            estado = EstadoTenant.ACTIVO;
        }

        if (plan == null || plan.isBlank()) {
            plan = "BASICO";
        }

        activo = estado == EstadoTenant.ACTIVO;
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
        slug = normalizarSlug(slug);
        activo = estado == EstadoTenant.ACTIVO;
    }

    private String normalizarSlug(String valor) {
        if (valor == null) {
            return null;
        }

        return valor
            .trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9-]", "-")
            .replaceAll("-{2,}", "-")
            .replaceAll("^-|-$", "");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public TipoNegocio getTipoNegocio() {
        return tipoNegocio;
    }

    public void setTipoNegocio(TipoNegocio tipoNegocio) {
        this.tipoNegocio = tipoNegocio;
    }

    public EstadoTenant getEstado() {
        return estado;
    }

    public void setEstado(EstadoTenant estado) {
        this.estado = estado;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(LocalDateTime fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Tenant otroTenant)) {
            return false;
        }

        return id != null && Objects.equals(id, otroTenant.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
EOF

echo "Entidades creadas."

echo
echo "=== CREANDO DTO ==="

cat > "$MULTITENANT_PATH/dto/TenantRequest.java" <<'EOF'
package com.abarrote.abarroteapi.multitenant.dto;

import com.abarrote.abarroteapi.multitenant.domain.TipoNegocio;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class TenantRequest {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(
        min = 3,
        max = 150,
        message = "El nombre debe contener entre 3 y 150 caracteres"
    )
    private String nombre;

    @NotBlank(message = "El slug es obligatorio")
    @Size(
        min = 3,
        max = 80,
        message = "El slug debe contener entre 3 y 80 caracteres"
    )
    @Pattern(
        regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
        message = "El slug solo puede contener letras minúsculas, números y guiones"
    )
    private String slug;

    @NotNull(message = "El tipo de negocio es obligatorio")
    private TipoNegocio tipoNegocio;

    @NotBlank(message = "El plan es obligatorio")
    @Size(
        max = 30,
        message = "El plan no puede exceder 30 caracteres"
    )
    private String plan;

    @FutureOrPresent(
        message = "La fecha de vencimiento no puede estar en el pasado"
    )
    private LocalDate fechaVencimiento;

    public TenantRequest() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public TipoNegocio getTipoNegocio() {
        return tipoNegocio;
    }

    public void setTipoNegocio(TipoNegocio tipoNegocio) {
        this.tipoNegocio = tipoNegocio;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
}
EOF

cat > "$MULTITENANT_PATH/dto/TenantResponse.java" <<'EOF'
package com.abarrote.abarroteapi.multitenant.dto;

import com.abarrote.abarroteapi.multitenant.domain.EstadoTenant;
import com.abarrote.abarroteapi.multitenant.domain.TipoNegocio;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TenantResponse {

    private Long id;
    private String nombre;
    private String slug;
    private TipoNegocio tipoNegocio;
    private EstadoTenant estado;
    private String plan;
    private LocalDateTime fechaAlta;
    private LocalDate fechaVencimiento;
    private boolean activo;

    public TenantResponse() {
    }

    public TenantResponse(
        Long id,
        String nombre,
        String slug,
        TipoNegocio tipoNegocio,
        EstadoTenant estado,
        String plan,
        LocalDateTime fechaAlta,
        LocalDate fechaVencimiento,
        boolean activo
    ) {
        this.id = id;
        this.nombre = nombre;
        this.slug = slug;
        this.tipoNegocio = tipoNegocio;
        this.estado = estado;
        this.plan = plan;
        this.fechaAlta = fechaAlta;
        this.fechaVencimiento = fechaVencimiento;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSlug() {
        return slug;
    }

    public TipoNegocio getTipoNegocio() {
        return tipoNegocio;
    }

    public EstadoTenant getEstado() {
        return estado;
    }

    public String getPlan() {
        return plan;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public boolean isActivo() {
        return activo;
    }
}
EOF

echo "DTO creados."

echo
echo "=== CREANDO REPOSITORIO ==="

cat > "$MULTITENANT_PATH/repository/TenantRepository.java" <<'EOF'
package com.abarrote.abarroteapi.multitenant.repository;

import com.abarrote.abarroteapi.multitenant.domain.EstadoTenant;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findBySlugIgnoreCase(String slug);

    Optional<Tenant> findBySlugIgnoreCaseAndActivoTrue(String slug);

    boolean existsBySlugIgnoreCase(String slug);

    List<Tenant> findAllByEstadoOrderByNombreAsc(
        EstadoTenant estado
    );

    List<Tenant> findAllByOrderByNombreAsc();
}
EOF

echo "Repositorio creado."

echo
echo "=== CREANDO SERVICIO ==="

cat > "$MULTITENANT_PATH/service/TenantService.java" <<'EOF'
package com.abarrote.abarroteapi.multitenant.service;

import com.abarrote.abarroteapi.multitenant.dto.TenantRequest;
import com.abarrote.abarroteapi.multitenant.dto.TenantResponse;

import java.util.List;

public interface TenantService {

    TenantResponse crear(TenantRequest request);

    TenantResponse obtenerPorId(Long id);

    TenantResponse obtenerPorSlug(String slug);

    List<TenantResponse> listar();

    TenantResponse suspender(Long id);

    TenantResponse reactivar(Long id);
}
EOF

cat > "$MULTITENANT_PATH/service/TenantServiceImpl.java" <<'EOF'
package com.abarrote.abarroteapi.multitenant.service;

import com.abarrote.abarroteapi.multitenant.domain.EstadoTenant;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.dto.TenantRequest;
import com.abarrote.abarroteapi.multitenant.dto.TenantResponse;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    public TenantServiceImpl(
        TenantRepository tenantRepository
    ) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public TenantResponse crear(TenantRequest request) {
        String slug = normalizarSlug(request.getSlug());

        if (tenantRepository.existsBySlugIgnoreCase(slug)) {
            throw new IllegalArgumentException(
                "Ya existe una empresa con el slug: " + slug
            );
        }

        Tenant tenant = new Tenant();
        tenant.setNombre(request.getNombre().trim());
        tenant.setSlug(slug);
        tenant.setTipoNegocio(request.getTipoNegocio());
        tenant.setPlan(
            request.getPlan()
                .trim()
                .toUpperCase(Locale.ROOT)
        );
        tenant.setFechaVencimiento(
            request.getFechaVencimiento()
        );
        tenant.setEstado(EstadoTenant.ACTIVO);
        tenant.setActivo(true);

        return convertirRespuesta(
            tenantRepository.save(tenant)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse obtenerPorId(Long id) {
        return convertirRespuesta(
            obtenerEntidad(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse obtenerPorSlug(String slug) {
        Tenant tenant = tenantRepository
            .findBySlugIgnoreCase(
                normalizarSlug(slug)
            )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "No existe el tenant con slug: " + slug
                )
            );

        return convertirRespuesta(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantResponse> listar() {
        return tenantRepository
            .findAllByOrderByNombreAsc()
            .stream()
            .map(this::convertirRespuesta)
            .toList();
    }

    @Override
    public TenantResponse suspender(Long id) {
        Tenant tenant = obtenerEntidad(id);

        tenant.setEstado(EstadoTenant.SUSPENDIDO);
        tenant.setActivo(false);

        return convertirRespuesta(
            tenantRepository.save(tenant)
        );
    }

    @Override
    public TenantResponse reactivar(Long id) {
        Tenant tenant = obtenerEntidad(id);

        tenant.setEstado(EstadoTenant.ACTIVO);
        tenant.setActivo(true);

        return convertirRespuesta(
            tenantRepository.save(tenant)
        );
    }

    private Tenant obtenerEntidad(Long id) {
        return tenantRepository
            .findById(id)
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "No existe el tenant con id: " + id
                )
            );
    }

    private TenantResponse convertirRespuesta(
        Tenant tenant
    ) {
        return new TenantResponse(
            tenant.getId(),
            tenant.getNombre(),
            tenant.getSlug(),
            tenant.getTipoNegocio(),
            tenant.getEstado(),
            tenant.getPlan(),
            tenant.getFechaAlta(),
            tenant.getFechaVencimiento(),
            tenant.isActivo()
        );
    }

    private String normalizarSlug(String valor) {
        if (valor == null) {
            return "";
        }

        return valor
            .trim()
            .toLowerCase(Locale.ROOT);
    }
}
EOF

echo "Servicio creado."

echo
echo "=== CREANDO CONTEXTO MULTI-TENANT ==="

cat > "$MULTITENANT_PATH/context/TenantContext.java" <<'EOF'
package com.abarrote.abarroteapi.multitenant.context;

public final class TenantContext {

    private static final ThreadLocal<Long> TENANT_ID =
        new ThreadLocal<>();

    private static final ThreadLocal<String> TENANT_SLUG =
        new ThreadLocal<>();

    private TenantContext() {
    }

    public static void establecer(
        Long tenantId,
        String tenantSlug
    ) {
        if (tenantId == null) {
            throw new IllegalArgumentException(
                "El tenantId no puede ser nulo"
            );
        }

        if (
            tenantSlug == null
                || tenantSlug.isBlank()
        ) {
            throw new IllegalArgumentException(
                "El tenantSlug no puede estar vacío"
            );
        }

        TENANT_ID.set(tenantId);
        TENANT_SLUG.set(tenantSlug);
    }

    public static Long obtenerTenantId() {
        Long tenantId = TENANT_ID.get();

        if (tenantId == null) {
            throw new IllegalStateException(
                "No existe tenant en la petición actual"
            );
        }

        return tenantId;
    }

    public static String obtenerTenantSlug() {
        String tenantSlug = TENANT_SLUG.get();

        if (
            tenantSlug == null
                || tenantSlug.isBlank()
        ) {
            throw new IllegalStateException(
                "No existe tenant en la petición actual"
            );
        }

        return tenantSlug;
    }

    public static Long obtenerTenantIdOpcional() {
        return TENANT_ID.get();
    }

    public static boolean existeTenant() {
        return TENANT_ID.get() != null;
    }

    public static void limpiar() {
        TENANT_ID.remove();
        TENANT_SLUG.remove();
    }
}
EOF

echo "Contexto creado."

echo
echo "=== CREANDO CONTROLADOR ==="

cat > "$MULTITENANT_PATH/web/TenantController.java" <<'EOF'
package com.abarrote.abarroteapi.multitenant.web;

import com.abarrote.abarroteapi.multitenant.dto.TenantRequest;
import com.abarrote.abarroteapi.multitenant.dto.TenantResponse;
import com.abarrote.abarroteapi.multitenant.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(
        TenantService tenantService
    ) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<TenantResponse> crear(
        @Valid @RequestBody TenantRequest request
    ) {
        TenantResponse response =
            tenantService.crear(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TenantResponse>> listar() {
        return ResponseEntity.ok(
            tenantService.listar()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> obtenerPorId(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(
            tenantService.obtenerPorId(id)
        );
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<TenantResponse> obtenerPorSlug(
        @PathVariable String slug
    ) {
        return ResponseEntity.ok(
            tenantService.obtenerPorSlug(slug)
        );
    }

    @PatchMapping("/{id}/suspender")
    public ResponseEntity<TenantResponse> suspender(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(
            tenantService.suspender(id)
        );
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<TenantResponse> reactivar(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(
            tenantService.reactivar(id)
        );
    }
}
EOF

echo "Controlador creado."

echo
echo "=== CREANDO MIGRACIÓN SQL ==="

cat > "$MIGRATION_PATH/V100__crear_tabla_tenant.sql" <<'EOF'
CREATE TABLE IF NOT EXISTS tenant (
    id BIGSERIAL PRIMARY KEY,

    nombre VARCHAR(150) NOT NULL,

    slug VARCHAR(80) NOT NULL,

    tipo_negocio VARCHAR(30) NOT NULL,

    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',

    plan VARCHAR(30) NOT NULL DEFAULT 'BASICO',

    fecha_alta TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    fecha_vencimiento DATE,

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT uk_tenant_slug
        UNIQUE (slug),

    CONSTRAINT ck_tenant_tipo_negocio
        CHECK (
            tipo_negocio IN (
                'ABARROTES',
                'GYM',
                'FARMACIA',
                'COFFEE_SHOP',
                'RESTAURANTE',
                'OTRO'
            )
        ),

    CONSTRAINT ck_tenant_estado
        CHECK (
            estado IN (
                'ACTIVO',
                'SUSPENDIDO',
                'VENCIDO',
                'CANCELADO'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_tenant_slug
    ON tenant(slug);

CREATE INDEX IF NOT EXISTS idx_tenant_estado
    ON tenant(estado);

CREATE INDEX IF NOT EXISTS idx_tenant_tipo_negocio
    ON tenant(tipo_negocio);
EOF

echo "Migración creada."

echo
echo "=== VALIDACIÓN DE DEPENDENCIA VALIDATION ==="

if ! grep -q "spring-boot-starter-validation" pom.xml; then
    echo
    echo "Agregando spring-boot-starter-validation a pom.xml..."

    python3 <<'PY'
from pathlib import Path

pom_path = Path("pom.xml")
contenido = pom_path.read_text(encoding="utf-8")

dependencia = """
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
"""

if "spring-boot-starter-validation" not in contenido:
    marcador = "<dependencies>"

    if marcador not in contenido:
        raise SystemExit(
            "ERROR: no se encontró <dependencies> en pom.xml"
        )

    contenido = contenido.replace(
        marcador,
        marcador + dependencia,
        1
    )

    pom_path.write_text(
        contenido,
        encoding="utf-8"
    )
PY
else
    echo "La dependencia validation ya existe."
fi

echo
echo "=== ARCHIVOS CREADOS ==="

find "$MULTITENANT_PATH" \
    -type f \
    -name "*.java" \
    | sort

echo "$MIGRATION_PATH/V100__crear_tabla_tenant.sql"

echo
echo "=== VALIDANDO CONFLICTOS GIT ==="

if grep -RniE \
    '^(<<<<<<<|=======|>>>>>>>)' \
    "$MULTITENANT_PATH" \
    "$MIGRATION_PATH"; then

    echo "ERROR: se encontraron marcadores de conflicto."
    exit 1
fi

echo
echo "=== VALIDACIÓN GIT ==="

git diff --check

echo
echo "=== COMPILACIÓN ==="

mvn clean compile

echo
echo "============================================================"
echo " MULTITENANT CORE INSTALADO CORRECTAMENTE"
echo "============================================================"

echo
echo "Proyecto:"
pwd

echo
echo "Respaldo:"
echo "$BACKUP_PATH"

echo
echo "Rama:"
git branch --show-current

echo
echo "Estado Git:"
git status --short

echo
echo "Resultado esperado:"
echo "BUILD SUCCESS"
