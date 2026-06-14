package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Consumo de alimento en el módulo Porcinos. Usa loteId y cultivoRelacionadoId para no depender de Cultivos.
 */
@Entity
@Table(name = "porcinos_consumos_alimento")
@EntityListeners(AuditingEntityListener.class)
public class ConsumoAlimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private CategoriaAlimento categoria;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "cantidad_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alimento", nullable = false)
    private TipoAlimento tipoAlimento;

    @Column(name = "cultivo_relacionado_id")
    private Long cultivoRelacionadoId;

    @Column(name = "lote_id")
    private Long loteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "madre_id")
    private Madre madre;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"userCompanyRoles", "usuarioEmpresas", "userRoles", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    private User usuario;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public enum CategoriaAlimento {
        MADRES, PADRILLOS, RECRIA, ENGORDE, LECHONES
    }

    public enum TipoAlimento {
        BALANCEADO, MAIZ, GRANO_PROPIO
    }

    public ConsumoAlimento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CategoriaAlimento getCategoria() { return categoria; }
    public void setCategoria(CategoriaAlimento categoria) { this.categoria = categoria; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public BigDecimal getCantidadKg() { return cantidadKg; }
    public void setCantidadKg(BigDecimal cantidadKg) { this.cantidadKg = cantidadKg; }
    public TipoAlimento getTipoAlimento() { return tipoAlimento; }
    public void setTipoAlimento(TipoAlimento tipoAlimento) { this.tipoAlimento = tipoAlimento; }
    public Long getCultivoRelacionadoId() { return cultivoRelacionadoId; }
    public void setCultivoRelacionadoId(Long cultivoRelacionadoId) { this.cultivoRelacionadoId = cultivoRelacionadoId; }
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public Madre getMadre() { return madre; }
    public void setMadre(Madre madre) { this.madre = madre; }
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
}
