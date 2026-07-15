package com.abarrote.abarroteapi.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "movimiento_inventario",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_movimiento_folio",
                        columnNames = "folio"
                )
        },
        indexes = {
                @Index(
                        name = "idx_movimiento_fecha",
                        columnList = "fecha_movimiento"
                ),
                @Index(
                        name = "idx_movimiento_producto",
                        columnList = "producto_id"
                ),
                @Index(
                        name = "idx_movimiento_origen",
                        columnList = "sucursal_origen_id"
                ),
                @Index(
                        name = "idx_movimiento_destino",
                        columnList = "sucursal_destino_id"
                )
        }
)
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            updatable = false,
            length = 36
    )
    private String folio;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "producto_id",
            nullable = false,
            updatable = false
    )
    private Producto producto;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "sucursal_origen_id",
            nullable = false,
            updatable = false
    )
    private Sucursal sucursalOrigen;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "sucursal_destino_id",
            nullable = false,
            updatable = false
    )
    private Sucursal sucursalDestino;

    @Column(
            nullable = false,
            updatable = false
    )
    private Integer cantidad;

    @Column(
            name = "existencia_origen_anterior",
            nullable = false,
            updatable = false
    )
    private Integer existenciaOrigenAnterior;

    @Column(
            name = "existencia_origen_nueva",
            nullable = false,
            updatable = false
    )
    private Integer existenciaOrigenNueva;

    @Column(
            name = "existencia_destino_anterior",
            nullable = false,
            updatable = false
    )
    private Integer existenciaDestinoAnterior;

    @Column(
            name = "existencia_destino_nueva",
            nullable = false,
            updatable = false
    )
    private Integer existenciaDestinoNueva;

    @Column(
            name = "usuario_responsable",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String usuarioResponsable;

    @Column(
            length = 500,
            updatable = false
    )
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            updatable = false,
            length = 20
    )
    private EstadoMovimiento estado;

    @Column(
            name = "fecha_movimiento",
            nullable = false,
            updatable = false
    )
    private LocalDateTime fechaMovimiento;

    public enum EstadoMovimiento {
        APLICADO,
        CANCELADO
    }

    public MovimientoInventario() {
    }

    @PrePersist
    public void prePersist() {

        if (folio == null || folio.isBlank()) {
            folio = UUID.randomUUID().toString();
        }

        if (fechaMovimiento == null) {
            fechaMovimiento = LocalDateTime.now();
        }

        if (estado == null) {
            estado = EstadoMovimiento.APLICADO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Sucursal getSucursalOrigen() {
        return sucursalOrigen;
    }

    public void setSucursalOrigen(
            Sucursal sucursalOrigen) {

        this.sucursalOrigen = sucursalOrigen;
    }

    public Sucursal getSucursalDestino() {
        return sucursalDestino;
    }

    public void setSucursalDestino(
            Sucursal sucursalDestino) {

        this.sucursalDestino = sucursalDestino;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getExistenciaOrigenAnterior() {
        return existenciaOrigenAnterior;
    }

    public void setExistenciaOrigenAnterior(
            Integer existenciaOrigenAnterior) {

        this.existenciaOrigenAnterior =
                existenciaOrigenAnterior;
    }

    public Integer getExistenciaOrigenNueva() {
        return existenciaOrigenNueva;
    }

    public void setExistenciaOrigenNueva(
            Integer existenciaOrigenNueva) {

        this.existenciaOrigenNueva =
                existenciaOrigenNueva;
    }

    public Integer getExistenciaDestinoAnterior() {
        return existenciaDestinoAnterior;
    }

    public void setExistenciaDestinoAnterior(
            Integer existenciaDestinoAnterior) {

        this.existenciaDestinoAnterior =
                existenciaDestinoAnterior;
    }

    public Integer getExistenciaDestinoNueva() {
        return existenciaDestinoNueva;
    }

    public void setExistenciaDestinoNueva(
            Integer existenciaDestinoNueva) {

        this.existenciaDestinoNueva =
                existenciaDestinoNueva;
    }

    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }

    public void setUsuarioResponsable(
            String usuarioResponsable) {

        this.usuarioResponsable =
                usuarioResponsable;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public EstadoMovimiento getEstado() {
        return estado;
    }

    public void setEstado(
            EstadoMovimiento estado) {

        this.estado = estado;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(
            LocalDateTime fechaMovimiento) {

        this.fechaMovimiento = fechaMovimiento;
    }
}
