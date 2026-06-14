package com.agrocloud.model.dto;

import com.agrocloud.porcinos.domain.TransferenciaLechon;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de TransferenciaLechon sin referencias circulares
 */
public class TransferenciaLechonDTO {
    private Long id;
    private Long partoOrigenId;
    private Long madreOrigenId;
    private Long partoDestinoId;
    private Long madreDestinoId;
    private LocalDate fechaTransferencia;
    private Integer cantidad;
    private String motivo;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    
    // Constructor por defecto
    public TransferenciaLechonDTO() {}
    
    // Constructor desde entidad
    public TransferenciaLechonDTO(TransferenciaLechon transferencia) {
        if (transferencia == null) {
            throw new IllegalArgumentException("La transferencia no puede ser null");
        }
        
        this.id = transferencia.getId();
        this.fechaTransferencia = transferencia.getFechaTransferencia();
        this.cantidad = transferencia.getCantidad();
        this.motivo = transferencia.getMotivo();
        this.observaciones = transferencia.getObservaciones();
        this.fechaCreacion = transferencia.getFechaCreacion();
        
        // Extraer IDs de las relaciones de forma segura
        try {
            if (transferencia.getPartoOrigen() != null) {
                this.partoOrigenId = transferencia.getPartoOrigen().getId();
            }
        } catch (org.hibernate.LazyInitializationException e) {
            // Si la relación no está cargada, no establecer el ID
        }
        
        try {
            if (transferencia.getMadreOrigen() != null) {
                this.madreOrigenId = transferencia.getMadreOrigen().getId();
            }
        } catch (org.hibernate.LazyInitializationException e) {
            // Si la relación no está cargada, no establecer el ID
        }
        
        try {
            if (transferencia.getPartoDestino() != null) {
                this.partoDestinoId = transferencia.getPartoDestino().getId();
            }
        } catch (org.hibernate.LazyInitializationException e) {
            // Si la relación no está cargada, no establecer el ID
        }
        
        try {
            if (transferencia.getMadreDestino() != null) {
                this.madreDestinoId = transferencia.getMadreDestino().getId();
            }
        } catch (org.hibernate.LazyInitializationException e) {
            // Si la relación no está cargada, no establecer el ID
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getPartoOrigenId() {
        return partoOrigenId;
    }
    
    public void setPartoOrigenId(Long partoOrigenId) {
        this.partoOrigenId = partoOrigenId;
    }
    
    public Long getMadreOrigenId() {
        return madreOrigenId;
    }
    
    public void setMadreOrigenId(Long madreOrigenId) {
        this.madreOrigenId = madreOrigenId;
    }
    
    public Long getPartoDestinoId() {
        return partoDestinoId;
    }
    
    public void setPartoDestinoId(Long partoDestinoId) {
        this.partoDestinoId = partoDestinoId;
    }
    
    public Long getMadreDestinoId() {
        return madreDestinoId;
    }
    
    public void setMadreDestinoId(Long madreDestinoId) {
        this.madreDestinoId = madreDestinoId;
    }
    
    public LocalDate getFechaTransferencia() {
        return fechaTransferencia;
    }
    
    public void setFechaTransferencia(LocalDate fechaTransferencia) {
        this.fechaTransferencia = fechaTransferencia;
    }
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
