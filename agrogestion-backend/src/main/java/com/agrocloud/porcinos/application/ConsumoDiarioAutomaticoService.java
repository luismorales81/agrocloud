package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.application.port.LoteParaPorcinosQuery;
import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.cultivos.domain.Cultivo;
import com.agrocloud.cultivos.infrastructure.CultivoRepository;
import com.agrocloud.core.inventory.domain.ComponenteInsumoCompuesto;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import com.agrocloud.core.inventory.domain.InventarioGrano;
import com.agrocloud.porcinos.domain.ConsumoDiarioAutomatico;
import com.agrocloud.porcinos.domain.ConsumoDiarioDetalle;
import com.agrocloud.porcinos.domain.DiaAlimentacion;
import com.agrocloud.porcinos.domain.Destete;
import com.agrocloud.porcinos.domain.Gestacion;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.Parto;
import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.domain.RecetaAlimentacionPorEtapa;
import com.agrocloud.porcinos.domain.ParametrosProductivosPorcino;
import com.agrocloud.porcinos.domain.MovimientoStockPorcino;
import com.agrocloud.porcinos.domain.RegistroPeso;
import com.agrocloud.porcinos.infrastructure.ConsumoDiarioAutomaticoRepository;
import com.agrocloud.porcinos.infrastructure.ConsumoDiarioDetalleRepository;
import com.agrocloud.porcinos.infrastructure.DiaAlimentacionRepository;
import com.agrocloud.porcinos.infrastructure.DesteteRepository;
import com.agrocloud.porcinos.infrastructure.GestacionRepository;
import com.agrocloud.porcinos.infrastructure.MadreRepository;
import com.agrocloud.porcinos.infrastructure.MovimientoStockPorcinoRepository;
import com.agrocloud.porcinos.infrastructure.PartoRepository;
import com.agrocloud.porcinos.infrastructure.RecriaRepository;
import com.agrocloud.porcinos.infrastructure.RecetaAlimentacionPorEtapaRepository;
import com.agrocloud.porcinos.infrastructure.ParametrosProductivosPorcinoRepository;
import com.agrocloud.porcinos.infrastructure.RegistroPesoRepository;
import com.agrocloud.core.inventory.infrastructure.ComponenteInsumoCompuestoRepository;
import com.agrocloud.core.inventory.infrastructure.InsumoCompuestoRepository;
import com.agrocloud.core.inventory.infrastructure.InventarioGranoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generar consumos diarios automáticos de alimento
 * 
 * REGLAS:
 * - Se ejecuta automáticamente todos los días a las 00:30 AM
 * - Genera el consumo del día anterior
 * - Permite stock negativo solo para consumo automático
 * - No recalculan días ya confirmados
 * - Registra trazabilidad completa de movimientos
 * 
 * INVENTARIO: Insumo, InsumoCompuesto y GRANO_PROPIO se descuentan vía core.inventory (InventoryService).
 */
@Service
@EnableScheduling
public class ConsumoDiarioAutomaticoService {

    private static final Logger logger = LoggerFactory.getLogger(ConsumoDiarioAutomaticoService.class);

    @Autowired
    private DiaAlimentacionRepository diaAlimentacionRepository;

    @Autowired
    private ConsumoDiarioAutomaticoRepository consumoDiarioRepository;

    @Autowired
    private ConsumoDiarioDetalleRepository consumoDetalleRepository;

    @Autowired
    private MovimientoStockPorcinoRepository movimientoStockRepository;

    @Autowired
    private RecetaAlimentacionPorEtapaRepository recetaEtapaRepository;

    @Autowired
    private RecriaRepository recriaRepository;

    @Autowired
    private MadreRepository madreRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private LoteParaPorcinosQuery loteParaPorcinosQuery;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CultivoRepository cultivoRepository;

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
    private GestacionRepository gestacionRepository;

    @Autowired
    private PartoRepository partoRepository;

    @Autowired
    private ParametrosProductivosPorcinoRepository parametrosProductivosRepository;

    @Autowired
    private DesteteRepository desteteRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private RecriaStockService recriaStockService;

    @Autowired
    private RegistroPesoRepository registroPesoRepository;

    /**
     * Job automático que se ejecuta diariamente a las 00:30 AM
     * Genera el consumo del día anterior para todas las empresas activas
     */
    @Scheduled(cron = "0 30 0 * * ?") // Todos los días a las 00:30 AM
    public void generarConsumoDiarioAutomatico() {
        logger.info("Iniciando generación de consumo diario automático para fecha: {}", 
                   LocalDate.now().minusDays(1));
        
        LocalDate fechaAyer = LocalDate.now().minusDays(1);
        
        // Obtener todas las empresas activas
        List<Empresa> empresas = empresaRepository.findByActivoTrue();
        
        for (Empresa empresa : empresas) {
            try {
                logger.info("Procesando empresa: {} (ID: {})", empresa.getNombre(), empresa.getId());
                generarConsumoDiarioParaFecha(fechaAyer, empresa);
            } catch (Exception e) {
                logger.error("Error al generar consumo diario para empresa {}: {}", 
                           empresa.getId(), e.getMessage(), e);
                // Continuar con la siguiente empresa
            }
        }
        
        logger.info("Finalizada generación de consumo diario automático");
    }

