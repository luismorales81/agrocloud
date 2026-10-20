package com.agrocloud.core.inventory.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Evita 500 al listar insumos si la base tiene un tipo que el enum no conocía
 * (p. ej. COADYUVANTE u OTRO del wizard).
 */
@Converter(autoApply = false)
public class ConvertidorTipoInsumo implements AttributeConverter<Insumo.TipoInsumo, String> {

    @Override
    public String convertToDatabaseColumn(Insumo.TipoInsumo atributo) {
        return atributo == null ? null : atributo.name();
    }

    @Override
    public Insumo.TipoInsumo convertToEntityAttribute(String valor) {
        return Insumo.TipoInsumo.desdeTexto(valor);
    }
}
