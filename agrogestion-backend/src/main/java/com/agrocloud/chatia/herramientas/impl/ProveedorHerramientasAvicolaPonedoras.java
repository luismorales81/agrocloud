package com.agrocloud.chatia.herramientas.impl;

import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasAmbienteDiario;
import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasGalpon;
import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasPostura;
import com.agrocloud.avicola.ponedoras.repository.AvicolaPonedorasAmbienteDiarioRepository;
import com.agrocloud.avicola.ponedoras.repository.AvicolaPonedorasGalponRepository;
import com.agrocloud.avicola.ponedoras.repository.AvicolaPonedorasPosturaRepository;
import com.agrocloud.chatia.herramientas.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProveedorHerramientasAvicolaPonedoras implements ProveedorHerramientasChatIa {

    private final AvicolaPonedorasGalponRepository galponRepository;
    private final AvicolaPonedorasPosturaRepository posturaRepository;
    private final AvicolaPonedorasAmbienteDiarioRepository ambienteRepository;

    public ProveedorHerramientasAvicolaPonedoras(
            AvicolaPonedorasGalponRepository galponRepository,
            AvicolaPonedorasPosturaRepository posturaRepository,
            AvicolaPonedorasAmbienteDiarioRepository ambienteRepository) {
        this.galponRepository = galponRepository;
        this.posturaRepository = posturaRepository;
        this.ambienteRepository = ambienteRepository;
    }

    @Override
    public List<HerramientaConsultaChatIa> obtenerHerramientas() {
        Map<String, Map<String, Object>> propsGalpon = Map.of(
                "limite", HerramientasUtil.propiedadEntero("Cantidad máxima"),
                "nombreGalpon", HerramientasUtil.propiedadString("Filtrar por nombre de galpón (opcional)")
        );
        Map<String, Map<String, Object>> propsAmbiente = Map.of(
                "limite", HerramientasUtil.propiedadEntero("Cantidad máxima"),
                "nombreGalpon", HerramientasUtil.propiedadString("Nombre de galpón (opcional)"),
                "fechaDesde", HerramientasUtil.propiedadString("Fecha desde ISO (opcional)"),
                "fechaHasta", HerramientasUtil.propiedadString("Fecha hasta ISO (opcional)")
        );
        return List.of(
                new HerramientaConsultaChatIa("listarGalponesPonedoras", "AVICOLA_PONEDORAS",
                        "Lista galpones del módulo ponedoras.",
                        HerramientasUtil.esquemaConPropiedades(Map.of("limite", HerramientasUtil.propiedadEntero("Cantidad máxima")), List.of()),
                        (ctx, args) -> listarGalpones(ctx, args)),
                new HerramientaConsultaChatIa("produccionPonedoras", "AVICOLA_PONEDORAS",
                        "Registros de postura / producción de huevos en ponedoras.",
                        HerramientasUtil.esquemaConPropiedades(propsGalpon, List.of()),
                        (ctx, args) -> produccion(ctx, args)),
                new HerramientaConsultaChatIa("ambienteDiarioPonedoras", "AVICOLA_PONEDORAS",
                        "Registros de ambiente diario en galpones ponedoras.",
                        HerramientasUtil.esquemaConPropiedades(propsAmbiente, List.of()),
                        (ctx, args) -> ambiente(ctx, args))
        );
    }

    private List<Map<String, Object>> listarGalpones(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        return galponRepository.buscarPorEmpresaId(ctx.getEmpresaId()).stream()
                .limit(limite)
                .map(g -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", g.getId());
                    m.put("nombre", g.getNombre());
                    m.put("estado", g.getEstado() != null ? g.getEstado().name() : null);
                    m.put("cantidadAves", g.getCantidadAves());
                    return m;
                }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> produccion(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        AvicolaPonedorasGalpon galpon = resolverGalpon(ctx, args);
        List<AvicolaPonedorasPostura> posturas = new ArrayList<>();
        if (galpon != null) {
            posturas.addAll(posturaRepository.buscarPorGalponIdYEmpresaId(galpon.getId(), ctx.getEmpresaId()));
        } else {
            for (AvicolaPonedorasGalpon g : galponRepository.buscarPorEmpresaId(ctx.getEmpresaId())) {
                posturas.addAll(posturaRepository.buscarPorGalponIdYEmpresaId(g.getId(), ctx.getEmpresaId()));
            }
            posturas.sort(Comparator.comparing(AvicolaPonedorasPostura::getFecha, Comparator.nullsLast(Comparator.reverseOrder())));
        }
        return posturas.stream().limit(limite).map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("fecha", p.getFecha() != null ? p.getFecha().toString() : null);
            m.put("cantidad", p.getCantidad());
            m.put("categoriaHuevo", p.getCategoriaHuevo() != null ? p.getCategoriaHuevo().name() : null);
            m.put("galponId", p.getGalpon() != null ? p.getGalpon().getId() : null);
            return m;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> ambiente(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        LocalDate desde = HerramientasUtil.obtenerFecha(args, "fechaDesde");
        LocalDate hasta = HerramientasUtil.obtenerFecha(args, "fechaHasta");
        if (desde == null) {
            desde = LocalDate.now().minusDays(30);
        }
        if (hasta == null) {
            hasta = LocalDate.now();
        }
        AvicolaPonedorasGalpon galpon = resolverGalpon(ctx, args);
        List<AvicolaPonedorasAmbienteDiario> registros = new ArrayList<>();
        if (galpon != null) {
            registros.addAll(ambienteRepository.listarPorGalponYFechas(galpon.getId(), ctx.getEmpresaId(), desde, hasta));
        } else {
            for (AvicolaPonedorasGalpon g : galponRepository.buscarPorEmpresaId(ctx.getEmpresaId())) {
                registros.addAll(ambienteRepository.listarPorGalponYFechas(g.getId(), ctx.getEmpresaId(), desde, hasta));
            }
            registros.sort(Comparator.comparing(AvicolaPonedorasAmbienteDiario::getFecha, Comparator.nullsLast(Comparator.reverseOrder())));
        }
        return registros.stream().limit(limite).map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("fecha", a.getFecha() != null ? a.getFecha().toString() : null);
            m.put("temperatura", a.getTemperaturaDia());
            m.put("humedad", a.getHumedadDia());
            m.put("galponId", a.getGalpon() != null ? a.getGalpon().getId() : null);
            return m;
        }).collect(Collectors.toList());
    }

    private AvicolaPonedorasGalpon resolverGalpon(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        String nombre = HerramientasUtil.obtenerTexto(args, "nombreGalpon");
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        return galponRepository.buscarPorEmpresaId(ctx.getEmpresaId()).stream()
                .filter(g -> g.getNombre() != null && (g.getNombre().equalsIgnoreCase(nombre)
                        || g.getNombre().toLowerCase().contains(nombre.toLowerCase())
                        || String.valueOf(g.getId()).equals(nombre)))
                .findFirst().orElse(null);
    }
}
