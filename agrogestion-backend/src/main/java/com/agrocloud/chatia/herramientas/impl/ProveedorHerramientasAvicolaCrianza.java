package com.agrocloud.chatia.herramientas.impl;

import com.agrocloud.avicola.crianza.model.entity.AvicolaLote;
import com.agrocloud.avicola.crianza.model.entity.AvicolaMuerte;
import com.agrocloud.avicola.crianza.model.entity.AvicolaPesada;
import com.agrocloud.avicola.crianza.model.enums.AvicolaModuloOrigen;
import com.agrocloud.avicola.crianza.repository.AvicolaLoteRepository;
import com.agrocloud.avicola.crianza.repository.AvicolaMuerteRepository;
import com.agrocloud.avicola.crianza.repository.AvicolaPesadaRepository;
import com.agrocloud.chatia.herramientas.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProveedorHerramientasAvicolaCrianza implements ProveedorHerramientasChatIa {

    private final AvicolaLoteRepository loteRepository;
    private final AvicolaMuerteRepository muerteRepository;
    private final AvicolaPesadaRepository pesadaRepository;

    public ProveedorHerramientasAvicolaCrianza(
            AvicolaLoteRepository loteRepository,
            AvicolaMuerteRepository muerteRepository,
            AvicolaPesadaRepository pesadaRepository) {
        this.loteRepository = loteRepository;
        this.muerteRepository = muerteRepository;
        this.pesadaRepository = pesadaRepository;
    }

    @Override
    public List<HerramientaConsultaChatIa> obtenerHerramientas() {
        Map<String, Map<String, Object>> props = Map.of(
                "limite", HerramientasUtil.propiedadEntero("Cantidad máxima"),
                "nombreLote", HerramientasUtil.propiedadString("Filtrar por nombre de lote (opcional)")
        );
        return List.of(
                new HerramientaConsultaChatIa("listarLotesAvicolaCrianza", "AVICOLA_CRIANZA",
                        "Lista lotes de avícola crianza (parrilleros).",
                        HerramientasUtil.esquemaConPropiedades(Map.of("limite", HerramientasUtil.propiedadEntero("Cantidad máxima")), List.of()),
                        (ctx, args) -> listarLotes(ctx, args)),
                new HerramientaConsultaChatIa("mortalidadAvicolaCrianza", "AVICOLA_CRIANZA",
                        "Registros de mortalidad recientes en crianza.",
                        HerramientasUtil.esquemaConPropiedades(props, List.of()),
                        (ctx, args) -> mortalidad(ctx, args)),
                new HerramientaConsultaChatIa("pesadasAvicolaCrianza", "AVICOLA_CRIANZA",
                        "Pesadas recientes en lotes de crianza.",
                        HerramientasUtil.esquemaConPropiedades(props, List.of()),
                        (ctx, args) -> pesadas(ctx, args))
        );
    }

    private List<Map<String, Object>> listarLotes(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        return loteRepository.listarPorEmpresaIdYModulo(ctx.getEmpresaId(), AvicolaModuloOrigen.AVICOLA_CRIANZA).stream()
                .limit(limite)
                .map(this::mapaLote)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> mortalidad(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        AvicolaLote lote = resolverLote(ctx, args);
        List<AvicolaMuerte> muertes = new ArrayList<>();
        if (lote != null) {
            muertes.addAll(muerteRepository.listarPorLoteIdYEmpresaId(lote.getId(), ctx.getEmpresaId()));
        } else {
            for (AvicolaLote l : loteRepository.listarPorEmpresaIdYModulo(ctx.getEmpresaId(), AvicolaModuloOrigen.AVICOLA_CRIANZA)) {
                muertes.addAll(muerteRepository.listarPorLoteIdYEmpresaId(l.getId(), ctx.getEmpresaId()));
            }
            muertes.sort(Comparator.comparing(AvicolaMuerte::getFecha, Comparator.nullsLast(Comparator.reverseOrder())));
        }
        return muertes.stream().limit(limite).map(m -> {
            Map<String, Object> mapa = new LinkedHashMap<>();
            mapa.put("fecha", m.getFecha() != null ? m.getFecha().toString() : null);
            mapa.put("cantidad", m.getCantidad());
            mapa.put("loteId", m.getLote() != null ? m.getLote().getId() : null);
            return mapa;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> pesadas(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        AvicolaLote lote = resolverLote(ctx, args);
        List<AvicolaPesada> pesadas = new ArrayList<>();
        if (lote != null) {
            pesadas.addAll(pesadaRepository.listarPorLoteIdYEmpresaId(lote.getId(), ctx.getEmpresaId()));
        } else {
            for (AvicolaLote l : loteRepository.listarPorEmpresaIdYModulo(ctx.getEmpresaId(), AvicolaModuloOrigen.AVICOLA_CRIANZA)) {
                pesadas.addAll(pesadaRepository.listarPorLoteIdYEmpresaId(l.getId(), ctx.getEmpresaId()));
            }
            pesadas.sort(Comparator.comparing(AvicolaPesada::getFecha, Comparator.nullsLast(Comparator.reverseOrder())));
        }
        return pesadas.stream().limit(limite).map(p -> {
            Map<String, Object> mapa = new LinkedHashMap<>();
            mapa.put("fecha", p.getFecha() != null ? p.getFecha().toString() : null);
            mapa.put("pesoPromedioKg", p.getPesoPromedio());
            mapa.put("cantidadAves", p.getCantidadPesada());
            return mapa;
        }).collect(Collectors.toList());
    }

    private AvicolaLote resolverLote(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        String nombre = HerramientasUtil.obtenerTexto(args, "nombreLote");
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        return loteRepository.listarPorEmpresaIdYModulo(ctx.getEmpresaId(), AvicolaModuloOrigen.AVICOLA_CRIANZA).stream()
                .filter(l -> l.getNombre() != null && (l.getNombre().equalsIgnoreCase(nombre)
                        || l.getNombre().toLowerCase().contains(nombre.toLowerCase())
                        || String.valueOf(l.getId()).equals(nombre)))
                .findFirst().orElse(null);
    }

    private Map<String, Object> mapaLote(AvicolaLote lote) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", lote.getId());
        m.put("nombre", lote.getNombre());
        m.put("estado", lote.getEstado() != null ? lote.getEstado().name() : null);
        m.put("cantidadAves", lote.getCantidadAnimales());
        return m;
    }
}
