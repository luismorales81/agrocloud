package com.agrocloud.core.application;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Servicio para generar archivos Excel (.xlsx) para reportes
 */
@Service("excelExportServiceCore")
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Genera un archivo Excel con múltiples hojas
     */
    public byte[] generarExcel(String titulo, List<HojaExcel> hojas) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Crear estilo para encabezados
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle titleStyle = crearEstiloTitulo(workbook);
            CellStyle numberStyle = crearEstiloNumero(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);

            // Crear cada hoja
            for (HojaExcel hoja : hojas) {
                Sheet sheet = workbook.createSheet(hoja.getNombre());
                int rowNum = 0;

                // Título de la hoja
                if (hoja.getTitulo() != null) {
                    Row titleRow = sheet.createRow(rowNum++);
                    Cell titleCell = titleRow.createCell(0);
                    titleCell.setCellValue(hoja.getTitulo());
                    titleCell.setCellStyle(titleStyle);
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, hoja.getColumnas().size() - 1));
                }

                // Encabezados
                Row headerRow = sheet.createRow(rowNum++);
                for (int i = 0; i < hoja.getColumnas().size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(hoja.getColumnas().get(i));
                    cell.setCellStyle(headerStyle);
                }

                // Datos
                for (Map<String, Object> fila : hoja.getDatos()) {
                    Row dataRow = sheet.createRow(rowNum++);
                    for (int i = 0; i < hoja.getColumnas().size(); i++) {
                        String columna = hoja.getColumnas().get(i);
                        Object valor = fila.get(columna);
                        Cell cell = dataRow.createCell(i);
                        
                        if (valor == null) {
                            cell.setCellValue("");
                        } else if (valor instanceof Number) {
                            cell.setCellValue(((Number) valor).doubleValue());
                            cell.setCellStyle(numberStyle);
                        } else if (valor instanceof LocalDate) {
                            cell.setCellValue(((LocalDate) valor).format(DATE_FORMATTER));
                            cell.setCellStyle(dateStyle);
                        } else if (valor instanceof LocalDateTime) {
                            cell.setCellValue(((LocalDateTime) valor).format(DATETIME_FORMATTER));
                            cell.setCellStyle(dateStyle);
                        } else {
                            cell.setCellValue(valor.toString());
                        }
                    }
                }

                // Ajustar ancho de columnas
                for (int i = 0; i < hoja.getColumnas().size(); i++) {
                    sheet.autoSizeColumn(i);
                    sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 1000, 15000));
                }
            }

            // Escribir a byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloNumero(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd/mm/yyyy"));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Clase interna para representar una hoja de Excel
     */
    public static class HojaExcel {
        private String nombre;
        private String titulo;
        private List<String> columnas;
        private List<Map<String, Object>> datos;

        public HojaExcel(String nombre, String titulo, List<String> columnas, List<Map<String, Object>> datos) {
            this.nombre = nombre;
            this.titulo = titulo;
            this.columnas = columnas;
            this.datos = datos;
        }

        public String getNombre() { return nombre; }
        public String getTitulo() { return titulo; }
        public List<String> getColumnas() { return columnas; }
        public List<Map<String, Object>> getDatos() { return datos; }
    }
}

