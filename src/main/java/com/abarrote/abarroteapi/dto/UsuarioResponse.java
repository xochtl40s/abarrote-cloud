package com.abarrote.abarroteapi.dto;

public class UsuarioResponse {

    private Long id;

    private String nombre;
    private String username;
    private String rol;

    private Boolean activo;

    private Long sucursalId;
    private String sucursalCodigo;
    private String sucursalNombre;

    public UsuarioResponse() {
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

    public String getSucursalCodigo() {
        return sucursalCodigo;
    }

    public void setSucursalCodigo(
            String sucursalCodigo) {

        this.sucursalCodigo = sucursalCodigo;
    }

    public String getSucursalNombre() {
        return sucursalNombre;
    }

    public void setSucursalNombre(
            String sucursalNombre) {

        this.sucursalNombre = sucursalNombre;
    }
}
