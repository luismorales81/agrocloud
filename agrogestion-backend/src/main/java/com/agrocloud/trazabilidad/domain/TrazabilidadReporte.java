package com.agrocloud.trazabilidad.domain;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Reporte sellado: inmutable lógicamente; no exponer operaciones de actualización desde la API.
 */
@Entity
@Table(name = "trazabilidad_reporte")
@EntityListeners(AuditingEntityListener.class)
public class TrazabilidadReporte {

    public static final String VERSION_MOTOR = "1.0";

    public enum ResultadoReporte {
        VALIDO, INVALIDO, INCOMPLETO
    }

    public enum TipoEntidadAlcance {
        LOTE,
        COSECHA,
        RECRIA,
        VENTA_PORCINO,
        AVICOLA_HUEVOS,
        AVICOLA_CRIANZA,
        AVICOLA_CARNE,
        AVICOLA_PONEDORAS,
        FEEDLOT_LOTE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "entidad_tipo", nullable = false, length = 32)
    private TipoEntidadAlcance entidadTipo;

    @Column(name = "entidad_id", nullable = false)
    private Long entidadId;

    @Column(name = "certificacion_codigo", nullable = false, length = 64)
    private String certificacionCodigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 20)
    private ResultadoReporte resultado;

    @Column(name = "version_motor", nullable = false, length = 16)
    private String versionMotor = VERSION_MOTOR;

    @Column(name = "snapshot_json", columnDefinition = "LONGTEXT", nullable = false)
    private String snapshotJson;

    @Column(name = "hash_snapshot", nullable = false, length = 64)
    private String hashSnapshot;

    @Column(name = "hash_pdf", length = 64)
    private String hashPdf;

    @Lob
    @Column(name = "contenido_pdf", columnDefinition = "LONGBLOB")
    private byte[] contenidoPdf;

    @CreatedDate
    @Column(name = "generado_en", nullable = false, updatable = false)
    private LocalDateTime generadoEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public TipoEntidadAlcance getEntidadTipo() { return entidadTipo; }
    public void setEntidadTipo(TipoEntidadAlcance entidadTipo) { this.entidadTipo = entidadTipo; }
    public Long getEntidadId() { return entidadId; }
    public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }
    public String getCertificacionCodigo() { return certificacionCodigo; }
    public void setCertificacionCodigo(String certificacionCodigo) { this.certificacionCodigo = certificacionCodigo; }
    public ResultadoReporte getResultado() { return resultado; }
    public void setResultado(ResultadoReporte resultado) { this.resultado = resultado; }
    public String getVersionMotor() { return versionMotor; }
    public void setVersionMotor(String versionMotor) { this.versionMotor = versionMotor; }
    public String getSnapshotJson() { return snapshotJson; }
    public void setSnapshotJson(String snapshotJson) { this.snapshotJson = snapshotJson; }
    public String getHashSnapshot() { return hashSnapshot; }
    public void setHashSnapshot(String hashSnapshot) { this.hashSnapshot = hashSnapshot; }
    public String getHashPdf() { return hashPdf; }
    public void setHashPdf(String hashPdf) { this.hashPdf = hashPdf; }
    public byte[] getContenidoPdf() { return contenidoPdf; }
    public void setContenidoPdf(byte[] contenidoPdf) { this.contenidoPdf = contenidoPdf; }
    public LocalDateTime getGeneradoEn() { return generadoEn; }
    public void setGeneradoEn(LocalDateTime generadoEn) { this.generadoEn = generadoEn; }
}
