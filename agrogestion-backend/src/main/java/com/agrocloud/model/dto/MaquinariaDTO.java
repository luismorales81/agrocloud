package com.agrocloud.model.dto;

import com.agrocloud.model.entity.Maquinaria;
import com.agrocloud.model.entity.Maquinaria.EstadoMaquinaria;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de Maquinaria sin referencias circulares
 */
public class MaquinariaDTO {
    private Long id;
    private String nombre;
    private String marca;
    private String modelo;
    private EstadoMaquinaria estado;
    private String descripcion;
    private Integer anioFabricacion;
    private String numeroSerie;
    private Long userId;
    private String userName;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructor por defecto
    public MaquinariaDTO() {}
    
    // Constructor desde entidad
    public MaquinariaDTO(Maquinaria maquinaria) {
        this.id = maquinaria.getId();
        this.nombre = maquinaria.getNombre();
        this.marca = maquinaria.getMarca();
        this.modelo = maquinaria.getModelo();
        this.estado = maquinaria.getEstado();
        this.descripcion = maquinaria.getDescripcion();
        this.anioFabricacion = maquinaria.getAnioFabricacion();
        this.numeroSerie = maquinaria.getNumeroSerie();
        this.fechaCreacion = maquinaria.getFechaCreacion();
        this.fechaActualizacion = maquinaria.getFechaActualizacion();
        
        // Información del usuario
        if (maquinaria.getUser() != null) {
            this.userId = maquinaria.getUser().getId();
            this.userName = maquinaria.getUser().getUsername();
        }
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
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
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
}
