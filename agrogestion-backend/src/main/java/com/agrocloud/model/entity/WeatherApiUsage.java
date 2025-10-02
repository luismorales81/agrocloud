package com.agrocloud.model.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad para trackear el uso de la API del clima
 */
@Entity
@Table(name = "weather_api_usage")
public class WeatherApiUsage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;
    
    @Column(name = "usos_hoy", nullable = false)
    private Integer usosHoy = 0;
    
    @Column(name = "limite_diario", nullable = false)
    private Integer limiteDiario = 1000;
    
    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion;
    
    // Constructor por defecto
    public WeatherApiUsage() {
        this.fecha = LocalDate.now();
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    // Constructor con fecha específica
    public WeatherApiUsage(LocalDate fecha) {
        this.fecha = fecha;
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public Integer getUsosHoy() {
        return usosHoy;
    }
    
    public void setUsosHoy(Integer usosHoy) {
        this.usosHoy = usosHoy;
    }
    
    public Integer getLimiteDiario() {
        return limiteDiario;
    }
    
    public void setLimiteDiario(Integer limiteDiario) {
        this.limiteDiario = limiteDiario;
    }
    
    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }
    
    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }
    
    /**
     * Incrementa el contador de usos
     */
    public void incrementarUso() {
        this.usosHoy++;
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    /**
     * Calcula el porcentaje de uso
     */
    public Double getPorcentajeUso() {
        if (limiteDiario == 0) return 0.0;
        return (double) usosHoy / limiteDiario * 100;
    }
    
    /**
     * Verifica si se ha alcanzado el límite
     */
    public Boolean isLimiteAlcanzado() {
        return usosHoy >= limiteDiario;
    }
}
