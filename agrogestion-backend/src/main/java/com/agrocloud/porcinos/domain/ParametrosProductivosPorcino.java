package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa los parámetros productivos del módulo porcino
 */
@Entity
@Table(name = "porcinos_parametros_productivos_porcinos")
@EntityListeners(AuditingEntityListener.class)
public class ParametrosProductivosPorcino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dias_promedio_gestacion")
    private Integer diasPromedioGestacion;

    @Column(name = "dias_lactancia")
    private Integer diasLactancia;

    @Column(name = "dias_recria_antes_engorde")
    private Integer diasRecriaAntesEngorde;

    @Column(name = "dias_engorde")
    private Integer diasEngorde;

    @Column(name = "cantidad_maxima_servicios_padrillo_dia")
    private Integer cantidadMaximaServiciosPadrilloDia;

    @Column(name = "tiempo_espera_entre_servicios_horas")
    private Integer tiempoEsperaEntreServiciosHoras;

    @Column(name = "dias_antelacion_alertar_partos")
    private Integer diasAntelacionAlertarPartos;

    @Column(name = "dias_antelacion_alertar_ecografias")
    private Integer diasAntelacionAlertarEcografias;

    @Column(name = "dias_antelacion_alertar_destetes")
    private Integer diasAntelacionAlertarDestetes;

    @Column(name = "dias_antelacion_alertar_pasaje_maternidad")
    private Integer diasAntelacionAlertarPasajeMaternidad;

    @Column(name = "dias_antelacion_alertar_revisiones_sanitarias")
    private Integer diasAntelacionAlertarRevisionesSanitarias;

    @Column(name = "umbral_mortalidad_lactancia_porcentaje", precision = 5, scale = 2)
    private BigDecimal umbralMortalidadLactanciaPorcentaje;

    @Column(name = "umbral_mortalidad_recria_porcentaje", precision = 5, scale = 2)
    private BigDecimal umbralMortalidadRecriaPorcentaje;

    @Column(name = "porcentaje_minimo_prenez_antes_advertencia", precision = 5, scale = 2)
    private BigDecimal porcentajeMinimoPrenezAntesAdvertencia;

    @Column(name = "dias_tolerancia_vencimiento_gestacion")
    private Integer diasToleranciaVencimientoGestacion;

    @Column(name = "dias_control_celo")
    private Integer diasControlCelo;

    @Column(name = "dias_entre_celos")
    private Integer diasEntreCelos;

    @Column(name = "dias_pasaje_maternidad")
    private Integer diasPasajeMaternidad;

    @Column(name = "peso_promedio_nacimiento", precision = 10, scale = 2)
    private BigDecimal pesoPromedioNacimiento;

    @Column(name = "peso_destete_objetivo", precision = 10, scale = 2)
    private BigDecimal pesoDesteteObjetivo;

    @Column(name = "peso_venta_objetivo", precision = 10, scale = 2)
    private BigDecimal pesoVentaObjetivo;

    @Column(name = "lechones_vivos_parto_objetivo", precision = 5, scale = 2)
    private BigDecimal lechonesVivosPartoObjetivo;

    @Column(name = "lechones_destetados_objetivo", precision = 5, scale = 2)
    private BigDecimal lechonesDestetadosObjetivo;

    @Column(name = "partos_madre_anio_objetivo", precision = 5, scale = 2)
    private BigDecimal partosMadreAnioObjetivo;

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

    public ParametrosProductivosPorcino() {}

    public ParametrosProductivosPorcino(Empresa empresa) {
        this.empresa = empresa;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getDiasPromedioGestacion() { return diasPromedioGestacion; }
    public void setDiasPromedioGestacion(Integer diasPromedioGestacion) { this.diasPromedioGestacion = diasPromedioGestacion; }
    public Integer getDiasLactancia() { return diasLactancia; }
    public void setDiasLactancia(Integer diasLactancia) { this.diasLactancia = diasLactancia; }
    public Integer getDiasRecriaAntesEngorde() { return diasRecriaAntesEngorde; }
    public void setDiasRecriaAntesEngorde(Integer diasRecriaAntesEngorde) { this.diasRecriaAntesEngorde = diasRecriaAntesEngorde; }
    public Integer getDiasEngorde() { return diasEngorde; }
    public void setDiasEngorde(Integer diasEngorde) { this.diasEngorde = diasEngorde; }
    public Integer getCantidadMaximaServiciosPadrilloDia() { return cantidadMaximaServiciosPadrilloDia; }
    public void setCantidadMaximaServiciosPadrilloDia(Integer cantidadMaximaServiciosPadrilloDia) { this.cantidadMaximaServiciosPadrilloDia = cantidadMaximaServiciosPadrilloDia; }
    public Integer getTiempoEsperaEntreServiciosHoras() { return tiempoEsperaEntreServiciosHoras; }
    public void setTiempoEsperaEntreServiciosHoras(Integer tiempoEsperaEntreServiciosHoras) { this.tiempoEsperaEntreServiciosHoras = tiempoEsperaEntreServiciosHoras; }
    public Integer getDiasAntelacionAlertarPartos() { return diasAntelacionAlertarPartos; }
    public void setDiasAntelacionAlertarPartos(Integer diasAntelacionAlertarPartos) { this.diasAntelacionAlertarPartos = diasAntelacionAlertarPartos; }
    public Integer getDiasAntelacionAlertarEcografias() { return diasAntelacionAlertarEcografias; }
    public void setDiasAntelacionAlertarEcografias(Integer diasAntelacionAlertarEcografias) { this.diasAntelacionAlertarEcografias = diasAntelacionAlertarEcografias; }
    public Integer getDiasAntelacionAlertarDestetes() { return diasAntelacionAlertarDestetes; }
    public void setDiasAntelacionAlertarDestetes(Integer diasAntelacionAlertarDestetes) { this.diasAntelacionAlertarDestetes = diasAntelacionAlertarDestetes; }
    public Integer getDiasAntelacionAlertarPasajeMaternidad() { return diasAntelacionAlertarPasajeMaternidad; }
    public void setDiasAntelacionAlertarPasajeMaternidad(Integer diasAntelacionAlertarPasajeMaternidad) { this.diasAntelacionAlertarPasajeMaternidad = diasAntelacionAlertarPasajeMaternidad; }
    public Integer getDiasAntelacionAlertarRevisionesSanitarias() { return diasAntelacionAlertarRevisionesSanitarias; }
    public void setDiasAntelacionAlertarRevisionesSanitarias(Integer diasAntelacionAlertarRevisionesSanitarias) { this.diasAntelacionAlertarRevisionesSanitarias = diasAntelacionAlertarRevisionesSanitarias; }
    public BigDecimal getUmbralMortalidadLactanciaPorcentaje() { return umbralMortalidadLactanciaPorcentaje; }
    public void setUmbralMortalidadLactanciaPorcentaje(BigDecimal umbralMortalidadLactanciaPorcentaje) { this.umbralMortalidadLactanciaPorcentaje = umbralMortalidadLactanciaPorcentaje; }
    public BigDecimal getUmbralMortalidadRecriaPorcentaje() { return umbralMortalidadRecriaPorcentaje; }
    public void setUmbralMortalidadRecriaPorcentaje(BigDecimal umbralMortalidadRecriaPorcentaje) { this.umbralMortalidadRecriaPorcentaje = umbralMortalidadRecriaPorcentaje; }
    public BigDecimal getPorcentajeMinimoPrenezAntesAdvertencia() { return porcentajeMinimoPrenezAntesAdvertencia; }
    public void setPorcentajeMinimoPrenezAntesAdvertencia(BigDecimal porcentajeMinimoPrenezAntesAdvertencia) { this.porcentajeMinimoPrenezAntesAdvertencia = porcentajeMinimoPrenezAntesAdvertencia; }
    public Integer getDiasToleranciaVencimientoGestacion() { return diasToleranciaVencimientoGestacion; }
    public void setDiasToleranciaVencimientoGestacion(Integer diasToleranciaVencimientoGestacion) { this.diasToleranciaVencimientoGestacion = diasToleranciaVencimientoGestacion; }
    public Integer getDiasControlCelo() { return diasControlCelo; }
    public void setDiasControlCelo(Integer diasControlCelo) { this.diasControlCelo = diasControlCelo; }
    public Integer getDiasEntreCelos() { return diasEntreCelos; }
    public void setDiasEntreCelos(Integer diasEntreCelos) { this.diasEntreCelos = diasEntreCelos; }
    public Integer getDiasPasajeMaternidad() { return diasPasajeMaternidad; }
    public void setDiasPasajeMaternidad(Integer diasPasajeMaternidad) { this.diasPasajeMaternidad = diasPasajeMaternidad; }
    public BigDecimal getPesoPromedioNacimiento() { return pesoPromedioNacimiento; }
    public void setPesoPromedioNacimiento(BigDecimal pesoPromedioNacimiento) { this.pesoPromedioNacimiento = pesoPromedioNacimiento; }
    public BigDecimal getPesoDesteteObjetivo() { return pesoDesteteObjetivo; }
    public void setPesoDesteteObjetivo(BigDecimal pesoDesteteObjetivo) { this.pesoDesteteObjetivo = pesoDesteteObjetivo; }
    public BigDecimal getPesoVentaObjetivo() { return pesoVentaObjetivo; }
    public void setPesoVentaObjetivo(BigDecimal pesoVentaObjetivo) { this.pesoVentaObjetivo = pesoVentaObjetivo; }
    public BigDecimal getLechonesVivosPartoObjetivo() { return lechonesVivosPartoObjetivo; }
    public void setLechonesVivosPartoObjetivo(BigDecimal lechonesVivosPartoObjetivo) { this.lechonesVivosPartoObjetivo = lechonesVivosPartoObjetivo; }
    public BigDecimal getLechonesDestetadosObjetivo() { return lechonesDestetadosObjetivo; }
    public void setLechonesDestetadosObjetivo(BigDecimal lechonesDestetadosObjetivo) { this.lechonesDestetadosObjetivo = lechonesDestetadosObjetivo; }
    public BigDecimal getPartosMadreAnioObjetivo() { return partosMadreAnioObjetivo; }
    public void setPartosMadreAnioObjetivo(BigDecimal partosMadreAnioObjetivo) { this.partosMadreAnioObjetivo = partosMadreAnioObjetivo; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
