#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT="/home/tonatiuh/commerce-cloud"
BASE_PACKAGE="src/main/java/com/abarrote/abarroteapi/gym"
MIGRATION_DIR="src/main/resources/db/migration"
PROPERTIES_FILE="src/main/resources/application.properties"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP_DIR=".backups/modulo-gym-${TIMESTAMP}"

mostrar_error() {
    echo
    echo "============================================================"
    echo " ERROR INSTALANDO COMMERCE CLOUD GYM"
    echo "============================================================"
    echo "Línea: $1"
    echo "Comando: $2"
}

trap 'mostrar_error "$LINENO" "$BASH_COMMAND"' ERR

cd "$PROJECT"

echo "============================================================"
echo " COMMERCE CLOUD GYM"
echo " Clientes + Planes + Membresías + Pagos"
echo "============================================================"

if [ ! -f pom.xml ]; then
    echo "ERROR: no existe pom.xml"
    exit 1
fi

if [ ! -f \
    "src/main/java/com/abarrote/abarroteapi/multitenant/domain/Tenant.java" ]; then

    echo "ERROR: primero debe instalarse el núcleo multi-tenant."
    exit 1
fi

echo
echo "Proyecto:"
pwd

echo
echo "Rama:"
git branch --show-current

echo
echo "Estado inicial:"
git status --short

mkdir -p "$BACKUP_DIR"

if [ -d "$BASE_PACKAGE" ]; then
    cp -R "$BASE_PACKAGE" "$BACKUP_DIR/"
fi

if [ -f "$PROPERTIES_FILE" ]; then
    cp "$PROPERTIES_FILE" \
       "$BACKUP_DIR/application.properties.bak"
fi

mkdir -p \
    "$BASE_PACKAGE/domain" \
    "$BASE_PACKAGE/dto" \
    "$BASE_PACKAGE/repository" \
    "$BASE_PACKAGE/service" \
    "$BASE_PACKAGE/web" \
    "$BASE_PACKAGE/bootstrap" \
    "$MIGRATION_DIR"

echo
echo "=== ENUMERACIONES ==="

cat > "$BASE_PACKAGE/domain/EstadoClienteGym.java" <<'EOF'
package com.abarrote.abarroteapi.gym.domain;

public enum EstadoClienteGym {

    ACTIVO,
    INACTIVO,
    BLOQUEADO
}
EOF

cat > "$BASE_PACKAGE/domain/EstadoMembresia.java" <<'EOF'
package com.abarrote.abarroteapi.gym.domain;

public enum EstadoMembresia {

    PENDIENTE,
    ACTIVA,
    VENCIDA,
    CANCELADA,
    SUSPENDIDA
}
EOF

cat > "$BASE_PACKAGE/domain/MetodoPagoGym.java" <<'EOF'
package com.abarrote.abarroteapi.gym.domain;

public enum MetodoPagoGym {

    EFECTIVO,
    TARJETA,
    TRANSFERENCIA,
    OTRO
}
EOF

echo
echo "=== ENTIDAD CLIENTE GYM ==="

cat > "$BASE_PACKAGE/domain/ClienteGym.java" <<'EOF'
package com.abarrote.abarroteapi.gym.domain;

