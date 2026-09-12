package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un esquema sanitario configurable
 */
@Entity
@Table(name = "porcinos_legacy_esquemas_sanitarios_porcinos")
@EntityListeners(AuditingEntityListener.class)
public class EsquemaSanitarioPorcino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoEsquema tipo;

    @Column(name = "producto", length = 200)
    private String producto;

    @Column(name = "dosis", length = 100)
    private String dosis;

    @Column(name = "frecuencia_dias")
    private Integer frecuenciaDias;

    @Column(name = "fecha_programada")
    private LocalDate fechaProgramada;

    @Column(name = "aplicable_a", length = 100)
    private String aplicableA;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public enum TipoEsquema {
        VACUNA, DESPARASITACION, ANTIBIOTICO, OTRO
    }

    public EsquemaSanitarioPorcino() {}

    public EsquemaSanitarioPorcino(String nombre, TipoEsquema tipo, Empresa empresa) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.empresa = empresa;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoEsquema getTipo() { return tipo; }
    public void setTipo(TipoEsquema tipo) { this.tipo = tipo; }
    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }
    public String getDosis() { return dosis; }
    public void setDosis(String dosis) { this.dosis = dosis; }
    public Integer getFrecuenciaDias() { return frecuenciaDias; }
    public void setFrecuenciaDias(Integer frecuenciaDias) { this.frecuenciaDias = frecuenciaDias; }
    public LocalDate getFechaProgramada() { return fechaProgramada; }
    public void setFechaProgramada(LocalDate fechaProgramada) { this.fechaProgramada = fechaProgramada; }
    public String getAplicableA() { return aplicableA; }
    public void setAplicableA(String aplicableA) { this.aplicableA = aplicableA; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
