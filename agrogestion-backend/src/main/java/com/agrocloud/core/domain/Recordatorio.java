package com.agrocloud.core.domain;
import com.agrocloud.core.domain.User;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un recordatorio personal de un usuario.
 */
@Entity
@Table(name = "recordatorios")
@EntityListeners(AuditingEntityListener.class)
public class Recordatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    @JsonIgnoreProperties({"password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    private User usuario;

    @NotBlank
    @Size(max = 200)
    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Size(max = 1000)
    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoRecordatorio tipo = TipoRecordatorio.GENERAL;

    @Column(name = "labor_id")
    private Long laborId;

    @Column(name = "lote_id")
    private Long loteId;

    @Column(name = "servicio_id")
    private Long servicioId;

    @Column(name = "gestacion_id")
    private Long gestacionId;

    @Column(name = "parto_id")
    private Long partoId;

    @Column(name = "madre_id")
    private Long madreId;

    @Column(name = "lote_avicola_huevo_id")
    private Long loteAvicolaHuevoId;

    @Column(name = "completado", nullable = false)
    private Boolean completado = false;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public enum TipoRecordatorio {
        GENERAL, LABOR, COSECHA, MANTENIMIENTO, INSUMO, REUNION, PARTO, REPRODUCCION, SANIDAD, ALIMENTACION, OTRO
    }

    public Recordatorio() {}

    public Recordatorio(User usuario, String titulo, LocalDate fecha, TipoRecordatorio tipo) {
        this.usuario = usuario;
        this.titulo = titulo;
        this.fecha = fecha;
        this.tipo = tipo;
        this.completado = false;
        this.activo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public TipoRecordatorio getTipo() { return tipo; }
    public void setTipo(TipoRecordatorio tipo) { this.tipo = tipo; }
    public Long getLaborId() { return laborId; }
    public void setLaborId(Long laborId) { this.laborId = laborId; }
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public Boolean getCompletado() { return completado; }
    public void setCompletado(Boolean completado) { this.completado = completado; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Long getServicioId() { return servicioId; }
    public void setServicioId(Long servicioId) { this.servicioId = servicioId; }
    public Long getGestacionId() { return gestacionId; }
    public void setGestacionId(Long gestacionId) { this.gestacionId = gestacionId; }
    public Long getPartoId() { return partoId; }
    public void setPartoId(Long partoId) { this.partoId = partoId; }
    public Long getMadreId() { return madreId; }
    public void setMadreId(Long madreId) { this.madreId = madreId; }
    public Long getLoteAvicolaHuevoId() { return loteAvicolaHuevoId; }
    public void setLoteAvicolaHuevoId(Long loteAvicolaHuevoId) { this.loteAvicolaHuevoId = loteAvicolaHuevoId; }
}
