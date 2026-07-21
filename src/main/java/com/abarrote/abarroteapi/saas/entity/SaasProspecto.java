package com.abarrote.abarroteapi.saas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "saas_prospecto",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_saas_prospecto_folio",
            columnNames = "folio"
        )
    }
)
public class SaasProspecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "folio",
        nullable = false,
        length = 40
    )
    private String folio;

    @Column(
        name = "nombre_negocio",
        nullable = false,
        length = 150
    )
    private String nombreNegocio;

    @Column(
        name = "propietario",
        nullable = false,
        length = 150
    )
    private String propietario;

    @Column(
        name = "correo",
        nullable = false,
        length = 150
    )
    private String correo;

    @Column(
        name = "whatsapp",
        nullable = false,
        length = 30
    )
    private String whatsapp;

    @Column(
        name = "ciudad",
        nullable = false,
        length = 120
    )
    private String ciudad;

    @Column(
        name = "tipo_negocio",
        nullable = false,
        length = 30
    )
    private String tipoNegocio;

    @Column(name = "numero_mesas")
    private Integer numeroMesas;

    @Column(
        name = "plan_solicitado_id",
        nullable = false
    )
    private Long planSolicitadoId;

    @Column(
        name = "estado",
        nullable = false,
        length = 30
    )
    private String estado;

    @Column(
        name = "origen",
        nullable = false,
        length = 50
    )
    private String origen;

    @Column(
        name = "observaciones",
        length = 1000
    )
    private String observaciones;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "atendido_por_usuario_id")
    private Long atendidoPorUsuarioId;

    @Column(
        name = "fecha_creacion",
        nullable = false,
        updatable = false
    )
    private LocalDateTime fechaCreacion;

    @Column(
        name = "fecha_actualizacion",
        nullable = false
    )
    private LocalDateTime fechaActualizacion;

    public SaasProspecto() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();

        if (fechaCreacion == null) {
            fechaCreacion = ahora;
        }

        fechaActualizacion = ahora;

        if (estado == null || estado.isBlank()) {
            estado = "NUEVO";
        }

        if (origen == null || origen.isBlank()) {
            origen = "LANDING";
        }

        if (tipoNegocio != null) {
            tipoNegocio = tipoNegocio.trim().toUpperCase();
        }

        estado = estado.trim().toUpperCase();
        origen = origen.trim().toUpperCase();
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();

        if (tipoNegocio != null) {
            tipoNegocio = tipoNegocio.trim().toUpperCase();
        }

        if (estado != null) {
            estado = estado.trim().toUpperCase();
        }

        if (origen != null) {
            origen = origen.trim().toUpperCase();
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
        this.folio = limpiarTexto(folio);
    }

    public String getNombreNegocio() {
        return nombreNegocio;
    }

    public void setNombreNegocio(String nombreNegocio) {
        this.nombreNegocio = limpiarTexto(nombreNegocio);
    }

    public String getPropietario() {
        return propietario;
    }

    public void setPropietario(String propietario) {
        this.propietario = limpiarTexto(propietario);
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        String valor = limpiarTexto(correo);
        this.correo = valor == null ? null : valor.toLowerCase();
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = limpiarTexto(whatsapp);
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = limpiarTexto(ciudad);
    }

    public String getTipoNegocio() {
        return tipoNegocio;
    }

    public void setTipoNegocio(String tipoNegocio) {
        String valor = limpiarTexto(tipoNegocio);
        this.tipoNegocio = valor == null ? null : valor.toUpperCase();
    }

    public Integer getNumeroMesas() {
        return numeroMesas;
    }

    public void setNumeroMesas(Integer numeroMesas) {
        this.numeroMesas = numeroMesas;
    }

    public Long getPlanSolicitadoId() {
        return planSolicitadoId;
    }

    public void setPlanSolicitadoId(Long planSolicitadoId) {
        this.planSolicitadoId = planSolicitadoId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        String valor = limpiarTexto(estado);
        this.estado = valor == null ? null : valor.toUpperCase();
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        String valor = limpiarTexto(origen);
        this.origen = valor == null ? null : valor.toUpperCase();
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = limpiarTexto(observaciones);
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getAtendidoPorUsuarioId() {
        return atendidoPorUsuarioId;
    }

    public void setAtendidoPorUsuarioId(Long atendidoPorUsuarioId) {
        this.atendidoPorUsuarioId = atendidoPorUsuarioId;
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

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    private String limpiarTexto(String valor) {
        if (valor == null) {
            return null;
        }

        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }
}
