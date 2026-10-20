package com.agrocloud.feedlot.service;

import com.agrocloud.feedlot.model.dto.FeedlotAliasColumnasPerfil;
import com.agrocloud.feedlot.model.dto.FeedlotEncabezadosMuestra;
import com.agrocloud.feedlot.model.dto.FeedlotLecturaArchivoManga;
import com.agrocloud.feedlot.model.dto.FeedlotMapeoColumnas;
import com.agrocloud.feedlot.model.dto.FeedlotResumenArchivoManga;
import com.agrocloud.feedlot.model.entity.FeedlotAnimal;
import com.agrocloud.feedlot.model.entity.FeedlotSesionManga;
import com.agrocloud.feedlot.model.entity.FeedlotSesionMangaFila;
import com.agrocloud.feedlot.model.enums.FeedlotEstadoAnimal;
import com.agrocloud.feedlot.model.enums.FeedlotResultadoFilaManga;
import com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga;
import com.agrocloud.feedlot.repository.FeedlotAnimalRepository;
import com.agrocloud.feedlot.repository.FeedlotSesionMangaFilaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ServicioFeedlotImportacionManga {

    private static final DateTimeFormatter[] FORMATOS_FECHA = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ISO_LOCAL_DATE
    };

    private static final DateTimeFormatter[] FORMATOS_HORA = {
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm")
    };

    private final FeedlotSesionMangaFilaRepository filaRepository;
    private final ServicioFeedlotNormalizacionEid normalizacionEid;
    private final ObjectMapper objectMapper;
    private final FeedlotAnimalRepository animalRepository;

    public ServicioFeedlotImportacionManga(
            FeedlotSesionMangaFilaRepository filaRepository,
            ServicioFeedlotNormalizacionEid normalizacionEid,
            ObjectMapper objectMapper,
            FeedlotAnimalRepository animalRepository) {
        this.filaRepository = filaRepository;
        this.normalizacionEid = normalizacionEid;
        this.objectMapper = objectMapper;
        this.animalRepository = animalRepository;
    }

    @Transactional
    public void cargarArchivo(FeedlotSesionManga sesion, MultipartFile archivo, FeedlotMapeoColumnas mapeo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo CSV es obligatorio");
        }
        FeedlotMapeoColumnas efectivo = mapeo == null ? mapeoVacio() : mapeo;
        try {
            String texto = leerTexto(archivo);
            ResultadoParseo resultado = parsear(sesion, texto, efectivo);
            filaRepository.eliminarPorSesionId(sesion.getId());
            filaRepository.flush();
            filaRepository.saveAll(resultado.filas);
            sesion.setNombreArchivo(archivo.getOriginalFilename());
            sesion.setDelimitador(resolverDelimitador(texto, efectivo));
            sesion.setPerfilMapeoJson(objectMapper.writeValueAsString(efectivo));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo guardar el mapeo de columnas");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Error leyendo CSV: " + e.getMessage());
        }
    }

    public FeedlotResumenArchivoManga analizar(MultipartFile archivo, FeedlotMapeoColumnas mapeo, Long empresaId) {
        return analizar(archivo, mapeo, empresaId, null, false, null);
    }

    public FeedlotResumenArchivoManga analizar(
            MultipartFile archivo,
            FeedlotMapeoColumnas mapeo,
            Long empresaId,
            FeedlotTipoSesionManga tipo,
            boolean cargaManualPeso,
            Long perfilLecturaId) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo CSV es obligatorio");
        }
        try {
            FeedlotResumenArchivoManga r = analizarTexto(leerTexto(archivo), mapeo, empresaId, tipo, cargaManualPeso);
            r.setPerfilLecturaId(perfilLecturaId);
            return r;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Error leyendo CSV: " + e.getMessage());
        }
    }

    public FeedlotEncabezadosMuestra extraerEncabezados(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo CSV es obligatorio");
        }
        try {
            String texto = leerTexto(archivo);
            FeedlotMapeoColumnas mapeo = mapeoVacio();
            FeedlotResumenArchivoManga r = new FeedlotResumenArchivoManga();
            rellenarMuestra(texto, mapeo, r);
            FeedlotEncabezadosMuestra m = new FeedlotEncabezadosMuestra();
            m.setEncabezados(r.getEncabezados());
            m.setFilaMuestra(r.getFilaMuestra());
            m.setDelimitador(resolverDelimitador(texto, mapeo));
            if (m.getEncabezados() == null || m.getEncabezados().isEmpty()) {
                throw new IllegalArgumentException("No se encontraron encabezados en el archivo");
            }
            return m;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Error leyendo CSV: " + e.getMessage());
        }
    }

    public void aplicarAlias(FeedlotMapeoColumnas mapeo, FeedlotAliasColumnasPerfil alias) {
        if (mapeo == null || alias == null) {
            return;
        }
        mapeo.setAliasEid(alias.getEid());
        mapeo.setAliasVid(alias.getVid());
        mapeo.setAliasPeso(alias.getPeso());
        mapeo.setAliasFecha(alias.getFecha());
        mapeo.setAliasHora(alias.getHora());
    }

    FeedlotResumenArchivoManga analizarTexto(String texto, FeedlotMapeoColumnas mapeo, Long empresaId) {
        return analizarTexto(texto, mapeo, empresaId, null, false);
    }

    FeedlotResumenArchivoManga analizarTexto(
            String texto,
            FeedlotMapeoColumnas mapeo,
            Long empresaId,
            FeedlotTipoSesionManga tipo,
            boolean cargaManualPeso) {
        FeedlotMapeoColumnas efectivo = mapeo == null ? mapeoVacio() : mapeo;
        FeedlotSesionManga dummy = new FeedlotSesionManga();
        dummy.setEmpresaId(empresaId);
        ResultadoParseo resultado = parsear(dummy, texto, efectivo);
        FeedlotResumenArchivoManga r = new FeedlotResumenArchivoManga();
        r.setMapeo(efectivo);
        r.setLecturas(resultado.filas.size());
        r.setEidRepetidos(resultado.eidRepetidos);
        r.setEntendioArchivo(efectivo.getIndiceEid() != null);
        int pesos = 0;
        Map<LocalDate, Integer> porFecha = new HashMap<>();
        LocalTime desde = null;
        LocalTime hasta = null;
        for (FeedlotSesionMangaFila f : resultado.filas) {
            if (f.getPesoKg() != null) {
                pesos++;
            }
            if (f.getFechaHoraLectura() != null) {
                LocalDate d = f.getFechaHoraLectura().toLocalDate();
                porFecha.merge(d, 1, Integer::sum);
                LocalTime t = f.getFechaHoraLectura().toLocalTime();
                if (!t.equals(LocalTime.MIDNIGHT) || f.getFechaHoraLectura().getNano() != 0) {
                    if (desde == null || t.isBefore(desde)) {
                        desde = t;
                    }
                    if (hasta == null || t.isAfter(hasta)) {
                        hasta = t;
                    }
                }
            }
        }
        r.setPesosEncontrados(pesos);
        r.setTienePeso(pesos > 0);
        r.setFechaMayoritaria(porFecha.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null));
        r.setHoraDesde(desde);
        r.setHoraHasta(hasta);
        rellenarMuestra(texto, efectivo, r);
        sugerirLote(r, resultado.filas, empresaId);
        List<FeedlotLecturaArchivoManga> detalle = new ArrayList<>();
        for (FeedlotSesionMangaFila f : resultado.filas) {
            FeedlotLecturaArchivoManga l = new FeedlotLecturaArchivoManga();
            l.setEidCrudo(f.getEidCrudo());
            l.setEidNormalizado(f.getEidNormalizado());
            l.setCaravanaVisual(f.getCaravanaVisual());
            l.setPesoKg(f.getPesoKg());
            l.setFechaHoraLectura(f.getFechaHoraLectura());
            detalle.add(l);
        }
        r.setLecturasDetalle(detalle);
        r.setMensajeResumen(armarMensaje(r));
        completarCoherenciaArchivo(r, resultado.filas, tipo, cargaManualPeso);
        return r;
    }

    ResultadoParseo parsear(FeedlotSesionManga sesion, String texto, FeedlotMapeoColumnas mapeo) {
        String delim = resolverDelimitador(texto, mapeo);
        String[] lineas = texto.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        List<FeedlotSesionMangaFila> filas = new ArrayList<>();
        boolean primera = true;
        int numero = 0;
        for (String linea : lineas) {
            if (linea == null || linea.isBlank()) {
                continue;
            }
            String[] cols = linea.split(java.util.regex.Pattern.quote(delim), -1);
            if (primera) {
                primera = false;
                if (pareceEncabezado(cols, mapeo)) {
                    completarMapeoDesdeEncabezado(cols, mapeo);
                    continue;
                }
                if (mapeo.getIndiceEid() == null) {
                    mapeo.setIndiceEid(0);
                }
            }
            numero++;
            FeedlotSesionMangaFila fila = new FeedlotSesionMangaFila();
            fila.setSesion(sesion);
            fila.setEmpresaId(sesion.getEmpresaId());
            fila.setNumeroLinea(numero);
            fila.setEidCrudo(celda(cols, mapeo.getIndiceEid()));
            normalizacionEid.normalizar(fila.getEidCrudo()).ifPresent(fila::setEidNormalizado);
            String visual = celda(cols, mapeo.getIndiceCaravanaVisual());
            fila.setCaravanaVisual(visual == null ? null : visual.trim());
            fila.setPesoKg(parsearPeso(celda(cols, mapeo.getIndicePeso())));
            fila.setFechaHoraLectura(parsearFechaHora(
                    celda(cols, mapeo.getIndiceFechaHora()),
                    celda(cols, mapeo.getIndiceHora())));
            fila.setResultado(FeedlotResultadoFilaManga.OK);
            filas.add(fila);
        }
        if (filas.isEmpty()) {
            throw new IllegalArgumentException("El archivo no contiene filas de lectura");
        }
        return colapsarDuplicados(filas);
    }

    private ResultadoParseo colapsarDuplicados(List<FeedlotSesionMangaFila> filas) {
        LinkedHashMap<String, FeedlotSesionMangaFila> porClave = new LinkedHashMap<>();
        int repetidos = 0;
        for (FeedlotSesionMangaFila fila : filas) {
            String clave = claveIdentidad(fila);
            if (clave == null) {
                porClave.put("linea-" + fila.getNumeroLinea(), fila);
                continue;
            }
            if (porClave.containsKey(clave)) {
                repetidos++;
            }
            porClave.put(clave, fila);
        }
        List<FeedlotSesionMangaFila> unicas = new ArrayList<>(porClave.values());
        int linea = 0;
        for (FeedlotSesionMangaFila f : unicas) {
            linea++;
            f.setNumeroLinea(linea);
        }
        return new ResultadoParseo(unicas, repetidos);
    }

    private static String claveIdentidad(FeedlotSesionMangaFila fila) {
        if (fila.getEidNormalizado() != null && !fila.getEidNormalizado().isBlank()) {
            return "e:" + fila.getEidNormalizado();
        }
        if (fila.getCaravanaVisual() != null && !fila.getCaravanaVisual().isBlank()) {
            return "v:" + fila.getCaravanaVisual().trim().toLowerCase();
        }
        return null;
    }

    private void completarMapeoDesdeEncabezado(String[] cols, FeedlotMapeoColumnas mapeo) {
        for (int i = 0; i < cols.length; i++) {
            String nombre = cols[i] == null ? "" : cols[i].trim().toLowerCase();
            if (nombre.isEmpty()) {
                continue;
            }
            if (mapeo.getIndiceEid() == null
                    && (coincideAlias(nombre, mapeo.getAliasEid())
                    || nombre.equals("eid") || nombre.contains("electronic"))) {
                mapeo.setIndiceEid(i);
            }
            if (mapeo.getIndiceCaravanaVisual() == null
                    && (coincideAlias(nombre, mapeo.getAliasVid())
                    || nombre.equals("vid") || nombre.equals("lid")
                    || nombre.contains("visual") || nombre.contains("caravana"))) {
                mapeo.setIndiceCaravanaVisual(i);
            }
            if (mapeo.getIndicePeso() == null
                    && (coincideAlias(nombre, mapeo.getAliasPeso())
                    || nombre.equals("weight") || nombre.startsWith("weight")
                    || nombre.equals("peso") || nombre.startsWith("peso"))) {
                mapeo.setIndicePeso(i);
            }
            if (mapeo.getIndiceFechaHora() == null
                    && (coincideAlias(nombre, mapeo.getAliasFecha())
                    || nombre.equals("date") || nombre.equals("fecha")
                    || nombre.startsWith("date") || nombre.startsWith("fecha"))) {
                mapeo.setIndiceFechaHora(i);
            }
            if (mapeo.getIndiceHora() == null
                    && (coincideAlias(nombre, mapeo.getAliasHora())
                    || nombre.equals("time") || nombre.equals("hora")
                    || nombre.startsWith("time") || nombre.startsWith("hora"))) {
                mapeo.setIndiceHora(i);
            }
        }
        if (mapeo.getIndiceEid() == null) {
            mapeo.setIndiceEid(0);
        }
    }

    private static boolean coincideAlias(String nombre, List<String> alias) {
        if (alias == null) {
            return false;
        }
        for (String a : alias) {
            if (a != null && nombre.equals(a.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private static BigDecimal parsearPeso(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String t = valor.trim().replaceAll("(?i)\\s*kgs?\\s*$", "").trim();
        if (t.matches(".*\\d\\.\\d{3},\\d+.*")) {
            t = t.replace(".", "").replace(',', '.');
        } else if (t.matches(".*\\d,\\d{3}\\.\\d+.*")) {
            t = t.replace(",", "");
        } else {
            t = t.replace(',', '.');
        }
        try {
            BigDecimal peso = new BigDecimal(t);
            return peso.signum() > 0 ? peso : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean pareceEncabezado(String[] cols, FeedlotMapeoColumnas mapeo) {
        String unido = String.join(" ", cols).toLowerCase();
        if (unido.contains("eid") || unido.contains("identific") || unido.contains("caravana")
                || unido.contains("tag") || unido.contains("peso") || unido.contains("weight")
                || unido.contains("vid") || unido.contains("date")) {
            return true;
        }
        String eid = celda(cols, mapeo.getIndiceEid() != null ? mapeo.getIndiceEid() : 0);
        return eid != null && normalizacionEid.normalizar(eid).isEmpty();
    }

    private String resolverDelimitador(String texto, FeedlotMapeoColumnas mapeo) {
        if (mapeo.getDelimitador() != null && !mapeo.getDelimitador().isBlank()
                && !"AUTO".equalsIgnoreCase(mapeo.getDelimitador())) {
            if ("\\t".equals(mapeo.getDelimitador()) || "TAB".equalsIgnoreCase(mapeo.getDelimitador())) {
                return "\t";
            }
            return mapeo.getDelimitador();
        }
        String primera = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8))
                .lines().filter(l -> !l.isBlank()).findFirst().orElse("");
        long puntocoma = primera.chars().filter(c -> c == ';').count();
        long coma = primera.chars().filter(c -> c == ',').count();
        long tab = primera.chars().filter(c -> c == '\t').count();
        if (tab >= puntocoma && tab >= coma && tab > 0) {
            return "\t";
        }
        if (puntocoma > coma) {
            return ";";
        }
        return ",";
    }

    private static String celda(String[] cols, Integer indice) {
        if (indice == null || indice < 0 || indice >= cols.length) {
            return null;
        }
        String v = cols[indice];
        return v == null || v.isBlank() ? null : v.trim();
    }

    private static LocalDateTime parsearFechaHora(String fecha, String hora) {
        LocalDateTime base = parsearFecha(fecha);
        if (base == null) {
            return null;
        }
        LocalTime t = parsearHora(hora);
        if (t == null) {
            return base;
        }
        return LocalDateTime.of(base.toLocalDate(), t);
    }

    private static LocalDateTime parsearFecha(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String t = valor.trim();
        for (DateTimeFormatter fmt : FORMATOS_FECHA) {
            try {
                return LocalDateTime.parse(t, fmt);
            } catch (DateTimeParseException ignored) {
                try {
                    return LocalDate.parse(t, fmt).atStartOfDay();
                } catch (DateTimeParseException ignored2) {
                    // siguiente formato
                }
            }
        }
        return null;
    }

    private static LocalTime parsearHora(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String t = valor.trim();
        for (DateTimeFormatter fmt : FORMATOS_HORA) {
            try {
                return LocalTime.parse(t, fmt);
            } catch (DateTimeParseException ignored) {
                // siguiente
            }
        }
        return null;
    }

    private String leerTexto(MultipartFile archivo) throws Exception {
        byte[] bytes = archivo.getBytes();
        String texto = new String(bytes, StandardCharsets.UTF_8);
        if (texto.contains("\uFFFD")) {
            texto = new String(bytes, Charset.forName("windows-1252"));
        }
        return texto;
    }

    private static FeedlotMapeoColumnas mapeoVacio() {
        FeedlotMapeoColumnas m = new FeedlotMapeoColumnas();
        m.setDelimitador("AUTO");
        return m;
    }

    private void rellenarMuestra(String texto, FeedlotMapeoColumnas mapeo, FeedlotResumenArchivoManga r) {
        String delim = resolverDelimitador(texto, mapeo);
        String[] lineas = texto.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        boolean encabezado = false;
        for (String linea : lineas) {
            if (linea == null || linea.isBlank()) {
                continue;
            }
            String[] cols = linea.split(java.util.regex.Pattern.quote(delim), -1);
            if (!encabezado && pareceEncabezado(cols, mapeo)) {
                r.setEncabezados(List.of(cols));
                encabezado = true;
                continue;
            }
            r.setFilaMuestra(List.of(cols));
            break;
        }
    }

    private void sugerirLote(FeedlotResumenArchivoManga r, List<FeedlotSesionMangaFila> filas, Long empresaId) {
        if (empresaId == null) {
            return;
        }
        List<String> eids = filas.stream()
                .map(FeedlotSesionMangaFila::getEidNormalizado)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (eids.isEmpty()) {
            return;
        }
        List<FeedlotAnimal> animales = animalRepository.listarActivosPorEids(
                empresaId, FeedlotEstadoAnimal.ACTIVO, eids);
        if (animales == null || animales.isEmpty()) {
            return;
        }
        Map<Long, Long> porLote = new HashMap<>();
        Map<Long, String> nombres = new HashMap<>();
        for (FeedlotAnimal a : animales) {
            if (a.getLote() == null) {
                continue;
            }
            porLote.merge(a.getLote().getId(), 1L, Long::sum);
            nombres.put(a.getLote().getId(), a.getLote().getNombre());
        }
        if (porLote.size() > 1) {
            r.setMezclaLotes(true);
            Long mayor = porLote.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
            for (FeedlotAnimal a : animales) {
                if (a.getLote() != null && mayor != null && !a.getLote().getId().equals(mayor)
                        && r.getEidsOtroLote().size() < 8) {
                    r.getEidsOtroLote().add(a.getEid());
                }
            }
            return;
        }
        if (porLote.size() == 1) {
            Map.Entry<Long, Long> unico = porLote.entrySet().iterator().next();
            r.setLoteSugeridoId(unico.getKey());
            r.setLoteSugeridoNombre(nombres.get(unico.getKey()));
        }
    }

    private static String armarMensaje(FeedlotResumenArchivoManga r) {
        StringBuilder sb = new StringBuilder();
        sb.append(r.getLecturas()).append(r.getLecturas() == 1 ? " lectura" : " lecturas");
        if (r.getFechaMayoritaria() != null) {
            sb.append(" el ").append(r.getFechaMayoritaria().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        if (r.getHoraDesde() != null && r.getHoraHasta() != null) {
            sb.append(" entre ").append(r.getHoraDesde()).append(" y ").append(r.getHoraHasta());
        }
        sb.append(". ").append(r.getPesosEncontrados()).append(r.getPesosEncontrados() == 1 ? " peso." : " pesos.");
        if (r.getEidRepetidos() > 0) {
            sb.append(" ").append(r.getEidRepetidos())
                    .append(r.getEidRepetidos() == 1 ? " EID repetido (se toma la última)." : " EID repetidos (se toma la última).");
        }
        return sb.toString();
    }

    private void completarCoherenciaArchivo(
            FeedlotResumenArchivoManga r,
            List<FeedlotSesionMangaFila> filas,
            FeedlotTipoSesionManga tipo,
            boolean cargaManualPeso) {
        int conEid = 0;
        int eidInvalidos = 0;
        for (FeedlotSesionMangaFila f : filas) {
            String crudo = f.getEidCrudo();
            if (crudo == null || crudo.isBlank()) {
                continue;
            }
            conEid++;
            if (f.getEidNormalizado() == null || f.getEidNormalizado().isBlank()) {
                eidInvalidos++;
            } else if (crudo.matches("(?i)^[0-9]*\\.?[0-9]+e[+-]?[0-9]+$")) {
                eidInvalidos++;
            }
        }
        int porcentaje = conEid == 0 ? 0 : (eidInvalidos * 100) / conEid;
        r.setPorcentajeEidInvalidos(porcentaje);
        if (r.isTienePeso()) {
            r.setPerfilArchivo("SESION_CON_PESO");
        } else if (r.getEncabezados() != null && r.getEncabezados().size() <= 1) {
            r.setPerfilArchivo("EID_ONLY");
        } else if (r.getMapeo() != null && r.getMapeo().getIndiceFechaHora() != null) {
            r.setPerfilArchivo("SESION_ESTANDAR");
        } else {
            r.setPerfilArchivo("DESCONOCIDO");
        }
        List<String> noMapeadas = new ArrayList<>();
        if (r.getEncabezados() != null && r.getMapeo() != null) {
            FeedlotMapeoColumnas m = r.getMapeo();
            for (int i = 0; i < r.getEncabezados().size(); i++) {
                if (Objects.equals(m.getIndiceEid(), i)
                        || Objects.equals(m.getIndiceCaravanaVisual(), i)
                        || Objects.equals(m.getIndicePeso(), i)
                        || Objects.equals(m.getIndiceFechaHora(), i)
                        || Objects.equals(m.getIndiceHora(), i)) {
                    continue;
                }
                String h = r.getEncabezados().get(i);
                if (h != null && !h.isBlank()) {
                    noMapeadas.add(h.trim());
                }
            }
        }
        r.setColumnasNoMapeadas(noMapeadas);
        if (porcentaje > 50) {
            r.getBloqueantes().add("Más de la mitad de los EID no se pudieron leer. Revisá que Excel no los haya pasado a notación científica.");
        }
        if (tipo == FeedlotTipoSesionManga.PESADA && !r.isTienePeso() && !cargaManualPeso) {
            r.getBloqueantes().add("Este archivo no tiene pesos. Usá el CSV del cabezal o marcá que vas a cargar el kilo a mano.");
        }
        if (!noMapeadas.isEmpty()) {
            r.getAvisos().add("Se ignoran columnas que no aplican a este evento: " + String.join(", ", noMapeadas));
        }
        if (tipo != null && tipo != FeedlotTipoSesionManga.PESADA && r.isTienePeso()) {
            r.getAvisos().add("El archivo trae peso; se ignora porque elegiste " + nombreEvento(tipo) + ".");
        }
        if (r.getEidRepetidos() > 0) {
            r.getAvisos().add("Hay EID repetidos: se toma la última lectura.");
        }
    }

    private static String nombreEvento(FeedlotTipoSesionManga tipo) {
        return switch (tipo) {
            case ENROLAMIENTO -> "enrolar";
            case SANIDAD -> "aplicar producto";
            case RECUENTO -> "contar";
            case BAJA -> "dar de baja";
            default -> tipo.name().toLowerCase();
        };
    }

    static final class ResultadoParseo {
        final List<FeedlotSesionMangaFila> filas;
        final int eidRepetidos;

        ResultadoParseo(List<FeedlotSesionMangaFila> filas, int eidRepetidos) {
            this.filas = filas;
            this.eidRepetidos = eidRepetidos;
        }
    }
}
