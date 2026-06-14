package com.agrocloud.core.inventory.domain;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un insumo compuesto (receta/formula)
 * Un insumo compuesto está hecho de otros insumos, cultivos (granos propios), u otros insumos compuestos
 * 
 * Ejemplos:
 * - Porcinos: "Ración Gestación" = 60% maíz + 30% soja + 8% núcleo gestación + 2% vitaminas
 * - Cultivos: "Mezcla Fertilizante NPK" = 50% Urea + 30% Superfosfato + 20% Cloruro de Potasio
 * 
 * Compartido entre módulos de Cultivos y Porcinos
 */
@Entity
@Table(name = "insumos_compuestos")
@EntityListeners(AuditingEntityListener.class)
public class InsumoCompuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del insumo compuesto es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    /**
     * Tipo de insumo compuesto (RACION, NUCLEO, MEZCLA, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoInsumoCompuesto tipo;

    /**
     * Unidad de medida del insumo compuesto (kg, toneladas, etc.)
     */
    @NotBlank(message = "La unidad de medida es obligatoria")
    @Size(max = 50, message = "La unidad de medida no puede exceder 50 caracteres")
    @Column(name = "unidad_medida", nullable = false, length = 50)
    private String unidadMedida = "kg";

    /**
     * Rendimiento: cantidad de insumo compuesto que se obtiene por unidad de componentes
     * Ejemplo: 100 kg de componentes producen 100 kg de ración (rendimiento = 1.0)
     * O 100 kg de componentes producen 95 kg de ración (rendimiento = 0.95) por mermas
     */
    @Column(name = "rendimiento", precision = 5, scale = 4, nullable = false)
    private BigDecimal rendimiento = BigDecimal.ONE;

    /**
     * Costo unitario calculado automáticamente basado en los componentes
     * Se actualiza cuando cambian los componentes o sus precios
     */
    @Column(name = "costo_unitario_calculado", precision = 15, scale = 2)
    private BigDecimal costoUnitarioCalculado;

    /**
     * Costo unitario manual (opcional, si se quiere sobreescribir el calculado)
     */
    @Column(name = "costo_unitario_manual", precision = 15, scale = 2)
    private BigDecimal costoUnitarioManual;

    /**
     * Stock disponible del insumo compuesto (se calcula o se registra manualmente)
     */
    @Column(name = "stock_actual", precision = 10, scale = 2, nullable = false)
    private BigDecimal stockActual = BigDecimal.ZERO;

    /**
     * Stock mínimo para alertas
     */
    @Column(name = "stock_minimo", precision = 10, scale = 2, nullable = false)
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private User usuario;

    /**
     * Componentes que forman este insumo compuesto
     */
    @OneToMany(mappedBy = "insumoCompuesto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ComponenteInsumoCompuesto> componentes = new ArrayList<>();

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public enum TipoInsumoCompuesto {
        RACION,           // Ración completa (maíz + soja + núcleo)
        NUCLEO,           // Núcleo vitamínico-mineral
        MEZCLA,           // Mezcla simple de granos
        PREMEZCLA,        // Premezcla de aditivos
        OTRO
    }

    // Constructors
    public InsumoCompuesto() {}

    public InsumoCompuesto(String nombre, TipoInsumoCompuesto tipo, Empresa empresa, User usuario) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.empresa = empresa;
        this.usuario = usuario;
        this.activo = true;
        this.rendimiento = BigDecimal.ONE;
        this.stockActual = BigDecimal.ZERO;
        this.stockMinimo = BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public TipoInsumoCompuesto getTipo() { return tipo; }
    public void setTipo(TipoInsumoCompuesto tipo) { this.tipo = tipo; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public BigDecimal getRendimiento() { return rendimiento; }
    public void setRendimiento(BigDecimal rendimiento) { this.rendimiento = rendimiento; }
    public BigDecimal getCostoUnitarioCalculado() { return costoUnitarioCalculado; }
    public void setCostoUnitarioCalculado(BigDecimal costoUnitarioCalculado) { this.costoUnitarioCalculado = costoUnitarioCalculado; }
    public BigDecimal getCostoUnitarioManual() { return costoUnitarioManual; }
    public void setCostoUnitarioManual(BigDecimal costoUnitarioManual) { this.costoUnitarioManual = costoUnitarioManual; }
    public BigDecimal getStockActual() { return stockActual; }
    public void setStockActual(BigDecimal stockActual) { this.stockActual = stockActual; }
    public BigDecimal getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(BigDecimal stockMinimo) { this.stockMinimo = stockMinimo; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public List<ComponenteInsumoCompuesto> getComponentes() { return componentes; }
    public void setComponentes(List<ComponenteInsumoCompuesto> componentes) { this.componentes = componentes; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    /**
     * Obtener el costo unitario a usar (manual si existe, sino calculado)
     */
    public BigDecimal getCostoUnitario() {
        return costoUnitarioManual != null ? costoUnitarioManual : costoUnitarioCalculado;
    }

    /**
     * Verificar si el stock está bajo
     */
    public boolean isStockBajo() {
        return stockActual.compareTo(stockMinimo) <= 0;
    }

    /**
     * Calcular el costo total de los componentes para una cantidad dada
     */
    public BigDecimal calcularCostoTotal(BigDecimal cantidad) {
        BigDecimal costoUnitario = getCostoUnitario();
        return costoUnitario != null ? costoUnitario.multiply(cantidad) : BigDecimal.ZERO;
    }
}





