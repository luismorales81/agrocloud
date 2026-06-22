package com.agrocloud.feedlot.model.entity;

import com.agrocloud.core.domain.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedlot_ajuste_plantel")
@EntityListeners(AuditingEntityListener.class)
public class FeedlotAjustePlantel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private FeedlotLote lote;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "cabezas_antes", nullable = false)
    private Integer cabezasAntes;

    @Column(name = "cabezas_despues", nullable = false)
    private Integer cabezasDespues;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FeedlotLote getLote() {
        return lote;
    }

    public void setLote(FeedlotLote lote) {
        this.lote = lote;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getCabezasAntes() {
        return cabezasAntes;
    }

    public void setCabezasAntes(Integer cabezasAntes) {
        this.cabezasAntes = cabezasAntes;
    }

    public Integer getCabezasDespues() {
        return cabezasDespues;
    }

    public void setCabezasDespues(Integer cabezasDespues) {
        this.cabezasDespues = cabezasDespues;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
