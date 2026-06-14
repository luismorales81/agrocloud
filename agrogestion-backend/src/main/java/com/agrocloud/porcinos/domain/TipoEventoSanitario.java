package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad que representa un tipo de evento sanitario configurable
 * Ejemplos: Vacunación, Desparasitación, Tratamiento, Antibiótico, etc.
 */
@Entity
@Table(name = "porcinos_tipos_evento_sanitario")
@EntityListeners(AuditingEntityListener.class)
public class TipoEventoSanitario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private CategoriaEvento categoria;

    @Column(name = "requiere_fecha_retiro", nullable = false)
    private Boolean requiereFechaRetiro = false;

    @Column(name = "dias_retiro_defecto")
    private Integer diasRetiroDefecto;

    @Column(name = "requiere_lote_medicamento", nullable = false)
    private Boolean requiereLoteMedicamento = false;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public enum CategoriaEvento {
        VACUNACION, DESPARASITACION, ANTIBIOTICO, VITAMINA, TRATAMIENTO, CONTROL, OTRO
    }

    public TipoEventoSanitario() {}

    public TipoEventoSanitario(String nombre, CategoriaEvento categoria, Empresa empresa) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.empresa = empresa;
        this.activo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public CategoriaEvento getCategoria() { return categoria; }
    public void setCategoria(CategoriaEvento categoria) { this.categoria = categoria; }
    public Boolean getRequiereFechaRetiro() { return requiereFechaRetiro; }
    public void setRequiereFechaRetiro(Boolean requiereFechaRetiro) { this.requiereFechaRetiro = requiereFechaRetiro; }
    public Integer getDiasRetiroDefecto() { return diasRetiroDefecto; }
    public void setDiasRetiroDefecto(Integer diasRetiroDefecto) { this.diasRetiroDefecto = diasRetiroDefecto; }
    public Boolean getRequiereLoteMedicamento() { return requiereLoteMedicamento; }
    public void setRequiereLoteMedicamento(Boolean requiereLoteMedicamento) { this.requiereLoteMedicamento = requiereLoteMedicamento; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
