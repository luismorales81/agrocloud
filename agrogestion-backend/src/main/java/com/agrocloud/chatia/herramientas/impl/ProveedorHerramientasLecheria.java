package com.agrocloud.chatia.herramientas.impl;

import com.agrocloud.chatia.herramientas.*;
import com.agrocloud.lecheria.model.entity.*;
import com.agrocloud.lecheria.repository.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProveedorHerramientasLecheria implements ProveedorHerramientasChatIa {

    private final LecheriaAnimalRepository animalRepository;
    private final LecheriaRegistroOrdeneRepository ordeneRepository;
    private final LecheriaEventoSanitarioRepository eventoSanitarioRepository;
    private final LecheriaVentaLecheRepository ventaLecheRepository;

    public ProveedorHerramientasLecheria(
            LecheriaAnimalRepository animalRepository,
            LecheriaRegistroOrdeneRepository ordeneRepository,
            LecheriaEventoSanitarioRepository eventoSanitarioRepository,
            LecheriaVentaLecheRepository ventaLecheRepository) {
        this.animalRepository = animalRepository;
        this.ordeneRepository = ordeneRepository;
        this.eventoSanitarioRepository = eventoSanitarioRepository;
        this.ventaLecheRepository = ventaLecheRepository;
    }

    @Override
    public List<HerramientaConsultaChatIa> obtenerHerramientas() {
        Map<String, Map<String, Object>> propsLimite = Map.of("limite", HerramientasUtil.propiedadEntero("Cantidad máxima"));
        Map<String, Map<String, Object>> propsProduccion = Map.of(
                "fechaDesde", HerramientasUtil.propiedadString("Fecha desde ISO (opcional)"),
                "fechaHasta", HerramientasUtil.propiedadString("Fecha hasta ISO (opcional)"),
                "limite", HerramientasUtil.propiedadEntero("Cantidad máxima de registros")
        );
        return List.of(
                new HerramientaConsultaChatIa("listarAnimalesLecheria", "LECHERIA",
                        "Lista animales lecheros de la empresa.",
                        HerramientasUtil.esquemaConPropiedades(propsLimite, List.of()),
                        (ctx, args) -> listarAnimales(ctx, args)),
                new HerramientaConsultaChatIa("produccionLechePeriodo", "LECHERIA",
                        "Registros de ordeñe / producción de leche en un período.",
                        HerramientasUtil.esquemaConPropiedades(propsProduccion, List.of()),
                        (ctx, args) -> produccionLeche(ctx, args)),
                new HerramientaConsultaChatIa("eventosSanitariosLecheria", "LECHERIA",
                        "Eventos sanitarios recientes en lechería.",
                        HerramientasUtil.esquemaConPropiedades(propsLimite, List.of()),
                        (ctx, args) -> eventosSanitarios(ctx, args)),
                new HerramientaConsultaChatIa("ventasLecheLecheria", "LECHERIA",
                        "Ventas de leche recientes.",
                        HerramientasUtil.esquemaConPropiedades(propsLimite, List.of()),
                        (ctx, args) -> ventasLeche(ctx, args))
        );
    }

    private List<Map<String, Object>> listarAnimales(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        return animalRepository.listarPorEmpresaId(ctx.getEmpresaId()).stream()
                .limit(limite)
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", a.getId());
                    m.put("identificacion", a.getIdentificacion());
                    m.put("especie", a.getEspecie() != null ? a.getEspecie().name() : null);
                    m.put("sexo", a.getSexo() != null ? a.getSexo().name() : null);
                    m.put("estado", a.getEstado() != null ? a.getEstado().name() : null);
                    m.put("raza", a.getRaza() != null ? a.getRaza().getNombre() : null);
                    m.put("rodeo", a.getRodeo() != null ? a.getRodeo().getNombre() : null);
                    m.put("fechaNacimiento", a.getFechaNacimiento() != null ? a.getFechaNacimiento().toString() : null);
                    m.put("fechaIngreso", a.getFechaIngreso() != null ? a.getFechaIngreso().toString() : null);
                    return m;
                }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> produccionLeche(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        LocalDate desde = HerramientasUtil.obtenerFecha(args, "fechaDesde");
        LocalDate hasta = HerramientasUtil.obtenerFecha(args, "fechaHasta");
        return ordeneRepository.listarPorEmpresaId(ctx.getEmpresaId()).stream()
                .filter(o -> desde == null || (o.getFecha() != null && !o.getFecha().isBefore(desde)))
                .filter(o -> hasta == null || (o.getFecha() != null && !o.getFecha().isAfter(hasta)))
                .limit(limite)
                .map(o -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("fecha", o.getFecha() != null ? o.getFecha().toString() : null);
                    m.put("litros", o.getLitros());
                    m.put("turno", o.getTurno() != null ? o.getTurno().name() : null);
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

    private List<Map<String, Object>> ventasLeche(ContextoConsultaChatIa ctx, Map<String, Object> args) {
        int limite = HerramientasUtil.resolverLimite(args);
        return ventaLecheRepository.listarPorEmpresaId(ctx.getEmpresaId()).stream()
                .limit(limite)
                .map(v -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("fecha", v.getFecha() != null ? v.getFecha().toString() : null);
                    m.put("litros", v.getLitros());
                    m.put("total", v.getTotal());
                    return m;
                }).collect(Collectors.toList());
    }
}
