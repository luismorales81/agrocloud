package com.agrocloud.porcinos.application;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.Recordatorio;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.core.infrastructure.RecordatorioRepository;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import com.agrocloud.porcinos.domain.PlanRecria;
import com.agrocloud.porcinos.domain.PlanRecriaRecetaSugerencia;
import com.agrocloud.porcinos.domain.PlanRecriaRecordatorioSugerido;
import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.domain.RecetaAlimentacionPorEtapa;
import com.agrocloud.porcinos.infrastructure.PlanRecriaEntityRepository;
import com.agrocloud.porcinos.infrastructure.RecetaAlimentacionPorEtapaRepository;
import com.agrocloud.porcinos.infrastructure.RecriaRepository;
import com.agrocloud.core.inventory.infrastructure.InsumoCompuestoRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ServicioWizardPlanRecriaAplicacion {

    private final EmpresaRepository empresaRepository;
    private final RecriaRepository recriaRepository;
    private final PlanRecriaEntityRepository planRecriaEntityRepository;
    private final InsumoCompuestoRepository insumoCompuestoRepository;
    private final RecetaAlimentacionPorEtapaRepository recetaAlimentacionPorEtapaRepository;
    private final RecordatorioRepository recordatorioRepository;

    public ServicioWizardPlanRecriaAplicacion(@Qualifier("empresaRepositoryCore") EmpresaRepository empresaRepository,
                                              RecriaRepository recriaRepository,
                                              PlanRecriaEntityRepository planRecriaEntityRepository,
                                              @Qualifier("insumoCompuestoRepositoryInventario") InsumoCompuestoRepository insumoCompuestoRepository,
                                              RecetaAlimentacionPorEtapaRepository recetaAlimentacionPorEtapaRepository,
                                              RecordatorioRepository recordatorioRepository) {
        this.empresaRepository = empresaRepository;
        this.recriaRepository = recriaRepository;
        this.planRecriaEntityRepository = planRecriaEntityRepository;
        this.insumoCompuestoRepository = insumoCompuestoRepository;
        this.recetaAlimentacionPorEtapaRepository = recetaAlimentacionPorEtapaRepository;
        this.recordatorioRepository = recordatorioRepository;
    }

    @Transactional
    public ResultadoAplicacion aplicarPlanARecria(Long recriaId, Long planRecriaId, Empresa empresa, User usuario) {
        Recria recria = recriaRepository.findByIdAndEmpresa(recriaId, empresa)
                .orElseThrow(() -> new IllegalArgumentException("Recría no encontrada"));
        PlanRecria plan = planRecriaEntityRepository.findByIdAndEmpresaAndActivoTrue(planRecriaId, empresa)
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));

        List<String> advertencias = new ArrayList<>();
        int recetasCreadas = 0;

        for (PlanRecriaRecetaSugerencia sug : plan.getRecetasSugeridas()) {
            Optional<InsumoCompuesto> ic = insumoCompuestoRepository.findFirstByEmpresaAndNombreIgnoreCaseAndActivoTrue(
                    empresa, sug.getNombreInsumoCompuesto());
            if (ic.isEmpty()) {
                advertencias.add("Sin insumo compuesto coincidente: " + sug.getNombreInsumoCompuesto());
                continue;
            }
            RecetaAlimentacionPorEtapa.EtapaAlimentacion etapa = parsearEtapa(sug.getEtapaCodigo());
            boolean existe = recetaAlimentacionPorEtapaRepository.findByEmpresaAndEtapaAndActivoTrue(empresa, etapa).stream()
                    .anyMatch(r -> r.getInsumoCompuesto().getId().equals(ic.get().getId()));
            if (existe) {
                advertencias.add("Receta ya existía para etapa " + etapa + " e insumo " + ic.get().getNombre());
                continue;
            }
            RecetaAlimentacionPorEtapa r = new RecetaAlimentacionPorEtapa(ic.get(), etapa, sug.getKgPorAnimalDia(), empresa);
            r.setObservaciones("Aplicado desde plan IA #" + plan.getId());
            recetaAlimentacionPorEtapaRepository.save(r);
            recetasCreadas++;
        }

        for (PlanRecriaRecordatorioSugerido rs : plan.getRecordatoriosSugeridos()) {
            String desc = rs.getDescripcion();
            String titulo = desc.length() > 200 ? desc.substring(0, 197) + "..." : desc;
            Recordatorio.TipoRecordatorio tipoRec = mapearTipoRecordatorio(rs.getTipo());
            Recordatorio rec = new Recordatorio(usuario, titulo,
                    recria.getFechaIngreso().plusDays(rs.getDiasDesdeIngreso()), tipoRec);
            rec.setDescripcion(desc);
            rec.setLoteId(recria.getLoteId());
            recordatorioRepository.save(rec);
        }

        return new ResultadoAplicacion(recetasCreadas, advertencias);
    }

    private static Recordatorio.TipoRecordatorio mapearTipoRecordatorio(String t) {
        if (t == null) {
            return Recordatorio.TipoRecordatorio.OTRO;
        }
        return switch (t.toUpperCase()) {
            case "CONTROL_PESO" -> Recordatorio.TipoRecordatorio.MANTENIMIENTO;
            case "REVISION_ETAPA", "FECHA_FAENA_ESTIMADA" -> Recordatorio.TipoRecordatorio.ALIMENTACION;
            default -> Recordatorio.TipoRecordatorio.OTRO;
        };
    }

    private static RecetaAlimentacionPorEtapa.EtapaAlimentacion parsearEtapa(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return RecetaAlimentacionPorEtapa.EtapaAlimentacion.F1;
        }
        try {
            return RecetaAlimentacionPorEtapa.EtapaAlimentacion.valueOf(codigo.trim().toUpperCase());
        } catch (Exception e) {
            return RecetaAlimentacionPorEtapa.EtapaAlimentacion.F1;
        }
    }

    public record ResultadoAplicacion(int recetasCreadas, List<String> advertencias) {}
}
