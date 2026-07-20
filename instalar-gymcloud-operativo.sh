#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT="/home/tonatiuh/commerce-cloud"
JAVA_BASE="src/main/java/com/abarrote/abarroteapi"
GYM_BASE="$JAVA_BASE/gym"
TEMPLATES="src/main/resources/templates/gym"
MIGRATIONS="src/main/resources/db/migration"

TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP=".backups/gymcloud-operativo-${TIMESTAMP}"

error_handler() {
    echo
    echo "============================================================"
    echo " ERROR INSTALANDO GYMCLOUD OPERATIVO"
    echo "============================================================"
    echo "Línea: $1"
    echo "Comando: $2"
}

trap 'error_handler "$LINENO" "$BASH_COMMAND"' ERR

cd "$PROJECT"

echo "============================================================"
echo " COMMERCE CLOUD"
echo " GYMCLOUD OPERATIVO"
echo " Productos + POS + Clientes + Membresías + Pagos"
echo "============================================================"

test -f pom.xml
test -d "$GYM_BASE"
test -f "$JAVA_BASE/security/CommerceUserPrincipal.java"
test -f "$GYM_BASE/domain/ClienteGym.java"
test -f "$GYM_BASE/domain/Membresia.java"

mkdir -p \
    "$BACKUP" \
    "$GYM_BASE/domain" \
    "$GYM_BASE/dto" \
    "$GYM_BASE/repository" \
    "$GYM_BASE/service" \
    "$GYM_BASE/web" \
    "$TEMPLATES" \
    "$MIGRATIONS"

if [ -d "$GYM_BASE" ]; then
    cp -R "$GYM_BASE" "$BACKUP/"
fi

if [ -d "$TEMPLATES" ]; then
    cp -R "$TEMPLATES" "$BACKUP/templates-gym"
fi

echo
echo "[1/12] Creando migración V104..."

cat > "$MIGRATIONS/V104__crear_productos_y_ventas_gym.sql" <<'EOF'
CREATE TABLE IF NOT EXISTS gym_producto (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    codigo VARCHAR(50) NOT NULL,

    nombre VARCHAR(150) NOT NULL,

    descripcion VARCHAR(500),

    categoria VARCHAR(100),

    precio NUMERIC(12,2) NOT NULL,

    costo NUMERIC(12,2),

    existencia INTEGER NOT NULL DEFAULT 0,

    stock_minimo INTEGER NOT NULL DEFAULT 0,

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_creacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_gym_producto_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT uk_gym_producto_tenant_codigo
        UNIQUE (tenant_id, codigo),

    CONSTRAINT ck_gym_producto_precio
        CHECK (precio >= 0),

    CONSTRAINT ck_gym_producto_costo
        CHECK (costo IS NULL OR costo >= 0),

    CONSTRAINT ck_gym_producto_existencia
        CHECK (existencia >= 0),

    CONSTRAINT ck_gym_producto_stock_minimo
        CHECK (stock_minimo >= 0)
);

CREATE INDEX IF NOT EXISTS idx_gym_producto_tenant
    ON gym_producto(tenant_id);

CREATE INDEX IF NOT EXISTS idx_gym_producto_nombre
    ON gym_producto(tenant_id, nombre);

CREATE INDEX IF NOT EXISTS idx_gym_producto_categoria
    ON gym_producto(tenant_id, categoria);

