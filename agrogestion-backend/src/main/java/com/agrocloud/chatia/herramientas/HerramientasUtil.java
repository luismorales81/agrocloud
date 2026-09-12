package com.agrocloud.chatia.herramientas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HerramientasUtil {

    private static final int LIMITE_MAXIMO = 20;
    private static final int LIMITE_DEFECTO = 10;

    private HerramientasUtil() {
    }

    public static int resolverLimite(Map<String, Object> args) {
        int limite = obtenerEntero(args, "limite", LIMITE_DEFECTO);
        return Math.min(Math.max(limite, 1), LIMITE_MAXIMO);
    }

    public static String obtenerTexto(Map<String, Object> args, String clave) {
        Object valor = args.get(clave);
        return valor != null ? valor.toString().trim() : null;
    }

    public static int obtenerEntero(Map<String, Object> args, String clave, int defecto) {
        Object valor = args.get(clave);
        if (valor == null) {
            return defecto;
        }
        if (valor instanceof Number numero) {
            return numero.intValue();
        }
        try {
            return Integer.parseInt(valor.toString());
        } catch (NumberFormatException e) {
            return defecto;
        }
    }

    public static boolean obtenerBooleano(Map<String, Object> args, String clave, boolean defecto) {
        Object valor = args.get(clave);
        if (valor == null) {
            return defecto;
        }
        if (valor instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(valor.toString());
    }

    public static LocalDate obtenerFecha(Map<String, Object> args, String clave) {
        String texto = obtenerTexto(args, clave);
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return LocalDate.parse(texto);
    }

    @SuppressWarnings("unchecked")
    public static List<String> obtenerListaTexto(Map<String, Object> args, String clave) {
        Object valor = args.get(clave);
        if (valor == null) {
            return List.of();
        }
        if (valor instanceof List<?> lista) {
            List<String> resultado = new ArrayList<>();
            for (Object item : lista) {
                if (item != null) {
                    resultado.add(item.toString());
                }
            }
            return resultado;
        }
        return List.of(valor.toString());
    }

    public static Map<String, Object> esquemaVacio() {
        Map<String, Object> esquema = new LinkedHashMap<>();
        esquema.put("type", "object");
        esquema.put("properties", Map.of());
        return esquema;
    }

    public static Map<String, Object> esquemaConPropiedades(Map<String, Map<String, Object>> propiedades, List<String> requeridos) {
        Map<String, Object> esquema = new LinkedHashMap<>();
        esquema.put("type", "object");
        esquema.put("properties", propiedades);
        if (requeridos != null && !requeridos.isEmpty()) {
            esquema.put("required", requeridos);
        }
        return esquema;
    }

    public static Map<String, Object> propiedadString(String descripcion) {
        return Map.of("type", "string", "description", descripcion);
    }

    public static Map<String, Object> propiedadEntero(String descripcion) {
        return Map.of("type", "integer", "description", descripcion);
    }

    public static Map<String, Object> propiedadBooleano(String descripcion) {
        return Map.of("type", "boolean", "description", descripcion);
    }

    public static Map<String, Object> propiedadListaString(String descripcion) {
        Map<String, Object> items = Map.of("type", "string");
        Map<String, Object> prop = new LinkedHashMap<>();
        prop.put("type", "array");
        prop.put("items", items);
        prop.put("description", descripcion);
        return prop;
    }

    public static Object valorNumerico(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    public static Map<String, Object> mapaResultado(String clave, Object valor) {
        Map<String, Object> mapa = new HashMap<>();
        mapa.put(clave, valor);
        return mapa;
    }
}
