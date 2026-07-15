package com.abarrote.abarroteapi.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "venta",
        indexes = {
                @Index(
                        name = "idx_venta_fecha_hora",
                        columnList = "fecha_hora"
                ),
                @Index(
                        name = "idx_venta_sucursal",
                        columnList = "sucursal_id"
                ),
                @Index(
                        name = "idx_venta_usuario",
                        columnList = "usuario_id"
                )
        }
)
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "fecha_hora",
            nullable = false
    )
    private LocalDateTime fechaHora;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal total;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "usuario_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_venta_usuario"
            )
    )
    private Usuario usuario;

    /*
     * Se deja nullable durante la transición.
     * El inicializador asignará a MAT las ventas históricas.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "sucursal_id",
            foreignKey = @ForeignKey(
                    name = "fk_venta_sucursal"
            )
    )
    private Sucursal sucursal;

    @OneToMany(
            mappedBy = "venta",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<DetalleVenta> detalles =
            new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private EstadoVenta estado =
            EstadoVenta.COMPLETADA;

    public enum EstadoVenta {
        COMPLETADA,
        CANCELADA
    }

    public Venta() {
    }

    @PrePersist
    public void prePersist() {

        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }

        if (estado == null) {
            estado = EstadoVenta.COMPLETADA;
        }

        if (total == null) {
            total = BigDecimal.ZERO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id) {

        this.id = id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(
            LocalDateTime fechaHora) {

        this.fechaHora = fechaHora;
    }

    public LocalDateTime getFecha() {
        return fechaHora;
    }

    public void setFecha(
            LocalDateTime fecha) {

        this.fechaHora = fecha;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(
            BigDecimal total) {

        this.total = total;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(
            Usuario usuario) {

        this.usuario = usuario;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(
            Sucursal sucursal) {

        this.sucursal = sucursal;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(
            List<DetalleVenta> detalles) {

        this.detalles =
                detalles != null
                        ? detalles
                        : new ArrayList<>();
    }

    public EstadoVenta getEstado() {
        return estado;
    }

    public void setEstado(
            EstadoVenta estado) {

        this.estado = estado;
    }

    public void agregarDetalle(
            DetalleVenta detalle) {

        if (detalle == null) {
            return;
        }

        detalles.add(detalle);
        detalle.setVenta(this);
    }

    public void removerDetalle(
            DetalleVenta detalle) {

        if (detalle == null) {
            return;
        }

        detalles.remove(detalle);
        detalle.setVenta(null);
    }
}
