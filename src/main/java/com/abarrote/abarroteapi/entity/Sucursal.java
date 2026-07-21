package com.abarrote.abarroteapi.entity;

import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
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

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
    name = "sucursal",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_sucursal_tenant_codigo",
            columnNames = {
                "tenant_id",
                "codigo"
            }
        )
    },
    indexes = {
        @Index(
            name = "idx_sucursal_tenant",
            columnList = "tenant_id"
        ),
        @Index(
            name = "idx_sucursal_tenant_activa",
            columnList = "tenant_id, activa"
        )
    }
)
public class Sucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "tenant_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_sucursal_tenant"
        )
    )
    private Tenant tenant;

    @Column(
        nullable = false,
        length = 20
    )
    private String codigo;

    @Column(
        nullable = false,
        length = 120
    )
    private String nombre;

    @Column(length = 250)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false)
    private Boolean activa = true;

    @Column(
        name = "fecha_creacion",
        nullable = false,
        updatable = false
    )
    private LocalDateTime fechaCreacion;

    public Sucursal() {
    }

    @PrePersist
    public void prePersist() {

        if (tenant == null) {
            throw new IllegalStateException(
                "La sucursal debe pertenecer a un tenant."
            );
        }

        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }

        if (activa == null) {
            activa = true;
        }

        normalizar();
    }

    @PreUpdate
    public void preUpdate() {

        if (tenant == null) {
            throw new IllegalStateException(
                "La sucursal debe pertenecer a un tenant."
            );
        }

        normalizar();
    }

    private void normalizar() {

        if (codigo != null) {
            codigo = codigo
                .trim()
                .toUpperCase();
        }

        if (nombre != null) {
            nombre = nombre.trim();
        }

        if (direccion != null) {
            direccion = direccion.trim();

            if (direccion.isBlank()) {
                direccion = null;
            }
        }

        if (telefono != null) {
            telefono = telefono.trim();

            if (telefono.isBlank()) {
                telefono = null;
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(
            LocalDateTime fechaCreacion) {

        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public boolean equals(Object objeto) {

        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Sucursal otra)) {
            return false;
        }

        return id != null
            && Objects.equals(id, otra.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
