package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "porcinos_legacy_recria")
@EntityListeners(AuditingEntityListener.class)
public class Recria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lote_id", nullable = false)
    private Long loteId;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "peso_individual", precision = 10, scale = 2)
    private BigDecimal pesoIndividual;

    @Column(name = "peso_promedio", precision = 10, scale = 2, nullable = false)
    private BigDecimal pesoPromedio;

    /** Peso inicial al ingreso (destete o alta); no se sobrescribe con pesadas */
    @Column(name = "peso_inicial_kg", precision = 10, scale = 2)
    private BigDecimal pesoInicialKg;

    /** Fecha de la última pesada registrada; se actualiza al cargar un RegistroPeso */
    @Column(name = "fecha_ultima_pesada")
    private LocalDate fechaUltimaPesada;

    /** Fecha en que comenzó la etapa actual (sin cambios automáticos de etapa) */
    @Column(name = "fecha_inicio_etapa")
    private LocalDate fechaInicioEtapa;

    @Column(name = "cantidad_animales", nullable = false)
    private Integer cantidadAnimales;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    private Sexo sexo;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa")
    private EtapaRecria etapa;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    /**
     * Origen de la recría: DESTETE (desde destete interno) o EXTERNO (compra/alta manual).
     */
    public enum OrigenRecria {
        DESTETE, EXTERNO
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", length = 20)
    @JsonProperty("origen")
    private OrigenRecria origen;

    @Enumerated(EnumType.STRING)
    @Column(name = "destino")
    private DestinoRecria destino;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "campana_id")
    private Long campanaId;

    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private User usuario;

    @OneToMany(mappedBy = "recria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MuerteRecria> muertesRecria = new ArrayList<>();

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Campos transitorios para información adicional (no se persisten en BD)
    @Transient
    @JsonProperty("loteNombre")
    private String loteNombre;

    @Transient
    @JsonProperty("madreNombre")
    private String madreNombre;

    /** Peso actual = último RegistroPeso o pesoPromedio (estimado si no hay pesadas). No editable directo. */
    @Transient
    @JsonProperty("pesoActualKg")
    public BigDecimal getPesoActualKg() {
        return getPesoPromedio();
    }

    public enum Sexo {
        MACHO, HEMBRA
    }

    public enum DestinoRecria {
        VENTA, FUTURA_MADRE, ENGORDE
    }

    public enum EtapaRecria {
        F1, F2, F3, F4, DESARROLLO, TERMINACION
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public BigDecimal getPesoIndividual() { return pesoIndividual; }
    public void setPesoIndividual(BigDecimal pesoIndividual) { this.pesoIndividual = pesoIndividual; }
    public BigDecimal getPesoPromedio() { return pesoPromedio; }
    public void setPesoPromedio(BigDecimal pesoPromedio) { this.pesoPromedio = pesoPromedio; }
    public BigDecimal getPesoInicialKg() { return pesoInicialKg; }
    public void setPesoInicialKg(BigDecimal pesoInicialKg) { this.pesoInicialKg = pesoInicialKg; }
    public LocalDate getFechaUltimaPesada() { return fechaUltimaPesada; }
    public void setFechaUltimaPesada(LocalDate fechaUltimaPesada) { this.fechaUltimaPesada = fechaUltimaPesada; }
    public LocalDate getFechaInicioEtapa() { return fechaInicioEtapa; }
    public void setFechaInicioEtapa(LocalDate fechaInicioEtapa) { this.fechaInicioEtapa = fechaInicioEtapa; }
    public Integer getCantidadAnimales() { return cantidadAnimales; }
    public void setCantidadAnimales(Integer cantidadAnimales) { this.cantidadAnimales = cantidadAnimales; }
    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }
    public EtapaRecria getEtapa() { return etapa; }
    public void setEtapa(EtapaRecria etapa) { this.etapa = etapa; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDate fechaSalida) { this.fechaSalida = fechaSalida; }
    public OrigenRecria getOrigen() { return origen; }
    public void setOrigen(OrigenRecria origen) { this.origen = origen; }
    public DestinoRecria getDestino() { return destino; }
    public void setDestino(DestinoRecria destino) { this.destino = destino; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Long getCampanaId() { return campanaId; }
    public void setCampanaId(Long campanaId) { this.campanaId = campanaId; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public List<MuerteRecria> getMuertesRecria() { return muertesRecria; }
    public void setMuertesRecria(List<MuerteRecria> muertesRecria) { this.muertesRecria = muertesRecria; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public String getLoteNombre() { return loteNombre; }
    public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }

    public String getMadreNombre() { return madreNombre; }
    public void setMadreNombre(String madreNombre) { this.madreNombre = madreNombre; }
}
