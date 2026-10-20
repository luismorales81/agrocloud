package com.agrocloud.feedlot.model.entity;

import com.agrocloud.feedlot.model.enums.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedlot_sesion_manga")
@EntityListeners(AuditingEntityListener.class)
public class FeedlotSesionManga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private FeedlotLote lote;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private FeedlotTipoSesionManga tipo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private FeedlotEstadoSesionManga estado = FeedlotEstadoSesionManga.BORRADOR;

    @Column(name = "nombre_archivo", length = 255)
    private String nombreArchivo;

    @Column(name = "perfil_mapeo_json", columnDefinition = "TEXT")
    private String perfilMapeoJson;

    @Column(name = "perfil_lectura_id")
    private Long perfilLecturaId;

    @Column(name = "delimitador", length = 8)
    private String delimitador;

    @Column(name = "filas_ok", nullable = false)
    private Integer filasOk = 0;

    @Column(name = "filas_error", nullable = false)
    private Integer filasError = 0;

    @Column(name = "detalle_errores", columnDefinition = "TEXT")
    private String detalleErrores;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "movimiento_inventario_ref")
    private Long movimientoInventarioRef;

    @Column(name = "ajustar_plantel", nullable = false)
    private Boolean ajustarPlantel = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", length = 30)
    private FeedlotTipoEventoSanitario tipoEvento;

    @Column(name = "insumo_id")
    private Long insumoId;

    @Column(name = "dosis_por_animal", precision = 12, scale = 4)
    private BigDecimal dosisPorAnimal;

    @Column(name = "unidad_dosis", length = 20)
    private String unidadDosis;

    @Column(name = "cantidad_total", precision = 14, scale = 4)
    private BigDecimal cantidadTotal;

    @Column(name = "dias_retiro")
    private Integer diasRetiro;

    @Enumerated(EnumType.STRING)
    @Column(name = "aplicador", length = 30)
    private FeedlotAplicador aplicador;

    @Column(name = "aplicador_nombre", length = 150)
    private String aplicadorNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_insumo", length = 30)
    private FeedlotOrigenInsumo origenInsumo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "subtipo_baja", length = 20)
    private FeedlotSubtipoBaja subtipoBaja;

    @Column(name = "motivo_muerte_id")
    private Long motivoMuerteId;

    @Column(name = "precio_kg", precision = 12, scale = 2)
    private BigDecimal precioKg;

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
    public FeedlotLote getLote() { return lote; }
    public void setLote(FeedlotLote lote) { this.lote = lote; }
    public FeedlotTipoSesionManga getTipo() { return tipo; }
    public void setTipo(FeedlotTipoSesionManga tipo) { this.tipo = tipo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public FeedlotEstadoSesionManga getEstado() { return estado; }
    public void setEstado(FeedlotEstadoSesionManga estado) { this.estado = estado; }
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    public String getPerfilMapeoJson() { return perfilMapeoJson; }
    public void setPerfilMapeoJson(String perfilMapeoJson) { this.perfilMapeoJson = perfilMapeoJson; }
    public Long getPerfilLecturaId() { return perfilLecturaId; }
    public void setPerfilLecturaId(Long perfilLecturaId) { this.perfilLecturaId = perfilLecturaId; }
    public String getDelimitador() { return delimitador; }
    public void setDelimitador(String delimitador) { this.delimitador = delimitador; }
    public Integer getFilasOk() { return filasOk; }
    public void setFilasOk(Integer filasOk) { this.filasOk = filasOk; }
    public Integer getFilasError() { return filasError; }
    public void setFilasError(Integer filasError) { this.filasError = filasError; }
    public String getDetalleErrores() { return detalleErrores; }
    public void setDetalleErrores(String detalleErrores) { this.detalleErrores = detalleErrores; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getMovimientoInventarioRef() { return movimientoInventarioRef; }
    public void setMovimientoInventarioRef(Long movimientoInventarioRef) { this.movimientoInventarioRef = movimientoInventarioRef; }
    public Boolean getAjustarPlantel() { return ajustarPlantel; }
    public void setAjustarPlantel(Boolean ajustarPlantel) { this.ajustarPlantel = ajustarPlantel; }
    public FeedlotTipoEventoSanitario getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(FeedlotTipoEventoSanitario tipoEvento) { this.tipoEvento = tipoEvento; }
    public Long getInsumoId() { return insumoId; }
    public void setInsumoId(Long insumoId) { this.insumoId = insumoId; }
    public BigDecimal getDosisPorAnimal() { return dosisPorAnimal; }
    public void setDosisPorAnimal(BigDecimal dosisPorAnimal) { this.dosisPorAnimal = dosisPorAnimal; }
    public String getUnidadDosis() { return unidadDosis; }
    public void setUnidadDosis(String unidadDosis) { this.unidadDosis = unidadDosis; }
    public BigDecimal getCantidadTotal() { return cantidadTotal; }
    public void setCantidadTotal(BigDecimal cantidadTotal) { this.cantidadTotal = cantidadTotal; }
    public Integer getDiasRetiro() { return diasRetiro; }
    public void setDiasRetiro(Integer diasRetiro) { this.diasRetiro = diasRetiro; }
    public FeedlotAplicador getAplicador() { return aplicador; }
    public void setAplicador(FeedlotAplicador aplicador) { this.aplicador = aplicador; }
    public String getAplicadorNombre() { return aplicadorNombre; }
    public void setAplicadorNombre(String aplicadorNombre) { this.aplicadorNombre = aplicadorNombre; }
    public FeedlotOrigenInsumo getOrigenInsumo() { return origenInsumo; }
    public void setOrigenInsumo(FeedlotOrigenInsumo origenInsumo) { this.origenInsumo = origenInsumo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public FeedlotSubtipoBaja getSubtipoBaja() { return subtipoBaja; }
    public void setSubtipoBaja(FeedlotSubtipoBaja subtipoBaja) { this.subtipoBaja = subtipoBaja; }
    public Long getMotivoMuerteId() { return motivoMuerteId; }
    public void setMotivoMuerteId(Long motivoMuerteId) { this.motivoMuerteId = motivoMuerteId; }
    public BigDecimal getPrecioKg() { return precioKg; }
    public void setPrecioKg(BigDecimal precioKg) { this.precioKg = precioKg; }
}
