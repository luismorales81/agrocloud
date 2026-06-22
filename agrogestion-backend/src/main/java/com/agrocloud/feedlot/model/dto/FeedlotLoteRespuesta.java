package com.agrocloud.feedlot.model.dto;

import com.agrocloud.feedlot.model.enums.FeedlotCorralEstado;
import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import com.agrocloud.feedlot.model.enums.FeedlotTipoTenencia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FeedlotLoteRespuesta {

    private Long id;
    private Long empresaId;
    private Long corralId;
    private String corralNombre;
    private FeedlotCorralEstado corralEstado;
    private Long establecimientoId;
    private String establecimientoNombre;
    private Long campanaId;
    private String nombre;
    private Long categoriaId;
    private String categoriaNombre;
    private Long razaId;
    private String razaNombre;
    private Long proveedorId;
    private String proveedorNombre;
    private FeedlotTipoTenencia tipoTenencia;
    private LocalDate fechaIngreso;
    private LocalDate fechaCierre;
    private Integer cabezasInicial;
    private Integer cabezasActuales;
    private BigDecimal pesoPromedioIngresoKg;
    private BigDecimal precioCompraKg;
    private BigDecimal costoHoteleriaDia;
    private Long dietaId;
    private String dietaNombre;
    private FeedlotLoteEstado estado;
    private String observaciones;
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

    public Long getCorralId() {
        return corralId;
    }

    public void setCorralId(Long corralId) {
        this.corralId = corralId;
    }

    public String getCorralNombre() {
        return corralNombre;
    }

    public void setCorralNombre(String corralNombre) {
        this.corralNombre = corralNombre;
    }

    public FeedlotCorralEstado getCorralEstado() {
        return corralEstado;
    }

    public void setCorralEstado(FeedlotCorralEstado corralEstado) {
        this.corralEstado = corralEstado;
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

    public Long getCampanaId() {
        return campanaId;
    }

    public void setCampanaId(Long campanaId) {
        this.campanaId = campanaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
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

    public Long getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(Long proveedorId) {
        this.proveedorId = proveedorId;
    }

    public String getProveedorNombre() {
        return proveedorNombre;
    }

    public void setProveedorNombre(String proveedorNombre) {
        this.proveedorNombre = proveedorNombre;
    }

    public FeedlotTipoTenencia getTipoTenencia() {
        return tipoTenencia;
    }

    public void setTipoTenencia(FeedlotTipoTenencia tipoTenencia) {
        this.tipoTenencia = tipoTenencia;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public Integer getCabezasInicial() {
        return cabezasInicial;
    }

    public void setCabezasInicial(Integer cabezasInicial) {
        this.cabezasInicial = cabezasInicial;
    }

    public Integer getCabezasActuales() {
        return cabezasActuales;
    }

    public void setCabezasActuales(Integer cabezasActuales) {
        this.cabezasActuales = cabezasActuales;
    }

    public BigDecimal getPesoPromedioIngresoKg() {
        return pesoPromedioIngresoKg;
    }

    public void setPesoPromedioIngresoKg(BigDecimal pesoPromedioIngresoKg) {
        this.pesoPromedioIngresoKg = pesoPromedioIngresoKg;
    }

    public BigDecimal getPrecioCompraKg() {
        return precioCompraKg;
    }

    public void setPrecioCompraKg(BigDecimal precioCompraKg) {
        this.precioCompraKg = precioCompraKg;
    }

    public BigDecimal getCostoHoteleriaDia() {
        return costoHoteleriaDia;
    }

    public void setCostoHoteleriaDia(BigDecimal costoHoteleriaDia) {
        this.costoHoteleriaDia = costoHoteleriaDia;
    }

    public Long getDietaId() {
        return dietaId;
    }

    public void setDietaId(Long dietaId) {
        this.dietaId = dietaId;
    }

    public String getDietaNombre() {
        return dietaNombre;
    }

    public void setDietaNombre(String dietaNombre) {
        this.dietaNombre = dietaNombre;
    }

    public FeedlotLoteEstado getEstado() {
        return estado;
    }

    public void setEstado(FeedlotLoteEstado estado) {
        this.estado = estado;
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
}
