package com.agrocloud.trazabilidad.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.agrocloud.trazabilidad.domain.TrazabilidadCertificacion;
import com.agrocloud.trazabilidad.domain.TrazabilidadReglaCertificacion;
import com.agrocloud.trazabilidad.domain.TrazabilidadReporte;
import com.agrocloud.trazabilidad.dto.HechosTrazabilidadDocumento;
import com.agrocloud.trazabilidad.dto.IncidenciaValidacionTrazabilidad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Aplica el conjunto de reglas configuradas para la certificación (extensible por {@code tipoRegla}).
 */
@Service
@Transactional(readOnly = true)
public class ValidacionTrazabilidadService {

    @Autowired
    private ObjectMapper objectMapper;

    public TrazabilidadReporte.ResultadoReporte validarOIncumple(
            HechosTrazabilidadDocumento hechos,
            TrazabilidadCertificacion certificacion,
            List<IncidenciaValidacionTrazabilidad> incidenciasAcumuladas) {
        List<TrazabilidadReglaCertificacion> reglas = certificacion.getReglas().stream()
                .filter(TrazabilidadReglaCertificacion::isActiva)
                .sorted(Comparator.comparingInt(TrazabilidadReglaCertificacion::getOrdenEjecucion))
                .collect(Collectors.toList());
        for (TrazabilidadReglaCertificacion r : reglas) {
            JsonNode p;
            try {
                p = objectMapper.readTree(r.getParametrosJson());
            } catch (Exception e) {
                incidenciasAcumuladas.add(new IncidenciaValidacionTrazabilidad("PARAM_JSON_INVALIDO",
                        "Parámetros de regla inválidos: " + r.getTipoRegla(), r.getTipoRegla()));
                return TrazabilidadReporte.ResultadoReporte.INCOMPLETO;
            }
            String tipo = r.getTipoRegla();
            if ("NINGUNO_TIPOS_INSUMO".equals(tipo)) {
                evaluarNingunTipoInsumo(hechos, p, r.getTipoRegla(), incidenciasAcumuladas);
            } else if ("SIN_CATEGORIA_SANITARIA".equals(tipo)) {
                evaluarSinCategoriaSanitaria(hechos, p, r.getTipoRegla(), incidenciasAcumuladas);
            } else {
                incidenciasAcumuladas.add(new IncidenciaValidacionTrazabilidad("TIPO_REGLA_DESCONOCIDO",
                        "No existe evaluador para: " + tipo, tipo));
                return TrazabilidadReporte.ResultadoReporte.INCOMPLETO;
            }
        }
        if (!incidenciasAcumuladas.isEmpty()) {
            return TrazabilidadReporte.ResultadoReporte.INVALIDO;
        }
        if (hechos.getLabores().isEmpty() && hechos.getInsumosPorLabor().isEmpty() && hechos.getEventosSanitarios().isEmpty()) {
            return TrazabilidadReporte.ResultadoReporte.INCOMPLETO;
        }
        return TrazabilidadReporte.ResultadoReporte.VALIDO;
    }

    private void evaluarNingunTipoInsumo(
            HechosTrazabilidadDocumento hechos,
            JsonNode params,
            String reglaAplicada,
            List<IncidenciaValidacionTrazabilidad> inc) {
        if (!params.has("tiposInsumoProhibidos") || !params.get("tiposInsumoProhibidos").isArray()) {
            return;
        }
        Set<String> prohibidos = new HashSet<>();
        for (JsonNode t : params.get("tiposInsumoProhibidos")) {
            if (t.isTextual()) {
                prohibidos.add(t.asText().toUpperCase(Locale.ROOT).trim());
            }
        }
        for (HechosTrazabilidadDocumento.LineaInsumoLabor l : hechos.getInsumosPorLabor()) {
            String tip = l.getTipoInsumo() != null ? l.getTipoInsumo().toUpperCase(Locale.ROOT).trim() : "";
            if (prohibidos.contains(tip)) {
                inc.add(new IncidenciaValidacionTrazabilidad("INSUMO_PROHIBIDO",
                        "Uso de insumo " + l.getNombreInsumo() + " (" + l.getTipoInsumo() + ") en labor " + l.getIdLabor(),
                        reglaAplicada));
            }
        }
    }

    private void evaluarSinCategoriaSanitaria(
            HechosTrazabilidadDocumento hechos,
            JsonNode params,
            String reglaAplicada,
            List<IncidenciaValidacionTrazabilidad> inc) {
        if (!params.has("categoriasProhibidas") || !params.get("categoriasProhibidas").isArray()) {
            return;
        }
        Set<String> prohibidas = new HashSet<>();
        for (JsonNode t : params.get("categoriasProhibidas")) {
            if (t.isTextual()) {
                prohibidas.add(t.asText().toUpperCase(Locale.ROOT).trim());
            }
        }
        for (HechosTrazabilidadDocumento.EventoSanitarioResumen e : hechos.getEventosSanitarios()) {
            String cat = e.getCategoria() != null ? e.getCategoria().toUpperCase(Locale.ROOT).trim() : "";
            if (prohibidas.contains(cat)) {
                inc.add(new IncidenciaValidacionTrazabilidad("EVENTO_SANITARIO_PROHIBIDO",
                        "Evento " + e.getId() + " con categoría " + e.getCategoria() + " el " + e.getFecha(),
                        reglaAplicada));
            }
        }
    }
}
