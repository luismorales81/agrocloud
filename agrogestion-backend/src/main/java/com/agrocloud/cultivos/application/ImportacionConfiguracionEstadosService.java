package com.agrocloud.cultivos.application;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.exception.ImportacionConfiguracionException;
import com.agrocloud.cultivos.domain.EstadoLoteConfig;
import com.agrocloud.cultivos.domain.TareaPorEstadoConfig;
import com.agrocloud.cultivos.domain.TipoCultivo;
import com.agrocloud.cultivos.domain.TransicionEstadoConfig;
import com.agrocloud.cultivos.infrastructure.EstadoLoteConfigRepository;
import com.agrocloud.cultivos.infrastructure.TareaPorEstadoConfigRepository;
import com.agrocloud.cultivos.infrastructure.TipoCultivoRepository;
import com.agrocloud.cultivos.infrastructure.TransicionEstadoConfigRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Servicio para importar y exportar configuración de estados, transiciones y tareas desde/hacia Excel.
 *
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Service("importacionConfiguracionEstadosServiceCultivos")
public class ImportacionConfiguracionEstadosService {

    private static final Logger logger = LoggerFactory.getLogger(ImportacionConfiguracionEstadosService.class);
    private static final int MAX_FILAS_POR_PESTANA = 500;
    private static final Set<String> TIPOS_LABOR_VALIDOS = Set.of(
        "SIEMBRA", "FERTILIZACION", "RIEGO", "COSECHA", "MANTENIMIENTO",
        "PODA", "CONTROL_PLAGAS", "CONTROL_MALEZAS", "ANALISIS_SUELO", "OTROS"
    );

    @Autowired
    private TipoCultivoRepository tipoCultivoRepository;
    @Autowired
    private EstadoLoteConfigRepository estadoLoteConfigRepository;
    @Autowired
    private TransicionEstadoConfigRepository transicionEstadoConfigRepository;
    @Autowired
    private TareaPorEstadoConfigRepository tareaPorEstadoConfigRepository;

    /**
     * Resultado de la importación
     */
    public static class ResultadoImportacion {
        private boolean exito;
        private Long tipoCultivoId;
        private String tipoCultivoNombre;
        private int estadosCreados;
        private int transicionesCreadas;
        private int tareasCreadas;
        private List<ErrorImportacion> errores = new ArrayList<>();

        public boolean isExito() { return exito; }
        public void setExito(boolean exito) { this.exito = exito; }
        public Long getTipoCultivoId() { return tipoCultivoId; }
        public void setTipoCultivoId(Long tipoCultivoId) { this.tipoCultivoId = tipoCultivoId; }
        public String getTipoCultivoNombre() { return tipoCultivoNombre; }
        public void setTipoCultivoNombre(String tipoCultivoNombre) { this.tipoCultivoNombre = tipoCultivoNombre; }
        public int getEstadosCreados() { return estadosCreados; }
        public void setEstadosCreados(int estadosCreados) { this.estadosCreados = estadosCreados; }
        public int getTransicionesCreadas() { return transicionesCreadas; }
        public void setTransicionesCreadas(int transicionesCreadas) { this.transicionesCreadas = transicionesCreadas; }
        public int getTareasCreadas() { return tareasCreadas; }
        public void setTareasCreadas(int tareasCreadas) { this.tareasCreadas = tareasCreadas; }
        public List<ErrorImportacion> getErrores() { return errores; }
        public void setErrores(List<ErrorImportacion> errores) { this.errores = errores; }
    }

    /**
     * Error de importación por fila
     */
    public static class ErrorImportacion {
        private String pestana;
        private int fila;
        private String mensaje;

        public ErrorImportacion(String pestana, int fila, String mensaje) {
            this.pestana = pestana;
            this.fila = fila;
            this.mensaje = mensaje;
        }
        public String getPestana() { return pestana; }
        public int getFila() { return fila; }
        public String getMensaje() { return mensaje; }
    }

