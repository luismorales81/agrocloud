package com.agrocloud.avicola.huevos.application;

import com.agrocloud.avicola.huevos.model.dto.AvicolaHuevosReporteAnalisisRespuesta;
import com.agrocloud.avicola.huevos.model.dto.AvicolaHuevosReporteResumenRespuesta;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoLote;
import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoProduccionDiaria;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoConsumoRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoLoteRepository;
import com.agrocloud.avicola.huevos.repository.AvicolaHuevoProduccionDiariaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioAvicolaHuevosReportes {

    private final AvicolaHuevoLoteRepository loteRepository;
    private final AvicolaHuevoProduccionDiariaRepository produccionRepository;
    private final AvicolaHuevoConsumoRepository consumoRepository;

    public ServicioAvicolaHuevosReportes(
            AvicolaHuevoLoteRepository loteRepository,
            AvicolaHuevoProduccionDiariaRepository produccionRepository,
            AvicolaHuevoConsumoRepository consumoRepository) {
        this.loteRepository = loteRepository;
        this.produccionRepository = produccionRepository;
        this.consumoRepository = consumoRepository;
    }

    @Transactional(readOnly = true)
    public AvicolaHuevosReporteResumenRespuesta resumenEmpresaEnRango(Long empresaId, LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            throw new IllegalArgumentException("Rango de fechas inválido");
        }
        AvicolaHuevosReporteResumenRespuesta salida = new AvicolaHuevosReporteResumenRespuesta();
        salida.setDesde(desde);
        salida.setHasta(hasta);
        Long totalEmpresa = produccionRepository.sumarTotalHuevosEmpresaEnRango(empresaId, desde, hasta);
        salida.setTotalHuevosRegistrados(totalEmpresa != null ? totalEmpresa : 0L);

        List<AvicolaHuevoLote> lotes = loteRepository.listarPorEmpresaId(empresaId);
        List<AvicolaHuevosReporteResumenRespuesta.FilaResumenLoteHuevos> filas = new ArrayList<>();
        for (AvicolaHuevoLote lote : lotes) {
            Long id = lote.getId();
            Long sumProd = produccionRepository.sumarTotalHuevosLoteEnRango(id, empresaId, desde, hasta);
            BigDecimal sumCons = consumoRepository.sumarConsumoLoteEnRango(id, empresaId, desde, hasta);
            Double temp = produccionRepository.promedioTemperaturaLoteEnRango(id, empresaId, desde, hasta);
            Double hum = produccionRepository.promedioHumedadLoteEnRango(id, empresaId, desde, hasta);
            if ((sumProd == null || sumProd == 0)
                    && (sumCons == null || sumCons.compareTo(BigDecimal.ZERO) == 0)
                    && temp == null
                    && hum == null) {
                continue;
            }
            AvicolaHuevosReporteResumenRespuesta.FilaResumenLoteHuevos f =
                    new AvicolaHuevosReporteResumenRespuesta.FilaResumenLoteHuevos();
            f.setLoteId(id);
            f.setNombreLote(lote.getNombre());
            f.setTotalHuevos(sumProd != null ? sumProd : 0L);
            f.setConsumoInsumoEnRango(sumCons != null ? sumCons : BigDecimal.ZERO);
            f.setTemperaturaPromedio(temp);
            f.setHumedadPromedio(hum);
            filas.add(f);
        }
        salida.setPorLote(filas);
        return salida;
    }

    /**
     * Serie diaria de postura (huevos, edad del lote, clima) y costos estimados de consumos (cantidad × precio unitario del insumo).
     */
    @Transactional(readOnly = true)
    public AvicolaHuevosReporteAnalisisRespuesta analisisPosturaLote(
            Long empresaId, Long loteId, LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            throw new IllegalArgumentException("Rango de fechas inválido");
        }
        AvicolaHuevoLote lote = loteRepository.findById(loteId)
                .filter(l -> l.getEmpresaId().equals(empresaId))
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));

        AvicolaHuevosReporteAnalisisRespuesta salida = new AvicolaHuevosReporteAnalisisRespuesta();
        salida.setLoteId(lote.getId());
        salida.setNombreLote(lote.getNombre());
        salida.setFechaInicioLote(lote.getFechaInicio());
        salida.setCantidadAvesActual(lote.getCantidadAvesActual());
        salida.setDesde(desde);
        salida.setHasta(hasta);

        BigDecimal totalCosto = consumoRepository.sumarCostoConsumosLoteEnRango(loteId, empresaId, desde, hasta);
        salida.setTotalCostoConsumosRango(totalCosto != null ? totalCosto : BigDecimal.ZERO);

        int aves = lote.getCantidadAvesActual() != null ? lote.getCantidadAvesActual() : 0;
        List<AvicolaHuevoProduccionDiaria> producciones =
                produccionRepository.listarPorLoteYEmpresaEnRango(loteId, empresaId, desde, hasta);
        List<AvicolaHuevosReporteAnalisisRespuesta.PuntoSerieDiaria> seriePostura = new ArrayList<>();
        for (AvicolaHuevoProduccionDiaria p : producciones) {
            AvicolaHuevosReporteAnalisisRespuesta.PuntoSerieDiaria punto =
                    new AvicolaHuevosReporteAnalisisRespuesta.PuntoSerieDiaria();
            punto.setFecha(p.getFecha());
            int total = p.getTotalHuevosDia() != null ? p.getTotalHuevosDia() : 0;
            punto.setTotalHuevos(total);
            punto.setTemperatura(p.getTemperaturaDia());
            punto.setHumedad(p.getHumedadDia());
            long diasEdad = ChronoUnit.DAYS.between(lote.getFechaInicio(), p.getFecha());
            punto.setDiasEdadLote(diasEdad);
            if (aves > 0 && total > 0) {
                punto.setHuevosPorAve(
                        BigDecimal.valueOf(total).divide(BigDecimal.valueOf(aves), 4, RoundingMode.HALF_UP));
            }
            seriePostura.add(punto);
        }
        salida.setSeriePostura(seriePostura);

        List<Object[]> filasCosto = consumoRepository.listarCostoConsumoPorDia(loteId, empresaId, desde, hasta);
        List<AvicolaHuevosReporteAnalisisRespuesta.PuntoGastoDia> serieGastos = new ArrayList<>();
        for (Object[] fila : filasCosto) {
            LocalDate fechaFila = aLocalDate(fila[0]);
            BigDecimal monto = fila[1] instanceof BigDecimal b ? b : BigDecimal.valueOf(((Number) fila[1]).doubleValue());
            AvicolaHuevosReporteAnalisisRespuesta.PuntoGastoDia g =
                    new AvicolaHuevosReporteAnalisisRespuesta.PuntoGastoDia();
            g.setFecha(fechaFila);
            g.setCostoEstimado(monto);
            serieGastos.add(g);
        }
        salida.setSerieGastos(serieGastos);
        return salida;
    }

    private static LocalDate aLocalDate(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof LocalDate ld) {
            return ld;
        }
        if (valor instanceof Date sd) {
            return sd.toLocalDate();
        }
        if (valor instanceof java.util.Date ud) {
            return new java.sql.Date(ud.getTime()).toLocalDate();
        }
        throw new IllegalArgumentException("Tipo de fecha no soportado: " + valor.getClass());
    }
}
