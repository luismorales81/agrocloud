package com.agrocloud.trazabilidad.application;

import com.agrocloud.avicola.crianza.model.entity.*;
import com.agrocloud.avicola.crianza.repository.*;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoConsumo;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoEventoSanitario;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoLote;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoProduccionDiaria;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoConsumoRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoEventoSanitarioRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoLoteRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoProduccionDiariaRepository;
import com.agrocloud.avicola.ponedoras.model.entity.*;
import com.agrocloud.avicola.ponedoras.repository.*;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.Campana;
import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import com.agrocloud.cultivos.domain.HistorialCosecha;
import com.agrocloud.porcinos.domain.*;
import com.agrocloud.porcinos.infrastructure.*;
import com.agrocloud.cultivos.domain.AplicacionAgroquimica;
import com.agrocloud.cultivos.infrastructure.AplicacionAgroquimicaRepository;
import com.agrocloud.cultivos.infrastructure.HistorialCosechaRepository;
import com.agrocloud.trazabilidad.domain.TrazabilidadCertificacion;
import com.agrocloud.trazabilidad.dto.HechosExpedienteTrazabilidad;
import com.agrocloud.trazabilidad.dto.HechosTrazabilidadDocumento;
import com.agrocloud.trazabilidad.excepcion.TrazabilidadEntidadInaccesibleExcepcion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Construye el snapshot estructurado del expediente de ciclo de vida (sin validación de reglas).
 */
@Service
@Transactional(readOnly = true)
public class TrazabilidadExpedienteConstruccionService {

    public static final String CODIGO_EXPEDIENTE = "EXPEDIENTE_CICLO_VIDA";

    @Autowired private TrazabilidadQueryService trazabilidadQueryService;
    @Autowired
    @Qualifier("campanaContextServiceCore")
    private CampanaContextService campanaContextService;
    @Autowired private HistorialCosechaRepository historialCosechaRepository;
    @Autowired private AplicacionAgroquimicaRepository aplicacionAgroquimicaRepository;
    @Autowired private PlotRepository plotRepository;
    @Autowired private RecriaRepository recriaRepository;
    @Autowired private VentaPorcinoRepository ventaPorcinoRepository;
    @Autowired private RegistroPesoRepository registroPesoRepository;
    @Autowired private MuerteRecriaRepository muerteRecriaRepository;
    @Autowired private ConsumoDiarioAutomaticoRepository consumoDiarioAutomaticoRepository;
    @Autowired private AvicolaHuevoLoteRepository avicolaHuevoLoteRepository;
    @Autowired private AvicolaHuevoProduccionDiariaRepository avicolaHuevoProduccionDiariaRepository;
    @Autowired private AvicolaHuevoConsumoRepository avicolaHuevoConsumoRepository;
    @Autowired private AvicolaHuevoEventoSanitarioRepository avicolaHuevoEventoSanitarioRepository;
    @Autowired private AvicolaLoteRepository avicolaLoteRepository;
    @Autowired private AvicolaPesadaRepository avicolaPesadaRepository;
    @Autowired private AvicolaConsumoRepository avicolaConsumoRepository;
    @Autowired private AvicolaEventoSanitarioRepository avicolaEventoSanitarioRepository;
    @Autowired private AvicolaMuerteRepository avicolaMuerteRepository;
    @Autowired private AvicolaVentaRepository avicolaVentaRepository;
    @Autowired private AvicolaPonedorasGalponRepository avicolaPonedorasGalponRepository;
    @Autowired private AvicolaPonedorasPosturaRepository avicolaPonedorasPosturaRepository;
    @Autowired private AvicolaPonedorasConsumoRepository avicolaPonedorasConsumoRepository;
    @Autowired private AvicolaPonedorasMuerteRepository avicolaPonedorasMuerteRepository;
    @Autowired private AvicolaPonedorasEventoSanitarioRepository avicolaPonedorasEventoSanitarioRepository;
    @Autowired private AvicolaPonedorasVentaHuevosRepository avicolaPonedorasVentaHuevosRepository;
    @Autowired private com.agrocloud.feedlot.repository.FeedlotLoteRepository feedlotLoteRepository;
    @Autowired private com.agrocloud.feedlot.repository.FeedlotPesadaRepository feedlotPesadaRepository;
    @Autowired private com.agrocloud.feedlot.repository.FeedlotConsumoRepository feedlotConsumoRepository;
    @Autowired private com.agrocloud.feedlot.repository.FeedlotMuerteRepository feedlotMuerteRepository;
    @Autowired private com.agrocloud.feedlot.repository.FeedlotEventoSanitarioRepository feedlotEventoSanitarioRepository;
    @Autowired private com.agrocloud.feedlot.repository.FeedlotVentaRepository feedlotVentaRepository;
    @Autowired private com.agrocloud.feedlot.service.ServicioFeedlotCloseout servicioFeedlotCloseout;

