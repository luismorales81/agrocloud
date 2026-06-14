package com.agrocloud.cultivos.application;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.core.infrastructure.UserRepository;
import com.agrocloud.cultivos.domain.Cultivo;
import com.agrocloud.cultivos.domain.Field;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.infrastructure.CultivoRepository;
import com.agrocloud.cultivos.infrastructure.FieldRepository;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import com.agrocloud.dto.ConfirmacionCambioEstado;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.model.enums.EstadoLote;
import com.agrocloud.cultivos.infrastructure.HistorialCosechaRepository;
import com.agrocloud.cultivos.infrastructure.LaborRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de integración del modelo de estado derivado del módulo agrícola.
 * Usa servicios reales (EstadoLoteUpdater, LaborService, etc.) y EstadoLoteCalculator real.
 * Cada test es transaccional y hace rollback al final.
 *
 * No usar @EntityScan/@EnableJpaRepositories aquí: DatabaseConfig ya los declara y
 * duplicarlos provoca BeanDefinitionOverrideException.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class EstadoDerivadoIntegracionTest {

    @Autowired
    private PlotRepository plotRepository;
    @Autowired
    private LaborRepository laborRepository;
    @Autowired
    private HistorialCosechaRepository historialCosechaRepository;
    @Autowired
    private EstadoLoteUpdater estadoLoteUpdater;
    @Autowired
    private HistorialCosechaService historialCosechaService;
    @Autowired
    private EstadoLoteService estadoLoteService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private CultivoRepository cultivoRepository;

    private User usuario;
    private Empresa empresa;
    private Field campo;
    private Plot lote;

    @BeforeEach
    void setUp() {
        usuario = new User();
        usuario.setUsername("test-estado-" + System.currentTimeMillis());
        usuario.setEmail("test-estado@test.com");
        usuario.setPassword("password123");
        usuario.setFirstName("Test");
        usuario.setLastName("Usuario");
        usuario.setActivo(true);
        usuario = userRepository.save(usuario);

        empresa = new Empresa();
        empresa.setNombre("Empresa Test Estado");
        empresa.setCuit("30-12345678-9");
        empresa = empresaRepository.save(empresa);

        campo = new Field();
        campo.setNombre("Campo Test");
        campo.setUbicacion("Test");
        campo.setAreaHectareas(BigDecimal.TEN);
        campo.setUser(usuario);
        campo.setEmpresa(empresa);
        campo.setActivo(true);
        campo.setEstado("ACTIVO");
        campo = fieldRepository.save(campo);

        lote = new Plot();
        lote.setNombre("Lote Test Estado");
        lote.setAreaHectareas(BigDecimal.ONE);
        lote.setUser(usuario);
        lote.setCampo(campo);
        lote.setActivo(true);
        lote.setLiberadoParaSiembra(false);
        lote = plotRepository.save(lote);
    }

    @Nested
    @DisplayName("1) Lote sin labores ni historial → DISPONIBLE")
    class LoteSinLaboresNiHistorial {
        @Test
        void recalcularEstado_loteNuevo_devuelveDISPONIBLE() {
            estadoLoteUpdater.recalcularEstado(lote.getId());
            Plot actualizado = plotRepository.findById(lote.getId()).orElseThrow();
            assertThat(actualizado.getEstado()).isEqualTo(EstadoLote.DISPONIBLE);
        }
    }

    @Nested
    @DisplayName("2) Crear Labor SIEMBRA → estado SEMBRADO")
    class CrearLaborSiembra {
        @Test
        void despuesDeCrearLaborSiembra_recalcular_devuelveSEMBRADO() {
            Labor labor = new Labor();
            labor.setTipoLabor(Labor.TipoLabor.SIEMBRA);
            labor.setDescripcion("Siembra test");
            labor.setFechaInicio(LocalDate.now());
            labor.setFechaFin(LocalDate.now());
            labor.setEstado(Labor.EstadoLabor.COMPLETADA);
            labor.setLote(lote);
            labor.setUsuario(usuario);
            labor.setActivo(true);
            laborRepository.save(labor);
            lote.setFechaSiembra(LocalDate.now());
            plotRepository.save(lote);

            estadoLoteUpdater.recalcularEstado(lote.getId());
            Plot actualizado = plotRepository.findById(lote.getId()).orElseThrow();
            assertThat(actualizado.getEstado()).isEqualTo(EstadoLote.SEMBRADO);
        }
    }

    @Nested
    @DisplayName("3–5) Simular paso de días (fechaSiembra en el pasado)")
    class SimularDias {
        @Test
        @DisplayName("3) días >= 15 → EN_CRECIMIENTO")
        void fechaSiembraHace20Dias_conLaborSiembra_devuelveEN_CRECIMIENTO() {
            Labor labor = new Labor();
            labor.setTipoLabor(Labor.TipoLabor.SIEMBRA);
            labor.setDescripcion("Siembra");
            labor.setFechaInicio(LocalDate.now().minusDays(20));
            labor.setEstado(Labor.EstadoLabor.COMPLETADA);
            labor.setLote(lote);
            labor.setUsuario(usuario);
            labor.setActivo(true);
            laborRepository.save(labor);
            lote.setFechaSiembra(LocalDate.now().minusDays(20));
            plotRepository.save(lote);

            estadoLoteUpdater.recalcularEstado(lote.getId());
            Plot actualizado = plotRepository.findById(lote.getId()).orElseThrow();
            assertThat(actualizado.getEstado()).isEqualTo(EstadoLote.EN_CRECIMIENTO);
        }

        @Test
        @DisplayName("4) días >= 45 → EN_FLORACION")
        void fechaSiembraHace50Dias_conLaborSiembra_devuelveEN_FLORACION() {
            Labor labor = new Labor();
            labor.setTipoLabor(Labor.TipoLabor.SIEMBRA);
            labor.setDescripcion("Siembra");
            labor.setFechaInicio(LocalDate.now().minusDays(50));
            labor.setEstado(Labor.EstadoLabor.COMPLETADA);
            labor.setLote(lote);
            labor.setUsuario(usuario);
            labor.setActivo(true);
            laborRepository.save(labor);
            lote.setFechaSiembra(LocalDate.now().minusDays(50));
            plotRepository.save(lote);

            estadoLoteUpdater.recalcularEstado(lote.getId());
            Plot actualizado = plotRepository.findById(lote.getId()).orElseThrow();
            assertThat(actualizado.getEstado()).isEqualTo(EstadoLote.EN_FLORACION);
        }

        @Test
        @DisplayName("5) días >= 100 → LISTO_PARA_COSECHA")
        void fechaSiembraHace110Dias_conLaborSiembra_devuelveLISTO_PARA_COSECHA() {
            Labor labor = new Labor();
            labor.setTipoLabor(Labor.TipoLabor.SIEMBRA);
            labor.setDescripcion("Siembra");
            labor.setFechaInicio(LocalDate.now().minusDays(110));
            labor.setEstado(Labor.EstadoLabor.COMPLETADA);
            labor.setLote(lote);
            labor.setUsuario(usuario);
            labor.setActivo(true);
            laborRepository.save(labor);
            lote.setFechaSiembra(LocalDate.now().minusDays(110));
            plotRepository.save(lote);

            estadoLoteUpdater.recalcularEstado(lote.getId());
            Plot actualizado = plotRepository.findById(lote.getId()).orElseThrow();
            assertThat(actualizado.getEstado()).isEqualTo(EstadoLote.LISTO_PARA_COSECHA);
        }
    }

    @Nested
    @DisplayName("6) Ejecutar cosecha → COSECHADO")
    class EjecutarCosecha {
        @Test
        void conHistorialCosechaVigente_recalcular_devuelveCOSECHADO() {
            Cultivo cultivo = new Cultivo();
            cultivo.setNombre("Maíz");
            cultivo.setTipo("Maíz");
            cultivo.setVariedad("Test");
            cultivo.setEmpresa(empresa);
            cultivo.setUsuario(usuario);
            cultivo.setActivo(true);
            cultivo = cultivoRepository.save(cultivo);

            com.agrocloud.cultivos.domain.HistorialCosecha historial = new com.agrocloud.cultivos.domain.HistorialCosecha();
            historial.setLote(lote);
            historial.setCultivo(cultivo);
            historial.setFechaSiembra(LocalDate.now().minusDays(120));
            historial.setFechaCosecha(LocalDate.now());
            historial.setSuperficieHectareas(BigDecimal.ONE);
            historial.setCantidadCosechada(BigDecimal.valueOf(5000));
            historial.setUnidadCosecha("kg");
            historial.setUsuario(usuario);
            historialCosechaRepository.save(historial);
            lote.setLiberadoParaSiembra(false);
            plotRepository.save(lote);

            estadoLoteUpdater.recalcularEstado(lote.getId());
            Plot actualizado = plotRepository.findById(lote.getId()).orElseThrow();
            assertThat(actualizado.getEstado()).isEqualTo(EstadoLote.COSECHADO);
        }
    }

    @Nested
    @DisplayName("7) Liberar lote → DISPONIBLE")
    class LiberarLote {
        @Test
        void despuesDeLiberarLote_recalcular_devuelveDISPONIBLE() {
            lote.setLiberadoParaSiembra(true);
            plotRepository.save(lote);
            estadoLoteUpdater.recalcularEstado(lote.getId());
            Plot actualizado = plotRepository.findById(lote.getId()).orElseThrow();
            assertThat(actualizado.getEstado()).isEqualTo(EstadoLote.DISPONIBLE);
        }

        @Test
        void liberarLoteForzadamente_actualizaEstado() {
            historialCosechaService.liberarLoteForzadamente(lote.getId(), usuario, "Test");
            Plot actualizado = plotRepository.findById(lote.getId()).orElseThrow();
            assertThat(actualizado.getEstado()).isEqualTo(EstadoLote.DISPONIBLE);
        }
    }

    @Nested
    @DisplayName("8) Crear Labor ABANDONO → ABANDONADO incluso con historial")
    class LaborAbandono {
        @Test
        void conLaborAbandonoActiva_recalcular_devuelveABANDONADO() {
            Labor abandono = new Labor();
            abandono.setTipoLabor(Labor.TipoLabor.OTROS);
            abandono.setDescripcion("Abandono de cultivo - sequía");
            abandono.setObservaciones("CULTIVO ABANDONADO");
            abandono.setFechaInicio(LocalDate.now());
            abandono.setEstado(Labor.EstadoLabor.COMPLETADA);
            abandono.setLote(lote);
            abandono.setUsuario(usuario);
            abandono.setActivo(true);
            laborRepository.save(abandono);

            estadoLoteUpdater.recalcularEstado(lote.getId());
            Plot actualizado = plotRepository.findById(lote.getId()).orElseThrow();
            assertThat(actualizado.getEstado()).isEqualTo(EstadoLote.ABANDONADO);
        }
    }

    @Nested
    @DisplayName("9) Eliminar Labor SIEMBRA → DISPONIBLE")
    class EliminarLaborSiembra {
        @Test
        void despuesDeCancelarLaborSiembra_recalcular_devuelveDISPONIBLE() {
            Labor labor = new Labor();
            labor.setTipoLabor(Labor.TipoLabor.SIEMBRA);
            labor.setDescripcion("Siembra");
            labor.setFechaInicio(LocalDate.now());
            labor.setEstado(Labor.EstadoLabor.PLANIFICADA);
            labor.setLote(lote);
            labor.setUsuario(usuario);
            labor.setActivo(true);
            labor = laborRepository.save(labor);
            lote.setFechaSiembra(LocalDate.now());
            plotRepository.save(lote);
            estadoLoteUpdater.recalcularEstado(lote.getId());
            assertThat(plotRepository.findById(lote.getId()).orElseThrow().getEstado()).isEqualTo(EstadoLote.SEMBRADO);

            labor.setEstado(Labor.EstadoLabor.CANCELADA);
            labor.setActivo(false);
            laborRepository.save(labor);
            estadoLoteUpdater.recalcularEstado(lote.getId());
            Plot actualizado = plotRepository.findById(lote.getId()).orElseThrow();
            assertThat(actualizado.getEstado()).isEqualTo(EstadoLote.DISPONIBLE);
        }
    }

    @Nested
    @DisplayName("10) Intentar forzar estado SEMBRADO manualmente → debe fallar")
    class ForzarEstadoSembradoFalla {
        @Test
        @SuppressWarnings("deprecation")
        void confirmarCambioEstado_conSEMBRADO_lanzaExcepcion() {
            ConfirmacionCambioEstado confirmacion = new ConfirmacionCambioEstado();
            confirmacion.setLoteId(lote.getId());
            confirmacion.setEstadoActual(EstadoLote.DISPONIBLE);
            confirmacion.setEstadoPropuesto(EstadoLote.SEMBRADO);
            confirmacion.setMotivo("Test");
            confirmacion.setConfirmado(true);

            assertThatThrownBy(() -> estadoLoteService.aplicarCambioEstadoManual(confirmacion, usuario))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SEMBRADO")
                .hasMessageContaining("derivados");
        }

        @Test
        @SuppressWarnings("deprecation")
        void confirmarCambioEstado_conCOSECHADO_lanzaExcepcion() {
            ConfirmacionCambioEstado confirmacion = new ConfirmacionCambioEstado();
            confirmacion.setLoteId(lote.getId());
            confirmacion.setEstadoActual(EstadoLote.DISPONIBLE);
            confirmacion.setEstadoPropuesto(EstadoLote.COSECHADO);
            confirmacion.setMotivo("Test");
            confirmacion.setConfirmado(true);

            assertThatThrownBy(() -> estadoLoteService.aplicarCambioEstadoManual(confirmacion, usuario))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("COSECHADO");
        }
    }
}
