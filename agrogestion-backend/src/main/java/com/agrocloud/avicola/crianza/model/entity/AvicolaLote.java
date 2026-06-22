package com.agrocloud.avicola.crianza.model.entity;

import com.agrocloud.avicola.crianza.model.enums.AvicolaEspecie;
import com.agrocloud.avicola.crianza.model.enums.AvicolaLoteEstado;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lote productivo de aves.
 */
@Entity
@Table(name = "avicola_lote")
@EntityListeners(AuditingEntityListener.class)
public class AvicolaLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "campana_id")
    private Long campanaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private AvicolaEstablecimiento establecimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raza_id", nullable = false)
    private AvicolaRaza raza;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "especie", nullable = false, length = 50)
    private AvicolaEspecie especie;

    @Column(name = "origen", nullable = false, length = 30)
    private String origen;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "cantidad_inicial", nullable = false)
    private Integer cantidadInicial;

    @Column(name = "cantidad_animales", nullable = false)
    private Integer cantidadAnimales;

    @Column(name = "peso_promedio_ingreso", precision = 8, scale = 3)
    private BigDecimal pesoPromedioIngreso;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private AvicolaLoteEstado estado = AvicolaLoteEstado.ACTIVO;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

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

    public Long getCampanaId() {
        return campanaId;
    }

    public void setCampanaId(Long campanaId) {
        this.campanaId = campanaId;
    }

    public AvicolaEstablecimiento getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(AvicolaEstablecimiento establecimiento) {
        this.establecimiento = establecimiento;
    }

    public AvicolaRaza getRaza() {
        return raza;
    }

    public void setRaza(AvicolaRaza raza) {
        this.raza = raza;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public AvicolaEspecie getEspecie() {
        return especie;
    }

    public void setEspecie(AvicolaEspecie especie) {
        this.especie = especie;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Integer getCantidadInicial() {
        return cantidadInicial;
    }

    public void setCantidadInicial(Integer cantidadInicial) {
        this.cantidadInicial = cantidadInicial;
    }

    public Integer getCantidadAnimales() {
        return cantidadAnimales;
    }

    public void setCantidadAnimales(Integer cantidadAnimales) {
        this.cantidadAnimales = cantidadAnimales;
    }

    public BigDecimal getPesoPromedioIngreso() {
        return pesoPromedioIngreso;
    }

    public void setPesoPromedioIngreso(BigDecimal pesoPromedioIngreso) {
        this.pesoPromedioIngreso = pesoPromedioIngreso;
    }

    public AvicolaLoteEstado getEstado() {
        return estado;
    }

    public void setEstado(AvicolaLoteEstado estado) {
        this.estado = estado;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
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
