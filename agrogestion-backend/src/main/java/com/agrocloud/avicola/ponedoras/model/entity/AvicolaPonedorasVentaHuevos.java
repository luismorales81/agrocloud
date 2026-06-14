package com.agrocloud.avicola.ponedoras.model.entity;

import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasHuevoCategoria;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Venta de huevos asociada a un galpón (trazabilidad de origen).
 */
@Entity
@Table(name = "avicola_ponedoras_venta_huevos")
@EntityListeners(AuditingEntityListener.class)
public class AvicolaPonedorasVentaHuevos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "galpon_id", nullable = false)
    private AvicolaPonedorasGalpon galpon;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_huevo", nullable = false, length = 40)
    private AvicolaPonedorasHuevoCategoria categoriaHuevo;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 12, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "total", precision = 14, scale = 2)
    private BigDecimal total;

    @Column(name = "comprador", length = 180)
    private String comprador;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AvicolaPonedorasGalpon getGalpon() {
        return galpon;
    }

    public void setGalpon(AvicolaPonedorasGalpon galpon) {
        this.galpon = galpon;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public AvicolaPonedorasHuevoCategoria getCategoriaHuevo() {
        return categoriaHuevo;
    }

    public void setCategoriaHuevo(AvicolaPonedorasHuevoCategoria categoriaHuevo) {
        this.categoriaHuevo = categoriaHuevo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getComprador() {
        return comprador;
    }

    public void setComprador(String comprador) {
        this.comprador = comprador;
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
}