    public HechosExpedienteTrazabilidad construir(String entidadTipo, Long entidadId, Empresa empresa) {
        String tipo = entidadTipo.trim().toUpperCase();
        return switch (tipo) {
            case "LOTE" -> construirDesdeCultivo(tipo, entidadId, empresa, "Cultivos");
            case "COSECHA" -> construirCosecha(entidadId, empresa);
            case "RECRIA" -> construirRecria(entidadId, empresa);
            case "VENTA_PORCINO" -> construirVentaPorcino(entidadId, empresa);
            case "AVICOLA_HUEVOS" -> construirAvicolaHuevos(entidadId, empresa);
            case "AVICOLA_CRIANZA", "AVICOLA_CARNE" -> construirAvicolaLoteCrianza(entidadId, empresa);
            case "AVICOLA_PONEDORAS" -> construirAvicolaPonedoras(entidadId, empresa);
            case "FEEDLOT_LOTE" -> construirFeedlotLote(entidadId, empresa);
            default -> throw new IllegalArgumentException("entidadTipo no soportado: " + entidadTipo);
        };
    }

    private HechosExpedienteTrazabilidad construirDesdeCultivo(
            String entidadTipo, Long entidadId, Empresa empresa, String modulo) {
        HechosTrazabilidadDocumento hechos = trazabilidadQueryService.construirHechos(
                entidadTipo, entidadId, empresa, certificadoExpediente());
        HechosExpedienteTrazabilidad exp = baseExpediente(
                "Expediente de lote agrícola",
                hechos.getDescripcionAlcance(),
                modulo,
                entidadTipo,
                entidadId,
                empresa,
                hechos.getFechaCorteDesde(),
                hechos.getFechaCorteHasta());
        HechosExpedienteTrazabilidad.Seccion ident = nuevaSeccion("Identificación");
        agregarFila(ident, "Lote / parcela", valor(hechos.getNombreLote()));
        agregarFila(ident, "Empresa", valor(hechos.getNombreEmpresa()));
        exp.getSecciones().add(ident);
        incorporarHechosCultivo(exp, hechos);
        ordenarLineaTiempo(exp);
        return exp;
    }

    private HechosExpedienteTrazabilidad construirCosecha(Long idHistorial, Empresa empresa) {
        HistorialCosecha hc = historialCosechaRepository.findByIdConLoteCampoEmpresa(idHistorial)
                .orElseThrow(() -> new IllegalArgumentException("Cosecha no encontrada: " + idHistorial));
        verificarEmpresa(hc.getLote().getCampo().getEmpresa().getId(), empresa);
        HechosTrazabilidadDocumento hechosDoc = trazabilidadQueryService.construirHechos(
                "COSECHA", idHistorial, empresa, certificadoExpediente());
        HechosExpedienteTrazabilidad exp = baseExpediente(
                "Expediente de ciclo de cosecha",
                hechosDoc.getDescripcionAlcance(),
                "Cultivos",
                "COSECHA",
                idHistorial,
                empresa,
                hc.getFechaSiembra(),
                hc.getFechaCosecha());
        exp.setSubtitulo("Cultivo: " + (hc.getCultivo() != null ? hc.getCultivo().getNombre() : "-")
                + " | Lote: " + hc.getLote().getNombre());
        HechosExpedienteTrazabilidad.Seccion ident = nuevaSeccion("Identificación");
        agregarFila(ident, "Campo", hc.getLote().getCampo() != null ? valor(hc.getLote().getCampo().getNombre()) : "-");
        agregarFila(ident, "Lote / parcela", valor(hc.getLote().getNombre()));
        agregarFila(ident, "Cultivo", hc.getCultivo() != null ? valor(hc.getCultivo().getNombre()) : "-");
        agregarFila(ident, "Variedad", valor(hc.getVariedadSemilla()));
        agregarFila(ident, "Empresa", valor(hechosDoc.getNombreEmpresa()));
        exp.getSecciones().add(ident);
        HechosExpedienteTrazabilidad.Seccion ciclo = nuevaSeccion("Ciclo productivo");
        agregarFila(ciclo, "Fecha siembra", valor(hc.getFechaSiembra()));
        agregarFila(ciclo, "Fecha cosecha", valor(hc.getFechaCosecha()));
        agregarFila(ciclo, "Superficie (ha)", valor(hc.getSuperficieHectareas()));
        agregarFila(ciclo, "Cantidad cosechada",
                valor(hc.getCantidadCosechada()) + " " + valor(hc.getUnidadCosecha()));
        agregarFila(ciclo, "Rendimiento real",
                hc.getRendimientoReal() != null ? hc.getRendimientoReal().toPlainString() + " t/ha" : "-");
        if (hc.getObservaciones() != null && !hc.getObservaciones().isBlank()) {
            agregarFila(ciclo, "Observaciones", hc.getObservaciones());
        }
        exp.getSecciones().add(ciclo);
        incorporarHechosCultivo(exp, hechosDoc);
        incorporarAplicacionesAgroquimicas(exp, hechosDoc);
        agregarEvento(exp, hc.getFechaSiembra(), "Siembra",
                "Inicio de ciclo — " + (hc.getCultivo() != null ? hc.getCultivo().getNombre() : "cultivo"));
        agregarEvento(exp, hc.getFechaCosecha(), "Cosecha",
                "Cierre de ciclo — " + valor(hc.getCantidadCosechada()) + " " + valor(hc.getUnidadCosecha()));
        agregarSeccionSiVacioCultivo(exp, hechosDoc, hc.getFechaSiembra(), hc.getFechaCosecha());
        ordenarLineaTiempo(exp);
        return exp;
    }

