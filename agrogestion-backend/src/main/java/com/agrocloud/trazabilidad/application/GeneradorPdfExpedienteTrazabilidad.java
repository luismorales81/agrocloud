package com.agrocloud.trazabilidad.application;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.trazabilidad.domain.TrazabilidadReporte;
import com.agrocloud.trazabilidad.dto.HechosExpedienteTrazabilidad;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF del expediente de ciclo de vida con maquetacion visual (cabecera, secciones, linea de tiempo).
 */
@Component
public class GeneradorPdfExpedienteTrazabilidad {

    private static final float ANCHO_PAGINA = 612f;
    private static final float ALTO_PAGINA = 792f;
    private static final float MARGEN = 42f;
    private static final float ANCHO_UTIL = ANCHO_PAGINA - 2 * MARGEN;
    private static final float ALTURA_LINEA = 12f;
    private static final float Y_PIE = 38f;
    private static final float Y_CONTENIDO_MAX = 755f;
    private static final float Y_CONTENIDO_MIN = 72f;

    private static final PDColor COLOR_PRIMARIO = rgb(5, 150, 105);
    private static final PDColor COLOR_PRIMARIO_OSCURO = rgb(4, 120, 87);
    private static final PDColor COLOR_BLANCO = rgb(255, 255, 255);
    private static final PDColor COLOR_TEXTO = rgb(31, 41, 55);
    private static final PDColor COLOR_TEXTO_SUAVE = rgb(107, 114, 128);
    private static final PDColor COLOR_FONDO_SECCION = rgb(236, 253, 245);
    private static final PDColor COLOR_FONDO_META = rgb(249, 250, 251);
    private static final PDColor COLOR_FONDO_FILA = rgb(255, 255, 255);
    private static final PDColor COLOR_FONDO_FILA_ALT = rgb(243, 244, 246);
    private static final PDColor COLOR_BORDE = rgb(229, 231, 235);
    private static final PDColor COLOR_ACENTO_LINEA = rgb(16, 185, 129);

