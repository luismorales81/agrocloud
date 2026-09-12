package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad que representa los parámetros generales del establecimiento porcino
 */
@Entity
@Table(name = "porcinos_legacy_parametros_establecimiento_porcinos")
@EntityListeners(AuditingEntityListener.class)
public class ParametrosEstablecimientoPorcino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_establecimiento", nullable = false, length = 200)
    private String nombreEstablecimiento;

    @Column(name = "provincia", length = 100)
    private String provincia;

    @Column(name = "localidad", length = 100)
    private String localidad;

    @Column(name = "razon_social", length = 200)
    private String razonSocial;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_manejo", nullable = false)
    private UnidadManejo unidadManejo;

    @Column(name = "maximo_madres")
    private Integer maximoMadres;

    @Column(name = "maximo_padrillos")
    private Integer maximoPadrillos;

    @Column(name = "maxima_capacidad_recria_engorde")
    private Integer maximaCapacidadRecriaEngorde;

    @Column(name = "categorias_habilitadas", length = 500)
    private String categoriasHabilitadas;

    @Column(name = "ciclos_productivos_propios", columnDefinition = "TEXT")
    private String ciclosProductivosPropios;

    @Column(name = "realiza_faena", nullable = false)
    private Boolean realizaFaena = false;

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

    public enum UnidadManejo {
        LOTES, GRUPOS, ANIMALES_INDIVIDUALES
    }

    public ParametrosEstablecimientoPorcino() {}

    public ParametrosEstablecimientoPorcino(String nombreEstablecimiento, UnidadManejo unidadManejo, Empresa empresa) {
        this.nombreEstablecimiento = nombreEstablecimiento;
        this.unidadManejo = unidadManejo;
        this.empresa = empresa;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreEstablecimiento() { return nombreEstablecimiento; }
    public void setNombreEstablecimiento(String nombreEstablecimiento) { this.nombreEstablecimiento = nombreEstablecimiento; }
    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public UnidadManejo getUnidadManejo() { return unidadManejo; }
    public void setUnidadManejo(UnidadManejo unidadManejo) { this.unidadManejo = unidadManejo; }
    public Integer getMaximoMadres() { return maximoMadres; }
    public void setMaximoMadres(Integer maximoMadres) { this.maximoMadres = maximoMadres; }
    public Integer getMaximoPadrillos() { return maximoPadrillos; }
    public void setMaximoPadrillos(Integer maximoPadrillos) { this.maximoPadrillos = maximoPadrillos; }
    public Integer getMaximaCapacidadRecriaEngorde() { return maximaCapacidadRecriaEngorde; }
    public void setMaximaCapacidadRecriaEngorde(Integer maximaCapacidadRecriaEngorde) { this.maximaCapacidadRecriaEngorde = maximaCapacidadRecriaEngorde; }
    public String getCategoriasHabilitadas() { return categoriasHabilitadas; }
    public void setCategoriasHabilitadas(String categoriasHabilitadas) { this.categoriasHabilitadas = categoriasHabilitadas; }
    public String getCiclosProductivosPropios() { return ciclosProductivosPropios; }
    public void setCiclosProductivosPropios(String ciclosProductivosPropios) { this.ciclosProductivosPropios = ciclosProductivosPropios; }
    public Boolean getRealizaFaena() { return realizaFaena; }
    public void setRealizaFaena(Boolean realizaFaena) { this.realizaFaena = realizaFaena; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
