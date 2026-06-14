package com.agrocloud.porcinos.application;



import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.infrastructure.EmpresaRepository;

import com.agrocloud.dto.porcinos.PlanRecriaEdicionRespuestaDto;

import com.agrocloud.dto.porcinos.PlanRecriaResumenDto;

import com.agrocloud.dto.wizard.WizardPlanRecriaPropuestaDto;

import com.agrocloud.porcinos.domain.*;

import com.agrocloud.porcinos.infrastructure.PlanRecriaEntityRepository;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.Comparator;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.Optional;

import java.util.stream.Collectors;



@Service

public class ServicioWizardPlanRecriaPersistencia {



    private final EmpresaRepository empresaRepository;

    private final PlanRecriaEntityRepository planRecriaEntityRepository;



    public ServicioWizardPlanRecriaPersistencia(EmpresaRepository empresaRepository,

                                                PlanRecriaEntityRepository planRecriaEntityRepository) {

        this.empresaRepository = empresaRepository;

        this.planRecriaEntityRepository = planRecriaEntityRepository;

    }



    @Transactional

    public long confirmar(WizardPlanRecriaPropuestaDto dto, Long empresaId) {

        validarNombrePlan(dto);

        Empresa empresa = empresaRepository.findById(empresaId)

                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));



        PlanRecria plan = new PlanRecria();

        plan.setEmpresa(empresa);

        plan.setActivo(true);

        plan.setFechaCreacion(LocalDateTime.now());

        poblarPlanDesdeDto(plan, dto);

        plan = planRecriaEntityRepository.save(plan);

        return plan.getId();

    }



    @Transactional(readOnly = true)

    public List<PlanRecriaResumenDto> listarResumenes(Long empresaId) {

        Empresa empresa = empresaRepository.findById(empresaId)

                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        return planRecriaEntityRepository.findByEmpresaAndActivoTrueOrderByNombreAsc(empresa).stream()

                .map(this::aResumenDto)

                .collect(Collectors.toList());

    }



    @Transactional(readOnly = true)

    public PlanRecriaEdicionRespuestaDto obtenerParaEdicion(long planId, Long empresaId) {

        Empresa empresa = empresaRepository.findById(empresaId)

                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        PlanRecria plan = planRecriaEntityRepository.findByIdAndEmpresaAndActivoTrue(planId, empresa)

                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));

        // Inicializar colecciones en la misma transacción

        plan.getEtapas().size();

        plan.getRecetasSugeridas().size();

        plan.getRecordatoriosSugeridos().size();

        PlanRecriaEdicionRespuestaDto out = new PlanRecriaEdicionRespuestaDto();

        out.setId(plan.getId());

        out.setPropuesta(planRecriaAPropuestaDto(plan));

        return out;

    }



    @Transactional

    public void actualizarPlan(long planId, Long empresaId, WizardPlanRecriaPropuestaDto dto) {

        validarNombrePlan(dto);

        Empresa empresa = empresaRepository.findById(empresaId)

                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        PlanRecria plan = planRecriaEntityRepository.findByIdAndEmpresaAndActivoTrue(planId, empresa)

                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));

        poblarPlanDesdeDto(plan, dto);

        plan.setFechaActualizacion(LocalDateTime.now());

        planRecriaEntityRepository.save(plan);

    }



    @Transactional

    public void desactivarPlan(long planId, Long empresaId) {

        Empresa empresa = empresaRepository.findById(empresaId)

                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        PlanRecria plan = planRecriaEntityRepository.findByIdAndEmpresaAndActivoTrue(planId, empresa)

                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));

        plan.setActivo(false);

        plan.setFechaActualizacion(LocalDateTime.now());

        planRecriaEntityRepository.save(plan);

    }



    private void validarNombrePlan(WizardPlanRecriaPropuestaDto dto) {

        if (dto == null || dto.getPlanRecria() == null || dto.getPlanRecria().getNombre() == null

                || dto.getPlanRecria().getNombre().isBlank()) {

            throw new IllegalArgumentException("Nombre del plan obligatorio");

        }

    }



    private PlanRecriaResumenDto aResumenDto(PlanRecria plan) {

        PlanRecriaResumenDto r = new PlanRecriaResumenDto();

        r.setId(plan.getId());

        r.setNombre(plan.getNombre());

        r.setDescripcion(plan.getDescripcion());

        r.setRazaObjetivo(plan.getRazaObjetivo());

        r.setProposito(plan.getProposito() != null ? plan.getProposito().name() : null);

        r.setFechaCreacion(plan.getFechaCreacion());

        return r;

    }



    public WizardPlanRecriaPropuestaDto planRecriaAPropuestaDto(PlanRecria plan) {

        WizardPlanRecriaPropuestaDto dto = new WizardPlanRecriaPropuestaDto();

        WizardPlanRecriaPropuestaDto.PlanRecriaInfo pr = new WizardPlanRecriaPropuestaDto.PlanRecriaInfo();

        pr.setNombre(plan.getNombre());

        pr.setDescripcion(plan.getDescripcion());

        pr.setRazaObjetivo(plan.getRazaObjetivo());

        pr.setProposito(plan.getProposito() != null ? plan.getProposito().name() : "ENGORDE");

        dto.setPlanRecria(pr);



        List<WizardPlanRecriaPropuestaDto.EtapaPropuesta> etapas = plan.getEtapas().stream()

                .sorted(Comparator.comparingInt(e -> Optional.ofNullable(e.getOrden()).orElse(0)))

                .map(e -> {

                    WizardPlanRecriaPropuestaDto.EtapaPropuesta ep = new WizardPlanRecriaPropuestaDto.EtapaPropuesta();

                    ep.setNombre(e.getCodigoEtapa());

                    ep.setOrden(e.getOrden());

                    ep.setDuracionEstimadaDias(e.getDuracionEstimadaDias());

                    ep.setPesoIngresoMinimoKg(e.getPesoIngresoMinimoKg());

                    ep.setPesoObjetivoKg(e.getPesoObjetivoKg());

                    ep.setGananciaDiariaEsperadaKg(e.getGananciaDiariaEsperadaKg());

                    ep.setUmbralMortalidadPct(e.getUmbralMortalidadPct());

                    return ep;

                })

                .collect(Collectors.toList());

        dto.setEtapas(etapas);



        Map<String, List<PlanRecriaRecetaSugerencia>> porEtapa = plan.getRecetasSugeridas().stream()

                .collect(Collectors.groupingBy(PlanRecriaRecetaSugerencia::getEtapaCodigo, LinkedHashMap::new, Collectors.toList()));

        List<WizardPlanRecriaPropuestaDto.RecetaPorEtapaPropuesta> recetasPorEtapa = new ArrayList<>();

        for (Map.Entry<String, List<PlanRecriaRecetaSugerencia>> en : porEtapa.entrySet()) {

            WizardPlanRecriaPropuestaDto.RecetaPorEtapaPropuesta rp = new WizardPlanRecriaPropuestaDto.RecetaPorEtapaPropuesta();

            rp.setEtapaNombre(en.getKey());

            List<WizardPlanRecriaPropuestaDto.InsumoRecetaPropuesta> insumos = new ArrayList<>();

            for (PlanRecriaRecetaSugerencia s : en.getValue()) {

                WizardPlanRecriaPropuestaDto.InsumoRecetaPropuesta ins = new WizardPlanRecriaPropuestaDto.InsumoRecetaPropuesta();

                ins.setNombreInsumo(s.getNombreInsumoCompuesto());

                ins.setKgPorAnimalDia(s.getKgPorAnimalDia());

                insumos.add(ins);

            }

            rp.setInsumos(insumos);

            recetasPorEtapa.add(rp);

        }

        dto.setRecetasPorEtapa(recetasPorEtapa);



        List<WizardPlanRecriaPropuestaDto.RecordatorioPropuesta> recordatorios = plan.getRecordatoriosSugeridos().stream()

                .map(rs -> {

                    WizardPlanRecriaPropuestaDto.RecordatorioPropuesta rec = new WizardPlanRecriaPropuestaDto.RecordatorioPropuesta();

                    rec.setTipo(rs.getTipo());

                    rec.setDiasDesdeIngreso(rs.getDiasDesdeIngreso());

                    rec.setDescripcion(rs.getDescripcion());

                    return rec;

                })

                .collect(Collectors.toList());

        dto.setRecordatorios(recordatorios);

        return dto;

    }



    private void poblarPlanDesdeDto(PlanRecria plan, WizardPlanRecriaPropuestaDto dto) {

        plan.setNombre(dto.getPlanRecria().getNombre().trim());

        plan.setDescripcion(dto.getPlanRecria().getDescripcion());

        plan.setRazaObjetivo(dto.getPlanRecria().getRazaObjetivo());

        plan.setProposito(parsearProposito(dto.getPlanRecria().getProposito()));



        plan.getEtapas().clear();

        plan.getRecetasSugeridas().clear();

        plan.getRecordatoriosSugeridos().clear();



        for (WizardPlanRecriaPropuestaDto.EtapaPropuesta ep : Optional.ofNullable(dto.getEtapas()).orElse(List.of())) {

            if (ep.getNombre() == null || ep.getNombre().isBlank()) {

                continue;

            }

            PlanRecriaEtapa e = new PlanRecriaEtapa();

            e.setPlanRecria(plan);

            e.setOrden(Optional.ofNullable(ep.getOrden()).orElse(0));

            e.setCodigoEtapa(ep.getNombre().trim().toUpperCase());

            e.setDuracionEstimadaDias(ep.getDuracionEstimadaDias());

            e.setPesoIngresoMinimoKg(ep.getPesoIngresoMinimoKg());

            e.setPesoObjetivoKg(ep.getPesoObjetivoKg());

            e.setGananciaDiariaEsperadaKg(ep.getGananciaDiariaEsperadaKg());

            e.setUmbralMortalidadPct(ep.getUmbralMortalidadPct());

            plan.getEtapas().add(e);

        }



        for (WizardPlanRecriaPropuestaDto.RecetaPorEtapaPropuesta rp : Optional.ofNullable(dto.getRecetasPorEtapa()).orElse(List.of())) {

            if (rp.getEtapaNombre() == null) {

                continue;

            }

            String etapa = rp.getEtapaNombre().trim().toUpperCase();

            for (WizardPlanRecriaPropuestaDto.InsumoRecetaPropuesta ins : Optional.ofNullable(rp.getInsumos()).orElse(List.of())) {

                if (ins.getNombreInsumo() == null || ins.getKgPorAnimalDia() == null) {

                    continue;

                }

                PlanRecriaRecetaSugerencia s = new PlanRecriaRecetaSugerencia();

                s.setPlanRecria(plan);

                s.setEtapaCodigo(etapa);

                s.setNombreInsumoCompuesto(ins.getNombreInsumo().trim());

                s.setKgPorAnimalDia(ins.getKgPorAnimalDia());

                plan.getRecetasSugeridas().add(s);

            }

        }



        for (WizardPlanRecriaPropuestaDto.RecordatorioPropuesta rec : Optional.ofNullable(dto.getRecordatorios()).orElse(List.of())) {

            if (rec.getDescripcion() == null || rec.getDiasDesdeIngreso() == null) {

                continue;

            }

            PlanRecriaRecordatorioSugerido r = new PlanRecriaRecordatorioSugerido();

            r.setPlanRecria(plan);

            r.setTipo(Optional.ofNullable(rec.getTipo()).orElse("OTRO"));

            r.setDiasDesdeIngreso(rec.getDiasDesdeIngreso());

            r.setDescripcion(rec.getDescripcion());

            plan.getRecordatoriosSugeridos().add(r);

        }

    }



    private static PlanRecria.PropositoPlan parsearProposito(String p) {

        if (p == null) {

            return PlanRecria.PropositoPlan.ENGORDE;

        }

        try {

            return PlanRecria.PropositoPlan.valueOf(p.trim().toUpperCase());

        } catch (Exception e) {

            return PlanRecria.PropositoPlan.ENGORDE;

        }

    }

}