    public byte[] generar(
            TrazabilidadReporte reporte,
            HechosExpedienteTrazabilidad hechos,
            Empresa empresa,
            User usuario) throws IOException {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            EstilosPdf est = new EstilosPdf(doc);
            est.reporteId = reporte.getId();
            est.nombreEmpresa = sanitizar(empresa.getNombre());
            est.abrirPagina();
            est.y = dibujarCabeceraPrincipal(est, hechos, reporte, empresa, usuario);
            est.y = dibujarBloqueMetadatos(est, reporte, hechos, usuario);
            est.y -= 10;
            for (HechosExpedienteTrazabilidad.Seccion sec : hechos.getSecciones()) {
                est.y = dibujarSeccion(est, sec);
            }
            est.y -= 6;
            est.y = dibujarLineaTiempo(est, hechos.getLineaTiempo());
            est.cerrarPaginaConPie();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private float dibujarCabeceraPrincipal(
            EstilosPdf est,
            HechosExpedienteTrazabilidad hechos,
            TrazabilidadReporte reporte,
            Empresa empresa,
            User usuario) throws IOException {
        float altoBanda = 78f;
        float yTop = est.y;
        float yBottom = yTop - altoBanda;
        rellenarRect(est, 0, yBottom, ANCHO_PAGINA, altoBanda, COLOR_PRIMARIO);
        rellenarRect(est, MARGEN, yBottom + 8, 4, altoBanda - 16, COLOR_BLANCO);

        float ty = yTop - 28;
        escribirTexto(est, est.bold, 16, MARGEN + 14, ty, "AGROGESTION", COLOR_BLANCO);
        escribirTexto(est, est.normal, 9, MARGEN + 14, ty - 16, "Expediente de trazabilidad — ciclo de vida", COLOR_BLANCO);

        String codigo = "TRC-" + reporte.getId();
        escribirTexto(est, est.bold, 11, ANCHO_PAGINA - MARGEN - 90, ty, codigo, COLOR_BLANCO);
        escribirTexto(est, est.normal, 8, ANCHO_PAGINA - MARGEN - 90, ty - 14,
                sanitizar(hechos.getModulo()), COLOR_BLANCO);

        float y = yBottom - 18;
        escribirTexto(est, est.bold, 13, MARGEN, y, sanitizar(hechos.getTitulo()), COLOR_TEXTO);
        y -= 16;
        if (hechos.getSubtitulo() != null && !hechos.getSubtitulo().isBlank()) {
            escribirTexto(est, est.normal, 10, MARGEN, y, sanitizar(hechos.getSubtitulo()), COLOR_TEXTO_SUAVE);
            y -= 14;
        }
        return y - 8;
    }

    private float dibujarBloqueMetadatos(
            EstilosPdf est,
            TrazabilidadReporte reporte,
            HechosExpedienteTrazabilidad hechos,
            User usuario) throws IOException {
        float altoCaja = 100f;
        est.asegurarEspacio(altoCaja + 20);
        float yTop = est.y;
        float yBottom = yTop - altoCaja;
        rellenarRect(est, MARGEN, yBottom, ANCHO_UTIL, altoCaja, COLOR_FONDO_META);
        dibujarBorde(est, MARGEN, yBottom, ANCHO_UTIL, altoCaja, COLOR_BORDE);

        float col1 = MARGEN + 12;
        float col2 = MARGEN + ANCHO_UTIL / 2f;
        float y = yTop - 18;
        escribirTexto(est, est.bold, 8, col1, y, "CODIGO DE REPORTE", COLOR_TEXTO_SUAVE);
        escribirTexto(est, est.bold, 10, col1, y - 12, "TRC-" + reporte.getId(), COLOR_PRIMARIO_OSCURO);

        escribirTexto(est, est.bold, 8, col2, y, "GENERADO", COLOR_TEXTO_SUAVE);
        escribirTexto(est, est.normal, 9, col2, y - 12,
                reporte.getGeneradoEn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                COLOR_TEXTO);

        y -= 32;
        escribirTexto(est, est.bold, 8, col1, y, "EMPRESA", COLOR_TEXTO_SUAVE);
        escribirTexto(est, est.normal, 9, col1, y - 12, sanitizar(est.nombreEmpresa), COLOR_TEXTO);

        escribirTexto(est, est.bold, 8, col2, y, "USUARIO", COLOR_TEXTO_SUAVE);
        escribirTexto(est, est.normal, 9, col2, y - 12, sanitizar(usuario.getEmail()), COLOR_TEXTO);

        if (hechos.getCampanaNombre() != null && !hechos.getCampanaNombre().isBlank()) {
            y -= 28;
            escribirTexto(est, est.bold, 8, col1, y, "CAMPANA DE GESTION", COLOR_TEXTO_SUAVE);
            String campanaTxt = hechos.getCampanaCodigo() != null
                    ? hechos.getCampanaCodigo() + " - " + hechos.getCampanaNombre()
                    : hechos.getCampanaNombre();
            escribirTexto(est, est.normal, 9, col1, y - 12, sanitizar(campanaTxt), COLOR_TEXTO);
        }

        String hash = reporte.getHashSnapshot();
        if (hash != null && hash.length() > 20) {
            hash = hash.substring(0, 20) + "...";
        }
        y -= 28;
        escribirTexto(est, est.bold, 8, col1, y, "HASH (SHA-256)", COLOR_TEXTO_SUAVE);
        escribirTexto(est, est.normal, 7, col1, y - 11, sanitizar(hash), COLOR_TEXTO_SUAVE);

        return yBottom - 14;
    }

    private float dibujarSeccion(EstilosPdf est, HechosExpedienteTrazabilidad.Seccion sec) throws IOException {
        List<HechosExpedienteTrazabilidad.Fila> filas = sec.getFilas();
        if (filas == null || filas.isEmpty()) {
            return est.y;
        }
        float altoTitulo = 22f;
        float altoFila = 16f;
        float altoTotal = altoTitulo + filas.size() * altoFila + 10;
        est.asegurarEspacio(altoTotal + 12);

        float yTop = est.y;
        float yBottom = yTop - altoTotal;
        rellenarRect(est, MARGEN, yBottom, ANCHO_UTIL, altoTotal, COLOR_FONDO_FILA);
        dibujarBorde(est, MARGEN, yBottom, ANCHO_UTIL, altoTotal, COLOR_BORDE);

        float yTituloBottom = yTop - altoTitulo;
        rellenarRect(est, MARGEN, yTituloBottom, ANCHO_UTIL, altoTitulo, COLOR_FONDO_SECCION);
        rellenarRect(est, MARGEN, yTituloBottom, 4, altoTitulo, COLOR_PRIMARIO);
        escribirTexto(est, est.bold, 10, MARGEN + 12, yTop - 14, sanitizar(sec.getTitulo()), COLOR_PRIMARIO_OSCURO);

        float yFila = yTituloBottom - 4;
        int i = 0;
        for (HechosExpedienteTrazabilidad.Fila f : filas) {
            if (i % 2 == 1) {
                rellenarRect(est, MARGEN + 1, yFila - altoFila, ANCHO_UTIL - 2, altoFila, COLOR_FONDO_FILA_ALT);
            }
            escribirTexto(est, est.bold, 8, MARGEN + 12, yFila - 11,
                    truncar(sanitizar(f.getEtiqueta()), 42), COLOR_TEXTO_SUAVE);
            escribirParrafoEnAncho(est, est.normal, 8, MARGEN + 160, yFila - 11,
                    sanitizar(f.getValor()), COLOR_TEXTO, ANCHO_UTIL - 172);
            yFila -= altoFila;
            i++;
        }
        return yBottom - 12;
    }

    private float dibujarLineaTiempo(EstilosPdf est, List<HechosExpedienteTrazabilidad.EventoLinea> eventos)
            throws IOException {
        est.asegurarEspacio(50);
        float yTop = est.y;
        rellenarRect(est, MARGEN, yTop - 24, ANCHO_UTIL, 24, COLOR_FONDO_SECCION);
        rellenarRect(est, MARGEN, yTop - 24, 4, 24, COLOR_PRIMARIO);
        escribirTexto(est, est.bold, 11, MARGEN + 12, yTop - 16, "Linea de tiempo", COLOR_PRIMARIO_OSCURO);
        est.y = yTop - 32;

        if (eventos == null || eventos.isEmpty()) {
            est.asegurarEspacio(24);
            escribirTexto(est, est.normal, 9, MARGEN + 12, est.y,
                    "Sin eventos adicionales registrados en el expediente.", COLOR_TEXTO_SUAVE);
            return est.y - 16;
        }

        float xLinea = MARGEN + 20;
        float yInicio = est.y;
        for (int i = 0; i < eventos.size(); i++) {
            HechosExpedienteTrazabilidad.EventoLinea ev = eventos.get(i);
            est.asegurarEspacio(36);
            float yEvento = est.y;

            rellenarRect(est, xLinea - 5, yEvento - 5, 10, 10, COLOR_ACENTO_LINEA);
            rellenarRect(est, xLinea - 3, yEvento - 3, 6, 6, COLOR_BLANCO);

            String fecha = ev.getFecha() != null ? ev.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "?";
            escribirTexto(est, est.bold, 8, MARGEN + 32, yEvento, fecha, COLOR_TEXTO);

            String cat = sanitizar(ev.getCategoria());
            float anchoCat = Math.min(cat.length() * 4.5f + 12, 90);
            rellenarRect(est, MARGEN + 100, yEvento - 9, anchoCat, 12, COLOR_PRIMARIO);
            escribirTexto(est, est.bold, 7, MARGEN + 104, yEvento - 1, cat, COLOR_BLANCO);

            est.y = escribirParrafoEnAncho(est, est.normal, 8, MARGEN + 32, yEvento - 14,
                    sanitizar(ev.getDescripcion()), COLOR_TEXTO, ANCHO_UTIL - 44);
            est.y -= 8;
        }

        float yFin = est.y + 4;
        if (yFin < yInicio - 10) {
            dibujarLineaVertical(est, xLinea, yInicio, yFin, COLOR_BORDE);
        }
        return est.y - 8;
    }

    private static void dibujarLineaVertical(EstilosPdf est, float x, float yTop, float yBottom, PDColor color)
            throws IOException {
        est.stream.setStrokingColor(color);
        est.stream.setLineWidth(1.2f);
        est.stream.moveTo(x, yBottom);
        est.stream.lineTo(x, yTop);
        est.stream.stroke();
    }

    private static void dibujarBorde(EstilosPdf est, float x, float y, float w, float h, PDColor color)
            throws IOException {
        est.stream.setStrokingColor(color);
        est.stream.setLineWidth(0.6f);
        est.stream.addRect(x, y, w, h);
        est.stream.stroke();
    }

    private static void rellenarRect(EstilosPdf est, float x, float y, float w, float h, PDColor color)
            throws IOException {
        est.stream.setNonStrokingColor(color);
        est.stream.addRect(x, y, w, h);
        est.stream.fill();
    }

    private static float escribirTexto(
            EstilosPdf est, PDType1Font font, float size, float x, float y, String texto, PDColor color)
            throws IOException {
        est.asegurarEspacio(ALTURA_LINEA + 4);
        y = est.y;
        est.stream.setNonStrokingColor(color);
        est.stream.beginText();
        est.stream.setFont(font, size);
        est.stream.newLineAtOffset(x, y);
        est.stream.showText(sanitizar(texto));
        est.stream.endText();
        est.y = y - ALTURA_LINEA;
        return est.y;
    }

    private static float escribirParrafoEnAncho(
            EstilosPdf est, PDType1Font font, float size, float x, float yIn,
            String texto, PDColor color, float anchoMax) throws IOException {
        if (texto == null || texto.isBlank()) {
            return yIn;
        }
        int maxChars = Math.max(20, (int) (anchoMax / (size * 0.52f)));
        String[] words = sanitizar(texto).split(" ");
        StringBuilder linea = new StringBuilder();
        float y = yIn;
        for (String w : words) {
            if (linea.length() + w.length() + 1 > maxChars) {
                y = escribirTexto(est, font, size, x, y, linea.toString(), color);
                linea = new StringBuilder(w);
            } else {
                if (linea.length() > 0) {
                    linea.append(" ");
                }
                linea.append(w);
            }
        }
        if (linea.length() > 0) {
            y = escribirTexto(est, font, size, x, y, linea.toString(), color);
        }
        return y;
    }

    private static String truncar(String s, int max) {
        if (s == null || s.length() <= max) {
            return s != null ? s : "";
        }
        return s.substring(0, max - 3) + "...";
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

    private static PDColor rgb(int r, int g, int b) {
        return new PDColor(new float[] { r / 255f, g / 255f, b / 255f }, PDDeviceRGB.INSTANCE);
    }

    private static class EstilosPdf {
        final PDDocument doc;
        final PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        final PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDPage page;
        PDPageContentStream stream;
        float y = Y_CONTENIDO_MAX;
        int numeroPagina = 0;
        Long reporteId;
        String nombreEmpresa = "";

        EstilosPdf(PDDocument doc) {
            this.doc = doc;
        }

        void abrirPagina() throws IOException {
            numeroPagina++;
            page = new PDPage();
            doc.addPage(page);
            stream = new PDPageContentStream(doc, page);
            y = Y_CONTENIDO_MAX;
            if (numeroPagina > 1) {
                dibujarMiniCabecera(this);
            }
        }

        void cerrarPaginaConPie() throws IOException {
            if (stream != null) {
                dibujarPie(this);
                stream.close();
                stream = null;
            }
        }

        void asegurarEspacio(float necesario) throws IOException {
            if (y - necesario < Y_CONTENIDO_MIN) {
                cerrarPaginaConPie();
                abrirPagina();
            }
        }
    }

    private static void dibujarMiniCabecera(EstilosPdf est) throws IOException {
        float alto = 28f;
        float yTop = est.y;
        float yBottom = yTop - alto;
        rellenarRect(est, 0, yBottom, ANCHO_PAGINA, alto, COLOR_PRIMARIO);
        escribirTexto(est, est.bold, 9, MARGEN, yTop - 12,
                "AgroGestion — Expediente TRC-" + est.reporteId, COLOR_BLANCO);
        est.y = yBottom - 10;
    }

    private static void dibujarPie(EstilosPdf est) throws IOException {
        float yLinea = Y_PIE + 14;
        est.stream.setStrokingColor(COLOR_BORDE);
        est.stream.setLineWidth(0.5f);
        est.stream.moveTo(MARGEN, yLinea);
        est.stream.lineTo(ANCHO_PAGINA - MARGEN, yLinea);
        est.stream.stroke();

        String izq = "AgroGestion | " + est.nombreEmpresa;
        String der = "Pagina " + est.numeroPagina;
        escribirTexto(est, est.normal, 7, MARGEN, Y_PIE, izq, COLOR_TEXTO_SUAVE);
        float anchoDer = der.length() * 4f;
        escribirTexto(est, est.normal, 7, ANCHO_PAGINA - MARGEN - anchoDer, Y_PIE, der, COLOR_TEXTO_SUAVE);
    }
}
