package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.application.port.LoteParaPorcinosQuery;
import com.agrocloud.cultivos.domain.Cultivo;
import com.agrocloud.cultivos.infrastructure.CultivoRepository;

import com.agrocloud.porcinos.domain.ConsumoDiarioAutomatico;
import com.agrocloud.porcinos.domain.ConsumoDiarioDetalle;
import com.agrocloud.porcinos.domain.Destete;
import com.agrocloud.porcinos.domain.Gestacion;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.MuerteLechon;
import com.agrocloud.porcinos.domain.MuerteRecria;
import com.agrocloud.porcinos.domain.Padrillo;
import com.agrocloud.porcinos.domain.Parto;
import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.domain.Servicio;
import com.agrocloud.porcinos.domain.VentaPorcino;
import com.agrocloud.porcinos.infrastructure.RecriaRepository;
import com.agrocloud.porcinos.infrastructure.MuerteRecriaRepository;
import com.agrocloud.porcinos.infrastructure.PadrilloRepository;
import com.agrocloud.porcinos.infrastructure.DesteteRepository;
import com.agrocloud.porcinos.infrastructure.GestacionRepository;
import com.agrocloud.porcinos.infrastructure.MadreRepository;
import com.agrocloud.porcinos.infrastructure.MadreMuerteRepository;
import com.agrocloud.porcinos.infrastructure.MuerteLechonRepository;
import com.agrocloud.porcinos.infrastructure.PartoRepository;
import com.agrocloud.porcinos.infrastructure.ServicioRepository;
import com.agrocloud.porcinos.infrastructure.VentaPorcinoRepository;
import com.agrocloud.porcinos.domain.DatosEconomicosPorcino;
import com.agrocloud.porcinos.domain.ParametrosProductivosPorcino;
import com.agrocloud.porcinos.infrastructure.DatosEconomicosPorcinoRepository;
import com.agrocloud.porcinos.infrastructure.ParametrosProductivosPorcinoRepository;
import com.agrocloud.porcinos.infrastructure.ConsumoAlimentoRepository;
import com.agrocloud.porcinos.infrastructure.ConsumoDiarioAutomaticoRepository;
import com.agrocloud.porcinos.infrastructure.ConsumoDiarioDetalleRepository;
import com.agrocloud.porcinos.infrastructure.DiaAlimentacionRepository;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import com.agrocloud.core.inventory.domain.InventarioGrano;
import com.agrocloud.core.inventory.infrastructure.ComponenteInsumoCompuestoRepository;
import com.agrocloud.core.inventory.infrastructure.InsumoCompuestoRepository;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import com.agrocloud.core.inventory.infrastructure.InventarioGranoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para calcular KPIs y estadísticas del dashboard de Porcinos
 */
@Service
public class DashboardPorcinosService {

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
    private LoteParaPorcinosQuery loteParaPorcinosQuery;

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
    @Qualifier("inventarioGranoRepositoryInventario")
        private InventarioGranoRepository inventarioGranoRepository;

    @Autowired
    private VentaPorcinoRepository ventaPorcinoRepository;

    @Autowired
    private ParametrosProductivosPorcinoRepository parametrosProductivosRepository;

    @Autowired
    private DatosEconomicosPorcinoRepository datosEconomicosRepository;

    @Autowired
    @SuppressWarnings("unused") // Reservado para futuras implementaciones
    private CultivoRepository cultivoRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    /**
     * Obtener todos los KPIs del dashboard
     */
    public Map<String, Object> obtenerKPIs(User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return new HashMap<>();
        }

        Empresa empresa = empresaActiva.get();
        Map<String, Object> kpis = new HashMap<>();

        // KPIs Productivos
        kpis.putAll(calcularKPIsProductivos(empresa));

        // KPIs Económicos
        kpis.putAll(calcularKPIsEconomicos(empresa));

        // Estadísticas de Mortalidad
        kpis.putAll(calcularEstadisticasMortalidad(empresa));

        // Alertas y Recordatorios
        kpis.put("alertas", obtenerAlertas(empresa));

