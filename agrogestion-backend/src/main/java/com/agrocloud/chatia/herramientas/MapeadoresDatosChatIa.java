package com.agrocloud.chatia.herramientas;

import com.agrocloud.core.domain.Egreso;
import com.agrocloud.core.domain.Ingreso;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.inventory.domain.MovimientoInventario;
import com.agrocloud.cultivos.domain.Field;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.cultivos.domain.Maquinaria;
import com.agrocloud.cultivos.domain.Plot;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MapeadoresDatosChatIa {

    private MapeadoresDatosChatIa() {
    }

    public static Map<String, Object> mapearMaquinaria(Maquinaria m) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", m.getId());
        mapa.put("nombre", m.getNombre());
        mapa.put("tipo", m.getTipo());
        mapa.put("marca", m.getMarca());
        mapa.put("modelo", m.getModelo());
        mapa.put("estado", m.getEstado() != null ? m.getEstado().name() : null);
        mapa.put("descripcion", m.getDescripcion());
        mapa.put("anioFabricacion", m.getAnioFabricacion());
        mapa.put("numeroSerie", m.getNumeroSerie());
        mapa.put("fechaCompra", m.getFechaCompra() != null ? m.getFechaCompra().toString() : null);

        BigDecimal kmUso = m.getKilometrosUso() != null ? m.getKilometrosUso() : BigDecimal.ZERO;
        BigDecimal ultMant = m.getUltimoMantenimientoKilometros() != null
                ? m.getUltimoMantenimientoKilometros() : BigDecimal.ZERO;
        int intervalo = m.getKilometrosMantenimientoIntervalo() != null
                ? m.getKilometrosMantenimientoIntervalo() : 5000;

        mapa.put("kilometrosUso", HerramientasUtil.valorNumerico(kmUso));
        mapa.put("ultimoMantenimientoKilometros", HerramientasUtil.valorNumerico(ultMant));
        mapa.put("kilometrosMantenimientoIntervalo", intervalo);
        mapa.put("kilometrosDesdeUltimoMantenimiento", HerramientasUtil.valorNumerico(kmUso.subtract(ultMant)));
        BigDecimal kmHastaProximo = ultMant.add(BigDecimal.valueOf(intervalo)).subtract(kmUso);
        mapa.put("kilometrosHastaProximoMantenimiento", HerramientasUtil.valorNumerico(kmHastaProximo));
        mapa.put("requiereMantenimiento", kmHastaProximo.compareTo(BigDecimal.ZERO) <= 0);

        mapa.put("costoPorHora", HerramientasUtil.valorNumerico(m.getCostoPorHora()));
        mapa.put("rendimientoCombustible", HerramientasUtil.valorNumerico(m.getRendimientoCombustible()));
        mapa.put("unidadRendimiento", m.getUnidadRendimiento());
        mapa.put("costoCombustiblePorLitro", HerramientasUtil.valorNumerico(m.getCostoCombustiblePorLitro()));
        mapa.put("valorActual", HerramientasUtil.valorNumerico(m.getValorActual()));
        return mapa;
    }

    public static Map<String, Object> mapearInsumo(Insumo i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("nombre", i.getNombre());
        m.put("descripcion", i.getDescripcion());
        m.put("tipo", i.getTipo() != null ? i.getTipo().name() : null);
        m.put("stockActual", HerramientasUtil.valorNumerico(i.getStockActual()));
        m.put("stockMinimo", HerramientasUtil.valorNumerico(i.getStockMinimo()));
        m.put("unidadMedida", i.getUnidadMedida());
        m.put("precioUnitario", HerramientasUtil.valorNumerico(i.getPrecioUnitario()));
        m.put("proveedor", i.getProveedor());
        m.put("fechaVencimiento", i.getFechaVencimiento() != null ? i.getFechaVencimiento().toString() : null);
        m.put("stockBajo", i.isStockBajo());
        if (i.esAgroquimico() || i.tienePropiedadesAgroquimicas()) {
            m.put("principioActivo", i.getPrincipioActivo());
            m.put("concentracion", i.getConcentracion());
            m.put("periodoCarenciaDias", i.getPeriodoCarenciaDias());
        }
        return m;
    }

    public static Map<String, Object> mapearCampo(Field f) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", f.getId());
        m.put("nombre", f.getNombre());
        m.put("descripcion", f.getDescripcion());
        m.put("superficieHa", f.getAreaHectareas());
        m.put("ubicacion", f.getUbicacion());
        m.put("estado", f.getEstado());
        return m;
    }

    public static Map<String, Object> mapearLote(Plot l) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", l.getId());
        m.put("nombre", l.getNombre());
        m.put("descripcion", l.getDescripcion());
        m.put("campo", l.getCampo() != null ? l.getCampo().getNombre() : null);
        m.put("estado", l.getEstado());
        m.put("tipoUso", l.getTipoUso() != null ? l.getTipoUso().name() : null);
        m.put("superficieHa", l.getAreaHectareas());
        m.put("tipoSuelo", l.getTipoSuelo());
        m.put("cultivoActual", l.getCultivoActual());
        m.put("fechaSiembra", l.getFechaSiembra() != null ? l.getFechaSiembra().toString() : null);
        m.put("fechaCosechaEsperada", l.getFechaCosechaEsperada() != null ? l.getFechaCosechaEsperada().toString() : null);
        m.put("fechaCosechaReal", l.getFechaCosechaReal() != null ? l.getFechaCosechaReal().toString() : null);
        m.put("rendimientoEsperado", HerramientasUtil.valorNumerico(l.getRendimientoEsperado()));
        m.put("rendimientoReal", HerramientasUtil.valorNumerico(l.getRendimientoReal()));
        return m;
    }

    public static Map<String, Object> mapearLabor(Labor l) {
        return mapearLabor(l, true);
    }

    public static Map<String, Object> mapearLabor(Labor l, boolean incluirCostos) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", l.getId());
        m.put("tipo", l.getTipoLabor() != null ? l.getTipoLabor().name() : null);
        m.put("descripcion", l.getDescripcion());
        m.put("lote", l.getLote() != null ? l.getLote().getNombre() : null);
        m.put("cultivo", l.getCultivo() != null ? l.getCultivo().getNombre() : null);
        m.put("fechaInicio", l.getFechaInicio() != null ? l.getFechaInicio().toString() : null);
        m.put("fechaFin", l.getFechaFin() != null ? l.getFechaFin().toString() : null);
        m.put("fechaRealizacion", l.getFechaRealizacion() != null ? l.getFechaRealizacion().toString() : null);
        m.put("estado", l.getEstado() != null ? l.getEstado().name() : null);
        m.put("responsable", l.getResponsable());
        m.put("observaciones", l.getObservaciones());
        if (incluirCostos) {
            m.put("costoTotal", HerramientasUtil.valorNumerico(l.getCostoTotal()));
        }
        return m;
    }

    public static Map<String, Object> mapearIngreso(Ingreso i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("fecha", i.getFecha() != null ? i.getFecha().toString() : null);
        m.put("concepto", i.getConcepto());
        m.put("descripcion", i.getDescripcion());
        m.put("tipo", i.getTipoIngreso() != null ? i.getTipoIngreso().name() : null);
        m.put("monto", HerramientasUtil.valorNumerico(i.getMonto()));
        m.put("cantidad", HerramientasUtil.valorNumerico(i.getCantidad()));
        m.put("unidadMedida", i.getUnidadMedida());
        m.put("clienteComprador", i.getClienteComprador());
        m.put("lote", i.getLote() != null ? i.getLote().getNombre() : null);
        m.put("estado", i.getEstado() != null ? i.getEstado().name() : null);
        return m;
    }

    public static Map<String, Object> mapearEgreso(Egreso e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("fecha", e.getFecha() != null ? e.getFecha().toString() : null);
        m.put("concepto", e.getConcepto());
        m.put("descripcion", e.getDescripcion());
        m.put("tipo", e.getTipo() != null ? e.getTipo().name() : null);
        m.put("monto", HerramientasUtil.valorNumerico(e.getCostoTotal()));
        m.put("proveedor", e.getProveedor());
        m.put("lote", e.getLote() != null ? e.getLote().getNombre() : null);
        m.put("estado", e.getEstado() != null ? e.getEstado().name() : null);
        return m;
    }

    public static Map<String, Object> mapearMovimiento(MovimientoInventario mov) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", mov.getId());
        mapa.put("fecha", mov.getFechaMovimiento() != null ? mov.getFechaMovimiento().toString() : null);
        mapa.put("tipo", mov.getTipoMovimiento() != null ? mov.getTipoMovimiento().name() : null);
        mapa.put("cantidad", HerramientasUtil.valorNumerico(mov.getCantidad()));
        mapa.put("insumo", mov.getInsumo() != null ? mov.getInsumo().getNombre() : null);
        mapa.put("unidadMedida", mov.getInsumo() != null ? mov.getInsumo().getUnidadMedida() : null);
        mapa.put("motivo", mov.getMotivo());
        mapa.put("origen", mov.getOrigen());
        return mapa;
    }
}
