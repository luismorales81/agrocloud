package com.agrocloud.cultivos.domain;

import com.agrocloud.cultivos.util.MapeadorEstadoLoteConfig;
import com.agrocloud.core.domain.User;
import com.agrocloud.model.enums.EstadoLote;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa los lotes dentro de los campos.
 */
@Entity
@Table(name = "cultivo_lotes")
@EntityListeners(AuditingEntityListener.class)
public class Plot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del lote es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre del lote debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @NotNull(message = "La superficie es obligatoria")
    @Positive(message = "La superficie debe ser un valor positivo")
    @Column(name = "area_hectareas", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaHectareas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoLote estado = EstadoLote.DISPONIBLE;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "estado_configurado_id", nullable = true)
    private EstadoLoteConfig estadoConfigurado;

    @Column(name = "fecha_ultimo_cambio_estado")
    private LocalDateTime fechaUltimoCambioEstado;

    @Column(name = "motivo_cambio_estado")
    private String motivoCambioEstado;

    @Size(max = 100) @Column(name = "tipo_suelo", length = 100)
    private String tipoSuelo;
    @Size(max = 100) @Column(name = "cultivo_actual", length = 100)
    private String cultivoActual;
    @Column(name = "fecha_siembra") private LocalDate fechaSiembra;
    @Column(name = "fecha_cosecha_esperada") private LocalDate fechaCosechaEsperada;
    @Column(name = "fecha_cosecha_real") private LocalDate fechaCosechaReal;
    @Column(name = "rendimiento_esperado", precision = 10, scale = 2) private BigDecimal rendimientoEsperado;
    @Column(name = "rendimiento_real", precision = 10, scale = 2) private BigDecimal rendimientoReal;
    @Column(name = "activo", nullable = false) private Boolean activo = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_uso", nullable = true, length = 20)
    private TipoUsoLote tipoUso = TipoUsoLote.CULTIVO;

    /** Si true, la última cosecha no se considera vigente; el estado derivado puede ser DISPONIBLE. */
    @Column(name = "liberado_para_siembra", nullable = false)
    private Boolean liberadoParaSiembra = false;

    @Column(name = "ciclo_activo_id")
    private Long cicloActivoId;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campo_id")
    @JsonIgnore
    private Field campo;

    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Labor> labores = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cultivo_id", nullable = true)
    @JsonIgnore
    private Cultivo cultivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "tipo_cultivo_id", nullable = true)
    @JsonIgnore
    private TipoCultivo tipoCultivo;

    public enum TipoUsoLote { CULTIVO, PORCINO }

    public Plot() {}
    public Plot(String nombre, BigDecimal areaHectareas, Field campo) {
        this.nombre = nombre;
        this.areaHectareas = areaHectareas;
        this.campo = campo;
        this.activo = true;
        this.estado = EstadoLote.DISPONIBLE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getAreaHectareas() { return areaHectareas; }
    public void setAreaHectareas(BigDecimal areaHectareas) { this.areaHectareas = areaHectareas; }
    public EstadoLote getEstado() { return estado; }
    public void setEstado(EstadoLote estado) { this.estado = estado; }
    public LocalDateTime getFechaUltimoCambioEstado() { return fechaUltimoCambioEstado; }
    public void setFechaUltimoCambioEstado(LocalDateTime fechaUltimoCambioEstado) { this.fechaUltimoCambioEstado = fechaUltimoCambioEstado; }
    public String getMotivoCambioEstado() { return motivoCambioEstado; }
    public void setMotivoCambioEstado(String motivoCambioEstado) { this.motivoCambioEstado = motivoCambioEstado; }
    public EstadoLoteConfig getEstadoConfigurado() { return estadoConfigurado; }
    public void setEstadoConfigurado(EstadoLoteConfig estadoConfigurado) { this.estadoConfigurado = estadoConfigurado; }
    public Cultivo getCultivo() { return cultivo; }
    public void setCultivo(Cultivo cultivo) { this.cultivo = cultivo; }
    public TipoCultivo getTipoCultivo() { return tipoCultivo; }
    public void setTipoCultivo(TipoCultivo tipoCultivo) { this.tipoCultivo = tipoCultivo; }
    public String getNombreEstado() {
        if (estadoConfigurado != null) return estadoConfigurado.getNombre();
        return estado != null ? estado.getDescripcion() : "Sin estado";
    }
    public String getColorEstado() {
        if (estadoConfigurado != null && estadoConfigurado.getColor() != null) return estadoConfigurado.getColor();
        return estado != null ? estado.getColor() : "#6b7280";
    }
    public String getIconoEstado() {
        if (estadoConfigurado != null && estadoConfigurado.getIcono() != null) return estadoConfigurado.getIcono();
        if (estado != null) {
            switch (estado) {
                case DISPONIBLE: return "🟢";
                case PREPARADO: return "🟡";
                case SEMBRADO: return "🌱";
                case EN_CRECIMIENTO: return "🌿";
                case EN_FLORACION: return "🌸";
                case EN_FRUTIFICACION: return "🍎";
                case LISTO_PARA_COSECHA: return "📦";
                case EN_COSECHA: return "⚙️";
                case COSECHADO: return "✅";
                case EN_DESCANSO: return "😴";
                case EN_PREPARACION: return "🔧";
                case ENFERMO: return "⚠️";
                case ABANDONADO: return "❌";
                default: return "📋";
            }
        }
        return "📋";
    }
    public String getTipoSuelo() { return tipoSuelo; }
    public void setTipoSuelo(String tipoSuelo) { this.tipoSuelo = tipoSuelo; }
    public String getCultivoActual() { return cultivoActual; }
    public void setCultivoActual(String cultivoActual) { this.cultivoActual = cultivoActual; }
    public LocalDate getFechaSiembra() { return fechaSiembra; }
    public void setFechaSiembra(LocalDate fechaSiembra) { this.fechaSiembra = fechaSiembra; }
    public LocalDate getFechaCosechaEsperada() { return fechaCosechaEsperada; }
    public void setFechaCosechaEsperada(LocalDate fechaCosechaEsperada) { this.fechaCosechaEsperada = fechaCosechaEsperada; }
    public LocalDate getFechaCosechaReal() { return fechaCosechaReal; }
    public void setFechaCosechaReal(LocalDate fechaCosechaReal) { this.fechaCosechaReal = fechaCosechaReal; }
    public BigDecimal getRendimientoEsperado() { return rendimientoEsperado; }
    public void setRendimientoEsperado(BigDecimal rendimientoEsperado) { this.rendimientoEsperado = rendimientoEsperado; }
    public BigDecimal getRendimientoReal() { return rendimientoReal; }
    public void setRendimientoReal(BigDecimal rendimientoReal) { this.rendimientoReal = rendimientoReal; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public TipoUsoLote getTipoUso() { return tipoUso != null ? tipoUso : TipoUsoLote.CULTIVO; }
    public void setTipoUso(TipoUsoLote tipoUso) { this.tipoUso = tipoUso; }
    public Boolean getLiberadoParaSiembra() { return liberadoParaSiembra != null ? liberadoParaSiembra : false; }
    public void setLiberadoParaSiembra(Boolean liberadoParaSiembra) { this.liberadoParaSiembra = Boolean.TRUE.equals(liberadoParaSiembra); }
    public Long getCicloActivoId() { return cicloActivoId; }
    public void setCicloActivoId(Long cicloActivoId) { this.cicloActivoId = cicloActivoId; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Field getCampo() { return campo; }
    public void setCampo(Field campo) { this.campo = campo; }
    @com.fasterxml.jackson.annotation.JsonProperty("campoId")
    public Long getCampoId() { return campo != null ? campo.getId() : null; }
    @com.fasterxml.jackson.annotation.JsonProperty("campoId")
    public void setCampoId(Long campoId) {
        if (campoId != null) {
            Field referencia = new Field();
            referencia.setId(campoId);
            this.campo = referencia;
        }
    }
    public List<Labor> getLabores() { return labores; }
    public void setLabores(List<Labor> labores) { this.labores = labores; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public void setUsuario(User usuario) { this.user = usuario; }
    @JsonIgnore
    public User getUsuario() { return this.user; }
    public void addLabor(Labor labor) { labores.add(labor); labor.setLote(this); }
    public void removeLabor(Labor labor) { labores.remove(labor); labor.setLote(null); }
    public boolean puedeSembrar() {
        return estado == EstadoLote.DISPONIBLE || estado == EstadoLote.PREPARADO || estado == EstadoLote.EN_PREPARACION;
    }
    public boolean puedeCosechar() {
        return estado == EstadoLote.SEMBRADO || estado == EstadoLote.EN_CRECIMIENTO || estado == EstadoLote.EN_FLORACION || estado == EstadoLote.EN_FRUTIFICACION || estado == EstadoLote.LISTO_PARA_COSECHA;
    }
    public void cambiarEstado(EstadoLote nuevoEstado, String motivo) {
        this.estado = nuevoEstado;
        this.estadoConfigurado = null;
        this.fechaUltimoCambioEstado = LocalDateTime.now();
        this.motivoCambioEstado = motivo;
    }
    public void cambiarEstadoConfigurado(EstadoLoteConfig nuevoEstado, String motivo) {
        this.estadoConfigurado = nuevoEstado;
        this.estado = MapeadorEstadoLoteConfig.mapearAEnum(nuevoEstado);
        this.fechaUltimoCambioEstado = LocalDateTime.now();
        this.motivoCambioEstado = motivo;
    }
    public long calcularDiasDesdeSiembra() {
        return fechaSiembra == null ? 0 : java.time.temporal.ChronoUnit.DAYS.between(fechaSiembra, LocalDate.now());
    }
    public long calcularDiasDesdeUltimoCambioEstado() {
        return fechaUltimoCambioEstado == null ? 0 : java.time.temporal.ChronoUnit.DAYS.between(fechaUltimoCambioEstado.toLocalDate(), LocalDate.now());
    }
    @Override
    public String toString() { return "Plot{id=" + id + ", nombre='" + nombre + "', areaHectareas=" + areaHectareas + ", estado='" + estado + "', activo=" + activo + "}"; }
}
