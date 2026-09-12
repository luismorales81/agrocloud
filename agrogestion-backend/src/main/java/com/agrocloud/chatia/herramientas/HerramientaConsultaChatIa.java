package com.agrocloud.chatia.herramientas;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * Herramienta de consulta invocable por Gemini (function calling).
 */
public class HerramientaConsultaChatIa {

    private final String nombre;
    private final String moduloCodigo;
    private final String descripcion;
    private final Map<String, Object> parametrosEsquema;
    private final BiFunction<ContextoConsultaChatIa, Map<String, Object>, Object> ejecutor;

    public HerramientaConsultaChatIa(
            String nombre,
            String moduloCodigo,
            String descripcion,
            Map<String, Object> parametrosEsquema,
            BiFunction<ContextoConsultaChatIa, Map<String, Object>, Object> ejecutor) {
        this.nombre = nombre;
        this.moduloCodigo = moduloCodigo;
        this.descripcion = descripcion;
        this.parametrosEsquema = parametrosEsquema;
        this.ejecutor = ejecutor;
    }

    public String getNombre() {
        return nombre;
    }

    public String getModuloCodigo() {
        return moduloCodigo;
    }

    public Map<String, Object> getDeclaracionFuncion() {
        return Map.of(
                "name", nombre,
                "description", descripcion,
                "parameters", parametrosEsquema
        );
    }

    public Object ejecutar(ContextoConsultaChatIa contexto, Map<String, Object> argumentos) {
        return ejecutor.apply(contexto, argumentos);
    }
}
