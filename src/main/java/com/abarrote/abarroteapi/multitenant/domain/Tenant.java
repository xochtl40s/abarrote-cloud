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
