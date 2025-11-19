package com.agrocloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired(required = false)
    private JavaMailSender javaMailSender;
    
    @Value("${spring.mail.from:noreply@agrocloud.com}")
    private String fromEmail;
    
    @Value("${email.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    /**
     * Enviar email de recuperación de contraseña con HTML
     */
    public void sendPasswordResetEmail(String email, String resetToken) {
        logger.info("========================================");
        logger.info("📧 [EmailService] Iniciando envío de email de recuperación");
        logger.info("========================================");
        logger.info("Destinatario: {}", email);
        logger.info("Remitente: {}", fromEmail);
        logger.info("Token: {}", resetToken);
        
        try {
            // Codificar el token para URL (importante para caracteres especiales)
            String encodedToken = java.net.URLEncoder.encode(resetToken, java.nio.charset.StandardCharsets.UTF_8);
            String resetUrl = frontendUrl + "/reset-password?token=" + encodedToken;
            logger.info("URL de recuperación: {}", resetUrl);
            logger.info("Token original: {}", resetToken);
            logger.info("Token codificado: {}", encodedToken);
            
            if (javaMailSender == null) {
                logger.error("❌ [EmailService] JavaMailSender es NULL - No está configurado");
                logger.warn("JavaMailSender no configurado. Solo se loguea la información.");
                logger.info("Email de recuperación (simulado) - Para: {}, URL: {}", email, resetUrl);
                return;
            }
            
            logger.info("✅ [EmailService] JavaMailSender encontrado, creando mensaje...");
            
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Recuperación de Contraseña - AgroCloud");
            
            logger.info("📝 [EmailService] Cargando template HTML...");
            // Cargar template HTML
            String htmlContent = loadEmailTemplate("templates/email-password-reset.html");
            
            // Reemplazar placeholders
            htmlContent = htmlContent.replace("{{resetUrl}}", resetUrl);
            logger.info("✅ [EmailService] Template HTML cargado y procesado ({} caracteres)", htmlContent.length());
            
            helper.setText(htmlContent, true);
            
            logger.info("📤 [EmailService] Enviando email a través de SMTP...");
            logger.info("   Host: {}", ((org.springframework.mail.javamail.JavaMailSenderImpl) javaMailSender).getHost());
            logger.info("   Port: {}", ((org.springframework.mail.javamail.JavaMailSenderImpl) javaMailSender).getPort());
            logger.info("   Username: {}", ((org.springframework.mail.javamail.JavaMailSenderImpl) javaMailSender).getUsername());
            
            javaMailSender.send(message);
            
            logger.info("========================================");
            logger.info("✅ [EmailService] Email enviado EXITOSAMENTE a: {}", email);
            logger.info("========================================");
            
        } catch (jakarta.mail.AuthenticationFailedException e) {
            logger.error("❌ [EmailService] ERROR DE AUTENTICACIÓN SMTP");
            logger.error("   Verifica las credenciales de Zoho Mail");
            logger.error("   Usuario: {}", ((org.springframework.mail.javamail.JavaMailSenderImpl) javaMailSender).getUsername());
            logger.error("   Error: {}", e.getMessage());
            throw new RuntimeException("Error de autenticación SMTP. Verifica las credenciales de Zoho Mail", e);
        } catch (jakarta.mail.MessagingException e) {
            logger.error("❌ [EmailService] ERROR DE MENSAJERÍA");
            logger.error("   Error: {}", e.getMessage());
            logger.error("   Causa: {}", e.getCause() != null ? e.getCause().getMessage() : "N/A");
            throw new RuntimeException("Error enviando email: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("❌ [EmailService] ERROR INESPERADO enviando email");
            logger.error("   Tipo: {}", e.getClass().getName());
            logger.error("   Mensaje: {}", e.getMessage());
            logger.error("   Stack trace completo:", e);
            throw new RuntimeException("Error enviando email de recuperación: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cargar template HTML desde recursos
     */
    private String loadEmailTemplate(String templatePath) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Error cargando template de email: {}", templatePath, e);
            // Fallback a template HTML básico
            return getDefaultPasswordResetTemplate();
        }
    }
    
    /**
     * Template HTML por defecto si no se puede cargar el archivo
     */
    private String getDefaultPasswordResetTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #1a5f2a 0%, #4a9556 100%); color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; background: #f9fafb; }
                    .button { display: inline-block; padding: 12px 24px; background: #2563eb; color: white; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { padding: 20px; text-align: center; color: #6b7280; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🌾 AgroCloud</h1>
                        <p>Sistema de Gestión Agropecuaria</p>
                    </div>
                    <div class="content">
                        <h2>Recuperación de Contraseña</h2>
                        <p>Hola,</p>
                        <p>Has solicitado recuperar tu contraseña. Haz clic en el botón siguiente:</p>
                        <a href="{{resetUrl}}" class="button">Restablecer Contraseña</a>
                        <p>Este enlace expirará en 24 horas.</p>
                        <p>Si no solicitaste este cambio, ignora este email.</p>
                    </div>
                    <div class="footer">
                        <p>Equipo AgroCloud</p>
                        <p>info@AgroCloud.com.ar</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
    
    /**
     * Enviar email de verificación con HTML
     */
    public void sendVerificationEmail(String email, String verificationToken) {
        logger.info("Enviando email de verificación a: {}", email);
        logger.info("Token de verificación: {}", verificationToken);
        
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;
            logger.info("URL de verificación: {}", verificationUrl);
            
            if (javaMailSender == null) {
                logger.warn("JavaMailSender no configurado. Solo se loguea la información.");
                logger.info("Email de verificación (simulado) - Para: {}, URL: {}", email, verificationUrl);
                return;
            }
            
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Verificación de Email - AgroCloud");
            
            // Usar template HTML similar al de recupero de contraseña
            String htmlContent = getDefaultPasswordResetTemplate()
                .replace("{{resetUrl}}", verificationUrl)
                .replace("Recuperación de Contraseña", "Verificación de Email")
                .replace("Has solicitado recuperar tu contraseña", "Gracias por registrarte en AgroCloud")
                .replace("Restablecer Contraseña", "Verificar Email")
                .replace("Este enlace expirará en 24 horas", "Haz clic en el botón para verificar tu cuenta")
                .replace("Si no solicitaste este cambio", "Si no creaste esta cuenta");
            
            helper.setText(htmlContent, true);
            
            javaMailSender.send(message);
            logger.info("Email de verificación enviado exitosamente a: {}", email);
            
        } catch (Exception e) {
            logger.error("Error enviando email de verificación a: {}", email, e);
            throw new RuntimeException("Error enviando email de verificación", e);
        }
    }
}
