package com.agrocloud.cultivos.domain;

import com.agrocloud.core.domain.Empresa;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Plantilla de labor sugerida (días relativos a siembra) generada por el wizard IA.
 */
@Entity
@Table(name = "cultivo_plantilla_labor")
public class PlantillaLabor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_cultivo_id", nullable = false)
    @JsonIgnore
    private TipoCultivo tipoCultivo;

    @NotBlank
    @Size(max = 200)
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @NotBlank
    @Size(max = 50)
    @Column(name = "tipo_labor", nullable = false, length = 50)
    private String tipoLabor;

    @NotNull
    @Column(name = "dia_relativo_siembra", nullable = false)
    private Integer diaRelativoSiembra = 0;

    @Size(max = 100)
    @Column(name = "estado_esperado_nombre", length = 100)
    private String estadoEsperadoNombre;

    @Column(name = "duracion_estimada_hs")
    private Integer duracionEstimadaHs;

    @Size(max = 500)
    @Column(name = "insumos_sugeridos", length = 500)
    private String insumosSugeridos;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    public PlantillaLabor() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public TipoCultivo getTipoCultivo() { return tipoCultivo; }
    public void setTipoCultivo(TipoCultivo tipoCultivo) { this.tipoCultivo = tipoCultivo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipoLabor() { return tipoLabor; }
    public void setTipoLabor(String tipoLabor) { this.tipoLabor = tipoLabor; }
    public Integer getDiaRelativoSiembra() { return diaRelativoSiembra; }
    public void setDiaRelativoSiembra(Integer diaRelativoSiembra) { this.diaRelativoSiembra = diaRelativoSiembra; }
    public String getEstadoEsperadoNombre() { return estadoEsperadoNombre; }
    public void setEstadoEsperadoNombre(String estadoEsperadoNombre) { this.estadoEsperadoNombre = estadoEsperadoNombre; }
    public Integer getDuracionEstimadaHs() { return duracionEstimadaHs; }
    public void setDuracionEstimadaHs(Integer duracionEstimadaHs) { this.duracionEstimadaHs = duracionEstimadaHs; }
    public String getInsumosSugeridos() { return insumosSugeridos; }
    public void setInsumosSugeridos(String insumosSugeridos) { this.insumosSugeridos = insumosSugeridos; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