    /**
     * Importa configuración desde un archivo Excel.
     *
     * @param archivo Archivo Excel (.xlsx)
     * @param nombreTipoCultivo Nombre del tipo de cultivo (crear nuevo o actualizar existente)
     * @param tipoCultivoIdExistente Si se proporciona, actualiza; si no, crea nuevo
     * @param empresaId Si se proporciona, crea personalización por empresa; si no, plantilla global
     * @return Resultado de la importación
     */
    @Transactional
    public ResultadoImportacion importarDesdeExcel(
            MultipartFile archivo,
            String nombreTipoCultivo,
            Long tipoCultivoIdExistente,
            Long empresaId
    ) throws IOException {
        ResultadoImportacion resultado = new ResultadoImportacion();
        resultado.setExito(false);

        if (archivo == null || archivo.isEmpty()) {
            resultado.getErrores().add(new ErrorImportacion("General", 0, "El archivo está vacío"));
            return resultado;
        }

        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || !nombreArchivo.toLowerCase().endsWith(".xlsx")) {
            resultado.getErrores().add(new ErrorImportacion("General", 0, "El archivo debe ser Excel (.xlsx)"));
            return resultado;
        }

        try (InputStream is = archivo.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheetEstados = workbook.getSheet("Estados");
            if (sheetEstados == null) {
                resultado.getErrores().add(new ErrorImportacion("General", 0, "Falta la pestaña 'Estados'"));
                return resultado;
            }

            // Determinar tipo de cultivo
            TipoCultivo tipoCultivo;
            String nombreFinal = (nombreTipoCultivo != null && !nombreTipoCultivo.isBlank())
                ? nombreTipoCultivo.trim()
                : "Importado_" + System.currentTimeMillis();

            if (tipoCultivoIdExistente != null) {
                tipoCultivo = tipoCultivoRepository.findById(tipoCultivoIdExistente)
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de cultivo no encontrado: " + tipoCultivoIdExistente));
                // Eliminar estados, transiciones y tareas existentes para reemplazar
                eliminarConfiguracionExistente(tipoCultivo.getId(), empresaId);
            } else {
                if (tipoCultivoRepository.findByNombre(nombreFinal).isPresent()) {
                    resultado.getErrores().add(new ErrorImportacion("General", 0,
                        "Ya existe un tipo de cultivo con el nombre: " + nombreFinal));
                    return resultado;
                }
                tipoCultivo = new TipoCultivo();
                tipoCultivo.setNombre(nombreFinal);
                tipoCultivo.setDescripcion("Importado desde Excel");
                tipoCultivo.setEsPlantilla(empresaId == null);
                tipoCultivo.setActivo(true);
                tipoCultivo = tipoCultivoRepository.save(tipoCultivo);
            }

            Empresa empresa = null;
            if (empresaId != null) {
                empresa = new Empresa();
                empresa.setId(empresaId);
            }

            // 1. Procesar Estados
            Map<String, EstadoLoteConfig> mapaEstadosPorNombre = new HashMap<>();
            List<ErrorImportacion> erroresEstados = procesarEstados(sheetEstados, tipoCultivo, empresa, mapaEstadosPorNombre);
            resultado.getErrores().addAll(erroresEstados);

            if (!erroresEstados.isEmpty()) {
                resultado.setTipoCultivoId(tipoCultivo.getId());
                resultado.setTipoCultivoNombre(tipoCultivo.getNombre());
                throw new ImportacionConfiguracionException(resultado);
            }

            // Validar: exactamente un estado inicial y uno final
            long countInicial = mapaEstadosPorNombre.values().stream().filter(EstadoLoteConfig::getEsEstadoInicial).count();
            long countFinal = mapaEstadosPorNombre.values().stream().filter(EstadoLoteConfig::getEsEstadoFinal).count();
            if (countInicial != 1) {
                resultado.getErrores().add(new ErrorImportacion("Estados", 0,
                    "Debe haber exactamente un estado inicial (es_estado_inicial=true). Encontrados: " + countInicial));
                throw new ImportacionConfiguracionException(resultado);
            }
            if (countFinal != 1) {
                resultado.getErrores().add(new ErrorImportacion("Estados", 0,
                    "Debe haber exactamente un estado final (es_estado_final=true). Encontrados: " + countFinal));
                throw new ImportacionConfiguracionException(resultado);
            }

            resultado.setEstadosCreados(mapaEstadosPorNombre.size());

            // 2. Procesar Transiciones
            Sheet sheetTransiciones = workbook.getSheet("Transiciones");
            int transicionesCreadas = 0;
            if (sheetTransiciones != null) {
                List<ErrorImportacion> erroresTrans = procesarTransiciones(
                    sheetTransiciones, tipoCultivo, empresa, mapaEstadosPorNombre);
                resultado.getErrores().addAll(erroresTrans);
                if (!erroresTrans.isEmpty()) {
                    throw new ImportacionConfiguracionException(resultado);
                }
                transicionesCreadas = (int) sheetTransiciones.getPhysicalNumberOfRows() - 1;
                if (transicionesCreadas < 0) transicionesCreadas = 0;
            }
            resultado.setTransicionesCreadas(transicionesCreadas);

            // 3. Procesar Tareas
            Sheet sheetTareas = workbook.getSheet("Tareas");
            int tareasCreadas = 0;
            if (sheetTareas != null) {
                List<ErrorImportacion> erroresTareas = procesarTareas(
                    sheetTareas, tipoCultivo, empresa, mapaEstadosPorNombre);
                resultado.getErrores().addAll(erroresTareas);
                if (!erroresTareas.isEmpty()) {
                    throw new ImportacionConfiguracionException(resultado);
                }
                tareasCreadas = (int) sheetTareas.getPhysicalNumberOfRows() - 1;
                if (tareasCreadas < 0) tareasCreadas = 0;
            }
            resultado.setTareasCreadas(tareasCreadas);

            if (resultado.getErrores().isEmpty()) {
                resultado.setExito(true);
                resultado.setTipoCultivoId(tipoCultivo.getId());
                resultado.setTipoCultivoNombre(tipoCultivo.getNombre());
                logger.info("Importación exitosa: {} estados, {} transiciones, {} tareas para tipo de cultivo {}",
                    resultado.getEstadosCreados(), resultado.getTransicionesCreadas(), resultado.getTareasCreadas(),
                    tipoCultivo.getNombre());
            }
        }

