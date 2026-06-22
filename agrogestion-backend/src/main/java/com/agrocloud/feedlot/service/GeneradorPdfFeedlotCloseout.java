package com.agrocloud.feedlot.service;

import com.agrocloud.feedlot.model.dto.FeedlotCloseoutRespuesta;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class GeneradorPdfFeedlotCloseout {

    private static final float MARGEN = 48f;
    private static final float ALTURA_LINEA = 14f;

    public byte[] generar(FeedlotCloseoutRespuesta closeout) throws IOException {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            try (PDPageContentStream c = new PDPageContentStream(doc, page)) {
                float y = 760;
                y = linea(c, bold, 14, MARGEN, y, "Closeout Feedlot (AgroGestion)");
                y -= 8;
                y = linea(c, normal, 10, MARGEN, y, "Lote: " + sanitizar(closeout.getLoteNombre()));
                y = linea(c, normal, 10, MARGEN, y, "Metodo closeout: " + sanitizar(closeout.getMetodoCloseout()));
                y = linea(c, normal, 10, MARGEN, y,
                        "Periodo: " + closeout.getFechaIngreso() + " a " + closeout.getFechaReferencia());
                y = linea(c, normal, 10, MARGEN, y, "Dias en feedlot: " + closeout.getDiasEnFeedlot());
                y -= 10;
                y = linea(c, bold, 11, MARGEN, y, "Plantel");
                y = linea(c, normal, 9, MARGEN, y,
                        "Cabezas inicial: " + closeout.getCabezasInicial()
                                + " | actuales: " + closeout.getCabezasActuales()
                                + " | muertes: " + closeout.getTotalMuertes());
                y = linea(c, normal, 9, MARGEN, y, "Mortalidad %: " + closeout.getMortalidadPct());
                y -= 8;
                y = linea(c, bold, 11, MARGEN, y, "Desempeno zootecnico");
                y = linea(c, normal, 9, MARGEN, y,
                        "Peso ingreso: " + closeout.getPesoIngresoKg() + " kg | actual: " + closeout.getPesoActualKg() + " kg");
                y = linea(c, normal, 9, MARGEN, y, "GMD: " + closeout.getGmd() + " kg/dia");
                y = linea(c, normal, 9, MARGEN, y, "Kg ganados: " + closeout.getKgGanados());
                y = linea(c, normal, 9, MARGEN, y, "Conversion alimenticia: " + valor(closeout.getConversionAlimenticia()));
                y = linea(c, normal, 9, MARGEN, y,
                        "Alimento total: " + closeout.getTotalAlimentoKg() + " kg | MS: " + closeout.getTotalAlimentoMsKg());
                y = linea(c, normal, 9, MARGEN, y, "Head days: " + closeout.getHeadDays());
                y -= 8;
                y = linea(c, bold, 11, MARGEN, y, "Economico");
                y = linea(c, normal, 9, MARGEN, y, "Costo compra: " + valor(closeout.getCostoCompra()));
                y = linea(c, normal, 9, MARGEN, y, "Costo alimento: " + valor(closeout.getCostoAlimento()));
                y = linea(c, normal, 9, MARGEN, y, "Costo hoteleria: " + valor(closeout.getCostoHoteleria()));
                y = linea(c, normal, 9, MARGEN, y, "Costo acumulado: " + closeout.getCostoAcumulado());
                y = linea(c, normal, 9, MARGEN, y, "Ingresos ventas: " + closeout.getIngresosVentas());
                y = linea(c, normal, 9, MARGEN, y, "Margen: " + closeout.getMargen());
                y = linea(c, normal, 9, MARGEN, y, "Breakeven $/kg: " + valor(closeout.getBreakevenKg()));
                y -= 12;
                y = linea(c, normal, 8, MARGEN, y,
                        "Generado: " + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private float linea(PDPageContentStream c, PDType1Font font, float size, float x, float y, String t)
            throws IOException {
        c.beginText();
        c.setFont(font, size);
        c.newLineAtOffset(x, y);
        c.showText(sanitizar(t));
        c.endText();
        return y - ALTURA_LINEA;
    }

    private static String valor(Object o) {
        return o != null ? o.toString() : "-";
    }

    private static String sanitizar(String s) {
        if (s == null) {
            return "";
        }
        return s
                .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                .replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U")
                .replace("ñ", "n").replace("Ñ", "N")
                .replace((char) 0xA0, ' ');
    }
}
