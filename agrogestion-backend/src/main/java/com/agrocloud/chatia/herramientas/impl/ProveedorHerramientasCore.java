package com.agrocloud.chatia.herramientas.impl;

import com.agrocloud.chatia.herramientas.*;
import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.application.CompanyModuleService;
import com.agrocloud.core.domain.Campana;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.dto.CompanyModuleDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProveedorHerramientasCore implements ProveedorHerramientasChatIa {

    private final EmpresaRepository empresaRepository;
    private final CompanyModuleService companyModuleService;
    private final CampanaContextService campanaContextService;

    public ProveedorHerramientasCore(
            EmpresaRepository empresaRepository,
            CompanyModuleService companyModuleService,
            @Qualifier("campanaContextServiceCore") CampanaContextService campanaContextService) {
        this.empresaRepository = empresaRepository;
        this.companyModuleService = companyModuleService;
        this.campanaContextService = campanaContextService;
    }

    @Override
    public List<HerramientaConsultaChatIa> obtenerHerramientas() {
        return List.of(
                new HerramientaConsultaChatIa(
                        "modulosActivos",
                        "CORE",
                        "Lista los módulos productivos contratados por la empresa activa.",
                        HerramientasUtil.esquemaVacio(),
                        (ctx, args) -> {
                            List<CompanyModuleDTO> modulos = companyModuleService.getEnabledModulesForCompany(ctx.getEmpresaId());
                            return modulos.stream()
                                    .filter(m -> Boolean.TRUE.equals(m.getEnabled()))
                                    .map(m -> Map.of(
                                            "codigo", RegistroHerramientasConsulta.normalizarCodigoModulo(m.getModuleCode()),
                                            "nombre", m.getModuleName() != null ? m.getModuleName() : m.getModuleCode()
                                    ))
                                    .collect(Collectors.toList());
                        }),
                new HerramientaConsultaChatIa(
                        "campanaActiva",
                        "CORE",
                        "Devuelve el período de gestión (campaña) activo.",
                        HerramientasUtil.esquemaVacio(),
                        (ctx, args) -> {
                            Campana campana = campanaContextService.resolverCampanaActiva(ctx.getEmpresaId());
                            Map<String, Object> info = new LinkedHashMap<>();
                            info.put("id", campana.getId());
                            info.put("nombre", campana.getNombre());
                            info.put("estado", campana.getEstado() != null ? campana.getEstado().name() : null);
                            info.put("fechaInicio", campana.getFechaInicio() != null ? campana.getFechaInicio().toString() : null);
                            info.put("fechaFin", campana.getFechaFin() != null ? campana.getFechaFin().toString() : null);
                            return info;
                        }),
                new HerramientaConsultaChatIa(
                        "resumenEmpresa",
                        "CORE",
                        "Resumen de la empresa activa del usuario.",
                        HerramientasUtil.esquemaVacio(),
                        (ctx, args) -> {
                            Empresa empresa = empresaRepository.findById(ctx.getEmpresaId())
                                    .orElseThrow(() -> new IllegalStateException("Empresa no encontrada"));
                            return Map.of(
                                    "id", empresa.getId(),
                                    "nombre", empresa.getNombre(),
                                    "moduloActivoUi", ctx.getModuloActivo() != null ? ctx.getModuloActivo() : ""
                            );
                        })
        );
    }
}
