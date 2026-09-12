package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa una transferencia de lechones entre madres
 */
@Entity
@Table(name = "porcinos_legacy_transferencias_lechones")
@EntityListeners(AuditingEntityListener.class)
public class TransferenciaLechon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parto_origen_id", nullable = false)
    private Parto partoOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "madre_origen_id", nullable = false)
    private Madre madreOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parto_destino_id", nullable = false)
    private Parto partoDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "madre_destino_id", nullable = false)
    private Madre madreDestino;

    @Column(name = "fecha_transferencia", nullable = false)
    private LocalDate fechaTransferencia;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 1;

    @Column(name = "motivo", length = 200)
    private String motivo;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public TransferenciaLechon() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Parto getPartoOrigen() { return partoOrigen; }
    public void setPartoOrigen(Parto partoOrigen) { this.partoOrigen = partoOrigen; }
    public Madre getMadreOrigen() { return madreOrigen; }
    public void setMadreOrigen(Madre madreOrigen) { this.madreOrigen = madreOrigen; }
    public Parto getPartoDestino() { return partoDestino; }
    public void setPartoDestino(Parto partoDestino) { this.partoDestino = partoDestino; }
    public Madre getMadreDestino() { return madreDestino; }
    public void setMadreDestino(Madre madreDestino) { this.madreDestino = madreDestino; }
    public LocalDate getFechaTransferencia() { return fechaTransferencia; }
    public void setFechaTransferencia(LocalDate fechaTransferencia) { this.fechaTransferencia = fechaTransferencia; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
