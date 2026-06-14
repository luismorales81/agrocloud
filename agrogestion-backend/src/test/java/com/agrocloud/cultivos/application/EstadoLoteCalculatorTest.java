package com.agrocloud.cultivos.application;

import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.domain.HistorialCosecha;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.model.enums.EstadoLote;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios del calculador de estado del lote.
 * Valida las reglas de derivación en el orden de prioridad obligatorio.
 * No usa Spring ni base de datos.
 */
class EstadoLoteCalculatorTest {

    private static Plot loteVacio() {
        Plot p = new Plot();
        p.setFechaSiembra(null);
        return p;
    }

    private static Plot loteConFechaSiembra(int diasAtras) {
        Plot p = new Plot();
        p.setFechaSiembra(LocalDate.now().minusDays(diasAtras));
        return p;
    }

    private static Labor laborActiva(Labor.TipoLabor tipo) {
        Labor l = new Labor();
        l.setTipoLabor(tipo);
        l.setActivo(true);
        l.setEstado(Labor.EstadoLabor.COMPLETADA);
        l.setFechaInicio(LocalDate.now());
        return l;
    }

    private static Labor laborAbandono() {
        Labor l = new Labor();
        l.setTipoLabor(Labor.TipoLabor.OTROS);
        l.setDescripcion("Abandono de cultivo - sequía");
        l.setActivo(true);
        l.setEstado(Labor.EstadoLabor.COMPLETADA);
        return l;
    }

    @Nested
    @DisplayName("1) Lote sin labores ni historial → DISPONIBLE")
    class LoteSinLaboresNiHistorial {
        @Test
        void sinLaboresNiCosechaVigente_devuelveDISPONIBLE() {
            Plot lote = loteVacio();
            EstadoLote resultado = EstadoLoteCalculator.calcularEstado(lote, Collections.emptyList(), Optional.empty());
            assertThat(resultado).isEqualTo(EstadoLote.DISPONIBLE);
        }
    }

    @Nested
    @DisplayName("2) Crear Labor SIEMBRA → SEMBRADO")
    class LaborSiembra {
        @Test
        void conLaborSiembraActiva_sinDiasSuficientes_devuelveSEMBRADO() {
            Plot lote = loteConFechaSiembra(5);
            List<Labor> labores = List.of(laborActiva(Labor.TipoLabor.SIEMBRA));
            EstadoLote resultado = EstadoLoteCalculator.calcularEstado(lote, labores, Optional.empty());
            assertThat(resultado).isEqualTo(EstadoLote.SEMBRADO);
        }

        @Test
        void conLaborSiembraActiva_sinFechaSiembraEnLote_devuelveSEMBRADO() {
            Plot lote = loteVacio();
            List<Labor> labores = List.of(laborActiva(Labor.TipoLabor.SIEMBRA));
            EstadoLote resultado = EstadoLoteCalculator.calcularEstado(lote, labores, Optional.empty());
            assertThat(resultado).isEqualTo(EstadoLote.SEMBRADO);
        }
    }

    @Nested
    @DisplayName("3–5) Simular días: EN_CRECIMIENTO, EN_FLORACION, LISTO_PARA_COSECHA")
    class SimularDias {
        @Test
        @DisplayName("3) días >= 15 → EN_CRECIMIENTO")
        void diasMayorIgualCrecimiento_devuelveEN_CRECIMIENTO() {
            Plot lote = loteConFechaSiembra(20);
            List<Labor> labores = List.of(laborActiva(Labor.TipoLabor.SIEMBRA));
            EstadoLote resultado = EstadoLoteCalculator.calcularEstado(lote, labores, Optional.empty());
            assertThat(resultado).isEqualTo(EstadoLote.EN_CRECIMIENTO);
        }

        @Test
        @DisplayName("4) días >= 45 → EN_FLORACION")
        void diasMayorIgualFloracion_devuelveEN_FLORACION() {
            Plot lote = loteConFechaSiembra(50);
            List<Labor> labores = List.of(laborActiva(Labor.TipoLabor.SIEMBRA));
            EstadoLote resultado = EstadoLoteCalculator.calcularEstado(lote, labores, Optional.empty());
            assertThat(resultado).isEqualTo(EstadoLote.EN_FLORACION);
        }

