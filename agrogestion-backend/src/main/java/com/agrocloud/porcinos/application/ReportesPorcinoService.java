package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.application.port.LoteParaPorcinosQuery;
import com.agrocloud.cultivos.domain.Cultivo;

import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.domain.VentaPorcino;
import com.agrocloud.porcinos.infrastructure.RecriaRepository;
import com.agrocloud.porcinos.domain.MuerteRecria;
import com.agrocloud.porcinos.infrastructure.MuerteRecriaRepository;
import com.agrocloud.porcinos.domain.Padrillo;
import com.agrocloud.porcinos.domain.Destete;
import com.agrocloud.porcinos.domain.Gestacion;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.Parto;
import com.agrocloud.porcinos.domain.Servicio;
import com.agrocloud.porcinos.infrastructure.DesteteRepository;
import com.agrocloud.porcinos.infrastructure.GestacionRepository;
import com.agrocloud.porcinos.infrastructure.MadreRepository;
import com.agrocloud.porcinos.infrastructure.PadrilloRepository;
import com.agrocloud.porcinos.domain.EventoSanitario;
import com.agrocloud.porcinos.domain.MadreMuerte;
import com.agrocloud.porcinos.domain.MuerteLechon;
import com.agrocloud.porcinos.infrastructure.EventoSanitarioRepository;
import com.agrocloud.porcinos.infrastructure.MadreMuerteRepository;
import com.agrocloud.porcinos.infrastructure.MuerteLechonRepository;
import com.agrocloud.porcinos.infrastructure.PartoRepository;
import com.agrocloud.porcinos.infrastructure.ServicioRepository;
import com.agrocloud.porcinos.infrastructure.VentaPorcinoRepository;
import com.agrocloud.porcinos.domain.ConsumoDiarioAutomatico;
import com.agrocloud.porcinos.domain.ConsumoDiarioDetalle;
import com.agrocloud.porcinos.domain.DatosEconomicosPorcino;
import com.agrocloud.porcinos.domain.ParametrosProductivosPorcino;
import com.agrocloud.porcinos.infrastructure.ConsumoAlimentoRepository;
import com.agrocloud.porcinos.infrastructure.ConsumoDiarioAutomaticoRepository;
import com.agrocloud.porcinos.infrastructure.ConsumoDiarioDetalleRepository;
import com.agrocloud.porcinos.infrastructure.DatosEconomicosPorcinoRepository;
import com.agrocloud.porcinos.infrastructure.DiaAlimentacionRepository;
import com.agrocloud.porcinos.infrastructure.ParametrosProductivosPorcinoRepository;
import com.agrocloud.core.inventory.domain.ComponenteInsumoCompuesto;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import com.agrocloud.core.inventory.domain.InventarioGrano;
import com.agrocloud.core.inventory.infrastructure.ComponenteInsumoCompuestoRepository;
import com.agrocloud.core.inventory.infrastructure.InsumoCompuestoRepository;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import com.agrocloud.core.inventory.infrastructure.InventarioGranoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generar datos de reportes del módulo Porcinos
 */
@Service
public class ReportesPorcinoService {

    @Autowired
    private MadreRepository madreRepository;
    
    @Autowired
    private PadrilloRepository padrilloRepository;
    
    @Autowired
    private ServicioRepository servicioRepository;
    
    @Autowired
    private GestacionRepository gestacionRepository;
    
    @Autowired
    private PartoRepository partoRepository;
    
    @Autowired
    private DesteteRepository desteteRepository;
    
    @Autowired
    private RecriaRepository recriaRepository;
    
    @Autowired
    private MuerteLechonRepository muerteLechonRepository;
    
    @Autowired
    private MuerteRecriaRepository muerteRecriaRepository;
    
    @Autowired
    private MadreMuerteRepository madreMuerteRepository;
    
    @Autowired
    private ConsumoAlimentoRepository consumoAlimentoRepository; // Mantener para retrocompatibilidad
    
    @Autowired
    private ConsumoDiarioAutomaticoRepository consumoDiarioAutomaticoRepository;
    
    @Autowired
    private ConsumoDiarioDetalleRepository consumoDiarioDetalleRepository;
    
    @Autowired
    private DiaAlimentacionRepository diaAlimentacionRepository;
    
    @Autowired
    @Qualifier("insumoRepositoryInventario")
        private InsumoRepository insumoRepository;
    
    @Autowired
    @Qualifier("insumoCompuestoRepositoryInventario")
        private InsumoCompuestoRepository insumoCompuestoRepository;

    @Autowired
    @Qualifier("componenteInsumoCompuestoRepositoryInventario")
        private ComponenteInsumoCompuestoRepository componenteRepository;
    
    @Autowired
    @Qualifier("inventarioGranoRepositoryInventario")
        private InventarioGranoRepository inventarioGranoRepository;
    
    @Autowired
    private VentaPorcinoRepository ventaPorcinoRepository;
    
    @Autowired
    private EventoSanitarioRepository eventoSanitarioRepository;
    
    @Autowired
    private ParametrosProductivosPorcinoRepository parametrosProductivosRepository;
    
    @Autowired
    private DatosEconomicosPorcinoRepository datosEconomicosRepository;
    
    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private LoteParaPorcinosQuery loteParaPorcinosQuery;

    // ============================================================================
    // REPORTE REPRODUCTIVO
    // ============================================================================

    @Transactional(readOnly = true)
    public Map<String, Object> generarReporteReproductivo(Long userId, LocalDate fechaInicio, LocalDate fechaFin) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> reporte = new HashMap<>();
        
        // Servicios
        List<Servicio> servicios = servicioRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
            .filter(s -> !s.getFechaServicio().isBefore(fechaInicio) && !s.getFechaServicio().isAfter(fechaFin))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> serviciosData = servicios.stream().map(s -> {
            Map<String, Object> row = new HashMap<>();
            row.put("Fecha", s.getFechaServicio());
            row.put("Madre", s.getMadre().getIdentificacion());
            row.put("Tipo", s.getTipo().toString());
            row.put("Padrillo", s.getMachoNombre() != null ? s.getMachoNombre() : "N/A");
            row.put("Estado", s.getEstadoServicio().toString());
            row.put("Número Intento", s.getNumeroIntento());
            return row;
        }).collect(Collectors.toList());
        
        // Gestaciones
        List<Gestacion> gestaciones = gestacionRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
            .filter(g -> !g.getFechaInicio().isBefore(fechaInicio) && !g.getFechaInicio().isAfter(fechaFin))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> gestacionesData = gestaciones.stream().map(g -> {
            Map<String, Object> row = new HashMap<>();
            row.put("Fecha Inicio", g.getFechaInicio());
            row.put("Fecha Probable Parto", g.getFechaProbableParto());
            row.put("Madre", g.getMadre().getIdentificacion());
            row.put("Estado", g.getEstado().toString());
            return row;
        }).collect(Collectors.toList());
        
