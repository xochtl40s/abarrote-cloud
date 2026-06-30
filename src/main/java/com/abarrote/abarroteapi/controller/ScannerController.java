package com.abarrote.abarroteapi.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/scanner")
public class ScannerController {

    // Lista concurrente segura para almacenar las conexiones activas del navegador
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // Endpoint 1: El navegador se suscribe aquí para escuchar eventos en tiempo real
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(60_000L); // 1 minuto de timeout
        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError((e) -> this.emitters.remove(emitter));

        return emitter;
    }

    // Endpoint 2: El teléfono Android/Motorola envía el código de barras aquí
    @PostMapping("/scan")
    public ResponseEntity<?> recibirCodigo(@RequestBody Map<String, String> payload) {
        String codigo = payload.get("codigo");
        if (codigo == null || codigo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Código inválido");
        }

        // Notificar de inmediato a todas las pantallas del navegador abiertas
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("barcode-scanned").data(codigo.trim()));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }

        return ResponseEntity.ok().body(Map.of("status", "success", "message", "Código transmitido al POS"));
    }
}
