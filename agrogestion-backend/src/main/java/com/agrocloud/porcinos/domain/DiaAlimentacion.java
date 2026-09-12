package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un día del calendario de alimentación
 * Registra el estado de confirmación y resumen del consumo diario
 */
@Entity
@Table(name = "porcinos_legacy_dias_alimentacion",
       uniqueConstraints = @UniqueConstraint(columnNames = {"empresa_id", "fecha"}))
@EntityListeners(AuditingEntityListener.class)
public class DiaAlimentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoDia estado = EstadoDia.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmado_por_id")
    @JsonIgnoreProperties({"userCompanyRoles", "usuarioEmpresas", "userRoles", "password",
                          "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    private User confirmadoPor;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Column(name = "observaciones_confirmacion", columnDefinition = "TEXT")
    private String observacionesConfirmacion;

    @Column(name = "total_lotes_atendidos")
    private Integer totalLotesAtendidos = 0;

    @Column(name = "total_animales_atendidos")
    private Integer totalAnimalesAtendidos = 0;

    @Column(name = "total_recetas_usadas")
    private Integer totalRecetasUsadas = 0;

    @Column(name = "total_insumos_consumidos")
    private Integer totalInsumosConsumidos = 0;

    @Column(name = "tiene_alertas_stock_insuficiente")
    private Boolean tieneAlertasStockInsuficiente = false;

    @Column(name = "cantidad_alertas")
    private Integer cantidadAlertas = 0;

    @OneToMany(mappedBy = "diaAlimentacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ConsumoDiarioAutomatico> consumos = new ArrayList<>();

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public enum EstadoDia {
        PENDIENTE, CONFIRMADO, CON_CORRECCIONES
    }

    public DiaAlimentacion() {}

    public DiaAlimentacion(LocalDate fecha, Empresa empresa) {
        this.fecha = fecha;
        this.empresa = empresa;
        this.estado = EstadoDia.PENDIENTE;
        this.totalLotesAtendidos = 0;
        this.totalAnimalesAtendidos = 0;
        this.totalRecetasUsadas = 0;
        this.totalInsumosConsumidos = 0;
        this.tieneAlertasStockInsuficiente = false;
        this.cantidadAlertas = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public EstadoDia getEstado() { return estado; }
    public void setEstado(EstadoDia estado) { this.estado = estado; }
    public User getConfirmadoPor() { return confirmadoPor; }
    public void setConfirmadoPor(User confirmadoPor) { this.confirmadoPor = confirmadoPor; }
    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }
    public void setFechaConfirmacion(LocalDateTime fechaConfirmacion) { this.fechaConfirmacion = fechaConfirmacion; }
    public String getObservacionesConfirmacion() { return observacionesConfirmacion; }
    public void setObservacionesConfirmacion(String observacionesConfirmacion) { this.observacionesConfirmacion = observacionesConfirmacion; }
    public Integer getTotalLotesAtendidos() { return totalLotesAtendidos; }
    public void setTotalLotesAtendidos(Integer totalLotesAtendidos) { this.totalLotesAtendidos = totalLotesAtendidos; }
    public Integer getTotalAnimalesAtendidos() { return totalAnimalesAtendidos; }
    public void setTotalAnimalesAtendidos(Integer totalAnimalesAtendidos) { this.totalAnimalesAtendidos = totalAnimalesAtendidos; }
    public Integer getTotalRecetasUsadas() { return totalRecetasUsadas; }
    public void setTotalRecetasUsadas(Integer totalRecetasUsadas) { this.totalRecetasUsadas = totalRecetasUsadas; }
    public Integer getTotalInsumosConsumidos() { return totalInsumosConsumidos; }
    public void setTotalInsumosConsumidos(Integer totalInsumosConsumidos) { this.totalInsumosConsumidos = totalInsumosConsumidos; }
    public Boolean getTieneAlertasStockInsuficiente() { return tieneAlertasStockInsuficiente; }
    public void setTieneAlertasStockInsuficiente(Boolean tieneAlertasStockInsuficiente) { this.tieneAlertasStockInsuficiente = tieneAlertasStockInsuficiente; }
    public Integer getCantidadAlertas() { return cantidadAlertas; }
    public void setCantidadAlertas(Integer cantidadAlertas) { this.cantidadAlertas = cantidadAlertas; }
    public List<ConsumoDiarioAutomatico> getConsumos() { return consumos; }
    public void setConsumos(List<ConsumoDiarioAutomatico> consumos) { this.consumos = consumos; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public void confirmar(User usuario, String observaciones) {
        if (this.estado == EstadoDia.CONFIRMADO && observaciones == null) return;
        this.confirmadoPor = usuario;
        this.fechaConfirmacion = LocalDateTime.now();
        this.observacionesConfirmacion = observaciones;
        if (observaciones != null && !observaciones.trim().isEmpty()) {
            this.estado = EstadoDia.CON_CORRECCIONES;
        } else {
            this.estado = EstadoDia.CONFIRMADO;
        }
    }

    public boolean estaCerrado() {
        return estado == EstadoDia.CONFIRMADO || estado == EstadoDia.CON_CORRECCIONES;
    }
}
