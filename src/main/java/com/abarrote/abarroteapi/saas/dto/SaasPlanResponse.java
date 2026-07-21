package com.abarrote.abarroteapi.saas.dto;

import com.abarrote.abarroteapi.saas.entity.SaasPlan;

import java.math.BigDecimal;

public class SaasPlanResponse {

    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;

    private BigDecimal precioMensual;
    private BigDecimal precioAnual;

    private Integer maxUsuarios;
    private Integer maxSucursales;
    private Integer maxMesas;

    private Boolean permiteReportesAvanzados;
    private Boolean permiteAsistenteIa;
    private Boolean permiteExportacionExcel;
    private Boolean permiteSoportePrioritario;

    private Integer diasPrueba;
    private Boolean activo;
    private Integer ordenVisual;

    public SaasPlanResponse() {
    }

    public static SaasPlanResponse desdeEntidad(SaasPlan plan) {
        SaasPlanResponse response = new SaasPlanResponse();

        response.setId(plan.getId());
        response.setCodigo(plan.getCodigo());
        response.setNombre(plan.getNombre());
        response.setDescripcion(plan.getDescripcion());

        response.setPrecioMensual(plan.getPrecioMensual());
        response.setPrecioAnual(plan.getPrecioAnual());

        response.setMaxUsuarios(plan.getMaxUsuarios());
        response.setMaxSucursales(plan.getMaxSucursales());
        response.setMaxMesas(plan.getMaxMesas());

        response.setPermiteReportesAvanzados(
            plan.getPermiteReportesAvanzados()
        );

        response.setPermiteAsistenteIa(
            plan.getPermiteAsistenteIa()
        );

        response.setPermiteExportacionExcel(
            plan.getPermiteExportacionExcel()
        );

        response.setPermiteSoportePrioritario(
            plan.getPermiteSoportePrioritario()
        );

        response.setDiasPrueba(plan.getDiasPrueba());
        response.setActivo(plan.getActivo());
        response.setOrdenVisual(plan.getOrdenVisual());

        return response;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioMensual() {
        return precioMensual;
    }

    public void setPrecioMensual(BigDecimal precioMensual) {
        this.precioMensual = precioMensual;
    }

    public BigDecimal getPrecioAnual() {
        return precioAnual;
    }

    public void setPrecioAnual(BigDecimal precioAnual) {
        this.precioAnual = precioAnual;
    }

    public Integer getMaxUsuarios() {
        return maxUsuarios;
    }

    public void setMaxUsuarios(Integer maxUsuarios) {
        this.maxUsuarios = maxUsuarios;
    }

    public Integer getMaxSucursales() {
        return maxSucursales;
    }

    public void setMaxSucursales(Integer maxSucursales) {
        this.maxSucursales = maxSucursales;
    }

    public Integer getMaxMesas() {
        return maxMesas;
    }

    public void setMaxMesas(Integer maxMesas) {
        this.maxMesas = maxMesas;
    }

    public Boolean getPermiteReportesAvanzados() {
        return permiteReportesAvanzados;
    }

    public void setPermiteReportesAvanzados(
        Boolean permiteReportesAvanzados
    ) {
        this.permiteReportesAvanzados = permiteReportesAvanzados;
    }

    public Boolean getPermiteAsistenteIa() {
        return permiteAsistenteIa;
    }

    public void setPermiteAsistenteIa(Boolean permiteAsistenteIa) {
        this.permiteAsistenteIa = permiteAsistenteIa;
    }

    public Boolean getPermiteExportacionExcel() {
        return permiteExportacionExcel;
    }

    public void setPermiteExportacionExcel(
        Boolean permiteExportacionExcel
    ) {
        this.permiteExportacionExcel = permiteExportacionExcel;
    }

    public Boolean getPermiteSoportePrioritario() {
        return permiteSoportePrioritario;
    }

    public void setPermiteSoportePrioritario(
        Boolean permiteSoportePrioritario
    ) {
        this.permiteSoportePrioritario = permiteSoportePrioritario;
    }

    public Integer getDiasPrueba() {
        return diasPrueba;
    }

    public void setDiasPrueba(Integer diasPrueba) {
        this.diasPrueba = diasPrueba;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Integer getOrdenVisual() {
        return ordenVisual;
    }

    public void setOrdenVisual(Integer ordenVisual) {
        this.ordenVisual = ordenVisual;
    }
}
