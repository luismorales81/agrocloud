package com.agrocloud.cultivos.application;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.cultivos.domain.*;
import com.agrocloud.cultivos.infrastructure.*;
import com.agrocloud.dto.wizard.WizardCultivoPropuestaDto;
import com.agrocloud.cultivos.domain.Labor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Persiste la propuesta del wizard (tipo de cultivo, estados, transiciones, tareas, plantillas).
 */
@Service
public class ServicioWizardCultivoPersistencia {

    private static final Set<String> TIPOS_LABOR_VALIDOS = Arrays.stream(Labor.TipoLabor.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    private final EmpresaRepository empresaRepository;
    private final TipoCultivoRepository tipoCultivoRepository;
    private final EstadoLoteConfigRepository estadoLoteConfigRepository;
    private final TransicionEstadoConfigRepository transicionEstadoConfigRepository;
    private final TareaPorEstadoConfigRepository tareaPorEstadoConfigRepository;
    private final PlantillaLaborRepository plantillaLaborRepository;
    private final PlotRepository plotRepository;
    private final CultivoRepository cultivoRepository;

    public ServicioWizardCultivoPersistencia(
            EmpresaRepository empresaRepository,
            TipoCultivoRepository tipoCultivoRepository,
            EstadoLoteConfigRepository estadoLoteConfigRepository,
            TransicionEstadoConfigRepository transicionEstadoConfigRepository,
            TareaPorEstadoConfigRepository tareaPorEstadoConfigRepository,
            PlantillaLaborRepository plantillaLaborRepository,
            PlotRepository plotRepository,
            CultivoRepository cultivoRepository) {
        this.empresaRepository = empresaRepository;
        this.tipoCultivoRepository = tipoCultivoRepository;
        this.estadoLoteConfigRepository = estadoLoteConfigRepository;
        this.transicionEstadoConfigRepository = transicionEstadoConfigRepository;
        this.tareaPorEstadoConfigRepository = tareaPorEstadoConfigRepository;
        this.plantillaLaborRepository = plantillaLaborRepository;
        this.plotRepository = plotRepository;
        this.cultivoRepository = cultivoRepository;
    }

    @Transactional
    public long confirmar(WizardCultivoPropuestaDto dto, Long empresaId) {
        if (dto == null || dto.getTipoCultivo() == null || dto.getTipoCultivo().getNombre() == null || dto.getTipoCultivo().getNombre().isBlank()) {
            throw new IllegalArgumentException("Nombre de tipo de cultivo obligatorio");
        }
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        String nombreBase = dto.getTipoCultivo().getNombre().trim();
        String nombreUnico = generarNombreTipoCultivoUnico(nombreBase);

        TipoCultivo tipo = new TipoCultivo();
        tipo.setNombre(nombreUnico);
        tipo.setDescripcion(Optional.ofNullable(dto.getTipoCultivo().getDescripcion()).orElse("Generado por wizard IA"));
        tipo.setEsPlantilla(false);
        tipo.setActivo(true);
        tipo = tipoCultivoRepository.save(tipo);

        List<WizardCultivoPropuestaDto.EstadoPropuesta> estadosOrdenados = new ArrayList<>(Optional.ofNullable(dto.getEstados()).orElse(List.of()));
        estadosOrdenados.sort(Comparator.comparing(e -> Optional.ofNullable(e.getOrden()).orElse(999)));

        Map<String, EstadoLoteConfig> porNombre = new LinkedHashMap<>();
        int idx = 0;
        for (WizardCultivoPropuestaDto.EstadoPropuesta ep : estadosOrdenados) {
            if (ep.getNombre() == null || ep.getNombre().isBlank()) {
                continue;
            }
            String nombreEstado = ep.getNombre().trim();
            EstadoLoteConfig elc = new EstadoLoteConfig();
            elc.setTipoCultivo(tipo);
            elc.setEmpresa(empresa);
            elc.setNombre(nombreEstado);
            elc.setDescripcion(null);
            elc.setColor(ep.getColor() != null && !ep.getColor().isBlank() ? truncar(ep.getColor(), 20) : "#10b981");
            elc.setIcono("🌱");
            elc.setOrden(Optional.ofNullable(ep.getOrden()).orElse(++idx));
            boolean inicial = Boolean.TRUE.equals(ep.getEsEstadoInicial());
            boolean fin = Boolean.TRUE.equals(ep.getEsEstadoFinal());
            elc.setEsEstadoInicial(inicial);
            elc.setEsEstadoFinal(fin);
            elc.setActivo(true);
            elc = estadoLoteConfigRepository.save(elc);
            porNombre.put(nombreEstado, elc);
        }

        if (porNombre.isEmpty()) {
            throw new IllegalArgumentException("La propuesta no contiene estados válidos");
        }

        asegurarInicialYFinal(porNombre);

        Set<String> paresTransicion = new HashSet<>();
        for (WizardCultivoPropuestaDto.EstadoPropuesta ep : estadosOrdenados) {
            if (ep.getNombre() == null || ep.getNombre().isBlank()) {
                continue;
            }
            EstadoLoteConfig origen = porNombre.get(ep.getNombre().trim());
            if (origen == null) {
                continue;
            }
            List<String> destinos = Optional.ofNullable(ep.getTransicionesPermitidas()).orElse(List.of());
            if (destinos.isEmpty()) {
                continue;
            }
            for (String d : destinos) {
                if (d == null || d.isBlank()) {
                    continue;
                }
                EstadoLoteConfig destino = porNombre.get(d.trim());
                if (destino == null) {
                    continue;
                }
                String par = origen.getId() + "->" + destino.getId();
                if (paresTransicion.add(par)) {
                    TransicionEstadoConfig tr = new TransicionEstadoConfig();
                    tr.setTipoCultivo(tipo);
                    tr.setEmpresa(empresa);
                    tr.setEstadoOrigen(origen);
                    tr.setEstadoDestino(destino);
                    tr.setRequiereMotivo(false);
                    tr.setActivo(true);
                    transicionEstadoConfigRepository.save(tr);
                }
            }
        }

        if (transicionEstadoConfigRepository.findByTipoCultivoIdAndEmpresaIdAndActivoTrue(tipo.getId(), empresa.getId()).isEmpty()) {
            List<EstadoLoteConfig> listaOrdenada = porNombre.values().stream()
                    .sorted(Comparator.comparing(EstadoLoteConfig::getOrden))
                    .toList();
            crearCadenaLineal(tipo, empresa, listaOrdenada, paresTransicion);
        }

        int ordenTarea = 0;
        for (WizardCultivoPropuestaDto.EstadoPropuesta ep : estadosOrdenados) {
            if (ep.getNombre() == null) {
                continue;
            }
            EstadoLoteConfig estado = porNombre.get(ep.getNombre().trim());
            if (estado == null) {
                continue;
            }
            for (String tl : Optional.ofNullable(ep.getTareasHabilitadas()).orElse(List.of())) {
                if (tl == null || tl.isBlank()) {
                    continue;
                }
                String codigo = tl.trim().toUpperCase();
                if (!TIPOS_LABOR_VALIDOS.contains(codigo)) {
                    continue;
                }
                TareaPorEstadoConfig t = new TareaPorEstadoConfig();
                t.setTipoCultivo(tipo);
                t.setEmpresa(empresa);
                t.setEstado(estado);
                t.setTipoLabor(codigo);
                t.setNombreTarea(codigo + " " + estado.getNombre());
                t.setDescripcion("Sugerido por wizard");
                t.setEsObligatoria(false);
                t.setOrden(ordenTarea++);
                t.setActivo(true);
                tareaPorEstadoConfigRepository.save(t);
            }
        }

        for (WizardCultivoPropuestaDto.CalendarioLaborPropuesta cl : Optional.ofNullable(dto.getCalendarioLabores()).orElse(List.of())) {
            if (cl.getNombre() == null || cl.getTipoLabor() == null) {
                continue;
            }
            String codigo = cl.getTipoLabor().trim().toUpperCase();
            if (!TIPOS_LABOR_VALIDOS.contains(codigo)) {
                continue;
            }
            PlantillaLabor pl = new PlantillaLabor();
            pl.setEmpresa(empresa);
            pl.setTipoCultivo(tipo);
            pl.setNombre(cl.getNombre().trim());
            pl.setTipoLabor(codigo);
            pl.setDiaRelativoSiembra(Optional.ofNullable(cl.getDiaRelativoSiembra()).orElse(0));
            pl.setEstadoEsperadoNombre(cl.getEstadoEsperado());
            pl.setDuracionEstimadaHs(cl.getDuracionEstimadaHs());
            if (cl.getInsumosSugeridos() != null && !cl.getInsumosSugeridos().isEmpty()) {
                pl.setInsumosSugeridos(String.join(", ", cl.getInsumosSugeridos()));
            }
            pl.setActivo(true);
            plantillaLaborRepository.save(pl);
        }

        return tipo.getId();
    }

    /**
     * Persiste la propuesta del wizard (como {@link #confirmar}) y vincula el tipo de cultivo y el cultivo al lote,
     * asignando el estado configurado inicial según la plantilla creada.
     */
    @Transactional
    public long confirmarPropuestaYVincularAlLote(WizardCultivoPropuestaDto dto, Long empresaId, Long loteId, Long cultivoId) {
        long tipoCultivoId = confirmar(dto, empresaId);
        Plot lote = plotRepository.findByIdConCampoYEmpresa(loteId)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
        if (lote.getCampo() == null || lote.getCampo().getEmpresa() == null
                || !lote.getCampo().getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El lote no pertenece a la empresa indicada");
        }
        Cultivo cultivo = cultivoRepository.findById(cultivoId)
                .orElseThrow(() -> new IllegalArgumentException("Cultivo no encontrado"));
        if (cultivo.getEmpresa() == null || !cultivo.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El cultivo no pertenece a la empresa indicada");
        }
        TipoCultivo tipo = tipoCultivoRepository.findById(tipoCultivoId)
                .orElseThrow(() -> new IllegalStateException("Tipo de cultivo recién creado no encontrado"));
        lote.setTipoCultivo(tipo);
        lote.setCultivo(cultivo);
        List<EstadoLoteConfig> estados = estadoLoteConfigRepository
                .findByTipoCultivoIdAndEmpresaIdAndActivoTrueOrderByOrdenAsc(tipoCultivoId, empresaId);
        EstadoLoteConfig inicial = estados.stream()
                .filter(e -> Boolean.TRUE.equals(e.getEsEstadoInicial()))
                .findFirst()
                .orElse(estados.isEmpty() ? null : estados.get(0));
        lote.setEstadoConfigurado(inicial);
        plotRepository.save(lote);
        return tipoCultivoId;
    }

    private void crearCadenaLineal(TipoCultivo tipo, Empresa empresa, List<EstadoLoteConfig> lista, Set<String> pares) {
        for (int i = 0; i < lista.size() - 1; i++) {
            EstadoLoteConfig a = lista.get(i);
            EstadoLoteConfig b = lista.get(i + 1);
            String par = a.getId() + "->" + b.getId();
            if (pares.add(par)) {
                TransicionEstadoConfig tr = new TransicionEstadoConfig();
                tr.setTipoCultivo(tipo);
                tr.setEmpresa(empresa);
                tr.setEstadoOrigen(a);
                tr.setEstadoDestino(b);
                tr.setRequiereMotivo(false);
                tr.setActivo(true);
                transicionEstadoConfigRepository.save(tr);
            }
        }
    }

    private void asegurarInicialYFinal(Map<String, EstadoLoteConfig> porNombre) {
        long iniciales = porNombre.values().stream().filter(EstadoLoteConfig::getEsEstadoInicial).count();
        long finales = porNombre.values().stream().filter(EstadoLoteConfig::getEsEstadoFinal).count();
        List<EstadoLoteConfig> ordenados = porNombre.values().stream()
                .sorted(Comparator.comparing(EstadoLoteConfig::getOrden))
                .toList();
        if (iniciales != 1 || finales != 1) {
            for (EstadoLoteConfig e : porNombre.values()) {
                e.setEsEstadoInicial(false);
                e.setEsEstadoFinal(false);
            }
            if (!ordenados.isEmpty()) {
                ordenados.get(0).setEsEstadoInicial(true);
                ordenados.get(ordenados.size() - 1).setEsEstadoFinal(true);
                estadoLoteConfigRepository.saveAll(porNombre.values());
            }
        }
    }

    private String generarNombreTipoCultivoUnico(String nombreBase) {
        String candidato = nombreBase;
        int suf = 0;
        while (tipoCultivoRepository.findByNombre(candidato).isPresent()) {
            suf++;
            candidato = nombreBase + " (" + suf + ")";
        }
        return candidato;
    }

    private static String truncar(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
