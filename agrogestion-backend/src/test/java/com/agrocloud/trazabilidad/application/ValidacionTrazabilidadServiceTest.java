package com.agrocloud.trazabilidad.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.agrocloud.trazabilidad.domain.TrazabilidadCertificacion;
import com.agrocloud.trazabilidad.domain.TrazabilidadReglaCertificacion;
import com.agrocloud.trazabilidad.domain.TrazabilidadReporte;
import com.agrocloud.trazabilidad.dto.HechosTrazabilidadDocumento;
import com.agrocloud.trazabilidad.dto.IncidenciaValidacionTrazabilidad;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidacionTrazabilidadServiceTest {

    @Test
    void libreAgroquimicos_rechazaHerbicida() {
        ValidacionTrazabilidadService s = new ValidacionTrazabilidadService();
        ReflectionTestUtils.setField(s, "objectMapper", new ObjectMapper());
        TrazabilidadCertificacion c = new TrazabilidadCertificacion();
        c.setCodigo("LIBRE_AGROQUIMICOS");
        TrazabilidadReglaCertificacion r = new TrazabilidadReglaCertificacion();
        r.setTipoRegla("NINGUNO_TIPOS_INSUMO");
        r.setParametrosJson("{\"tiposInsumoProhibidos\":[\"HERBICIDA\"]}");
        r.setOrdenEjecucion(0);
        r.setActiva(true);
        r.setCertificacion(c);
        c.getReglas().add(r);
        HechosTrazabilidadDocumento h = new HechosTrazabilidadDocumento();
        HechosTrazabilidadDocumento.LineaInsumoLabor li = new HechosTrazabilidadDocumento.LineaInsumoLabor();
        li.setTipoInsumo("HERBICIDA");
        li.setNombreInsumo("X");
        li.setIdLabor(1L);
        h.getInsumosPorLabor().add(li);
        h.getLabores().add(new HechosTrazabilidadDocumento.LineaHechoLabor());
        List<IncidenciaValidacionTrazabilidad> inc = new ArrayList<>();
        TrazabilidadReporte.ResultadoReporte res = s.validarOIncumple(h, c, inc);
        assertEquals(TrazabilidadReporte.ResultadoReporte.INVALIDO, res);
        assertTrue(inc.stream().anyMatch(i -> "INSUMO_PROHIBIDO".equals(i.getCodigo())));
    }
}
