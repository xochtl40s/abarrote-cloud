package com.abarrote.abarroteapi.dto;

import java.util.ArrayList;
import java.util.List;

public class InventarioTendenciaResponse {

    private List<String> etiquetas =
            new ArrayList<>();

    private List<SerieSucursal> series =
            new ArrayList<>();

    public InventarioTendenciaResponse() {
    }

    public List<String> getEtiquetas() {
        return etiquetas;
    }

    public void setEtiquetas(
            List<String> etiquetas) {

        this.etiquetas =
                etiquetas != null
                        ? etiquetas
                        : new ArrayList<>();
    }

    public List<SerieSucursal> getSeries() {
        return series;
    }

    public void setSeries(
            List<SerieSucursal> series) {

        this.series =
                series != null
                        ? series
                        : new ArrayList<>();
    }

    public static class SerieSucursal {

        private Long sucursalId;

        private String codigo;

        private String nombre;

        private List<Integer> valores =
                new ArrayList<>();

        public SerieSucursal() {
        }

        public Long getSucursalId() {
            return sucursalId;
        }

        public void setSucursalId(
                Long sucursalId) {

            this.sucursalId = sucursalId;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(
                String codigo) {

            this.codigo = codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(
                String nombre) {

            this.nombre = nombre;
        }

        public List<Integer> getValores() {
            return valores;
        }

        public void setValores(
                List<Integer> valores) {

            this.valores =
                    valores != null
                            ? valores
                            : new ArrayList<>();
        }
    }
}
