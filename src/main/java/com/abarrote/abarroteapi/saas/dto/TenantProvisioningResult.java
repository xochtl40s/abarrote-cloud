package com.abarrote.abarroteapi.saas.dto;

import java.io.Serial;
import java.io.Serializable;

public class TenantProvisioningResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long tenantId;
    private final String tenantNombre;
    private final String tenantSlug;
    private final String tipoNegocio;
    private final String plan;
    private final String username;
    private final String passwordTemporal;
    private final String propietario;
    private final String folio;
    private final boolean credencialesRegeneradas;

    public TenantProvisioningResult(
            Long tenantId,
            String tenantNombre,
            String tenantSlug,
            String tipoNegocio,
            String plan,
            String username,
            String passwordTemporal,
            String propietario,
            String folio,
            boolean credencialesRegeneradas) {

        this.tenantId = tenantId;
        this.tenantNombre = tenantNombre;
        this.tenantSlug = tenantSlug;
        this.tipoNegocio = tipoNegocio;
        this.plan = plan;
        this.username = username;
        this.passwordTemporal = passwordTemporal;
        this.propietario = propietario;
        this.folio = folio;
        this.credencialesRegeneradas = credencialesRegeneradas;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getTenantNombre() {
        return tenantNombre;
    }

    public String getTenantSlug() {
        return tenantSlug;
    }

    public String getTipoNegocio() {
        return tipoNegocio;
    }

    public String getPlan() {
        return plan;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordTemporal() {
        return passwordTemporal;
    }

    public String getPropietario() {
        return propietario;
    }

    public String getFolio() {
        return folio;
    }

    public boolean isCredencialesRegeneradas() {
        return credencialesRegeneradas;
    }
}
