package com.agrocloud.chatia.herramientas.impl;

import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoLote;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoProduccionDiaria;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoLoteRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoProduccionDiariaRepository;
import com.agrocloud.chatia.herramientas.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProveedorHerramientasAvicolaHuevos implements ProveedorHerramientasChatIa {

    private final AvicolaHuevoLoteRepository loteRepository;
    private final AvicolaHuevoProduccionDiariaRepository produccionRepository;

    public ProveedorHerramientasAvicolaHuevos(
            AvicolaHuevoLoteRepository loteRepository,
            AvicolaHuevoProduccionDiariaRepository produccionRepository) {
        this.loteRepository = loteRepository;
        this.produccionRepository = produccionRepository;
    }

    @Override
    public List<HerramientaConsultaChatIa> obtenerHerramientas() {
        Map<String, Map<String, Object>> propsProd = Map.of(
                "limite", HerramientasUtil.propiedadEntero("Cantidad máxima"),
                "nombreLote", HerramientasUtil.propiedadString("Filtrar por nombre de lote (opcional)"),
                "fechaDesde", HerramientasUtil.propiedadString("Fecha desde ISO (opcional)"),
                "fechaHasta", HerramientasUtil.propiedadString("Fecha hasta ISO (opcional)")
        );
        return List.of(
                new HerramientaConsultaChatIa("listarLotesAvicolaHuevos", "AVICOLA_HUEVOS",
                        "Lista lotes de producción de huevos.",
                        HerramientasUtil.esquemaConPropiedades(Map.of("limite", HerramientasUtil.propiedadEntero("Cantidad máxima")), List.of()),
                        (ctx, args) -> listarLotes(ctx, args)),
                new HerramientaConsultaChatIa("produccionDiariaHuevos", "AVICOLA_HUEVOS",
                        "Producción diaria de huevos por lote.",
                        HerramientasUtil.esquemaConPropiedades(propsProd, List.of()),
                        (ctx, args) -> produccionDiaria(ctx, args))
        );
    }

    private List<Map<String, Object>> listarLotes(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        return loteRepository.listarPorEmpresaId(ctx.getEmpresaId()).stream()
                .limit(limite)
                .map(l -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", l.getId());
                    m.put("nombre", l.getNombre());
                    m.put("estado", l.getEstado() != null ? l.getEstado().name() : null);
                    return m;
                }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> produccionDiaria(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        LocalDate desde = HerramientasUtil.obtenerFecha(args, "fechaDesde");
        LocalDate hasta = HerramientasUtil.obtenerFecha(args, "fechaHasta");
        AvicolaHuevoLote lote = resolverLote(ctx, args);
        List<AvicolaHuevoProduccionDiaria> registros = new ArrayList<>();
        if (lote != null) {
            if (desde != null && hasta != null) {
                registros.addAll(produccionRepository.listarPorLoteYEmpresaEnRango(lote.getId(), ctx.getEmpresaId(), desde, hasta));
            } else {
                registros.addAll(produccionRepository.listarPorLoteIdYEmpresaId(lote.getId(), ctx.getEmpresaId()));
            }
        } else {
            for (AvicolaHuevoLote l : loteRepository.listarPorEmpresaId(ctx.getEmpresaId())) {
                registros.addAll(produccionRepository.listarPorLoteIdYEmpresaId(l.getId(), ctx.getEmpresaId()));
            }
            registros.sort(Comparator.comparing(AvicolaHuevoProduccionDiaria::getFecha, Comparator.nullsLast(Comparator.reverseOrder())));
        }
        return registros.stream()
                .filter(r -> desde == null || (r.getFecha() != null && !r.getFecha().isBefore(desde)))
                .filter(r -> hasta == null || (r.getFecha() != null && !r.getFecha().isAfter(hasta)))
                .limit(limite)
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("fecha", r.getFecha() != null ? r.getFecha().toString() : null);
                    m.put("totalHuevosDia", r.getTotalHuevosDia());
                    m.put("loteId", r.getLote() != null ? r.getLote().getId() : null);
                    return m;
                }).collect(Collectors.toList());
    }

    private AvicolaHuevoLote resolverLote(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        String nombre = HerramientasUtil.obtenerTexto(args, "nombreLote");
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        return loteRepository.listarPorEmpresaId(ctx.getEmpresaId()).stream()
                .filter(l -> l.getNombre() != null && (l.getNombre().equalsIgnoreCase(nombre)
                        || l.getNombre().toLowerCase().contains(nombre.toLowerCase())
                        || String.valueOf(l.getId()).equals(nombre)))
                .findFirst().orElse(null);
    }
}
