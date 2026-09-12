package com.agrocloud.chatia.herramientas;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RegistroHerramientasConsulta {

    private final Map<String, HerramientaConsultaChatIa> herramientasPorNombre = new LinkedHashMap<>();

    public RegistroHerramientasConsulta(List<ProveedorHerramientasChatIa> proveedores) {
        for (ProveedorHerramientasChatIa proveedor : proveedores) {
            for (HerramientaConsultaChatIa herramienta : proveedor.obtenerHerramientas()) {
                herramientasPorNombre.put(herramienta.getNombre(), herramienta);
            }
        }
    }

    public List<Map<String, Object>> declaracionesParaContexto(ContextoConsultaChatIa contexto) {
        return herramientasPorNombre.values().stream()
                .filter(h -> moduloHabilitado(h.getModuloCodigo(), contexto.getModulosActivos()))
                .map(HerramientaConsultaChatIa::getDeclaracionFuncion)
                .collect(Collectors.toList());
    }

    public Object ejecutar(String nombre, ContextoConsultaChatIa contexto, Map<String, Object> argumentos) {
        HerramientaConsultaChatIa herramienta = herramientasPorNombre.get(nombre);
        if (herramienta == null) {
            return Map.of("error", "Herramienta no encontrada: " + nombre);
        }
        if (!moduloHabilitado(herramienta.getModuloCodigo(), contexto.getModulosActivos())) {
            return Map.of("error", "Módulo no habilitado para esta herramienta");
        }
        try {
            return herramienta.ejecutar(contexto, argumentos != null ? argumentos : Map.of());
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    public static Set<String> normalizarModulos(Collection<String> codigos) {
        Set<String> normalizados = new HashSet<>();
        for (String codigo : codigos) {
            normalizados.add(normalizarCodigoModulo(codigo));
        }
        normalizados.add("CORE");
        return normalizados;
    }

    public static String normalizarCodigoModulo(String codigo) {
        if (codigo == null) {
            return "";
        }
        return switch (codigo.toUpperCase()) {
            case "CROPS", "CULTIVOS" -> "CULTIVOS";
            case "PIGS", "PORCINOS" -> "PORCINOS";
            case "AVICOLA_CRIANZA", "AVICOLA_CARNE", "AVICOLA-CRIANZA" -> "AVICOLA_CRIANZA";
            case "AVICOLA_HUEVOS", "AVICOLA-HUEVOS" -> "AVICOLA_HUEVOS";
            case "AVICOLA_PONEDORAS", "AVICOLA-PONEDORAS" -> "AVICOLA_PONEDORAS";
            case "FEEDLOT" -> "FEEDLOT";
            case "LECHERIA" -> "LECHERIA";
            case "CORE" -> "CORE";
            default -> codigo.toUpperCase();
        };
    }

    private boolean moduloHabilitado(String moduloCodigo, Set<String> modulosActivos) {
        if (moduloCodigo == null || "CORE".equals(moduloCodigo)) {
            return true;
        }
        return modulosActivos.contains(moduloCodigo);
    }
}
