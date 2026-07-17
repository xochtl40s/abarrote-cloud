package com.abarrote.abarroteapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(
            message = "El nombre no puede estar vacío"
    )
    @Column(
            nullable = false,
            length = 120
    )
    private String nombre;

    @NotBlank(
            message = "El nombre de usuario no puede estar vacío"
    )
    @Size(
            min = 4,
            max = 20,
            message = "El login debe tener entre 4 y 20 caracteres"
    )
    @Column(
            nullable = false,
            unique = true,
            length = 20
    )
    private String username;

    @NotBlank(
            message = "La contraseña es obligatoria"
    )
    @Column(nullable = false)
    private String password;

    @Column(
            nullable = false,
            length = 30
    )
    private String rol;

    @Column(nullable = false)
    private Boolean activo = true;

    /*
     * Temporalmente nullable para permitir la migración de los
     * usuarios que ya existen. El inicializador los asignará a MAT.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "sucursal_id",
            foreignKey = @ForeignKey(
                    name = "fk_usuario_sucursal"
            )
    )
    private Sucursal sucursal;

    public Usuario() {
    }

    public Usuario(
            String nombre,
            String username,
            String password,
            String rol) {

        this.nombre = nombre;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.activo = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id) {

        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(
            String nombre) {

        this.nombre = nombre;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(
            String username) {

        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(
            String password) {

        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(
            String rol) {

        this.rol = rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(
            Boolean activo) {

        this.activo = activo;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(
            Sucursal sucursal) {

        this.sucursal = sucursal;
    }
}
