package com.agrocloud.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "calendario_cumplimiento_serie_tarea")
public class CumplimientoSerieTareaRecurrente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serie_id", nullable = false)
    @JsonIgnore
    private SerieTareaRecurrenteCalendario serie;

    @NotNull
    @Column(name = "fecha_ocurrencia", nullable = false)
    private LocalDate fechaOcurrencia;

    @NotNull
    @Column(name = "cumplida", nullable = false)
    private Boolean cumplida = true;

    @Column(name = "fecha_marcado")
    private LocalDateTime fechaMarcado;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SerieTareaRecurrenteCalendario getSerie() {
        return serie;
    }

    public void setSerie(SerieTareaRecurrenteCalendario serie) {
        this.serie = serie;
    }

    public LocalDate getFechaOcurrencia() {
        return fechaOcurrencia;
    }

    public void setFechaOcurrencia(LocalDate fechaOcurrencia) {
        this.fechaOcurrencia = fechaOcurrencia;
    }

    public Boolean getCumplida() {
        return cumplida;
    }

    public void setCumplida(Boolean cumplida) {
        this.cumplida = cumplida;
    }

    public LocalDateTime getFechaMarcado() {
        return fechaMarcado;
    }

    public void setFechaMarcado(LocalDateTime fechaMarcado) {
        this.fechaMarcado = fechaMarcado;
    }
}
