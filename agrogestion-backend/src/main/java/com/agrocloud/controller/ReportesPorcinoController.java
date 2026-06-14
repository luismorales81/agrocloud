package com.agrocloud.controller;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.application.ExcelExportService;
import org.springframework.beans.factory.annotation.Qualifier;
import com.agrocloud.porcinos.application.ReportesPorcinoService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controlador REST para generar reportes del mÃ³dulo Porcinos
 */
@RestController
@RequestMapping("/api/v1/porcinos/reportes")
public class ReportesPorcinoController {

    @Autowired
    private ReportesPorcinoService reportesService;

    @Autowired
    @Qualifier("excelExportServiceCore")
    private ExcelExportService excelExportService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    // ============================================================================
    // ENDPOINTS PARA OBTENER DATOS DE REPORTES (JSON)
    // ============================================================================

    @GetMapping("/reproductivo")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> obtenerReporteReproductivo(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusMonths(1);
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();

            Map<String, Object> reporte = reportesService.generarReporteReproductivo(user.getId(), inicio, fin);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/mortalidad")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> obtenerReporteMortalidad(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusMonths(1);
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();

            Map<String, Object> reporte = reportesService.generarReporteMortalidad(user.getId(), inicio, fin);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/productivo")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> obtenerReporteProductivo(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : null;
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : null;

            Map<String, Object> reporte = reportesService.generarReporteProductivo(user.getId(), inicio, fin);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/alimentacion")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> obtenerReporteAlimentacion(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusMonths(1);
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();

            Map<String, Object> reporte = reportesService.generarReporteAlimentacion(user.getId(), inicio, fin);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/economico")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> obtenerReporteEconomico(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusMonths(1);
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();

            Map<String, Object> reporte = reportesService.generarReporteEconomico(user.getId(), inicio, fin);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/inventario")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> obtenerReporteInventario(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : null;
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : null;

            Map<String, Object> reporte = reportesService.generarReporteInventario(user.getId(), inicio, fin);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/sanitario")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> obtenerReporteSanitario(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusMonths(1);
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();

            Map<String, Object> reporte = reportesService.generarReporteSanitario(user.getId(), inicio, fin);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ventas")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> obtenerReporteVentas(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusMonths(1);
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();

            Map<String, Object> reporte = reportesService.generarReporteVentas(user.getId(), inicio, fin);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================================
    // ENDPOINTS PARA EXPORTAR A EXCEL
    // ============================================================================

    @GetMapping("/reproductivo/excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportarReporteReproductivoExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusMonths(1);
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();

            Map<String, Object> datos = reportesService.generarReporteReproductivo(user.getId(), inicio, fin);
            byte[] excel = generarExcelReproductivo(datos, inicio, fin);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                "Reporte_Reproductivo_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

            return new ResponseEntity<>(excel, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/mortalidad/excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportarReporteMortalidadExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusMonths(1);
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();

            Map<String, Object> datos = reportesService.generarReporteMortalidad(user.getId(), inicio, fin);
            byte[] excel = generarExcelMortalidad(datos, inicio, fin);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                "Reporte_Mortalidad_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

            return new ResponseEntity<>(excel, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/productivo/excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportarReporteProductivoExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : null;
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : null;

            Map<String, Object> datos = reportesService.generarReporteProductivo(user.getId(), inicio, fin);
            byte[] excel = generarExcelProductivo(datos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                "Reporte_Productivo_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

            return new ResponseEntity<>(excel, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/alimentacion/excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportarReporteAlimentacionExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusMonths(1);
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();

            Map<String, Object> datos = reportesService.generarReporteAlimentacion(user.getId(), inicio, fin);
            byte[] excel = generarExcelAlimentacion(datos, inicio, fin);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                "Reporte_Alimentacion_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

            return new ResponseEntity<>(excel, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/economico/excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportarReporteEconomicoExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusMonths(1);
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();

            Map<String, Object> datos = reportesService.generarReporteEconomico(user.getId(), inicio, fin);
            byte[] excel = generarExcelEconomico(datos, inicio, fin);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                "Reporte_Economico_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

            return new ResponseEntity<>(excel, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/inventario/excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportarReporteInventarioExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : null;
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : null;

            Map<String, Object> datos = reportesService.generarReporteInventario(user.getId(), inicio, fin);
            byte[] excel = generarExcelInventario(datos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                "Reporte_Inventario_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

            return new ResponseEntity<>(excel, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/sanitario/excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportarReporteSanitarioExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusMonths(1);
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();

            Map<String, Object> datos = reportesService.generarReporteSanitario(user.getId(), inicio, fin);
            byte[] excel = generarExcelSanitario(datos, inicio, fin);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                "Reporte_Sanitario_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

            return new ResponseEntity<>(excel, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ventas/excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportarReporteVentasExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) return ResponseEntity.badRequest().build();

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusMonths(1);
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();

            Map<String, Object> datos = reportesService.generarReporteVentas(user.getId(), inicio, fin);
            byte[] excel = generarExcelVentas(datos, inicio, fin);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                "Reporte_Ventas_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

            return new ResponseEntity<>(excel, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================================
    // MÃ‰TODOS PRIVADOS PARA GENERAR EXCEL
    // ============================================================================

    private byte[] generarExcelReproductivo(Map<String, Object> datos, LocalDate inicio, LocalDate fin) throws Exception {
        List<ExcelExportService.HojaExcel> hojas = new ArrayList<>();

        // Hoja 1: Resumen de Ãndices
        @SuppressWarnings("unchecked")
        Map<String, Object> indices = (Map<String, Object>) datos.get("indices");
        List<Map<String, Object>> indicesData = new ArrayList<>();
        if (indices != null) {
            Map<String, Object> row = new HashMap<>();
            indices.forEach((k, v) -> row.put(k, v));
            indicesData.add(row);
        }
        hojas.add(new ExcelExportService.HojaExcel(
            "Resumen",
            "Reporte Reproductivo - Resumen de Ãndices",
            new ArrayList<>(indices != null ? indices.keySet() : Collections.emptyList()),
            indicesData
        ));

        // Hoja 2: Servicios
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> servicios = (List<Map<String, Object>>) datos.get("servicios");
        if (servicios != null && !servicios.isEmpty()) {
            hojas.add(new ExcelExportService.HojaExcel(
                "Servicios",
                "Detalle de Servicios",
                new ArrayList<>(servicios.get(0).keySet()),
                servicios
            ));
        }

        // Hoja 3: Gestaciones
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> gestaciones = (List<Map<String, Object>>) datos.get("gestaciones");
        if (gestaciones != null && !gestaciones.isEmpty()) {
            hojas.add(new ExcelExportService.HojaExcel(
                "Gestaciones",
                "Detalle de Gestaciones",
                new ArrayList<>(gestaciones.get(0).keySet()),
                gestaciones
            ));
        }

        // Hoja 4: Partos
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> partos = (List<Map<String, Object>>) datos.get("partos");
        if (partos != null && !partos.isEmpty()) {
            hojas.add(new ExcelExportService.HojaExcel(
                "Partos",
                "Detalle de Partos",
                new ArrayList<>(partos.get(0).keySet()),
                partos
            ));
        }

        // Hoja 5: Destetes
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> destetes = (List<Map<String, Object>>) datos.get("destetes");
        if (destetes != null && !destetes.isEmpty()) {
            hojas.add(new ExcelExportService.HojaExcel(
                "Destetes",
                "Detalle de Destetes",
                new ArrayList<>(destetes.get(0).keySet()),
                destetes
            ));
        }

        return excelExportService.generarExcel("Reporte Reproductivo", hojas);
    }

    private byte[] generarExcelMortalidad(Map<String, Object> datos, LocalDate inicio, LocalDate fin) throws Exception {
        List<ExcelExportService.HojaExcel> hojas = new ArrayList<>();

        // Hoja 1: Resumen
        @SuppressWarnings("unchecked")
        Map<String, Object> estadisticas = (Map<String, Object>) datos.get("estadisticas");
        List<Map<String, Object>> resumenData = new ArrayList<>();
        if (estadisticas != null) {
            Map<String, Object> row = new HashMap<>();
            estadisticas.forEach((k, v) -> row.put(k, v));
            resumenData.add(row);
        }
        hojas.add(new ExcelExportService.HojaExcel(
            "Resumen",
            "Reporte de Mortalidad - Resumen",
            new ArrayList<>(estadisticas != null ? estadisticas.keySet() : Collections.emptyList()),
            resumenData
        ));

        // Hoja 2: Muertes de Madres
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> muertesMadres = (List<Map<String, Object>>) datos.get("muertesMadres");
        if (muertesMadres != null && !muertesMadres.isEmpty()) {
            hojas.add(new ExcelExportService.HojaExcel(
                "Muertes Madres",
                "Detalle de Muertes de Madres",
                new ArrayList<>(muertesMadres.get(0).keySet()),
                muertesMadres
            ));
        }

        // Hoja 3: Muertes de Lechones
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> muertesLechones = (List<Map<String, Object>>) datos.get("muertesLechones");
        if (muertesLechones != null && !muertesLechones.isEmpty()) {
            hojas.add(new ExcelExportService.HojaExcel(
                "Muertes Lechones",
                "Detalle de Muertes de Lechones",
                new ArrayList<>(muertesLechones.get(0).keySet()),
                muertesLechones
            ));
        }

        // Hoja 4: Muertes en RecrÃ­a
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> muertesRecria = (List<Map<String, Object>>) datos.get("muertesRecria");
        if (muertesRecria != null && !muertesRecria.isEmpty()) {
            hojas.add(new ExcelExportService.HojaExcel(
                "Muertes RecrÃ­a",
                "Detalle de Muertes en RecrÃ­a",
                new ArrayList<>(muertesRecria.get(0).keySet()),
                muertesRecria
            ));
        }

        return excelExportService.generarExcel("Reporte de Mortalidad", hojas);
    }

    private byte[] generarExcelProductivo(Map<String, Object> datos) throws Exception {
        List<ExcelExportService.HojaExcel> hojas = new ArrayList<>();

        // Hoja 1: KPIs
        @SuppressWarnings("unchecked")
        Map<String, Object> kpis = (Map<String, Object>) datos.get("kpis");
        List<Map<String, Object>> kpisData = new ArrayList<>();
        if (kpis != null) {
            Map<String, Object> row = new HashMap<>();
            kpis.forEach((k, v) -> row.put(k, v));
            kpisData.add(row);
        }
        hojas.add(new ExcelExportService.HojaExcel(
            "KPIs",
            "Reporte Productivo - Indicadores Clave",
            new ArrayList<>(kpis != null ? kpis.keySet() : Collections.emptyList()),
            kpisData
        ));

        return excelExportService.generarExcel("Reporte Productivo", hojas);
    }

    private byte[] generarExcelAlimentacion(Map<String, Object> datos, LocalDate inicio, LocalDate fin) throws Exception {
        List<ExcelExportService.HojaExcel> hojas = new ArrayList<>();

        // Hoja 1: Resumen
        @SuppressWarnings("unchecked")
        Map<String, Object> resumen = (Map<String, Object>) datos.get("resumen");
        List<Map<String, Object>> resumenData = new ArrayList<>();
        if (resumen != null) {
            Map<String, Object> row = new HashMap<>();
            resumen.forEach((k, v) -> row.put(k, v));
            resumenData.add(row);
        }
        hojas.add(new ExcelExportService.HojaExcel(
            "Resumen",
            "Reporte de AlimentaciÃ³n - Resumen",
            new ArrayList<>(resumen != null ? resumen.keySet() : Collections.emptyList()),
            resumenData
        ));

        // Hoja 2: Detalle de Consumos
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> consumos = (List<Map<String, Object>>) datos.get("consumos");
        if (consumos != null && !consumos.isEmpty()) {
            hojas.add(new ExcelExportService.HojaExcel(
                "Consumos",
                "Detalle de Consumos de Alimento",
                new ArrayList<>(consumos.get(0).keySet()),
                consumos
            ));
        }

        return excelExportService.generarExcel("Reporte de AlimentaciÃ³n", hojas);
    }

    private byte[] generarExcelEconomico(Map<String, Object> datos, LocalDate inicio, LocalDate fin) throws Exception {
        List<ExcelExportService.HojaExcel> hojas = new ArrayList<>();

        // Hoja 1: Resumen
        @SuppressWarnings("unchecked")
        Map<String, Object> resumen = (Map<String, Object>) datos.get("resumen");
        List<Map<String, Object>> resumenData = new ArrayList<>();
        if (resumen != null) {
            Map<String, Object> row = new HashMap<>();
            resumen.forEach((k, v) -> row.put(k, v));
            resumenData.add(row);
        }
        hojas.add(new ExcelExportService.HojaExcel(
            "Resumen",
            "Reporte EconÃ³mico - Resumen",
            new ArrayList<>(resumen != null ? resumen.keySet() : Collections.emptyList()),
            resumenData
        ));

        // Hoja 2: Detalle de Ventas
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ventas = (List<Map<String, Object>>) datos.get("ventas");
        if (ventas != null && !ventas.isEmpty()) {
            hojas.add(new ExcelExportService.HojaExcel(
                "Ventas",
                "Detalle de Ventas",
                new ArrayList<>(ventas.get(0).keySet()),
                ventas
            ));
        }

        return excelExportService.generarExcel("Reporte EconÃ³mico", hojas);
    }

    private byte[] generarExcelInventario(Map<String, Object> datos) throws Exception {
        List<ExcelExportService.HojaExcel> hojas = new ArrayList<>();

        // Hoja 1: Resumen
        @SuppressWarnings("unchecked")
        Map<String, Object> resumen = (Map<String, Object>) datos.get("resumen");
        List<Map<String, Object>> resumenData = new ArrayList<>();
        if (resumen != null) {
            Map<String, Object> row = new HashMap<>();
            resumen.forEach((k, v) -> row.put(k, v));
            resumenData.add(row);
        }
        hojas.add(new ExcelExportService.HojaExcel(
            "Resumen",
            "Reporte de Inventario - Resumen",
            new ArrayList<>(resumen != null ? resumen.keySet() : Collections.emptyList()),
            resumenData
        ));

        // Hoja 2: Madres
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> madres = (List<Map<String, Object>>) datos.get("madres");
        if (madres != null && !madres.isEmpty()) {
            hojas.add(new ExcelExportService.HojaExcel(
                "Madres",
                "Detalle de Madres",
                new ArrayList<>(madres.get(0).keySet()),
                madres
            ));
        }

        // Hoja 3: Padrillos
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> padrillos = (List<Map<String, Object>>) datos.get("padrillos");
        if (padrillos != null && !padrillos.isEmpty()) {
            hojas.add(new ExcelExportService.HojaExcel(
                "Padrillos",
                "Detalle de Padrillos",
                new ArrayList<>(padrillos.get(0).keySet()),
                padrillos
            ));
        }

        // Hoja 4: RecrÃ­as
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recrias = (List<Map<String, Object>>) datos.get("recrias");
        if (recrias != null && !recrias.isEmpty()) {
            hojas.add(new ExcelExportService.HojaExcel(
                "RecrÃ­as",
                "Detalle de RecrÃ­as",
                new ArrayList<>(recrias.get(0).keySet()),
                recrias
            ));
        }

        return excelExportService.generarExcel("Reporte de Inventario", hojas);
    }

    private byte[] generarExcelSanitario(Map<String, Object> datos, LocalDate inicio, LocalDate fin) throws Exception {
        List<ExcelExportService.HojaExcel> hojas = new ArrayList<>();

        // Hoja 1: Resumen
        @SuppressWarnings("unchecked")
        Map<String, Object> resumen = (Map<String, Object>) datos.get("resumen");
        List<Map<String, Object>> resumenData = new ArrayList<>();
        if (resumen != null) {
            Map<String, Object> row = new HashMap<>();
            resumen.forEach((k, v) -> row.put(k, v));
            resumenData.add(row);
        }
        hojas.add(new ExcelExportService.HojaExcel(
            "Resumen",
            "Reporte Sanitario - Resumen",
            new ArrayList<>(resumen != null ? resumen.keySet() : Collections.emptyList()),
            resumenData
        ));

        // Hoja 2: Detalle de Eventos
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> eventos = (List<Map<String, Object>>) datos.get("eventos");
        if (eventos != null && !eventos.isEmpty()) {
            hojas.add(new ExcelExportService.HojaExcel(
                "Eventos",
                "Detalle de Eventos Sanitarios",
                new ArrayList<>(eventos.get(0).keySet()),
                eventos
            ));
        }

        return excelExportService.generarExcel("Reporte Sanitario", hojas);
    }

    private byte[] generarExcelVentas(Map<String, Object> datos, LocalDate inicio, LocalDate fin) throws Exception {
        List<ExcelExportService.HojaExcel> hojas = new ArrayList<>();

        // Hoja 1: Resumen
        @SuppressWarnings("unchecked")
        Map<String, Object> resumen = (Map<String, Object>) datos.get("resumen");
        List<Map<String, Object>> resumenData = new ArrayList<>();
        if (resumen != null) {
            Map<String, Object> row = new HashMap<>();
            resumen.forEach((k, v) -> row.put(k, v));
            resumenData.add(row);
        }
        hojas.add(new ExcelExportService.HojaExcel(
            "Resumen",
            "Reporte de Ventas - Resumen",
            new ArrayList<>(resumen != null ? resumen.keySet() : Collections.emptyList()),
            resumenData
        ));

        // Hoja 2: Detalle de Ventas
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ventas = (List<Map<String, Object>>) datos.get("ventas");
        if (ventas != null && !ventas.isEmpty()) {
            hojas.add(new ExcelExportService.HojaExcel(
                "Ventas",
                "Detalle de Ventas",
                new ArrayList<>(ventas.get(0).keySet()),
                ventas
            ));
        }

        return excelExportService.generarExcel("Reporte de Ventas", hojas);
    }

    // ============================================================================
    // VALIDACIÃ“N DEL CICLO PRODUCTIVO
    // ============================================================================

    /**
     * Endpoint para validar que el ciclo productivo se cumpla correctamente
     * Verifica fechas, estados y consistencia de datos segÃºn parÃ¡metros productivos
     */
    @GetMapping("/validar-ciclo-productivo")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> validarCicloProductivo(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Map<String, Object> resultado = reportesService.validarCicloProductivo(user.getId());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            System.err.println("Error al validar ciclo productivo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

