package com.agrocloud.chatia.herramientas.impl;

import com.agrocloud.chatia.herramientas.*;
import com.agrocloud.cultivos.domain.HistorialCosecha;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.infrastructure.HistorialCosechaRepository;
import com.agrocloud.cultivos.infrastructure.LaborRepository;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProveedorHerramientasCultivos implements ProveedorHerramientasChatIa {

    private final PlotRepository plotRepository;
    private final LaborRepository laborRepository;
    private final HistorialCosechaRepository historialCosechaRepository;

    public ProveedorHerramientasCultivos(
            @Qualifier("plotRepositoryCultivos") PlotRepository plotRepository,
            @Qualifier("laborRepositoryCultivos") LaborRepository laborRepository,
            @Qualifier("historialCosechaRepositoryCultivos") HistorialCosechaRepository historialCosechaRepository) {
        this.plotRepository = plotRepository;
        this.laborRepository = laborRepository;
        this.historialCosechaRepository = historialCosechaRepository;
    }

    @Override
    public List<HerramientaConsultaChatIa> obtenerHerramientas() {
        Map<String, Map<String, Object>> propsLotes = Map.of(
                "busqueda", HerramientasUtil.propiedadString("Filtrar por nombre de lote (opcional)"),
                "limite", HerramientasUtil.propiedadEntero("Cantidad máxima de resultados (default 10)")
        );
        Map<String, Map<String, Object>> propsLabores = Map.of(
                "nombreLote", HerramientasUtil.propiedadString("Nombre o identificador del lote agrícola"),
                "tipos", HerramientasUtil.propiedadListaString("Tipos de labor: CONTROL_PLAGAS, CONTROL_MALEZAS, SIEMBRA, FERTILIZACION, etc."),
                "limite", HerramientasUtil.propiedadEntero("Cantidad máxima (default 10)"),
                "incluirCostos", HerramientasUtil.propiedadBooleano("Incluir costo total de cada labor")
        );
        Map<String, Map<String, Object>> propsCosechas = Map.of(
                "nombreLote", HerramientasUtil.propiedadString("Filtrar por nombre de lote (opcional)"),
                "limite", HerramientasUtil.propiedadEntero("Cantidad máxima (default 10)")
        );

        return List.of(
                new HerramientaConsultaChatIa(
                        "listarLotesCultivo",
                        "CULTIVOS",
                        "Lista lotes agrícolas (parcelas de cultivo) accesibles para el usuario.",
                        HerramientasUtil.esquemaConPropiedades(propsLotes, List.of()),
                        (ctx, args) -> listarLotes(ctx, args)),
                new HerramientaConsultaChatIa(
                        "consultarLaboresPorLote",
                        "CULTIVOS",
                        "Consulta labores de un lote agrícola. Usar tipos CONTROL_PLAGAS y CONTROL_MALEZAS para fumigaciones.",
                        HerramientasUtil.esquemaConPropiedades(propsLabores, List.of("nombreLote")),
                        (ctx, args) -> consultarLabores(ctx, args)),
                new HerramientaConsultaChatIa(
                        "ultimasCosechas",
                        "CULTIVOS",
                        "Últimas cosechas registradas en lotes de la empresa.",
                        HerramientasUtil.esquemaConPropiedades(propsCosechas, List.of()),
                        (ctx, args) -> ultimasCosechas(ctx, args))
        );
    }

    private List<Map<String, Object>> listarLotes(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        String busqueda = HerramientasUtil.obtenerTexto(args, "busqueda");
        List<Plot> lotes = plotRepository.findAccessibleByUserAndActivoTrue(ctx.getUsuario());
        return lotes.stream()
                .filter(l -> busqueda == null || busqueda.isBlank()
                        || l.getNombre().toLowerCase().contains(busqueda.toLowerCase()))
                .limit(limite)
                .map(MapeadoresDatosChatIa::mapearLote)
                .collect(Collectors.toList());
    }

    private Object consultarLabores(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        String nombreLote = HerramientasUtil.obtenerTexto(args, "nombreLote");
        if (nombreLote == null || nombreLote.isBlank()) {
            return Map.of("error", "Debe indicar nombreLote");
        }
        Plot lote = resolverLoteCultivo(ctx, nombreLote);
        if (lote == null) {
            return Map.of("error", "No se encontró lote agrícola con nombre: " + nombreLote);
        }
        int limite = HerramientasUtil.resolverLimite(args);
        boolean incluirCostos = HerramientasUtil.obtenerBooleano(args, "incluirCostos", true);
        List<String> tiposTexto = HerramientasUtil.obtenerListaTexto(args, "tipos");

        List<Labor> labores;
        if (tiposTexto.isEmpty()) {
            labores = laborRepository.findByLoteIdOrderByFechaInicioDesc(lote.getId());
        } else {
            labores = new ArrayList<>();
            for (String tipo : tiposTexto) {
                try {
                    Labor.TipoLabor tipoLabor = Labor.TipoLabor.valueOf(tipo.toUpperCase());
                    labores.addAll(laborRepository.findByLoteAndTipoLaborOrderByFechaInicioDesc(lote, tipoLabor));
                } catch (IllegalArgumentException ignored) {
                    // tipo inválido, omitir
                }
            }
            labores.sort(Comparator.comparing(Labor::getFechaInicio, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        return labores.stream()
                .filter(Labor::getActivo)
                .limit(limite)
                .map(l -> MapeadoresDatosChatIa.mapearLabor(l, incluirCostos))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> ultimasCosechas(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        String nombreLote = HerramientasUtil.obtenerTexto(args, "nombreLote");
        List<HistorialCosecha> historial = historialCosechaRepository.findAccessibleByUserConLoteYCultivo(ctx.getUsuario());
        return historial.stream()
                .filter(h -> nombreLote == null || nombreLote.isBlank()
                        || (h.getLote() != null && h.getLote().getNombre().toLowerCase().contains(nombreLote.toLowerCase())))
                .limit(limite)
                .map(h -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("lote", h.getLote() != null ? h.getLote().getNombre() : null);
                    m.put("cultivo", h.getCultivo() != null ? h.getCultivo().getNombre() : null);
                    m.put("fechaCosecha", h.getFechaCosecha() != null ? h.getFechaCosecha().toString() : null);
                    m.put("rendimientoKgHa", h.getRendimientoReal());
                    m.put("cantidadCosechada", h.getCantidadCosechada());
                    return m;
                })
                .collect(Collectors.toList());
    }

    private Plot resolverLoteCultivo(ContextoConsultaChatIa ctx, String nombreLote) {
        List<Plot> lotes = plotRepository.findAccessibleByUserAndActivoTrue(ctx.getUsuario());
        String busqueda = nombreLote.toLowerCase();
        return lotes.stream()
                .filter(l -> l.getNombre().equalsIgnoreCase(nombreLote)
                        || l.getNombre().toLowerCase().contains(busqueda)
                        || String.valueOf(l.getId()).equals(nombreLote))
                .findFirst()
                .orElse(null);
    }
}
