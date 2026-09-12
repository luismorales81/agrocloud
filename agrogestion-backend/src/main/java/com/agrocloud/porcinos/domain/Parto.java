package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.domain.TipoParto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "porcinos_legacy_partos")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"empresa", "usuario"})
public class Parto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "madre_id", nullable = false)
    @JsonIgnore
    private Madre madre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestacion_id")
    @JsonIgnore
    private Gestacion gestacion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "nacidos_vivos", nullable = false)
    private Integer nacidosVivos = 0;

    @Column(name = "nacidos_muertos", nullable = false)
    private Integer nacidosMuertos = 0;

    @Column(name = "momias", nullable = false)
    private Integer momias = 0;

    @Column(name = "total_nacidos", nullable = false)
    private Integer totalNacidos;

    @Column(name = "peso_promedio_nacimiento", precision = 10, scale = 2)
    private java.math.BigDecimal pesoPromedioNacimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_parto_id")
    @JsonIgnoreProperties({"empresa"})
    private TipoParto tipoParto;

    @Column(name = "intervenciones", columnDefinition = "TEXT")
    private String intervenciones;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private User usuario;

    @OneToMany(mappedBy = "parto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MuerteLechon> muertesLechones = new ArrayList<>();

    @OneToMany(mappedBy = "parto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Destete> destetes = new ArrayList<>();

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Madre getMadre() { return madre; }
    public void setMadre(Madre madre) { this.madre = madre; }
    public Gestacion getGestacion() { return gestacion; }
    public void setGestacion(Gestacion gestacion) { this.gestacion = gestacion; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public Integer getNacidosVivos() { return nacidosVivos; }
    public void setNacidosVivos(Integer nacidosVivos) { this.nacidosVivos = nacidosVivos; }
    public Integer getNacidosMuertos() { return nacidosMuertos; }
    public void setNacidosMuertos(Integer nacidosMuertos) { this.nacidosMuertos = nacidosMuertos; }
    public Integer getMomias() { return momias; }
    public void setMomias(Integer momias) { this.momias = momias; }
    public Integer getTotalNacidos() { return totalNacidos; }
    public void setTotalNacidos(Integer totalNacidos) { this.totalNacidos = totalNacidos; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public java.math.BigDecimal getPesoPromedioNacimiento() { return pesoPromedioNacimiento; }
    public void setPesoPromedioNacimiento(java.math.BigDecimal pesoPromedioNacimiento) { this.pesoPromedioNacimiento = pesoPromedioNacimiento; }
    public List<MuerteLechon> getMuertesLechones() { return muertesLechones; }
    public void setMuertesLechones(List<MuerteLechon> muertesLechones) { this.muertesLechones = muertesLechones; }
    public List<Destete> getDestetes() { return destetes; }
    public void setDestetes(List<Destete> destetes) { this.destetes = destetes; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public TipoParto getTipoParto() { return tipoParto; }
    public void setTipoParto(TipoParto tipoParto) { this.tipoParto = tipoParto; }
    public String getIntervenciones() { return intervenciones; }
    public void setIntervenciones(String intervenciones) { this.intervenciones = intervenciones; }

    @JsonProperty("madreId")
    public Long getMadreId() {
        return madre != null ? madre.getId() : null;
    }

    @JsonProperty("madreIdentificacion")
    public String getMadreIdentificacion() {
        return madre != null ? madre.getIdentificacion() : null;
    }
}
