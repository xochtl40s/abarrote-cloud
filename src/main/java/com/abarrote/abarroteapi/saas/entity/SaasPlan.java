package com.abarrote.abarroteapi.saas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "saas_plan",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_saas_plan_codigo",
            columnNames = "codigo"
        )
    }
)
public class SaasPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "codigo",
        nullable = false,
        length = 30
    )
    private String codigo;

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
        name = "precio_mensual",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal precioMensual;

    @Column(
        name = "precio_anual",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal precioAnual;

    @Column(name = "max_usuarios")
    private Integer maxUsuarios;

    @Column(name = "max_sucursales")
    private Integer maxSucursales;

    @Column(name = "max_mesas")
    private Integer maxMesas;

    @Column(
        name = "permite_reportes_avanzados",
        nullable = false
    )
    private Boolean permiteReportesAvanzados = Boolean.FALSE;

    @Column(
        name = "permite_asistente_ia",
        nullable = false
    )
    private Boolean permiteAsistenteIa = Boolean.FALSE;

    @Column(
        name = "permite_exportacion_excel",
        nullable = false
    )
    private Boolean permiteExportacionExcel = Boolean.FALSE;

    @Column(
        name = "permite_soporte_prioritario",
        nullable = false
    )
    private Boolean permiteSoportePrioritario = Boolean.FALSE;

    @Column(
        name = "dias_prueba",
        nullable = false
    )
    private Integer diasPrueba = 0;

    @Column(
        name = "activo",
        nullable = false
    )
    private Boolean activo = Boolean.TRUE;

    @Column(
        name = "orden_visual",
        nullable = false
    )
    private Integer ordenVisual = 0;

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

    public SaasPlan() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();

        if (fechaCreacion == null) {
            fechaCreacion = ahora;
        }

        fechaActualizacion = ahora;

        if (activo == null) {
            activo = Boolean.TRUE;
        }

        if (permiteReportesAvanzados == null) {
            permiteReportesAvanzados = Boolean.FALSE;
        }

        if (permiteAsistenteIa == null) {
            permiteAsistenteIa = Boolean.FALSE;
        }

        if (permiteExportacionExcel == null) {
            permiteExportacionExcel = Boolean.FALSE;
        }

        if (permiteSoportePrioritario == null) {
            permiteSoportePrioritario = Boolean.FALSE;
        }

        if (diasPrueba == null) {
            diasPrueba = 0;
        }

        if (ordenVisual == null) {
            ordenVisual = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = normalizarCodigo(codigo);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = limpiarTexto(nombre);
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = limpiarTexto(descripcion);
    }

    public BigDecimal getPrecioMensual() {
        return precioMensual;
    }

    public void setPrecioMensual(BigDecimal precioMensual) {
        this.precioMensual = precioMensual;
    }

    public BigDecimal getPrecioAnual() {
        return precioAnual;
    }

    public void setPrecioAnual(BigDecimal precioAnual) {
        this.precioAnual = precioAnual;
    }

    public Integer getMaxUsuarios() {
        return maxUsuarios;
    }

    public void setMaxUsuarios(Integer maxUsuarios) {
        this.maxUsuarios = maxUsuarios;
    }

    public Integer getMaxSucursales() {
        return maxSucursales;
    }

    public void setMaxSucursales(Integer maxSucursales) {
        this.maxSucursales = maxSucursales;
    }

    public Integer getMaxMesas() {
        return maxMesas;
    }

    public void setMaxMesas(Integer maxMesas) {
        this.maxMesas = maxMesas;
    }

    public Boolean getPermiteReportesAvanzados() {
        return permiteReportesAvanzados;
    }

    public void setPermiteReportesAvanzados(Boolean permiteReportesAvanzados) {
        this.permiteReportesAvanzados = permiteReportesAvanzados;
    }

    public Boolean getPermiteAsistenteIa() {
        return permiteAsistenteIa;
    }

    public void setPermiteAsistenteIa(Boolean permiteAsistenteIa) {
        this.permiteAsistenteIa = permiteAsistenteIa;
    }

    public Boolean getPermiteExportacionExcel() {
        return permiteExportacionExcel;
    }

    public void setPermiteExportacionExcel(Boolean permiteExportacionExcel) {
        this.permiteExportacionExcel = permiteExportacionExcel;
    }

    public Boolean getPermiteSoportePrioritario() {
        return permiteSoportePrioritario;
    }

    public void setPermiteSoportePrioritario(Boolean permiteSoportePrioritario) {
        this.permiteSoportePrioritario = permiteSoportePrioritario;
    }

    public Integer getDiasPrueba() {
        return diasPrueba;
    }

    public void setDiasPrueba(Integer diasPrueba) {
        this.diasPrueba = diasPrueba;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Integer getOrdenVisual() {
        return ordenVisual;
    }

    public void setOrdenVisual(Integer ordenVisual) {
        this.ordenVisual = ordenVisual;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    private String normalizarCodigo(String valor) {
        if (valor == null) {
            return null;
        }

        return valor.trim().toUpperCase();
    }

    private String limpiarTexto(String valor) {
        if (valor == null) {
            return null;
        }

        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }
}