import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "gym_cliente",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_gym_cliente_tenant_numero",
            columnNames = {
                "tenant_id",
                "numero_cliente"
            }
        )
    },
    indexes = {
        @Index(
            name = "idx_gym_cliente_tenant",
            columnList = "tenant_id"
        ),
        @Index(
            name = "idx_gym_cliente_nombre",
            columnList = "tenant_id,nombre,apellido_paterno"
        )
    }
)
public class ClienteGym {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "tenant_id",
        nullable = false
    )
    private Tenant tenant;

    @Column(
        name = "numero_cliente",
        nullable = false,
        length = 30
    )
    private String numeroCliente;

    @Column(
        name = "nombre",
        nullable = false,
        length = 100
    )
    private String nombre;

    @Column(
        name = "apellido_paterno",
        length = 100
    )
    private String apellidoPaterno;

    @Column(
        name = "apellido_materno",
        length = 100
    )
    private String apellidoMaterno;

    @Column(
        name = "telefono",
        length = 30
    )
    private String telefono;

    @Column(
        name = "email",
        length = 150
    )
    private String email;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(
        name = "contacto_emergencia",
        length = 150
    )
    private String contactoEmergencia;

    @Column(
        name = "telefono_emergencia",
        length = 30
    )
    private String telefonoEmergencia;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "estado",
        nullable = false,
        length = 20
    )
    private EstadoClienteGym estado;

    @Column(
        name = "fecha_registro",
        nullable = false,
        updatable = false
    )
    private LocalDateTime fechaRegistro;

    @Column(
        name = "fecha_actualizacion",
        nullable = false
    )
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();

        fechaRegistro = ahora;
        fechaActualizacion = ahora;

        if (estado == null) {
            estado = EstadoClienteGym.ACTIVO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public String getNumeroCliente() {
        return numeroCliente;
    }

    public void setNumeroCliente(String numeroCliente) {
        this.numeroCliente = numeroCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getContactoEmergencia() {
        return contactoEmergencia;
    }

    public void setContactoEmergencia(
        String contactoEmergencia
    ) {
        this.contactoEmergencia = contactoEmergencia;
    }

    public String getTelefonoEmergencia() {
        return telefonoEmergencia;
    }

    public void setTelefonoEmergencia(
        String telefonoEmergencia
    ) {
        this.telefonoEmergencia = telefonoEmergencia;
    }

    public EstadoClienteGym getEstado() {
        return estado;
    }

    public void setEstado(EstadoClienteGym estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
EOF

echo
echo "=== ENTIDAD PLAN DE MEMBRESÍA ==="

cat > "$BASE_PACKAGE/domain/PlanMembresia.java" <<'EOF'
package com.abarrote.abarroteapi.gym.domain;

import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "gym_plan_membresia",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_gym_plan_tenant_nombre",
            columnNames = {
                "tenant_id",
                "nombre"
            }
        )
    },
    indexes = {
        @Index(
            name = "idx_gym_plan_tenant",
            columnList = "tenant_id"
        )
    }
)
public class PlanMembresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "tenant_id",
        nullable = false
    )
    private Tenant tenant;

    @Column(
        name = "nombre",
        nullable = false,
        length = 100
    )
    private String nombre;

    @Column(
        name = "descripcion",
        length = 500
    )
    private String descripcion;

    @Column(
        name = "duracion_dias",
        nullable = false
    )
    private Integer duracionDias;

    @Column(
        name = "precio",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal precio;

    @Column(
        name = "activo",
        nullable = false
    )
    private boolean activo;

    @Column(
        name = "fecha_creacion",
        nullable = false,
        updatable = false
    )
    private LocalDateTime fechaCreacion;

    @Column(
        name = "fecha_actualizacion",
        nullable = false
    )
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();

        fechaCreacion = ahora;
        fechaActualizacion = ahora;
        activo = true;
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getDuracionDias() {
        return duracionDias;
    }

    public void setDuracionDias(Integer duracionDias) {
        this.duracionDias = duracionDias;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
EOF

echo
echo "=== ENTIDAD MEMBRESÍA ==="

cat > "$BASE_PACKAGE/domain/Membresia.java" <<'EOF'
package com.abarrote.abarroteapi.gym.domain;

import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "gym_membresia",
    indexes = {
        @Index(
            name = "idx_gym_membresia_tenant",
            columnList = "tenant_id"
        ),
        @Index(
            name = "idx_gym_membresia_cliente",
            columnList = "tenant_id,cliente_id"
        ),
        @Index(
            name = "idx_gym_membresia_vencimiento",
            columnList = "tenant_id,fecha_fin"
        )
    }
)
public class Membresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "tenant_id",
        nullable = false
    )
    private Tenant tenant;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "cliente_id",
        nullable = false
    )
    private ClienteGym cliente;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "plan_id",
        nullable = false
    )
    private PlanMembresia plan;

    @Column(
        name = "fecha_inicio",
        nullable = false
    )
    private LocalDate fechaInicio;

    @Column(
        name = "fecha_fin",
        nullable = false
    )
    private LocalDate fechaFin;

    @Column(
        name = "precio_contratado",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal precioContratado;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "estado",
        nullable = false,
        length = 20
    )
    private EstadoMembresia estado;

    @Column(
        name = "fecha_creacion",
        nullable = false,
        updatable = false
    )
    private LocalDateTime fechaCreacion;

    @Column(
        name = "fecha_actualizacion",
        nullable = false
    )
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();

        fechaCreacion = ahora;
        fechaActualizacion = ahora;

        if (estado == null) {
            estado = EstadoMembresia.PENDIENTE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public ClienteGym getCliente() {
        return cliente;
    }

    public void setCliente(ClienteGym cliente) {
        this.cliente = cliente;
    }

    public PlanMembresia getPlan() {
        return plan;
    }

    public void setPlan(PlanMembresia plan) {
        this.plan = plan;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getPrecioContratado() {
        return precioContratado;
    }

    public void setPrecioContratado(
        BigDecimal precioContratado
    ) {
        this.precioContratado = precioContratado;
    }

    public EstadoMembresia getEstado() {
        return estado;
    }

    public void setEstado(EstadoMembresia estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
EOF

echo
echo "=== ENTIDAD PAGO DE MEMBRESÍA ==="

cat > "$BASE_PACKAGE/domain/PagoMembresia.java" <<'EOF'
package com.abarrote.abarroteapi.gym.domain;

import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "gym_pago_membresia",
    indexes = {
        @Index(
            name = "idx_gym_pago_tenant",
            columnList = "tenant_id"
        ),
        @Index(
            name = "idx_gym_pago_membresia",
            columnList = "tenant_id,membresia_id"
        ),
        @Index(
            name = "idx_gym_pago_fecha",
            columnList = "tenant_id,fecha_pago"
        )
    }
)
public class PagoMembresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "tenant_id",
        nullable = false
    )
    private Tenant tenant;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "membresia_id",
        nullable = false
    )
    private Membresia membresia;

    @Column(
        name = "importe",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal importe;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "metodo_pago",
        nullable = false,
        length = 30
    )
    private MetodoPagoGym metodoPago;

    @Column(
        name = "referencia",
        length = 100
    )
    private String referencia;

    @Column(
        name = "fecha_pago",
        nullable = false,
        updatable = false
    )
    private LocalDateTime fechaPago;

    @Column(
        name = "observaciones",
        length = 500
    )
    private String observaciones;

    @PrePersist
    public void prePersist() {
        fechaPago = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Membresia getMembresia() {
        return membresia;
    }

    public void setMembresia(Membresia membresia) {
        this.membresia = membresia;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public MetodoPagoGym getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(
        MetodoPagoGym metodoPago
    ) {
        this.metodoPago = metodoPago;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
        String observaciones
    ) {
        this.observaciones = observaciones;
    }
}
EOF

echo
echo "=== REPOSITORIOS AISLADOS POR TENANT ==="

cat > "$BASE_PACKAGE/repository/ClienteGymRepository.java" <<'EOF'
package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.ClienteGym;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
EOF

cat > "$BASE_PACKAGE/repository/PlanMembresiaRepository.java" <<'EOF'
package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.PlanMembresia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanMembresiaRepository
    extends JpaRepository<PlanMembresia, Long> {

    List<PlanMembresia>
        findAllByTenantIdAndActivoTrueOrderByPrecioAsc(
            Long tenantId
        );

    Optional<PlanMembresia> findByIdAndTenantId(
        Long id,
        Long tenantId
    );

    boolean existsByTenantIdAndNombreIgnoreCase(
        Long tenantId,
        String nombre
    );
}
EOF

cat > "$BASE_PACKAGE/repository/MembresiaRepository.java" <<'EOF'
package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;
import com.abarrote.abarroteapi.gym.domain.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MembresiaRepository
    extends JpaRepository<Membresia, Long> {

    Optional<Membresia> findByIdAndTenantId(
        Long id,
        Long tenantId
    );

    List<Membresia>
        findAllByTenantIdAndClienteIdOrderByFechaInicioDesc(
            Long tenantId,
            Long clienteId
        );

    List<Membresia>
        findAllByTenantIdAndEstadoAndFechaFinBefore(
            Long tenantId,
            EstadoMembresia estado,
            LocalDate fecha
        );
}
EOF

cat > "$BASE_PACKAGE/repository/PagoMembresiaRepository.java" <<'EOF'
package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.PagoMembresia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoMembresiaRepository
    extends JpaRepository<PagoMembresia, Long> {

    List<PagoMembresia>
        findAllByTenantIdAndMembresiaIdOrderByFechaPagoDesc(
            Long tenantId,
            Long membresiaId
        );
}
EOF

echo
echo "=== DTO ==="

cat > "$BASE_PACKAGE/dto/ClienteGymRequest.java" <<'EOF'
package com.abarrote.abarroteapi.gym.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ClienteGymRequest(

    @NotBlank
    @Size(max = 30)
    String numeroCliente,

    @NotBlank
    @Size(max = 100)
    String nombre,

    @Size(max = 100)
    String apellidoPaterno,

    @Size(max = 100)
    String apellidoMaterno,

    @Size(max = 30)
    String telefono,

    @Email
    @Size(max = 150)
    String email,

    LocalDate fechaNacimiento,

    @Size(max = 150)
    String contactoEmergencia,

    @Size(max = 30)
    String telefonoEmergencia
) {
}
EOF

cat > "$BASE_PACKAGE/dto/PlanMembresiaRequest.java" <<'EOF'
package com.abarrote.abarroteapi.gym.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PlanMembresiaRequest(

    @NotBlank
    @Size(max = 100)
    String nombre,

    @Size(max = 500)
    String descripcion,

    @NotNull
    @Min(1)
    Integer duracionDias,

    @NotNull
    @DecimalMin("0.01")
    BigDecimal precio
) {
}
EOF

cat > "$BASE_PACKAGE/dto/MembresiaRequest.java" <<'EOF'
package com.abarrote.abarroteapi.gym.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MembresiaRequest(

    @NotNull
    Long clienteId,

    @NotNull
    Long planId,

    LocalDate fechaInicio
) {
}
EOF

cat > "$BASE_PACKAGE/dto/PagoMembresiaRequest.java" <<'EOF'
package com.abarrote.abarroteapi.gym.dto;

import com.abarrote.abarroteapi.gym.domain.MetodoPagoGym;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PagoMembresiaRequest(

    @NotNull
    Long membresiaId,

    @NotNull
    @DecimalMin("0.01")
    BigDecimal importe,

    @NotNull
    MetodoPagoGym metodoPago,

    @Size(max = 100)
    String referencia,

    @Size(max = 500)
    String observaciones
) {
}
EOF

cat > "$BASE_PACKAGE/dto/ClienteGymResponse.java" <<'EOF'
package com.abarrote.abarroteapi.gym.dto;

import com.abarrote.abarroteapi.gym.domain.EstadoClienteGym;

import java.time.LocalDate;

public record ClienteGymResponse(
    Long id,
    String numeroCliente,
    String nombreCompleto,
    String telefono,
    String email,
    LocalDate fechaNacimiento,
    EstadoClienteGym estado
) {
}
EOF

cat > "$BASE_PACKAGE/dto/PlanMembresiaResponse.java" <<'EOF'
package com.abarrote.abarroteapi.gym.dto;

import java.math.BigDecimal;

public record PlanMembresiaResponse(
    Long id,
    String nombre,
    String descripcion,
    Integer duracionDias,
    BigDecimal precio,
    boolean activo
) {
}
EOF

cat > "$BASE_PACKAGE/dto/MembresiaResponse.java" <<'EOF'
package com.abarrote.abarroteapi.gym.dto;

import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MembresiaResponse(
    Long id,
    Long clienteId,
    String cliente,
    Long planId,
    String plan,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    BigDecimal precio,
    EstadoMembresia estado
) {
}
EOF

cat > "$BASE_PACKAGE/dto/PagoMembresiaResponse.java" <<'EOF'
package com.abarrote.abarroteapi.gym.dto;

import com.abarrote.abarroteapi.gym.domain.MetodoPagoGym;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoMembresiaResponse(
    Long id,
    Long membresiaId,
    BigDecimal importe,
    MetodoPagoGym metodoPago,
    String referencia,
    LocalDateTime fechaPago
) {
}
EOF

echo
echo "=== SERVICIO GYM ==="

cat > "$BASE_PACKAGE/service/GymService.java" <<'EOF'
package com.abarrote.abarroteapi.gym.service;

import com.abarrote.abarroteapi.gym.domain.ClienteGym;
import com.abarrote.abarroteapi.gym.domain.EstadoClienteGym;
import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;
import com.abarrote.abarroteapi.gym.domain.Membresia;
import com.abarrote.abarroteapi.gym.domain.PagoMembresia;
import com.abarrote.abarroteapi.gym.domain.PlanMembresia;
import com.abarrote.abarroteapi.gym.dto.ClienteGymRequest;
import com.abarrote.abarroteapi.gym.dto.ClienteGymResponse;
import com.abarrote.abarroteapi.gym.dto.MembresiaRequest;
import com.abarrote.abarroteapi.gym.dto.MembresiaResponse;
import com.abarrote.abarroteapi.gym.dto.PagoMembresiaRequest;
import com.abarrote.abarroteapi.gym.dto.PagoMembresiaResponse;
import com.abarrote.abarroteapi.gym.dto.PlanMembresiaRequest;
import com.abarrote.abarroteapi.gym.dto.PlanMembresiaResponse;
import com.abarrote.abarroteapi.gym.repository.ClienteGymRepository;
import com.abarrote.abarroteapi.gym.repository.MembresiaRepository;
import com.abarrote.abarroteapi.gym.repository.PagoMembresiaRepository;
import com.abarrote.abarroteapi.gym.repository.PlanMembresiaRepository;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.domain.TipoNegocio;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class GymService {

    private final TenantRepository tenantRepository;
    private final ClienteGymRepository clienteRepository;
    private final PlanMembresiaRepository planRepository;
    private final MembresiaRepository membresiaRepository;
    private final PagoMembresiaRepository pagoRepository;

    public GymService(
        TenantRepository tenantRepository,
        ClienteGymRepository clienteRepository,
        PlanMembresiaRepository planRepository,
        MembresiaRepository membresiaRepository,
        PagoMembresiaRepository pagoRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.clienteRepository = clienteRepository;
        this.planRepository = planRepository;
        this.membresiaRepository = membresiaRepository;
        this.pagoRepository = pagoRepository;
    }

    public ClienteGymResponse crearCliente(
        String tenantSlug,
        ClienteGymRequest request
    ) {
        Tenant tenant = obtenerGym(tenantSlug);

        if (
            clienteRepository
                .existsByTenantIdAndNumeroClienteIgnoreCase(
                    tenant.getId(),
                    request.numeroCliente()
                )
        ) {
            throw new IllegalArgumentException(
                "El número de cliente ya existe"
            );
        }

        ClienteGym cliente = new ClienteGym();
        cliente.setTenant(tenant);
        cliente.setNumeroCliente(
            request.numeroCliente().trim()
        );
        cliente.setNombre(request.nombre().trim());
        cliente.setApellidoPaterno(
            request.apellidoPaterno()
        );
        cliente.setApellidoMaterno(
            request.apellidoMaterno()
        );
        cliente.setTelefono(request.telefono());
        cliente.setEmail(request.email());
        cliente.setFechaNacimiento(
            request.fechaNacimiento()
        );
        cliente.setContactoEmergencia(
            request.contactoEmergencia()
        );
        cliente.setTelefonoEmergencia(
            request.telefonoEmergencia()
        );
        cliente.setEstado(EstadoClienteGym.ACTIVO);

        return clienteResponse(
            clienteRepository.save(cliente)
        );
    }

    @Transactional(readOnly = true)
    public List<ClienteGymResponse> listarClientes(
        String tenantSlug
    ) {
        Tenant tenant = obtenerGym(tenantSlug);

        return clienteRepository
            .findAllByTenantIdOrderByNombreAsc(
                tenant.getId()
            )
            .stream()
            .map(this::clienteResponse)
            .toList();
    }

    public PlanMembresiaResponse crearPlan(
        String tenantSlug,
        PlanMembresiaRequest request
    ) {
        Tenant tenant = obtenerGym(tenantSlug);

        if (
            planRepository
                .existsByTenantIdAndNombreIgnoreCase(
                    tenant.getId(),
                    request.nombre()
                )
        ) {
            throw new IllegalArgumentException(
                "Ya existe un plan con ese nombre"
            );
        }

        PlanMembresia plan = new PlanMembresia();
        plan.setTenant(tenant);
        plan.setNombre(request.nombre().trim());
        plan.setDescripcion(request.descripcion());
        plan.setDuracionDias(request.duracionDias());
        plan.setPrecio(request.precio());
        plan.setActivo(true);

        return planResponse(
            planRepository.save(plan)
        );
    }

    @Transactional(readOnly = true)
    public List<PlanMembresiaResponse> listarPlanes(
        String tenantSlug
    ) {
        Tenant tenant = obtenerGym(tenantSlug);

        return planRepository
            .findAllByTenantIdAndActivoTrueOrderByPrecioAsc(
                tenant.getId()
            )
            .stream()
            .map(this::planResponse)
            .toList();
    }

    public MembresiaResponse crearMembresia(
        String tenantSlug,
        MembresiaRequest request
    ) {
        Tenant tenant = obtenerGym(tenantSlug);

        ClienteGym cliente = clienteRepository
            .findByIdAndTenantId(
                request.clienteId(),
                tenant.getId()
            )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Cliente no encontrado"
                )
            );

        PlanMembresia plan = planRepository
            .findByIdAndTenantId(
                request.planId(),
                tenant.getId()
            )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Plan no encontrado"
                )
            );

        LocalDate fechaInicio =
            request.fechaInicio() != null
                ? request.fechaInicio()
                : LocalDate.now();

        Membresia membresia = new Membresia();
        membresia.setTenant(tenant);
        membresia.setCliente(cliente);
        membresia.setPlan(plan);
        membresia.setFechaInicio(fechaInicio);
        membresia.setFechaFin(
            fechaInicio.plusDays(
                plan.getDuracionDias()
            )
        );
        membresia.setPrecioContratado(
            plan.getPrecio()
        );
        membresia.setEstado(
            EstadoMembresia.PENDIENTE
        );

        return membresiaResponse(
            membresiaRepository.save(membresia)
        );
    }

    public PagoMembresiaResponse registrarPago(
        String tenantSlug,
        PagoMembresiaRequest request
    ) {
        Tenant tenant = obtenerGym(tenantSlug);

        Membresia membresia = membresiaRepository
            .findByIdAndTenantId(
                request.membresiaId(),
                tenant.getId()
            )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Membresía no encontrada"
                )
            );

        PagoMembresia pago = new PagoMembresia();
        pago.setTenant(tenant);
        pago.setMembresia(membresia);
        pago.setImporte(request.importe());
        pago.setMetodoPago(request.metodoPago());
        pago.setReferencia(request.referencia());
        pago.setObservaciones(
            request.observaciones()
        );

        PagoMembresia guardado =
            pagoRepository.save(pago);

        membresia.setEstado(EstadoMembresia.ACTIVA);
        membresiaRepository.save(membresia);

        return pagoResponse(guardado);
    }

    private Tenant obtenerGym(String tenantSlug) {
        Tenant tenant = tenantRepository
            .findBySlugIgnoreCaseAndActivoTrue(
                tenantSlug
            )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Tenant no encontrado o inactivo"
                )
            );

        if (tenant.getTipoNegocio() != TipoNegocio.GYM) {
            throw new IllegalArgumentException(
                "El tenant no pertenece al vertical GYM"
            );
        }

        return tenant;
    }

    private ClienteGymResponse clienteResponse(
        ClienteGym cliente
    ) {
        String nombreCompleto = String.join(
            " ",
            texto(cliente.getNombre()),
            texto(cliente.getApellidoPaterno()),
            texto(cliente.getApellidoMaterno())
        ).trim();

        return new ClienteGymResponse(
            cliente.getId(),
            cliente.getNumeroCliente(),
            nombreCompleto,
            cliente.getTelefono(),
            cliente.getEmail(),
            cliente.getFechaNacimiento(),
            cliente.getEstado()
        );
    }

    private PlanMembresiaResponse planResponse(
        PlanMembresia plan
    ) {
        return new PlanMembresiaResponse(
            plan.getId(),
            plan.getNombre(),
            plan.getDescripcion(),
            plan.getDuracionDias(),
            plan.getPrecio(),
            plan.isActivo()
        );
    }

    private MembresiaResponse membresiaResponse(
        Membresia membresia
    ) {
        return new MembresiaResponse(
            membresia.getId(),
            membresia.getCliente().getId(),
            membresia.getCliente().getNombre(),
            membresia.getPlan().getId(),
            membresia.getPlan().getNombre(),
            membresia.getFechaInicio(),
            membresia.getFechaFin(),
            membresia.getPrecioContratado(),
            membresia.getEstado()
        );
    }

    private PagoMembresiaResponse pagoResponse(
        PagoMembresia pago
    ) {
        return new PagoMembresiaResponse(
            pago.getId(),
            pago.getMembresia().getId(),
            pago.getImporte(),
            pago.getMetodoPago(),
            pago.getReferencia(),
            pago.getFechaPago()
        );
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
EOF

echo
echo "=== CONTROLADOR REST GYM ==="

cat > "$BASE_PACKAGE/web/GymController.java" <<'EOF'
package com.abarrote.abarroteapi.gym.web;

import com.abarrote.abarroteapi.gym.dto.ClienteGymRequest;
import com.abarrote.abarroteapi.gym.dto.ClienteGymResponse;
import com.abarrote.abarroteapi.gym.dto.MembresiaRequest;
import com.abarrote.abarroteapi.gym.dto.MembresiaResponse;
import com.abarrote.abarroteapi.gym.dto.PagoMembresiaRequest;
import com.abarrote.abarroteapi.gym.dto.PagoMembresiaResponse;
import com.abarrote.abarroteapi.gym.dto.PlanMembresiaRequest;
import com.abarrote.abarroteapi.gym.dto.PlanMembresiaResponse;
import com.abarrote.abarroteapi.gym.service.GymService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gym/{tenantSlug}")
public class GymController {

    private final GymService gymService;

    public GymController(GymService gymService) {
        this.gymService = gymService;
    }

    @PostMapping("/clientes")
    public ResponseEntity<ClienteGymResponse>
        crearCliente(
            @PathVariable String tenantSlug,
            @Valid
            @RequestBody ClienteGymRequest request
        ) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                gymService.crearCliente(
                    tenantSlug,
                    request
                )
            );
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<ClienteGymResponse>>
        listarClientes(
            @PathVariable String tenantSlug
        ) {

        return ResponseEntity.ok(
            gymService.listarClientes(tenantSlug)
        );
    }

    @PostMapping("/planes")
    public ResponseEntity<PlanMembresiaResponse>
        crearPlan(
            @PathVariable String tenantSlug,
            @Valid
            @RequestBody PlanMembresiaRequest request
        ) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                gymService.crearPlan(
                    tenantSlug,
                    request
                )
            );
    }

    @GetMapping("/planes")
    public ResponseEntity<List<PlanMembresiaResponse>>
        listarPlanes(
            @PathVariable String tenantSlug
        ) {

        return ResponseEntity.ok(
            gymService.listarPlanes(tenantSlug)
        );
    }

    @PostMapping("/membresias")
    public ResponseEntity<MembresiaResponse>
        crearMembresia(
            @PathVariable String tenantSlug,
            @Valid
            @RequestBody MembresiaRequest request
        ) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                gymService.crearMembresia(
                    tenantSlug,
                    request
                )
            );
    }

    @PostMapping("/pagos")
    public ResponseEntity<PagoMembresiaResponse>
        registrarPago(
            @PathVariable String tenantSlug,
            @Valid
            @RequestBody PagoMembresiaRequest request
        ) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                gymService.registrarPago(
                    tenantSlug,
                    request
                )
            );
    }
}
EOF

