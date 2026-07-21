package com.abarrote.abarroteapi.saas.dto;

import com.abarrote.abarroteapi.saas.entity.SaasPlan;
import com.abarrote.abarroteapi.saas.entity.SaasProspecto;

import java.time.LocalDateTime;

public class SaasProspectoResponse {

    private Long id;
    private String folio;
    private String nombreNegocio;
    private String propietario;
    private String correo;
    private String whatsapp;
    private String ciudad;
    private String tipoNegocio;
    private Integer numeroMesas;

    private Long planId;
    private String planCodigo;
    private String planNombre;

    private String estado;
    private String origen;
    private String observaciones;
    private Long tenantId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public SaasProspectoResponse() {
    }

    public static SaasProspectoResponse desdeEntidad(
        SaasProspecto prospecto,
        SaasPlan plan
    ) {
        SaasProspectoResponse response =
            new SaasProspectoResponse();

        response.setId(prospecto.getId());
        response.setFolio(prospecto.getFolio());
        response.setNombreNegocio(prospecto.getNombreNegocio());
        response.setPropietario(prospecto.getPropietario());
        response.setCorreo(prospecto.getCorreo());
        response.setWhatsapp(prospecto.getWhatsapp());
        response.setCiudad(prospecto.getCiudad());
        response.setTipoNegocio(prospecto.getTipoNegocio());
        response.setNumeroMesas(prospecto.getNumeroMesas());

        response.setPlanId(plan.getId());
        response.setPlanCodigo(plan.getCodigo());
        response.setPlanNombre(plan.getNombre());

        response.setEstado(prospecto.getEstado());
        response.setOrigen(prospecto.getOrigen());
        response.setObservaciones(prospecto.getObservaciones());
        response.setTenantId(prospecto.getTenantId());
        response.setFechaCreacion(prospecto.getFechaCreacion());
        response.setFechaActualizacion(
            prospecto.getFechaActualizacion()
        );

        return response;
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

    public String getNombreNegocio() {
        return nombreNegocio;
    }

    public void setNombreNegocio(String nombreNegocio) {
        this.nombreNegocio = nombreNegocio;
    }

    public String getPropietario() {
        return propietario;
    }

    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getTipoNegocio() {
        return tipoNegocio;
    }

    public void setTipoNegocio(String tipoNegocio) {
        this.tipoNegocio = tipoNegocio;
    }

    public Integer getNumeroMesas() {
        return numeroMesas;
    }

    public void setNumeroMesas(Integer numeroMesas) {
        this.numeroMesas = numeroMesas;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public String getPlanCodigo() {
        return planCodigo;
    }

    public void setPlanCodigo(String planCodigo) {
        this.planCodigo = planCodigo;
    }

    public String getPlanNombre() {
        return planNombre;
    }

    public void setPlanNombre(String planNombre) {
        this.planNombre = planNombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(
        LocalDateTime fechaActualizacion
    ) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
