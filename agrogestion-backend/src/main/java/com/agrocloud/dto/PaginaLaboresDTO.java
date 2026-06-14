package com.agrocloud.dto;

import java.util.List;

/**
 * Respuesta paginada de labores detalladas.
 */
public class PaginaLaboresDTO {

    private List<LaborDetalladoDTO> contenido;
    private int pagina;
    private int tamano;
    private long totalElementos;
    private int totalPaginas;

    public PaginaLaboresDTO() {}

    public PaginaLaboresDTO(List<LaborDetalladoDTO> contenido, int pagina, int tamano, long totalElementos) {
        this.contenido = contenido;
        this.pagina = pagina;
        this.tamano = tamano;
        this.totalElementos = totalElementos;
        this.totalPaginas = tamano > 0 ? (int) Math.ceil((double) totalElementos / tamano) : 0;
    }

    public List<LaborDetalladoDTO> getContenido() {
        return contenido;
    }

    public void setContenido(List<LaborDetalladoDTO> contenido) {
        this.contenido = contenido;
    }

    public int getPagina() {
        return pagina;
    }

    public void setPagina(int pagina) {
        this.pagina = pagina;
    }

    public int getTamano() {
        return tamano;
    }

    public void setTamano(int tamano) {
        this.tamano = tamano;
    }

    public long getTotalElementos() {
        return totalElementos;
    }

    public void setTotalElementos(long totalElementos) {
        this.totalElementos = totalElementos;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }
}
