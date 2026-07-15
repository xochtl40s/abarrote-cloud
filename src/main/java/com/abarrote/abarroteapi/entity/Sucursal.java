package com.abarrote.abarroteapi.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "sucursal",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sucursal_codigo",
                        columnNames = "codigo"
                )
        }
)
public class Sucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }

        if (activa == null) {
            activa = true;
        }

        if (codigo != null) {
            codigo = codigo.trim().toUpperCase();
        }

        if (nombre != null) {
            nombre = nombre.trim();
        }
    }

    @PreUpdate
    public void preUpdate() {

        if (codigo != null) {
            codigo = codigo.trim().toUpperCase();
        }

        if (nombre != null) {
            nombre = nombre.trim();
        }
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
}