echo
echo "=== BOOTSTRAP DE DEMOSTRACIÓN ==="

cat > "$BASE_PACKAGE/bootstrap/GymBootstrapInitializer.java" <<'EOF'
package com.abarrote.abarroteapi.gym.bootstrap;

import com.abarrote.abarroteapi.gym.domain.ClienteGym;
import com.abarrote.abarroteapi.gym.domain.EstadoClienteGym;
import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;
import com.abarrote.abarroteapi.gym.domain.Membresia;
import com.abarrote.abarroteapi.gym.domain.MetodoPagoGym;
import com.abarrote.abarroteapi.gym.domain.PagoMembresia;
import com.abarrote.abarroteapi.gym.domain.PlanMembresia;
import com.abarrote.abarroteapi.gym.repository.ClienteGymRepository;
import com.abarrote.abarroteapi.gym.repository.MembresiaRepository;
import com.abarrote.abarroteapi.gym.repository.PagoMembresiaRepository;
import com.abarrote.abarroteapi.gym.repository.PlanMembresiaRepository;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@ConditionalOnProperty(
    name = "commerce.bootstrap.gym.enabled",
    havingValue = "true"
)
public class GymBootstrapInitializer
    implements ApplicationRunner {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            GymBootstrapInitializer.class
        );

    private final TenantRepository tenantRepository;
    private final ClienteGymRepository clienteRepository;
    private final PlanMembresiaRepository planRepository;
    private final MembresiaRepository membresiaRepository;
    private final PagoMembresiaRepository pagoRepository;

    public GymBootstrapInitializer(
        TenantRepository tenantRepository,
        ClienteGymRepository clienteRepository,
        PlanMembresiaRepository planRepository,
        MembresiaRepository membresiaRepository,
        PagoMembresiaRepository pagoRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.clienteRepository = clienteRepository;
        this.planRepository = planRepository;
        this.membresiaRepository = membresiaRepository;
        this.pagoRepository = pagoRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Tenant tenant = tenantRepository
            .findBySlugIgnoreCaseAndActivoTrue(
                "gym-titan"
            )
            .orElse(null);

        if (tenant == null) {
            LOGGER.warn(
                "No existe gym-titan. No se ejecuta bootstrap Gym."
            );
            return;
        }

        crearPlanSiNoExiste(
            tenant,
            "Mensual",
            "Acceso general durante 30 días",
            30,
            new BigDecimal("500.00")
        );

        crearPlanSiNoExiste(
            tenant,
            "Trimestral",
            "Acceso general durante 90 días",
            90,
            new BigDecimal("1350.00")
        );

        crearPlanSiNoExiste(
            tenant,
            "Anual",
            "Acceso general durante 365 días",
            365,
            new BigDecimal("4800.00")
        );

        ClienteGym cliente =
            crearClienteSiNoExiste(tenant);

        crearMembresiaSiNoExiste(
            tenant,
            cliente
        );

        LOGGER.info(
            "Bootstrap de Commerce Cloud Gym terminado"
        );
    }

    private void crearPlanSiNoExiste(
        Tenant tenant,
        String nombre,
        String descripcion,
        int duracionDias,
        BigDecimal precio
    ) {
        if (
            planRepository
                .existsByTenantIdAndNombreIgnoreCase(
                    tenant.getId(),
                    nombre
                )
        ) {
            return;
        }

        PlanMembresia plan = new PlanMembresia();
        plan.setTenant(tenant);
        plan.setNombre(nombre);
        plan.setDescripcion(descripcion);
        plan.setDuracionDias(duracionDias);
        plan.setPrecio(precio);
        plan.setActivo(true);

        planRepository.save(plan);
    }

    private ClienteGym crearClienteSiNoExiste(
        Tenant tenant
    ) {
        return clienteRepository
            .findByTenantIdAndNumeroClienteIgnoreCase(
                tenant.getId(),
                "GYM-000001"
            )
            .orElseGet(
                () -> {
                    ClienteGym cliente =
                        new ClienteGym();

                    cliente.setTenant(tenant);
                    cliente.setNumeroCliente(
                        "GYM-000001"
                    );
                    cliente.setNombre("Cliente");
                    cliente.setApellidoPaterno(
                        "Demostración"
                    );
                    cliente.setTelefono(
                        "5512345678"
                    );
                    cliente.setEmail(
                        "cliente.demo@gym.local"
                    );
                    cliente.setEstado(
                        EstadoClienteGym.ACTIVO
                    );

                    return clienteRepository.save(
                        cliente
                    );
                }
            );
    }

    private void crearMembresiaSiNoExiste(
        Tenant tenant,
        ClienteGym cliente
    ) {
        List<Membresia> existentes =
            membresiaRepository
                .findAllByTenantIdAndClienteIdOrderByFechaInicioDesc(
                    tenant.getId(),
                    cliente.getId()
                );

        if (!existentes.isEmpty()) {
            return;
        }

        PlanMembresia plan = planRepository
            .findAllByTenantIdAndActivoTrueOrderByPrecioAsc(
                tenant.getId()
            )
            .stream()
            .filter(
                item -> item
                    .getNombre()
                    .equalsIgnoreCase("Mensual")
            )
            .findFirst()
            .orElseThrow();

        LocalDate inicio = LocalDate.now();

        Membresia membresia = new Membresia();
        membresia.setTenant(tenant);
        membresia.setCliente(cliente);
        membresia.setPlan(plan);
        membresia.setFechaInicio(inicio);
        membresia.setFechaFin(
            inicio.plusDays(plan.getDuracionDias())
        );
        membresia.setPrecioContratado(
            plan.getPrecio()
        );
        membresia.setEstado(
            EstadoMembresia.ACTIVA
        );

        Membresia guardada =
            membresiaRepository.save(membresia);

        PagoMembresia pago =
            new PagoMembresia();

        pago.setTenant(tenant);
        pago.setMembresia(guardada);
        pago.setImporte(plan.getPrecio());
        pago.setMetodoPago(
            MetodoPagoGym.EFECTIVO
        );
        pago.setReferencia(
            "BOOTSTRAP-DEMO"
        );
        pago.setObservaciones(
            "Pago inicial de demostración"
        );

        pagoRepository.save(pago);
    }
}
EOF

