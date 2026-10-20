package com.agrocloud.feedlot.service;

import com.agrocloud.feedlot.model.dto.FeedlotMapeoColumnas;
import com.agrocloud.feedlot.model.entity.FeedlotSesionManga;
import com.agrocloud.feedlot.model.entity.FeedlotSesionMangaFila;
import com.agrocloud.feedlot.repository.FeedlotAnimalRepository;
import com.agrocloud.feedlot.repository.FeedlotSesionMangaFilaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ServicioFeedlotImportacionMangaEjemplosTest {

    private static final String BASE = "scripts/ejemplos-manga-baston";

    @Mock
    private FeedlotSesionMangaFilaRepository filaRepository;
    @Mock
    private FeedlotAnimalRepository animalRepository;

    private ServicioFeedlotImportacionManga servicio;
    private FeedlotSesionManga sesion;

    @BeforeEach
    void setUp() {
        servicio = new ServicioFeedlotImportacionManga(
                filaRepository, new ServicioFeedlotNormalizacionEid(), new ObjectMapper(), animalRepository);
        sesion = new FeedlotSesionManga();
        sesion.setEmpresaId(1L);
    }

    @Test
    void enrolamientoFabrica() throws Exception {
        List<FeedlotSesionMangaFila> filas = parsear("corrida-01-fabrica/1-enrolar.csv", mapeo(0, 1, null, 2));
        assertEquals(8, filas.size());
        assertEquals("982000123456701", filas.get(0).getEidNormalizado());
        assertEquals("AG-MANGA-01", filas.get(0).getCaravanaVisual());
        assertEquals(LocalDate.of(2026, 9, 12), filas.get(0).getFechaHoraLectura().toLocalDate());
        assertEquals("982000123456708", filas.get(7).getEidNormalizado());
    }

    @Test
    void pesadaFabrica() throws Exception {
        List<FeedlotSesionMangaFila> filas = parsear("corrida-01-fabrica/2-pesar.csv", mapeo(0, 1, 2, 3));
        assertEquals(8, filas.size());
        assertEquals(0, new BigDecimal("286.5").compareTo(filas.get(0).getPesoKg()));
        assertEquals(0, new BigDecimal("255.6").compareTo(filas.get(7).getPesoKg()));
    }

    @Test
    void pesadaInfereWeightSiNoHayMapeoDePeso() throws Exception {
        List<FeedlotSesionMangaFila> filas = parsear("corrida-01-fabrica/2-pesar.csv", mapeo(0, null, null, null));
        assertEquals(8, filas.size());
        assertEquals(0, new BigDecimal("286.5").compareTo(filas.get(0).getPesoKg()));
    }

    @Test
    void sanidadFabrica() throws Exception {
        List<FeedlotSesionMangaFila> filas = parsear("corrida-01-fabrica/3-sanidad.csv", mapeo(0, 1, null, 2));
        assertEquals(8, filas.size());
        assertTrue(filas.stream().allMatch(f -> f.getEidNormalizado() != null && f.getEidNormalizado().length() == 15));
    }

    @Test
    void recuentoConIntruso() throws Exception {
        List<FeedlotSesionMangaFila> filas = parsear("corrida-01-fabrica/4-contar.csv", mapeo(0, null, null, null));
        assertEquals(7, filas.size());
        assertEquals("982000999111222", filas.get(6).getEidNormalizado());
    }

    @Test
    void bajaDosAnimales() throws Exception {
        List<FeedlotSesionMangaFila> filas = parsear("corrida-01-fabrica/5-baja.csv", mapeo(0, 1, null, 2));
        assertEquals(2, filas.size());
        assertEquals("982000123456707", filas.get(0).getEidNormalizado());
        assertEquals("982000123456708", filas.get(1).getEidNormalizado());
    }

    @Test
    void duplicadosQuedaLaUltima() {
        FeedlotMapeoColumnas m = mapeo(0, null, null, null);
        String csv = "EID\n982 000123456701\n982 000123456702\n982 000123456701\n";
        var r = servicio.parsear(sesion, csv, m);
        assertEquals(2, r.filas.size());
        assertEquals(1, r.eidRepetidos);
        assertEquals("982000123456701", r.filas.get(0).getEidNormalizado());
        assertEquals("982000123456702", r.filas.get(1).getEidNormalizado());
    }

    @Test
    void analisisDetectaPesoYFecha() throws Exception {
        var resumen = analizar("corrida-01-fabrica/2-pesar.csv", mapeo(null, null, null, null));
        assertEquals(8, resumen.getLecturas());
        assertTrue(resumen.isTienePeso());
        assertEquals(LocalDate.of(2026, 9, 12), resumen.getFechaMayoritaria());
    }

    @Test
    void aliasPesoVivoLeeKilos() throws Exception {
        FeedlotMapeoColumnas m = mapeo(null, null, null, null);
        m.setAliasPeso(List.of("Peso vivo"));
        var resumen = servicio.analizarTexto(
                texto("corrida-05-peso-vivo/2-pesar.csv"), m, 1L,
                com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga.PESADA, false);
        assertEquals(8, resumen.getLecturas());
        assertTrue(resumen.isTienePeso());
        assertEquals(0, new BigDecimal("286.5").compareTo(resumen.getLecturasDetalle().get(0).getPesoKg()));
        assertTrue(resumen.getBloqueantes().isEmpty());
    }

    @Test
    void perfilCustomKilosSinAliasNoDetectaPeso() throws Exception {
        var resumen = servicio.analizarTexto(
                texto("corrida-03-kilos/2-pesar.csv"), mapeo(null, null, null, null), 1L,
                com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga.PESADA, false);
        assertEquals(8, resumen.getLecturas());
        assertTrue(!resumen.isTienePeso());
        assertTrue(resumen.getBloqueantes().stream().anyMatch(b -> b.toLowerCase().contains("peso")));
    }

    @Test
    void perfilCustomKilosConAliasLeePesoYFecha() throws Exception {
        FeedlotMapeoColumnas m = mapeo(null, null, null, null);
        m.setAliasEid(List.of("Identificador"));
        m.setAliasVid(List.of("TagVisual"));
        m.setAliasPeso(List.of("Kilos"));
        m.setAliasFecha(List.of("Dia"));
        m.setAliasHora(List.of("Instante"));
        var resumen = servicio.analizarTexto(
                texto("corrida-03-kilos/2-pesar.csv"), m, 1L,
                com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga.PESADA, false);
        assertTrue(resumen.isTienePeso());
        assertEquals(8, resumen.getPesosEncontrados());
        assertEquals(LocalDate.of(2026, 9, 16), resumen.getFechaMayoritaria());
        assertEquals("AG-MANGA-01", resumen.getLecturasDetalle().get(0).getCaravanaVisual());
        assertTrue(resumen.getBloqueantes().isEmpty());
    }

    @Test
    void puntoYComaConAlias() throws Exception {
        FeedlotMapeoColumnas m = mapeo(null, null, null, null);
        m.setAliasEid(List.of("Identificador"));
        m.setAliasPeso(List.of("Kilos"));
        m.setAliasFecha(List.of("Dia"));
        var resumen = servicio.analizarTexto(
                texto("corrida-08-punto-y-coma/2-pesar.csv"), m, 1L,
                com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga.PESADA, false);
        assertEquals(8, resumen.getLecturas());
        assertTrue(resumen.isTienePeso());
    }

    @Test
    void extrasDraftBcsQuedanComoNoMapeadas() throws Exception {
        FeedlotMapeoColumnas m = mapeo(null, null, null, null);
        m.setAliasEid(List.of("Identificador"));
        m.setAliasPeso(List.of("Kilos"));
        var resumen = servicio.analizarTexto(
                texto("corrida-09-draft-bcs/2-pesar.csv"), m, 1L,
                com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga.PESADA, false);
        assertTrue(resumen.getColumnasNoMapeadas().stream().anyMatch(c -> c.equalsIgnoreCase("Draft")));
        assertTrue(resumen.getColumnasNoMapeadas().stream().anyMatch(c -> c.equalsIgnoreCase("BCS")));
    }

    @Test
    void caravanaElecConAliasCuentaSiete() throws Exception {
        FeedlotMapeoColumnas m = mapeo(null, null, null, null);
        m.setAliasEid(List.of("CaravanaElec"));
        var resumen = servicio.analizarTexto(
                texto("corrida-04-caravana-elec/3-contar-faltantes.csv"), m, 1L,
                com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga.RECUENTO, false);
        assertEquals(7, resumen.getLecturas());
        assertTrue(resumen.getBloqueantes().isEmpty());
    }

    @Test
    void recuentoConPesoAvisaEIgnoraParaElEvento() throws Exception {
        var resumen = servicio.analizarTexto(
                texto("corrida-01-fabrica/2-pesar.csv"), mapeo(null, null, null, null), 1L,
                com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga.RECUENTO, false);
        assertTrue(resumen.isTienePeso());
        assertTrue(resumen.getAvisos().stream().anyMatch(a -> a.toLowerCase().contains("peso")));
    }

    @Test
    void notacionCientificaBloquea() throws Exception {
        var resumen = analizar("corrida-06-bloqueos/5-eid-excel.csv", mapeo(null, null, null, null));
        assertTrue(resumen.getPorcentajeEidInvalidos() > 50);
        assertTrue(resumen.getBloqueantes().stream().anyMatch(b -> b.toLowerCase().contains("excel")
                || b.toLowerCase().contains("científica") || b.toLowerCase().contains("cientifica")));
    }

    @Test
    void pesadaSinPesoBloqueaSiNoEsCargaManual() throws Exception {
        var resumen = servicio.analizarTexto(
                texto("corrida-01-fabrica/1-enrolar.csv"), mapeo(null, null, null, null), 1L,
                com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga.PESADA, false);
        assertTrue(resumen.getBloqueantes().stream().anyMatch(b -> b.toLowerCase().contains("peso")));
    }

    private List<FeedlotSesionMangaFila> parsear(String relativo, FeedlotMapeoColumnas mapeo) throws Exception {
        return servicio.parsear(sesion, texto(relativo), mapeo).filas;
    }

    private com.agrocloud.feedlot.model.dto.FeedlotResumenArchivoManga analizar(String relativo, FeedlotMapeoColumnas mapeo)
            throws Exception {
        return servicio.analizarTexto(texto(relativo), mapeo, 1L);
    }

    private static String texto(String relativo) throws Exception {
        return Files.readString(Path.of(BASE, relativo), StandardCharsets.UTF_8);
    }

    private static FeedlotMapeoColumnas mapeo(Integer eid, Integer visual, Integer peso, Integer fecha) {
        FeedlotMapeoColumnas m = new FeedlotMapeoColumnas();
        m.setIndiceEid(eid);
        m.setIndiceCaravanaVisual(visual);
        m.setIndicePeso(peso);
        m.setIndiceFechaHora(fecha);
        m.setDelimitador("AUTO");
        return m;
    }
}
