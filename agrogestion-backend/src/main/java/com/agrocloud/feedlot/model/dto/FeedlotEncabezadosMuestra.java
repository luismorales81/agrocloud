package com.agrocloud.feedlot.model.dto;

import java.util.ArrayList;
import java.util.List;

public class FeedlotEncabezadosMuestra {

    private List<String> encabezados = new ArrayList<>();
    private List<String> filaMuestra = new ArrayList<>();
    private String delimitador;

    public List<String> getEncabezados() { return encabezados; }
    public void setEncabezados(List<String> encabezados) { this.encabezados = encabezados; }
    public List<String> getFilaMuestra() { return filaMuestra; }
    public void setFilaMuestra(List<String> filaMuestra) { this.filaMuestra = filaMuestra; }
    public String getDelimitador() { return delimitador; }
    public void setDelimitador(String delimitador) { this.delimitador = delimitador; }
}
