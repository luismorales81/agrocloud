package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un movimiento entre etapas de recría
 */
@Entity
@Table(name = "porcinos_movimientos_etapas")
@EntityListeners(AuditingEntityListener.class)
public class MovimientoEtapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recria_origen_id", nullable = false)
    private Recria recriaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recria_destino_id")
    private Recria recriaDestino;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_origen", nullable = false)
    private EtapaRecria etapaOrigen;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_destino", nullable = false)
    private EtapaRecria etapaDestino;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDate fechaMovimiento;

    @Column(name = "cantidad_animales", nullable = false)
    private Integer cantidadAnimales;

    @Column(name = "peso_promedio", precision = 10, scale = 2)
    private BigDecimal pesoPromedio;

    @Column(name = "lote_destino_id")
    private Long loteDestinoId;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"userCompanyRoles", "usuarioEmpresas", "userRoles", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    private User usuario;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public enum EtapaRecria {
        F1, F2, F3, F4, DESARROLLO, TERMINACION
    }

    public MovimientoEtapa() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Recria getRecriaOrigen() { return recriaOrigen; }
    public void setRecriaOrigen(Recria recriaOrigen) { this.recriaOrigen = recriaOrigen; }
    public Recria getRecriaDestino() { return recriaDestino; }
    public void setRecriaDestino(Recria recriaDestino) { this.recriaDestino = recriaDestino; }
    public EtapaRecria getEtapaOrigen() { return etapaOrigen; }
    public void setEtapaOrigen(EtapaRecria etapaOrigen) { this.etapaOrigen = etapaOrigen; }
    public EtapaRecria getEtapaDestino() { return etapaDestino; }
    public void setEtapaDestino(EtapaRecria etapaDestino) { this.etapaDestino = etapaDestino; }
    public LocalDate getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDate fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
    public Integer getCantidadAnimales() { return cantidadAnimales; }
    public void setCantidadAnimales(Integer cantidadAnimales) { this.cantidadAnimales = cantidadAnimales; }
    public BigDecimal getPesoPromedio() { return pesoPromedio; }
    public void setPesoPromedio(BigDecimal pesoPromedio) { this.pesoPromedio = pesoPromedio; }
    public Long getLoteDestinoId() { return loteDestinoId; }
    public void setLoteDestinoId(Long loteDestinoId) { this.loteDestinoId = loteDestinoId; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
