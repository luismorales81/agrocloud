package com.agrocloud.trazabilidad.application;

import com.agrocloud.trazabilidad.domain.TrazabilidadReporte;
import com.agrocloud.trazabilidad.dto.HechosTrazabilidadDocumento;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
/**
 * Construcción de PDF (PDFBox) para trazabilidad. Texto básico sin caracteres no Latin-1
 * en títulos críticos; cuerpo tolera alfabeto con Helvetica.
 */
@Component
public class GeneradorPdfTrazabilidadComercial {

    private static final float MARGEN = 48f;
    private static final float ALTURA_LINEA = 13f;

    public byte[] generar(
            TrazabilidadReporte reporte,
            HechosTrazabilidadDocumento hechos,
            Empresa empresa,
            User usuario) throws IOException {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            try (PDPageContentStream c = new PDPageContentStream(doc, page)) {
                float y = 760;
                y = linea(c, bold, 14, MARGEN, y, "Trazabilidad comercial (AgroGestion)");
                y -= 6;
                y = linea(c, normal, 9, MARGEN, y, "Codigo unico (reporte): TRC-" + reporte.getId());
                y = linea(c, normal, 9, MARGEN, y, "Hash (snapshot, SHA-256): " + reporte.getHashSnapshot());
                y = linea(c, normal, 9, MARGEN, y, "Fecha: " + reporte.getGeneradoEn().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                y = linea(c, normal, 9, MARGEN, y, "Empresa: " + (empresa.getNombre() != null ? empresa.getNombre() : ""));
                y = linea(c, normal, 9, MARGEN, y, "Usuario: " + usuario.getEmail());
                y -= 10;
                y = linea(c, bold, 11, MARGEN, y, "Alcance");
                y = parrafoEnvuelto(c, normal, 9, MARGEN, 540, y, hechos.getDescripcionAlcance() != null ? hechos.getDescripcionAlcance() : "-");
                y = linea(c, normal, 9, MARGEN, y, "Entidad: " + reporte.getEntidadTipo() + " / id " + reporte.getEntidadId());
                y = linea(c, normal, 9, MARGEN, y, "Lote: " + (hechos.getNombreLote() != null ? hechos.getNombreLote() : "-"));
                y = linea(c, normal, 9, MARGEN, y, "Certificacion: " + reporte.getCertificacionCodigo());
                y = linea(c, bold, 10, MARGEN, y, "Resultado: APROBADO (VALIDO)");
                y -= 8;
                y = linea(c, bold, 10, MARGEN, y, "Insumos en labores (alcance analizado)");
                y -= 2;
                for (HechosTrazabilidadDocumento.LineaInsumoLabor li : hechos.getInsumosPorLabor()) {
                    String t = "Labor " + li.getIdLabor() + " " + (li.getFechaLabor() != null ? li.getFechaLabor() : "")
                            + " " + li.getTipoInsumo() + " " + li.getNombreInsumo();
                    y = parrafoEnvuelto(c, normal, 8, MARGEN + 10, 520, y, t);
                }
                y = linea(c, bold, 10, MARGEN, y, "Eventos sanitarios");
                for (HechosTrazabilidadDocumento.EventoSanitarioResumen e : hechos.getEventosSanitarios()) {
                    String t = (e.getFecha() != null ? e.getFecha().toString() : "") + " " + e.getCategoria() + " " + e.getNombreTipo();
                    y = parrafoEnvuelto(c, normal, 8, MARGEN + 10, 520, y, t);
                }
            }
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private float linea(PDPageContentStream c, PDType1Font font, float size, float x, float y, String t) throws IOException {
        c.beginText();
        c.setFont(font, size);
        c.newLineAtOffset(x, y);
        c.showText(sanitizar(t));
        c.endText();
        return y - ALTURA_LINEA;
    }

    /**
     * Evita caracteres fuera de winansi para showText; simplifica tildes comunes.
     */
    private String sanitizar(String s) {
        if (s == null) {
            return "";
        }
        return s
                .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                .replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U")
                .replace("ñ", "n").replace("Ñ", "N")
                .replace((char) 0xA0, ' ');
    }

    private float parrafoEnvuelto(
            PDPageContentStream c, PDType1Font font, float size,
            float x, float ancho, float yIn, String texto) throws IOException {
        if (texto == null) {
            return yIn;
        }
        String t = sanitizar(texto);
        String[] words = t.split(" ");
        StringBuilder linea = new StringBuilder();
        float y = yIn;
        for (String w : words) {
            if (linea.length() + w.length() + 1 > 90) {
                y = linea(c, font, size, x, y, linea.toString());
                linea = new StringBuilder(w);
            } else {
                if (linea.length() > 0) {
                    linea.append(" ");
                }
                linea.append(w);
            }
        }
        if (linea.length() > 0) {
            y = linea(c, font, size, x, y, linea.toString());
        }
        return y;
    }
}