    /**
     * Genera consumo diario automático para una fecha y empresa específica
     * Se puede llamar manualmente para reprocesar días anteriores (solo si no están confirmados)
     * 
     * @param fecha Fecha para la cual generar el consumo
     * @param empresa Empresa para la cual generar el consumo
     * @return DiaAlimentacion creado o existente
     */
    @Transactional
    public DiaAlimentacion generarConsumoDiarioParaFecha(LocalDate fecha, Empresa empresa) {
        logger.info("Generando consumo diario para fecha: {} - Empresa: {}", fecha, empresa.getNombre());
        
        // 1. Verificar si ya existe día para esta fecha
        Optional<DiaAlimentacion> diaExistente = diaAlimentacionRepository.findByEmpresaAndFecha(empresa, fecha);
        
        if (diaExistente.isPresent()) {
            DiaAlimentacion dia = diaExistente.get();
            
            // REGLA: No recalcular días ya confirmados
            if (dia.estaCerrado()) {
                logger.warn("Día {} ya está confirmado (estado: {}), no se recalcula. Empresa: {}", 
                          fecha, dia.getEstado(), empresa.getNombre());
                return dia;
            }
            
            // Si está pendiente, eliminar consumos anteriores para recalcular
            logger.info("Día {} existe pero está pendiente, recalculando...", fecha);
            List<ConsumoDiarioAutomatico> consumosAnteriores = 
                consumoDiarioRepository.findByDiaAlimentacion(dia);
            for (ConsumoDiarioAutomatico consumo : consumosAnteriores) {
                consumoDetalleRepository.deleteAll(consumo.getDetalles());
                consumoDiarioRepository.delete(consumo);
            }
            dia.setTotalLotesAtendidos(0);
            dia.setTotalAnimalesAtendidos(0);
            dia.setTotalRecetasUsadas(0);
            dia.setTotalInsumosConsumidos(0);
            dia.setTieneAlertasStockInsuficiente(false);
            dia.setCantidadAlertas(0);
        }
        
        // 2. Crear o obtener DiaAlimentacion
        DiaAlimentacion diaAlimentacion = diaExistente.orElseGet(() -> 
            new DiaAlimentacion(fecha, empresa));
        
        // 3. Calcular consumos para todas las recrías y madres activas
        List<ConsumoDiarioAutomatico> consumos = calcularConsumosParaFecha(fecha, empresa);
        
        if (consumos.isEmpty()) {
            logger.warn("No se encontraron recrías/madres activas para generar consumo. Fecha: {} - Empresa: {}", 
                       fecha, empresa.getNombre());
            return diaAlimentacionRepository.save(diaAlimentacion);
        }
        
        // 4. Procesar cada consumo: crear detalles sin descontar stock (el descuento es al confirmar el día)
        int totalAnimales = 0;
        int cantidadAlertas = 0;
        boolean tieneAlertas = false;
        
        Set<Long> lotesAtendidos = new HashSet<>();
        Set<Long> recetasUsadas = new HashSet<>();
        Set<Long> insumosConsumidos = new HashSet<>();
        
        for (ConsumoDiarioAutomatico consumo : consumos) {
            consumo.setDiaAlimentacion(diaAlimentacion);
            ConsumoDiarioAutomatico consumoGuardado = consumoDiarioRepository.save(consumo);
            
            // Procesar componentes de la receta
            procesarComponentesReceta(consumoGuardado, empresa);
            
            // Actualizar contadores
            if (consumo.getRecria() != null) {
                lotesAtendidos.add(consumo.getRecria().getLoteId());
                totalAnimales += consumo.getCantidadAnimales();
            }
            if (consumo.getMadre() != null) {
                totalAnimales += 1; // Cada madre cuenta como 1
            }
            recetasUsadas.add(consumo.getReceta().getId());
            
            // Contar alertas
            Long alertasEnConsumo = consumoDetalleRepository.countByConsumoDiarioAndTieneDeficitTrue(consumoGuardado);
            cantidadAlertas += alertasEnConsumo.intValue();
            if (alertasEnConsumo > 0) {
                tieneAlertas = true;
            }
            
            // Contar insumos únicos consumidos
            List<ConsumoDiarioDetalle> detalles = consumoDetalleRepository
                .findByConsumoDiarioOrderByTipoComponenteAsc(consumoGuardado);
            for (ConsumoDiarioDetalle detalle : detalles) {
                if (detalle.getInsumo() != null) {
                    insumosConsumidos.add(detalle.getInsumo().getId());
                } else if (detalle.getCultivoId() != null) {
                    insumosConsumidos.add(detalle.getCultivoId());
                } else if (detalle.getInsumoCompuesto() != null) {
                    insumosConsumidos.add(detalle.getInsumoCompuesto().getId());
                }
            }
            
            consumoGuardado.marcarComoProcesado();
            consumoDiarioRepository.save(consumoGuardado);
        }
        
        // 5. Actualizar resumen del día
        diaAlimentacion.setTotalLotesAtendidos(lotesAtendidos.size());
        diaAlimentacion.setTotalAnimalesAtendidos(totalAnimales);
        diaAlimentacion.setTotalRecetasUsadas(recetasUsadas.size());
        diaAlimentacion.setTotalInsumosConsumidos(insumosConsumidos.size());
        diaAlimentacion.setTieneAlertasStockInsuficiente(tieneAlertas);
        diaAlimentacion.setCantidadAlertas(cantidadAlertas);
        
        DiaAlimentacion diaFinal = diaAlimentacionRepository.save(diaAlimentacion);
        
        logger.info("Consumo diario generado exitosamente. Fecha: {} - Lotes: {} - Animales: {} - Alertas: {}", 
                   fecha, lotesAtendidos.size(), totalAnimales, cantidadAlertas);
        
        return diaFinal;
    }

