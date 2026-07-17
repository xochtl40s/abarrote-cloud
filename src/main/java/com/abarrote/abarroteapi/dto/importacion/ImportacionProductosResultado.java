package com.abarrote.abarroteapi.dto.importacion;

import java.util.ArrayList;
import java.util.List;

public class ImportacionProductosResultado {

    private int filasLeidas;
    private int productosNuevos;
    private int productosActualizados;
    private int categoriasCreadas;
    private int inventariosCreados;
    private int inventariosActualizados;

    private final List<String> mensajes =
            new ArrayList<>();

    public int getFilasLeidas() {
        return filasLeidas;
    }

    public void setFilasLeidas(
            int filasLeidas) {

        this.filasLeidas = filasLeidas;
    }

    public int getProductosNuevos() {
        return productosNuevos;
    }

    public void incrementarProductosNuevos() {
        this.productosNuevos++;
    }

    public int getProductosActualizados() {
        return productosActualizados;
    }

    public void incrementarProductosActualizados() {
        this.productosActualizados++;
    }

    public int getCategoriasCreadas() {
        return categoriasCreadas;
    }

    public void incrementarCategoriasCreadas() {
        this.categoriasCreadas++;
    }

    public int getInventariosCreados() {
        return inventariosCreados;
    }

    public void incrementarInventariosCreados() {
        this.inventariosCreados++;
    }

    public int getInventariosActualizados() {
        return inventariosActualizados;
    }

    public void incrementarInventariosActualizados() {
        this.inventariosActualizados++;
    }

    public List<String> getMensajes() {
        return mensajes;
    }

    public void agregarMensaje(
            String mensaje) {

        mensajes.add(mensaje);
    }
}
