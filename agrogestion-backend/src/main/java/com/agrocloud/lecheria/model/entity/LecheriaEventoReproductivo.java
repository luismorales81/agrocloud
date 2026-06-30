package com.agrocloud.lecheria.model.entity;

import com.agrocloud.lecheria.model.enums.LecheriaTipoEventoReproductivo;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lecheria_evento_reproductivo")
@EntityListeners(AuditingEntityListener.class)
public class LecheriaEventoReproductivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private LecheriaAnimal animal;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private LecheriaTipoEventoReproductivo tipo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "resultado", length = 30)
    private String resultado;

    @Column(name = "toro_pajuela", length = 150)
    private String toroPajuela;

    @Column(name = "fecha_prevista_parto")
    private LocalDate fechaPrevistaParto;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LecheriaAnimal getAnimal() { return animal; }
    public void setAnimal(LecheriaAnimal animal) { this.animal = animal; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public LecheriaTipoEventoReproductivo getTipo() { return tipo; }
    public void setTipo(LecheriaTipoEventoReproductivo tipo) { this.tipo = tipo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getToroPajuela() { return toroPajuela; }
    public void setToroPajuela(String toroPajuela) { this.toroPajuela = toroPajuela; }
    public LocalDate getFechaPrevistaParto() { return fechaPrevistaParto; }
    public void setFechaPrevistaParto(LocalDate fechaPrevistaParto) { this.fechaPrevistaParto = fechaPrevistaParto; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
