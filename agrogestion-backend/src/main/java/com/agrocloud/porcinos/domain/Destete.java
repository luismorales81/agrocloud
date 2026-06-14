package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa el destete de lechones
 */
@Entity
@Table(name = "porcinos_destetes")
@EntityListeners(AuditingEntityListener.class)
public class Destete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parto_id", nullable = false)
    @JsonIgnore
    private Parto parto;

    @Column(name = "fecha_destete", nullable = false)
    private LocalDate fechaDestete;

    @Column(name = "cantidad_destetados", nullable = false)
    private Integer cantidadDestetados;

    @Column(name = "peso_promedio_destete", precision = 10, scale = 2, nullable = false)
    private BigDecimal pesoPromedioDestete;

    @Column(name = "dias_lactancia")
    private Integer diasLactancia;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private User usuario;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Parto getParto() { return parto; }
    public void setParto(Parto parto) { this.parto = parto; }
    public LocalDate getFechaDestete() { return fechaDestete; }
    public void setFechaDestete(LocalDate fechaDestete) { this.fechaDestete = fechaDestete; }
    public Integer getCantidadDestetados() { return cantidadDestetados; }
    public void setCantidadDestetados(Integer cantidadDestetados) { this.cantidadDestetados = cantidadDestetados; }
    public BigDecimal getPesoPromedioDestete() { return pesoPromedioDestete; }
    public void setPesoPromedioDestete(BigDecimal pesoPromedioDestete) { this.pesoPromedioDestete = pesoPromedioDestete; }
    public Integer getDiasLactancia() { return diasLactancia; }
    public void setDiasLactancia(Integer diasLactancia) { this.diasLactancia = diasLactancia; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    @JsonGetter("partoId")
    public Long getPartoId() {
        return parto != null ? parto.getId() : null;
    }
}
