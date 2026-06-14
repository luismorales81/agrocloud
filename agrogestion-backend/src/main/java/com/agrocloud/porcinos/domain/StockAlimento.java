package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.inventory.domain.Insumo;
import jakarta.persistence.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Stock de alimento (insumos, granos propios, subproductos). Usa cultivoId para no depender de Cultivos.
 */
@Entity
@Table(name = "porcinos_stock_alimento")
@EntityListeners(AuditingEntityListener.class)
public class StockAlimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_id")
    private Insumo insumo;

    @Column(name = "cultivo_id")
    private Long cultivoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoStock tipo;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "cantidad_disponible", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidadDisponible = BigDecimal.ZERO;

    @Column(name = "unidad_medida", nullable = false, length = 50)
    private String unidadMedida = "kg";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @LastModifiedDate
    @Column(name = "fecha_ultima_actualizacion", nullable = false)
    private LocalDateTime fechaUltimaActualizacion;

    public enum TipoStock {
        INSUMO, GRANO_PROPIO, SUBPRODUCTO
    }

    public StockAlimento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Insumo getInsumo() { return insumo; }
    public void setInsumo(Insumo insumo) { this.insumo = insumo; }
    public Long getCultivoId() { return cultivoId; }
    public void setCultivoId(Long cultivoId) { this.cultivoId = cultivoId; }
    public TipoStock getTipo() { return tipo; }
    public void setTipo(TipoStock tipo) { this.tipo = tipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(BigDecimal cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public LocalDateTime getFechaUltimaActualizacion() { return fechaUltimaActualizacion; }
    public void setFechaUltimaActualizacion(LocalDateTime fechaUltimaActualizacion) { this.fechaUltimaActualizacion = fechaUltimaActualizacion; }
}
