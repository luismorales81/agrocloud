package com.agrocloud.core.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP del cliente cuando el proxy (Caddy o nginx) reemplaza las cabeceras reenviadas.
 * Si todavía llega una cadena, se usa el último tramo: es el que agrega el proxy, no el cliente.
 */
public final class DireccionIpCliente {

    private DireccionIpCliente() {
    }

    public static String obtener(HttpServletRequest solicitud) {
        String real = valorUtil(solicitud.getHeader("X-Real-IP"));
        if (real != null) {
            return real;
        }
        String reenviada = valorUtil(solicitud.getHeader("X-Forwarded-For"));
        if (reenviada != null) {
            String[] partes = reenviada.split(",");
            String ultimo = partes[partes.length - 1].trim();
            if (!ultimo.isEmpty()) {
                return ultimo;
            }
        }
        return solicitud.getRemoteAddr();
    }

    private static String valorUtil(String cabecera) {
        if (cabecera == null) {
            return null;
        }
        String limpio = cabecera.trim();
        if (limpio.isEmpty() || "unknown".equalsIgnoreCase(limpio)) {
            return null;
        }
        return limpio;
    }
}
