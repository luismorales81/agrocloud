package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.inventory.domain.Insumo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un evento sanitario (tratamiento, vacuna, etc.)
 * Puede estar asociado a una Madre, Padrillo o Lote (Recria)
 */
@Entity
@Table(name = "porcinos_legacy_eventos_sanitarios")
@EntityListeners(AuditingEntityListener.class)
public class EventoSanitario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_evento_sanitario_id", nullable = false)
    @JsonIgnoreProperties({"empresa"})
    private TipoEventoSanitario tipoEventoSanitario;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entidad", nullable = false)
    private TipoEntidad tipoEntidad;

    @Column(name = "entidad_id", nullable = false)
    private Long entidadId;

    @Column(name = "dosis", precision = 10, scale = 2)
    private java.math.BigDecimal dosis;

    @Column(name = "unidad_dosis", length = 50)
    private String unidadDosis = "ml";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_id")
    @JsonIgnoreProperties({"user", "empresa"})
    private Insumo insumo;

    @Column(name = "lote_medicamento", length = 100)
    private String loteMedicamento;

    @Column(name = "profesional_responsable", length = 200)
    private String profesionalResponsable;

    @Column(name = "fecha_retiro")
    private LocalDate fechaRetiro;

    @Column(name = "retiro_cumplido", nullable = false)
    private Boolean retiroCumplido = false;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private User usuario;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Transient
    @JsonProperty("entidadNombre")
    private String entidadNombre;

    @Transient
    @JsonProperty("entidadCodigo")
    private String entidadCodigo;

    public enum TipoEntidad {
        MADRE, PADRILLO, LOTE
    }

    public EventoSanitario() {}

    public EventoSanitario(TipoEventoSanitario tipoEventoSanitario, LocalDate fecha,
                          TipoEntidad tipoEntidad, Long entidadId, Empresa empresa, User usuario) {
        this.tipoEventoSanitario = tipoEventoSanitario;
        this.fecha = fecha;
        this.tipoEntidad = tipoEntidad;
        this.entidadId = entidadId;
        this.empresa = empresa;
        this.usuario = usuario;
        this.activo = true;
        this.retiroCumplido = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoEventoSanitario getTipoEventoSanitario() { return tipoEventoSanitario; }
    public void setTipoEventoSanitario(TipoEventoSanitario tipoEventoSanitario) { this.tipoEventoSanitario = tipoEventoSanitario; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public TipoEntidad getTipoEntidad() { return tipoEntidad; }
    public void setTipoEntidad(TipoEntidad tipoEntidad) { this.tipoEntidad = tipoEntidad; }
    public Long getEntidadId() { return entidadId; }
    public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }
    public java.math.BigDecimal getDosis() { return dosis; }
    public void setDosis(java.math.BigDecimal dosis) { this.dosis = dosis; }
    public String getUnidadDosis() { return unidadDosis; }
    public void setUnidadDosis(String unidadDosis) { this.unidadDosis = unidadDosis; }
    public Insumo getInsumo() { return insumo; }
    public void setInsumo(Insumo insumo) { this.insumo = insumo; }
    public String getLoteMedicamento() { return loteMedicamento; }
    public void setLoteMedicamento(String loteMedicamento) { this.loteMedicamento = loteMedicamento; }
    public String getProfesionalResponsable() { return profesionalResponsable; }
    public void setProfesionalResponsable(String profesionalResponsable) { this.profesionalResponsable = profesionalResponsable; }
    public LocalDate getFechaRetiro() { return fechaRetiro; }
    public void setFechaRetiro(LocalDate fechaRetiro) { this.fechaRetiro = fechaRetiro; }
    public Boolean getRetiroCumplido() { return retiroCumplido; }
    public void setRetiroCumplido(Boolean retiroCumplido) { this.retiroCumplido = retiroCumplido; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public String getEntidadNombre() { return entidadNombre; }
    public void setEntidadNombre(String entidadNombre) { this.entidadNombre = entidadNombre; }
    public String getEntidadCodigo() { return entidadCodigo; }
    public void setEntidadCodigo(String entidadCodigo) { this.entidadCodigo = entidadCodigo; }

    public boolean requiereFechaRetiro() {
        return tipoEventoSanitario != null && tipoEventoSanitario.getRequiereFechaRetiro();
    }
    public boolean retiroVencido() {
        if (fechaRetiro == null || retiroCumplido) return false;
        return LocalDate.now().isAfter(fechaRetiro);
    }
    public boolean retiroProximoAVencer() {
        if (fechaRetiro == null || retiroCumplido) return false;
        LocalDate hoy = LocalDate.now();
        LocalDate proximoVencimiento = fechaRetiro.minusDays(7);
        return !hoy.isBefore(proximoVencimiento) && hoy.isBefore(fechaRetiro);
    }
}