        @Test
        @DisplayName("5) días >= 100 → LISTO_PARA_COSECHA")
        void diasMayorIgualParaCosecha_devuelveLISTO_PARA_COSECHA() {
            Plot lote = loteConFechaSiembra(110);
            List<Labor> labores = List.of(laborActiva(Labor.TipoLabor.SIEMBRA));
            EstadoLote resultado = EstadoLoteCalculator.calcularEstado(lote, labores, Optional.empty());
            assertThat(resultado).isEqualTo(EstadoLote.LISTO_PARA_COSECHA);
        }
    }

    @Nested
    @DisplayName("6) HistorialCosecha vigente → COSECHADO")
    class CosechaVigente {
        @Test
        void conCosechaVigente_devuelveCOSECHADO() {
            Plot lote = loteVacio();
            HistorialCosecha historial = new HistorialCosecha();
            EstadoLote resultado = EstadoLoteCalculator.calcularEstado(lote, Collections.emptyList(), Optional.of(historial));
            assertThat(resultado).isEqualTo(EstadoLote.COSECHADO);
        }
    }

    @Nested
    @DisplayName("8) Labor ABANDONO activa → ABANDONADO (prioridad sobre historial)")
    class AbandonoPrioritario {
        @Test
        void conLaborAbandonoActiva_devuelveABANDONADO() {
            Plot lote = loteVacio();
            List<Labor> labores = List.of(laborAbandono());
            EstadoLote resultado = EstadoLoteCalculator.calcularEstado(lote, labores, Optional.empty());
            assertThat(resultado).isEqualTo(EstadoLote.ABANDONADO);
        }

        @Test
        void conLaborAbandonoYHistorialVigente_prioridadAbandono_devuelveABANDONADO() {
            Plot lote = loteVacio();
            List<Labor> labores = List.of(laborAbandono());
            EstadoLote resultado = EstadoLoteCalculator.calcularEstado(lote, labores, Optional.of(new HistorialCosecha()));
            assertThat(resultado).isEqualTo(EstadoLote.ABANDONADO);
        }
    }

    @Nested
    @DisplayName("Labor COSECHA activa → EN_COSECHA")
    class LaborCosechaActiva {
        @Test
        void conLaborCosechaActiva_devuelveEN_COSECHA() {
            Plot lote = loteConFechaSiembra(110);
            List<Labor> labores = List.of(
                laborActiva(Labor.TipoLabor.SIEMBRA),
                laborActiva(Labor.TipoLabor.COSECHA)
            );
            EstadoLote resultado = EstadoLoteCalculator.calcularEstado(lote, labores, Optional.empty());
            assertThat(resultado).isEqualTo(EstadoLote.EN_COSECHA);
        }
    }

    @Nested
    @DisplayName("Labor cancelada o anulada no cuenta como activa")
    class LaborInactiva {
        @Test
        void laborSiembraCancelada_sinOtrasLabores_devuelveDISPONIBLE() {
            Plot lote = loteConFechaSiembra(10);
            Labor cancelada = laborActiva(Labor.TipoLabor.SIEMBRA);
            cancelada.setEstado(Labor.EstadoLabor.CANCELADA);
            List<Labor> labores = List.of(cancelada);
            EstadoLote resultado = EstadoLoteCalculator.calcularEstado(lote, labores, Optional.empty());
            assertThat(resultado).isEqualTo(EstadoLote.DISPONIBLE);
        }

        @Test
        void laborSiembraAnulada_sinOtrasLabores_devuelveDISPONIBLE() {
            Plot lote = loteVacio();
            Labor anulada = laborActiva(Labor.TipoLabor.SIEMBRA);
            anulada.setEstado(Labor.EstadoLabor.ANULADA);
            List<Labor> labores = List.of(anulada);
            EstadoLote resultado = EstadoLoteCalculator.calcularEstado(lote, labores, Optional.empty());
            assertThat(resultado).isEqualTo(EstadoLote.DISPONIBLE);
        }
    }
}
