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
