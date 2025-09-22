package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.agrocloud.model.enums.EstadoLote;
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
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "lotes")
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
    
    @Column(name = "fecha_ultimo_cambio_estado")
    private LocalDateTime fechaUltimoCambioEstado;
    
    @Column(name = "motivo_cambio_estado")
    private String motivoCambioEstado;

    @Size(max = 100, message = "El tipo de suelo no puede exceder 100 caracteres")
    @Column(name = "tipo_suelo", length = 100)
    private String tipoSuelo;

    @Size(max = 100, message = "El cultivo actual no puede exceder 100 caracteres")
    @Column(name = "cultivo_actual", length = 100)
    private String cultivoActual;

    @Column(name = "fecha_siembra")
    private LocalDate fechaSiembra;

    @Column(name = "fecha_cosecha_esperada")
    private LocalDate fechaCosechaEsperada;
    
    @Column(name = "fecha_cosecha_real")
    private LocalDate fechaCosechaReal;
    
    @Column(name = "rendimiento_esperado", precision = 10, scale = 2)
    private BigDecimal rendimientoEsperado;
    
    @Column(name = "rendimiento_real", precision = 10, scale = 2)
    private BigDecimal rendimientoReal;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campo_id")
    private Field campo;

    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Labor> labores = new ArrayList<>();

    // Relación con el usuario propietario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // Relación con la empresa (para sistema multitenant)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    @JsonIgnore
    private Empresa empresa;


    // Constructors
    public Plot() {}

    public Plot(String nombre, BigDecimal areaHectareas, Field campo) {
        this.nombre = nombre;
        this.areaHectareas = areaHectareas;
        this.campo = campo;
        this.activo = true;
        this.estado = EstadoLote.DISPONIBLE;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getAreaHectareas() {
        return areaHectareas;
    }

    public void setAreaHectareas(BigDecimal areaHectareas) {
        this.areaHectareas = areaHectareas;
    }

    public EstadoLote getEstado() {
        return estado;
    }

    public void setEstado(EstadoLote estado) {
        this.estado = estado;
    }
    
    public LocalDateTime getFechaUltimoCambioEstado() {
        return fechaUltimoCambioEstado;
    }
    
    public void setFechaUltimoCambioEstado(LocalDateTime fechaUltimoCambioEstado) {
        this.fechaUltimoCambioEstado = fechaUltimoCambioEstado;
    }
    
    public String getMotivoCambioEstado() {
        return motivoCambioEstado;
    }
    
    public void setMotivoCambioEstado(String motivoCambioEstado) {
        this.motivoCambioEstado = motivoCambioEstado;
    }

    public String getTipoSuelo() {
        return tipoSuelo;
    }

    public void setTipoSuelo(String tipoSuelo) {
        this.tipoSuelo = tipoSuelo;
    }

    public String getCultivoActual() {
        return cultivoActual;
    }

    public void setCultivoActual(String cultivoActual) {
        this.cultivoActual = cultivoActual;
    }

    public LocalDate getFechaSiembra() {
        return fechaSiembra;
    }

    public void setFechaSiembra(LocalDate fechaSiembra) {
        this.fechaSiembra = fechaSiembra;
    }

    public LocalDate getFechaCosechaEsperada() {
        return fechaCosechaEsperada;
    }

    public void setFechaCosechaEsperada(LocalDate fechaCosechaEsperada) {
        this.fechaCosechaEsperada = fechaCosechaEsperada;
    }
    
    public LocalDate getFechaCosechaReal() {
        return fechaCosechaReal;
    }
    
    public void setFechaCosechaReal(LocalDate fechaCosechaReal) {
        this.fechaCosechaReal = fechaCosechaReal;
    }
    
    public BigDecimal getRendimientoEsperado() {
        return rendimientoEsperado;
    }
    
    public void setRendimientoEsperado(BigDecimal rendimientoEsperado) {
        this.rendimientoEsperado = rendimientoEsperado;
    }
    
    public BigDecimal getRendimientoReal() {
        return rendimientoReal;
    }
    
    public void setRendimientoReal(BigDecimal rendimientoReal) {
        this.rendimientoReal = rendimientoReal;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Field getCampo() {
        return campo;
    }

    public void setCampo(Field campo) {
        this.campo = campo;
    }

    public List<Labor> getLabores() {
        return labores;
    }

    public void setLabores(List<Labor> labores) {
        this.labores = labores;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    // Métodos de compatibilidad para los tests
    public void setUsuario(User usuario) {
        this.user = usuario;
    }
    
    public User getUsuario() {
        return this.user;
    }


    // Helper methods
    public void addLabor(Labor labor) {
        labores.add(labor);
        labor.setLote(this);
    }

    public void removeLabor(Labor labor) {
        labores.remove(labor);
        labor.setLote(null);
    }
    
    // Métodos para gestión de estados
    public boolean puedeSembrar() {
        return estado == EstadoLote.DISPONIBLE || 
               estado == EstadoLote.PREPARADO ||
               estado == EstadoLote.EN_PREPARACION;
    }
    
    public boolean puedeCosechar() {
        return estado == EstadoLote.LISTO_PARA_COSECHA;
    }
    
    public void cambiarEstado(EstadoLote nuevoEstado, String motivo) {
        this.estado = nuevoEstado;
        this.fechaUltimoCambioEstado = LocalDateTime.now();
        this.motivoCambioEstado = motivo;
    }
    
    public long calcularDiasDesdeSiembra() {
        if (fechaSiembra == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(fechaSiembra, LocalDate.now());
    }
    
    public long calcularDiasDesdeUltimoCambioEstado() {
        if (fechaUltimoCambioEstado == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(fechaUltimoCambioEstado.toLocalDate(), LocalDate.now());
    }

    // Getters y Setters para empresa
    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    @Override
    public String toString() {
        return "Plot{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", areaHectareas=" + areaHectareas +
                ", estado='" + estado + '\'' +
                ", activo=" + activo +
                '}';
    }
}
