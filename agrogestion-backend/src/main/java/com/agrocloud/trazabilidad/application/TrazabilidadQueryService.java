package com.agrocloud.trazabilidad.application;

import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import com.agrocloud.cultivos.domain.HistorialCosecha;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.cultivos.domain.LaborInsumo;
import com.agrocloud.porcinos.domain.EventoSanitario;
import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.infrastructure.EventoSanitarioRepository;
import com.agrocloud.porcinos.infrastructure.RecriaRepository;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.cultivos.infrastructure.HistorialCosechaRepository;
import com.agrocloud.cultivos.infrastructure.LaborInsumoRepository;
import com.agrocloud.cultivos.infrastructure.LaborRepository;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.trazabilidad.dto.HechosTrazabilidadDocumento;
import com.agrocloud.trazabilidad.domain.TrazabilidadCertificacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Obtiene hechos de trazabilidad (solo lectura sobre dominio existente), sin lógica de certificación.
 */
@Service
@Transactional(readOnly = true)
public class TrazabilidadQueryService {

    @Autowired
    private PlotRepository plotRepository;
    @Autowired
    private HistorialCosechaRepository historialCosechaRepository;
    @Autowired
    private RecriaRepository recriaRepository;
    @Autowired
    private LaborRepository laborRepository;
    @Autowired
    private LaborInsumoRepository laborInsumoRepository;
    @Autowired
    private EventoSanitarioRepository eventoSanitarioRepository;

    public HechosTrazabilidadDocumento construirHechos(
            String entidadTipo,
            Long entidadId,
            Empresa empresa,
            TrazabilidadCertificacion certificacion) {
        return switch (entidadTipo) {
            case "LOTE" -> construirParaLote(entidadId, empresa, null, null, certificacion);
            case "COSECHA" -> construirParaCosecha(entidadId, empresa, certificacion);
            case "RECRIA" -> construirParaRecria(entidadId, empresa, certificacion);
            default -> throw new IllegalArgumentException("entidadTipo no soportado: " + entidadTipo);
        };
    }

    private HechosTrazabilidadDocumento construirParaCosecha(
            Long idHistorial, Empresa empresa, TrazabilidadCertificacion certificacion) {
        HistorialCosecha hc = historialCosechaRepository.findByIdConLoteCampoEmpresa(idHistorial)
                .orElseThrow(() -> new IllegalArgumentException("Cosecha (historial) no encontrada: " + idHistorial));
        verificarEmpresa(hc.getLote().getCampo().getEmpresa().getId(), empresa);
        return construirParaLote(
                hc.getLote().getId(),
                empresa,
                hc.getFechaSiembra(),
                hc.getFechaCosecha(),
                certificacion,
                "Ciclo cosechado: " + hc.getCultivo().getNombre() + " (" + hc.getFechaSiembra() + " a " + hc.getFechaCosecha() + ")",
                hc.getLote().getNombre(),
                true
        );
    }

    private HechosTrazabilidadDocumento construirParaRecria(Long recriaId, Empresa empresa, TrazabilidadCertificacion cert) {
        Recria recria = recriaRepository.findByIdAndEmpresa(recriaId, empresa)
                .orElseThrow(() -> new IllegalArgumentException("Recria no encontrada o no pertenece a la empresa: " + recriaId));
        HechosTrazabilidadDocumento doc = construirParaLote(
                recria.getLoteId(), empresa, null, null, cert,
                "Recria " + recriaId + " en lote (ID parcela) " + recria.getLoteId(),
                null,
                false
        );
        // Solo eventos vinculados a esta recría (entidad = LOTE en dominio, id = recria)
        List<EventoSanitario> evs = eventoSanitarioRepository.findByEmpresaYEntidadConTipo(
                empresa, EventoSanitario.TipoEntidad.LOTE, recria.getId());
        añadirEventos(doc, evs, recria.getId());
        return doc;
    }

    private HechosTrazabilidadDocumento construirParaLote(
            Long loteId, Empresa empresa, LocalDate desde, LocalDate hasta, TrazabilidadCertificacion cert) {
        return construirParaLote(loteId, empresa, desde, hasta, cert, null, null, true);
    }

