package com.agrocloud.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado del recálculo masivo de estados de lotes.
 */
public class ResultadoRecalculoEstadosDTO {

    private int lotesProcesados;
    private int lotesActualizados;
    private int lotesConError;
    private LocalDateTime fechaEjecucion;
    private List<String> errores = new ArrayList<>();
    private List<Long> lotesCambiados = new ArrayList<>();

    public int getLotesProcesados() { return lotesProcesados; }
    public void setLotesProcesados(int lotesProcesados) { this.lotesProcesados = lotesProcesados; }
    public int getLotesActualizados() { return lotesActualizados; }
    public void setLotesActualizados(int lotesActualizados) { this.lotesActualizados = lotesActualizados; }
    public int getLotesConError() { return lotesConError; }
    public void setLotesConError(int lotesConError) { this.lotesConError = lotesConError; }
    public LocalDateTime getFechaEjecucion() { return fechaEjecucion; }
    public void setFechaEjecucion(LocalDateTime fechaEjecucion) { this.fechaEjecucion = fechaEjecucion; }
    public List<String> getErrores() { return errores; }
    public void setErrores(List<String> errores) { this.errores = errores; }
    public List<Long> getLotesCambiados() { return lotesCambiados; }
    public void setLotesCambiados(List<Long> lotesCambiados) { this.lotesCambiados = lotesCambiados; }
}
