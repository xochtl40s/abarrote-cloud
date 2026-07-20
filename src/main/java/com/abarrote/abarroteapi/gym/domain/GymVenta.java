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
