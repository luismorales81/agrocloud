package com.agrocloud.feedlot;

import com.agrocloud.feedlot.service.ServicioFeedlotNormalizacionEid;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioFeedlotNormalizacionEidTest {

    private final ServicioFeedlotNormalizacionEid servicio = new ServicioFeedlotNormalizacionEid();

    @Test
    void normalizaDecimalConEspacios() {
        assertEquals("982000123456789", servicio.normalizar(" 982 000123456789 ").orElseThrow());
    }

    @Test
    void rellenaCerosHastaQuince() {
        assertEquals("000000000000123", servicio.normalizar("123").orElseThrow());
    }

    @Test
    void convierteHexCorto() {
        assertEquals("000000000000255", servicio.normalizar("FF").orElseThrow());
    }

    @Test
    void invalidoVacio() {
        assertTrue(servicio.normalizar("###").isEmpty());
        assertTrue(servicio.normalizar(" ").isEmpty());
    }
}