    private HechosTrazabilidadDocumento construirParaLote(
            Long loteId,
            Empresa empresa,
            LocalDate desde,
            LocalDate hasta,
            TrazabilidadCertificacion cert,
            String descripcionAlcance,
            String nombreLoteOverride,
            boolean incluirEventosTodasLasRecriasDelLote) {
        Plot plot = plotRepository.findByIdConCampoYEmpresa(loteId)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado: " + loteId));
        verificarEmpresa(plot.getCampo().getEmpresa().getId(), empresa);
        HechosTrazabilidadDocumento d = new HechosTrazabilidadDocumento();
        d.setNombreLote(nombreLoteOverride != null ? nombreLoteOverride : plot.getNombre());
        d.setDescripcionAlcance(descripcionAlcance != null
                ? descripcionAlcance
                : "Lote: " + plot.getNombre() + " (todos los registros de labores activos).");
        d.setNombreEmpresa(empresa.getNombre());
        d.setCodigoCertificacion(cert.getCodigo());
        d.setNombreCertificacion(cert.getDescripcion() != null ? cert.getDescripcion() : cert.getCodigo());
        d.setFechaCorteDesde(desde);
        d.setFechaCorteHasta(hasta);
        List<Labor> labores = laborRepository.findByLoteIdAndActivoTrue(loteId);
        if (desde != null && hasta != null) {
            labores = labores.stream()
                    .filter(l -> laborEnRangoDeFechas(l, desde, hasta))
                    .collect(Collectors.toList());
        }
        List<Long> laborIds = labores.stream().map(Labor::getId).collect(Collectors.toList());
        for (Labor l : labores.stream().sorted(Comparator.comparing(Labor::getFechaInicio, Comparator.nullsLast(Comparator.naturalOrder()))).toList()) {
            HechosTrazabilidadDocumento.LineaHechoLabor linea = new HechosTrazabilidadDocumento.LineaHechoLabor();
            linea.setIdLabor(l.getId());
            linea.setFecha(l.getFechaInicio());
            linea.setTipoLabor(l.getTipoLabor() != null ? l.getTipoLabor().name() : "");
            linea.setEstado(l.getEstado() != null ? l.getEstado().name() : "");
            d.getLabores().add(linea);
        }
        if (laborIds.isEmpty()) {
            return d;
        }
        List<LaborInsumo> li = laborInsumoRepository.findByLaborIdInWithInsumo(laborIds);
        for (LaborInsumo u : li) {
            Insumo ins = u.getInsumo();
            HechosTrazabilidadDocumento.LineaInsumoLabor t = new HechosTrazabilidadDocumento.LineaInsumoLabor();
            t.setIdLabor(u.getLabor() != null ? u.getLabor().getId() : null);
            t.setFechaLabor(u.getLabor() != null ? u.getLabor().getFechaInicio() : null);
            t.setTipoLabor(u.getLabor() != null && u.getLabor().getTipoLabor() != null
                    ? u.getLabor().getTipoLabor().name() : "");
            t.setIdInsumo(ins.getId());
            t.setNombreInsumo(ins.getNombre());
            t.setTipoInsumo(ins.getTipo() != null ? ins.getTipo().name() : "");
            t.setCantidadUsada(u.getCantidadUsada() != null ? u.getCantidadUsada().toPlainString() : "");
            d.getInsumosPorLabor().add(t);
        }
        // Trazabilidad sanitaria en parcela: recrías asociadas al número de lote
        if (incluirEventosTodasLasRecriasDelLote) {
            for (Recria r : recriaRepository.findByLoteIdAndActivoTrue(loteId)) {
                List<EventoSanitario> re = eventoSanitarioRepository.findByEmpresaYEntidadConTipo(
                        empresa, EventoSanitario.TipoEntidad.LOTE, r.getId());
                añadirEventos(d, re, r.getId());
            }
        }
        return d;
    }

    private void añadirEventos(HechosTrazabilidadDocumento doc, List<EventoSanitario> re, Long recriaId) {
        for (EventoSanitario e : re) {
            if (!e.getActivo()) {
                continue;
            }
            HechosTrazabilidadDocumento.EventoSanitarioResumen s = new HechosTrazabilidadDocumento.EventoSanitarioResumen();
            s.setId(e.getId());
            s.setFecha(e.getFecha());
            s.setCategoria(e.getTipoEventoSanitario() != null && e.getTipoEventoSanitario().getCategoria() != null
                    ? e.getTipoEventoSanitario().getCategoria().name() : "");
            s.setNombreTipo(e.getTipoEventoSanitario() != null ? e.getTipoEventoSanitario().getNombre() : "");
            s.setRecriaId(recriaId);
            doc.getEventosSanitarios().add(s);
        }
    }

    /** Fecha efectiva de la labor: realizacion si existe, si no fecha de inicio planificada. */
    private static LocalDate fechaEfectivaLabor(Labor labor) {
        if (labor.getFechaRealizacion() != null) {
            return labor.getFechaRealizacion();
        }
        return labor.getFechaInicio();
    }

    private static boolean laborEnRangoDeFechas(Labor labor, LocalDate desde, LocalDate hasta) {
        LocalDate f = fechaEfectivaLabor(labor);
        if (f == null) {
            return false;
        }
        return !f.isBefore(desde) && !f.isAfter(hasta);
    }

    private void verificarEmpresa(Long idEmpresaEntidad, Empresa contexto) {
        if (idEmpresaEntidad == null || contexto.getId() == null || !idEmpresaEntidad.equals(contexto.getId())) {
            throw new com.agrocloud.trazabilidad.excepcion.TrazabilidadEntidadInaccesibleExcepcion(
                    "La entidad no pertenece a la empresa del usuario.");
        }
    }
}
