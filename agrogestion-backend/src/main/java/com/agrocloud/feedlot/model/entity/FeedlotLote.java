package com.agrocloud.feedlot.model.entity;

import com.agrocloud.feedlot.model.enums.FeedlotLoteEstado;
import com.agrocloud.feedlot.model.enums.FeedlotTipoTenencia;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedlot_lote")
@EntityListeners(AuditingEntityListener.class)
public class FeedlotLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corral_id", nullable = false)
    private FeedlotCorral corral;

    @Column(name = "campana_id", nullable = false)
    private Long campanaId;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private FeedlotCategoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raza_id")
    private FeedlotRaza raza;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private FeedlotProveedorOrigen proveedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tenencia", nullable = false, length = 20)
    private FeedlotTipoTenencia tipoTenencia = FeedlotTipoTenencia.PROPIO;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "fecha_cierre")
    private LocalDate fechaCierre;

    @Column(name = "cabezas_inicial", nullable = false)
    private Integer cabezasInicial;

    @Column(name = "cabezas_actuales", nullable = false)
    private Integer cabezasActuales;

    @Column(name = "peso_promedio_ingreso_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoPromedioIngresoKg;

    @Column(name = "precio_compra_kg", precision = 12, scale = 2)
    private BigDecimal precioCompraKg;

    @Column(name = "costo_hoteleria_dia", precision = 12, scale = 2)
    private BigDecimal costoHoteleriaDia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private FeedlotLoteEstado estado = FeedlotLoteEstado.ACTIVO;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dieta_id")
    private FeedlotDieta dieta;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
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

    public FeedlotCorral getCorral() {
        return corral;
    }

    public void setCorral(FeedlotCorral corral) {
        this.corral = corral;
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

    public FeedlotCategoria getCategoria() {
        return categoria;
    }

    public void setCategoria(FeedlotCategoria categoria) {
        this.categoria = categoria;
    }

    public FeedlotRaza getRaza() {
        return raza;
    }

    public void setRaza(FeedlotRaza raza) {
        this.raza = raza;
    }

    public FeedlotProveedorOrigen getProveedor() {
        return proveedor;
    }

    public void setProveedor(FeedlotProveedorOrigen proveedor) {
        this.proveedor = proveedor;
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

    public FeedlotDieta getDieta() {
        return dieta;
    }

    public void setDieta(FeedlotDieta dieta) {
        this.dieta = dieta;
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
