package com.abarrote.abarroteapi.dto;

import java.time.LocalDateTime;

public class AsistenteResponse {

    private String pregunta;

    private String respuesta;

    private LocalDateTime generadoEn;

    public AsistenteResponse() {
    }

    public AsistenteResponse(
            String pregunta,
            String respuesta,
            LocalDateTime generadoEn) {

        this.pregunta = pregunta;
        this.respuesta = respuesta;
        this.generadoEn = generadoEn;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(
            String pregunta) {

        this.pregunta = pregunta;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(
            String respuesta) {

        this.respuesta = respuesta;
    }

    public LocalDateTime getGeneradoEn() {
        return generadoEn;
    }

    public void setGeneradoEn(
            LocalDateTime generadoEn) {

        this.generadoEn = generadoEn;
    }
}
