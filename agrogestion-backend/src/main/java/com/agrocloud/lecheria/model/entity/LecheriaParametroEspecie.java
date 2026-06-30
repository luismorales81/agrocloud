package com.agrocloud.lecheria.model.entity;

import com.agrocloud.lecheria.model.enums.LecheriaEspecie;
import jakarta.persistence.*;

@Entity
@Table(name = "lecheria_parametro_especie")
public class LecheriaParametroEspecie {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "especie", length = 20)
    private LecheriaEspecie especie;

    @Column(name = "dias_gestacion", nullable = false)
    private Integer diasGestacion;

    @Column(name = "ordenees_dia", nullable = false)
    private Integer ordeneesDia = 2;

    @Column(name = "umbral_rcs_alerta", nullable = false)
    private Long umbralRcsAlerta;

    @Column(name = "dim_objetivo_secado", nullable = false)
    private Integer dimObjetivoSecado;

    @Column(name = "nombre_visible", nullable = false, length = 80)
    private String nombreVisible;

    public LecheriaEspecie getEspecie() { return especie; }
    public void setEspecie(LecheriaEspecie especie) { this.especie = especie; }
    public Integer getDiasGestacion() { return diasGestacion; }
    public void setDiasGestacion(Integer diasGestacion) { this.diasGestacion = diasGestacion; }
    public Integer getOrdeneesDia() { return ordeneesDia; }
    public void setOrdeneesDia(Integer ordeneesDia) { this.ordeneesDia = ordeneesDia; }
    public Long getUmbralRcsAlerta() { return umbralRcsAlerta; }
    public void setUmbralRcsAlerta(Long umbralRcsAlerta) { this.umbralRcsAlerta = umbralRcsAlerta; }
    public Integer getDimObjetivoSecado() { return dimObjetivoSecado; }
    public void setDimObjetivoSecado(Integer dimObjetivoSecado) { this.dimObjetivoSecado = dimObjetivoSecado; }
    public String getNombreVisible() { return nombreVisible; }
    public void setNombreVisible(String nombreVisible) { this.nombreVisible = nombreVisible; }
}