        // Partos
        List<Parto> partos = partoRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
            .filter(p -> !p.getFechaInicio().toLocalDate().isBefore(fechaInicio) 
                      && !p.getFechaInicio().toLocalDate().isAfter(fechaFin))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> partosData = partos.stream().map(p -> {
            Map<String, Object> row = new HashMap<>();
            row.put("Fecha", p.getFechaInicio().toLocalDate());
            row.put("Madre", p.getMadre().getIdentificacion());
            row.put("Nacidos Vivos", p.getNacidosVivos());
            row.put("Nacidos Muertos", p.getNacidosMuertos());
            row.put("Momias", p.getMomias());
            row.put("Total Nacidos", p.getTotalNacidos());
            row.put("Peso Promedio Nacimiento", p.getPesoPromedioNacimiento());
            return row;
        }).collect(Collectors.toList());
        
        // Destetes
        List<Destete> destetes = desteteRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
            .filter(d -> !d.getFechaDestete().isBefore(fechaInicio) && !d.getFechaDestete().isAfter(fechaFin))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> destetesData = destetes.stream().map(d -> {
            Map<String, Object> row = new HashMap<>();
            row.put("Fecha Destete", d.getFechaDestete());
            row.put("Madre", d.getParto().getMadre().getIdentificacion());
            row.put("Cantidad Destetados", d.getCantidadDestetados());
            row.put("Peso Promedio Destete", d.getPesoPromedioDestete());
            row.put("Días Lactancia", d.getDiasLactancia());
            return row;
        }).collect(Collectors.toList());
        
