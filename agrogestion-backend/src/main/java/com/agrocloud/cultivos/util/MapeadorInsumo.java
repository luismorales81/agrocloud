package com.agrocloud.cultivos.util;

import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.inventory.infrastructure.InsumoResumenProyeccion;
import com.agrocloud.dto.InsumoDTO;

/**
 * Convierte entidades {@link Insumo} a DTO sin tocar relaciones lazy (user, empresa).
 */
public final class MapeadorInsumo {

    private MapeadorInsumo() {
    }

    public static InsumoDTO aDtoListado(Insumo insumo) {
        InsumoDTO dto = new InsumoDTO();
        dto.setId(insumo.getId());
        dto.setNombre(insumo.getNombre());
        dto.setDescripcion(insumo.getDescripcion());
        if (insumo.getTipo() != null) {
            dto.setTipo(insumo.getTipo().name());
            dto.setEsAgroquimico(insumo.esAgroquimico());
            dto.setTienePropiedadesAgroquimicas(insumo.tienePropiedadesAgroquimicas());
        }
        dto.setUnidadMedida(insumo.getUnidadMedida());
        dto.setPrecioUnitario(insumo.getPrecioUnitario());
        dto.setStockActual(insumo.getStockActual());
        dto.setStockMinimo(insumo.getStockMinimo());
        dto.setProveedor(insumo.getProveedor());
        dto.setActivo(insumo.getActivo());
        if (insumo.getFechaVencimiento() != null) {
            dto.setFechaVencimiento(insumo.getFechaVencimiento().toString());
        }
        dto.setPrincipioActivo(insumo.getPrincipioActivo());
        dto.setConcentracion(insumo.getConcentracion());
        dto.setClaseQuimica(insumo.getClaseQuimica());
        dto.setCategoriaToxicologica(insumo.getCategoriaToxicologica());
        dto.setPeriodoCarenciaDias(insumo.getPeriodoCarenciaDias());
        dto.setDosisMinimaPorHa(insumo.getDosisMinimaPorHa());
        dto.setDosisMaximaPorHa(insumo.getDosisMaximaPorHa());
        dto.setUnidadDosis(insumo.getUnidadDosis());
        return dto;
    }

    public static InsumoDTO desdeFilaNativa(Object[] fila) {
        if (fila != null && fila.length == 1 && fila[0] instanceof Object[] interno) {
            fila = interno;
        }
        InsumoDTO dto = new InsumoDTO();
        dto.setId(aLong(indice(fila, 0)));
        dto.setNombre(aTexto(indice(fila, 1)));
        dto.setDescripcion(aTexto(indice(fila, 2)));
        Insumo.TipoInsumo tipo = Insumo.TipoInsumo.desdeTexto(aTexto(indice(fila, 3)));
        dto.setTipo(tipo.name());
        dto.setEsAgroquimico(tipo.esAgroquimico());
        dto.setUnidadMedida(aTexto(indice(fila, 4)));
        dto.setPrecioUnitario(aDecimal(indice(fila, 5)));
        dto.setStockActual(aDecimal(indice(fila, 6)));
        dto.setStockMinimo(aDecimal(indice(fila, 7)));
        dto.setProveedor(aTexto(indice(fila, 8)));
        dto.setActivo(aBooleano(indice(fila, 9)));
        dto.setTienePropiedadesAgroquimicas(false);
        return dto;
    }

    public static InsumoDTO desdeResumen(InsumoResumenProyeccion fila) {
        return desdeFilaNativa(new Object[] {
                fila.getId(), fila.getNombre(), fila.getDescripcion(), fila.getTipo(),
                fila.getUnidadMedida(), fila.getPrecioUnitario(), fila.getStockActual(),
                fila.getStockMinimo(), fila.getProveedor(), fila.getActivo()
        });
    }

    private static Object indice(Object[] fila, int i) {
        return fila != null && fila.length > i ? fila[i] : null;
    }

    private static Long aLong(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(valor.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String aTexto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private static java.math.BigDecimal aDecimal(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof java.math.BigDecimal d) {
            return d;
        }
        if (valor instanceof Number n) {
            return java.math.BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new java.math.BigDecimal(valor.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Boolean aBooleano(Object valor) {
        if (valor == null) {
            return true;
        }
        if (valor instanceof Boolean b) {
            return b;
        }
        if (valor instanceof Number n) {
            return n.intValue() != 0;
        }
        return !"false".equalsIgnoreCase(valor.toString()) && !"0".equals(valor.toString());
    }
}
