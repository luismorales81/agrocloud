package com.agrogestion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa la maquinaria agrícola en el sistema.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "maquinaria")
@EntityListeners(AuditingEntityListener.class)
public class Maquinaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la maquinaria es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @NotNull(message = "El tipo de maquinaria es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoMaquinaria tipo;

    @Size(max = 100, message = "La marca no puede exceder 100 caracteres")
    @Column(name = "marca", length = 100)
    private String marca;

    @Size(max = 100, message = "El modelo no puede exceder 100 caracteres")
    @Column(name = "modelo", length = 100)
    private String modelo;

    @Column(name = "año_fabricacion")
    private Integer añoFabricacion;

    @Size(max = 100, message = "El número de serie no puede exceder 100 caracteres")
    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoMaquinaria estado = EstadoMaquinaria.OPERATIVA;

    @Positive(message = "Las horas de trabajo deben ser un valor positivo")
    @Column(name = "horas_trabajo", precision = 10, scale = 2)
    private BigDecimal horasTrabajo = BigDecimal.ZERO;

    @Positive(message = "El costo de adquisición debe ser un valor positivo")
    @Column(name = "costo_adquisicion", precision = 15, scale = 2)
    private BigDecimal costoAdquisicion;

    @Positive(message = "El valor actual debe ser un valor positivo")
    @Column(name = "valor_actual", precision = 15, scale = 2)
    private BigDecimal valorActual;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "maquinaria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MantenimientoMaquinaria> mantenimientos = new ArrayList<>();

    @OneToMany(mappedBy = "maquinaria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Labor> labores = new ArrayList<>();

    // Enums
    public enum TipoMaquinaria {
        TRACTOR, COSECHADORA, SEMBRADORA, PULVERIZADORA, ARADO, RASTRA, OTROS
    }

    public enum EstadoMaquinaria {
        OPERATIVA, MANTENIMIENTO, REPARACION, FUERA_SERVICIO
    }

    // Constructors
    public Maquinaria() {}

    public Maquinaria(String nombre, TipoMaquinaria tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.activo = true;
        this.estado = EstadoMaquinaria.OPERATIVA;
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

    public TipoMaquinaria getTipo() {
        return tipo;
    }

    public void setTipo(TipoMaquinaria tipo) {
        this.tipo = tipo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAñoFabricacion() {
        return añoFabricacion;
    }

    public void setAñoFabricacion(Integer añoFabricacion) {
        this.añoFabricacion = añoFabricacion;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public EstadoMaquinaria getEstado() {
        return estado;
    }

    public void setEstado(EstadoMaquinaria estado) {
        this.estado = estado;
    }

    public BigDecimal getHorasTrabajo() {
        return horasTrabajo;
    }

    public void setHorasTrabajo(BigDecimal horasTrabajo) {
        this.horasTrabajo = horasTrabajo;
    }

    public BigDecimal getCostoAdquisicion() {
        return costoAdquisicion;
    }

    public void setCostoAdquisicion(BigDecimal costoAdquisicion) {
        this.costoAdquisicion = costoAdquisicion;
    }

    public BigDecimal getValorActual() {
        return valorActual;
    }

    public void setValorActual(BigDecimal valorActual) {
        this.valorActual = valorActual;
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

    public List<MantenimientoMaquinaria> getMantenimientos() {
        return mantenimientos;
    }

    public void setMantenimientos(List<MantenimientoMaquinaria> mantenimientos) {
        this.mantenimientos = mantenimientos;
    }

    public List<Labor> getLabores() {
        return labores;
    }

    public void setLabores(List<Labor> labores) {
        this.labores = labores;
    }

    // Helper methods
    public void addMantenimiento(MantenimientoMaquinaria mantenimiento) {
        mantenimientos.add(mantenimiento);
        mantenimiento.setMaquinaria(this);
    }

    public void removeMantenimiento(MantenimientoMaquinaria mantenimiento) {
        mantenimientos.remove(mantenimiento);
        mantenimiento.setMaquinaria(null);
    }

    public void addLabor(Labor labor) {
        labores.add(labor);
        labor.setMaquinaria(this);
    }

    public void removeLabor(Labor labor) {
        labores.remove(labor);
        labor.setMaquinaria(null);
    }

    @Override
    public String toString() {
        return "Maquinaria{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", tipo=" + tipo +
                ", estado=" + estado +
                ", activo=" + activo +
                '}';
    }
}
