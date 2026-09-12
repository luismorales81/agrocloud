package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro histórico de pesada por lote (recría/engorde).
 */
@Entity
@Table(name = "porcinos_legacy_registros_peso")
@EntityListeners(AuditingEntityListener.class)
public class RegistroPeso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recria_id", nullable = false)
    @JsonIgnore
    private Recria recria;

    @Column(name = "fecha_pesaje", nullable = false)
    private LocalDate fechaPesaje;

    @Column(name = "peso_promedio", precision = 10, scale = 2, nullable = false)
    private BigDecimal pesoPromedio;

    @Column(name = "cantidad_animales", nullable = false)
    private Integer cantidadAnimales;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo", length = 20, nullable = false)
    private MetodoPesaje metodo = MetodoPesaje.BALANZA;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_al_momento", length = 20)
    private Recria.EtapaRecria etapaAlMomento;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"userCompanyRoles", "usuarioEmpresas", "userRoles", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    private User usuario;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public enum MetodoPesaje {
        BALANZA, MUESTREO, ESTIMADO
    }

    public RegistroPeso() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Recria getRecria() { return recria; }
    public void setRecria(Recria recria) { this.recria = recria; }
    public LocalDate getFechaPesaje() { return fechaPesaje; }
    public void setFechaPesaje(LocalDate fechaPesaje) { this.fechaPesaje = fechaPesaje; }
    public BigDecimal getPesoPromedio() { return pesoPromedio; }
    public void setPesoPromedio(BigDecimal pesoPromedio) { this.pesoPromedio = pesoPromedio; }
    public Integer getCantidadAnimales() { return cantidadAnimales; }
    public void setCantidadAnimales(Integer cantidadAnimales) { this.cantidadAnimales = cantidadAnimales; }
    public MetodoPesaje getMetodo() { return metodo; }
    public void setMetodo(MetodoPesaje metodo) { this.metodo = metodo != null ? metodo : MetodoPesaje.BALANZA; }
    public Recria.EtapaRecria getEtapaAlMomento() { return etapaAlMomento; }
    public void setEtapaAlMomento(Recria.EtapaRecria etapaAlMomento) { this.etapaAlMomento = etapaAlMomento; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @JsonProperty("recriaId")
    public Long getRecriaId() {
        return recria != null ? recria.getId() : null;
    }
}
