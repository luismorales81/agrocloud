package com.agrocloud.chatia.herramientas.impl;

import com.agrocloud.chatia.herramientas.*;
import com.agrocloud.feedlot.model.entity.FeedlotConsumo;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.entity.FeedlotPesada;
import com.agrocloud.feedlot.repository.FeedlotConsumoRepository;
import com.agrocloud.feedlot.repository.FeedlotLoteRepository;
import com.agrocloud.feedlot.repository.FeedlotPesadaRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProveedorHerramientasFeedlot implements ProveedorHerramientasChatIa {

    private final FeedlotLoteRepository loteRepository;
    private final FeedlotConsumoRepository consumoRepository;
    private final FeedlotPesadaRepository pesadaRepository;

    public ProveedorHerramientasFeedlot(
            FeedlotLoteRepository loteRepository,
            FeedlotConsumoRepository consumoRepository,
            FeedlotPesadaRepository pesadaRepository) {
        this.loteRepository = loteRepository;
        this.consumoRepository = consumoRepository;
        this.pesadaRepository = pesadaRepository;
    }

    @Override
    public List<HerramientaConsultaChatIa> obtenerHerramientas() {
        Map<String, Map<String, Object>> props = Map.of(
                "limite", HerramientasUtil.propiedadEntero("Cantidad máxima"),
                "nombreLote", HerramientasUtil.propiedadString("Filtrar por nombre/código de lote feedlot (opcional)")
        );
        return List.of(
                new HerramientaConsultaChatIa("listarLotesFeedlot", "FEEDLOT",
                        "Lista lotes de engorde a corral (feedlot).",
                        HerramientasUtil.esquemaConPropiedades(Map.of("limite", HerramientasUtil.propiedadEntero("Cantidad máxima")), List.of()),
                        (ctx, args) -> listarLotes(ctx, args)),
                new HerramientaConsultaChatIa("consumosRecientesFeedlot", "FEEDLOT",
                        "Consumos de alimento recientes en feedlot.",
                        HerramientasUtil.esquemaConPropiedades(props, List.of()),
                        (ctx, args) -> consumos(ctx, args)),
                new HerramientaConsultaChatIa("pesadasRecientesFeedlot", "FEEDLOT",
                        "Pesadas recientes en feedlot.",
                        HerramientasUtil.esquemaConPropiedades(props, List.of()),
                        (ctx, args) -> pesadas(ctx, args))
        );
    }

    private List<Map<String, Object>> listarLotes(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        return loteRepository.listarPorEmpresaIdYCampanaId(ctx.getEmpresaId(), ctx.getCampanaId()).stream()
                .limit(limite)
                .map(l -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", l.getId());
                    m.put("nombre", l.getNombre());
                    m.put("estado", l.getEstado() != null ? l.getEstado().name() : null);
                    m.put("cabezasActuales", l.getCabezasActuales());
                    return m;
                }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> consumos(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        FeedlotLote lote = resolverLote(ctx, args);
        List<FeedlotConsumo> consumos = new ArrayList<>();
        if (lote != null) {
            consumos.addAll(consumoRepository.listarPorLoteIdYEmpresaId(lote.getId(), ctx.getEmpresaId()));
        } else {
            for (FeedlotLote l : loteRepository.listarPorEmpresaIdYCampanaId(ctx.getEmpresaId(), ctx.getCampanaId())) {
                consumos.addAll(consumoRepository.listarPorLoteIdYEmpresaId(l.getId(), ctx.getEmpresaId()));
            }
            consumos.sort(Comparator.comparing(FeedlotConsumo::getFecha, Comparator.nullsLast(Comparator.reverseOrder())));
        }
        return consumos.stream().limit(limite).map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("fecha", c.getFecha() != null ? c.getFecha().toString() : null);
            m.put("cantidadKg", c.getCantidadKg());
            m.put("loteId", c.getLote() != null ? c.getLote().getId() : null);
            return m;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> pesadas(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        FeedlotLote lote = resolverLote(ctx, args);
        List<FeedlotPesada> pesadas = new ArrayList<>();
        if (lote != null) {
            pesadas.addAll(pesadaRepository.listarPorLoteIdYEmpresaId(lote.getId(), ctx.getEmpresaId()));
        } else {
            for (FeedlotLote l : loteRepository.listarPorEmpresaIdYCampanaId(ctx.getEmpresaId(), ctx.getCampanaId())) {
                pesadas.addAll(pesadaRepository.listarPorLoteIdYEmpresaId(l.getId(), ctx.getEmpresaId()));
            }
            pesadas.sort(Comparator.comparing(FeedlotPesada::getFecha, Comparator.nullsLast(Comparator.reverseOrder())));
        }
        return pesadas.stream().limit(limite).map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("fecha", p.getFecha() != null ? p.getFecha().toString() : null);
            m.put("pesoPromedioKg", p.getPesoPromedioKg());
            m.put("cabezasMuestreadas", p.getCabezasMuestreadas());
            return m;
        }).collect(Collectors.toList());
    }

    private FeedlotLote resolverLote(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        String nombre = HerramientasUtil.obtenerTexto(args, "nombreLote");
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        return loteRepository.listarPorEmpresaIdYCampanaId(ctx.getEmpresaId(), ctx.getCampanaId()).stream()
                .filter(l -> l.getNombre() != null && (l.getNombre().equalsIgnoreCase(nombre)
                        || l.getNombre().toLowerCase().contains(nombre.toLowerCase())
                        || String.valueOf(l.getId()).equals(nombre)))
                .findFirst().orElse(null);
    }
}
