package com.agrocloud.core.domain;
import com.agrocloud.core.domain.User;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad para registrar logs de acceso de usuarios.
 */
@Entity
@Table(name = "logs_acceso")
@EntityListeners(AuditingEntityListener.class)
public class LogAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "tipo_acceso", length = 50)
    private String tipoAcceso;

    @Column(name = "resultado", length = 50)
    private String resultado;

    @Column(name = "detalles", length = 1000)
    private String detalles;

    @CreatedDate
    @Column(name = "fecha_acceso", nullable = false, updatable = false)
    private LocalDateTime fechaAcceso;

    public LogAcceso() {}

    public LogAcceso(User usuario, String ipAddress, String userAgent, String tipoAcceso, String resultado) {
        this.usuario = usuario;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.tipoAcceso = tipoAcceso;
        this.resultado = resultado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getTipoAcceso() { return tipoAcceso; }
    public void setTipoAcceso(String tipoAcceso) { this.tipoAcceso = tipoAcceso; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }
    public LocalDateTime getFechaAcceso() { return fechaAcceso; }
    public void setFechaAcceso(LocalDateTime fechaAcceso) { this.fechaAcceso = fechaAcceso; }
}
