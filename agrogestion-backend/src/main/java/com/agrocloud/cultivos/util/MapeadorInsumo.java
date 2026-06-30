package com.agrocloud.cultivos.util;

import com.agrocloud.core.inventory.domain.Insumo;
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
}
