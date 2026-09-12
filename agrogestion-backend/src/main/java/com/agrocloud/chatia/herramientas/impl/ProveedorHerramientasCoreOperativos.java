package com.agrocloud.chatia.herramientas.impl;

import com.agrocloud.chatia.herramientas.*;
import com.agrocloud.core.domain.Egreso;
import com.agrocloud.core.domain.Ingreso;
import com.agrocloud.core.infrastructure.EgresoRepository;
import com.agrocloud.core.infrastructure.IngresoRepository;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import com.agrocloud.core.inventory.infrastructure.MovimientoInventarioRepository;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.infrastructure.FieldRepository;
import com.agrocloud.cultivos.infrastructure.LaborRepository;
import com.agrocloud.cultivos.infrastructure.MaquinariaRepository;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProveedorHerramientasCoreOperativos implements ProveedorHerramientasChatIa {

    private final InsumoRepository insumoRepository;
    private final FieldRepository fieldRepository;
    private final PlotRepository plotRepository;
    private final LaborRepository laborRepository;
    private final MaquinariaRepository maquinariaRepository;
    private final IngresoRepository ingresoRepository;
    private final EgresoRepository egresoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public ProveedorHerramientasCoreOperativos(
            @Qualifier("insumoRepositoryInventario") InsumoRepository insumoRepository,
            @Qualifier("fieldRepositoryCultivos") FieldRepository fieldRepository,
            @Qualifier("plotRepositoryCultivos") PlotRepository plotRepository,
            @Qualifier("laborRepositoryCultivos") LaborRepository laborRepository,
            @Qualifier("maquinariaRepositoryCultivos") MaquinariaRepository maquinariaRepository,
            @Qualifier("ingresoRepositoryCore") IngresoRepository ingresoRepository,
            @Qualifier("egresoRepositoryCore") EgresoRepository egresoRepository,
            @Qualifier("movimientoInventarioRepositoryInventario") MovimientoInventarioRepository movimientoInventarioRepository) {
        this.insumoRepository = insumoRepository;
        this.fieldRepository = fieldRepository;
        this.plotRepository = plotRepository;
        this.laborRepository = laborRepository;
        this.maquinariaRepository = maquinariaRepository;
        this.ingresoRepository = ingresoRepository;
        this.egresoRepository = egresoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    @Override
    public List<HerramientaConsultaChatIa> obtenerHerramientas() {
        Map<String, Map<String, Object>> propsMaquinaria = new LinkedHashMap<>();
        propsMaquinaria.put("busqueda", HerramientasUtil.propiedadString("Filtrar por nombre (opcional)"));
        propsMaquinaria.put("soloRequiereMantenimiento", HerramientasUtil.propiedadBooleano(
                "Solo maquinaria que superó el intervalo de mantenimiento por kilometraje"));
        propsMaquinaria.put("limite", HerramientasUtil.propiedadEntero("Cantidad máxima de resultados (default 10)"));
        Map<String, Map<String, Object>> propsBusquedaLimite = Map.of(
                "busqueda", HerramientasUtil.propiedadString("Filtrar por nombre (opcional)"),
                "limite", HerramientasUtil.propiedadEntero("Cantidad máxima de resultados (default 10)")
        );
        Map<String, Map<String, Object>> propsInsumos = new LinkedHashMap<>();
        propsInsumos.put("busqueda", HerramientasUtil.propiedadString("Filtrar por nombre (opcional)"));
        propsInsumos.put("tipo", HerramientasUtil.propiedadString("Tipo: FERTILIZANTE, HERBICIDA, FUNGICIDA, INSECTICIDA, SEMILLA, etc."));
        propsInsumos.put("soloStockBajo", HerramientasUtil.propiedadBooleano("Solo insumos con stock en o bajo el mínimo"));
        propsInsumos.put("limite", HerramientasUtil.propiedadEntero("Cantidad máxima (default 10)"));

        Map<String, Map<String, Object>> propsPorVencer = Map.of(
                "diasAnticipacion", HerramientasUtil.propiedadEntero("Días hacia adelante para considerar por vencer (default 30)"),
                "incluirVencidos", HerramientasUtil.propiedadBooleano("Incluir insumos ya vencidos"),
                "limite", HerramientasUtil.propiedadEntero("Cantidad máxima (default 10)")
        );

        Map<String, Map<String, Object>> propsLabores = new LinkedHashMap<>();
        propsLabores.put("busqueda", HerramientasUtil.propiedadString("Buscar por lote, tipo o responsable (opcional)"));
        propsLabores.put("nombreLote", HerramientasUtil.propiedadString("Filtrar por nombre de lote (opcional)"));
        propsLabores.put("estado", HerramientasUtil.propiedadString("Estado: PLANIFICADA, EN_PROGRESO, COMPLETADA, CANCELADA"));
        propsLabores.put("soloVencidas", HerramientasUtil.propiedadBooleano("Solo labores planificadas con fecha pasada"));
        propsLabores.put("fechaDesde", HerramientasUtil.propiedadString("Fecha desde (YYYY-MM-DD, opcional)"));
        propsLabores.put("fechaHasta", HerramientasUtil.propiedadString("Fecha hasta (YYYY-MM-DD, opcional)"));
        propsLabores.put("limite", HerramientasUtil.propiedadEntero("Cantidad máxima (default 10)"));

        Map<String, Map<String, Object>> propsFinanzas = Map.of(
                "fechaDesde", HerramientasUtil.propiedadString("Fecha desde (YYYY-MM-DD, opcional)"),
                "fechaHasta", HerramientasUtil.propiedadString("Fecha hasta (YYYY-MM-DD, opcional)"),
                "limite", HerramientasUtil.propiedadEntero("Cantidad máxima de movimientos listados (default 10)")
        );

        Map<String, Map<String, Object>> propsInventario = Map.of(
                "nombreInsumo", HerramientasUtil.propiedadString("Filtrar por nombre de insumo (opcional)"),
                "limite", HerramientasUtil.propiedadEntero("Cantidad máxima (default 10)")
        );

        return List.of(
                new HerramientaConsultaChatIa(
                        "listarInsumos",
                        "CORE",
                        "Lista insumos del inventario de la empresa activa. Incluye stock, tipo y vencimiento.",
                        HerramientasUtil.esquemaConPropiedades(propsInsumos, List.of()),
                        (ctx, args) -> listarInsumos(ctx, args)),
                new HerramientaConsultaChatIa(
                        "insumosPorVencer",
                        "CORE",
                        "Lista insumos próximos a vencer o vencidos de la empresa activa.",
                        HerramientasUtil.esquemaConPropiedades(propsPorVencer, List.of()),
                        (ctx, args) -> insumosPorVencer(ctx, args)),
                new HerramientaConsultaChatIa(
                        "listarCampos",
                        "CORE",
                        "Lista campos (chacras/fincas) de la empresa activa.",
                        HerramientasUtil.esquemaConPropiedades(propsBusquedaLimite, List.of()),
                        (ctx, args) -> listarCampos(ctx, args)),
                new HerramientaConsultaChatIa(
                        "listarLotes",
                        "CORE",
                        "Lista lotes de la empresa activa (agrícolas y de otros usos).",
                        HerramientasUtil.esquemaConPropiedades(propsBusquedaLimite, List.of()),
                        (ctx, args) -> listarLotes(ctx, args)),
                new HerramientaConsultaChatIa(
                        "consultarLabores",
                        "CORE",
                        "Consulta labores de la empresa: planificadas, en progreso o completadas.",
                        HerramientasUtil.esquemaConPropiedades(propsLabores, List.of()),
                        (ctx, args) -> consultarLabores(ctx, args)),
                new HerramientaConsultaChatIa(
                        "listarMaquinaria",
                        "CORE",
                        "Lista maquinaria con kilometraje de uso, mantenimiento, costos y rendimiento de combustible.",
                        HerramientasUtil.esquemaConPropiedades(propsMaquinaria, List.of()),
                        (ctx, args) -> listarMaquinaria(ctx, args)),
                new HerramientaConsultaChatIa(
                        "resumenFinanzas",
                        "CORE",
                        "Resumen de ingresos y egresos de la campaña activa en un período.",
                        HerramientasUtil.esquemaConPropiedades(propsFinanzas, List.of()),
                        (ctx, args) -> resumenFinanzas(ctx, args)),
                new HerramientaConsultaChatIa(
                        "movimientosInventarioRecientes",
                        "CORE",
                        "Últimos movimientos de inventario (entradas/salidas) de insumos de la empresa.",
                        HerramientasUtil.esquemaConPropiedades(propsInventario, List.of()),
                        (ctx, args) -> movimientosInventarioRecientes(ctx, args))
        );
    }

    private List<Map<String, Object>> listarInsumos(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        String busqueda = HerramientasUtil.obtenerTexto(args, "busqueda");
        String tipoTexto = HerramientasUtil.obtenerTexto(args, "tipo");
        boolean soloStockBajo = HerramientasUtil.obtenerBooleano(args, "soloStockBajo", false);

        return insumoRepository.findByEmpresaIdAndActivoTrue(ctx.getEmpresaId()).stream()
                .filter(i -> busqueda == null || busqueda.isBlank()
                        || i.getNombre().toLowerCase().contains(busqueda.toLowerCase()))
                .filter(i -> {
                    if (tipoTexto == null || tipoTexto.isBlank()) {
                        return true;
                    }
                    try {
                        return i.getTipo() == Insumo.TipoInsumo.valueOf(tipoTexto.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return i.getTipo().name().toLowerCase().contains(tipoTexto.toLowerCase());
                    }
                })
                .filter(i -> !soloStockBajo || i.isStockBajo())
                .limit(limite)
                .map(MapeadoresDatosChatIa::mapearInsumo)
                .collect(Collectors.toList());
    }

    private Map<String, Object> mapearInsumo(Insumo i) {
        return MapeadoresDatosChatIa.mapearInsumo(i);
    }

    private List<Map<String, Object>> insumosPorVencer(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        int dias = HerramientasUtil.obtenerEntero(args, "diasAnticipacion", 30);
        boolean incluirVencidos = HerramientasUtil.obtenerBooleano(args, "incluirVencidos", true);
        LocalDate hoy = LocalDate.now();
        LocalDate limiteVencimiento = hoy.plusDays(dias);

        return insumoRepository.findByEmpresaIdAndActivoTrue(ctx.getEmpresaId()).stream()
                .filter(i -> i.getFechaVencimiento() != null)
                .filter(i -> {
                    LocalDate venc = i.getFechaVencimiento();
                    boolean vencido = venc.isBefore(hoy);
                    boolean porVencer = !venc.isBefore(hoy) && !venc.isAfter(limiteVencimiento);
                    if (vencido) {
                        return incluirVencidos;
                    }
                    return porVencer;
                })
                .sorted(Comparator.comparing(Insumo::getFechaVencimiento))
                .limit(limite)
                .map(i -> {
                    Map<String, Object> m = mapearInsumo(i);
                    LocalDate venc = i.getFechaVencimiento();
                    m.put("estadoVencimiento", venc.isBefore(hoy) ? "VENCIDO" : "POR_VENCER");
                    m.put("diasRestantes", java.time.temporal.ChronoUnit.DAYS.between(hoy, venc));
                    return m;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> listarCampos(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        String busqueda = HerramientasUtil.obtenerTexto(args, "busqueda");

        return fieldRepository.findByEmpresaIdAndActivoTrue(ctx.getEmpresaId()).stream()
                .filter(f -> busqueda == null || busqueda.isBlank()
                        || f.getNombre().toLowerCase().contains(busqueda.toLowerCase()))
                .limit(limite)
                .map(MapeadoresDatosChatIa::mapearCampo)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> listarLotes(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        String busqueda = HerramientasUtil.obtenerTexto(args, "busqueda");

        return plotRepository.findByCampo_Empresa_IdAndActivoTrue(ctx.getEmpresaId()).stream()
                .filter(l -> busqueda == null || busqueda.isBlank()
                        || l.getNombre().toLowerCase().contains(busqueda.toLowerCase()))
                .limit(limite)
                .map(MapeadoresDatosChatIa::mapearLote)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> consultarLabores(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        List<Long> loteIds = FiltroEmpresaChatIa.idsLotesEmpresa(
                plotRepository.findByCampo_Empresa_IdAndActivoTrue(ctx.getEmpresaId()));
        if (loteIds.isEmpty()) {
            return List.of();
        }

        String busqueda = HerramientasUtil.obtenerTexto(args, "busqueda");
        String nombreLote = HerramientasUtil.obtenerTexto(args, "nombreLote");
        Long loteIdFiltro = null;
        if (nombreLote != null && !nombreLote.isBlank()) {
            loteIdFiltro = plotRepository.findByCampo_Empresa_IdAndActivoTrue(ctx.getEmpresaId()).stream()
                    .filter(l -> l.getNombre().equalsIgnoreCase(nombreLote)
                            || l.getNombre().toLowerCase().contains(nombreLote.toLowerCase()))
                    .map(Plot::getId)
                    .findFirst()
                    .orElse(null);
            if (loteIdFiltro == null) {
                return List.of();
            }
        }

        Labor.EstadoLabor estado = null;
        String estadoTexto = HerramientasUtil.obtenerTexto(args, "estado");
        if (estadoTexto != null && !estadoTexto.isBlank()) {
            try {
                estado = Labor.EstadoLabor.valueOf(estadoTexto.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // sin filtro de estado
            }
        }

        boolean soloVencidas = HerramientasUtil.obtenerBooleano(args, "soloVencidas", false);
        LocalDate fechaDesde = HerramientasUtil.obtenerFecha(args, "fechaDesde");
        LocalDate fechaHasta = HerramientasUtil.obtenerFecha(args, "fechaHasta");

        return laborRepository.findActivasFiltradasByLoteIdIn(
                        loteIds, loteIdFiltro, estado, soloVencidas, fechaDesde, fechaHasta, busqueda)
                .stream()
                .limit(limite)
                .map(MapeadoresDatosChatIa::mapearLabor)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> listarMaquinaria(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        String busqueda = HerramientasUtil.obtenerTexto(args, "busqueda");
        boolean soloRequiereMantenimiento = HerramientasUtil.obtenerBooleano(args, "soloRequiereMantenimiento", false);

        return maquinariaRepository.findByEmpresaIdAndActivoTrue(ctx.getEmpresaId()).stream()
                .filter(m -> busqueda == null || busqueda.isBlank()
                        || m.getNombre().toLowerCase().contains(busqueda.toLowerCase()))
                .map(MapeadoresDatosChatIa::mapearMaquinaria)
                .filter(m -> !soloRequiereMantenimiento || Boolean.TRUE.equals(m.get("requiereMantenimiento")))
                .limit(limite)
                .collect(Collectors.toList());
    }

    private Map<String, Object> resumenFinanzas(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        LocalDate hoy = LocalDate.now();
        LocalDate fechaDesde = HerramientasUtil.obtenerFecha(args, "fechaDesde");
        LocalDate fechaHasta = HerramientasUtil.obtenerFecha(args, "fechaHasta");
        if (fechaDesde == null) {
            fechaDesde = hoy.minusMonths(3);
        }
        if (fechaHasta == null) {
            fechaHasta = hoy;
        }

        Long campanaId = ctx.getCampanaId();
        BigDecimal totalIngresos = campanaId != null
                ? ingresoRepository.calcularTotalIngresosPorCampanaYFecha(campanaId, fechaDesde, fechaHasta)
                : ingresoRepository.calcularTotalIngresosPorUsuarioYFecha(ctx.getUsuarioId(), fechaDesde, fechaHasta);
        BigDecimal totalEgresos = campanaId != null
                ? egresoRepository.calcularTotalEgresosPorCampanaYFecha(campanaId, fechaDesde, fechaHasta)
                : egresoRepository.calcularTotalEgresosPorUsuarioYFecha(ctx.getUsuarioId(), fechaDesde, fechaHasta);

        List<Ingreso> ingresos = campanaId != null
                ? ingresoRepository.findByCampanaIdAndFechaBetweenOrderByFechaDesc(campanaId, fechaDesde, fechaHasta)
                : ingresoRepository.findByUserIdAndFechaBetweenOrderByFechaDesc(ctx.getUsuarioId(), fechaDesde, fechaHasta);
        List<Egreso> egresos = campanaId != null
                ? egresoRepository.findByCampanaIdAndFechaBetweenOrderByFechaDesc(campanaId, fechaDesde, fechaHasta)
                : egresoRepository.findByUserIdAndFechaBetweenOrderByFechaDesc(ctx.getUsuarioId(), fechaDesde, fechaHasta);

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("fechaDesde", fechaDesde.toString());
        resumen.put("fechaHasta", fechaHasta.toString());
        resumen.put("campanaId", campanaId);
        resumen.put("totalIngresos", HerramientasUtil.valorNumerico(totalIngresos));
        resumen.put("totalEgresos", HerramientasUtil.valorNumerico(totalEgresos));
        resumen.put("balance", HerramientasUtil.valorNumerico(
                (totalIngresos != null ? totalIngresos : BigDecimal.ZERO)
                        .subtract(totalEgresos != null ? totalEgresos : BigDecimal.ZERO)));
        resumen.put("ultimosIngresos", ingresos.stream().limit(limite).map(MapeadoresDatosChatIa::mapearIngreso).toList());
        resumen.put("ultimosEgresos", egresos.stream().limit(limite).map(MapeadoresDatosChatIa::mapearEgreso).toList());
        return resumen;
    }

    private List<Map<String, Object>> movimientosInventarioRecientes(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        String nombreInsumo = HerramientasUtil.obtenerTexto(args, "nombreInsumo");

        return movimientoInventarioRepository
                .findRecientesPorEmpresa(ctx.getEmpresaId(), PageRequest.of(0, limite * 3))
                .stream()
                .filter(m -> nombreInsumo == null || nombreInsumo.isBlank()
                        || (m.getInsumo() != null && m.getInsumo().getNombre().toLowerCase()
                        .contains(nombreInsumo.toLowerCase())))
                .limit(limite)
                .map(MapeadoresDatosChatIa::mapearMovimiento)
                .collect(Collectors.toList());
    }
}