        return resultado;
    }

    private void eliminarConfiguracionExistente(Long tipoCultivoId, Long empresaId) {
        List<TransicionEstadoConfig> transiciones = empresaId != null
            ? transicionEstadoConfigRepository.findByTipoCultivoIdAndEmpresaIdAndActivoTrue(tipoCultivoId, empresaId)
            : transicionEstadoConfigRepository.findPlantillasByTipoCultivoId(tipoCultivoId);
        transicionEstadoConfigRepository.deleteAll(transiciones);

        List<TareaPorEstadoConfig> tareas = empresaId != null
            ? tareaPorEstadoConfigRepository.findByTipoCultivoIdAndEmpresaIdAndActivoTrueOrderByOrdenAsc(tipoCultivoId, empresaId)
            : tareaPorEstadoConfigRepository.findPlantillasByTipoCultivoId(tipoCultivoId);
        tareaPorEstadoConfigRepository.deleteAll(tareas);

        List<EstadoLoteConfig> estados = empresaId != null
            ? estadoLoteConfigRepository.findByTipoCultivoIdAndEmpresaIdAndActivoTrueOrderByOrdenAsc(tipoCultivoId, empresaId)
            : estadoLoteConfigRepository.findPlantillasByTipoCultivoId(tipoCultivoId);
        estadoLoteConfigRepository.deleteAll(estados);
    }

    private List<ErrorImportacion> procesarEstados(
            Sheet sheet,
            TipoCultivo tipoCultivo,
            Empresa empresa,
            Map<String, EstadoLoteConfig> mapaEstados
    ) {
        List<ErrorImportacion> errores = new ArrayList<>();
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            errores.add(new ErrorImportacion("Estados", 1, "La pestaña Estados está vacía"));
            return errores;
        }

        Map<String, Integer> columnas = mapearColumnas(headerRow,
            "nombre", "descripcion", "color", "icono", "orden", "es_estado_inicial", "es_estado_final");
        if (!columnas.containsKey("nombre") || !columnas.containsKey("orden")) {
            errores.add(new ErrorImportacion("Estados", 1, "Faltan columnas obligatorias: nombre, orden"));
            return errores;
        }

        Set<String> nombresUsados = new HashSet<>();
        int filaNum = 1;
        for (Row row : sheet) {
            if (filaNum > MAX_FILAS_POR_PESTANA) break;
            if (row.getRowNum() == 0) continue;

            String nombre = obtenerCeldaString(row, columnas.get("nombre"));
            if (nombre == null || nombre.isBlank()) {
                filaNum++;
                continue;
            }
            nombre = nombre.trim();

            if (nombresUsados.contains(nombre)) {
                errores.add(new ErrorImportacion("Estados", filaNum + 1, "Estado duplicado: " + nombre));
                filaNum++;
                continue;
            }
            nombresUsados.add(nombre);

            Integer orden = obtenerCeldaInt(row, columnas.get("orden"));
            if (orden == null) {
                errores.add(new ErrorImportacion("Estados", filaNum + 1, "El orden es obligatorio"));
                filaNum++;
                continue;
            }

            EstadoLoteConfig estado = new EstadoLoteConfig();
            estado.setTipoCultivo(tipoCultivo);
            estado.setEmpresa(empresa);
            estado.setNombre(nombre);
            estado.setDescripcion(obtenerCeldaString(row, columnas.get("descripcion")));
            estado.setColor(obtenerCeldaString(row, columnas.get("color")) != null
                ? obtenerCeldaString(row, columnas.get("color")).trim() : "#10b981");
            if (estado.getColor().length() > 20) estado.setColor("#10b981");
            estado.setIcono(obtenerCeldaString(row, columnas.get("icono")));
            if (estado.getIcono() != null && estado.getIcono().length() > 10) estado.setIcono(estado.getIcono().substring(0, 10));
            estado.setOrden(orden);
            estado.setEsEstadoInicial(parsearBooleano(obtenerCeldaString(row, columnas.get("es_estado_inicial"))));
            estado.setEsEstadoFinal(parsearBooleano(obtenerCeldaString(row, columnas.get("es_estado_final"))));
            estado.setActivo(true);

            EstadoLoteConfig guardado = estadoLoteConfigRepository.save(estado);
            mapaEstados.put(nombre, guardado);
            filaNum++;
        }

        return errores;
    }

    private List<ErrorImportacion> procesarTransiciones(
            Sheet sheet,
            TipoCultivo tipoCultivo,
            Empresa empresa,
            Map<String, EstadoLoteConfig> mapaEstados
    ) {
        List<ErrorImportacion> errores = new ArrayList<>();
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) return errores;

        Map<String, Integer> columnas = mapearColumnas(headerRow,
            "estado_origen", "estado_destino", "requiere_motivo");
        if (!columnas.containsKey("estado_origen") || !columnas.containsKey("estado_destino")) {
            errores.add(new ErrorImportacion("Transiciones", 1, "Faltan columnas: estado_origen, estado_destino"));
            return errores;
        }

        int filaNum = 1;
        for (Row row : sheet) {
            if (filaNum > MAX_FILAS_POR_PESTANA) break;
            if (row.getRowNum() == 0) continue;

            String origen = obtenerCeldaString(row, columnas.get("estado_origen"));
            String destino = obtenerCeldaString(row, columnas.get("estado_destino"));
            if (origen == null || origen.isBlank() || destino == null || destino.isBlank()) {
                filaNum++;
                continue;
            }
            origen = origen.trim();
            destino = destino.trim();

            EstadoLoteConfig estadoOrigen = mapaEstados.get(origen);
            EstadoLoteConfig estadoDestino = mapaEstados.get(destino);
            if (estadoOrigen == null) {
                errores.add(new ErrorImportacion("Transiciones", filaNum + 1, "Estado origen no encontrado: " + origen));
                filaNum++;
                continue;
            }
            if (estadoDestino == null) {
                errores.add(new ErrorImportacion("Transiciones", filaNum + 1, "Estado destino no encontrado: " + destino));
                filaNum++;
                continue;
            }

            TransicionEstadoConfig transicion = new TransicionEstadoConfig();
            transicion.setTipoCultivo(tipoCultivo);
            transicion.setEmpresa(empresa);
            transicion.setEstadoOrigen(estadoOrigen);
            transicion.setEstadoDestino(estadoDestino);
            transicion.setRequiereMotivo(parsearBooleano(obtenerCeldaString(row, columnas.get("requiere_motivo"))));
            transicion.setActivo(true);
            transicionEstadoConfigRepository.save(transicion);
            filaNum++;
        }
        return errores;
    }

    private List<ErrorImportacion> procesarTareas(
            Sheet sheet,
            TipoCultivo tipoCultivo,
            Empresa empresa,
            Map<String, EstadoLoteConfig> mapaEstados
    ) {
        List<ErrorImportacion> errores = new ArrayList<>();
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) return errores;

        Map<String, Integer> columnas = mapearColumnas(headerRow,
            "estado", "tipo_labor", "nombre_tarea", "descripcion", "es_obligatoria", "orden");
        if (!columnas.containsKey("estado") || !columnas.containsKey("tipo_labor") || !columnas.containsKey("nombre_tarea")) {
            errores.add(new ErrorImportacion("Tareas", 1, "Faltan columnas: estado, tipo_labor, nombre_tarea"));
            return errores;
        }

        int filaNum = 1;
        int ordenTarea = 0;
        for (Row row : sheet) {
            if (filaNum > MAX_FILAS_POR_PESTANA) break;
            if (row.getRowNum() == 0) continue;

            String nombreEstado = obtenerCeldaString(row, columnas.get("estado"));
            String tipoLabor = obtenerCeldaString(row, columnas.get("tipo_labor"));
            String nombreTarea = obtenerCeldaString(row, columnas.get("nombre_tarea"));
            if (nombreEstado == null || nombreEstado.isBlank() || tipoLabor == null || tipoLabor.isBlank()
                || nombreTarea == null || nombreTarea.isBlank()) {
                filaNum++;
                continue;
            }
            nombreEstado = nombreEstado.trim();
            tipoLabor = tipoLabor.trim().toUpperCase();
            nombreTarea = nombreTarea.trim();

            if (!TIPOS_LABOR_VALIDOS.contains(tipoLabor)) {
                errores.add(new ErrorImportacion("Tareas", filaNum + 1,
                    "Tipo de labor inválido: " + tipoLabor + ". Valores: " + String.join(", ", TIPOS_LABOR_VALIDOS)));
                filaNum++;
                continue;
            }

            EstadoLoteConfig estado = mapaEstados.get(nombreEstado);
            if (estado == null) {
                errores.add(new ErrorImportacion("Tareas", filaNum + 1, "Estado no encontrado: " + nombreEstado));
                filaNum++;
                continue;
            }

            TareaPorEstadoConfig tarea = new TareaPorEstadoConfig();
            tarea.setTipoCultivo(tipoCultivo);
            tarea.setEmpresa(empresa);
            tarea.setEstado(estado);
            tarea.setTipoLabor(tipoLabor);
            tarea.setNombreTarea(nombreTarea);
            tarea.setDescripcion(obtenerCeldaString(row, columnas.get("descripcion")));
            tarea.setEsObligatoria(parsearBooleano(obtenerCeldaString(row, columnas.get("es_obligatoria"))));
            Integer orden = obtenerCeldaInt(row, columnas.get("orden"));
            tarea.setOrden(orden != null ? orden : ordenTarea++);
            tarea.setActivo(true);
            tareaPorEstadoConfigRepository.save(tarea);
            filaNum++;
        }
        return errores;
    }

    private Map<String, Integer> mapearColumnas(Row headerRow, String... nombres) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            String valor = obtenerValorCelda(cell);
            if (valor != null) {
                String normalizado = normalizarNombreColumna(valor.trim().toLowerCase());
                for (String n : nombres) {
                    String nNorm = normalizarNombreColumna(n.toLowerCase());
                    if (normalizado.equals(nNorm) || normalizado.replace("_", "").equals(nNorm.replace("_", ""))) {
                        map.put(n, cell.getColumnIndex());
                        break;
                    }
                }
            }
        }
        return map;
    }

    private String normalizarNombreColumna(String s) {
        if (s == null) return "";
        return s.replace(" ", "_")
            .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
            .replace("ñ", "n");
    }

    private String obtenerCeldaString(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        return obtenerValorCelda(cell);
    }

    private Integer obtenerCeldaInt(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    private String obtenerValorCelda(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private boolean parsearBooleano(String valor) {
        if (valor == null || valor.isBlank()) return false;
        String v = valor.trim().toLowerCase();
        return "true".equals(v) || "si".equals(v) || "sí".equals(v) || "1".equals(v) || "yes".equals(v);
    }

    /**
     * Genera un archivo Excel de ejemplo para descarga.
     */
    public byte[] generarPlantillaEjemplo() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            crearHojaEstados(workbook);
            crearHojaTransiciones(workbook);
            crearHojaTareas(workbook);
            crearHojaInstrucciones(workbook);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void crearHojaEstados(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Estados");
        String[] headers = {"nombre", "descripcion", "color", "icono", "orden", "es_estado_inicial", "es_estado_final"};
        crearFilaEncabezado(sheet, headers);

        Object[][] datos = {
            {"Disponible", "Lote listo para trabajar", "#10b981", "🟢", 1, "true", "false"},
            {"Preparado", "Suelo preparado para siembra", "#f59e0b", "🟡", 2, "false", "false"},
            {"Sembrado", "Cultivo sembrado", "#3b82f6", "🔵", 3, "false", "false"},
            {"Emergencia", "Plantas emergiendo", "#22c55e", "🌱", 4, "false", "false"},
            {"R3", "Inicio de llenado de granos", "#8b5cf6", "🌾", 5, "false", "false"},
            {"R6", "Llenado completo", "#ec4899", "🌽", 6, "false", "false"},
            {"Listo para Cosecha", "Listo para cosechar", "#f97316", "📦", 7, "false", "false"},
            {"Cosechado", "Cosecha finalizada", "#6b7280", "✅", 8, "false", "true"},
        };
        int rowNum = 1;
        for (Object[] fila : datos) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < fila.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(String.valueOf(fila[i]));
            }
        }
        autoAjustarColumnas(sheet, headers.length);
    }

    private void crearHojaTransiciones(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Transiciones");
        String[] headers = {"estado_origen", "estado_destino", "requiere_motivo"};
        crearFilaEncabezado(sheet, headers);

        Object[][] datos = {
            {"Disponible", "Preparado", "false"},
            {"Preparado", "Sembrado", "false"},
            {"Sembrado", "Emergencia", "false"},
            {"Emergencia", "R3", "false"},
            {"R3", "R6", "false"},
            {"R6", "Listo para Cosecha", "false"},
            {"Listo para Cosecha", "Cosechado", "false"},
            {"Cosechado", "Disponible", "false"},
        };
        int rowNum = 1;
        for (Object[] fila : datos) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < fila.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(String.valueOf(fila[i]));
            }
        }
        autoAjustarColumnas(sheet, headers.length);
    }

    private void crearHojaTareas(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Tareas");
        String[] headers = {"estado", "tipo_labor", "nombre_tarea", "descripcion", "es_obligatoria", "orden"};
        crearFilaEncabezado(sheet, headers);

        Object[][] datos = {
            {"Preparado", "SIEMBRA", "Siembra", "Plantación del cultivo", "true", 1},
            {"Sembrado", "RIEGO", "Riego", "Aplicación de agua", "false", 1},
            {"Sembrado", "FERTILIZACION", "Fertilización", "Aplicación de nutrientes", "false", 2},
            {"Emergencia", "RIEGO", "Riego", "Aplicación de agua", "false", 1},
            {"Emergencia", "CONTROL_MALEZAS", "Control de Malezas", "Control de malezas", "false", 2},
            {"R3", "FERTILIZACION", "Fertilización", "Aplicación de nutrientes", "false", 1},
            {"R3", "RIEGO", "Riego", "Aplicación de agua", "false", 2},
            {"R6", "RIEGO", "Riego", "Aplicación de agua", "false", 1},
            {"Listo para Cosecha", "COSECHA", "Cosecha", "Cosecha del cultivo", "true", 1},
        };
        int rowNum = 1;
        for (Object[] fila : datos) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < fila.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(String.valueOf(fila[i]));
            }
        }
        autoAjustarColumnas(sheet, headers.length);
    }

    private void crearHojaInstrucciones(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Instrucciones");
        Row r0 = sheet.createRow(0);
        r0.createCell(0).setCellValue("INSTRUCCIONES PARA IMPORTAR CONFIGURACIÓN DE ESTADOS Y TAREAS");
        Row r1 = sheet.createRow(1);
        r1.createCell(0).setCellValue("");
        Row r2 = sheet.createRow(2);
        r2.createCell(0).setCellValue("1. Pestaña ESTADOS: Defina los estados del ciclo del cultivo. Debe haber exactamente un estado inicial y uno final.");
        Row r3 = sheet.createRow(3);
        r3.createCell(0).setCellValue("2. Pestaña TRANSICIONES: Defina qué estados pueden pasar a otros. Use los nombres exactos de la pestaña Estados.");
        Row r4 = sheet.createRow(4);
        r4.createCell(0).setCellValue("3. Pestaña TAREAS: Defina las labores permitidas en cada estado. Tipos válidos: SIEMBRA, FERTILIZACION, RIEGO, COSECHA, MANTENIMIENTO, PODA, CONTROL_PLAGAS, CONTROL_MALEZAS, ANALISIS_SUELO, OTROS");
        Row r5 = sheet.createRow(5);
        r5.createCell(0).setCellValue("");
        Row r6 = sheet.createRow(6);
        r6.createCell(0).setCellValue("Los nombres en Transiciones y Tareas deben coincidir exactamente con los de la pestaña Estados.");
        sheet.setColumnWidth(0, 12000);
    }

    private void crearFilaEncabezado(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
    }

    private void autoAjustarColumnas(Sheet sheet, int numCols) {
        for (int i = 0; i < numCols; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
