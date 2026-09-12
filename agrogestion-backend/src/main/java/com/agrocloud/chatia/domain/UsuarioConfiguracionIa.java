package com.agrocloud.chatia.domain;

import com.agrocloud.core.domain.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_configuracion_ia")
@EntityListeners(AuditingEntityListener.class)
public class UsuarioConfiguracionIa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private User usuario;

    @Column(name = "clave_api_cifrada", nullable = false, length = 512)
    private String claveApiCifrada;

    @Column(name = "modelo", nullable = false, length = 64)
    private String modelo = "gemini-flash-latest";

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "clave_valida", nullable = false)
    private boolean claveValida = true;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public String getClaveApiCifrada() {
        return claveApiCifrada;
    }

    public void setClaveApiCifrada(String claveApiCifrada) {
        this.claveApiCifrada = claveApiCifrada;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isClaveValida() {
        return claveValida;
    }

    public void setClaveValida(boolean claveValida) {
        this.claveValida = claveValida;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
