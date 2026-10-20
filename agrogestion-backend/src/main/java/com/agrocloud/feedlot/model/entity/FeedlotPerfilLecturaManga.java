package com.agrocloud.feedlot.model.entity;

import com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedlot_perfil_lectura_manga")
@EntityListeners(AuditingEntityListener.class)
public class FeedlotPerfilLecturaManga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private FeedlotTipoSesionManga tipo;

    @Column(name = "predeterminado", nullable = false)
    private Boolean predeterminado = false;

    @Column(name = "mapeo_alias_json", nullable = false, columnDefinition = "TEXT")
    private String mapeoAliasJson;

    @Column(name = "insumo_id")
    private Long insumoId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public FeedlotTipoSesionManga getTipo() { return tipo; }
    public void setTipo(FeedlotTipoSesionManga tipo) { this.tipo = tipo; }
    public Boolean getPredeterminado() { return predeterminado; }
    public void setPredeterminado(Boolean predeterminado) { this.predeterminado = predeterminado; }
    public String getMapeoAliasJson() { return mapeoAliasJson; }
    public void setMapeoAliasJson(String mapeoAliasJson) { this.mapeoAliasJson = mapeoAliasJson; }
    public Long getInsumoId() { return insumoId; }
    public void setInsumoId(Long insumoId) { this.insumoId = insumoId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
