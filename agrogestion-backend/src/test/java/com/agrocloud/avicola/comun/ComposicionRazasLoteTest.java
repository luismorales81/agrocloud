package com.agrocloud.avicola.comun;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComposicionRazasLoteTest {

    @Test
    void idsSolicitados_listaVarias_sinRepetir() {
        assertEquals(List.of(2L, 5L), ComposicionRazasLote.idsSolicitados(List.of(2L, 5L), 9L, true));
    }

    @Test
    void idsSolicitados_soloRazaSuelta() {
        assertEquals(List.of(4L), ComposicionRazasLote.idsSolicitados(null, 4L, true));
    }

    @Test
    void idsSolicitados_sinDatosYNoObligatorio_devuelveNulo() {
        assertNull(ComposicionRazasLote.idsSolicitados(null, null, false));
    }

    @Test
    void idsSolicitados_vacia_rechaza() {
        assertThrows(IllegalArgumentException.class, () -> ComposicionRazasLote.idsSolicitados(List.of(), null, false));
    }

    @Test
    void idsSolicitados_repetida_rechaza() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ComposicionRazasLote.idsSolicitados(List.of(1L, 1L), null, true));
    }

    @Test
    void unirNombres_ordena() {
        assertEquals("Hy-Line · Lohmann", ComposicionRazasLote.unirNombres(List.of("Lohmann", "Hy-Line")));
    }
}
