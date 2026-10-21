package com.agrocloud.core.security;

import com.agrocloud.exception.LimiteTasaExcedidoException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Límite de tasa en memoria para endpoints públicos de autenticación (por IP).
 */
@Service
public class ServicioLimiteTasaAuth {

    private static final int MAXIMO_LOGIN_POR_MINUTO = 10;
    private static final int MAXIMO_REGISTRO_POR_HORA = 3;
    private static final int MAXIMO_RECUPERO_POR_HORA = 5;
    private static final int MAXIMO_RECUPERO_POR_CORREO = 3;
    private static final long VENTANA_LOGIN_MS = 60_000L;
    private static final long VENTANA_REGISTRO_MS = 3_600_000L;

    private final Map<String, Deque<Long>> loginPorIp = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> registroPorIp = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> recuperoPorIp = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> recuperoPorCorreo = new ConcurrentHashMap<>();

    public void verificarLogin(String direccionIp) {
        verificar(direccionIp, loginPorIp, MAXIMO_LOGIN_POR_MINUTO, VENTANA_LOGIN_MS,
                "Demasiados intentos de inicio de sesión. Esperá un minuto e intentá de nuevo.");
    }

    public void verificarRegistro(String direccionIp) {
        verificar(direccionIp, registroPorIp, MAXIMO_REGISTRO_POR_HORA, VENTANA_REGISTRO_MS,
                "Demasiados registros desde esta dirección. Intentá más tarde.");
    }

    public void verificarRecupero(String direccionIp, String email) {
        verificar(direccionIp, recuperoPorIp, MAXIMO_RECUPERO_POR_HORA, VENTANA_REGISTRO_MS,
                "Demasiados intentos. Intentá más tarde.");
        if (email != null && !email.isBlank()) {
            verificar("correo:" + email.trim().toLowerCase(), recuperoPorCorreo, MAXIMO_RECUPERO_POR_CORREO,
                    VENTANA_REGISTRO_MS, "Demasiados intentos. Intentá más tarde.");
        }
    }

    private void verificar(String clave, Map<String, Deque<Long>> mapa, int maximo, long ventanaMs, String mensaje) {
        String ip = clave != null && !clave.isBlank() ? clave : "desconocida";
        Deque<Long> ventana = mapa.computeIfAbsent(ip, k -> new ArrayDeque<>());
        long ahora = Instant.now().toEpochMilli();
        synchronized (ventana) {
            while (!ventana.isEmpty() && ahora - ventana.peekFirst() > ventanaMs) {
                ventana.pollFirst();
            }
            if (ventana.size() >= maximo) {
                throw new LimiteTasaExcedidoException(mensaje);
            }
            ventana.addLast(ahora);
        }
    }
}