    private HechosExpedienteTrazabilidad construirRecria(Long recriaId, Empresa empresa) {
        Recria recria = recriaRepository.findByIdAndEmpresa(recriaId, empresa)
                .orElseThrow(() -> new IllegalArgumentException("Recría no encontrada: " + recriaId));
        String nombreLote = plotRepository.findById(recria.getLoteId())
                .map(Plot::getNombre)
                .orElse("Parcela ID " + recria.getLoteId());
        HechosExpedienteTrazabilidad exp = construirDesdeCultivo("RECRIA", recriaId, empresa, "Porcinos");
        exp.setTitulo("Expediente de recría porcina");
        exp.setSubtitulo("Recría #" + recriaId + " en " + nombreLote);
        HechosExpedienteTrazabilidad.Seccion ident = exp.getSecciones().get(0);
        agregarFila(ident, "Fecha ingreso", valor(recria.getFechaIngreso()));
        agregarFila(ident, "Cantidad animales", valor(recria.getCantidadAnimales()));
        agregarFila(ident, "Etapa", valor(recria.getEtapa()));
        agregarFila(ident, "Origen", valor(recria.getOrigen()));
        agregarFila(ident, "Peso promedio actual", valor(recria.getPesoPromedio()));
        incorporarDatosRecria(exp, recria, empresa);
        ordenarLineaTiempo(exp);
        return exp;
    }

    private HechosExpedienteTrazabilidad construirVentaPorcino(Long ventaId, Empresa empresa) {
        VentaPorcino venta = ventaPorcinoRepository.findById(ventaId)
                .filter(v -> v.getActivo() && v.getEmpresa() != null && empresa.getId().equals(v.getEmpresa().getId()))
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada: " + ventaId));
        HechosExpedienteTrazabilidad exp = baseExpediente(
                "Expediente de venta porcina",
                "Venta #" + ventaId + " (" + venta.getTipo() + ")",
                "Porcinos",
                "VENTA_PORCINO",
                ventaId,
                empresa,
                venta.getFecha(),
                venta.getFecha());
        HechosExpedienteTrazabilidad.Seccion ventaSec = nuevaSeccion("Datos de venta / faena");
        agregarFila(ventaSec, "Fecha", valor(venta.getFecha()));
        agregarFila(ventaSec, "Tipo", valor(venta.getTipo()));
        agregarFila(ventaSec, "Cantidad", valor(venta.getCantidad()));
        agregarFila(ventaSec, "Peso promedio (kg)", valor(venta.getPesoPromedio()));
        agregarFila(ventaSec, "Precio por kg", valor(venta.getPrecioKg()));
        agregarFila(ventaSec, "Ingreso total", valor(venta.getIngresoTotal()));
        agregarFila(ventaSec, "Cliente", valor(venta.getCliente()));
        exp.getSecciones().add(ventaSec);
        agregarEvento(exp, venta.getFecha(), "Venta", "Venta " + venta.getTipo() + " - " + venta.getCantidad() + " animales");
        if (venta.getRecria() != null) {
            incorporarDatosRecria(exp, venta.getRecria(), empresa);
        }
        ordenarLineaTiempo(exp);
        return exp;
    }

