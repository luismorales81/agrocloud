package com.agrocloud.feedlot.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FeedlotAjustePlantelRespuesta {

    private Long id;
    private Long loteId;
    private Long empresaId;
    private Long usuarioId;
    private String usuarioNombre;
    private LocalDate fecha;
    private Integer cabezasAntes;
    private Integer cabezasDespues;
    private String motivo;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getCabezasAntes() {
        return cabezasAntes;
    }

    public void setCabezasAntes(Integer cabezasAntes) {
        this.cabezasAntes = cabezasAntes;
    }

    public Integer getCabezasDespues() {
        return cabezasDespues;
    }

    public void setCabezasDespues(Integer cabezasDespues) {
        this.cabezasDespues = cabezasDespues;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
