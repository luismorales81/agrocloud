package com.agrocloud.lecheria.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "lecheria_import_control_lechero")
@EntityListeners(AuditingEntityListener.class)
public class LecheriaImportControlLechero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "filas_procesadas", nullable = false)
    private Integer filasProcesadas = 0;

    @Column(name = "filas_error", nullable = false)
    private Integer filasError = 0;

    @Column(name = "detalle_errores", columnDefinition = "TEXT")
    private String detalleErrores;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    public Integer getFilasProcesadas() { return filasProcesadas; }
    public void setFilasProcesadas(Integer filasProcesadas) { this.filasProcesadas = filasProcesadas; }
    public Integer getFilasError() { return filasError; }
    public void setFilasError(Integer filasError) { this.filasError = filasError; }
    public String getDetalleErrores() { return detalleErrores; }
    public void setDetalleErrores(String detalleErrores) { this.detalleErrores = detalleErrores; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