    private HechosExpedienteTrazabilidad construirAvicolaHuevos(Long loteId, Empresa empresa) {
        AvicolaHuevoLote lote = avicolaHuevoLoteRepository.buscarPorIdYEmpresaId(loteId, empresa.getId())
                .orElseThrow(() -> new IllegalArgumentException("Lote huevos no encontrado: " + loteId));
        HechosExpedienteTrazabilidad exp = baseExpediente(
                "Expediente lote postura (huevos)",
                lote.getNombre(),
                "Avícola huevos",
                "AVICOLA_HUEVOS",
                loteId,
                empresa,
                lote.getFechaInicio(),
                lote.getFechaCierre());
        HechosExpedienteTrazabilidad.Seccion ident = nuevaSeccion("Identificación");
        agregarFila(ident, "Nombre", valor(lote.getNombre()));
        agregarFila(ident, "Establecimiento", lote.getEstablecimiento() != null ? valor(lote.getEstablecimiento().getNombre()) : "-");
        agregarFila(ident, "Raza", lote.getRaza() != null ? valor(lote.getRaza().getNombre()) : "-");
        agregarFila(ident, "Aves iniciales", valor(lote.getCantidadAvesInicial()));
        agregarFila(ident, "Aves actuales", valor(lote.getCantidadAvesActual()));
        agregarFila(ident, "Estado", valor(lote.getEstado()));
        exp.getSecciones().add(ident);
        agregarEvento(exp, lote.getFechaInicio(), "Alta", "Inicio lote " + lote.getNombre());
        for (AvicolaHuevoProduccionDiaria p : avicolaHuevoProduccionDiariaRepository.listarPorLoteIdYEmpresaId(loteId, empresa.getId())) {
            agregarEvento(exp, p.getFecha(), "Producción",
                    "Postura: " + p.getTotalHuevosDia() + " huevos (tam1-4 + rotos)");
        }
        HechosExpedienteTrazabilidad.Seccion consumos = nuevaSeccion("Consumos de alimento");
        for (AvicolaHuevoConsumo c : avicolaHuevoConsumoRepository.listarPorLoteIdYEmpresaId(loteId, empresa.getId())) {
            agregarFila(consumos, valor(c.getFecha()), "Insumo " + c.getInsumoId() + " cant. " + valor(c.getCantidad()));
            agregarEvento(exp, c.getFecha(), "Consumo", "Insumo " + c.getInsumoId() + " - " + c.getCantidad());
        }
        if (!consumos.getFilas().isEmpty()) {
            exp.getSecciones().add(consumos);
        }
        HechosExpedienteTrazabilidad.Seccion sanidad = nuevaSeccion("Sanidad");
        for (AvicolaHuevoEventoSanitario e : avicolaHuevoEventoSanitarioRepository.listarPorLoteIdYEmpresaId(loteId, empresa.getId())) {
            agregarFila(sanidad, valor(e.getFecha()), valor(e.getDescripcion()));
            agregarEvento(exp, e.getFecha(), "Sanidad", valor(e.getDescripcion()));
        }
        if (!sanidad.getFilas().isEmpty()) {
            exp.getSecciones().add(sanidad);
        }
        ordenarLineaTiempo(exp);
        return exp;
    }

    private HechosExpedienteTrazabilidad construirAvicolaLoteCrianza(Long loteId, Empresa empresa) {
        AvicolaLote lote = avicolaLoteRepository.buscarPorIdYEmpresaId(loteId, empresa.getId())
                .orElseThrow(() -> new IllegalArgumentException("Lote avícola no encontrado: " + loteId));
        return construirAvicolaLoteGenerico(
                lote, empresa, "AVICOLA_CRIANZA", "Avícola crianza",
                "Expediente lote avícola (crianza)",
                avicolaPesadaRepository.listarPorLoteIdYEmpresaId(loteId, empresa.getId()),
                avicolaConsumoRepository.listarPorLoteIdYEmpresaId(loteId, empresa.getId()),
                avicolaEventoSanitarioRepository.listarPorLoteIdYEmpresaId(loteId, empresa.getId()),
                avicolaMuerteRepository.listarPorLoteIdYEmpresaId(loteId, empresa.getId()),
                avicolaVentaRepository.listarPorLoteIdYEmpresaId(loteId, empresa.getId()));
    }