    /**
     * Calcular consumos para una fecha específica
     * Obtiene todas las recrías y madres activas y calcula su consumo según recetas
     */
    private List<ConsumoDiarioAutomatico> calcularConsumosParaFecha(LocalDate fecha, Empresa empresa) {
        List<ConsumoDiarioAutomatico> consumos = new ArrayList<>();
        
        // 1. Obtener todas las recrías activas al día
        // Filtrar: fecha ingreso <= fecha Y (sin fecha salida O fecha salida >= fecha)
        List<Recria> todasRecrias = recriaRepository.findByEmpresaAndActivoTrue(empresa);
        List<Recria> recrias = todasRecrias.stream()
            .filter(r -> r.getFechaIngreso() != null && 
                       (r.getFechaIngreso().isBefore(fecha) || r.getFechaIngreso().isEqual(fecha)))
            .filter(r -> r.getFechaSalida() == null || 
                       r.getFechaSalida().isAfter(fecha) || r.getFechaSalida().isEqual(fecha))
            .filter(r -> recriaStockService.animalesDisponibles(r) > 0)
            .collect(Collectors.toList());
        
        // 2. Obtener todas las madres gestantes/lactantes al día
        List<Madre> madres = obtenerMadresActivasEnFecha(fecha, empresa);
        
        // 3. Para cada recría: calcular consumo
        for (Recria recria : recrias) {
            try {
                // a. Determinar etapa de alimentación según edad/peso
                String etapaAlimentacion = determinarEtapaAlimentacion(recria, fecha);
                
                if (etapaAlimentacion == null) {
                    logger.warn("No se pudo determinar etapa de alimentación para recría {} en fecha {}", 
                               recria.getId(), fecha);
                    continue;
                }
                
                // b. Obtener receta asignada para esa etapa
                RecetaAlimentacionPorEtapa recetaEtapa = obtenerRecetaParaEtapa(etapaAlimentacion, empresa, recria, fecha);
                
                if (recetaEtapa == null) {
                    logger.warn("No hay receta asignada para etapa {} en fecha {}. Recría: {}", 
                               etapaAlimentacion, fecha, recria.getId());
                    continue;
                }

                int animalesVivos = recriaStockService.animalesDisponibles(recria);

                // c. Crear consumo: cantidadRecetaTotal = cantidad por animal × animales vivos (cantidadAnimales − muertes activas)
                // d. Crear ConsumoDiarioAutomatico
                ConsumoDiarioAutomatico consumo = new ConsumoDiarioAutomatico(
                    null, // Se asignará después
                    recria,
                    etapaAlimentacion,
                    animalesVivos,
                    recetaEtapa.getInsumoCompuesto(),
                    recetaEtapa.getCantidadDiariaPorAnimal()
                );
                
                consumo.setLoteId(recria.getLoteId());
                if (recria.getLoteId() != null && recria.getEmpresa() != null) {
                    String nom = loteParaPorcinosQuery.listarLotesPorcinosPorEmpresa(recria.getEmpresa().getId()).stream()
                        .filter(dto -> recria.getLoteId().equals(dto.id()))
                        .findFirst()
                        .map(dto -> dto.nombre())
                        .orElse(null);
                    recria.setLoteNombre(nom);
                    consumo.setRecriaNombre(nom);
                }
                if (recetaEtapa.getInsumoCompuesto() != null) {
                    consumo.setRecetaNombre(recetaEtapa.getInsumoCompuesto().getNombre());
                }
                
                consumos.add(consumo);
                
            } catch (Exception e) {
                logger.error("Error al calcular consumo para recría {}: {}", recria.getId(), e.getMessage(), e);
                // Continuar con la siguiente recría
            }
        }
        
        // 4. Para cada madre gestante/lactante: calcular consumo
        for (Madre madre : madres) {
            try {
                String etapaAlimentacion = determinarEtapaAlimentacionMadre(madre, fecha, empresa);
                
                if (etapaAlimentacion == null) {
                    continue;
                }
                
                RecetaAlimentacionPorEtapa recetaEtapa = obtenerRecetaParaEtapa(etapaAlimentacion, empresa, null, fecha);
                
                if (recetaEtapa == null) {
                    continue;
                }
                
                // Madres consumen cantidad por animal (normalmente 1 madre = 1 animal)
                BigDecimal cantidadTotal = recetaEtapa.getCantidadDiariaPorAnimal();
                
                ConsumoDiarioAutomatico consumo = new ConsumoDiarioAutomatico();
                consumo.setMadre(madre);
                consumo.setEtapaAlimentacion(etapaAlimentacion);
                consumo.setCantidadAnimales(1);
                consumo.setReceta(recetaEtapa.getInsumoCompuesto());
                consumo.setCantidadDiariaPorAnimal(recetaEtapa.getCantidadDiariaPorAnimal());
                consumo.setCantidadRecetaTotal(cantidadTotal);
                consumo.setCantidadRecetaReal(null);
                consumo.setTipoRegistroConsumo(ConsumoDiarioAutomatico.TipoRegistroConsumo.ESTIMADO);
                consumo.setMadreNombre(madre.getIdentificacion() != null ? madre.getIdentificacion() : "Madre " + madre.getId());
                consumo.setRecetaNombre(recetaEtapa.getInsumoCompuesto().getNombre());
                
                consumos.add(consumo);
                
            } catch (Exception e) {
                logger.error("Error al calcular consumo para madre {}: {}", madre.getId(), e.getMessage(), e);
            }
        }
        
        return consumos;
    }

