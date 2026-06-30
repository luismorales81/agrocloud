package com.agrocloud.lecheria.model.dto;

import java.time.LocalDateTime;

public class LecheriaImportControlLecheroRespuesta {

    private Long id;
    private String nombreArchivo;
    private Integer filasProcesadas;
    private Integer filasError;
    private String detalleErrores;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public Integer getFilasProcesadas() {
        return filasProcesadas;
    }

    public void setFilasProcesadas(Integer filasProcesadas) {
        this.filasProcesadas = filasProcesadas;
    }

    public Integer getFilasError() {
        return filasError;
    }

    public void setFilasError(Integer filasError) {
        this.filasError = filasError;
    }

    public String getDetalleErrores() {
        return detalleErrores;
    }

    public void setDetalleErrores(String detalleErrores) {
        this.detalleErrores = detalleErrores;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