    private HechosExpedienteTrazabilidad construirAvicolaLoteGenerico(
            AvicolaLote lote,
            Empresa empresa,
            String entidadTipo,
            String modulo,
            String titulo,
            List<AvicolaPesada> pesadas,
            List<AvicolaConsumo> consumos,
            List<AvicolaEventoSanitario> eventos,
            List<AvicolaMuerte> muertes,
            List<AvicolaVenta> ventas) {
        HechosExpedienteTrazabilidad exp = baseExpediente(
                titulo,
                lote.getNombre(),
                modulo,
                entidadTipo,
                lote.getId(),
                empresa,
                lote.getFechaIngreso(),
                lote.getFechaSalida());
        HechosExpedienteTrazabilidad.Seccion ident = nuevaSeccion("Identificación");
        agregarFila(ident, "Nombre", valor(lote.getNombre()));
        agregarFila(ident, "Especie", valor(lote.getEspecie()));
        agregarFila(ident, "Origen", valor(lote.getOrigen()));
        agregarFila(ident, "Cantidad inicial", valor(lote.getCantidadInicial()));
        agregarFila(ident, "Cantidad actual", valor(lote.getCantidadAnimales()));
        agregarFila(ident, "Estado", valor(lote.getEstado()));
        exp.getSecciones().add(ident);
        agregarEvento(exp, lote.getFechaIngreso(), "Alta", "Ingreso lote " + lote.getNombre());
        for (AvicolaPesada p : pesadas) {
            agregarEvento(exp, p.getFecha(), "Pesada",
                    "Peso prom. " + valor(p.getPesoPromedio()) + " kg, " + valor(p.getCantidadPesada()) + " aves");
        }
        HechosExpedienteTrazabilidad.Seccion cons = nuevaSeccion("Consumos");
        for (AvicolaConsumo c : consumos) {
            agregarFila(cons, valor(c.getFecha()), "Insumo " + c.getInsumoId() + " - " + c.getCantidad());
            agregarEvento(exp, c.getFecha(), "Consumo", "Insumo " + c.getInsumoId());
        }
        if (!cons.getFilas().isEmpty()) {
            exp.getSecciones().add(cons);
        }
        for (AvicolaEventoSanitario e : eventos) {
            agregarEvento(exp, e.getFecha(), "Sanidad", valor(e.getDescripcion()));
        }
        for (AvicolaMuerte m : muertes) {
            agregarEvento(exp, m.getFecha(), "Mortalidad", m.getCantidad() + " aves - " + valor(m.getCausa()));
        }
        for (AvicolaVenta v : ventas) {
            agregarEvento(exp, v.getFecha(), "Venta",
                    v.getCantidad() + " aves, total " + valor(v.getTotal()));
        }
        ordenarLineaTiempo(exp);
        return exp;
    }

    private HechosExpedienteTrazabilidad construirAvicolaPonedoras(Long galponId, Empresa empresa) {
        AvicolaPonedorasGalpon g = avicolaPonedorasGalponRepository.buscarPorIdYEmpresaId(galponId, empresa.getId())
                .orElseThrow(() -> new IllegalArgumentException("Galpón no encontrado: " + galponId));
        HechosExpedienteTrazabilidad exp = baseExpediente(
                "Expediente galpón ponedoras",
                g.getNombre(),
                "Avícola ponedoras",
                "AVICOLA_PONEDORAS",
                galponId,
                empresa,
                g.getFechaIngreso(),
                g.getFechaCierre());
        HechosExpedienteTrazabilidad.Seccion ident = nuevaSeccion("Identificación");
        agregarFila(ident, "Nombre", valor(g.getNombre()));
        agregarFila(ident, "Raza", valor(g.getRaza()));
        agregarFila(ident, "Aves iniciales", valor(g.getCantidadInicial()));
        agregarFila(ident, "Aves actuales", valor(g.getCantidadAves()));
        agregarFila(ident, "Estado", valor(g.getEstado()));
        exp.getSecciones().add(ident);
        agregarEvento(exp, g.getFechaIngreso(), "Alta", "Ingreso galpón " + g.getNombre());
        for (AvicolaPonedorasPostura p : avicolaPonedorasPosturaRepository.buscarPorGalponIdYEmpresaId(galponId, empresa.getId())) {
            agregarEvento(exp, p.getFecha(), "Postura",
                    p.getCantidad() + " huevos (" + valor(p.getCategoriaHuevo()) + ")");
        }
        for (AvicolaPonedorasConsumo c : avicolaPonedorasConsumoRepository.buscarPorGalponIdYEmpresaId(galponId, empresa.getId())) {
            agregarEvento(exp, c.getFecha(), "Consumo", "Insumo " + c.getInsumoId() + " - " + c.getCantidad());
        }
        for (AvicolaPonedorasMuerte m : avicolaPonedorasMuerteRepository.buscarPorGalponIdYEmpresaId(galponId, empresa.getId())) {
            agregarEvento(exp, m.getFecha(), "Mortalidad", m.getCantidad() + " aves");
        }
        for (AvicolaPonedorasEventoSanitario e : avicolaPonedorasEventoSanitarioRepository.buscarPorGalponIdYEmpresaId(galponId, empresa.getId())) {
            agregarEvento(exp, e.getFecha(), "Sanidad", valor(e.getDescripcion()));
        }
        for (AvicolaPonedorasVentaHuevos v : avicolaPonedorasVentaHuevosRepository.buscarPorGalponIdYEmpresaId(galponId, empresa.getId())) {
            agregarEvento(exp, v.getFecha(), "Venta huevos",
                    v.getCantidad() + " huevos, total " + valor(v.getTotal()));
        }
        ordenarLineaTiempo(exp);
        return exp;
    }

