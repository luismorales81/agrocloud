package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad para gestionar la maquinaria del sistema agropecuario
 */
@Entity
@Table(name = "maquinaria")
public class Maquinaria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;
    
    @Column(name = "marca", length = 100)
    private String marca;
    
    @Column(name = "modelo", length = 100)
    private String modelo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoMaquinaria estado;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "anio_fabricacion")
    private Integer anioFabricacion;
    
    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
    
    // Campos para reportes de costos
    @Column(name = "kilometros_uso", precision = 10, scale = 2)
    private BigDecimal kilometrosUso = BigDecimal.ZERO;
    
    @Column(name = "costo_por_hora", precision = 10, scale = 2)
    private BigDecimal costoPorHora = BigDecimal.ZERO;
    
    @Column(name = "kilometros_mantenimiento_intervalo")
    private Integer kilometrosMantenimientoIntervalo = 5000;
    
    @Column(name = "ultimo_mantenimiento_kilometros", precision = 10, scale = 2)
    private BigDecimal ultimoMantenimientoKilometros = BigDecimal.ZERO;
    
    @Column(name = "rendimiento_combustible", precision = 8, scale = 2)
    private BigDecimal rendimientoCombustible = BigDecimal.ZERO;
    
    @Column(name = "unidad_rendimiento", length = 20)
    private String unidadRendimiento = "km/L";
    
    @Column(name = "costo_combustible_por_litro", precision = 8, scale = 2)
    private BigDecimal costoCombustiblePorLitro = BigDecimal.ZERO;
    
    @Column(name = "valor_actual", precision = 12, scale = 2)
    private BigDecimal valorActual = BigDecimal.ZERO;
    
    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;
    
    @Column(name = "tipo", length = 50)
    private String tipo;
    
    // Enum para estados de maquinaria
    public enum EstadoMaquinaria {
        ACTIVA,
        OPERATIVA,
        FUERA_DE_SERVICIO,
        EN_MANTENIMIENTO,
        RETIRADA,
        DISPONIBLE,
        EN_USO
    }
    
    // Constructor por defecto
    public Maquinaria() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = EstadoMaquinaria.ACTIVA;
        this.activo = true;
    }
    
    // Constructor con campos obligatorios
    public Maquinaria(String nombre, User user) {
        this();
        this.nombre = nombre;
        this.user = user;
    }
    
    // Getters y Setters
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
    
    public String getMarca() {
        return marca;
    }
    
    public void setMarca(String marca) {
        this.marca = marca;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    
    public EstadoMaquinaria getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoMaquinaria estado) {
        this.estado = estado;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public Integer getAnioFabricacion() {
        return anioFabricacion;
    }
    
    public void setAnioFabricacion(Integer anioFabricacion) {
        this.anioFabricacion = anioFabricacion;
    }
    
    public String getNumeroSerie() {
        return numeroSerie;
    }
    
    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
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
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    // Getters y Setters para campos de costos
    public BigDecimal getKilometrosUso() {
        return kilometrosUso;
    }
    
    public void setKilometrosUso(BigDecimal kilometrosUso) {
        this.kilometrosUso = kilometrosUso;
    }
    
    public BigDecimal getCostoPorHora() {
        return costoPorHora;
    }
    
    public void setCostoPorHora(BigDecimal costoPorHora) {
        this.costoPorHora = costoPorHora;
    }
    
    public Integer getKilometrosMantenimientoIntervalo() {
        return kilometrosMantenimientoIntervalo;
    }
    
    public void setKilometrosMantenimientoIntervalo(Integer kilometrosMantenimientoIntervalo) {
        this.kilometrosMantenimientoIntervalo = kilometrosMantenimientoIntervalo;
    }
    
    public BigDecimal getUltimoMantenimientoKilometros() {
        return ultimoMantenimientoKilometros;
    }
    
    public void setUltimoMantenimientoKilometros(BigDecimal ultimoMantenimientoKilometros) {
        this.ultimoMantenimientoKilometros = ultimoMantenimientoKilometros;
    }
    
    public BigDecimal getRendimientoCombustible() {
        return rendimientoCombustible;
    }
    
    public void setRendimientoCombustible(BigDecimal rendimientoCombustible) {
        this.rendimientoCombustible = rendimientoCombustible;
    }
    
    public String getUnidadRendimiento() {
        return unidadRendimiento;
    }
    
    public void setUnidadRendimiento(String unidadRendimiento) {
        this.unidadRendimiento = unidadRendimiento;
    }
    
    public BigDecimal getCostoCombustiblePorLitro() {
        return costoCombustiblePorLitro;
    }
    
    public void setCostoCombustiblePorLitro(BigDecimal costoCombustiblePorLitro) {
        this.costoCombustiblePorLitro = costoCombustiblePorLitro;
    }
    
    public BigDecimal getValorActual() {
        return valorActual;
    }
    
    public void setValorActual(BigDecimal valorActual) {
        this.valorActual = valorActual;
    }
    
    public LocalDate getFechaCompra() {
        return fechaCompra;
    }
    
    public void setFechaCompra(LocalDate fechaCompra) {
        this.fechaCompra = fechaCompra;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    // Método para obtener nombre completo de la maquinaria
    public String getNombreCompleto() {
        StringBuilder nombreCompleto = new StringBuilder(nombre);
        if (marca != null && !marca.trim().isEmpty()) {
            nombreCompleto.append(" - ").append(marca);
        }
        if (modelo != null && !modelo.trim().isEmpty()) {
            nombreCompleto.append(" ").append(modelo);
        }
        return nombreCompleto.toString();
    }
    
    // Método para actualizar fecha de modificación
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    // Métodos faltantes para los tests
    public void setAño(int año) {
        this.anioFabricacion = año;
    }
    
    public void setUsuario(User usuario) {
        this.user = usuario;
    }
}