    /**
     * Determinar etapa de alimentación para una recría según edad y peso
     */
    private String determinarEtapaAlimentacion(Recria recria, LocalDate fecha) {
        // Calcular edad en días
        long edadDias = ChronoUnit.DAYS.between(recria.getFechaIngreso(), fecha);
        
        // Si tiene etapa asignada, usar esa
        if (recria.getEtapa() != null) {
            return mapearEtapaRecriaAEtapaAlimentacion(recria.getEtapa());
        }
        
        // Si no, determinar según edad (reglas generales)
        // F1: 0-35 días, F2: 36-70 días, F3: 71-105 días, F4: 106-140 días
        // DESARROLLO: 141-180 días, TERMINACION: >180 días
        if (edadDias <= 35) {
            return "F1";
        } else if (edadDias <= 70) {
            return "F2";
        } else if (edadDias <= 105) {
            return "F3";
        } else if (edadDias <= 140) {
            return "F4";
        } else if (edadDias <= 180) {
            return "DESARROLLO";
        } else {
            return "TERMINACION";
        }
    }

    /**
     * Mapear EtapaRecria a EtapaAlimentacion (string)
     */
    private String mapearEtapaRecriaAEtapaAlimentacion(Recria.EtapaRecria etapaRecria) {
        if (etapaRecria == null) {
            return "F1";
        }
        
        switch (etapaRecria) {
            case F1: return "F1";
            case F2: return "F2";
            case F3: return "F3";
            case F4: return "F4";
            case DESARROLLO: return "DESARROLLO";
            case TERMINACION: return "TERMINACION";
            default: return "F1";
        }
    }

    /**
     * Determinar etapa de alimentación para una madre según gestaciones y partos activos
     */
    private String determinarEtapaAlimentacionMadre(Madre madre, LocalDate fecha, Empresa empresa) {
        // 1. Buscar gestación activa en la fecha
        Optional<Gestacion> gestacionActivaOpt = gestacionRepository.findActivaByMadre(madre);
        
        if (gestacionActivaOpt.isPresent()) {
            Gestacion gestacion = gestacionActivaOpt.get();
            // Verificar que la fecha está dentro del rango de la gestación
            if (gestacion.getFechaInicio() != null && 
                (gestacion.getFechaInicio().isBefore(fecha) || gestacion.getFechaInicio().isEqual(fecha)) &&
                (gestacion.getFechaProbableParto() == null || 
                 gestacion.getFechaProbableParto().isAfter(fecha) || gestacion.getFechaProbableParto().isEqual(fecha))) {
                return "GESTACION";
            }
        }
        
        // 2. Buscar partos activos (sin destete) para determinar lactancia
        List<Parto> partos = partoRepository.findByMadreAndActivoTrue(madre);
        
        // Buscar parto más reciente sin destete o con destete posterior a la fecha
        for (Parto parto : partos) {
            if (parto.getFechaInicio() != null) {
                LocalDate fechaParto = parto.getFechaInicio().toLocalDate();
                
                // Si la fecha está después o igual al parto, verificar si está en período de lactancia
                if (fechaParto.isBefore(fecha) || fechaParto.isEqual(fecha)) {
                    // Buscar destete activo para este parto usando repositorio (evitar lazy loading)
                    Optional<Destete> desteteOpt = desteteRepository.findByPartoAndActivoTrue(parto);
                    
                    if (desteteOpt.isEmpty()) {
                        // No tiene destete, está en lactancia
                        // Obtener parámetros para calcular días de lactancia
                        int diasLactancia = 21; // Valor por defecto
                        try {
                            Optional<ParametrosProductivosPorcino> parametrosOpt = 
                                parametrosProductivosRepository.findByEmpresa(empresa);
                            
                            if (parametrosOpt.isPresent() && parametrosOpt.get().getDiasLactancia() != null) {
                                diasLactancia = parametrosOpt.get().getDiasLactancia();
                            }
                        } catch (Exception e) {
                            logger.warn("No se pudo obtener parámetros de lactancia para empresa {}. Usando valor por defecto: {} días", 
                                       empresa.getId(), diasLactancia);
                        }
                        
                        LocalDate fechaFinLactancia = fechaParto.plusDays(diasLactancia);
                        
                        // Si la fecha está dentro del período de lactancia
                        if (fecha.isBefore(fechaFinLactancia) || fecha.isEqual(fechaFinLactancia)) {
                            return "LACTANCIA";
                        }
                    } else {
                        // Tiene destete, verificar si la fecha es antes del destete
                        Destete destete = desteteOpt.get();
                        if (destete.getFechaDestete() != null) {
                            if (fecha.isBefore(destete.getFechaDestete())) {
                                return "LACTANCIA";
                            }
                        }
                    }
                }
            }
        }
        
        // Si no hay gestación activa ni parto sin destete, asumir GESTACION (estado por defecto)
        // Esto cubre casos donde la madre no tiene registros recientes
        return "GESTACION";
    }