    private HechosExpedienteTrazabilidad construirFeedlotLote(Long loteId, Empresa empresa) {
        com.agrocloud.feedlot.model.entity.FeedlotLote lote = feedlotLoteRepository.buscarPorIdYEmpresaId(loteId, empresa.getId())
                .orElseThrow(() -> new IllegalArgumentException("Lote feedlot no encontrado: " + loteId));
        verificarEmpresa(lote.getEmpresaId(), empresa);
        LocalDate hasta = lote.getFechaCierre() != null ? lote.getFechaCierre() : LocalDate.now();
        HechosExpedienteTrazabilidad exp = baseExpediente(
                "Expediente lote feedlot",
                lote.getNombre(),
                "Feedlot",
                "FEEDLOT_LOTE",
                loteId,
                empresa,
                lote.getFechaIngreso(),
                hasta);
        HechosExpedienteTrazabilidad.Seccion ident = nuevaSeccion("Identificación");
        agregarFila(ident, "Nombre", valor(lote.getNombre()));
        agregarFila(ident, "Corral", lote.getCorral() != null ? valor(lote.getCorral().getNombre()) : "-");
        agregarFila(ident, "Categoría", lote.getCategoria() != null ? valor(lote.getCategoria().getNombre()) : "-");
        agregarFila(ident, "Cabezas iniciales", valor(lote.getCabezasInicial()));
        agregarFila(ident, "Cabezas actuales", valor(lote.getCabezasActuales()));
        agregarFila(ident, "Estado", valor(lote.getEstado()));
        agregarFila(ident, "Fecha ingreso", valor(lote.getFechaIngreso()));
        agregarFila(ident, "Fecha cierre", valor(lote.getFechaCierre()));
        exp.getSecciones().add(ident);

        agregarEvento(exp, lote.getFechaIngreso(), "Ingreso",
                "Alta lote " + lote.getNombre() + " — " + lote.getCabezasInicial() + " cabezas");

        for (com.agrocloud.feedlot.model.entity.FeedlotPesada p
                : feedlotPesadaRepository.listarPorLoteIdYEmpresaId(loteId, empresa.getId())) {
            agregarEvento(exp, p.getFecha(), "Pesada", "Peso promedio " + valor(p.getPesoPromedioKg()) + " kg");
        }
        for (com.agrocloud.feedlot.model.entity.FeedlotConsumo c
                : feedlotConsumoRepository.listarPorLoteIdYEmpresaId(loteId, empresa.getId())) {
            agregarEvento(exp, c.getFecha(), "Consumo", "Alimento " + valor(c.getCantidadKg()) + " kg");
        }
        for (com.agrocloud.feedlot.model.entity.FeedlotMuerte m
                : feedlotMuerteRepository.listarPorLoteIdYEmpresaId(loteId, empresa.getId())) {
            agregarEvento(exp, m.getFecha(), "Mortalidad", m.getCabezas() + " cabezas");
        }
        for (com.agrocloud.feedlot.model.entity.FeedlotEventoSanitario s
                : feedlotEventoSanitarioRepository.listarPorLoteIdYEmpresaId(loteId, empresa.getId())) {
            agregarEvento(exp, s.getFecha(), "Sanidad",
                    valor(s.getTipo()) + (s.getDescripcion() != null ? " — " + s.getDescripcion() : ""));
        }
        for (com.agrocloud.feedlot.model.entity.FeedlotVenta v
                : feedlotVentaRepository.listarPorLoteIdYEmpresaId(loteId, empresa.getId())) {
            agregarEvento(exp, v.getFecha(), "Venta/Faena",
                    valor(v.getTipo()) + " — " + v.getCabezas() + " cabezas");
        }

        com.agrocloud.feedlot.model.dto.FeedlotCloseoutRespuesta closeout =
                servicioFeedlotCloseout.construirCloseout(lote, empresa.getId());
        HechosExpedienteTrazabilidad.Seccion kpis = nuevaSeccion("Indicadores de cierre");
        agregarFila(kpis, "GMD (kg/día)", valor(closeout.getGmd()));
        agregarFila(kpis, "Conversión alimenticia", valor(closeout.getConversionAlimenticia()));
        agregarFila(kpis, "Mortalidad %", valor(closeout.getMortalidadPct()));
        agregarFila(kpis, "Margen", valor(closeout.getMargen()));
        agregarFila(kpis, "Método closeout", valor(closeout.getMetodoCloseout()));
        exp.getSecciones().add(kpis);

        ordenarLineaTiempo(exp);
        return exp;
    }

