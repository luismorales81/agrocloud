package com.agrocloud.chatia.herramientas.impl;

import com.agrocloud.chatia.herramientas.*;
import com.agrocloud.porcinos.model.entity.*;
import com.agrocloud.porcinos.repository.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProveedorHerramientasPorcinos implements ProveedorHerramientasChatIa {

    private final PorcinosLoteRepository loteRepository;
    private final PorcinosConsumoRepository consumoRepository;
    private final PorcinosEventoSanitarioRepository eventoSanitarioRepository;
    private final PorcinosVentaRepository ventaRepository;
    private final PorcinosPesadaRepository pesadaRepository;

    public ProveedorHerramientasPorcinos(
            PorcinosLoteRepository loteRepository,
            PorcinosConsumoRepository consumoRepository,
            PorcinosEventoSanitarioRepository eventoSanitarioRepository,
            PorcinosVentaRepository ventaRepository,
            PorcinosPesadaRepository pesadaRepository) {
        this.loteRepository = loteRepository;
        this.consumoRepository = consumoRepository;
        this.eventoSanitarioRepository = eventoSanitarioRepository;
        this.ventaRepository = ventaRepository;
        this.pesadaRepository = pesadaRepository;
    }

    @Override
    public List<HerramientaConsultaChatIa> obtenerHerramientas() {
        Map<String, Map<String, Object>> propsComun = Map.of(
                "limite", HerramientasUtil.propiedadEntero("Cantidad máxima (default 10)"),
                "nombreLote", HerramientasUtil.propiedadString("Filtrar por nombre de lote porcino (opcional)")
        );
        return List.of(
                new HerramientaConsultaChatIa("listarLotesPorcinos", "PORCINOS",
                        "Lista lotes porcinos de la empresa en la campaña activa.",
                        HerramientasUtil.esquemaConPropiedades(Map.of("limite", HerramientasUtil.propiedadEntero("Cantidad máxima")), List.of()),
                        (ctx, args) -> listarLotes(ctx, args)),
                new HerramientaConsultaChatIa("consumosRecientesPorcinos", "PORCINOS",
                        "Consumos de alimento recientes en porcinos.",
                        HerramientasUtil.esquemaConPropiedades(propsComun, List.of()),
                        (ctx, args) -> consumos(ctx, args)),
                new HerramientaConsultaChatIa("eventosSanitariosPorcinos", "PORCINOS",
                        "Eventos sanitarios recientes en porcinos.",
                        HerramientasUtil.esquemaConPropiedades(propsComun, List.of()),
                        (ctx, args) -> eventosSanitarios(ctx, args)),
                new HerramientaConsultaChatIa("ventasRecientesPorcinos", "PORCINOS",
                        "Ventas recientes de porcinos.",
                        HerramientasUtil.esquemaConPropiedades(propsComun, List.of()),
                        (ctx, args) -> ventas(ctx, args)),
                new HerramientaConsultaChatIa("pesadasRecientesPorcinos", "PORCINOS",
                        "Pesadas recientes en lotes porcinos.",
                        HerramientasUtil.esquemaConPropiedades(propsComun, List.of()),
                        (ctx, args) -> pesadas(ctx, args))
        );
    }

    private List<Map<String, Object>> listarLotes(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        List<PorcinosLote> lotes = loteRepository.listarPorEmpresaIdYCampanaId(ctx.getEmpresaId(), ctx.getCampanaId());
        return lotes.stream().limit(limite).map(this::mapaLote).collect(Collectors.toList());
    }

    private List<Map<String, Object>> consumos(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        PorcinosLote lote = resolverLote(ctx, args);
        List<PorcinosConsumo> consumos = lote != null
                ? consumoRepository.listarPorLoteIdYEmpresaId(lote.getId(), ctx.getEmpresaId())
                : consumoRepository.listarPorEmpresaId(ctx.getEmpresaId());
        return consumos.stream().limit(limite).map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("fecha", c.getFecha() != null ? c.getFecha().toString() : null);
            m.put("cantidadKg", c.getCantidadKg());
            m.put("lote", c.getLote() != null ? c.getLote().getNombre() : null);
            return m;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> eventosSanitarios(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        return eventoSanitarioRepository.listarPorEmpresaId(ctx.getEmpresaId()).stream()
                .limit(limite)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("fecha", e.getFecha() != null ? e.getFecha().toString() : null);
                    m.put("tipo", e.getTipo() != null ? e.getTipo().name() : null);
                    m.put("descripcion", e.getDescripcion());
                    return m;
                }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> ventas(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        return ventaRepository.listarPorEmpresaId(ctx.getEmpresaId()).stream()
                .limit(limite)
                .map(v -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("fecha", v.getFecha() != null ? v.getFecha().toString() : null);
                    m.put("tipo", v.getTipo() != null ? v.getTipo().name() : null);
                    m.put("cabezas", v.getCabezas());
                    m.put("total", v.getTotal());
                    return m;
                }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> pesadas(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        return pesadaRepository.listarPorEmpresaId(ctx.getEmpresaId()).stream()
                .limit(limite)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("fecha", p.getFecha() != null ? p.getFecha().toString() : null);
                    m.put("pesoPromedioKg", p.getPesoPromedioKg());
                    m.put("cabezasMuestreadas", p.getCabezasMuestreadas());
                    return m;
                }).collect(Collectors.toList());
    }

    private PorcinosLote resolverLote(ContextoConsultaChatIa ctx, Map<String, Object> args) {
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

    private Map<String, Object> mapaLote(PorcinosLote lote) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", lote.getId());
        m.put("nombre", lote.getNombre());
        m.put("estado", lote.getEstado() != null ? lote.getEstado().name() : null);
        m.put("etapa", lote.getEtapa() != null ? lote.getEtapa().name() : null);
        m.put("cabezasInicial", lote.getCabezasInicial());
        m.put("cabezasActuales", lote.getCabezasActuales());
        m.put("pesoPromedioIngresoKg", lote.getPesoPromedioIngresoKg());
        m.put("fechaIngreso", lote.getFechaIngreso() != null ? lote.getFechaIngreso().toString() : null);
        m.put("fechaCierre", lote.getFechaCierre() != null ? lote.getFechaCierre().toString() : null);
        m.put("galpon", lote.getGalpon() != null ? lote.getGalpon().getNombre() : null);
        m.put("origen", lote.getOrigen() != null ? lote.getOrigen().name() : null);
        return m;
    }
}
