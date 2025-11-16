package com.agrocloud.service;

import com.agrocloud.model.entity.User;
import com.agrocloud.repository.UserRepository;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para manejar el EULA (End User License Agreement)
 */
@Service
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
    
    /**
     * Acepta el EULA para un usuario y genera el PDF firmado
     */
    @Transactional
    public void aceptarEula(User usuario, String ipAddress, String userAgent) {
        logger.info("Aceptando EULA para usuario: {}", usuario.getEmail());
        
        usuario.setEulaAceptado(true);
        usuario.setEulaFechaAceptacion(LocalDateTime.now());
        usuario.setEulaIpAddress(ipAddress);
        usuario.setEulaUserAgent(userAgent);
        usuario.setEulaVersion(VERSION_EULA);
        
        // Generar PDF del EULA firmado
        String pdfPath = generarPdfEulaFirmado(usuario);
        usuario.setEulaPdfPath(pdfPath);
        
        userRepository.save(usuario);
        logger.info("EULA aceptado y PDF generado para usuario: {}", usuario.getEmail());
    }
    
    /**
     * Genera el PDF del EULA con la información de aceptación del usuario
     */
    private String generarPdfEulaFirmado(User usuario) {
        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // Fuentes
            PDType1Font fontTitulo = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontNegrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            
            float margin = 50;
            float yPosition = 750;
            float lineHeight = 20;
            float fontSize = 12;
            float fontSizeTitulo = 16;
            
            // Título
            contentStream.beginText();
            contentStream.setFont(fontTitulo, fontSizeTitulo);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("EULA – Acuerdo de Licencia de Usuario Final (Agrocloud)");
            contentStream.endText();
            
            yPosition -= 30;
            
            // Versión y fecha
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String fechaFormateada = usuario.getEulaFechaAceptacion().format(formatter);
            
            contentStream.beginText();
            contentStream.setFont(fontNormal, fontSize);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Versión " + VERSION_EULA + " – Fecha: " + fechaFormateada);
            contentStream.endText();
            
            yPosition -= 40;
            
            // Contenido del EULA
            String[] parrafos = {
                "Este Acuerdo de Licencia de Usuario Final (\"EULA\") establece los términos y condiciones que rigen el uso del software Agrocloud.",
                "Al registrarse, acceder o utilizar Agrocloud, el Usuario acepta íntegramente este acuerdo. Si no está de acuerdo, no debe utilizar el sistema.",
                "",
                "1. Objeto",
                "Agrocloud es un software de gestión para el sector agropecuario. Este EULA regula el uso del sistema, incluyendo su acceso, funciones, actualizaciones y soporte.",
                "",
                "2. Otorgamiento de Licencia",
                "El titular de Agrocloud otorga al Usuario una licencia personal, limitada, no exclusiva, intransferible y revocable para utilizar el sistema conforme a este EULA.",
                "El Usuario no adquiere derechos de propiedad sobre el software.",
                "",
                "3. Propiedad Intelectual",
                "Agrocloud, su código fuente, diseño, bases de datos, contenidos, marcas y documentación son propiedad exclusiva de su titular.",
                "Los datos ingresados por el Usuario son de su exclusiva propiedad; Agrocloud solo los utiliza para la operación del sistema.",
                "",
                "4. Restricciones",
                "El Usuario no podrá, en ningún caso:",
                "• Copiar, modificar, desarmar, descompilar o realizar ingeniería inversa del software.",
                "• Transferir, sublicenciar, revender o ceder el acceso a terceros.",
                "• Intentar obtener acceso no autorizado a funciones, servidores o bases de datos.",
                "• Interferir con la operación del servicio o utilizarlo con fines ilegales o incompatibles con normativas vigentes (incluyendo SENASA u otros organismos).",
                "",
                "5. Protección de Datos Personales",
                "Agrocloud cumple con la Ley 25.326 (Argentina).",
                "El Usuario autoriza el tratamiento de sus datos únicamente para:",
                "• Operación normal del sistema",
                "• Soporte, mantenimiento y mejoras",
                "• Cumplimiento de obligaciones legales",
                "Los datos no se comercializan ni se comparten con terceros sin autorización del Usuario, salvo exigencia legal.",
                "",
                "6. Disponibilidad del Servicio",
                "Agrocloud procurará mantener el sistema operativo de forma continua, pero no garantiza la disponibilidad permanente debido a:",
                "• Actualizaciones",
                "• Mantenimiento",
                "• Fallas de red o servicios de terceros",
                "El titular no será responsable por interrupciones, pérdida de datos o daños derivados de causas fuera de su control.",
                "",
                "7. Limitación de Responsabilidad",
                "El Usuario acepta que:",
                "• Agrocloud no es responsable por decisiones de producción o comerciales basadas en la información generada por el sistema.",
                "• El titular del software no garantiza resultados productivos, financieros ni técnicos.",
                "• La responsabilidad total por cualquier reclamo no superará el monto efectivamente abonado por el Usuario durante los últimos tres (3) meses previos al reclamo.",
                "",
                "8. Planes, Pagos y Renovaciones",
                "Los precios, planes y modalidades de contratación se detallan en la web o dentro del sistema.",
                "Agrocloud podrá realizar cambios en las tarifas con notificación previa.",
                "La falta de pago podrá derivar en suspensión o finalización del acceso.",
                "",
                "9. Cancelación",
                "El Usuario puede cancelar su cuenta en cualquier momento.",
                "Agrocloud podrá suspender o finalizar el acceso si detecta:",
                "• Incumplimiento del EULA",
                "• Uso indebido del sistema",
                "• Actividades fraudulentas o ilegales",
                "Los datos podrán conservarse por un plazo máximo de 90 días, salvo obligación legal en contrario.",
                "",
                "10. Actualizaciones",
                "Agrocloud podrá implementar mejoras, cambios, nuevas funciones o modificaciones sin previo aviso.",
                "El Usuario acepta que dichas actualizaciones forman parte del servicio y quedan cubiertas por este EULA.",
                "",
                "11. Jurisdicción y Ley Aplicable",
                "Este EULA se rige por las leyes de la República Argentina.",
                "Las partes se someten a los tribunales con competencia en el domicilio del titular del software, renunciando a cualquier otra jurisdicción.",
                "",
                "12. Aceptación",
                "Al presionar \"Aceptar\", \"Crear cuenta\", \"Ingresar\" o al utilizar el sistema, el Usuario declara haber leído, comprendido y aceptado la totalidad de este EULA.",
                "",
                "DECLARACIÓN DE ACEPTACIÓN",
                "",
                "Yo, " + usuario.getFirstName() + " " + usuario.getLastName() + " (Email: " + usuario.getEmail() + "),",
                "declaro haber leído, comprendido y aceptado íntegramente el presente Acuerdo de Licencia de Usuario Final.",
                "",
                "Fecha y hora de aceptación: " + fechaFormateada,
                "Dirección IP: " + usuario.getEulaIpAddress(),
                "Navegador: " + (usuario.getEulaUserAgent() != null && usuario.getEulaUserAgent().length() > 100 
                    ? usuario.getEulaUserAgent().substring(0, 100) + "..." 
                    : usuario.getEulaUserAgent()),
                "",
                "Firma digital: [ACEPTADO]",
                "",
                "Fin del documento."
            };
            
            // Escribir contenido
            for (String parrafo : parrafos) {
                if (yPosition < 50) {
                    // Nueva página si es necesario
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
                
                // Detectar títulos (números seguidos de punto)
                if (parrafo.matches("^\\d+\\..*")) {
                    contentStream.beginText();
                    contentStream.setFont(fontNegrita, fontSize);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(parrafo);
                    contentStream.endText();
                    yPosition -= lineHeight + 5;
                } else {
                    // Dividir texto largo en líneas
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
            
            // Guardar PDF
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
    
    /**
     * Divide un texto largo en líneas que caben en el ancho de página
     */
    private String[] dividirTexto(String texto, int maxCaracteres) {
        if (texto.length() <= maxCaracteres) {
            return new String[]{texto};
        }
        
        java.util.List<String> lineas = new java.util.ArrayList<>();
        String[] palabras = texto.split(" ");
        StringBuilder lineaActual = new StringBuilder();
        
        for (String palabra : palabras) {
            if (lineaActual.length() + palabra.length() + 1 <= maxCaracteres) {
                if (lineaActual.length() > 0) {
                    lineaActual.append(" ");
                }
                lineaActual.append(palabra);
            } else {
                if (lineaActual.length() > 0) {
                    lineas.add(lineaActual.toString());
                    lineaActual = new StringBuilder(palabra);
                } else {
                    // Palabra muy larga, dividirla
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
    
    /**
     * Verifica si un usuario tiene el EULA aceptado
     */
    public boolean tieneEulaAceptado(User usuario) {
        return usuario.getEulaAceptado() != null && usuario.getEulaAceptado();
    }
    
    /**
     * Obtiene el PDF del EULA firmado por un usuario
     */
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

