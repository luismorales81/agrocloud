package com.agrocloud.core.inventory.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConvertidorTipoInsumoTest {

    private final ConvertidorTipoInsumo convertidor = new ConvertidorTipoInsumo();

    @Test
    void leeTiposDelWizardSinFallar() {
        assertEquals(Insumo.TipoInsumo.COADYUVANTE, convertidor.convertToEntityAttribute("COADYUVANTE"));
        assertEquals(Insumo.TipoInsumo.OTROS, convertidor.convertToEntityAttribute("OTRO"));
        assertEquals(Insumo.TipoInsumo.OTROS, convertidor.convertToEntityAttribute("TIPO_INEXISTENTE"));
        assertEquals(Insumo.TipoInsumo.OTROS, convertidor.convertToEntityAttribute(""));
    }

    @Test
    void persisteElNombreDelEnum() {
        assertEquals("HERBICIDA", convertidor.convertToDatabaseColumn(Insumo.TipoInsumo.HERBICIDA));
    }
}
