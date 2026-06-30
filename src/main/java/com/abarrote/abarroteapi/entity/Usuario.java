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

    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Size(min = 4, max = 20, message = "El login debe tener entre 4 y 20 caracteres")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String rol; // Valores esperados: "ADMIN" o "CAJERO"

    @Column(nullable = false)
    private Boolean activo = true;

    // --- CONSTRUCTORES ---
    public Usuario() {}

    public Usuario(String nombre, String username, String password, String rol) {
        this.nombre = nombre;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.activo = true;
    }

    // --- GETTERS Y SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
