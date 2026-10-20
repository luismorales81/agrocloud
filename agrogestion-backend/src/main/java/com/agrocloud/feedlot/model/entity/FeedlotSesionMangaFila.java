package com.agrocloud.feedlot.model.entity;

import com.agrocloud.feedlot.model.enums.FeedlotAccionDesconocido;
import com.agrocloud.feedlot.model.enums.FeedlotOrigenFilaRecuento;
import com.agrocloud.feedlot.model.enums.FeedlotResultadoFilaManga;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedlot_sesion_manga_fila")
public class FeedlotSesionMangaFila {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id", nullable = false)
    private FeedlotSesionManga sesion;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "numero_linea", nullable = false)
    private Integer numeroLinea;

    @Column(name = "eid_crudo", length = 64)
    private String eidCrudo;

    @Column(name = "eid_normalizado", length = 15)
    private String eidNormalizado;

    @Column(name = "caravana_visual", length = 40)
    private String caravanaVisual;

    @Column(name = "peso_kg", precision = 10, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "fecha_hora_lectura")
    private LocalDateTime fechaHoraLectura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id")
    private FeedlotAnimal animal;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 20)
    private FeedlotResultadoFilaManga resultado;

    @Column(name = "mensaje", length = 500)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_recuento", length = 20)
    private FeedlotOrigenFilaRecuento origenRecuento;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion_desconocido", length = 30)
    private FeedlotAccionDesconocido accionDesconocido;

    @Column(name = "lote_destino_id")
    private Long loteDestinoId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public FeedlotSesionManga getSesion() { return sesion; }
    public void setSesion(FeedlotSesionManga sesion) { this.sesion = sesion; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public Integer getNumeroLinea() { return numeroLinea; }
    public void setNumeroLinea(Integer numeroLinea) { this.numeroLinea = numeroLinea; }
    public String getEidCrudo() { return eidCrudo; }
    public void setEidCrudo(String eidCrudo) { this.eidCrudo = eidCrudo; }
    public String getEidNormalizado() { return eidNormalizado; }
    public void setEidNormalizado(String eidNormalizado) { this.eidNormalizado = eidNormalizado; }
    public String getCaravanaVisual() { return caravanaVisual; }
    public void setCaravanaVisual(String caravanaVisual) { this.caravanaVisual = caravanaVisual; }
    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }
    public LocalDateTime getFechaHoraLectura() { return fechaHoraLectura; }
    public void setFechaHoraLectura(LocalDateTime fechaHoraLectura) { this.fechaHoraLectura = fechaHoraLectura; }
    public FeedlotAnimal getAnimal() { return animal; }
    public void setAnimal(FeedlotAnimal animal) { this.animal = animal; }
    public FeedlotResultadoFilaManga getResultado() { return resultado; }
    public void setResultado(FeedlotResultadoFilaManga resultado) { this.resultado = resultado; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public FeedlotOrigenFilaRecuento getOrigenRecuento() { return origenRecuento; }
    public void setOrigenRecuento(FeedlotOrigenFilaRecuento origenRecuento) { this.origenRecuento = origenRecuento; }
    public FeedlotAccionDesconocido getAccionDesconocido() { return accionDesconocido; }
    public void setAccionDesconocido(FeedlotAccionDesconocido accionDesconocido) { this.accionDesconocido = accionDesconocido; }
    public Long getLoteDestinoId() { return loteDestinoId; }
    public void setLoteDestinoId(Long loteDestinoId) { this.loteDestinoId = loteDestinoId; }
}
