package com.agrocloud.avicola.crianza.application;

import com.agrocloud.avicola.crianza.model.dto.AvicolaCrianzaCurvaPesoRespuesta;
import com.agrocloud.avicola.crianza.model.dto.AvicolaCrianzaReporteResumenRespuesta;
import com.agrocloud.avicola.crianza.model.dto.AvicolaCrianzaResumenRespuesta;
import com.agrocloud.avicola.crianza.model.entity.AvicolaLote;
import com.agrocloud.avicola.crianza.model.entity.AvicolaPesada;
import com.agrocloud.avicola.crianza.model.enums.AvicolaLoteEstado;
import com.agrocloud.avicola.crianza.model.enums.AvicolaModuloOrigen;
import com.agrocloud.avicola.crianza.repository.AvicolaLoteRepository;
import com.agrocloud.avicola.crianza.repository.AvicolaPesadaRepository;
import com.agrocloud.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ServicioAvicolaCrianzaReportes {

    private static final AvicolaModuloOrigen MODULO = AvicolaModuloOrigen.AVICOLA_CRIANZA;

    private final AvicolaLoteRepository loteRepository;
    private final AvicolaPesadaRepository pesadaRepository;
    private final ServicioAvicolaCrianzaOperaciones servicioOperaciones;

    public ServicioAvicolaCrianzaReportes(
            AvicolaLoteRepository loteRepository,
            AvicolaPesadaRepository pesadaRepository,
            ServicioAvicolaCrianzaOperaciones servicioOperaciones) {
        this.loteRepository = loteRepository;
        this.pesadaRepository = pesadaRepository;
        this.servicioOperaciones = servicioOperaciones;
    }

    @Transactional(readOnly = true)
    public AvicolaCrianzaReporteResumenRespuesta resumenEmpresa(Long empresaId) {
        List<AvicolaLote> lotes = loteRepository.listarPorEmpresaIdYModulo(empresaId, MODULO);

        AvicolaCrianzaReporteResumenRespuesta salida = new AvicolaCrianzaReporteResumenRespuesta();
        List<AvicolaCrianzaReporteResumenRespuesta.FilaLote> filas = new ArrayList<>();
        int activos = 0;
        int cerrados = 0;
        BigDecimal sumaMortalidad = BigDecimal.ZERO;
        int cuentaMortalidad = 0;
        BigDecimal sumaConversion = BigDecimal.ZERO;
        int cuentaConversion = 0;

        for (AvicolaLote lote : lotes) {
            if (lote.getEstado() == AvicolaLoteEstado.ACTIVO) {
                activos++;
            } else {
                cerrados++;
            }
            AvicolaCrianzaResumenRespuesta kpi = servicioOperaciones.resumenLote(empresaId, lote.getId());
            AvicolaCrianzaReporteResumenRespuesta.FilaLote f = new AvicolaCrianzaReporteResumenRespuesta.FilaLote();
            f.setLoteId(lote.getId());
            f.setNombreLote(lote.getNombre());
            f.setEstado(lote.getEstado() != null ? lote.getEstado().name() : null);
            f.setCantidadDisponible(kpi.getCantidadDisponible());
            f.setMortalidadPct(kpi.getMortalidadPorcentaje());
            f.setConversionAlimenticia(kpi.getConversionAlimenticia());
            f.setDiasEnProduccion(kpi.getDiasEnProduccion());
            filas.add(f);

            if (kpi.getMortalidadPorcentaje() != null) {
                sumaMortalidad = sumaMortalidad.add(kpi.getMortalidadPorcentaje());
                cuentaMortalidad++;
            }
            if (kpi.getConversionAlimenticia() != null) {
                sumaConversion = sumaConversion.add(kpi.getConversionAlimenticia());
                cuentaConversion++;
            }
        }

        salida.setLotesActivos(activos);
        salida.setLotesCerrados(cerrados);
        if (cuentaMortalidad > 0) {
            salida.setMortalidadPromedioPct(
                    sumaMortalidad.divide(BigDecimal.valueOf(cuentaMortalidad), 4, RoundingMode.HALF_UP));
        } else {
            salida.setMortalidadPromedioPct(BigDecimal.ZERO);
        }
        if (cuentaConversion > 0) {
            salida.setConversionPromedio(
                    sumaConversion.divide(BigDecimal.valueOf(cuentaConversion), 4, RoundingMode.HALF_UP));
        }
        salida.setPorLote(filas);
        return salida;
    }

    @Transactional(readOnly = true)
    public AvicolaCrianzaReporteResumenRespuesta analisisLotes(Long empresaId) {
        return resumenEmpresa(empresaId);
    }

    @Transactional(readOnly = true)
    public AvicolaCrianzaCurvaPesoRespuesta curvaPesoLote(Long empresaId, Long loteId) {
        AvicolaLote lote = loteRepository.buscarPorIdYEmpresaIdYModulo(loteId, empresaId, MODULO)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));

        AvicolaCrianzaCurvaPesoRespuesta salida = new AvicolaCrianzaCurvaPesoRespuesta();
        salida.setLoteId(lote.getId());
        salida.setNombreLote(lote.getNombre());
        salida.setFechaIngreso(lote.getFechaIngreso());
        salida.setPesoPromedioIngreso(lote.getPesoPromedioIngreso());

        List<AvicolaPesada> pesadas = pesadaRepository.listarPorLoteIdYEmpresaId(loteId, empresaId);
        pesadas.sort(Comparator.comparing(AvicolaPesada::getFecha));
        List<AvicolaCrianzaCurvaPesoRespuesta.PuntoPesada> serie = new ArrayList<>();
        for (AvicolaPesada p : pesadas) {
            AvicolaCrianzaCurvaPesoRespuesta.PuntoPesada punto = new AvicolaCrianzaCurvaPesoRespuesta.PuntoPesada();
            punto.setFecha(p.getFecha());
            punto.setPesoPromedio(p.getPesoPromedio());
            punto.setCantidadPesada(p.getCantidadPesada());
            if (lote.getFechaIngreso() != null && p.getFecha() != null) {
                punto.setDiasDesdeIngreso(ChronoUnit.DAYS.between(lote.getFechaIngreso(), p.getFecha()));
            }
            serie.add(punto);
        }
        salida.setSerie(serie);
        return salida;
    }
}