    private void incorporarAplicacionesAgroquimicas(
            HechosExpedienteTrazabilidad exp, HechosTrazabilidadDocumento hechos) {
        List<Long> laborIds = hechos.getLabores().stream()
                .map(HechosTrazabilidadDocumento.LineaHechoLabor::getIdLabor)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (laborIds.isEmpty()) {
            return;
        }
        List<AplicacionAgroquimica> apps = aplicacionAgroquimicaRepository.findActivasByLaborIdIn(laborIds);
        if (apps.isEmpty()) {
            return;
        }
        HechosExpedienteTrazabilidad.Seccion sec = nuevaSeccion("Aplicaciones agroquimicas");
        for (AplicacionAgroquimica a : apps) {
            String insumo = a.getInsumo() != null ? a.getInsumo().getNombre() : "Insumo";
            LocalDate fecha = a.getFechaAplicacion() != null ? a.getFechaAplicacion().toLocalDate() : null;
            String det = insumo + " — " + valor(a.getCantidadTotalAplicar()) + " "
                    + valor(a.getUnidadMedida()) + " (" + valor(a.getTipoAplicacion()) + ")";
            Long idLabor = a.getLabor() != null ? a.getLabor().getId() : null;
            agregarFila(sec, fecha != null ? fecha.toString() : "Labor " + valor(idLabor), det);
            agregarEvento(exp, fecha, "Agroquimico", det);
        }
        exp.getSecciones().add(sec);
    }

    private void agregarSeccionSiVacioCultivo(
            HechosExpedienteTrazabilidad exp,
            HechosTrazabilidadDocumento hechos,
            java.time.LocalDate desde,
            java.time.LocalDate hasta) {
        boolean tieneLabores = hechos.getLabores() != null && !hechos.getLabores().isEmpty();
        boolean tieneInsumos = hechos.getInsumosPorLabor() != null && !hechos.getInsumosPorLabor().isEmpty();
        if (!tieneLabores && !tieneInsumos) {
            HechosExpedienteTrazabilidad.Seccion aviso = nuevaSeccion("Labores e insumos del ciclo");
            agregarFila(aviso, "Periodo analizado", desde + " a " + hasta);
            agregarFila(aviso, "Registros",
                    "No hay labores ni insumos registrados en cultivo_labores para este lote en ese rango de fechas.");
            exp.getSecciones().add(aviso);
        }
    }

    private void incorporarHechosCultivo(HechosExpedienteTrazabilidad exp, HechosTrazabilidadDocumento hechos) {
        HechosExpedienteTrazabilidad.Seccion labores = nuevaSeccion("Labores");
        for (HechosTrazabilidadDocumento.LineaHechoLabor l : hechos.getLabores()) {
            agregarFila(labores, valor(l.getFecha()), l.getTipoLabor() + " (" + l.getEstado() + ")");
            agregarEvento(exp, l.getFecha(), "Labor", "Labor " + l.getTipoLabor() + " #" + l.getIdLabor());
        }
        if (!labores.getFilas().isEmpty()) {
            exp.getSecciones().add(labores);
        }
        HechosExpedienteTrazabilidad.Seccion insumos = nuevaSeccion("Insumos en labores");
        for (HechosTrazabilidadDocumento.LineaInsumoLabor li : hechos.getInsumosPorLabor()) {
            String det = li.getNombreInsumo() + " (" + li.getTipoInsumo() + ") " + li.getCantidadUsada();
            agregarFila(insumos, "Labor " + li.getIdLabor(), det);
            agregarEvento(exp, li.getFechaLabor(), "Insumo", det);
        }
        if (!insumos.getFilas().isEmpty()) {
            exp.getSecciones().add(insumos);
        }
        HechosExpedienteTrazabilidad.Seccion sanidad = nuevaSeccion("Eventos sanitarios (parcela)");
        for (HechosTrazabilidadDocumento.EventoSanitarioResumen e : hechos.getEventosSanitarios()) {
            agregarFila(sanidad, valor(e.getFecha()), e.getCategoria() + " - " + e.getNombreTipo());
            agregarEvento(exp, e.getFecha(), "Sanidad", e.getNombreTipo());
        }
        if (!sanidad.getFilas().isEmpty()) {
            exp.getSecciones().add(sanidad);
        }
    }

