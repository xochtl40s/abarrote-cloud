package com.abarrote.abarroteapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UsuarioRequest {

    @NotBlank(
            message = "El nombre es obligatorio"
    )
    @Size(
            min = 2,
            max = 100,
            message = "El nombre debe tener entre 2 y 100 caracteres"
    )
    private String nombre;

    @NotBlank(
            message = "El username es obligatorio"
    )
    @Size(
            min = 4,
            max = 20,
            message = "El username debe tener entre 4 y 20 caracteres"
    )
    private String username;

    /*
     * La contraseña puede venir vacía durante una edición.
     *
     * Si contiene texto, debe tener al menos cuatro caracteres.
     * La obligatoriedad durante el alta se valida en el servicio.
     */
    @Pattern(
            regexp = "^$|^.{4,}$",
            message = "La contraseña debe tener al menos 4 caracteres"
    )
    private String password;

    @NotBlank(
            message = "El rol es obligatorio"
    )
    @Pattern(
            regexp = "ADMIN|CAJERO",
            message = "El rol debe ser ADMIN o CAJERO"
    )
    private String rol;

    private Boolean activo = true;

    /*
     * Si únicamente existe una sucursal activa,
     * el servicio la asigna automáticamente.
     *
     * Si existen varias, la selección es obligatoria.
     */
    private Long sucursalId;

    public UsuarioRequest() {
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

    public Long getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(
            Long sucursalId) {

        this.sucursalId = sucursalId;
    }
}