echo
echo "=== MIGRACIÓN FLYWAY V101 ==="

cat > "$MIGRATION_DIR/V101__crear_modulo_gym.sql" <<'EOF'
CREATE TABLE IF NOT EXISTS gym_cliente (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    numero_cliente VARCHAR(30) NOT NULL,

    nombre VARCHAR(100) NOT NULL,

    apellido_paterno VARCHAR(100),

    apellido_materno VARCHAR(100),

    telefono VARCHAR(30),

    email VARCHAR(150),

    fecha_nacimiento DATE,

    contacto_emergencia VARCHAR(150),

    telefono_emergencia VARCHAR(30),

    estado VARCHAR(20) NOT NULL
        DEFAULT 'ACTIVO',

    fecha_registro TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_gym_cliente_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT uk_gym_cliente_tenant_numero
        UNIQUE (tenant_id, numero_cliente),

    CONSTRAINT ck_gym_cliente_estado
        CHECK (
            estado IN (
                'ACTIVO',
                'INACTIVO',
                'BLOQUEADO'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_gym_cliente_tenant
    ON gym_cliente(tenant_id);

CREATE INDEX IF NOT EXISTS idx_gym_cliente_nombre
    ON gym_cliente(
        tenant_id,
        nombre,
        apellido_paterno
    );

CREATE TABLE IF NOT EXISTS gym_plan_membresia (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    nombre VARCHAR(100) NOT NULL,

    descripcion VARCHAR(500),

    duracion_dias INTEGER NOT NULL,

    precio NUMERIC(12,2) NOT NULL,

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_creacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_gym_plan_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT uk_gym_plan_tenant_nombre
        UNIQUE (tenant_id, nombre),

    CONSTRAINT ck_gym_plan_duracion
        CHECK (duracion_dias > 0),

    CONSTRAINT ck_gym_plan_precio
        CHECK (precio >= 0)
);

CREATE INDEX IF NOT EXISTS idx_gym_plan_tenant
    ON gym_plan_membresia(tenant_id);

CREATE TABLE IF NOT EXISTS gym_membresia (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    cliente_id BIGINT NOT NULL,

    plan_id BIGINT NOT NULL,

    fecha_inicio DATE NOT NULL,

    fecha_fin DATE NOT NULL,

    precio_contratado NUMERIC(12,2) NOT NULL,

    estado VARCHAR(20) NOT NULL
        DEFAULT 'PENDIENTE',

    fecha_creacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_gym_membresia_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT fk_gym_membresia_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES gym_cliente(id),

    CONSTRAINT fk_gym_membresia_plan
        FOREIGN KEY (plan_id)
        REFERENCES gym_plan_membresia(id),

    CONSTRAINT ck_gym_membresia_estado
        CHECK (
            estado IN (
                'PENDIENTE',
                'ACTIVA',
                'VENCIDA',
                'CANCELADA',
                'SUSPENDIDA'
            )
        ),

    CONSTRAINT ck_gym_membresia_fechas
        CHECK (fecha_fin >= fecha_inicio),

    CONSTRAINT ck_gym_membresia_precio
        CHECK (precio_contratado >= 0)
);

CREATE INDEX IF NOT EXISTS idx_gym_membresia_tenant
    ON gym_membresia(tenant_id);

CREATE INDEX IF NOT EXISTS idx_gym_membresia_cliente
    ON gym_membresia(
        tenant_id,
        cliente_id
    );

CREATE INDEX IF NOT EXISTS idx_gym_membresia_vencimiento
    ON gym_membresia(
        tenant_id,
        fecha_fin
    );

CREATE TABLE IF NOT EXISTS gym_pago_membresia (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    membresia_id BIGINT NOT NULL,

    importe NUMERIC(12,2) NOT NULL,

    metodo_pago VARCHAR(30) NOT NULL,

    referencia VARCHAR(100),

    fecha_pago TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    observaciones VARCHAR(500),

    CONSTRAINT fk_gym_pago_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT fk_gym_pago_membresia
        FOREIGN KEY (membresia_id)
        REFERENCES gym_membresia(id),

    CONSTRAINT ck_gym_pago_importe
        CHECK (importe > 0),

    CONSTRAINT ck_gym_pago_metodo
        CHECK (
            metodo_pago IN (
                'EFECTIVO',
                'TARJETA',
                'TRANSFERENCIA',
                'OTRO'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_gym_pago_tenant
    ON gym_pago_membresia(tenant_id);

CREATE INDEX IF NOT EXISTS idx_gym_pago_membresia
    ON gym_pago_membresia(
        tenant_id,
        membresia_id
    );

CREATE INDEX IF NOT EXISTS idx_gym_pago_fecha
    ON gym_pago_membresia(
        tenant_id,
        fecha_pago
    );
EOF

echo
echo "=== HABILITANDO BOOTSTRAP GYM ==="

if grep -q \
    '^commerce\.bootstrap\.gym\.enabled=' \
    "$PROPERTIES_FILE"; then

    sed -i \
        's/^commerce\.bootstrap\.gym\.enabled=.*/commerce.bootstrap.gym.enabled=true/' \
        "$PROPERTIES_FILE"
else
    cat >> "$PROPERTIES_FILE" <<'EOF'

# Commerce Cloud Gym
# Datos de demostración controlados.
commerce.bootstrap.gym.enabled=true
EOF
fi

echo
echo "=== VALIDACIONES ==="

grep -RniE \
    '^(<<<<<<<|=======|>>>>>>>)' \
    "$BASE_PACKAGE" \
    "$MIGRATION_DIR/V101__crear_modulo_gym.sql" \
    && {
        echo "ERROR: existen conflictos Git."
        exit 1
    } || true

git diff --check

echo
echo "=== COMPILACIÓN ==="

mvn clean compile

echo
echo "============================================================"
echo " COMMERCE CLOUD GYM INSTALADO"
echo "============================================================"

echo
echo "Respaldo:"
echo "$BACKUP_DIR"

echo
echo "Archivos Gym:"
find "$BASE_PACKAGE" -type f | sort

echo
echo "Migración:"
echo "$MIGRATION_DIR/V101__crear_modulo_gym.sql"

echo
echo "Bootstrap:"
grep -n \
    'commerce.bootstrap.gym.enabled' \
    "$PROPERTIES_FILE"

echo
echo "Estado Git:"
git status --short
