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
