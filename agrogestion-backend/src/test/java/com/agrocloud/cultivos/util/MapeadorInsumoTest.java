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

    @Test
    void desdeResumen_aceptaTipoDelWizard() {
        InsumoDTO dto = MapeadorInsumo.desdeResumen(new com.agrocloud.core.inventory.infrastructure.InsumoResumenProyeccion() {
            @Override public Long getId() { return 3L; }
            @Override public String getNombre() { return "Vacuna aftosa"; }
            @Override public String getDescripcion() { return null; }
            @Override public String getTipo() { return "COADYUVANTE"; }
            @Override public String getUnidadMedida() { return "ml"; }
            @Override public BigDecimal getPrecioUnitario() { return BigDecimal.ONE; }
            @Override public BigDecimal getStockActual() { return BigDecimal.TEN; }
            @Override public BigDecimal getStockMinimo() { return BigDecimal.ONE; }
            @Override public String getProveedor() { return null; }
            @Override public Boolean getActivo() { return true; }
        });
        assertEquals("COADYUVANTE", dto.getTipo());
        assertTrue(dto.getEsAgroquimico());
        assertEquals("Vacuna aftosa", dto.getNombre());
    }

    @Test
    void desdeResumen_otroSeNormalizaAOtros() {
        InsumoDTO dto = MapeadorInsumo.desdeResumen(new com.agrocloud.core.inventory.infrastructure.InsumoResumenProyeccion() {
            @Override public Long getId() { return 4L; }
            @Override public String getNombre() { return "Producto"; }
            @Override public String getDescripcion() { return null; }
            @Override public String getTipo() { return "OTRO"; }
            @Override public String getUnidadMedida() { return "kg"; }
            @Override public BigDecimal getPrecioUnitario() { return BigDecimal.ONE; }
            @Override public BigDecimal getStockActual() { return BigDecimal.ONE; }
            @Override public BigDecimal getStockMinimo() { return BigDecimal.ONE; }
            @Override public String getProveedor() { return null; }
            @Override public Boolean getActivo() { return true; }
        });
        assertEquals("OTROS", dto.getTipo());
    }

    @Test
    void desdeFilaNativa_mapeaIdAunqueVengaComoEntero() {
        InsumoDTO dto = MapeadorInsumo.desdeFilaNativa(new Object[] {
                7, "Vacuna", null, "OTROS", "ml",
                new BigDecimal("10"), new BigDecimal("50"), new BigDecimal("1"),
                null, 1
        });
        assertEquals(7L, dto.getId());
        assertEquals("Vacuna", dto.getNombre());
        assertEquals("OTROS", dto.getTipo());
        assertEquals(0, new BigDecimal("50").compareTo(dto.getStockActual()));
    }

    @Test
    void desdeFilaNativa_desanidaFilaEnvueltaPorHibernate() {
        Object[] columnas = new Object[] {
                9, "Ivermectina", null, "OTROS", "ml",
                new BigDecimal("1"), new BigDecimal("20"), new BigDecimal("1"),
                null, 1
        };
        InsumoDTO dto = MapeadorInsumo.desdeFilaNativa(new Object[] { columnas });
        assertEquals(9L, dto.getId());
        assertEquals("Ivermectina", dto.getNombre());
    }
}
