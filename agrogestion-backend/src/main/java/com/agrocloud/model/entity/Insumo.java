package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa los insumos en el sistema (fertilizantes, agroquímicos, semillas).
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "insumos")
@EntityListeners(AuditingEntityListener.class)
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del insumo es obligatorio")
    @Size(min = 2, max = 200, message = "El nombre del insumo debe tener entre 2 y 200 caracteres")
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @NotNull(message = "El tipo de insumo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoInsumo tipo;

    @NotBlank(message = "La unidad de medida es obligatoria")
    @Size(max = 50, message = "La unidad de medida no puede exceder 50 caracteres")
    @Column(name = "unidad_medida", nullable = false, length = 50)
    private String unidadMedida;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser un valor positivo")
    @Column(name = "precio_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Positive(message = "El stock mínimo debe ser un valor positivo")
    @Column(name = "stock_minimo", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @NotNull(message = "El stock actual es obligatorio")
    @Column(name = "stock_actual", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockActual = BigDecimal.ZERO;

    @Size(max = 200, message = "El proveedor no puede exceder 200 caracteres")
    @Column(name = "proveedor", length = 200)
    private String proveedor;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relación con el usuario propietario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // Relación con la empresa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    // Relación con las dosis de insumos (tabla dosis_insumos)
    @OneToMany(mappedBy = "insumo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private java.util.List<DosisAgroquimico> dosisInsumos;

    // CAMPOS ESPECÍFICOS PARA AGROQUÍMICOS
    @Size(max = 200, message = "El principio activo no puede exceder 200 caracteres")
    @Column(name = "principio_activo", length = 200)
    private String principioActivo;

    @Size(max = 100, message = "La concentración no puede exceder 100 caracteres")
    @Column(name = "concentracion", length = 100)
    private String concentracion;

    @Size(max = 100, message = "La clase química no puede exceder 100 caracteres")
    @Column(name = "clase_quimica", length = 100)
    private String claseQuimica;

    @Size(max = 50, message = "La categoría toxicológica no puede exceder 50 caracteres")
    @Column(name = "categoria_toxicologica", length = 50)
    private String categoriaToxicologica;

    @Column(name = "periodo_carencia_dias")
    private Integer periodoCarenciaDias;

    @Column(name = "dosis_minima_por_ha", precision = 10, scale = 2)
    private BigDecimal dosisMinimaPorHa;

    @Column(name = "dosis_maxima_por_ha", precision = 10, scale = 2)
    private BigDecimal dosisMaximaPorHa;

    @Size(max = 50, message = "La unidad de dosis no puede exceder 50 caracteres")
    @Column(name = "unidad_dosis", length = 50)
    private String unidadDosis;

    // Relación con dosis por tipo de aplicación para agroquímicos - ELIMINADA (simplificada)

    // Enums
    public enum TipoInsumo {
        FERTILIZANTE, HERBICIDA, FUNGICIDA, INSECTICIDA, SEMILLA, COMBUSTIBLE, LUBRICANTE, REPUESTO, HERRAMIENTA, OTROS
    }

    // Constructors
    public Insumo() {}

    public Insumo(String nombre, TipoInsumo tipo, String unidadMedida, BigDecimal precioUnitario) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.unidadMedida = unidadMedida;
        this.precioUnitario = precioUnitario;
        this.activo = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoInsumo getTipo() {
        return tipo;
    }

    public void setTipo(TipoInsumo tipo) {
        this.tipo = tipo;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getStockActual() {
        return stockActual;
    }

    public void setStockActual(BigDecimal stockActual) {
        this.stockActual = stockActual;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }
    
    // Métodos faltantes para los tests
    public BigDecimal getStockDisponible() {
        return this.stockActual;
    }
    
    public void setStockDisponible(BigDecimal stockDisponible) {
        this.stockActual = stockDisponible;
    }
    
    public void setUsuario(User usuario) {
        this.user = usuario;
    }
    
    public User getUsuario() {
        return this.user;
    }

    // Helper methods
    public boolean isStockBajo() {
        return stockActual.compareTo(stockMinimo) <= 0;
    }

    public boolean isVencido() {
        return fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
    }

    public boolean estaPorVencer() {
        if (fechaVencimiento == null) return false;
        LocalDate hoy = LocalDate.now();
        LocalDate proximoMes = hoy.plusMonths(1);
        return fechaVencimiento.isBefore(proximoMes) && !fechaVencimiento.isBefore(hoy);
    }

    public java.util.List<DosisAgroquimico> getDosisInsumos() {
        return dosisInsumos;
    }

    public void setDosisInsumos(java.util.List<DosisAgroquimico> dosisInsumos) {
        this.dosisInsumos = dosisInsumos;
    }

    // Getters y Setters para campos específicos de agroquímicos
    public String getPrincipioActivo() {
        return principioActivo;
    }

    public void setPrincipioActivo(String principioActivo) {
        this.principioActivo = principioActivo;
    }

    public String getConcentracion() {
        return concentracion;
    }

    public void setConcentracion(String concentracion) {
        this.concentracion = concentracion;
    }

    public String getClaseQuimica() {
        return claseQuimica;
    }

    public void setClaseQuimica(String claseQuimica) {
        this.claseQuimica = claseQuimica;
    }

    public String getCategoriaToxicologica() {
        return categoriaToxicologica;
    }

    public void setCategoriaToxicologica(String categoriaToxicologica) {
        this.categoriaToxicologica = categoriaToxicologica;
    }

    public Integer getPeriodoCarenciaDias() {
        return periodoCarenciaDias;
    }

    public void setPeriodoCarenciaDias(Integer periodoCarenciaDias) {
        this.periodoCarenciaDias = periodoCarenciaDias;
    }

    public BigDecimal getDosisMinimaPorHa() {
        return dosisMinimaPorHa;
    }

    public void setDosisMinimaPorHa(BigDecimal dosisMinimaPorHa) {
        this.dosisMinimaPorHa = dosisMinimaPorHa;
    }

    public BigDecimal getDosisMaximaPorHa() {
        return dosisMaximaPorHa;
    }

    public void setDosisMaximaPorHa(BigDecimal dosisMaximaPorHa) {
        this.dosisMaximaPorHa = dosisMaximaPorHa;
    }

    public String getUnidadDosis() {
        return unidadDosis;
    }

    public void setUnidadDosis(String unidadDosis) {
        this.unidadDosis = unidadDosis;
    }

    // Métodos de dosis eliminados - simplificados

    // Métodos helper para agroquímicos
    public boolean esAgroquimico() {
        return tipo == TipoInsumo.HERBICIDA || tipo == TipoInsumo.FUNGICIDA || 
               tipo == TipoInsumo.INSECTICIDA || tipo == TipoInsumo.FERTILIZANTE;
    }

    public boolean tienePropiedadesAgroquimicas() {
        return principioActivo != null && !principioActivo.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "Insumo{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", tipo=" + tipo +
                ", stockActual=" + stockActual +
                ", activo=" + activo +
                '}';
    }
}
