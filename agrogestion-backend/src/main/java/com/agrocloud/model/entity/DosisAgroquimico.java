package com.agrocloud.model.entity;

import com.agrocloud.model.enums.FormaAplicacion;
import com.agrocloud.model.enums.TipoAplicacion;
import com.agrocloud.model.enums.UnidadDosis;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad simplificada para dosis de agroquímicos por tipo de aplicación
 * Relacionada con Insumo (no con la tabla separada de agroquímicos)
 */
@Entity
@Table(name = "dosis_insumos")
@EntityListeners(AuditingEntityListener.class)
public class DosisAgroquimico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El tipo de aplicación es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_aplicacion", nullable = false)
    private TipoAplicacion tipoAplicacion;

    @NotNull(message = "La forma de aplicación es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "forma_aplicacion", nullable = false)
    private FormaAplicacion formaAplicacion;

    @NotNull(message = "La dosis recomendada es obligatoria")
    @DecimalMin(value = "0.01", message = "La dosis recomendada debe ser mayor que 0")
    @Column(name = "dosis_recomendada_por_ha", nullable = false)
    private Double dosisRecomendadaPorHa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Getters y Setters
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoAplicacion getTipoAplicacion() {
        return tipoAplicacion;
    }

    public void setTipoAplicacion(TipoAplicacion tipoAplicacion) {
        this.tipoAplicacion = tipoAplicacion;
    }

    public FormaAplicacion getFormaAplicacion() {
        return formaAplicacion;
    }

    public void setFormaAplicacion(FormaAplicacion formaAplicacion) {
        this.formaAplicacion = formaAplicacion;
    }

    /**
     * Obtiene la unidad de dosis derivada desde el insumo relacionado
     * Mapea la unidad de medida del insumo a UnidadDosis
     */
    public UnidadDosis getUnidad() {
        if (insumo == null || insumo.getUnidadMedida() == null) {
            return null;
        }
        return mapearUnidadMedidaAUnidadDosis(insumo.getUnidadMedida());
    }

    /**
     * Helper para mapear unidadMedida del insumo a UnidadDosis
     */
    private UnidadDosis mapearUnidadMedidaAUnidadDosis(String unidadMedida) {
        if (unidadMedida == null) {
            return null;
        }
        String unidadLower = unidadMedida.toLowerCase().trim();
        
        // Mapeo común de unidades a UnidadDosis
        if (unidadLower.equals("l") || unidadLower.equals("litro") || 
            unidadLower.equals("litros") || unidadLower.equals("lts") ||
            unidadLower.equals("lt") || unidadLower.equals("l.")) {
            return UnidadDosis.L_HA;
        } else if (unidadLower.equals("kg") || unidadLower.equals("kilogramo") ||
                   unidadLower.equals("kilogramos") || unidadLower.equals("kgs")) {
            return UnidadDosis.KG_HA;
        } else if (unidadLower.equals("ml") || unidadLower.equals("mililitro") ||
                   unidadLower.equals("mililitros") || unidadLower.equals("mls")) {
            return UnidadDosis.ML_HA;
        }
        
        // Valor por defecto si no se encuentra mapeo
        return UnidadDosis.L_HA;
    }

    public Double getDosisRecomendadaPorHa() {
        return dosisRecomendadaPorHa;
    }

    public void setDosisRecomendadaPorHa(Double dosisRecomendadaPorHa) {
        this.dosisRecomendadaPorHa = dosisRecomendadaPorHa;
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}

