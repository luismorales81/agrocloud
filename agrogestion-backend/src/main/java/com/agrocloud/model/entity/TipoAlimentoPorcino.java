package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un tipo de alimento configurable para porcinos
 */
@Entity
@Table(name = "porcinos_tipos_alimento_porcinos")
@EntityListeners(AuditingEntityListener.class)
public class TipoAlimentoPorcino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private CategoriaAlimento categoria;

    @Column(name = "porcentaje_proteina", precision = 5, scale = 2)
    private BigDecimal porcentajeProteina;

    @Column(name = "precio_kg", precision = 10, scale = 2)
    private BigDecimal precioKg;

    @Column(name = "unidad_medida", length = 50, nullable = false)
    private String unidadMedida = "kg";

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

    public enum CategoriaAlimento {
        BALANCEADO, GRANO_PROPIO, OTRO
        // NOTA: Las categorías RACION_* fueron eliminadas porque están solapadas con InsumoCompuesto
        // Las recetas (raciones) ahora se gestionan completamente en InsumoCompuesto (tipo RACION)
        // que se asocia a etapas mediante RecetaAlimentacionPorEtapa
    }

    // Constructors
    public TipoAlimentoPorcino() {}

    public TipoAlimentoPorcino(String nombre, CategoriaAlimento categoria, Empresa empresa) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.empresa = empresa;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public CategoriaAlimento getCategoria() { return categoria; }
    public void setCategoria(CategoriaAlimento categoria) { this.categoria = categoria; }
    public BigDecimal getPorcentajeProteina() { return porcentajeProteina; }
    public void setPorcentajeProteina(BigDecimal porcentajeProteina) { this.porcentajeProteina = porcentajeProteina; }
    public BigDecimal getPrecioKg() { return precioKg; }
    public void setPrecioKg(BigDecimal precioKg) { this.precioKg = precioKg; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
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

