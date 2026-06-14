package com.agrocloud.cultivos.domain;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Entidad para representar cultivos agropecuarios
 */
@Entity
@Table(name = "cultivo_cultivos")
@EntityListeners(AuditingEntityListener.class)
public class Cultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "variedad", nullable = false, length = 100)
    private String variedad;

    @Column(name = "ciclo_dias")
    private Integer cicloDias;

    @Column(name = "rendimiento_esperado", precision = 10, scale = 2)
    private BigDecimal rendimientoEsperado;

    @Column(name = "unidad_rendimiento", length = 50)
    private String unidadRendimiento;

    @Column(name = "precio_por_tonelada", precision = 10, scale = 2)
    private BigDecimal precioPorTonelada;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCultivo estado = EstadoCultivo.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private User usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    @JsonIgnore
    private Empresa empresa;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    public enum EstadoCultivo {
        ACTIVO, INACTIVO
    }

    public Cultivo() {}

    public Cultivo(String nombre, String tipo, String variedad, Integer cicloDias,
                   BigDecimal rendimientoEsperado, String unidadRendimiento,
                   BigDecimal precioPorTonelada, String descripcion, User usuario) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.variedad = variedad;
        this.cicloDias = cicloDias;
        this.rendimientoEsperado = rendimientoEsperado;
        this.unidadRendimiento = unidadRendimiento;
        this.precioPorTonelada = precioPorTonelada;
        this.descripcion = descripcion;
        this.usuario = usuario;
        this.estado = EstadoCultivo.ACTIVO;
        this.activo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getVariedad() { return variedad; }
    public void setVariedad(String variedad) { this.variedad = variedad; }
    public Integer getCicloDias() { return cicloDias; }
    public void setCicloDias(Integer cicloDias) { this.cicloDias = cicloDias; }
    public BigDecimal getRendimientoEsperado() { return rendimientoEsperado; }
    public void setRendimientoEsperado(BigDecimal rendimientoEsperado) { this.rendimientoEsperado = rendimientoEsperado; }
    public String getUnidadRendimiento() { return unidadRendimiento; }
    public void setUnidadRendimiento(String unidadRendimiento) { this.unidadRendimiento = unidadRendimiento; }
    public BigDecimal getPrecioPorTonelada() { return precioPorTonelada; }
    public void setPrecioPorTonelada(BigDecimal precioPorTonelada) { this.precioPorTonelada = precioPorTonelada; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public EstadoCultivo getEstado() { return estado; }
    public void setEstado(EstadoCultivo estado) { this.estado = estado; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public BigDecimal getDiferenciaRendimiento() {
        if (rendimientoEsperado == null) return BigDecimal.ZERO;
        return rendimientoEsperado;
    }
    public BigDecimal getPorcentajeDiferencia() {
        if (rendimientoEsperado == null || rendimientoEsperado.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        BigDecimal diferencia = getDiferenciaRendimiento();
        return diferencia.divide(rendimientoEsperado, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }
    @Override
    public String toString() {
        return "Cultivo{id=" + id + ", nombre='" + nombre + "', variedad='" + variedad + "', cicloDias=" + cicloDias + ", rendimientoEsperado=" + rendimientoEsperado + ", unidadRendimiento='" + unidadRendimiento + "', estado=" + estado + "}";
    }
}
