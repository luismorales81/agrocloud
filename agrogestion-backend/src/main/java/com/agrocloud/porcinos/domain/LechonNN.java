package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un lechón sin origen claro (NN - No Nominal).
 * Usa loteId para no depender del módulo Cultivos.
 */
@Entity
@Table(name = "porcinos_lechones_nn")
@EntityListeners(AuditingEntityListener.class)
public class LechonNN {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identificacion", length = 100)
    private String identificacion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Column(name = "lote_id", nullable = false)
    private Long loteId;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 1;

    @Column(name = "peso_promedio", precision = 10, scale = 2)
    private BigDecimal pesoPromedio;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_ingreso", nullable = false)
    private EtapaIngreso etapaIngreso;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public enum EtapaIngreso {
        DESTETE, RECRIA, F1, F2, F3, F4
    }

    public LechonNN() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPesoPromedio() { return pesoPromedio; }
    public void setPesoPromedio(BigDecimal pesoPromedio) { this.pesoPromedio = pesoPromedio; }
    public EtapaIngreso getEtapaIngreso() { return etapaIngreso; }
    public void setEtapaIngreso(EtapaIngreso etapaIngreso) { this.etapaIngreso = etapaIngreso; }
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
}
