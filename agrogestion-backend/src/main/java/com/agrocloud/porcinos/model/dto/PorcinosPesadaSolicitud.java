package com.agrocloud.porcinos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PorcinosPesadaSolicitud {

    private LocalDate fecha;
    private BigDecimal pesoPromedioKg;
    private Integer cabezasMuestreadas;
    private String observaciones;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getPesoPromedioKg() {
        return pesoPromedioKg;
    }

    public void setPesoPromedioKg(BigDecimal pesoPromedioKg) {
        this.pesoPromedioKg = pesoPromedioKg;
    }

    public Integer getCabezasMuestreadas() {
        return cabezasMuestreadas;
    }

    public void setCabezasMuestreadas(Integer cabezasMuestreadas) {
        this.cabezasMuestreadas = cabezasMuestreadas;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