    /**
     * Obtener receta asignada para una etapa.
     * Si {@code recria} y {@code fecha} están presentes, filtra por edad y peso (rangos en {@link RecetaAlimentacionPorEtapa});
     * prioriza la receta marcada por defecto entre las candidatas; si ninguna filtra, usa todas las activas.
     * Para madres pasar {@code recria} = null (sin filtro por peso/edad de lote).
     */
    private RecetaAlimentacionPorEtapa obtenerRecetaParaEtapa(
            String etapaAlimentacion,
            Empresa empresa,
            Recria recria,
            LocalDate fecha) {
        try {
            RecetaAlimentacionPorEtapa.EtapaAlimentacion etapa =
                RecetaAlimentacionPorEtapa.EtapaAlimentacion.valueOf(etapaAlimentacion);

            List<RecetaAlimentacionPorEtapa> todasActivas =
                recetaEtapaRepository.findByEmpresaAndEtapaAndActivoTrue(empresa, etapa);
            if (todasActivas.isEmpty()) {
                return null;
            }

            List<RecetaAlimentacionPorEtapa> candidatas = filtrarRecetasPorContextoAnimal(todasActivas, recria, fecha);
            if (candidatas.isEmpty()) {
                candidatas = todasActivas;
            }

            Optional<RecetaAlimentacionPorEtapa> recetaDefecto =
                recetaEtapaRepository.findByEmpresaAndEtapaAndEsPorDefectoTrueAndActivoTrue(empresa, etapa);
            if (recetaDefecto.isPresent()) {
                RecetaAlimentacionPorEtapa def = recetaDefecto.get();
                boolean defectoEnCandidatas = candidatas.stream().anyMatch(r -> r.getId().equals(def.getId()));
                if (defectoEnCandidatas) {
                    return def;
                }
            }

            return candidatas.get(0);
        } catch (IllegalArgumentException e) {
            logger.error("Etapa de alimentación no válida: {}", etapaAlimentacion);
            return null;
        }
    }

    private List<RecetaAlimentacionPorEtapa> filtrarRecetasPorContextoAnimal(
            List<RecetaAlimentacionPorEtapa> recetas,
            Recria recria,
            LocalDate fecha) {
        if (recria == null || fecha == null) {
            return new ArrayList<>(recetas);
        }
        int edadDias = calcularEdadDiasRecriaEnFecha(recria, fecha);
        BigDecimal peso = resolverPesoPromedioRecriaEnFecha(recria, fecha);
        List<RecetaAlimentacionPorEtapa> filtradas = recetas.stream()
            .filter(r -> recetaCumpleRangosPesoYEdad(r, peso, edadDias))
            .collect(Collectors.toList());
        return filtradas;
    }

    private int calcularEdadDiasRecriaEnFecha(Recria recria, LocalDate fecha) {
        if (recria.getFechaIngreso() == null) {
            return 0;
        }
        long dias = ChronoUnit.DAYS.between(recria.getFechaIngreso(), fecha);
        long positivos = Math.max(0L, dias);
        return (int) Math.min(positivos, Integer.MAX_VALUE - 1L);
    }

    /**
     * Último peso promedio registrado en o antes de {@code fecha}; si no hay, el de la recría.
     */
    private BigDecimal resolverPesoPromedioRecriaEnFecha(Recria recria, LocalDate fecha) {
        List<RegistroPeso> pesajes = registroPesoRepository.findByRecriaOrderByFechaPesajeDesc(recria);
        for (RegistroPeso rp : pesajes) {
            if (rp.getFechaPesaje() != null && !rp.getFechaPesaje().isAfter(fecha)) {
                return rp.getPesoPromedio();
            }
        }
        return recria.getPesoPromedio();
    }

