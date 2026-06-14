package com.agrocloud.core.application;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.infrastructure.UserRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para manejar el EULA (End User License Agreement)
 */
@Service("eulaServiceCore")
public class EulaService {

    private static final Logger logger = LoggerFactory.getLogger(EulaService.class);
    private static final String VERSION_EULA = "1.0";

    @Value("${app.eula.storage.path:./eula-pdfs}")
    private String storagePath;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    private void inicializar() {
        crearDirectorioSiNoExiste();
    }

    private void crearDirectorioSiNoExiste() {
        try {
            if (storagePath == null || storagePath.isEmpty()) {
                storagePath = "./eula-pdfs";
            }
            Path path = Paths.get(storagePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Directorio EULA creado: {}", storagePath);
            }
        } catch (IOException e) {
            logger.error("Error creando directorio EULA: {}", e.getMessage());
        }
    }

    @Transactional
    public void aceptarEula(User usuario, String ipAddress, String userAgent) {
        logger.info("Aceptando EULA para usuario: {}", usuario.getEmail());
        usuario.setEulaAceptado(true);
        usuario.setEulaFechaAceptacion(LocalDateTime.now());
        usuario.setEulaIpAddress(ipAddress);
        usuario.setEulaUserAgent(userAgent);
        usuario.setEulaVersion(VERSION_EULA);
        String pdfPath = generarPdfEulaFirmado(usuario);
        usuario.setEulaPdfPath(pdfPath);
        userRepository.save(usuario);
        logger.info("EULA aceptado y PDF generado para usuario: {}", usuario.getEmail());
    }

    private String generarPdfEulaFirmado(User usuario) {
        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            PDType1Font fontTitulo = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontNegrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            float margin = 50;
            float yPosition = 750;
            float lineHeight = 20;
            float fontSize = 12;
            float fontSizeTitulo = 16;
            contentStream.beginText();
            contentStream.setFont(fontTitulo, fontSizeTitulo);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("EULA – Acuerdo de Licencia de Usuario Final (Agrocloud)");
            contentStream.endText();
            yPosition -= 30;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String fechaFormateada = usuario.getEulaFechaAceptacion().format(formatter);
            contentStream.beginText();
            contentStream.setFont(fontNormal, fontSize);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Versión " + VERSION_EULA + " – Fecha: " + fechaFormateada);
            contentStream.endText();
            yPosition -= 40;
            String[] parrafos = {
                "Este Acuerdo de Licencia de Usuario Final (\"EULA\") establece los términos y condiciones que rigen el uso del software Agrocloud.",
                "Al registrarse, acceder o utilizar Agrocloud, el Usuario acepta íntegramente este acuerdo.",
                "",
                "DECLARACIÓN DE ACEPTACIÓN",
                "",
                "Yo, " + usuario.getFirstName() + " " + usuario.getLastName() + " (Email: " + usuario.getEmail() + "),",
                "declaro haber leído, comprendido y aceptado íntegramente el presente Acuerdo de Licencia de Usuario Final.",
                "",
                "Fecha y hora de aceptación: " + fechaFormateada,
                "Dirección IP: " + usuario.getEulaIpAddress(),
                "",
                "Firma digital: [ACEPTADO]",
                "",
                "Fin del documento."
            };
            for (String parrafo : parrafos) {
                if (yPosition < 50) {
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = 750;
                }
                if (parrafo.isEmpty()) {
                    yPosition -= lineHeight;
                    continue;
                }
                if (parrafo.matches("^\\d+\\..*")) {
                    contentStream.beginText();
                    contentStream.setFont(fontNegrita, fontSize);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(parrafo);
                    contentStream.endText();
                    yPosition -= lineHeight + 5;
                } else {
                    String[] lineas = dividirTexto(parrafo, 90);
                    for (String linea : lineas) {
                        if (yPosition < 50) {
                            contentStream.close();
                            page = new PDPage();
                            document.addPage(page);
                            contentStream = new PDPageContentStream(document, page);
                            yPosition = 750;
                        }
                        contentStream.beginText();
                        contentStream.setFont(fontNormal, fontSize);
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText(linea);
                        contentStream.endText();
                        yPosition -= lineHeight;
                    }
                }
            }
            contentStream.close();
            String nombreArchivo = "EULA_" + usuario.getId() + "_" +
                usuario.getEulaFechaAceptacion().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            Path pdfPath = Paths.get(storagePath, nombreArchivo);
            document.save(pdfPath.toFile());
            document.close();
            logger.info("PDF EULA generado: {}", pdfPath.toString());
            return pdfPath.toString();
        } catch (IOException e) {
            logger.error("Error generando PDF EULA: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar PDF del EULA", e);
        }
    }

    private String[] dividirTexto(String texto, int maxCaracteres) {
        if (texto.length() <= maxCaracteres) {
            return new String[]{texto};
        }
        java.util.List<String> lineas = new java.util.ArrayList<>();
        String[] palabras = texto.split(" ");
        StringBuilder lineaActual = new StringBuilder();
        for (String palabra : palabras) {
            if (lineaActual.length() + palabra.length() + 1 <= maxCaracteres) {
                if (lineaActual.length() > 0) lineaActual.append(" ");
                lineaActual.append(palabra);
            } else {
                if (lineaActual.length() > 0) {
                    lineas.add(lineaActual.toString());
                    lineaActual = new StringBuilder(palabra);
                } else {
                    lineas.add(palabra.substring(0, Math.min(maxCaracteres, palabra.length())));
                    if (palabra.length() > maxCaracteres) {
                        lineaActual.append(palabra.substring(maxCaracteres));
                    }
                }
            }
        }
        if (lineaActual.length() > 0) {
            lineas.add(lineaActual.toString());
        }
        return lineas.toArray(new String[0]);
    }

    public boolean tieneEulaAceptado(User usuario) {
        return usuario.getEulaAceptado() != null && usuario.getEulaAceptado();
    }

    public Resource obtenerPdfEula(User usuario) {
        try {
            if (usuario.getEulaPdfPath() == null || usuario.getEulaPdfPath().isEmpty()) {
                throw new RuntimeException("No existe PDF del EULA para este usuario");
            }
            Path filePath = Paths.get(usuario.getEulaPdfPath());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("El archivo PDF no existe o no es accesible");
            }
        } catch (Exception e) {
            logger.error("Error obteniendo PDF EULA: {}", e.getMessage());
            throw new RuntimeException("Error al obtener PDF del EULA", e);
        }
    }
}
