package com.agrocloud.lecheria.model.dto;

import com.agrocloud.lecheria.model.enums.LecheriaEspecie;
import com.agrocloud.lecheria.model.enums.LecheriaEstadoAnimal;
import com.agrocloud.lecheria.model.enums.LecheriaSexoAnimal;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LecheriaAnimalRespuesta {

    private Long id;
    private Long empresaId;
    private String identificacion;
    private LecheriaEspecie especie;
    private LecheriaSexoAnimal sexo;
    private LecheriaEstadoAnimal estado;
    private Long razaId;
    private String razaNombre;
    private Long rodeoId;
    private String rodeoNombre;
    private Long campanaId;
    private LocalDate fechaNacimiento;
    private LocalDate fechaIngreso;
    private LocalDate fechaBaja;
    private Boolean activo;
    private String observaciones;
    private LecheriaLactanciaRespuesta lactanciaActiva;
    private Double climaLatitud;
    private Double climaLongitud;
    private Integer dim;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public LecheriaEspecie getEspecie() {
        return especie;
    }

    public void setEspecie(LecheriaEspecie especie) {
        this.especie = especie;
    }

    public LecheriaSexoAnimal getSexo() {
        return sexo;
    }

    public void setSexo(LecheriaSexoAnimal sexo) {
        this.sexo = sexo;
    }

    public LecheriaEstadoAnimal getEstado() {
        return estado;
    }

    public void setEstado(LecheriaEstadoAnimal estado) {
        this.estado = estado;
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

    public Long getRodeoId() {
        return rodeoId;
    }

    public void setRodeoId(Long rodeoId) {
        this.rodeoId = rodeoId;
    }

    public String getRodeoNombre() {
        return rodeoNombre;
    }

    public void setRodeoNombre(String rodeoNombre) {
        this.rodeoNombre = rodeoNombre;
    }

    public Long getCampanaId() {
        return campanaId;
    }

    public void setCampanaId(Long campanaId) {
        this.campanaId = campanaId;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalDate getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(LocalDate fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LecheriaLactanciaRespuesta getLactanciaActiva() {
        return lactanciaActiva;
    }

    public void setLactanciaActiva(LecheriaLactanciaRespuesta lactanciaActiva) {
        this.lactanciaActiva = lactanciaActiva;
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

    public Integer getDim() {
        return dim;
    }

    public void setDim(Integer dim) {
        this.dim = dim;
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
}