        // Índices calculados
        long serviciosExitosos = servicios.stream()
            .filter(s -> s.getEstadoServicio() == Servicio.EstadoServicio.PREÑEZ_CONFIRMADA)
            .count();
        BigDecimal tasaParicion = servicios.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(serviciosExitosos)
                .divide(BigDecimal.valueOf(servicios.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        BigDecimal lechonesVivosPromedio = partos.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(partos.stream().mapToInt(Parto::getNacidosVivos).sum())
                .divide(BigDecimal.valueOf(partos.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal lechonesDestetadosPromedio = destetes.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(destetes.stream().mapToInt(Destete::getCantidadDestetados).sum())
                .divide(BigDecimal.valueOf(destetes.size()), 2, RoundingMode.HALF_UP);
        
        Map<String, Object> indices = new HashMap<>();
        indices.put("Tasa de Parición (%)", tasaParicion.setScale(2, RoundingMode.HALF_UP));
        indices.put("Lechones Vivos por Parto", lechonesVivosPromedio.setScale(2, RoundingMode.HALF_UP));
        indices.put("Lechones Destetados por Parto", lechonesDestetadosPromedio.setScale(2, RoundingMode.HALF_UP));
        indices.put("Total Servicios", servicios.size());
        indices.put("Total Gestaciones", gestaciones.size());
        indices.put("Total Partos", partos.size());
        indices.put("Total Destetes", destetes.size());
        
        reporte.put("servicios", serviciosData);
        reporte.put("gestaciones", gestacionesData);
        reporte.put("partos", partosData);
        reporte.put("destetes", destetesData);
        reporte.put("indices", indices);
        
        return reporte;
    }

    // ============================================================================
    // REPORTE DE MORTALIDAD
    // ============================================================================

    @Transactional(readOnly = true)
    public Map<String, Object> generarReporteMortalidad(Long userId, LocalDate fechaInicio, LocalDate fechaFin) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> reporte = new HashMap<>();
        
        // Muertes de madres
        List<MadreMuerte> muertesMadres = madreMuerteRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
            .filter(m -> !m.getFecha().isBefore(fechaInicio) && !m.getFecha().isAfter(fechaFin))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> muertesMadresData = muertesMadres.stream().map(m -> {
            Map<String, Object> row = new HashMap<>();
            row.put("Fecha", m.getFecha());
            row.put("Madre", m.getMadre().getIdentificacion());
            row.put("Causa", m.getCausa() != null ? m.getCausa().toString() : "N/A");
            row.put("Número Partos", m.getMadre().getNumeroPartos());
            row.put("Observaciones", m.getObservaciones() != null ? m.getObservaciones() : "N/A");
            return row;
        }).collect(Collectors.toList());
        
        // Muertes de lechones
        List<MuerteLechon> muertesLechones = muerteLechonRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
            .filter(m -> !m.getFecha().isBefore(fechaInicio) && !m.getFecha().isAfter(fechaFin))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> muertesLechonesData = muertesLechones.stream().map(m -> {
            Map<String, Object> row = new HashMap<>();
            row.put("Fecha", m.getFecha());
            row.put("Madre", m.getMadre().getIdentificacion());
            row.put("Etapa", m.getEtapa().toString());
            row.put("Causa", m.getCausaMortalidad() != null ? m.getCausaMortalidad().getNombre() : m.getCausa().toString());
            row.put("Cantidad", m.getCantidad());
            return row;
        }).collect(Collectors.toList());
        
        // Muertes en recría
        List<MuerteRecria> muertesRecria = muerteRecriaRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
            .filter(m -> !m.getFecha().isBefore(fechaInicio) && !m.getFecha().isAfter(fechaFin))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> muertesRecriaData = muertesRecria.stream().map(m -> {
            Map<String, Object> row = new HashMap<>();
            row.put("Fecha", m.getFecha());
            row.put("Recría ID", m.getRecria().getId());
            row.put("Causa", m.getCausaMortalidad() != null ? m.getCausaMortalidad().getNombre() : m.getCausa().toString());
            row.put("Cantidad", m.getCantidad());
            row.put("Peso Promedio", m.getPesoPromedioSiAplica());
            return row;
        }).collect(Collectors.toList());
        
        // Estadísticas
        int totalMuertesMadres = muertesMadres.size();
        int totalMuertesLechones = muertesLechones.stream().mapToInt(MuerteLechon::getCantidad).sum();
        int totalMuertesRecria = muertesRecria.stream().mapToInt(MuerteRecria::getCantidad).sum();
        
        // Agrupar por causa
        Map<String, Long> causasLechones = muertesLechones.stream()
            .collect(Collectors.groupingBy(
                m -> m.getCausaMortalidad() != null ? m.getCausaMortalidad().getNombre() : m.getCausa().toString(),
                Collectors.summingLong(MuerteLechon::getCantidad)
            ));
        
        Map<String, Long> causasRecria = muertesRecria.stream()
            .collect(Collectors.groupingBy(
                m -> m.getCausaMortalidad() != null ? m.getCausaMortalidad().getNombre() : m.getCausa().toString(),
                Collectors.summingLong(MuerteRecria::getCantidad)
            ));
        
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("Total Muertes Madres", totalMuertesMadres);
        estadisticas.put("Total Muertes Lechones", totalMuertesLechones);
        estadisticas.put("Total Muertes Recría", totalMuertesRecria);
        estadisticas.put("Causas Lechones", causasLechones);
        estadisticas.put("Causas Recría", causasRecria);
        
        reporte.put("muertesMadres", muertesMadresData);
        reporte.put("muertesLechones", muertesLechonesData);
        reporte.put("muertesRecria", muertesRecriaData);
        reporte.put("estadisticas", estadisticas);
        
        return reporte;
    }

    // ============================================================================
    // REPORTE PRODUCTIVO (KPIs)
    // ============================================================================

    @Transactional(readOnly = true)
    public Map<String, Object> generarReporteProductivo(Long userId, LocalDate fechaInicio, LocalDate fechaFin) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> reporte = new HashMap<>();
        
        // KPIs básicos (no se filtran por fecha, son valores actuales)
        long totalMadres = madreRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
            .filter(m -> m.getEstadoActual() != Madre.EstadoMadre.DESCARTE)
            .count();
        long totalPadrillos = padrilloRepository.findByEmpresaAndActivoTrue(empresa.get()).size();
        long gestacionesActivas = gestacionRepository.findByEmpresaAndActivas(empresa.get()).size();
        
        LocalDate hoy = LocalDate.now();
        List<Gestacion> proximosPartos = gestacionRepository.findProximasPartos(empresa.get(), hoy, hoy.plusDays(7));
        
        // Calcular índices (filtrar por fecha si están presentes)
        List<Servicio> servicios = servicioRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
            .filter(s -> {
                if (fechaInicio != null && s.getFechaServicio().isBefore(fechaInicio)) {
                    return false;
                }
                if (fechaFin != null && s.getFechaServicio().isAfter(fechaFin)) {
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());
        
        long serviciosExitosos = servicios.stream()
            .filter(s -> s.getEstadoServicio() == Servicio.EstadoServicio.PREÑEZ_CONFIRMADA)
            .count();
        BigDecimal tasaParicion = servicios.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(serviciosExitosos)
                .divide(BigDecimal.valueOf(servicios.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        List<Parto> partos = partoRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
            .filter(p -> {
                if (fechaInicio != null && p.getFechaInicio().toLocalDate().isBefore(fechaInicio)) {
                    return false;
                }
                if (fechaFin != null && p.getFechaInicio().toLocalDate().isAfter(fechaFin)) {
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());
        
        BigDecimal lechonesVivosPromedio = partos.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(partos.stream().mapToInt(Parto::getNacidosVivos).sum())
                .divide(BigDecimal.valueOf(partos.size()), 2, RoundingMode.HALF_UP);
        
        List<Destete> destetes = desteteRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
            .filter(d -> {
                if (fechaInicio != null && d.getFechaDestete().isBefore(fechaInicio)) {
                    return false;
                }
                if (fechaFin != null && d.getFechaDestete().isAfter(fechaFin)) {
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());
        
        BigDecimal lechonesDestetadosPromedio = destetes.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(destetes.stream().mapToInt(Destete::getCantidadDestetados).sum())
                .divide(BigDecimal.valueOf(destetes.size()), 2, RoundingMode.HALF_UP);
        
        // Obtener objetivos
        Optional<ParametrosProductivosPorcino> parametros = parametrosProductivosRepository.findByEmpresa(empresa.get());
        
        Map<String, Object> kpis = new HashMap<>();
        kpis.put("Total Madres", totalMadres);
        kpis.put("Total Padrillos", totalPadrillos);
        kpis.put("Gestaciones Activas", gestacionesActivas);
        kpis.put("Partos Próximos (7 días)", proximosPartos.size());
        kpis.put("Tasa de Parición (%)", tasaParicion.setScale(2, RoundingMode.HALF_UP));
        kpis.put("Lechones Vivos/Parto", lechonesVivosPromedio.setScale(2, RoundingMode.HALF_UP));
        kpis.put("Lechones Destetados/Parto", lechonesDestetadosPromedio.setScale(2, RoundingMode.HALF_UP));
        
        if (parametros.isPresent()) {
            kpis.put("Objetivo Lechones Vivos/Parto", parametros.get().getLechonesVivosPartoObjetivo());
            kpis.put("Objetivo Lechones Destetados/Parto", parametros.get().getLechonesDestetadosObjetivo());
            kpis.put("Objetivo Partos/Madre/Año", parametros.get().getPartosMadreAnioObjetivo());
        }
        
        reporte.put("kpis", kpis);
        reporte.put("fechaGeneracion", LocalDate.now());
        
        return reporte;
    }

    // ============================================================================
    // REPORTE DE ALIMENTACIÓN
    // ============================================================================

    @Transactional(readOnly = true)
    public Map<String, Object> generarReporteAlimentacion(Long userId, LocalDate fechaInicio, LocalDate fechaFin) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> reporte = new HashMap<>();
        
        // Obtener consumos diarios automáticos del rango de fechas usando el nuevo sistema
        List<ConsumoDiarioAutomatico> consumosDiarios = consumoDiarioAutomaticoRepository
            .findByEmpresaAndFechaBetween(empresa.get(), fechaInicio, fechaFin);
        
        List<Map<String, Object>> consumosData = new ArrayList<>();
        BigDecimal consumoTotalRecetas = BigDecimal.ZERO;
        BigDecimal consumoTotalInsumos = BigDecimal.ZERO;
        
        // Agrupar por día y tipo
        Map<String, BigDecimal> consumoPorEtapa = new HashMap<>();
        Map<String, BigDecimal> consumoPorReceta = new HashMap<>();
        
        for (ConsumoDiarioAutomatico consumo : consumosDiarios) {
            // Poblar nombres transitorios
            if (consumo.getRecria() != null && consumo.getRecria().getLoteId() != null) {
                String nom = consumo.getRecria().getLoteNombre();
                if (nom == null && loteParaPorcinosQuery != null) {
                    nom = loteParaPorcinosQuery.obtenerPorId(consumo.getRecria().getLoteId()).map(dto -> dto.nombre()).orElse(null);
                }
                consumo.setRecriaNombre(nom);
            } else if (consumo.getMadre() != null) {
                consumo.setMadreNombre(consumo.getMadre().getIdentificacion());
            }
            if (consumo.getReceta() != null) {
                consumo.setRecetaNombre(consumo.getReceta().getNombre());
            }
            
            // Agregar al detalle
            Map<String, Object> row = new HashMap<>();
            row.put("Fecha", consumo.getDiaAlimentacion().getFecha());
            row.put("Etapa", consumo.getEtapaAlimentacion());
            row.put("Recría/Lote", consumo.getRecriaNombre() != null ? consumo.getRecriaNombre() : 
                                  (consumo.getMadreNombre() != null ? consumo.getMadreNombre() : "N/A"));
            row.put("Receta", consumo.getRecetaNombre() != null ? consumo.getRecetaNombre() : "N/A");
            row.put("Cantidad Receta (kg)", consumo.getCantidadRecetaEfectiva());
            row.put("Cantidad Animales", consumo.getCantidadAnimales());
            row.put("Cantidad Diaria por Animal (kg)", consumo.getCantidadDiariaPorAnimal());
            row.put("Estado", consumo.getDiaAlimentacion().getEstado().toString());
            consumosData.add(row);
            
            // Acumular totales
            consumoTotalRecetas = consumoTotalRecetas.add(consumo.getCantidadRecetaEfectiva());
            
            // Agrupar por etapa
            consumoPorEtapa.merge(consumo.getEtapaAlimentacion(), consumo.getCantidadRecetaEfectiva(), BigDecimal::add);
            
            // Agrupar por receta
            String recetaNombre = consumo.getRecetaNombre() != null ? consumo.getRecetaNombre() : "Sin receta";
            consumoPorReceta.merge(recetaNombre, consumo.getCantidadRecetaEfectiva(), BigDecimal::add);
            
            // Calcular consumo total de insumos desde los detalles
            List<ConsumoDiarioDetalle> detalles = consumoDiarioDetalleRepository
                .findByConsumoDiarioOrderByTipoComponenteAsc(consumo);
            
            for (ConsumoDiarioDetalle detalle : detalles) {
                consumoTotalInsumos = consumoTotalInsumos.add(detalle.getCantidadRequerida());
            }
        }
        
        // Resumen
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("Consumo Total Recetas (kg)", consumoTotalRecetas.setScale(2, RoundingMode.HALF_UP));
        resumen.put("Consumo Total Insumos (kg)", consumoTotalInsumos.setScale(2, RoundingMode.HALF_UP));
        resumen.put("Consumo por Etapa", consumoPorEtapa.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().setScale(2, RoundingMode.HALF_UP))));
        resumen.put("Consumo por Receta", consumoPorReceta.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().setScale(2, RoundingMode.HALF_UP))));
        resumen.put("Total Registros", consumosDiarios.size());
        resumen.put("Días con Consumo", consumosDiarios.stream()
            .map(c -> c.getDiaAlimentacion().getFecha())
            .collect(Collectors.toSet()).size());
        
        reporte.put("consumos", consumosData);
        reporte.put("resumen", resumen);
        
        return reporte;
    }

    // ============================================================================
    // REPORTE ECONÓMICO
    // ============================================================================

    @Transactional(readOnly = true)
    public Map<String, Object> generarReporteEconomico(Long userId, LocalDate fechaInicio, LocalDate fechaFin) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> reporte = new HashMap<>();
        
        // Ventas
        List<VentaPorcino> ventas = ventaPorcinoRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
            .filter(v -> !v.getFecha().isBefore(fechaInicio) && !v.getFecha().isAfter(fechaFin))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> ventasData = ventas.stream().map(v -> {
            Map<String, Object> row = new HashMap<>();
            row.put("Fecha", v.getFecha());
            row.put("Tipo", v.getTipo().toString());
            row.put("Cantidad", v.getCantidad());
            row.put("Peso Promedio (kg)", v.getPesoPromedio());
            row.put("Precio por kg", v.getPrecioKg());
            row.put("Ingreso Total", v.getIngresoTotal());
            row.put("Cliente", v.getCliente() != null ? v.getCliente() : "N/A");
            return row;
        }).collect(Collectors.toList());
        
        BigDecimal ingresosTotales = ventas.stream()
            .map(VentaPorcino::getIngresoTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Obtener costos económicos
        Optional<DatosEconomicosPorcino> datosEconomicosOpt = datosEconomicosRepository.findByEmpresa(empresa.get());
        
        // Calcular costos de alimentación usando el nuevo sistema de consumo diario automático
        BigDecimal costoAlimentacion = calcularCostoAlimentacionNuevoSistema(empresa.get(), fechaInicio, fechaFin, datosEconomicosOpt);
        BigDecimal costoManoObra = BigDecimal.ZERO;
        if (datosEconomicosOpt.isPresent()) {
            // Calcular costos basados en parámetros económicos
            costoManoObra = datosEconomicosOpt.get().getCostoManoObraDia() != null ?
                datosEconomicosOpt.get().getCostoManoObraDia() : BigDecimal.ZERO;
        }
        
        BigDecimal costosTotales = costoAlimentacion.add(costoManoObra);
        BigDecimal balanceNeto = ingresosTotales.subtract(costosTotales);
        BigDecimal margenBeneficio = ingresosTotales.compareTo(BigDecimal.ZERO) > 0 ?
            balanceNeto.divide(ingresosTotales, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
            BigDecimal.ZERO;
        
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("Ingresos Totales", ingresosTotales.setScale(2, RoundingMode.HALF_UP));
        resumen.put("Costos Totales", costosTotales.setScale(2, RoundingMode.HALF_UP));
        resumen.put("Balance Neto", balanceNeto.setScale(2, RoundingMode.HALF_UP));
        resumen.put("Margen de Beneficio (%)", margenBeneficio.setScale(2, RoundingMode.HALF_UP));
        resumen.put("Total Ventas", ventas.size());
        
        reporte.put("ventas", ventasData);
        reporte.put("resumen", resumen);
        
        return reporte;
    }

    // ============================================================================
    // REPORTE DE INVENTARIO
    // ============================================================================

    @Transactional(readOnly = true)
    public Map<String, Object> generarReporteInventario(Long userId, LocalDate fechaInicio, LocalDate fechaFin) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> reporte = new HashMap<>();
        
        // Madres por estado (no se filtran por fecha, son valores actuales)
        List<Madre> madres = madreRepository.findByEmpresaAndActivoTrue(empresa.get());
        Map<String, Long> madresPorEstado = madres.stream()
            .filter(m -> m.getEstadoActual() != Madre.EstadoMadre.DESCARTE)
            .collect(Collectors.groupingBy(
                m -> m.getEstadoActual().toString(),
                Collectors.counting()
            ));
        
        List<Map<String, Object>> madresData = madres.stream()
            .filter(m -> m.getEstadoActual() != Madre.EstadoMadre.DESCARTE)
            .map(m -> {
                Map<String, Object> row = new HashMap<>();
                row.put("Identificación", m.getIdentificacion());
                row.put("Estado", m.getEstadoActual().toString());
                row.put("Raza", m.getRaza() != null ? m.getRaza().getNombre() : "N/A");
                row.put("Número Partos", m.getNumeroPartos());
                row.put("Ubicación", m.getUbicacionInterna() != null ? m.getUbicacionInterna().getNombre() : "N/A");
                return row;
            }).collect(Collectors.toList());
        
        // Padrillos (no se filtran por fecha, son valores actuales)
        List<Padrillo> padrillos = padrilloRepository.findByEmpresaAndActivoTrue(empresa.get());
        List<Map<String, Object>> padrillosData = padrillos.stream().map(p -> {
            Map<String, Object> row = new HashMap<>();
            row.put("Identificación", p.getIdentificacion());
            row.put("Raza", p.getRaza() != null ? p.getRaza().getNombre() : "N/A");
            row.put("Ubicación", p.getUbicacionInterna() != null ? p.getUbicacionInterna().getNombre() : "N/A");
            return row;
        }).collect(Collectors.toList());
        
        // Recrías activas (filtrar por fecha de ingreso si están presentes)
        List<Recria> recrias = recriaRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
            .filter(r -> {
                if (fechaInicio != null && r.getFechaIngreso().isBefore(fechaInicio)) {
                    return false;
                }
                if (fechaFin != null && r.getFechaIngreso().isAfter(fechaFin)) {
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());
        
        List<Map<String, Object>> recriasData = recrias.stream().map(r -> {
            Map<String, Object> row = new HashMap<>();
            row.put("Lote", nombreLoteRecria(r));
            row.put("Fecha Ingreso", r.getFechaIngreso());
            row.put("Cantidad Animales", r.getCantidadAnimales());
            row.put("Peso Promedio (kg)", r.getPesoPromedio());
            row.put("Sexo", r.getSexo().toString());
            row.put("Etapa", r.getEtapa() != null ? r.getEtapa().toString() : "N/A");
            row.put("Destino", r.getDestino() != null ? r.getDestino().toString() : "N/A");
            return row;
        }).collect(Collectors.toList());
        
        int totalAnimalesRecria = recrias.stream().mapToInt(Recria::getCantidadAnimales).sum();
        
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("Total Madres", madres.stream().filter(m -> m.getEstadoActual() != Madre.EstadoMadre.DESCARTE).count());
        resumen.put("Madres por Estado", madresPorEstado);
        resumen.put("Total Padrillos", padrillos.size());
        resumen.put("Total Animales en Recría", totalAnimalesRecria);
        resumen.put("Total Recrías Activas", recrias.size());
        
        reporte.put("madres", madresData);
        reporte.put("padrillos", padrillosData);
        reporte.put("recrias", recriasData);
        reporte.put("resumen", resumen);
        reporte.put("fechaCorte", LocalDate.now());
        
        return reporte;
    }

    // ============================================================================
    // REPORTE SANITARIO
    // ============================================================================

    @Transactional(readOnly = true)
    public Map<String, Object> generarReporteSanitario(Long userId, LocalDate fechaInicio, LocalDate fechaFin) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> reporte = new HashMap<>();
        
        List<EventoSanitario> eventos = eventoSanitarioRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
            .filter(e -> !e.getFecha().isBefore(fechaInicio) && !e.getFecha().isAfter(fechaFin))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> eventosData = eventos.stream().map(e -> {
            Map<String, Object> row = new HashMap<>();
            row.put("Fecha", e.getFecha());
            row.put("Tipo Evento", e.getTipoEventoSanitario() != null ? e.getTipoEventoSanitario().getNombre() : "N/A");
            row.put("Categoría", e.getTipoEventoSanitario() != null ? e.getTipoEventoSanitario().getCategoria() : "N/A");
            row.put("Tipo Entidad", e.getTipoEntidad().toString());
            row.put("Entidad ID", e.getEntidadId());
            row.put("Dosis", e.getDosis());
            row.put("Unidad", e.getUnidadDosis());
            row.put("Lote Medicamento", e.getLoteMedicamento() != null ? e.getLoteMedicamento() : "N/A");
            row.put("Profesional", e.getProfesionalResponsable() != null ? e.getProfesionalResponsable() : "N/A");
            row.put("Fecha Retiro", e.getFechaRetiro() != null ? e.getFechaRetiro() : "N/A");
            row.put("Retiro Cumplido", e.getRetiroCumplido() ? "Sí" : "No");
            return row;
        }).collect(Collectors.toList());
        
        // Resumen por tipo
        Map<String, Long> eventosPorTipo = eventos.stream()
            .collect(Collectors.groupingBy(
                e -> e.getTipoEventoSanitario() != null ? e.getTipoEventoSanitario().getNombre() : "N/A",
                Collectors.counting()
            ));
        
        // Retiros pendientes
        List<EventoSanitario> retirosPendientes = eventos.stream()
            .filter(e -> e.getFechaRetiro() != null && !e.getRetiroCumplido() && e.getFechaRetiro().isAfter(LocalDate.now()))
            .collect(Collectors.toList());
        
        // Retiros vencidos
        List<EventoSanitario> retirosVencidos = eventos.stream()
            .filter(e -> e.getFechaRetiro() != null && !e.getRetiroCumplido() && e.getFechaRetiro().isBefore(LocalDate.now()))
            .collect(Collectors.toList());
        
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("Total Eventos", eventos.size());
        resumen.put("Eventos por Tipo", eventosPorTipo);
        resumen.put("Retiros Pendientes", retirosPendientes.size());
        resumen.put("Retiros Vencidos", retirosVencidos.size());
        
        reporte.put("eventos", eventosData);
        reporte.put("retirosPendientes", retirosPendientes.size());
        reporte.put("retirosVencidos", retirosVencidos.size());
        reporte.put("resumen", resumen);
        
        return reporte;
    }

    // ============================================================================
    // REPORTE DE VENTAS
    // ============================================================================

    @Transactional(readOnly = true)
    public Map<String, Object> generarReporteVentas(Long userId, LocalDate fechaInicio, LocalDate fechaFin) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> reporte = new HashMap<>();
        
        List<VentaPorcino> ventas = ventaPorcinoRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
            .filter(v -> !v.getFecha().isBefore(fechaInicio) && !v.getFecha().isAfter(fechaFin))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> ventasData = ventas.stream().map(v -> {
            Map<String, Object> row = new HashMap<>();
            row.put("Fecha", v.getFecha());
            row.put("Tipo", v.getTipo().toString());
            row.put("Cantidad", v.getCantidad());
            row.put("Peso Promedio (kg)", v.getPesoPromedio());
            row.put("Precio por kg", v.getPrecioKg());
            row.put("Ingreso Total", v.getIngresoTotal());
            row.put("Cliente", v.getCliente() != null ? v.getCliente() : "N/A");
            row.put("Lote", nombreLoteVenta(v));
            return row;
        }).collect(Collectors.toList());
        
        BigDecimal ingresosTotales = ventas.stream()
            .map(VentaPorcino::getIngresoTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal precioPromedio = ventas.isEmpty() ? BigDecimal.ZERO :
            ventas.stream()
                .map(VentaPorcino::getPrecioKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(ventas.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal pesoPromedio = ventas.isEmpty() ? BigDecimal.ZERO :
            ventas.stream()
                .map(VentaPorcino::getPesoPromedio)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(ventas.size()), 2, RoundingMode.HALF_UP);
        
        int totalCantidad = ventas.stream().mapToInt(VentaPorcino::getCantidad).sum();
        
        // Agrupar por cliente
        Map<String, BigDecimal> ingresosPorCliente = ventas.stream()
            .filter(v -> v.getCliente() != null)
            .collect(Collectors.groupingBy(
                VentaPorcino::getCliente,
                Collectors.reducing(BigDecimal.ZERO, VentaPorcino::getIngresoTotal, BigDecimal::add)
            ));
        
        // Agrupar por tipo
        Map<String, BigDecimal> ingresosPorTipo = ventas.stream()
            .collect(Collectors.groupingBy(
                v -> v.getTipo().toString(),
                Collectors.reducing(BigDecimal.ZERO, VentaPorcino::getIngresoTotal, BigDecimal::add)
            ));
        
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("Total Ventas", ventas.size());
        resumen.put("Total Cantidad Vendida", totalCantidad);
        resumen.put("Ingresos Totales", ingresosTotales.setScale(2, RoundingMode.HALF_UP));
        resumen.put("Precio Promedio por kg", precioPromedio.setScale(2, RoundingMode.HALF_UP));
        resumen.put("Peso Promedio Vendido (kg)", pesoPromedio.setScale(2, RoundingMode.HALF_UP));
        resumen.put("Ingresos por Cliente", ingresosPorCliente);
        resumen.put("Ingresos por Tipo", ingresosPorTipo);
        
        reporte.put("ventas", ventasData);
        reporte.put("resumen", resumen);
        
        return reporte;
    }

    // ============================================================================
    // VALIDACIÓN DEL CICLO PRODUCTIVO
    // ============================================================================

    /**
     * Validar que el ciclo productivo se cumpla correctamente según los parámetros
     * Verifica fechas, estados y consistencia de datos
     */
    @Transactional(readOnly = true)
    public Map<String, Object> validarCicloProductivo(Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            return Map.of("error", "Usuario no tiene empresa activa");
        }

        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, Object>> validaciones = new ArrayList<>();
        int totalValidaciones = 0;
        int validacionesExitosas = 0;
        int validacionesFallidas = 0;

        // Obtener parámetros productivos
        Optional<ParametrosProductivosPorcino> parametrosOpt = 
            parametrosProductivosRepository.findByEmpresa(empresa.get());
        
        if (parametrosOpt.isEmpty()) {
            return Map.of("error", "No hay parámetros productivos configurados");
        }

        ParametrosProductivosPorcino parametros = parametrosOpt.get();
        int diasGestacion = parametros.getDiasPromedioGestacion() != null 
            ? parametros.getDiasPromedioGestacion() 
            : 115;
        int diasLactancia = parametros.getDiasLactancia() != null 
            ? parametros.getDiasLactancia() 
            : 21;
        int toleranciaGestacion = parametros.getDiasToleranciaVencimientoGestacion() != null 
            ? parametros.getDiasToleranciaVencimientoGestacion() 
            : 5;

        // 1. Validar Servicios → Gestaciones
        List<Servicio> servicios = servicioRepository.findByEmpresaAndActivoTrue(empresa.get());
        for (Servicio servicio : servicios) {
            totalValidaciones++;
            Map<String, Object> validacion = new HashMap<>();
            validacion.put("tipo", "Servicio → Gestación");
            validacion.put("madre", servicio.getMadre().getIdentificacion());
            validacion.put("fechaServicio", servicio.getFechaServicio());

            // Buscar gestación relacionada con este servicio
            Optional<Gestacion> gestacionOpt = gestacionRepository.findByEmpresaAndActivoTrue(empresa.get()).stream()
                .filter(g -> g.getServicio() != null && g.getServicio().getId().equals(servicio.getId()))
                .findFirst();
            
            if (gestacionOpt.isPresent()) {
                Gestacion gestacion = gestacionOpt.get();
                long diasCalculados = java.time.temporal.ChronoUnit.DAYS
                    .between(servicio.getFechaServicio(), gestacion.getFechaProbableParto());
                
                if (Math.abs(diasCalculados - diasGestacion) <= toleranciaGestacion) {
                    validacion.put("estado", "OK");
                    validacion.put("mensaje", String.format("Gestación correcta: %d días (esperado: %d)", 
                        diasCalculados, diasGestacion));
                    validacionesExitosas++;
                } else {
                    validacion.put("estado", "ERROR");
                    validacion.put("mensaje", String.format("Días de gestación incorrectos: %d (esperado: %d ± %d)", 
                        diasCalculados, diasGestacion, toleranciaGestacion));
                    validacionesFallidas++;
                }
            } else if (servicio.getEstadoServicio() == Servicio.EstadoServicio.PREÑEZ_CONFIRMADA) {
                validacion.put("estado", "ADVERTENCIA");
                validacion.put("mensaje", "Servicio con preñez confirmada pero sin gestación registrada");
                validacionesFallidas++;
            } else {
                validacion.put("estado", "INFO");
                validacion.put("mensaje", "Servicio sin gestación (normal si no hay preñez confirmada)");
                validacionesExitosas++;
            }
            validaciones.add(validacion);
        }

        // 2. Validar Gestaciones → Partos
        List<Gestacion> gestaciones = gestacionRepository.findByEmpresaAndActivoTrue(empresa.get());
        for (Gestacion gestacion : gestaciones) {
            if (gestacion.getEstado() == Gestacion.EstadoGestacion.FINALIZADA) {
                totalValidaciones++;
                Map<String, Object> validacion = new HashMap<>();
                validacion.put("tipo", "Gestación → Parto");
                validacion.put("madre", gestacion.getMadre().getIdentificacion());
                validacion.put("fechaProbableParto", gestacion.getFechaProbableParto());

                List<Parto> partos = partoRepository.findByMadreAndActivoTrue(gestacion.getMadre());
                Optional<Parto> partoRelacionado = partos.stream()
                    .filter(p -> {
                        LocalDate fechaParto = p.getFechaInicio().toLocalDate();
                        return !fechaParto.isBefore(gestacion.getFechaProbableParto().minusDays(toleranciaGestacion)) &&
                               !fechaParto.isAfter(gestacion.getFechaProbableParto().plusDays(toleranciaGestacion));
                    })
                    .findFirst();

                if (partoRelacionado.isPresent()) {
                    Parto parto = partoRelacionado.get();
                    long diasDiferencia = Math.abs(java.time.temporal.ChronoUnit.DAYS
                        .between(gestacion.getFechaProbableParto(), parto.getFechaInicio().toLocalDate()));
                    
                    validacion.put("estado", "OK");
                    validacion.put("mensaje", String.format("Parto registrado con %d días de diferencia", diasDiferencia));
                    validacion.put("fechaParto", parto.getFechaInicio().toLocalDate());
                    validacionesExitosas++;
                } else {
                    validacion.put("estado", "ADVERTENCIA");
                    validacion.put("mensaje", "Gestación finalizada sin parto registrado en rango esperado");
                    validacionesFallidas++;
                }
                validaciones.add(validacion);
            }
        }

        // 3. Validar Partos → Destetes
        List<Parto> partos = partoRepository.findByEmpresaAndActivoTrue(empresa.get());
        for (Parto parto : partos) {
            if (parto.getFechaFin() != null) { // Parto cerrado
                totalValidaciones++;
                Map<String, Object> validacion = new HashMap<>();
                validacion.put("tipo", "Parto → Destete");
                validacion.put("madre", parto.getMadre().getIdentificacion());
                validacion.put("fechaParto", parto.getFechaInicio().toLocalDate());

                Optional<Destete> desteteOpt = desteteRepository.findByPartoAndActivoTrue(parto);
                if (desteteOpt.isPresent()) {
                    Destete destete = desteteOpt.get();
                    long diasLactanciaCalculados = java.time.temporal.ChronoUnit.DAYS
                        .between(parto.getFechaInicio().toLocalDate(), destete.getFechaDestete());
                    
                    if (Math.abs(diasLactanciaCalculados - diasLactancia) <= 3) { // Tolerancia de 3 días
                        validacion.put("estado", "OK");
                        validacion.put("mensaje", String.format("Destete correcto: %d días de lactancia (esperado: %d)", 
                            diasLactanciaCalculados, diasLactancia));
                        validacion.put("fechaDestete", destete.getFechaDestete());
                        validacionesExitosas++;
                    } else {
                        validacion.put("estado", "ADVERTENCIA");
                        validacion.put("mensaje", String.format("Días de lactancia fuera de rango: %d (esperado: %d)", 
                            diasLactanciaCalculados, diasLactancia));
                        validacionesFallidas++;
                    }
                } else {
                    validacion.put("estado", "ADVERTENCIA");
                    validacion.put("mensaje", "Parto cerrado sin destete registrado");
                    validacionesFallidas++;
                }
                validaciones.add(validacion);
            }
        }

        // 4. Validar Estados de Madres
        List<Madre> madres = madreRepository.findByEmpresaAndActivoTrue(empresa.get());
        for (Madre madre : madres) {
            totalValidaciones++;
            Map<String, Object> validacion = new HashMap<>();
            validacion.put("tipo", "Estado de Madre");
            validacion.put("madre", madre.getIdentificacion());
            validacion.put("estadoActual", madre.getEstadoActual().toString());

            boolean estadoConsistente = true;
            String mensaje = "";

            // Verificar si tiene gestación activa
            Optional<Gestacion> gestacionActiva = gestacionRepository.findActivaByMadre(madre);
            if (gestacionActiva.isPresent() && madre.getEstadoActual() != Madre.EstadoMadre.GESTACION) {
                estadoConsistente = false;
                mensaje = "Madre con gestación activa pero estado no es GESTACION";
            }

            // Verificar si tiene parto abierto
            List<Parto> partosAbiertos = partoRepository.findByMadreAndActivoTrue(madre).stream()
                .filter(p -> p.getFechaFin() == null)
                .collect(Collectors.toList());
            
            if (!partosAbiertos.isEmpty() && madre.getEstadoActual() != Madre.EstadoMadre.LACTANCIA) {
                estadoConsistente = false;
                mensaje = "Madre con parto abierto pero estado no es LACTANCIA";
            }

            if (estadoConsistente) {
                validacion.put("estado", "OK");
                validacion.put("mensaje", "Estado consistente con eventos registrados");
                validacionesExitosas++;
            } else {
                validacion.put("estado", "ERROR");
                validacion.put("mensaje", mensaje);
                validacionesFallidas++;
            }
            validaciones.add(validacion);
        }

        // Resumen
        resultado.put("validaciones", validaciones);
        resultado.put("resumen", Map.of(
            "total", totalValidaciones,
            "exitosas", validacionesExitosas,
            "fallidas", validacionesFallidas,
            "porcentajeExito", totalValidaciones > 0 
                ? BigDecimal.valueOf(validacionesExitosas)
                    .divide(BigDecimal.valueOf(totalValidaciones), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO
        ));
        resultado.put("parametros", Map.of(
            "diasGestacion", diasGestacion,
            "diasLactancia", diasLactancia,
            "toleranciaGestacion", toleranciaGestacion
        ));

        return resultado;
    }

    // ============================================================================
    // MÉTODOS AUXILIARES PARA COSTOS Y ALIMENTACIÓN
    // ============================================================================

    /**
     * Calcular costo de alimentación usando el nuevo sistema de consumo diario automático
     */
    private BigDecimal calcularCostoAlimentacionNuevoSistema(Empresa empresa, LocalDate fechaDesde, LocalDate fechaHasta, 
                                                             Optional<DatosEconomicosPorcino> datosEconomicosOpt) {
        BigDecimal costoTotal = BigDecimal.ZERO;
        
        // Obtener consumos diarios automáticos del período
        List<ConsumoDiarioAutomatico> consumos = consumoDiarioAutomaticoRepository
            .findByEmpresaAndFechaBetween(empresa, fechaDesde, fechaHasta);
        
        for (ConsumoDiarioAutomatico consumo : consumos) {
            // Calcular costo de la receta
            InsumoCompuesto receta = consumo.getReceta();
            BigDecimal cantidadReceta = consumo.getCantidadRecetaEfectiva();
            
            // Obtener costo unitario de la receta (manual si existe, sino calculado)
            BigDecimal costoUnitarioReceta = BigDecimal.ZERO;
            if (receta != null) {
                costoUnitarioReceta = receta.getCostoUnitario() != null ? receta.getCostoUnitario() : BigDecimal.ZERO;
                if (costoUnitarioReceta.compareTo(BigDecimal.ZERO) == 0) {
                    // Si no tiene costo, calcular desde componentes
                    costoUnitarioReceta = calcularCostoRecetaDesdeComponentes(receta, datosEconomicosOpt);
                }
            }
            
            // Costo de la receta consumida
            BigDecimal costoReceta = cantidadReceta.multiply(costoUnitarioReceta);
            costoTotal = costoTotal.add(costoReceta);
        }
        
        return costoTotal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcular costo de una receta desde sus componentes
     */
    private BigDecimal calcularCostoRecetaDesdeComponentes(InsumoCompuesto receta, Optional<DatosEconomicosPorcino> datosEconomicosOpt) {
        BigDecimal costoTotal100kg = BigDecimal.ZERO;
        
        List<ComponenteInsumoCompuesto> componentes = componenteRepository.findByInsumoCompuesto(receta);
        
        for (ComponenteInsumoCompuesto componente : componentes) {
            BigDecimal cantidadNecesaria = componente.calcularCantidadNecesaria(BigDecimal.valueOf(100));
            BigDecimal precioUnitario = BigDecimal.ZERO;
            
            if (componente.getTipoComponente() == ComponenteInsumoCompuesto.TipoComponente.INSUMO && 
                componente.getInsumo() != null) {
                precioUnitario = componente.getInsumo().getPrecioUnitario() != null ? 
                    componente.getInsumo().getPrecioUnitario() : BigDecimal.ZERO;
                    
            } else if (componente.getTipoComponente() == ComponenteInsumoCompuesto.TipoComponente.GRANO_PROPIO && 
                       componente.getCultivo() != null) {
                // Calcular precio del cultivo (grano propio)
                Empresa empresa = receta.getEmpresa();
                precioUnitario = calcularPrecioCultivo(componente.getCultivo(), datosEconomicosOpt.orElse(null), empresa);
                
            } else if (componente.getTipoComponente() == ComponenteInsumoCompuesto.TipoComponente.INSUMO_COMPUESTO && 
                       componente.getInsumoCompuestoPadre() != null) {
                // Sub-receta: obtener su costo unitario (recursivo)
                InsumoCompuesto subReceta = componente.getInsumoCompuestoPadre();
                precioUnitario = subReceta.getCostoUnitario() != null ? 
                    subReceta.getCostoUnitario() : calcularCostoRecetaDesdeComponentes(subReceta, datosEconomicosOpt);
            }
            
            if (precioUnitario.compareTo(BigDecimal.ZERO) > 0 && cantidadNecesaria.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal costoComponente = cantidadNecesaria.multiply(precioUnitario);
                costoTotal100kg = costoTotal100kg.add(costoComponente);
            }
        }
        
        // Dividir por rendimiento y por 100 para obtener costo por kg
        if (receta.getRendimiento() != null && receta.getRendimiento().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal costoUnitario = costoTotal100kg
                .divide(receta.getRendimiento(), 4, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            return costoUnitario;
        }
        
        return costoTotal100kg.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }

    /**
     * Calcular precio de un cultivo (grano propio) según método de imputación
     */
    private BigDecimal calcularPrecioCultivo(Cultivo cultivo, DatosEconomicosPorcino datosEconomicos, Empresa empresa) {
        if (cultivo == null) {
            return BigDecimal.ZERO;
        }

        if (datosEconomicos == null || datosEconomicos.getMetodoImputacionCostoCultivo() == null) {
            return BigDecimal.valueOf(0.3); // Valor por defecto
        }

        switch (datosEconomicos.getMetodoImputacionCostoCultivo()) {
            case PRECIO_MANUAL:
                if (datosEconomicos.getPrecioManualCultivoKg() != null) {
                    return datosEconomicos.getPrecioManualCultivoKg();
                }
                return BigDecimal.valueOf(0.3);
            
            case PRECIO_MERCADO:
                // Por ahora retornamos un valor fijo, en producción se obtendría de una API o tabla de precios
                return BigDecimal.valueOf(0.35);
            
            case PROMEDIO_PONDERADO:
            default:
                // Calcular promedio ponderado del costo de producción del cultivo usando InventarioGrano
                // Obtener inventarios disponibles del cultivo y calcular costo promedio
                List<InventarioGrano> inventarios = inventarioGranoRepository
                    .findByCultivoIdOrderByFechaIngresoDesc(cultivo.getId());
                
                List<InventarioGrano> inventariosDisponibles = inventarios.stream()
                    .filter(inv -> "DISPONIBLE".equals(inv.getEstado()))
                    .filter(inv -> inv.getCantidadDisponible().compareTo(BigDecimal.ZERO) > 0)
                    .collect(Collectors.toList());
                
                if (inventariosDisponibles.isEmpty()) {
                    return BigDecimal.valueOf(0.3); // Valor por defecto si no hay inventario
                }
                
                // Calcular costo promedio ponderado
                BigDecimal costoTotal = BigDecimal.ZERO;
                BigDecimal cantidadTotal = BigDecimal.ZERO;
                
                for (InventarioGrano inventario : inventariosDisponibles) {
                    BigDecimal cantidad = inventario.getCantidadDisponible();
                    BigDecimal costoUnitario = inventario.getCostoUnitario() != null ? 
                        inventario.getCostoUnitario() : BigDecimal.ZERO;
                    
                    if (costoUnitario.compareTo(BigDecimal.ZERO) > 0) {
                        costoTotal = costoTotal.add(cantidad.multiply(costoUnitario));
                        cantidadTotal = cantidadTotal.add(cantidad);
                    }
                }
                
                if (cantidadTotal.compareTo(BigDecimal.ZERO) > 0) {
                    return costoTotal.divide(cantidadTotal, 2, RoundingMode.HALF_UP);
                }
                
                return BigDecimal.valueOf(0.3); // Valor por defecto
        }
    }

    private String nombreLoteRecria(Recria r) {
        if (r == null || r.getLoteId() == null) return "N/A";
        if (r.getLoteNombre() != null) return r.getLoteNombre();
        if (loteParaPorcinosQuery != null) {
            return loteParaPorcinosQuery.obtenerPorId(r.getLoteId()).map(dto -> dto.nombre()).orElse("N/A");
        }
        return "Lote " + r.getLoteId();
    }

    private String nombreLoteVenta(VentaPorcino v) {
        if (v == null || v.getLoteId() == null) return "N/A";
        if (loteParaPorcinosQuery != null) {
            return loteParaPorcinosQuery.obtenerPorId(v.getLoteId()).map(dto -> dto.nombre()).orElse("N/A");
        }
        return "Lote " + v.getLoteId();
    }
}

