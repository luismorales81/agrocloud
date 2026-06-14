package com.agrocloud.cultivos.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro de envío de notificación derivada de una labor (spec SDD).
 * No es un recordatorio: solo evita duplicados (24h antes / mismo día).
 */
@Entity
@Table(name = "notificacion_labor_enviada",
       uniqueConstraints = @UniqueConstraint(columnNames = {"labor_id", "tipo", "fecha_envio"}))
@EntityListeners(AuditingEntityListener.class)
public class NotificacionLaborEnviada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "labor_id", nullable = false)
    private Long laborId;

    @Column(name = "tipo", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoNotificacion tipo;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDate fechaEnvio;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TipoNotificacion {
        /** Notificación 24 horas antes de fecha_planificada */
        H_24_ANTES,
        /** Notificación el mismo día de fecha_planificada */
        MISMO_DIA
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLaborId() { return laborId; }
    public void setLaborId(Long laborId) { this.laborId = laborId; }
    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }
    public LocalDate getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDate fechaEnvio) { this.fechaEnvio = fechaEnvio; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
