package com.agrocloud.feedlot.model.entity;

import com.agrocloud.feedlot.model.enums.FeedlotEstadoAnimal;
import com.agrocloud.feedlot.model.enums.FeedlotSexoAnimal;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedlot_animal")
@EntityListeners(AuditingEntityListener.class)
public class FeedlotAnimal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private FeedlotLote lote;

    @Column(name = "eid", length = 15)
    private String eid;

    @Column(name = "caravana_visual", length = 40)
    private String caravanaVisual;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", length = 20)
    private FeedlotSexoAnimal sexo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private FeedlotCategoria categoria;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDate fechaAlta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private FeedlotEstadoAnimal estado = FeedlotEstadoAnimal.ACTIVO;

    @Column(name = "fecha_baja")
    private LocalDate fechaBaja;

    @Column(name = "peso_ingreso_kg", precision = 10, scale = 2)
    private BigDecimal pesoIngresoKg;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public FeedlotLote getLote() { return lote; }
    public void setLote(FeedlotLote lote) { this.lote = lote; }
    public String getEid() { return eid; }
    public void setEid(String eid) { this.eid = eid; }
    public String getCaravanaVisual() { return caravanaVisual; }
    public void setCaravanaVisual(String caravanaVisual) { this.caravanaVisual = caravanaVisual; }
    public FeedlotSexoAnimal getSexo() { return sexo; }
    public void setSexo(FeedlotSexoAnimal sexo) { this.sexo = sexo; }
    public FeedlotCategoria getCategoria() { return categoria; }
    public void setCategoria(FeedlotCategoria categoria) { this.categoria = categoria; }
    public LocalDate getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDate fechaAlta) { this.fechaAlta = fechaAlta; }
    public FeedlotEstadoAnimal getEstado() { return estado; }
    public void setEstado(FeedlotEstadoAnimal estado) { this.estado = estado; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
    public BigDecimal getPesoIngresoKg() { return pesoIngresoKg; }
    public void setPesoIngresoKg(BigDecimal pesoIngresoKg) { this.pesoIngresoKg = pesoIngresoKg; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
