package com.agrocloud.wizard;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.core.infrastructure.UserRepository;
import com.agrocloud.cultivos.application.ServicioWizardCultivoPersistencia;
import com.agrocloud.cultivos.domain.Field;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.domain.TipoCultivo;
import com.agrocloud.cultivos.infrastructure.EstadoLoteConfigRepository;
import com.agrocloud.cultivos.infrastructure.FieldRepository;
import com.agrocloud.cultivos.infrastructure.PlantillaLaborRepository;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import com.agrocloud.cultivos.infrastructure.TipoCultivoRepository;
import com.agrocloud.dto.wizard.WizardCultivoPropuestaDto;
import com.agrocloud.dto.wizard.WizardPlanRecriaPropuestaDto;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import com.agrocloud.porcinos.application.ServicioWizardPlanRecriaAplicacion;
import com.agrocloud.porcinos.application.ServicioWizardPlanRecriaPersistencia;
import com.agrocloud.porcinos.domain.PlanRecria;
import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.infrastructure.PlanRecriaEntityRepository;
import com.agrocloud.porcinos.infrastructure.RecetaAlimentacionPorEtapaRepository;
import com.agrocloud.porcinos.infrastructure.RecriaRepository;
import com.agrocloud.core.inventory.infrastructure.InsumoCompuestoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integración de persistencia y aplicación de wizards (sin llamada a Ollama).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class WizardIntegracionTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private ServicioWizardCultivoPersistencia servicioWizardCultivoPersistencia;
    @Autowired
    private ServicioWizardPlanRecriaPersistencia servicioWizardPlanRecriaPersistencia;
    @Autowired
    private ServicioWizardPlanRecriaAplicacion servicioWizardPlanRecriaAplicacion;
    @Autowired
    private TipoCultivoRepository tipoCultivoRepository;
    @Autowired
    private EstadoLoteConfigRepository estadoLoteConfigRepository;
    @Autowired
    private PlantillaLaborRepository plantillaLaborRepository;
    @Autowired
    private PlanRecriaEntityRepository planRecriaEntityRepository;
    @Autowired
    private RecriaRepository recriaRepository;
    @Autowired
    @Qualifier("insumoCompuestoRepositoryInventario")
        private InsumoCompuestoRepository insumoCompuestoRepository;
    @Autowired
    private RecetaAlimentacionPorEtapaRepository recetaAlimentacionPorEtapaRepository;
    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private PlotRepository plotRepository;

    private User usuario;
    private Empresa empresa;
    private Plot lote;

    @BeforeEach
    void setUp() {
        usuario = new User();
        usuario.setUsername("wiz-test-" + System.currentTimeMillis());
        usuario.setEmail("wizard-it-" + System.currentTimeMillis() + "@test.com");
        usuario.setPassword("password123");
        usuario.setFirstName("Test");
        usuario.setLastName("Wizard");
        usuario.setActivo(true);
        usuario = userRepository.save(usuario);

        empresa = new Empresa();
        empresa.setNombre("Empresa Wizard IT");
        empresa.setCuit("30-" + (System.currentTimeMillis() % 100000000) + "-9");
        empresa = empresaRepository.save(empresa);

        Field campo = new Field();
        campo.setNombre("Campo wizard IT");
        campo.setUbicacion("Test");
        campo.setAreaHectareas(BigDecimal.TEN);
        campo.setUser(usuario);
        campo.setEmpresa(empresa);
        campo.setActivo(true);
        campo.setEstado("ACTIVO");
        campo = fieldRepository.save(campo);

        lote = new Plot();
        lote.setNombre("Lote wizard IT");
        lote.setAreaHectareas(BigDecimal.ONE);
        lote.setUser(usuario);
        lote.setCampo(campo);
        lote.setActivo(true);
        lote.setLiberadoParaSiembra(false);
        lote = plotRepository.save(lote);
    }

    @Test
    void confirmar_wizard_cultivo_persiste_tipo_estados_y_plantilla() {
        String suf = String.valueOf(System.currentTimeMillis());
        WizardCultivoPropuestaDto dto = new WizardCultivoPropuestaDto();
        WizardCultivoPropuestaDto.TipoCultivoInfo tc = new WizardCultivoPropuestaDto.TipoCultivoInfo();
        tc.setNombre("Cultivo Wiz " + suf);
        tc.setDescripcion("IT");
        tc.setDuracionCicloDias(100);
        dto.setTipoCultivo(tc);

        WizardCultivoPropuestaDto.EstadoPropuesta e1 = new WizardCultivoPropuestaDto.EstadoPropuesta();
        e1.setNombre("Disponible");
        e1.setOrden(1);
        e1.setColor("#22c55e");
        e1.setEsEstadoInicial(true);
        e1.setEsEstadoFinal(false);
        e1.setTareasHabilitadas(List.of("SIEMBRA"));
        e1.setTransicionesPermitidas(List.of("En crecimiento"));

        WizardCultivoPropuestaDto.EstadoPropuesta e2 = new WizardCultivoPropuestaDto.EstadoPropuesta();
        e2.setNombre("En crecimiento");
        e2.setOrden(2);
        e2.setColor("#eab308");
        e2.setEsEstadoInicial(false);
        e2.setEsEstadoFinal(false);
        e2.setTareasHabilitadas(List.of("RIEGO"));
        e2.setTransicionesPermitidas(List.of("Cosechado"));

        WizardCultivoPropuestaDto.EstadoPropuesta e3 = new WizardCultivoPropuestaDto.EstadoPropuesta();
        e3.setNombre("Cosechado");
        e3.setOrden(3);
        e3.setColor("#64748b");
        e3.setEsEstadoInicial(false);
        e3.setEsEstadoFinal(true);
        e3.setTareasHabilitadas(List.of("COSECHA"));
        e3.setTransicionesPermitidas(List.of());
        dto.setEstados(List.of(e1, e2, e3));

        WizardCultivoPropuestaDto.CalendarioLaborPropuesta cl = new WizardCultivoPropuestaDto.CalendarioLaborPropuesta();
        cl.setNombre("Riego temprano");
        cl.setTipoLabor("RIEGO");
        cl.setDiaRelativoSiembra(30);
        cl.setEstadoEsperado("En crecimiento");
        cl.setDuracionEstimadaHs(4);
        cl.setInsumosSugeridos(List.of("Agua"));
        dto.setCalendarioLabores(List.of(cl));

        long tipoId = servicioWizardCultivoPersistencia.confirmar(dto, empresa.getId());

        TipoCultivo guardado = tipoCultivoRepository.findById(tipoId).orElseThrow();
        assertThat(guardado.getNombre()).contains("Cultivo Wiz");

        var estados = estadoLoteConfigRepository.findByTipoCultivoIdAndEmpresaIdAndActivoTrueOrderByOrdenAsc(tipoId, empresa.getId());
        assertThat(estados).hasSize(3);

        var plantillas = plantillaLaborRepository.findByEmpresaAndTipoCultivoAndActivoTrueOrderByDiaRelativoSiembraAsc(empresa, guardado);
        assertThat(plantillas).hasSize(1);
        assertThat(plantillas.get(0).getTipoLabor()).isEqualTo("RIEGO");
    }

    @Test
    void confirmar_plan_recria_y_aplicar_crea_receta_si_existe_insumo_compuesto() {
        String suf = String.valueOf(System.currentTimeMillis());
        String nombreRacion = "Ración IT " + suf;
        InsumoCompuesto ic = new InsumoCompuesto(nombreRacion, InsumoCompuesto.TipoInsumoCompuesto.RACION, empresa, usuario);
        ic.setUnidadMedida("kg");
        insumoCompuestoRepository.save(ic);

        WizardPlanRecriaPropuestaDto dto = new WizardPlanRecriaPropuestaDto();
        WizardPlanRecriaPropuestaDto.PlanRecriaInfo pr = new WizardPlanRecriaPropuestaDto.PlanRecriaInfo();
        pr.setNombre("Plan IT " + suf);
        pr.setDescripcion("Test integración");
        pr.setRazaObjetivo("Duroc");
        pr.setProposito("ENGORDE");
        dto.setPlanRecria(pr);

        WizardPlanRecriaPropuestaDto.EtapaPropuesta ep = new WizardPlanRecriaPropuestaDto.EtapaPropuesta();
        ep.setNombre("F1");
        ep.setOrden(1);
        ep.setDuracionEstimadaDias(40);
        ep.setPesoIngresoMinimoKg(new BigDecimal("20"));
        ep.setPesoObjetivoKg(new BigDecimal("110"));
        ep.setGananciaDiariaEsperadaKg(new BigDecimal("0.7"));
        ep.setUmbralMortalidadPct(new BigDecimal("3"));
        dto.setEtapas(List.of(ep));

        WizardPlanRecriaPropuestaDto.RecetaPorEtapaPropuesta rp = new WizardPlanRecriaPropuestaDto.RecetaPorEtapaPropuesta();
        rp.setEtapaNombre("F1");
        WizardPlanRecriaPropuestaDto.InsumoRecetaPropuesta ins = new WizardPlanRecriaPropuestaDto.InsumoRecetaPropuesta();
        ins.setNombreInsumo(nombreRacion);
        ins.setKgPorAnimalDia(new BigDecimal("2.5"));
        rp.setInsumos(List.of(ins));
        dto.setRecetasPorEtapa(List.of(rp));

        WizardPlanRecriaPropuestaDto.RecordatorioPropuesta rec = new WizardPlanRecriaPropuestaDto.RecordatorioPropuesta();
        rec.setTipo("CONTROL_PESO");
        rec.setDiasDesdeIngreso(14);
        rec.setDescripcion("Control de peso quincenal");
        dto.setRecordatorios(List.of(rec));

        long planId = servicioWizardPlanRecriaPersistencia.confirmar(dto, empresa.getId());
        PlanRecria plan = planRecriaEntityRepository.findById(planId).orElseThrow();
        assertThat(plan.getRecetasSugeridas()).hasSize(1);

        Recria recria = new Recria();
        recria.setLoteId(lote.getId());
        recria.setFechaIngreso(LocalDate.now());
        recria.setPesoPromedio(new BigDecimal("28"));
        recria.setPesoInicialKg(new BigDecimal("28"));
        recria.setCantidadAnimales(50);
        recria.setSexo(Recria.Sexo.MACHO);
        recria.setEtapa(Recria.EtapaRecria.F1);
        recria.setEmpresa(empresa);
        recria.setUsuario(usuario);
        recria.setActivo(true);
        recria.setOrigen(Recria.OrigenRecria.EXTERNO);
        recria = recriaRepository.save(recria);

        int antes = recetaAlimentacionPorEtapaRepository.findByEmpresaAndActivoTrue(empresa).size();
        var resultado = servicioWizardPlanRecriaAplicacion.aplicarPlanARecria(recria.getId(), planId, empresa, usuario);
        assertThat(resultado.recetasCreadas()).isEqualTo(1);
        int despues = recetaAlimentacionPorEtapaRepository.findByEmpresaAndActivoTrue(empresa).size();
        assertThat(despues).isGreaterThan(antes);
    }

    @Test
    void actualizar_plan_recria_persiste_cambios_en_mismo_registro() {
        String suf = String.valueOf(System.currentTimeMillis());
        WizardPlanRecriaPropuestaDto dto = new WizardPlanRecriaPropuestaDto();
        WizardPlanRecriaPropuestaDto.PlanRecriaInfo pr = new WizardPlanRecriaPropuestaDto.PlanRecriaInfo();
        pr.setNombre("Plan v1 " + suf);
        pr.setProposito("ENGORDE");
        dto.setPlanRecria(pr);
        WizardPlanRecriaPropuestaDto.EtapaPropuesta ep = new WizardPlanRecriaPropuestaDto.EtapaPropuesta();
        ep.setNombre("F1");
        ep.setOrden(1);
        dto.setEtapas(List.of(ep));

        long planId = servicioWizardPlanRecriaPersistencia.confirmar(dto, empresa.getId());
        dto.getPlanRecria().setNombre("Plan v2 " + suf);
        WizardPlanRecriaPropuestaDto.EtapaPropuesta ep2 = new WizardPlanRecriaPropuestaDto.EtapaPropuesta();
        ep2.setNombre("F2");
        ep2.setOrden(2);
        dto.setEtapas(List.of(ep, ep2));

        servicioWizardPlanRecriaPersistencia.actualizarPlan(planId, empresa.getId(), dto);

        var detalle = servicioWizardPlanRecriaPersistencia.obtenerParaEdicion(planId, empresa.getId());
        assertThat(detalle.getPropuesta().getPlanRecria().getNombre()).contains("Plan v2");
        assertThat(detalle.getPropuesta().getEtapas()).hasSize(2);
    }
}
