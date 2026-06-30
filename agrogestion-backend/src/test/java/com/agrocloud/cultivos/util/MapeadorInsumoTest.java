package com.agrocloud.cultivos.util;

import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.dto.InsumoDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MapeadorInsumoTest {

    @Test
    void aDtoListado_mapeaCamposSinRelacionesLazy() {
        Insumo insumo = new Insumo();
        insumo.setId(10L);
        insumo.setNombre("Balanceado demo");
        insumo.setDescripcion("Ración feedlot");
        insumo.setTipo(Insumo.TipoInsumo.OTROS);
        insumo.setUnidadMedida("kg");
        insumo.setPrecioUnitario(new BigDecimal("420.00"));
        insumo.setStockActual(new BigDecimal("1000"));
        insumo.setStockMinimo(new BigDecimal("100"));
        insumo.setActivo(true);

        InsumoDTO dto = MapeadorInsumo.aDtoListado(insumo);

        assertEquals(10L, dto.getId());
        assertEquals("Balanceado demo", dto.getNombre());
        assertEquals("OTROS", dto.getTipo());
        assertEquals("kg", dto.getUnidadMedida());
        assertEquals(new BigDecimal("1000"), dto.getStockActual());
        assertFalse(dto.getEsAgroquimico());
    }
}