        return kpis;
    }

    /**
     * Calcular KPIs productivos
     */
    private Map<String, Object> calcularKPIsProductivos(Empresa empresa) {
        Map<String, Object> kpis = new HashMap<>();

        // Total de madres activas
        long totalMadres = madreRepository.findByEmpresaAndActivoTrue(empresa).stream()
            .filter(m -> m.getEstadoActual() != Madre.EstadoMadre.DESCARTE)
            .count();
        kpis.put("totalMadres", totalMadres);

        // Total de padrillos activos
        long totalPadrillos = padrilloRepository.findByEmpresaAndActivoTrue(empresa).size();
        kpis.put("totalPadrillos", totalPadrillos);

        // Servicios pendientes de control
        long serviciosPendientes = servicioRepository.findPendientesControlByEmpresa(empresa).size();
        kpis.put("serviciosPendientesControl", serviciosPendientes);

        // Gestaciones activas
        long gestacionesActivas = gestacionRepository.findByEmpresaAndActivas(empresa).size();
        kpis.put("gestacionesActivas", gestacionesActivas);

        // Partos próximos (próximos 7 días)
        LocalDate hoy = LocalDate.now();
        List<Gestacion> proximosPartos = gestacionRepository.findProximasPartos(
            empresa, hoy, hoy.plusDays(7));
        kpis.put("partosProximos", proximosPartos.size());

        // TASA DE PARICIÓN
        BigDecimal tasaParicion = calcularTasaParicion(empresa);
        kpis.put("tasaParicion", tasaParicion);

        // INTERVALO DESTETE → CONCEPCIÓN
        BigDecimal intervaloDesteteConcepcion = calcularIntervaloDesteteConcepcion(empresa);
        kpis.put("intervaloDesteteConcepcion", intervaloDesteteConcepcion);

        // NACIDOS VIVOS / NACIDOS TOTALES
        Map<String, Object> estadisticasNacimiento = calcularEstadisticasNacimiento(empresa);
        kpis.putAll(estadisticasNacimiento);

        // PESO PROMEDIO POR ETAPA
        Map<String, BigDecimal> pesosPorEtapa = calcularPesoPromedioPorEtapa(empresa);
        kpis.put("pesosPromedioPorEtapa", pesosPorEtapa);

        // UNIFORMIDAD (coeficiente de variación de pesos)
        Map<String, BigDecimal> uniformidad = calcularUniformidad(empresa);
        kpis.put("uniformidad", uniformidad);

        return kpis;
    }

    /**
     * Calcular tasa de parición (% de servicios que resultan en parto)
     */
    private BigDecimal calcularTasaParicion(Empresa empresa) {
        List<Servicio> servicios = servicioRepository.findByEmpresaAndActivoTrue(empresa);
        
        if (servicios.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long serviciosExitosos = servicios.stream()
            .filter(s -> s.getEstadoServicio() == Servicio.EstadoServicio.PREÑEZ_CONFIRMADA)
            .count();

        BigDecimal tasa = BigDecimal.valueOf(serviciosExitosos)
            .divide(BigDecimal.valueOf(servicios.size()), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        return tasa.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcular intervalo destete → concepción (días promedio)
     */
    private BigDecimal calcularIntervaloDesteteConcepcion(Empresa empresa) {
        List<Destete> destetes = desteteRepository.findByEmpresaAndActivoTrue(empresa);
        List<Servicio> servicios = servicioRepository.findByEmpresaAndActivoTrue(empresa);

        List<Long> intervalos = new ArrayList<>();

        for (Destete destete : destetes) {
            Madre madre = destete.getParto().getMadre();
            LocalDate fechaDestete = destete.getFechaDestete();

            // Buscar siguiente servicio después del destete
            Optional<Servicio> siguienteServicio = servicios.stream()
                .filter(s -> s.getMadre().getId().equals(madre.getId()))
                .filter(s -> s.getFechaServicio().isAfter(fechaDestete))
                .min(Comparator.comparing(Servicio::getFechaServicio));

            if (siguienteServicio.isPresent()) {
                long dias = ChronoUnit.DAYS.between(fechaDestete, siguienteServicio.get().getFechaServicio());
                intervalos.add(dias);
            }
        }

        if (intervalos.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double promedio = intervalos.stream().mapToLong(Long::longValue).average().orElse(0.0);
        return BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcular estadísticas de nacimiento
     */
    private Map<String, Object> calcularEstadisticasNacimiento(Empresa empresa) {
        Map<String, Object> stats = new HashMap<>();

        List<Parto> partos = partoRepository.findByEmpresaAndActivoTrue(empresa);

        int totalNacidosVivos = partos.stream()
            .mapToInt(p -> p.getNacidosVivos() != null ? p.getNacidosVivos() : 0)
            .sum();

        int totalNacidosMuertos = partos.stream()
            .mapToInt(p -> p.getNacidosMuertos() != null ? p.getNacidosMuertos() : 0)
            .sum();

        int totalNacidos = totalNacidosVivos + totalNacidosMuertos;

        stats.put("totalNacidosVivos", totalNacidosVivos);
        stats.put("totalNacidosMuertos", totalNacidosMuertos);
        stats.put("totalNacidos", totalNacidos);

        if (totalNacidos > 0) {
            BigDecimal porcentajeVivos = BigDecimal.valueOf(totalNacidosVivos)
                .divide(BigDecimal.valueOf(totalNacidos), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            stats.put("porcentajeNacidosVivos", porcentajeVivos.setScale(2, RoundingMode.HALF_UP));
        } else {
            stats.put("porcentajeNacidosVivos", BigDecimal.ZERO);
        }

        return stats;
    }

    /**
     * Calcular peso promedio por etapa
     */
    private Map<String, BigDecimal> calcularPesoPromedioPorEtapa(Empresa empresa) {
        Map<String, BigDecimal> pesos = new HashMap<>();

        List<Recria> recrias = recriaRepository.findByEmpresaAndActivoTrue(empresa);

        Map<Recria.EtapaRecria, List<BigDecimal>> pesosPorEtapa = recrias.stream()
            .filter(r -> r.getPesoPromedio() != null)
            .collect(Collectors.groupingBy(
                Recria::getEtapa,
                Collectors.mapping(Recria::getPesoPromedio, Collectors.toList())
            ));

        for (Map.Entry<Recria.EtapaRecria, List<BigDecimal>> entry : pesosPorEtapa.entrySet()) {
            BigDecimal promedio = entry.getValue().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(entry.getValue().size()), 2, RoundingMode.HALF_UP);
            pesos.put(entry.getKey().name(), promedio);
        }

        return pesos;
    }

    /**
     * Calcular uniformidad (coeficiente de variación)
     */
    private Map<String, BigDecimal> calcularUniformidad(Empresa empresa) {
        Map<String, BigDecimal> uniformidad = new HashMap<>();

        List<Recria> recrias = recriaRepository.findByEmpresaAndActivoTrue(empresa);

        Map<Recria.EtapaRecria, List<BigDecimal>> pesosPorEtapa = recrias.stream()
            .filter(r -> r.getPesoPromedio() != null)
            .collect(Collectors.groupingBy(
                Recria::getEtapa,
                Collectors.mapping(Recria::getPesoPromedio, Collectors.toList())
            ));

        for (Map.Entry<Recria.EtapaRecria, List<BigDecimal>> entry : pesosPorEtapa.entrySet()) {
            List<BigDecimal> pesos = entry.getValue();
            if (pesos.size() < 2) {
                continue;
            }

            // Calcular promedio
            BigDecimal promedio = pesos.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(pesos.size()), 4, RoundingMode.HALF_UP);

            // Calcular desviación estándar
            BigDecimal sumaDiferencias = pesos.stream()
                .map(p -> p.subtract(promedio).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal varianza = sumaDiferencias.divide(
                BigDecimal.valueOf(pesos.size()), 4, RoundingMode.HALF_UP);

            BigDecimal desviacion = new BigDecimal(Math.sqrt(varianza.doubleValue()));

            // Coeficiente de variación (CV) = (desviación / promedio) * 100
            BigDecimal cv = promedio.compareTo(BigDecimal.ZERO) > 0 ?
                desviacion.divide(promedio, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;

            uniformidad.put(entry.getKey().name(), cv.setScale(2, RoundingMode.HALF_UP));
        }

        return uniformidad;
    }

    /**
     * Calcular KPIs económicos
     */
    private Map<String, Object> calcularKPIsEconomicos(Empresa empresa) {
        Map<String, Object> kpis = new HashMap<>();

        Optional<DatosEconomicosPorcino> datosEconomicosOpt = 
            datosEconomicosRepository.findByEmpresa(empresa);

        if (datosEconomicosOpt.isEmpty()) {
            return kpis;
        }

        DatosEconomicosPorcino datosEconomicos = datosEconomicosOpt.get();

        // CONVERSIÓN ALIMENTICIA
        BigDecimal conversionAlimenticia = calcularConversionAlimenticia(empresa);
        kpis.put("conversionAlimenticia", conversionAlimenticia);

        // COSTO ALIMENTICIO POR CABEZA
        BigDecimal costoAlimenticioPorCabeza = calcularCostoAlimenticioPorCabeza(empresa, datosEconomicos);
        kpis.put("costoAlimenticioPorCabeza", costoAlimenticioPorCabeza);

        // COSTO ALIMENTICIO POR LOTE
        Map<String, BigDecimal> costoPorLote = calcularCostoAlimenticioPorLote(empresa, datosEconomicos);
        kpis.put("costoAlimenticioPorLote", costoPorLote);

        // PRECIO DE VENTA ESTIMADO
        if (datosEconomicos.getPrecioVentaCerdoTerminadoKg() != null) {
            kpis.put("precioVentaEstimadoKg", datosEconomicos.getPrecioVentaCerdoTerminadoKg());
        }

        // MARGEN BRUTO ESTIMADO
        BigDecimal margenBruto = calcularMargenBruto(empresa, datosEconomicos);
        kpis.put("margenBrutoEstimado", margenBruto);

        // COSTO DE ALIMENTO PROPIO VS COMPRADO
        Map<String, Object> costosAlimento = calcularCostosAlimentoPropioVsComprado(empresa, datosEconomicos);
        kpis.putAll(costosAlimento);

        // IMPACTO DEL RENDIMIENTO DEL CULTIVO EN COSTO POR KILO PORCINO
        Map<String, BigDecimal> impactoCultivo = calcularImpactoCultivoEnCostoPorcino(empresa, datosEconomicos);
        kpis.put("impactoCultivoEnCostoPorcino", impactoCultivo);

        return kpis;
    }

    /**
     * Calcular conversión alimenticia promedio real
     */
    private BigDecimal calcularConversionAlimenticia(Empresa empresa) {
        List<Recria> recrias = recriaRepository.findByEmpresaAndActivoTrue(empresa);
        
        if (recrias.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal conversionTotal = BigDecimal.ZERO;
        int recriasConDatos = 0;

        for (Recria recria : recrias) {
            // Obtener consumo total del lote
            BigDecimal consumoTotal = calcularConsumoTotalLote(recria);
            
            if (consumoTotal.compareTo(BigDecimal.ZERO) > 0 && recria.getPesoPromedio() != null) {
                // Obtener peso inicial (del primer registro o del ingreso)
                BigDecimal pesoInicial = recria.getPesoPromedio(); // Por ahora usamos el peso promedio actual
                // En producción se debería obtener del primer registro de peso
                
                BigDecimal pesoFinal = recria.getPesoPromedio();
                BigDecimal gananciaPeso = pesoFinal.subtract(pesoInicial);
                
                if (gananciaPeso.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal conversion = consumoTotal.divide(gananciaPeso, 2, RoundingMode.HALF_UP);
                    conversionTotal = conversionTotal.add(conversion);
                    recriasConDatos++;
                }
            }
        }

        if (recriasConDatos == 0) {
            return BigDecimal.ZERO;
        }

        return conversionTotal.divide(BigDecimal.valueOf(recriasConDatos), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcular costo alimenticio por cabeza
     */
    private BigDecimal calcularCostoAlimenticioPorCabeza(Empresa empresa, DatosEconomicosPorcino datosEconomicos) {
        // Calcular consumo promedio por animal y multiplicar por precio
        // Por ahora retornamos un valor estimado
        if (datosEconomicos.getCostoEngordeDia() != null) {
            return datosEconomicos.getCostoEngordeDia();
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calcular costo alimenticio por lote
     */
    private Map<String, BigDecimal> calcularCostoAlimenticioPorLote(Empresa empresa, DatosEconomicosPorcino datosEconomicos) {
        Map<String, BigDecimal> costosPorLote = new HashMap<>();

        List<Recria> recrias = recriaRepository.findByEmpresaAndActivoTrue(empresa);
        
        for (Recria recria : recrias) {
            if (recria.getLoteId() != null) {
                String loteNombre = recria.getLoteNombre();
                if (loteNombre == null && loteParaPorcinosQuery != null) {
                    loteNombre = loteParaPorcinosQuery.obtenerPorId(recria.getLoteId()).map(dto -> dto.nombre()).orElse("Lote " + recria.getLoteId());
                } else if (loteNombre == null) {
                    loteNombre = "Lote " + recria.getLoteId();
                }
                // Calcular consumo total del lote
                BigDecimal consumoTotal = calcularConsumoTotalLote(recria);
                
                // Calcular costo (consumo * precio por kg)
                BigDecimal precioKg = datosEconomicos.getCostoEngordeDia() != null ?
                    datosEconomicos.getCostoEngordeDia() : BigDecimal.ZERO;
                
                BigDecimal costo = consumoTotal.multiply(precioKg);
                costosPorLote.put(loteNombre, costo.setScale(2, RoundingMode.HALF_UP));
            }
        }

        return costosPorLote;
    }

    /**
     * Calcular consumo total de un lote usando el nuevo sistema de consumo diario automático
     */
    private BigDecimal calcularConsumoTotalLote(Recria recria) {
        if (recria == null || recria.getLoteId() == null) {
            return BigDecimal.ZERO;
        }
        
        // Obtener consumos diarios automáticos asociados a esta recría
        List<ConsumoDiarioAutomatico> consumos = consumoDiarioAutomaticoRepository.findByRecria(recria);
        
        // Sumar las cantidades totales de receta consumidas
        BigDecimal consumoTotal = consumos.stream()
            .map(ConsumoDiarioAutomatico::getCantidadRecetaEfectiva)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return consumoTotal;
    }

    /**
     * Calcular margen bruto estimado
     */
    private BigDecimal calcularMargenBruto(Empresa empresa, DatosEconomicosPorcino datosEconomicos) {
        // Calcular ingresos estimados de ventas
        LocalDate hoy = LocalDate.now();
        LocalDate fechaDesde = hoy.minusMonths(1);
        BigDecimal ingresosTotales = ventaPorcinoRepository.sumIngresosByRangoFechas(
            empresa, fechaDesde, hoy);
        
        if (ingresosTotales == null) {
            ingresosTotales = BigDecimal.ZERO;
        }

        // Calcular costos estimados (alimentación + otros costos) usando el nuevo sistema
        BigDecimal costosAlimentacion = calcularCostoAlimentacionNuevoSistema(empresa, fechaDesde, hoy, datosEconomicos);

        // Calcular margen bruto
        BigDecimal margenBruto = ingresosTotales.subtract(costosAlimentacion);
        return margenBruto.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcular estadísticas de mortalidad
     */
    private Map<String, Object> calcularEstadisticasMortalidad(Empresa empresa) {
        Map<String, Object> stats = new HashMap<>();

        // Mortalidad acumulada por etapa
        Map<String, Integer> mortalidadPorEtapa = new HashMap<>();
        
        // Mortalidad en lactancia
        List<MuerteLechon> muertesLechones = muerteLechonRepository.findByEmpresaAndActivoTrue(empresa);
        int muertesLactancia = muertesLechones.stream()
            .filter(m -> m.getEtapa() == MuerteLechon.EtapaLechon.LACTANCIA)
            .mapToInt(m -> m.getCantidad() != null ? m.getCantidad() : 0)
            .sum();
        mortalidadPorEtapa.put("LACTANCIA", muertesLactancia);

        // Mortalidad en recría
        List<MuerteRecria> muertesRecria = muerteRecriaRepository.findByEmpresaAndActivoTrue(empresa);
        int muertesRecriaTotal = muertesRecria.stream()
            .mapToInt(m -> m.getCantidad() != null ? m.getCantidad() : 0)
            .sum();
        mortalidadPorEtapa.put("RECRIA", muertesRecriaTotal);

        // Mortalidad de madres
        long muertesMadres = madreMuerteRepository.findByEmpresaAndActivoTrue(empresa).size();
        mortalidadPorEtapa.put("MADRES", (int) muertesMadres);

        stats.put("mortalidadPorEtapa", mortalidadPorEtapa);

        // Mortalidad por causa
        Map<String, Integer> mortalidadPorCausa = new HashMap<>();
        
        // Causas de muerte en lechones
        Map<MuerteLechon.CausaMuerteLechon, Integer> causasLechones = muertesLechones.stream()
            .collect(Collectors.groupingBy(
                MuerteLechon::getCausa,
                Collectors.summingInt(m -> m.getCantidad() != null ? m.getCantidad() : 0)
            ));
        
        causasLechones.forEach((causa, cantidad) -> 
            mortalidadPorCausa.put("LECHON_" + causa.name(), cantidad));

        // Causas de muerte en recría
        Map<MuerteRecria.CausaMuerteRecria, Integer> causasRecria = muertesRecria.stream()
            .collect(Collectors.groupingBy(
                MuerteRecria::getCausa,
                Collectors.summingInt(m -> m.getCantidad() != null ? m.getCantidad() : 0)
            ));
        
        causasRecria.forEach((causa, cantidad) -> 
            mortalidadPorCausa.put("RECRIA_" + causa.name(), cantidad));

        stats.put("mortalidadPorCausa", mortalidadPorCausa);

        // Porcentajes de mortalidad
        List<Parto> partos = partoRepository.findByEmpresaAndActivoTrue(empresa);
        int totalNacidosVivos = partos.stream()
            .mapToInt(p -> p.getNacidosVivos() != null ? p.getNacidosVivos() : 0)
            .sum();

        if (totalNacidosVivos > 0) {
            BigDecimal porcentajeMortalidadLactancia = BigDecimal.valueOf(muertesLactancia)
                .divide(BigDecimal.valueOf(totalNacidosVivos), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            stats.put("porcentajeMortalidadLactancia", porcentajeMortalidadLactancia.setScale(2, RoundingMode.HALF_UP));
        }

        return stats;
    }

    /**
     * Obtener alertas y recordatorios
     */
    private List<Map<String, Object>> obtenerAlertas(Empresa empresa) {
        List<Map<String, Object>> alertas = new ArrayList<>();

        Optional<ParametrosProductivosPorcino> parametrosOpt = 
            parametrosProductivosRepository.findByEmpresa(empresa);

        if (parametrosOpt.isEmpty()) {
            return alertas;
        }

        ParametrosProductivosPorcino parametros = parametrosOpt.get();
        LocalDate hoy = LocalDate.now();

        // Alertas de partos próximos
        List<Gestacion> gestacionesActivas = gestacionRepository.findByEmpresaAndActivas(empresa);
        for (Gestacion gestacion : gestacionesActivas) {
            long diasHastaParto = ChronoUnit.DAYS.between(hoy, gestacion.getFechaProbableParto());
            
            if (parametros.getDiasAntelacionAlertarPartos() != null &&
                diasHastaParto <= parametros.getDiasAntelacionAlertarPartos() &&
                diasHastaParto >= 0) {
                
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("tipo", "PARTO_PROXIMO");
                alerta.put("mensaje", String.format("Parto próximo en %d días - Madre: %s", 
                    diasHastaParto, gestacion.getMadre().getIdentificacion()));
                alerta.put("fecha", gestacion.getFechaProbableParto());
                alerta.put("prioridad", "ALTA");
                alertas.add(alerta);
            }
        }

        // Alertas de gestaciones vencidas
        for (Gestacion gestacion : gestacionesActivas) {
            if (hoy.isAfter(gestacion.getFechaProbableParto().plusDays(5))) {
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("tipo", "GESTACION_VENCIDA");
                alerta.put("mensaje", String.format("Gestación vencida sin parto - Madre: %s", 
                    gestacion.getMadre().getIdentificacion()));
                alerta.put("fecha", gestacion.getFechaProbableParto());
                alerta.put("prioridad", "CRITICA");
                alertas.add(alerta);
            }
        }

        // Alertas de mortalidad alta
        if (parametros.getUmbralMortalidadLactanciaPorcentaje() != null) {
            List<Parto> partos = partoRepository.findByEmpresaAndActivoTrue(empresa);
            for (Parto parto : partos) {
                if (parto.getNacidosVivos() != null && parto.getNacidosVivos() > 0) {
                    List<MuerteLechon> muertes = muerteLechonRepository.findByPartoAndActivoTrue(parto);
                    int totalMuertes = muertes.stream()
                        .mapToInt(m -> m.getCantidad() != null ? m.getCantidad() : 0)
                        .sum();
                    
                    BigDecimal porcentajeMortalidad = BigDecimal.valueOf(totalMuertes)
                        .divide(BigDecimal.valueOf(parto.getNacidosVivos()), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                    if (porcentajeMortalidad.compareTo(parametros.getUmbralMortalidadLactanciaPorcentaje()) > 0) {
                        Map<String, Object> alerta = new HashMap<>();
                        alerta.put("tipo", "MORTALIDAD_ALTA");
                        alerta.put("mensaje", String.format("Mortalidad alta en lactancia: %.2f%% - Parto: %s", 
                            porcentajeMortalidad, parto.getId()));
                        alerta.put("porcentaje", porcentajeMortalidad);
                        alerta.put("prioridad", "ALTA");
                        alertas.add(alerta);
                    }
                }
            }
        }

        return alertas;
    }

    /**
     * Calcular costo de alimento propio vs comprado usando el nuevo sistema
     */
    private Map<String, Object> calcularCostosAlimentoPropioVsComprado(Empresa empresa, DatosEconomicosPorcino datosEconomicos) {
        Map<String, Object> costos = new HashMap<>();

        // Obtener consumos diarios automáticos (últimos 30 días como referencia)
        LocalDate fechaHasta = LocalDate.now();
        LocalDate fechaDesde = fechaHasta.minusDays(30);
        
        List<ConsumoDiarioAutomatico> consumos = consumoDiarioAutomaticoRepository
            .findByEmpresaAndFechaBetween(empresa, fechaDesde, fechaHasta);

        BigDecimal costoAlimentoPropio = BigDecimal.ZERO;
        BigDecimal costoAlimentoComprado = BigDecimal.ZERO;
        BigDecimal cantidadAlimentoPropio = BigDecimal.ZERO;
        BigDecimal cantidadAlimentoComprado = BigDecimal.ZERO;

        for (ConsumoDiarioAutomatico consumo : consumos) {
            // Obtener detalles del consumo para identificar componentes
            List<ConsumoDiarioDetalle> detalles = consumoDiarioDetalleRepository
                .findByConsumoDiarioOrderByTipoComponenteAsc(consumo);
            
            for (ConsumoDiarioDetalle detalle : detalles) {
                BigDecimal cantidad = detalle.getCantidadRequerida();
                
                if (detalle.getTipoComponente() == ConsumoDiarioDetalle.TipoComponente.GRANO_PROPIO && 
                    detalle.getCultivoId() != null) {
                    // Grano propio
                    cantidadAlimentoPropio = cantidadAlimentoPropio.add(cantidad);
                    Cultivo cultivo = cultivoRepository.findById(detalle.getCultivoId()).orElse(null);
                    BigDecimal precioKg = calcularPrecioCultivo(cultivo, datosEconomicos, empresa);
                    costoAlimentoPropio = costoAlimentoPropio.add(cantidad.multiply(precioKg));
                    
                } else if (detalle.getTipoComponente() == ConsumoDiarioDetalle.TipoComponente.INSUMO && 
                          detalle.getInsumo() != null) {
                    // Insumo comprado (balanceado, etc.)
                    cantidadAlimentoComprado = cantidadAlimentoComprado.add(cantidad);
                    
                    // Usar precio del insumo o precio de balanceado de datos económicos
                    BigDecimal precioKg = detalle.getInsumo().getPrecioUnitario() != null ?
                        detalle.getInsumo().getPrecioUnitario() : 
                        (datosEconomicos != null && datosEconomicos.getCostoEngordeDia() != null ?
                            datosEconomicos.getCostoEngordeDia() : BigDecimal.valueOf(0.5));
                    costoAlimentoComprado = costoAlimentoComprado.add(cantidad.multiply(precioKg));
                }
                // Nota: INSUMO_COMPUESTO se considera como receta procesada, no se cuenta aquí
            }
        }

        costos.put("costoAlimentoPropio", costoAlimentoPropio.setScale(2, RoundingMode.HALF_UP));
        costos.put("costoAlimentoComprado", costoAlimentoComprado.setScale(2, RoundingMode.HALF_UP));
        costos.put("cantidadAlimentoPropioKg", cantidadAlimentoPropio.setScale(2, RoundingMode.HALF_UP));
        costos.put("cantidadAlimentoCompradoKg", cantidadAlimentoComprado.setScale(2, RoundingMode.HALF_UP));

        BigDecimal totalCantidad = cantidadAlimentoPropio.add(cantidadAlimentoComprado);
        if (totalCantidad.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal porcentajePropio = cantidadAlimentoPropio
                .divide(totalCantidad, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            costos.put("porcentajeAlimentoPropio", porcentajePropio.setScale(2, RoundingMode.HALF_UP));
        }

        return costos;
    }

    /**
     * Calcular precio del cultivo según método de imputación configurado
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

    /**
     * Calcular impacto del rendimiento del cultivo en costo por kilo porcino usando el nuevo sistema
     */
    private Map<String, BigDecimal> calcularImpactoCultivoEnCostoPorcino(Empresa empresa, DatosEconomicosPorcino datosEconomicos) {
        Map<String, BigDecimal> impacto = new HashMap<>();

        // Obtener consumos diarios automáticos (últimos 30 días como referencia)
        LocalDate fechaHasta = LocalDate.now();
        LocalDate fechaDesde = fechaHasta.minusDays(30);
        
        List<ConsumoDiarioAutomatico> consumos = consumoDiarioAutomaticoRepository
            .findByEmpresaAndFechaBetween(empresa, fechaDesde, fechaHasta);

        BigDecimal costoTotalGranoPropio = BigDecimal.ZERO;
        BigDecimal cantidadTotalGranoPropio = BigDecimal.ZERO;
        Map<Long, Cultivo> cultivosUsados = new HashMap<>();

        // Obtener detalles con grano propio
        for (ConsumoDiarioAutomatico consumo : consumos) {
            List<ConsumoDiarioDetalle> detalles = consumoDiarioDetalleRepository
                .findByConsumoDiarioOrderByTipoComponenteAsc(consumo);
            
            for (ConsumoDiarioDetalle detalle : detalles) {
                if (detalle.getTipoComponente() == ConsumoDiarioDetalle.TipoComponente.GRANO_PROPIO && 
                    detalle.getCultivoId() != null) {
                    
                    BigDecimal cantidad = detalle.getCantidadRequerida();
                    cantidadTotalGranoPropio = cantidadTotalGranoPropio.add(cantidad);
                    
                    Optional<Cultivo> cultivoOpt = cultivoRepository.findById(detalle.getCultivoId());
                    if (cultivoOpt.isPresent()) {
                        Cultivo cultivo = cultivoOpt.get();
                        cultivosUsados.put(cultivo.getId(), cultivo);
                        BigDecimal precioKg = calcularPrecioCultivo(cultivo, datosEconomicos, empresa);
                    costoTotalGranoPropio = costoTotalGranoPropio.add(cantidad.multiply(precioKg));
                    }
                }
            }
        }

        if (cantidadTotalGranoPropio.compareTo(BigDecimal.ZERO) == 0) {
            return impacto;
        }

        // Calcular costo promedio por kg de grano propio
        if (cantidadTotalGranoPropio.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal costoPromedioKg = costoTotalGranoPropio
                .divide(cantidadTotalGranoPropio, 2, RoundingMode.HALF_UP);
            impacto.put("costoPromedioGranoPropioKg", costoPromedioKg);
        }

        // Calcular costo promedio por kg de grano propio
        if (cantidadTotalGranoPropio.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal costoPromedioKg = costoTotalGranoPropio
                .divide(cantidadTotalGranoPropio, 2, RoundingMode.HALF_UP);
            impacto.put("costoPromedioGranoPropioKg", costoPromedioKg);
        }

        // Calcular impacto en costo por kilo de cerdo producido
        // Esto requiere conocer la conversión alimenticia y el peso de los animales vendidos
        List<VentaPorcino> ventas = ventaPorcinoRepository.findByEmpresaAndActivoTrue(empresa);
        BigDecimal pesoTotalVendido = ventas.stream()
            .filter(v -> v.getPesoPromedio() != null)
            .map(v -> v.getPesoPromedio().multiply(BigDecimal.valueOf(v.getCantidad())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (pesoTotalVendido.compareTo(BigDecimal.ZERO) > 0 && cantidadTotalGranoPropio.compareTo(BigDecimal.ZERO) > 0) {
            // Calcular costo de grano propio por kg de cerdo producido
            BigDecimal conversionAlimenticia = calcularConversionAlimenticia(empresa);
            if (conversionAlimenticia.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal costoPromedioGranoPropio = costoTotalGranoPropio
                    .divide(cantidadTotalGranoPropio, 2, RoundingMode.HALF_UP);
                BigDecimal costoGranoPropioPorKgCerdo = cantidadTotalGranoPropio
                    .multiply(conversionAlimenticia)
                    .divide(pesoTotalVendido, 4, RoundingMode.HALF_UP)
                    .multiply(costoPromedioGranoPropio);
                
                impacto.put("costoGranoPropioPorKgCerdo", costoGranoPropioPorKgCerdo.setScale(2, RoundingMode.HALF_UP));
            }
        }

        return impacto;
    }

    /**
     * Calcular costo de alimentación usando el nuevo sistema de consumo diario automático
     */
    private BigDecimal calcularCostoAlimentacionNuevoSistema(Empresa empresa, LocalDate fechaDesde, LocalDate fechaHasta, DatosEconomicosPorcino datosEconomicos) {
        BigDecimal costoTotal = BigDecimal.ZERO;
        
        // Obtener consumos diarios automáticos del período
        List<ConsumoDiarioAutomatico> consumos = consumoDiarioAutomaticoRepository
            .findByEmpresaAndFechaBetween(empresa, fechaDesde, fechaHasta);
        
        for (ConsumoDiarioAutomatico consumo : consumos) {
            // Calcular costo de la receta
            InsumoCompuesto receta = consumo.getReceta();
            BigDecimal cantidadReceta = consumo.getCantidadRecetaEfectiva();
            
            // Obtener costo unitario de la receta
            BigDecimal costoUnitarioReceta = BigDecimal.ZERO;
            if (receta != null) {
                costoUnitarioReceta = receta.getCostoUnitario() != null ? receta.getCostoUnitario() : BigDecimal.ZERO;
            }
            
            // Si no tiene costo, usar un valor estimado basado en datos económicos
            if (costoUnitarioReceta.compareTo(BigDecimal.ZERO) == 0 && datosEconomicos != null) {
                // Estimar costo según etapa de alimentación
                String etapa = consumo.getEtapaAlimentacion();
                if (etapa != null) {
                    if (etapa.equals("GESTACION") || etapa.equals("LACTANCIA")) {
                        costoUnitarioReceta = datosEconomicos.getCostoMadreGestacionDia() != null ?
                            datosEconomicos.getCostoMadreGestacionDia() : BigDecimal.valueOf(0.5);
                    } else {
                        costoUnitarioReceta = datosEconomicos.getCostoEngordeDia() != null ?
                            datosEconomicos.getCostoEngordeDia() : BigDecimal.valueOf(0.5);
                    }
                } else {
                    costoUnitarioReceta = datosEconomicos.getCostoEngordeDia() != null ?
                        datosEconomicos.getCostoEngordeDia() : BigDecimal.valueOf(0.5);
                }
            }
            
            // Costo de la receta consumida
            BigDecimal costoReceta = cantidadReceta.multiply(costoUnitarioReceta);
            costoTotal = costoTotal.add(costoReceta);
        }
        
        return costoTotal.setScale(2, RoundingMode.HALF_UP);
    }
}

