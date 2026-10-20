package com.agrocloud.feedlot.model.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class FeedlotResumenArchivoManga {

    private int lecturas;
    private boolean tienePeso;
    private int pesosEncontrados;
    private int eidRepetidos;
    private LocalDate fechaMayoritaria;
    private LocalTime horaDesde;
    private LocalTime horaHasta;
    private boolean entendioArchivo;
    private FeedlotMapeoColumnas mapeo;
    private List<String> encabezados = new ArrayList<>();
    private List<String> filaMuestra = new ArrayList<>();
    private String mensajeResumen;
    private Long loteSugeridoId;
    private String loteSugeridoNombre;
    private boolean mezclaLotes;
    private List<String> eidsOtroLote = new ArrayList<>();
    private List<FeedlotLecturaArchivoManga> lecturasDetalle = new ArrayList<>();
    private String perfilArchivo;
    private List<String> columnasNoMapeadas = new ArrayList<>();
    private int porcentajeEidInvalidos;
    private List<String> avisos = new ArrayList<>();
    private List<String> bloqueantes = new ArrayList<>();
    private Long perfilLecturaId;

    public int getLecturas() { return lecturas; }
    public void setLecturas(int lecturas) { this.lecturas = lecturas; }
    public boolean isTienePeso() { return tienePeso; }
    public void setTienePeso(boolean tienePeso) { this.tienePeso = tienePeso; }
    public int getPesosEncontrados() { return pesosEncontrados; }
    public void setPesosEncontrados(int pesosEncontrados) { this.pesosEncontrados = pesosEncontrados; }
    public int getEidRepetidos() { return eidRepetidos; }
    public void setEidRepetidos(int eidRepetidos) { this.eidRepetidos = eidRepetidos; }
    public LocalDate getFechaMayoritaria() { return fechaMayoritaria; }
    public void setFechaMayoritaria(LocalDate fechaMayoritaria) { this.fechaMayoritaria = fechaMayoritaria; }
    public LocalTime getHoraDesde() { return horaDesde; }
    public void setHoraDesde(LocalTime horaDesde) { this.horaDesde = horaDesde; }
    public LocalTime getHoraHasta() { return horaHasta; }
    public void setHoraHasta(LocalTime horaHasta) { this.horaHasta = horaHasta; }
    public boolean isEntendioArchivo() { return entendioArchivo; }
    public void setEntendioArchivo(boolean entendioArchivo) { this.entendioArchivo = entendioArchivo; }
    public FeedlotMapeoColumnas getMapeo() { return mapeo; }
    public void setMapeo(FeedlotMapeoColumnas mapeo) { this.mapeo = mapeo; }
    public List<String> getEncabezados() { return encabezados; }
    public void setEncabezados(List<String> encabezados) { this.encabezados = encabezados; }
    public List<String> getFilaMuestra() { return filaMuestra; }
    public void setFilaMuestra(List<String> filaMuestra) { this.filaMuestra = filaMuestra; }
    public String getMensajeResumen() { return mensajeResumen; }
    public void setMensajeResumen(String mensajeResumen) { this.mensajeResumen = mensajeResumen; }
    public Long getLoteSugeridoId() { return loteSugeridoId; }
    public void setLoteSugeridoId(Long loteSugeridoId) { this.loteSugeridoId = loteSugeridoId; }
    public String getLoteSugeridoNombre() { return loteSugeridoNombre; }
    public void setLoteSugeridoNombre(String loteSugeridoNombre) { this.loteSugeridoNombre = loteSugeridoNombre; }
    public boolean isMezclaLotes() { return mezclaLotes; }
    public void setMezclaLotes(boolean mezclaLotes) { this.mezclaLotes = mezclaLotes; }
    public List<String> getEidsOtroLote() { return eidsOtroLote; }
    public void setEidsOtroLote(List<String> eidsOtroLote) { this.eidsOtroLote = eidsOtroLote; }
    public List<FeedlotLecturaArchivoManga> getLecturasDetalle() { return lecturasDetalle; }
    public void setLecturasDetalle(List<FeedlotLecturaArchivoManga> lecturasDetalle) { this.lecturasDetalle = lecturasDetalle; }
    public String getPerfilArchivo() { return perfilArchivo; }
    public void setPerfilArchivo(String perfilArchivo) { this.perfilArchivo = perfilArchivo; }
    public List<String> getColumnasNoMapeadas() { return columnasNoMapeadas; }
    public void setColumnasNoMapeadas(List<String> columnasNoMapeadas) { this.columnasNoMapeadas = columnasNoMapeadas; }
    public int getPorcentajeEidInvalidos() { return porcentajeEidInvalidos; }
    public void setPorcentajeEidInvalidos(int porcentajeEidInvalidos) { this.porcentajeEidInvalidos = porcentajeEidInvalidos; }
    public List<String> getAvisos() { return avisos; }
    public void setAvisos(List<String> avisos) { this.avisos = avisos; }
    public List<String> getBloqueantes() { return bloqueantes; }
    public void setBloqueantes(List<String> bloqueantes) { this.bloqueantes = bloqueantes; }
    public Long getPerfilLecturaId() { return perfilLecturaId; }
    public void setPerfilLecturaId(Long perfilLecturaId) { this.perfilLecturaId = perfilLecturaId; }
}
