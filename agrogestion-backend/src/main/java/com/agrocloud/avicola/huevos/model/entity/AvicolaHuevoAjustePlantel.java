package com.agrocloud.avicola.huevos.model.entity;

import com.agrocloud.core.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "avicola_huevo_ajuste_plantel")
public class AvicolaHuevoAjustePlantel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private AvicolaHuevoLote lote;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "cantidad_aves_anterior", nullable = false)
    private Integer cantidadAvesAnterior;

    @Column(name = "cantidad_aves_nueva", nullable = false)
    private Integer cantidadAvesNueva;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AvicolaHuevoLote getLote() {
        return lote;
    }

    public void setLote(AvicolaHuevoLote lote) {
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

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Integer getCantidadAvesAnterior() {
        return cantidadAvesAnterior;
    }

    public void setCantidadAvesAnterior(Integer cantidadAvesAnterior) {
        this.cantidadAvesAnterior = cantidadAvesAnterior;
    }

    public Integer getCantidadAvesNueva() {
        return cantidadAvesNueva;
    }

    public void setCantidadAvesNueva(Integer cantidadAvesNueva) {
        this.cantidadAvesNueva = cantidadAvesNueva;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
