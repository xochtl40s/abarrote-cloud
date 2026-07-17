package com.abarrote.abarroteapi.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "inventario_sucursal",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventario_sucursal_producto",
                        columnNames = {
                                "sucursal_id",
                                "producto_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_inventario_sucursal",
                        columnList = "sucursal_id"
                ),
                @Index(
                        name = "idx_inventario_producto",
                        columnList = "producto_id"
                )
        }
)
public class InventarioSucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "sucursal_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_inventario_sucursal"
            )
    )
    private Sucursal sucursal;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "producto_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_inventario_producto"
            )
    )
    private Producto producto;

    @Column(nullable = false)
    private Integer existencia = 0;

    @Column(
            name = "stock_minimo",
            nullable = false
    )
    private Integer stockMinimo = 5;

    @Column(
            name = "fecha_actualizacion",
            nullable = false
    )
    private LocalDateTime fechaActualizacion;

    @Version
    private Long version;

    public InventarioSucursal() {
    }

    @PrePersist
    public void prePersist() {

        validarExistencia();

        if (stockMinimo == null || stockMinimo < 0) {
            stockMinimo = 0;
        }

        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {

        validarExistencia();
        fechaActualizacion = LocalDateTime.now();
    }

    private void validarExistencia() {

        if (existencia == null) {
            existencia = 0;
        }

        if (existencia < 0) {
            throw new IllegalStateException(
                    "La existencia de una sucursal no puede ser negativa"
            );
        }
    }

    @Transient
    public boolean isStockBajo() {

        return existencia != null
                && stockMinimo != null
                && existencia <= stockMinimo;
    }

    @Transient
    public boolean isAgotado() {

        return existencia == null
                || existencia <= 0;
    }

    public void disminuirExistencia(
            Integer cantidad) {

        validarCantidad(cantidad);

        if (existencia < cantidad) {
            throw new IllegalStateException(
                    "La sucursal no tiene existencia suficiente"
            );
        }

        existencia -= cantidad;
    }

    public void aumentarExistencia(
            Integer cantidad) {

        validarCantidad(cantidad);
        existencia += cantidad;
    }

    private void validarCantidad(
            Integer cantidad) {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero"
            );
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
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

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(
            LocalDateTime fechaActualizacion) {

        this.fechaActualizacion = fechaActualizacion;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
