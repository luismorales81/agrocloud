package com.agrocloud.avicola.crianza.model.dto;

import com.agrocloud.avicola.crianza.model.enums.AvicolaEspecie;
import com.agrocloud.avicola.crianza.model.enums.AvicolaLoteEstado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Salida de lote avícola.
 */
public class AvicolaLoteRespuesta {

    private Long id;
    private Long empresaId;
    private Long establecimientoId;
    private String establecimientoNombre;
    private Long razaId;
    private String razaNombre;
    private String nombre;
    private AvicolaEspecie especie;
    private String origen;
    private LocalDate fechaIngreso;
    private Integer cantidadInicial;
    private Integer cantidadAnimales;
    private BigDecimal pesoPromedioIngreso;
    private AvicolaLoteEstado estado;
    private LocalDate fechaSalida;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** Centro del polígono del establecimiento (clima). Nulos si no hay coordenadas. */
    private Double climaLatitud;
    private Double climaLongitud;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public Long getEstablecimientoId() {
        return establecimientoId;
    }

    public void setEstablecimientoId(Long establecimientoId) {
        this.establecimientoId = establecimientoId;
    }

    public String getEstablecimientoNombre() {
        return establecimientoNombre;
    }

    public void setEstablecimientoNombre(String establecimientoNombre) {
        this.establecimientoNombre = establecimientoNombre;
    }

    public Long getRazaId() {
        return razaId;
    }

    public void setRazaId(Long razaId) {
        this.razaId = razaId;
    }

    public String getRazaNombre() {
        return razaNombre;
    }

    public void setRazaNombre(String razaNombre) {
        this.razaNombre = razaNombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public AvicolaEspecie getEspecie() {
        return especie;
    }

    public void setEspecie(AvicolaEspecie especie) {
        this.especie = especie;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Integer getCantidadInicial() {
        return cantidadInicial;
    }

    public void setCantidadInicial(Integer cantidadInicial) {
        this.cantidadInicial = cantidadInicial;
    }

    public Integer getCantidadAnimales() {
        return cantidadAnimales;
    }

    public void setCantidadAnimales(Integer cantidadAnimales) {
        this.cantidadAnimales = cantidadAnimales;
    }

    public BigDecimal getPesoPromedioIngreso() {
        return pesoPromedioIngreso;
    }

    public void setPesoPromedioIngreso(BigDecimal pesoPromedioIngreso) {
        this.pesoPromedioIngreso = pesoPromedioIngreso;
    }

    public AvicolaLoteEstado getEstado() {
        return estado;
    }

    public void setEstado(AvicolaLoteEstado estado) {
        this.estado = estado;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getClimaLatitud() {
        return climaLatitud;
    }

    public void setClimaLatitud(Double climaLatitud) {
        this.climaLatitud = climaLatitud;
    }

    public Double getClimaLongitud() {
        return climaLongitud;
    }

    public void setClimaLongitud(Double climaLongitud) {
        this.climaLongitud = climaLongitud;
    }
}
