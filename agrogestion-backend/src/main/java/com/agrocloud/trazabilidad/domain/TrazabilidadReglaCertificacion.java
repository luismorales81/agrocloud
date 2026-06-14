package com.agrocloud.trazabilidad.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Regla de certificación configurable. El campo {@code tipoRegla} se resuelve en el motor
 * (estrategia por tipo); {@code parametrosJson} almacena parámetros sin recompilar.
 */
@Entity
@Table(name = "trazabilidad_regla_certificacion")
@EntityListeners(AuditingEntityListener.class)
public class TrazabilidadReglaCertificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "certificacion_id", nullable = false)
    @JsonIgnore
    private TrazabilidadCertificacion certificacion;

    @Column(name = "tipo_regla", nullable = false, length = 64)
    private String tipoRegla;

    @Column(name = "parametros_json", nullable = false, columnDefinition = "TEXT")
    private String parametrosJson;

    @Column(name = "orden_ejecucion", nullable = false)
    private int ordenEjecucion;

    @Column(name = "activa", nullable = false)
    private boolean activa = true;

    @CreatedDate
    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TrazabilidadCertificacion getCertificacion() { return certificacion; }
    public void setCertificacion(TrazabilidadCertificacion certificacion) { this.certificacion = certificacion; }
    public String getTipoRegla() { return tipoRegla; }
    public void setTipoRegla(String tipoRegla) { this.tipoRegla = tipoRegla; }
    public String getParametrosJson() { return parametrosJson; }
    public void setParametrosJson(String parametrosJson) { this.parametrosJson = parametrosJson; }
    public int getOrdenEjecucion() { return ordenEjecucion; }
    public void setOrdenEjecucion(int ordenEjecucion) { this.ordenEjecucion = ordenEjecucion; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
