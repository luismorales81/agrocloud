package com.agrocloud.chatia.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Límite de tasa en memoria: 20 mensajes por minuto por usuario.
 * Nota: en despliegues multi-instancia usar almacén compartido (p. ej. Redis).
 */
@Service
public class ServicioLimiteTasaChatIa {

    private static final int MAXIMO_POR_MINUTO = 20;
    private static final long VENTANA_MS = 60_000L;

    private final Map<Long, Deque<Long>> solicitudesPorUsuario = new ConcurrentHashMap<>();

    public void verificarLimite(Long usuarioId) {
        Deque<Long> ventana = solicitudesPorUsuario.computeIfAbsent(usuarioId, id -> new ArrayDeque<>());
        long ahora = Instant.now().toEpochMilli();
        synchronized (ventana) {
            while (!ventana.isEmpty() && ahora - ventana.peekFirst() > VENTANA_MS) {
                ventana.pollFirst();
            }
            if (ventana.size() >= MAXIMO_POR_MINUTO) {
                throw new IllegalStateException("Límite de consultas alcanzado. Esperá un minuto e intentá de nuevo.");
            }
            ventana.addLast(ahora);
        }
    }
}