    /**
     * Aplica rangos de edad y peso definidos en la receta; si no hay peso conocido, solo se aplican rangos de edad
     * y los límites de peso de la receta se omiten.
     */
    private boolean recetaCumpleRangosPesoYEdad(RecetaAlimentacionPorEtapa r, BigDecimal pesoPromedio, int edadDias) {
        if (r.getEdadMinimaDias() != null && edadDias < r.getEdadMinimaDias()) {
            return false;
        }
        if (r.getEdadMaximaDias() != null && edadDias > r.getEdadMaximaDias()) {
            return false;
        }
        if (pesoPromedio != null) {
            if (r.getPesoMinimoAnimal() != null && pesoPromedio.compareTo(r.getPesoMinimoAnimal()) < 0) {
                return false;
            }
            if (r.getPesoMaximoAnimal() != null && pesoPromedio.compareTo(r.getPesoMaximoAnimal()) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Obtener madres activas en una fecha específica
     * Filtra por: madre activa Y (sin fecha de baja O fecha de baja >= fecha)
     */
    private List<Madre> obtenerMadresActivasEnFecha(LocalDate fecha, Empresa empresa) {
        // Obtener todas las madres activas de la empresa
        List<Madre> todasMadres = madreRepository.findByEmpresaAndActivoTrue(empresa);
        
        // Filtrar: madre debe estar activa en esa fecha
        // - No debe tener fecha de baja O fecha de baja debe ser >= fecha
        return todasMadres.stream()
            .filter(m -> m.getFechaBaja() == null || 
                        m.getFechaBaja().isAfter(fecha) || 
                        m.getFechaBaja().isEqual(fecha))
            .collect(Collectors.toList());
    }

    /**
     * Registra la cantidad real de ración (kg) para un consumo del día, recalcula detalles y el resumen de alertas.
     * Solo si el día sigue {@link DiaAlimentacion.EstadoDia#PENDIENTE} y pertenece a la empresa del usuario.
     */
    @Transactional
    public ConsumoDiarioAutomatico registrarCantidadRecetaReal(Long consumoDiarioId, BigDecimal cantidadRecetaKg, Long usuarioId) {
        if (cantidadRecetaKg == null || cantidadRecetaKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad de ración real debe ser mayor que cero");
        }
        Empresa empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario sin empresa activa"));
        ConsumoDiarioAutomatico consumo = consumoDiarioRepository.findByIdConDiaYEmpresa(consumoDiarioId)
            .orElseThrow(() -> new IllegalArgumentException("Consumo diario no encontrado: " + consumoDiarioId));
        DiaAlimentacion dia = consumo.getDiaAlimentacion();
        if (!dia.getEmpresa().getId().equals(empresa.getId())) {
            throw new IllegalArgumentException("El consumo no pertenece a la empresa del usuario");
        }
        if (dia.estaCerrado()) {
            throw new IllegalStateException("No se puede modificar un día de alimentación ya confirmado");
        }
        if (dia.getEstado() != DiaAlimentacion.EstadoDia.PENDIENTE) {
            throw new IllegalStateException("El día no está en estado PENDIENTE");
        }
        List<ConsumoDiarioDetalle> antiguos = consumoDetalleRepository.findByConsumoDiarioOrderByTipoComponenteAsc(consumo);
        consumoDetalleRepository.deleteAll(antiguos);
        consumo.getDetalles().clear();
        consumo.setCantidadRecetaReal(cantidadRecetaKg);
        consumo.setTipoRegistroConsumo(ConsumoDiarioAutomatico.TipoRegistroConsumo.REAL);
        consumoDiarioRepository.save(consumo);
        procesarComponentesReceta(consumo, empresa);
        consumo.marcarComoProcesado();
        consumoDiarioRepository.save(consumo);
        actualizarResumenAlertasDia(dia);
        return consumo;
    }

    private void actualizarResumenAlertasDia(DiaAlimentacion dia) {
        List<ConsumoDiarioAutomatico> consumosDelDia = consumoDiarioRepository.findByDiaAlimentacion(dia);
        int cantidadAlertas = 0;
        boolean tieneAlertas = false;
        for (ConsumoDiarioAutomatico c : consumosDelDia) {
            long alertas = consumoDetalleRepository.countByConsumoDiarioAndTieneDeficitTrue(c);
            cantidadAlertas += (int) alertas;
            if (alertas > 0) {
                tieneAlertas = true;
            }
        }
        dia.setCantidadAlertas(cantidadAlertas);
        dia.setTieneAlertasStockInsuficiente(tieneAlertas);
        diaAlimentacionRepository.save(dia);
    }

    /**
     * Procesar componentes de una receta y crear detalles SIN descontar stock.
     * El descuento se realiza al confirmar el día desde el calendario.
     */
    @Transactional
    private void procesarComponentesReceta(ConsumoDiarioAutomatico consumo, Empresa empresa) {
        InsumoCompuesto receta = consumo.getReceta();
        BigDecimal cantidadTotalReceta = consumo.getCantidadRecetaEfectiva();
        
        // Obtener componentes de la receta
        List<ComponenteInsumoCompuesto> componentes = componenteRepository
            .findByInsumoCompuesto(receta);
        
        if (componentes.isEmpty()) {
            logger.warn("La receta {} no tiene componentes. Consumo: {}", 
                       receta.getNombre(), consumo.getId());
            return;
        }
        
        // Para cada componente, calcular cantidad requerida y crear detalle (sin descontar)
        for (ComponenteInsumoCompuesto componente : componentes) {
            try {
                BigDecimal cantidadRequerida = componente.calcularCantidadNecesaria(cantidadTotalReceta);
                crearDetalleSinDescontar(consumo, componente, cantidadRequerida, empresa);
            } catch (Exception e) {
                logger.error("Error al procesar componente {} en consumo {}: {}", 
                           componente.getId(), consumo.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Crear detalle del consumo SIN descontar stock.
     * Solo calcula cantidades y alertas; el descuento se hace al confirmar el día.
     */
    private void crearDetalleSinDescontar(
            ConsumoDiarioAutomatico consumo,
            ComponenteInsumoCompuesto componente,
            BigDecimal cantidadRequerida,
            Empresa empresa) {
        
        BigDecimal stockAnterior;
        BigDecimal stockPosterior;
        BigDecimal deficit = BigDecimal.ZERO;
        boolean tieneDeficit = false;
        
        if (componente.getTipoComponente() == ComponenteInsumoCompuesto.TipoComponente.INSUMO &&
            componente.getInsumo() != null) {

            Long empresaId = empresa.getId();
            Long insumoId = componente.getInsumo().getId();
            stockAnterior = inventoryService.consultarStock(empresaId, insumoId);
            if (stockAnterior == null) stockAnterior = BigDecimal.ZERO;
            stockPosterior = stockAnterior;
            if (stockAnterior.compareTo(cantidadRequerida) < 0) {
                deficit = cantidadRequerida.subtract(stockAnterior);
                tieneDeficit = true;
            }

            Insumo ref = new Insumo();
            ref.setId(insumoId);
            String unidadMedida = inventoryService.consultarProducto(empresaId, insumoId).map(pi -> pi.unidadMedida()).orElse("");

            ConsumoDiarioDetalle detalle = new ConsumoDiarioDetalle(
                consumo,
                ConsumoDiarioDetalle.TipoComponente.INSUMO,
                cantidadRequerida,
                stockAnterior
            );
            detalle.setInsumo(ref);
            detalle.setStockResultante(stockPosterior);
            detalle.setCantidadDescontada(BigDecimal.ZERO);
            detalle.setDeficit(deficit);
            detalle.setTieneDeficit(tieneDeficit);
            detalle.setUnidadMedida(unidadMedida);

            consumoDetalleRepository.save(detalle);
            consumo.agregarDetalle(detalle);

        } else if (componente.getTipoComponente() == ComponenteInsumoCompuesto.TipoComponente.GRANO_PROPIO && 
                   componente.getCultivo() != null) {
            Cultivo cultivo = componente.getCultivo();
            Long empresaId = empresa.getId();
            stockAnterior = inventoryService.consultarStockGrano(empresaId, cultivo.getId());
            if (stockAnterior == null) stockAnterior = BigDecimal.ZERO;
            stockPosterior = stockAnterior;
            if (stockAnterior.compareTo(cantidadRequerida) < 0) {
                deficit = cantidadRequerida.subtract(stockAnterior);
                tieneDeficit = true;
            }

            ConsumoDiarioDetalle detalle = new ConsumoDiarioDetalle(
                consumo,
                ConsumoDiarioDetalle.TipoComponente.GRANO_PROPIO,
                cantidadRequerida,
                stockAnterior
            );
            detalle.setCultivoId(cultivo.getId());
            detalle.setStockResultante(stockPosterior);
            detalle.setCantidadDescontada(BigDecimal.ZERO);
            detalle.setDeficit(deficit);
            detalle.setTieneDeficit(tieneDeficit);
            detalle.setUnidadMedida("kg");
            
            consumoDetalleRepository.save(detalle);
            consumo.agregarDetalle(detalle);
            
        } else if (componente.getTipoComponente() == ComponenteInsumoCompuesto.TipoComponente.INSUMO_COMPUESTO && 
                   componente.getInsumoCompuestoPadre() != null) {
            InsumoCompuesto subReceta = componente.getInsumoCompuestoPadre();
            Long empresaIdComp = empresa.getId();
            Long insumoCompuestoId = subReceta.getId();
            stockAnterior = inventoryService.consultarStockInsumoCompuesto(empresaIdComp, insumoCompuestoId);
            if (stockAnterior == null) stockAnterior = BigDecimal.ZERO;
            stockPosterior = stockAnterior;
            if (stockAnterior.compareTo(cantidadRequerida) < 0) {
                deficit = cantidadRequerida.subtract(stockAnterior);
                tieneDeficit = true;
            }

            ConsumoDiarioDetalle detalle = new ConsumoDiarioDetalle(
                consumo,
                ConsumoDiarioDetalle.TipoComponente.INSUMO_COMPUESTO,
                cantidadRequerida,
                stockAnterior
            );
            detalle.setInsumoCompuesto(subReceta);
            detalle.setStockResultante(stockPosterior);
            detalle.setCantidadDescontada(BigDecimal.ZERO);
            detalle.setDeficit(deficit);
            detalle.setTieneDeficit(tieneDeficit);
            detalle.setUnidadMedida(subReceta.getUnidadMedida());
            
            consumoDetalleRepository.save(detalle);
            consumo.agregarDetalle(detalle);
        }
    }

    /**
     * Descontar stock al confirmar el día desde el calendario.
     * Ejecuta el consumo real en inventario y registra movimientos.
     */
    @Transactional
    public void descontarStockAlConfirmar(DiaAlimentacion dia, User usuario) {
        Empresa empresa = dia.getEmpresa();
        List<ConsumoDiarioAutomatico> consumos = consumoDiarioRepository
            .findByDiaAlimentacionOrderByEtapaAlimentacionAsc(dia);

        for (ConsumoDiarioAutomatico consumo : consumos) {
            List<ConsumoDiarioDetalle> detalles = consumoDetalleRepository
                .findByConsumoDiarioOrderByTipoComponenteAsc(consumo);

            for (ConsumoDiarioDetalle detalle : detalles) {
                BigDecimal cantidadRequerida = detalle.getCantidadRequerida();
                Long empresaId = empresa.getId();

                if (detalle.getTipoComponente() == ConsumoDiarioDetalle.TipoComponente.INSUMO && detalle.getInsumo() != null) {
                    Long insumoId = detalle.getInsumo().getId();
                    BigDecimal stockAnterior = inventoryService.consultarStock(empresaId, insumoId);
                    if (stockAnterior == null) stockAnterior = BigDecimal.ZERO;

                    InventoryResult r = inventoryService.consumirPermitiendoNegativo(empresaId, insumoId, cantidadRequerida,
                            InventoryOrigin.PORCINOS, consumo.getId(), usuario != null ? usuario.getId() : null);
                    if (!r.exito()) throw new RuntimeException(r.mensaje());
                    BigDecimal stockPosterior = r.stockRestante();

                    Insumo ref = new Insumo();
                    ref.setId(insumoId);
                    registrarMovimientoStock(
                        MovimientoStockPorcino.TipoMovimiento.CONSUMO_AUTOMATICO,
                        empresa, dia.getFecha(), ref, null, null,
                        MovimientoStockPorcino.TipoInsumo.INSUMO,
                        cantidadRequerida, stockAnterior, stockPosterior, true,
                        dia, consumo, usuario, consumo.getRecria(),
                        consumo.getRecria() != null ? consumo.getRecria().getLoteId() : null);

                    detalle.setCantidadDescontada(cantidadRequerida);
                    detalle.setStockResultante(stockPosterior);
                    consumoDetalleRepository.save(detalle);

                } else if (detalle.getTipoComponente() == ConsumoDiarioDetalle.TipoComponente.GRANO_PROPIO && detalle.getCultivoId() != null) {
                    Long cultivoId = detalle.getCultivoId();
                    BigDecimal stockAnterior = inventoryService.consultarStockGrano(empresaId, cultivoId);
                    if (stockAnterior == null) stockAnterior = BigDecimal.ZERO;

                    InventoryResult r = inventoryService.consumirGranoPermitiendoNegativo(empresaId, cultivoId,
                            cantidadRequerida, InventoryOrigin.PORCINOS, consumo.getId(), usuario != null ? usuario.getId() : null);
                    if (!r.exito()) throw new RuntimeException(r.mensaje());
                    BigDecimal stockPosterior = r.stockRestante();

                    registrarMovimientoStock(
                        MovimientoStockPorcino.TipoMovimiento.CONSUMO_AUTOMATICO,
                        empresa, dia.getFecha(), null, cultivoId, null,
                        MovimientoStockPorcino.TipoInsumo.GRANO_PROPIO,
                        cantidadRequerida, stockAnterior, stockPosterior, true,
                        dia, consumo, usuario, consumo.getRecria(), null);

                    detalle.setCantidadDescontada(cantidadRequerida);
                    detalle.setStockResultante(stockPosterior);
                    consumoDetalleRepository.save(detalle);

                } else if (detalle.getTipoComponente() == ConsumoDiarioDetalle.TipoComponente.INSUMO_COMPUESTO && detalle.getInsumoCompuesto() != null) {
                    InsumoCompuesto subReceta = detalle.getInsumoCompuesto();
                    Long insumoCompuestoId = subReceta.getId();
                    BigDecimal stockAnterior = inventoryService.consultarStockInsumoCompuesto(empresaId, insumoCompuestoId);
                    if (stockAnterior == null) stockAnterior = BigDecimal.ZERO;

                    InventoryResult r = inventoryService.consumirInsumoCompuestoPermitiendoNegativo(empresaId, insumoCompuestoId,
                            cantidadRequerida, InventoryOrigin.PORCINOS, consumo.getId(), usuario != null ? usuario.getId() : null);
                    if (!r.exito()) throw new RuntimeException(r.mensaje());
                    BigDecimal stockPosterior = r.stockRestante();

                    registrarMovimientoStock(
                        MovimientoStockPorcino.TipoMovimiento.CONSUMO_AUTOMATICO,
                        empresa, dia.getFecha(), null, null, subReceta,
                        MovimientoStockPorcino.TipoInsumo.INSUMO_COMPUESTO,
                        cantidadRequerida, stockAnterior, stockPosterior, true,
                        dia, consumo, usuario, consumo.getRecria(),
                        consumo.getRecria() != null ? consumo.getRecria().getLoteId() : null);

                    detalle.setCantidadDescontada(cantidadRequerida);
                    detalle.setStockResultante(stockPosterior);
                    consumoDetalleRepository.save(detalle);
                }
            }
        }
    }

    /**
     * Registrar movimiento de stock para trazabilidad
     */
    private void registrarMovimientoStock(
            MovimientoStockPorcino.TipoMovimiento tipoMovimiento,
            Empresa empresa,
            LocalDate fechaMovimiento,
            Insumo insumo, Long cultivoId, InsumoCompuesto insumoCompuesto,
            MovimientoStockPorcino.TipoInsumo tipoInsumo,
            BigDecimal cantidad,
            BigDecimal stockAnterior,
            BigDecimal stockPosterior,
            Boolean permiteNegativo,
            DiaAlimentacion diaAlimentacion,
            ConsumoDiarioAutomatico consumoDiario,
            User usuario,
            Recria recria,
            Long loteId) {
        
        MovimientoStockPorcino movimiento = new MovimientoStockPorcino(
            empresa, tipoMovimiento, fechaMovimiento, tipoInsumo,
            cantidad, stockAnterior, stockPosterior, permiteNegativo
        );
        
        movimiento.setConsumoDiario(consumoDiario);
        movimiento.setDiaAlimentacion(diaAlimentacion);
        movimiento.setInsumo(insumo);
        movimiento.setCultivoId(cultivoId);
        movimiento.setInsumoCompuesto(insumoCompuesto);
        movimiento.setUsuario(usuario);
        movimiento.setRecria(recria);
        movimiento.setLoteId(loteId);
        
        if (consumoDiario != null) {
            movimiento.setMotivo("Consumo diario automático - " + 
                                (consumoDiario.getRecria() != null ? 
                                 "Recría: " + consumoDiario.getRecriaNombre() : 
                                 "Madre: " + consumoDiario.getMadreNombre()));
        }
        
        movimientoStockRepository.save(movimiento);
    }

    /**
     * Obtener día de alimentación por fecha
     */
    @Transactional(readOnly = true)
    public Optional<DiaAlimentacion> obtenerDiaPorFecha(LocalDate fecha, Long userId) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresaOpt.isEmpty()) {
            return Optional.empty();
        }
        
        return diaAlimentacionRepository.findByEmpresaAndFecha(empresaOpt.get(), fecha);
    }

    /**
     * Obtener calendario mensual
     */
    @Transactional(readOnly = true)
    public Map<LocalDate, DiaAlimentacion> obtenerCalendarioMensual(int año, int mes, Long userId) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresaOpt.isEmpty()) {
            return Map.of();
        }
        
        LocalDate fechaInicio = LocalDate.of(año, mes, 1);
        LocalDate fechaFin = fechaInicio.withDayOfMonth(fechaInicio.lengthOfMonth());
        
        List<DiaAlimentacion> dias = diaAlimentacionRepository
            .findByEmpresaAndFechaBetweenOrderByFechaDesc(empresaOpt.get(), fechaInicio, fechaFin);
        
        return dias.stream()
            .collect(Collectors.toMap(DiaAlimentacion::getFecha, dia -> dia));
    }
}
