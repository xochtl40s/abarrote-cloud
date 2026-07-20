package com.abarrote.abarroteapi.gym.service;

import java.util.ArrayList;
import java.util.List;

public class GymExcelImportResult {

    private int procesados;

    private int importados;

    private int rechazados;

    private final List<String> errores =
        new ArrayList<>();

    public void procesado() {
        procesados++;
    }

    public void importado() {
        importados++;
    }

    public void rechazado(
        int fila,
        String mensaje
    ) {
        rechazados++;

        if (errores.size() < 15) {
            errores.add(
                "Fila " + fila + ": " + mensaje
            );
        }
    }

    public int getProcesados() {
        return procesados;
    }

    public int getImportados() {
        return importados;
    }

    public int getRechazados() {
        return rechazados;
    }

    public List<String> getErrores() {
        return errores;
    }

    public String getResumen() {
        return "Procesados: " + procesados
            + ", importados: " + importados
            + ", rechazados: " + rechazados;
    }
}