CREATE TABLE IF NOT EXISTS gym_venta (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    usuario_username VARCHAR(50) NOT NULL,

    total NUMERIC(12,2) NOT NULL,

    metodo_pago VARCHAR(30) NOT NULL,

    fecha_venta TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_gym_venta_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT ck_gym_venta_total
        CHECK (total >= 0),

    CONSTRAINT ck_gym_venta_metodo_pago
        CHECK (
            metodo_pago IN (
                'EFECTIVO',
                'TARJETA',
                'TRANSFERENCIA',
                'OTRO'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_gym_venta_tenant
    ON gym_venta(tenant_id);

CREATE INDEX IF NOT EXISTS idx_gym_venta_fecha
    ON gym_venta(tenant_id, fecha_venta);

CREATE TABLE IF NOT EXISTS gym_venta_detalle (
    id BIGSERIAL PRIMARY KEY,

    venta_id BIGINT NOT NULL,

    producto_id BIGINT NOT NULL,

    cantidad INTEGER NOT NULL,

    precio_unitario NUMERIC(12,2) NOT NULL,

    subtotal NUMERIC(12,2) NOT NULL,

    CONSTRAINT fk_gym_detalle_venta
        FOREIGN KEY (venta_id)
        REFERENCES gym_venta(id),

    CONSTRAINT fk_gym_detalle_producto
        FOREIGN KEY (producto_id)
        REFERENCES gym_producto(id),

    CONSTRAINT ck_gym_detalle_cantidad
        CHECK (cantidad > 0),

    CONSTRAINT ck_gym_detalle_precio
        CHECK (precio_unitario >= 0),

    CONSTRAINT ck_gym_detalle_subtotal
        CHECK (subtotal >= 0)
);

CREATE INDEX IF NOT EXISTS idx_gym_detalle_venta
    ON gym_venta_detalle(venta_id);

CREATE INDEX IF NOT EXISTS idx_gym_detalle_producto
    ON gym_venta_detalle(producto_id);
EOF

echo
echo "[2/12] Creando entidad GymProducto..."

cat > "$GYM_BASE/domain/GymProducto.java" <<'EOF'
package com.abarrote.abarroteapi.gym.domain;

import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gym_producto")
public class GymProducto {

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
        nullable = false,
        length = 50
    )
    private String codigo;

    @Column(
        nullable = false,
        length = 150
    )
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 100)
    private String categoria;

    @Column(
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal precio;

    @Column(
        precision = 12,
        scale = 2
    )
    private BigDecimal costo;

    @Column(nullable = false)
    private Integer existencia;

    @Column(
        name = "stock_minimo",
        nullable = false
    )
    private Integer stockMinimo;

    @Column(nullable = false)
    private Boolean activo;

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

        if (existencia == null) {
            existencia = 0;
        }

        if (stockMinimo == null) {
            stockMinimo = 0;
        }

        if (activo == null) {
            activo = true;
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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public Integer getExistencia() {
        return existencia;
    }

    public void setExistencia(Integer existencia) {
        this.existencia = existencia;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public boolean isStockBajo() {
        return existencia != null
            && stockMinimo != null
            && existencia <= stockMinimo;
    }
}
EOF

echo
echo "[3/12] Creando entidades de venta..."

cat > "$GYM_BASE/domain/GymVenta.java" <<'EOF'
package com.abarrote.abarroteapi.gym.domain;

import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gym_venta")
public class GymVenta {

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
        name = "usuario_username",
        nullable = false,
        length = 50
    )
    private String usuarioUsername;

    @Column(
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal total;

    @Column(
        name = "metodo_pago",
        nullable = false,
        length = 30
    )
    private String metodoPago;

    @Column(
        name = "fecha_venta",
        nullable = false,
        updatable = false
    )
    private LocalDateTime fechaVenta;

    @OneToMany(
        mappedBy = "venta",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<GymVentaDetalle> detalles =
        new ArrayList<>();

    @PrePersist
    public void prePersist() {
        fechaVenta = LocalDateTime.now();
    }

    public void agregarDetalle(
        GymVentaDetalle detalle
    ) {
        detalle.setVenta(this);
        detalles.add(detalle);
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

    public String getUsuarioUsername() {
        return usuarioUsername;
    }

    public void setUsuarioUsername(
        String usuarioUsername
    ) {
        this.usuarioUsername = usuarioUsername;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public List<GymVentaDetalle> getDetalles() {
        return detalles;
    }
}
EOF

cat > "$GYM_BASE/domain/GymVentaDetalle.java" <<'EOF'
package com.abarrote.abarroteapi.gym.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "gym_venta_detalle")
public class GymVentaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "venta_id",
        nullable = false
    )
    private GymVenta venta;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "producto_id",
        nullable = false
    )
    private GymProducto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(
        name = "precio_unitario",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal precioUnitario;

    @Column(
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal subtotal;

    public Long getId() {
        return id;
    }

    public GymVenta getVenta() {
        return venta;
    }

    public void setVenta(GymVenta venta) {
        this.venta = venta;
    }

    public GymProducto getProducto() {
        return producto;
    }

    public void setProducto(GymProducto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(
        BigDecimal precioUnitario
    ) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
EOF

echo
echo "[4/12] Creando repositorios..."

cat > "$GYM_BASE/repository/GymProductoRepository.java" <<'EOF'
package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.GymProducto;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
EOF

cat > "$GYM_BASE/repository/GymVentaRepository.java" <<'EOF'
package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.GymVenta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GymVentaRepository
    extends JpaRepository<GymVenta, Long> {

    @EntityGraph(
        attributePaths = {
            "detalles",
            "detalles.producto"
        }
    )
    List<GymVenta>
        findTop50ByTenantIdOrderByFechaVentaDesc(
            Long tenantId
        );

    long countByTenantId(Long tenantId);
}
EOF

echo
echo "[5/12] Ampliando repositorio de membresías..."

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
echo "[6/12] Creando DTO de venta..."

cat > "$GYM_BASE/dto/GymVentaRequest.java" <<'EOF'
package com.abarrote.abarroteapi.gym.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GymVentaRequest(

    @NotEmpty
    List<@Valid Item> productos,

    @NotNull
    @Size(max = 30)
    String metodoPago
) {

    public record Item(

        @NotNull
        Long productoId,

        @NotNull
        @Positive
        Integer cantidad
    ) {
    }
}
EOF

cat > "$GYM_BASE/dto/GymVentaResponse.java" <<'EOF'
package com.abarrote.abarroteapi.gym.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GymVentaResponse(
    Long ventaId,
    BigDecimal total,
    String metodoPago,
    LocalDateTime fechaVenta
) {
}
EOF

echo
echo "[7/12] Creando servicio comercial Gym..."

cat > "$GYM_BASE/service/GymComercialService.java" <<'EOF'
package com.abarrote.abarroteapi.gym.service;

import com.abarrote.abarroteapi.gym.domain.GymProducto;
import com.abarrote.abarroteapi.gym.domain.GymVenta;
import com.abarrote.abarroteapi.gym.domain.GymVentaDetalle;
import com.abarrote.abarroteapi.gym.dto.GymVentaRequest;
import com.abarrote.abarroteapi.gym.dto.GymVentaResponse;
import com.abarrote.abarroteapi.gym.repository.GymProductoRepository;
import com.abarrote.abarroteapi.gym.repository.GymVentaRepository;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class GymComercialService {

    private final GymProductoRepository productoRepository;

    private final GymVentaRepository ventaRepository;

    private final TenantRepository tenantRepository;

    public GymComercialService(
        GymProductoRepository productoRepository,
        GymVentaRepository ventaRepository,
        TenantRepository tenantRepository
    ) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.tenantRepository = tenantRepository;
    }

    public GymProducto crearProducto(
        Long tenantId,
        String codigo,
        String nombre,
        String descripcion,
        String categoria,
        BigDecimal precio,
        BigDecimal costo,
        Integer existencia,
        Integer stockMinimo
    ) {
        Tenant tenant = obtenerTenant(tenantId);

        String codigoNormalizado =
            codigo.trim().toUpperCase(Locale.ROOT);

        if (
            productoRepository
                .existsByTenantIdAndCodigoIgnoreCase(
                    tenantId,
                    codigoNormalizado
                )
        ) {
            throw new IllegalArgumentException(
                "Ya existe un producto con el código "
                    + codigoNormalizado
            );
        }

        GymProducto producto = new GymProducto();

        producto.setTenant(tenant);
        producto.setCodigo(codigoNormalizado);
        producto.setNombre(nombre.trim());
        producto.setDescripcion(descripcion);
        producto.setCategoria(categoria);
        producto.setPrecio(precio);
        producto.setCosto(costo);
        producto.setExistencia(
            existencia != null ? existencia : 0
        );
        producto.setStockMinimo(
            stockMinimo != null ? stockMinimo : 0
        );
        producto.setActivo(true);

        return productoRepository.save(producto);
    }

    public GymProducto actualizarExistencia(
        Long tenantId,
        Long productoId,
        Integer nuevaExistencia
    ) {
        if (nuevaExistencia == null || nuevaExistencia < 0) {
            throw new IllegalArgumentException(
                "La existencia no puede ser negativa"
            );
        }

        GymProducto producto = productoRepository
            .findByIdAndTenantId(
                productoId,
                tenantId
            )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Producto no encontrado"
                )
            );

        producto.setExistencia(nuevaExistencia);

        return productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public List<GymProducto> listarProductos(
        Long tenantId
    ) {
        return productoRepository
            .findAllByTenantIdAndActivoTrueOrderByNombreAsc(
                tenantId
            );
    }

    public GymVentaResponse registrarVenta(
        Long tenantId,
        String username,
        GymVentaRequest request
    ) {
        Tenant tenant = obtenerTenant(tenantId);

        if (
            request.productos() == null
                || request.productos().isEmpty()
        ) {
            throw new IllegalArgumentException(
                "La venta no contiene productos"
            );
        }

        String metodoPago = request
            .metodoPago()
            .trim()
            .toUpperCase(Locale.ROOT);

        if (
            !List.of(
                "EFECTIVO",
                "TARJETA",
                "TRANSFERENCIA",
                "OTRO"
            ).contains(metodoPago)
        ) {
            throw new IllegalArgumentException(
                "Método de pago no válido"
            );
        }

        GymVenta venta = new GymVenta();

        venta.setTenant(tenant);
        venta.setUsuarioUsername(username);
        venta.setMetodoPago(metodoPago);

        BigDecimal total = BigDecimal.ZERO;

        for (GymVentaRequest.Item item
            : request.productos()) {

            GymProducto producto =
                productoRepository
                    .findByIdAndTenantId(
                        item.productoId(),
                        tenantId
                    )
                    .orElseThrow(
                        () ->
                            new IllegalArgumentException(
                                "Producto no encontrado: "
                                    + item.productoId()
                            )
                    );

            int cantidad = item.cantidad();

            if (cantidad <= 0) {
                throw new IllegalArgumentException(
                    "Cantidad no válida"
                );
            }

            if (producto.getExistencia() < cantidad) {
                throw new IllegalArgumentException(
                    "Existencia insuficiente para "
                        + producto.getNombre()
                );
            }

            BigDecimal subtotal =
                producto
                    .getPrecio()
                    .multiply(
                        BigDecimal.valueOf(cantidad)
                    );

            GymVentaDetalle detalle =
                new GymVentaDetalle();

            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(
                producto.getPrecio()
            );
            detalle.setSubtotal(subtotal);

            venta.agregarDetalle(detalle);

            producto.setExistencia(
                producto.getExistencia() - cantidad
            );

            productoRepository.save(producto);

            total = total.add(subtotal);
        }

        venta.setTotal(total);

        GymVenta guardada =
            ventaRepository.save(venta);

        return new GymVentaResponse(
            guardada.getId(),
            guardada.getTotal(),
            guardada.getMetodoPago(),
            guardada.getFechaVenta()
        );
    }

    @Transactional(readOnly = true)
    public List<GymVenta> listarVentas(
        Long tenantId
    ) {
        return ventaRepository
            .findTop50ByTenantIdOrderByFechaVentaDesc(
                tenantId
            );
    }

    private Tenant obtenerTenant(Long tenantId) {
        return tenantRepository
            .findById(tenantId)
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Tenant no encontrado"
                )
            );
    }
}
EOF

echo
echo "[8/12] Creando controlador Gym Cloud..."

cat > "$GYM_BASE/web/GymCloudController.java" <<'EOF'
package com.abarrote.abarroteapi.gym.web;

import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;
import com.abarrote.abarroteapi.gym.dto.ClienteGymRequest;
import com.abarrote.abarroteapi.gym.dto.GymVentaRequest;
import com.abarrote.abarroteapi.gym.dto.GymVentaResponse;
import com.abarrote.abarroteapi.gym.dto.MembresiaRequest;
import com.abarrote.abarroteapi.gym.dto.PagoMembresiaRequest;
import com.abarrote.abarroteapi.gym.repository.ClienteGymRepository;
import com.abarrote.abarroteapi.gym.repository.GymProductoRepository;
import com.abarrote.abarroteapi.gym.repository.GymVentaRepository;
import com.abarrote.abarroteapi.gym.repository.MembresiaRepository;
import com.abarrote.abarroteapi.gym.repository.PlanMembresiaRepository;
import com.abarrote.abarroteapi.gym.service.GymComercialService;
import com.abarrote.abarroteapi.gym.service.GymService;
import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
public class GymCloudController {

    private final GymComercialService comercialService;

    private final GymService gymService;

    private final GymProductoRepository productoRepository;

    private final GymVentaRepository ventaRepository;

    private final ClienteGymRepository clienteRepository;

    private final PlanMembresiaRepository planRepository;

    private final MembresiaRepository membresiaRepository;

    public GymCloudController(
        GymComercialService comercialService,
        GymService gymService,
        GymProductoRepository productoRepository,
        GymVentaRepository ventaRepository,
        ClienteGymRepository clienteRepository,
        PlanMembresiaRepository planRepository,
        MembresiaRepository membresiaRepository
    ) {
        this.comercialService = comercialService;
        this.gymService = gymService;
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.planRepository = planRepository;
        this.membresiaRepository = membresiaRepository;
    }

    @GetMapping("/gym/dashboard")
    public String dashboard(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        validarGym(principal);

        Long tenantId = principal.getTenantId();

        model.addAttribute(
            "principal",
            principal
        );

        model.addAttribute(
            "totalProductos",
            productoRepository
                .countByTenantIdAndActivoTrue(
                    tenantId
                )
        );

        model.addAttribute(
            "totalVentas",
            ventaRepository
                .countByTenantId(tenantId)
        );

        model.addAttribute(
            "totalClientes",
            clienteRepository
                .findAllByTenantIdOrderByNombreAsc(
                    tenantId
                )
                .size()
        );

        model.addAttribute(
            "membresiasActivas",
            membresiaRepository
                .countByTenantIdAndEstado(
                    tenantId,
                    EstadoMembresia.ACTIVA
                )
        );

        model.addAttribute(
            "membresiasVencidas",
            membresiaRepository
                .countByTenantIdAndFechaFinBeforeAndEstado(
                    tenantId,
                    LocalDate.now(),
                    EstadoMembresia.ACTIVA
                )
        );

        return "gym/dashboard";
    }

    @GetMapping("/gym/productos")
    public String productos(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        validarGym(principal);

        model.addAttribute(
            "productos",
            comercialService.listarProductos(
                principal.getTenantId()
            )
        );

        return "gym/productos";
    }

    @PostMapping("/gym/productos")
    public String crearProducto(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,

        @RequestParam String codigo,

        @RequestParam String nombre,

        @RequestParam(required = false)
        String descripcion,

        @RequestParam(required = false)
        String categoria,

        @RequestParam BigDecimal precio,

        @RequestParam(required = false)
        BigDecimal costo,

        @RequestParam(defaultValue = "0")
        Integer existencia,

        @RequestParam(defaultValue = "0")
        Integer stockMinimo
    ) {
        validarGym(principal);

        comercialService.crearProducto(
            principal.getTenantId(),
            codigo,
            nombre,
            descripcion,
            categoria,
            precio,
            costo,
            existencia,
            stockMinimo
        );

        return "redirect:/gym/productos?creado";
    }

    @PostMapping("/gym/productos/existencia")
    public String actualizarExistencia(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,

        @RequestParam Long productoId,

        @RequestParam Integer existencia
    ) {
        validarGym(principal);

        comercialService.actualizarExistencia(
            principal.getTenantId(),
            productoId,
            existencia
        );

        return "redirect:/gym/productos?existenciaActualizada";
    }

    @GetMapping("/gym/pos")
    public String pos(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        validarGym(principal);

        model.addAttribute(
            "productos",
            comercialService.listarProductos(
                principal.getTenantId()
            )
        );

        return "gym/pos";
    }

    @PostMapping("/gym/api/ventas")
    @ResponseBody
    public ResponseEntity<GymVentaResponse>
        registrarVenta(
            @AuthenticationPrincipal
            CommerceUserPrincipal principal,

            @Valid
            @RequestBody GymVentaRequest request
        ) {

        validarGym(principal);

        GymVentaResponse response =
            comercialService.registrarVenta(
                principal.getTenantId(),
                principal.getUsername(),
                request
            );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping("/gym/ventas")
    public String ventas(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        validarGym(principal);

        model.addAttribute(
            "ventas",
            comercialService.listarVentas(
                principal.getTenantId()
            )
        );

        return "gym/ventas";
    }

    @GetMapping("/gym/clientes")
    public String clientes(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        validarGym(principal);

        model.addAttribute(
            "clientes",
            clienteRepository
                .findAllByTenantIdOrderByNombreAsc(
                    principal.getTenantId()
                )
        );

        return "gym/clientes";
    }

    @PostMapping("/gym/clientes")
    public String crearCliente(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,

        @RequestParam String numeroCliente,

        @RequestParam String nombre,

        @RequestParam(required = false)
        String apellidoPaterno,

        @RequestParam(required = false)
        String apellidoMaterno,

        @RequestParam(required = false)
        String telefono,

        @RequestParam(required = false)
        String email,

        @RequestParam(required = false)
        LocalDate fechaNacimiento,

        @RequestParam(required = false)
        String contactoEmergencia,

        @RequestParam(required = false)
        String telefonoEmergencia
    ) {
        validarGym(principal);

        gymService.crearCliente(
            principal.getTenantSlug(),
            new ClienteGymRequest(
                numeroCliente,
                nombre,
                apellidoPaterno,
                apellidoMaterno,
                telefono,
                email,
                fechaNacimiento,
                contactoEmergencia,
                telefonoEmergencia
            )
        );

        return "redirect:/gym/clientes?creado";
    }

    @GetMapping("/gym/membresias")
    public String membresias(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        validarGym(principal);

        Long tenantId = principal.getTenantId();

        model.addAttribute(
            "clientes",
            clienteRepository
                .findAllByTenantIdOrderByNombreAsc(
                    tenantId
                )
        );

        model.addAttribute(
            "planes",
            planRepository
                .findAllByTenantIdAndActivoTrueOrderByPrecioAsc(
                    tenantId
                )
        );

        model.addAttribute(
            "membresias",
            membresiaRepository
                .findAllByTenantIdOrderByFechaCreacionDesc(
                    tenantId
                )
        );

        return "gym/membresias";
    }

    @PostMapping("/gym/membresias")
    public String crearMembresia(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,

        @RequestParam Long clienteId,

        @RequestParam Long planId,

        @RequestParam(required = false)
        LocalDate fechaInicio
    ) {
        validarGym(principal);

        gymService.crearMembresia(
            principal.getTenantSlug(),
            new MembresiaRequest(
                clienteId,
                planId,
                fechaInicio
            )
        );

        return "redirect:/gym/membresias?creada";
    }

    @PostMapping("/gym/membresias/pagar")
    public String pagarMembresia(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,

        @RequestParam Long membresiaId,

        @RequestParam BigDecimal importe,

        @RequestParam
        com.abarrote.abarroteapi.gym.domain.MetodoPagoGym
            metodoPago,

        @RequestParam(required = false)
        String referencia
    ) {
        validarGym(principal);

        gymService.registrarPago(
            principal.getTenantSlug(),
            new PagoMembresiaRequest(
                membresiaId,
                importe,
                metodoPago,
                referencia,
                "Pago registrado desde Gym Cloud"
            )
        );

        return "redirect:/gym/membresias?pagoRegistrado";
    }

    private void validarGym(
        CommerceUserPrincipal principal
    ) {
        if (principal == null) {
            throw new IllegalStateException(
                "Sesión no disponible"
            );
        }

        if (!principal.esGym()) {
            throw new IllegalArgumentException(
                "El usuario no pertenece a Gym Cloud"
            );
        }
    }
}
EOF

echo
echo "[9/12] Eliminando controlador Dashboard duplicado..."

rm -f \
    "$GYM_BASE/web/GymDashboardController.java"

echo
echo "[10/12] Creando plantilla base Gym Cloud..."

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
            padding: 2rem;
        }

        .cards {
            display: grid;
            grid-template-columns:
                repeat(auto-fit, minmax(210px, 1fr));
            gap: 1rem;
            margin-top: 2rem;
        }

        .card {
            padding: 1.4rem;
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

        @media (max-width: 760px) {
            .layout {
                display: block;
            }

            .sidebar {
                border-right: none;
                border-bottom: 1px solid #27272a;
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
                Membresías vencidas
                <strong
                    th:text="${membresiasVencidas}">
                    0
                </strong>
            </article>

        </section>

    </main>

</div>

</body>
</html>
EOF

echo
echo "[11/12] Creando páginas operativas..."

cat > "$TEMPLATES/productos.html" <<'EOF'
<!DOCTYPE html>
<html
    lang="es"
    xmlns:th="http://www.thymeleaf.org">

<head>
    <meta charset="UTF-8">
    <title>Productos | Gym Cloud</title>

    <style>
        body {
            margin: 0;
            padding: 2rem;
            font-family: sans-serif;
            color: #f8fafc;
            background: #09090b;
        }

        a {
            color: #c084fc;
        }

        form,
        table {
            width: 100%;
            margin-top: 1rem;
            padding: 1rem;
            border-radius: 12px;
            background: #18181b;
        }

        input {
            margin: 0.3rem;
            padding: 0.7rem;
            border: 1px solid #52525b;
            border-radius: 8px;
            color: white;
            background: #27272a;
        }

        button {
            padding: 0.75rem 1rem;
            border: none;
            border-radius: 8px;
            font-weight: bold;
            cursor: pointer;
        }

        th,
        td {
            padding: 0.7rem;
            border-bottom: 1px solid #3f3f46;
            text-align: left;
        }

        .bajo {
            color: #fca5a5;
        }
    </style>
</head>

<body>

<a th:href="@{/gym/dashboard}">
    ← Regresar al dashboard
</a>

<h1>Productos de Gym Cloud</h1>

<form
    method="post"
    th:action="@{/gym/productos}">

    <h2>Alta de producto</h2>

    <input
        name="codigo"
        placeholder="Código"
        required>

    <input
        name="nombre"
        placeholder="Nombre"
        required>

    <input
        name="categoria"
        placeholder="Categoría">

    <input
        name="descripcion"
        placeholder="Descripción">

    <input
        name="precio"
        type="number"
        step="0.01"
        min="0"
        placeholder="Precio"
        required>

    <input
        name="costo"
        type="number"
        step="0.01"
        min="0"
        placeholder="Costo">

    <input
        name="existencia"
        type="number"
        min="0"
        value="0"
        placeholder="Existencia">

    <input
        name="stockMinimo"
        type="number"
        min="0"
        value="0"
        placeholder="Stock mínimo">

    <button type="submit">
        Guardar producto
    </button>

</form>

<table>

    <thead>
    <tr>
        <th>Código</th>
        <th>Producto</th>
        <th>Categoría</th>
        <th>Precio</th>
        <th>Existencia</th>
        <th>Actualizar</th>
    </tr>
    </thead>

    <tbody>

    <tr th:each="producto : ${productos}">

        <td th:text="${producto.codigo}"></td>

        <td th:text="${producto.nombre}"></td>

        <td th:text="${producto.categoria}"></td>

        <td
            th:text="${'$' + producto.precio}">
        </td>

        <td
            th:classappend="${producto.stockBajo} ? 'bajo' : ''"
            th:text="${producto.existencia}">
        </td>

        <td>

            <form
                method="post"
                th:action="@{/gym/productos/existencia}">

                <input
                    type="hidden"
                    name="productoId"
                    th:value="${producto.id}">

                <input
                    type="number"
                    name="existencia"
                    min="0"
                    th:value="${producto.existencia}">

                <button type="submit">
                    Actualizar
                </button>

            </form>

        </td>

    </tr>

    </tbody>

</table>

</body>
</html>
EOF

cat > "$TEMPLATES/pos.html" <<'EOF'
<!DOCTYPE html>
<html
    lang="es"
    xmlns:th="http://www.thymeleaf.org">

<head>
    <meta charset="UTF-8">
    <title>POS | Gym Cloud</title>

    <style>
        body {
            margin: 0;
            padding: 2rem;
            font-family: sans-serif;
            color: white;
            background: #09090b;
        }

        a {
            color: #c084fc;
        }

        .grid {
            display: grid;
            grid-template-columns: 1.4fr 1fr;
            gap: 1.5rem;
            margin-top: 1.5rem;
        }

        .productos,
        .carrito {
            padding: 1rem;
            border-radius: 14px;
            background: #18181b;
        }

        .producto {
            margin-bottom: 0.7rem;
            padding: 0.8rem;
            border: 1px solid #3f3f46;
            border-radius: 10px;
        }

        button,
        select {
            padding: 0.7rem;
            border-radius: 8px;
        }

        button {
            border: none;
            font-weight: bold;
            cursor: pointer;
        }

        li {
            margin-bottom: 0.6rem;
        }

        @media (max-width: 760px) {
            .grid {
                display: block;
            }

            .carrito {
                margin-top: 1rem;
            }
        }
    </style>
</head>

<body>

<a th:href="@{/gym/dashboard}">
    ← Regresar al dashboard
</a>

<h1>Punto de venta Gym Cloud</h1>

<div class="grid">

    <section class="productos">

        <h2>Productos</h2>

        <article
            class="producto"
            th:each="producto : ${productos}">

            <strong
                th:text="${producto.nombre}">
            </strong>

            <p>
                Precio:
                <span
                    th:text="${'$' + producto.precio}">
                </span>
            </p>

            <p>
                Existencia:
                <span
                    th:text="${producto.existencia}">
                </span>
            </p>

            <button
                type="button"
                th:attr="
                    data-id=${producto.id},
                    data-nombre=${producto.nombre},
                    data-precio=${producto.precio},
                    data-existencia=${producto.existencia}
                "
                onclick="agregarDesdeBoton(this)">
                Agregar
            </button>

        </article>

    </section>

    <aside class="carrito">

        <h2>Ticket</h2>

        <ul id="ticket"></ul>

        <h3>
            Total:
            $<span id="total">0.00</span>
        </h3>

        <select id="metodoPago">
            <option value="EFECTIVO">
                Efectivo
            </option>
            <option value="TARJETA">
                Tarjeta
            </option>
            <option value="TRANSFERENCIA">
                Transferencia
            </option>
        </select>

        <button
            type="button"
            onclick="cobrar()">
            Cobrar
        </button>

        <p id="mensaje"></p>

    </aside>

</div>

<script>
    const carrito = [];

    function agregarDesdeBoton(boton) {
        const producto = {
            id: Number(boton.dataset.id),
            nombre: boton.dataset.nombre,
            precio: Number(boton.dataset.precio),
            existencia: Number(
                boton.dataset.existencia
            )
        };

        const existente = carrito.find(
            item => item.productoId === producto.id
        );

        if (existente) {
            if (
                existente.cantidad
                    >= producto.existencia
            ) {
                alert('No hay más existencia disponible');
                return;
            }

            existente.cantidad++;
        } else {
            if (producto.existencia <= 0) {
                alert('Producto sin existencia');
                return;
            }

            carrito.push({
                productoId: producto.id,
                nombre: producto.nombre,
                precio: producto.precio,
                cantidad: 1,
                existencia: producto.existencia
            });
        }

        renderTicket();
    }

    function cambiarCantidad(index, cambio) {
        const item = carrito[index];

        const nuevaCantidad =
            item.cantidad + cambio;

        if (nuevaCantidad <= 0) {
            carrito.splice(index, 1);
        } else if (
            nuevaCantidad <= item.existencia
        ) {
            item.cantidad = nuevaCantidad;
        }

        renderTicket();
    }

    function renderTicket() {
        const lista =
            document.getElementById('ticket');

        lista.innerHTML = '';

        let total = 0;

        carrito.forEach((item, index) => {
            const subtotal =
                item.precio * item.cantidad;

            total += subtotal;

            const li =
                document.createElement('li');

            li.innerHTML = `
                ${item.nombre}
                x ${item.cantidad}
                = $${subtotal.toFixed(2)}
                <button onclick="cambiarCantidad(${index}, 1)">
                    +
                </button>
                <button onclick="cambiarCantidad(${index}, -1)">
                    -
                </button>
            `;

            lista.appendChild(li);
        });

        document.getElementById('total')
            .textContent = total.toFixed(2);
    }

    async function cobrar() {
        const mensaje =
            document.getElementById('mensaje');

        if (carrito.length === 0) {
            mensaje.textContent =
                'Agrega productos al ticket.';
            return;
        }

        mensaje.textContent =
            'Procesando venta...';

        const payload = {
            productos: carrito.map(item => ({
                productoId: item.productoId,
                cantidad: item.cantidad
            })),
            metodoPago:
                document.getElementById(
                    'metodoPago'
                ).value
        };

        try {
            const response = await fetch(
                '/gym/api/ventas',
                {
                    method: 'POST',
                    headers: {
                        'Content-Type':
                            'application/json'
                    },
                    body: JSON.stringify(payload)
                }
            );

            if (!response.ok) {
                const text = await response.text();

                throw new Error(
                    text || 'No fue posible registrar la venta'
                );
            }

            const venta = await response.json();

            mensaje.textContent =
                `Venta ${venta.ventaId} registrada. `
                + `Total: $${Number(venta.total).toFixed(2)}`;

            carrito.length = 0;

            renderTicket();

            setTimeout(
                () => window.location.reload(),
                1300
            );
        } catch (error) {
            mensaje.textContent =
                'Error: ' + error.message;
        }
    }
</script>

</body>
</html>
EOF

cat > "$TEMPLATES/clientes.html" <<'EOF'
<!DOCTYPE html>
<html
    lang="es"
    xmlns:th="http://www.thymeleaf.org">

<head>
    <meta charset="UTF-8">
    <title>Clientes | Gym Cloud</title>

    <style>
        body {
            margin: 0;
            padding: 2rem;
            font-family: sans-serif;
            color: white;
            background: #09090b;
        }

        a {
            color: #c084fc;
        }

        form,
        table {
            width: 100%;
            margin-top: 1rem;
            padding: 1rem;
            border-radius: 12px;
            background: #18181b;
        }

        input {
            margin: 0.3rem;
            padding: 0.7rem;
            border: 1px solid #52525b;
            border-radius: 8px;
            color: white;
            background: #27272a;
        }

        button {
            padding: 0.75rem 1rem;
            border: none;
            border-radius: 8px;
            font-weight: bold;
        }

        th,
        td {
            padding: 0.7rem;
            border-bottom: 1px solid #3f3f46;
            text-align: left;
        }
    </style>
</head>

<body>

<a th:href="@{/gym/dashboard}">
    ← Regresar al dashboard
</a>

<h1>Clientes del gimnasio</h1>

<form
    method="post"
    th:action="@{/gym/clientes}">

    <h2>Registrar cliente</h2>

    <input
        name="numeroCliente"
        placeholder="Número de cliente"
        required>

    <input
        name="nombre"
        placeholder="Nombre"
        required>

    <input
        name="apellidoPaterno"
        placeholder="Apellido paterno">

    <input
        name="apellidoMaterno"
        placeholder="Apellido materno">

    <input
        name="telefono"
        placeholder="Teléfono">

    <input
        name="email"
        type="email"
        placeholder="Correo">

    <input
        name="fechaNacimiento"
        type="date">

    <input
        name="contactoEmergencia"
        placeholder="Contacto de emergencia">

    <input
        name="telefonoEmergencia"
        placeholder="Teléfono de emergencia">

    <button type="submit">
        Registrar cliente
    </button>

</form>

<table>

    <thead>
    <tr>
        <th>Número</th>
        <th>Nombre</th>
        <th>Teléfono</th>
        <th>Correo</th>
        <th>Estado</th>
    </tr>
    </thead>

    <tbody>

    <tr th:each="cliente : ${clientes}">

        <td th:text="${cliente.numeroCliente}"></td>

        <td>
            <span th:text="${cliente.nombre}"></span>
            <span th:text="${cliente.apellidoPaterno}"></span>
            <span th:text="${cliente.apellidoMaterno}"></span>
        </td>

        <td th:text="${cliente.telefono}"></td>

        <td th:text="${cliente.email}"></td>

        <td th:text="${cliente.estado}"></td>

    </tr>

    </tbody>

</table>

</body>
</html>
EOF

cat > "$TEMPLATES/membresias.html" <<'EOF'
<!DOCTYPE html>
<html
    lang="es"
    xmlns:th="http://www.thymeleaf.org">

<head>
    <meta charset="UTF-8">
    <title>Membresías | Gym Cloud</title>

    <style>
        body {
            margin: 0;
            padding: 2rem;
            font-family: sans-serif;
            color: white;
            background: #09090b;
        }

        a {
            color: #c084fc;
        }

        form,
        table {
            width: 100%;
            margin-top: 1rem;
            padding: 1rem;
            border-radius: 12px;
            background: #18181b;
        }

        input,
        select {
            margin: 0.3rem;
            padding: 0.7rem;
            border: 1px solid #52525b;
            border-radius: 8px;
            color: white;
            background: #27272a;
        }

        button {
            padding: 0.75rem 1rem;
            border: none;
            border-radius: 8px;
            font-weight: bold;
        }

        th,
        td {
            padding: 0.7rem;
            border-bottom: 1px solid #3f3f46;
            text-align: left;
        }
    </style>
</head>

<body>

<a th:href="@{/gym/dashboard}">
    ← Regresar al dashboard
</a>

<h1>Membresías y pagos</h1>

<form
    method="post"
    th:action="@{/gym/membresias}">

    <h2>Asignar membresía</h2>

    <select
        name="clienteId"
        required>

        <option value="">
            Selecciona cliente
        </option>

        <option
            th:each="cliente : ${clientes}"
            th:value="${cliente.id}"
            th:text="${cliente.numeroCliente + ' - ' + cliente.nombre}">
        </option>

    </select>

    <select
        name="planId"
        required>

        <option value="">
            Selecciona plan
        </option>

        <option
            th:each="plan : ${planes}"
            th:value="${plan.id}"
            th:text="${plan.nombre + ' - $' + plan.precio}">
        </option>

    </select>

    <input
        name="fechaInicio"
        type="date">

    <button type="submit">
        Crear membresía
    </button>

</form>

<table>

    <thead>
    <tr>
        <th>Cliente</th>
        <th>Plan</th>
        <th>Inicio</th>
        <th>Fin</th>
        <th>Precio</th>
        <th>Estado</th>
        <th>Registrar pago</th>
    </tr>
    </thead>

    <tbody>

    <tr th:each="membresia : ${membresias}">

        <td
            th:text="${membresia.cliente.nombre}">
        </td>

        <td
            th:text="${membresia.plan.nombre}">
        </td>

        <td
            th:text="${membresia.fechaInicio}">
        </td>

        <td
            th:text="${membresia.fechaFin}">
        </td>

        <td
            th:text="${'$' + membresia.precioContratado}">
        </td>

        <td
            th:text="${membresia.estado}">
        </td>

        <td>

            <form
                method="post"
                th:action="@{/gym/membresias/pagar}">

                <input
                    type="hidden"
                    name="membresiaId"
                    th:value="${membresia.id}">

                <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    name="importe"
                    th:value="${membresia.precioContratado}"
                    required>

                <select name="metodoPago">
                    <option value="EFECTIVO">
                        Efectivo
                    </option>
                    <option value="TARJETA">
                        Tarjeta
                    </option>
                    <option value="TRANSFERENCIA">
                        Transferencia
                    </option>
                </select>

                <input
                    name="referencia"
                    placeholder="Referencia">

                <button type="submit">
                    Registrar pago
                </button>

            </form>

        </td>

    </tr>

    </tbody>

</table>

</body>
</html>
EOF

cat > "$TEMPLATES/ventas.html" <<'EOF'
<!DOCTYPE html>
<html
    lang="es"
    xmlns:th="http://www.thymeleaf.org">

<head>
    <meta charset="UTF-8">
    <title>Ventas | Gym Cloud</title>

    <style>
        body {
            margin: 0;
            padding: 2rem;
            font-family: sans-serif;
            color: white;
            background: #09090b;
        }

        a {
            color: #c084fc;
        }

        table {
            width: 100%;
            margin-top: 1rem;
            border-collapse: collapse;
            background: #18181b;
        }

        th,
        td {
            padding: 0.8rem;
            border-bottom: 1px solid #3f3f46;
            text-align: left;
        }
    </style>
</head>

<body>

<a th:href="@{/gym/dashboard}">
    ← Regresar al dashboard
</a>

<h1>Últimas ventas</h1>

<table>

    <thead>
    <tr>
        <th>Folio</th>
        <th>Fecha</th>
        <th>Usuario</th>
        <th>Método</th>
        <th>Total</th>
    </tr>
    </thead>

    <tbody>

    <tr th:each="venta : ${ventas}">

        <td th:text="${venta.id}"></td>

        <td th:text="${venta.fechaVenta}"></td>

        <td th:text="${venta.usuarioUsername}"></td>

        <td th:text="${venta.metodoPago}"></td>

        <td th:text="${'$' + venta.total}"></td>

    </tr>

    </tbody>

</table>

</body>
</html>
EOF

echo
echo "[12/12] Validando y compilando..."

if grep -RniE \
    '^(<<<<<<<|=======|>>>>>>>)' \
    "$GYM_BASE" \
    "$TEMPLATES" \
    "$MIGRATIONS/V104__crear_productos_y_ventas_gym.sql"; then

    echo "ERROR: existen conflictos Git."
    exit 1
fi

git diff --check

mvn clean compile

echo
echo "============================================================"
echo " GYMCLOUD OPERATIVO INSTALADO"
echo "============================================================"

echo
echo "Módulos:"
echo "  /gym/dashboard"
echo "  /gym/productos"
echo "  /gym/pos"
echo "  /gym/ventas"
echo "  /gym/clientes"
echo "  /gym/membresias"

echo
echo "Migración:"
echo "  V104__crear_productos_y_ventas_gym.sql"

echo
echo "Respaldo:"
echo "  $BACKUP"

echo
echo "Estado Git:"
git status --short