    private void incorporarDatosRecria(HechosExpedienteTrazabilidad exp, Recria recria, Empresa empresa) {
        HechosExpedienteTrazabilidad.Seccion pesadas = nuevaSeccion("Pesadas");
        for (RegistroPeso rp : registroPesoRepository.findByRecriaOrderByFechaPesajeDesc(recria)) {
            agregarFila(pesadas, valor(rp.getFechaPesaje()),
                    rp.getPesoPromedio() + " kg (" + rp.getCantidadAnimales() + " animales)");
            agregarEvento(exp, rp.getFechaPesaje(), "Pesada",
                    "Peso prom. " + rp.getPesoPromedio() + " kg");
        }
        if (!pesadas.getFilas().isEmpty()) {
            exp.getSecciones().add(pesadas);
        }
        HechosExpedienteTrazabilidad.Seccion muertes = nuevaSeccion("Mortalidad");
        for (MuerteRecria m : muerteRecriaRepository.findByRecriaAndActivoTrue(recria)) {
            agregarFila(muertes, valor(m.getFecha()), m.getCantidad() + " - " + valor(m.getCausa()));
            agregarEvento(exp, m.getFecha(), "Mortalidad", m.getCantidad() + " animales");
        }
        if (!muertes.getFilas().isEmpty()) {
            exp.getSecciones().add(muertes);
        }
        HechosExpedienteTrazabilidad.Seccion consumos = nuevaSeccion("Consumo de alimento");
        for (ConsumoDiarioAutomatico c : consumoDiarioAutomaticoRepository.findByRecria(recria)) {
            String det = "Etapa " + valor(c.getEtapaAlimentacion())
                    + ", cant. " + valor(c.getCantidadRecetaEfectiva()) + " kg";
            agregarFila(consumos, valor(c.getDiaAlimentacion() != null ? c.getDiaAlimentacion().getFecha() : null), det);
            if (c.getDiaAlimentacion() != null) {
                agregarEvento(exp, c.getDiaAlimentacion().getFecha(), "Alimentación", det);
            }
        }
        if (!consumos.getFilas().isEmpty()) {
            exp.getSecciones().add(consumos);
        }
    }

    private HechosExpedienteTrazabilidad baseExpediente(
            String titulo,
            String subtitulo,
            String modulo,
            String entidadTipo,
            Long entidadId,
            Empresa empresa,
            java.time.LocalDate desde,
            java.time.LocalDate hasta) {
        HechosExpedienteTrazabilidad exp = new HechosExpedienteTrazabilidad();
        exp.setTitulo(titulo);
        exp.setSubtitulo(subtitulo);
        exp.setModulo(modulo);
        exp.setEntidadTipo(entidadTipo);
        exp.setEntidadId(entidadId);
        exp.setNombreEmpresa(empresa.getNombre());
        exp.setFechaCorteDesde(desde);
        exp.setFechaCorteHasta(hasta);
        enriquecerConCampanaActiva(exp, empresa);
        return exp;
    }

    private void enriquecerConCampanaActiva(HechosExpedienteTrazabilidad exp, Empresa empresa) {
        try {
            Campana campana = campanaContextService.resolverCampanaActiva(empresa.getId());
            exp.setCampanaId(campana.getId());
            exp.setCampanaCodigo(campana.getCodigo());
            exp.setCampanaNombre(campana.getNombre());
        } catch (Exception ignored) {
        }
    }

    private static TrazabilidadCertificacion certificadoExpediente() {
        TrazabilidadCertificacion c = new TrazabilidadCertificacion();
        c.setCodigo(CODIGO_EXPEDIENTE);
        c.setDescripcion("Expediente ciclo de vida");
        return c;
    }

    private static HechosExpedienteTrazabilidad.Seccion nuevaSeccion(String titulo) {
        HechosExpedienteTrazabilidad.Seccion s = new HechosExpedienteTrazabilidad.Seccion();
        s.setTitulo(titulo);
        return s;
    }

    private static void agregarFila(HechosExpedienteTrazabilidad.Seccion s, String etiqueta, String valor) {
        s.getFilas().add(new HechosExpedienteTrazabilidad.Fila(etiqueta, valor));
    }

    private static void agregarEvento(
            HechosExpedienteTrazabilidad exp, java.time.LocalDate fecha, String categoria, String descripcion) {
        exp.getLineaTiempo().add(new HechosExpedienteTrazabilidad.EventoLinea(fecha, categoria, descripcion));
    }

    private static void ordenarLineaTiempo(HechosExpedienteTrazabilidad exp) {
        exp.getLineaTiempo().sort(Comparator.comparing(
                HechosExpedienteTrazabilidad.EventoLinea::getFecha,
                Comparator.nullsLast(Comparator.naturalOrder())));
    }

    private static String valor(Object o) {
        return o == null ? "-" : Objects.toString(o);
    }

    private void verificarEmpresa(Long idEmpresaEntidad, Empresa contexto) {
        if (idEmpresaEntidad == null || contexto.getId() == null || !idEmpresaEntidad.equals(contexto.getId())) {
            throw new TrazabilidadEntidadInaccesibleExcepcion(
                    "La entidad no pertenece a la empresa del usuario.");
        }
    }
}
