package com.agrocloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Servicio para enmascarar datos sensibles en logs y respuestas
 * Protege información fiscal y financiera según normativas argentinas
 */
@Service
public class EnmascaramientoDatosService {

    private static final Logger logger = LoggerFactory.getLogger(EnmascaramientoDatosService.class);

    /**
     * Enmascara un CUIT argentino
     * Formato: 20-12345678-9 -> 20-****5678-9
     */
    public String enmascararCuit(String cuit) {
        if (cuit == null || cuit.isEmpty()) {
            return cuit;
        }
        
        try {
            // Remover guiones para procesar
            String cuitSinGuiones = cuit.replace("-", "");
            
            if (cuitSinGuiones.length() != 11) {
                // Si no tiene formato válido, enmascarar todo excepto últimos 4 caracteres
                return enmascararTexto(cuit, 4);
            }
            
            // Formato: XX-XXXXXXXX-X
            // Mostrar primeros 2 dígitos, enmascarar 4 del medio, mostrar últimos 4 dígitos
            String prefijo = cuitSinGuiones.substring(0, 2);
            String medio = cuitSinGuiones.substring(2, 8);
            String sufijo = cuitSinGuiones.substring(8, 11);
            
            return prefijo + "-****" + medio.substring(4) + "-" + sufijo;
        } catch (Exception e) {
            logger.warn("Error al enmascarar CUIT: {}", cuit, e);
            return "**-**-*";
        }
    }

    /**
     * Enmascara un email
     * Formato: usuario@dominio.com -> usua***@dominio.com
     */
    public String enmascararEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        
        try {
            int indiceArroba = email.indexOf('@');
            if (indiceArroba == -1) {
                return enmascararTexto(email, 0);
            }
            
            String usuario = email.substring(0, indiceArroba);
            String dominio = email.substring(indiceArroba);
            
            // Mostrar primeros 4 caracteres del usuario, resto enmascarar
            if (usuario.length() <= 4) {
                return "***" + dominio;
            }
            
            String usuarioVisible = usuario.substring(0, Math.min(4, usuario.length()));
            return usuarioVisible + "***" + dominio;
        } catch (Exception e) {
            logger.warn("Error al enmascarar email: {}", email, e);
            return "***@***";
        }
    }

    /**
     * Enmascara un teléfono
     * Formato: 1123456789 -> 1123***789
     */
    public String enmascararTelefono(String telefono) {
        if (telefono == null || telefono.isEmpty()) {
            return telefono;
        }
        
        try {
            // Remover espacios y caracteres especiales
            String telefonoLimpio = telefono.replaceAll("[^0-9]", "");
            
            if (telefonoLimpio.length() < 4) {
                return "***";
            }
            
            // Mostrar primeros 4 y últimos 3 dígitos
            int longitud = telefonoLimpio.length();
            String prefijo = telefonoLimpio.substring(0, Math.min(4, longitud));
            String sufijo = longitud > 7 ? telefonoLimpio.substring(longitud - 3) : "";
            
            return prefijo + "***" + sufijo;
        } catch (Exception e) {
            logger.warn("Error al enmascarar teléfono: {}", telefono, e);
            return "***";
        }
    }

    /**
     * Enmascara un monto financiero
     * Formato: 123456.78 -> ******.** (solo muestra decimales)
     */
    public String enmascararMonto(BigDecimal monto) {
        if (monto == null) {
            return "0.00";
        }
        
        try {
            // Solo mostrar la parte decimal para referencia, pero no el monto completo
            String montoStr = monto.toString();
            int indicePunto = montoStr.indexOf('.');
            
            if (indicePunto != -1) {
                String decimales = montoStr.substring(indicePunto);
                return "******" + decimales;
            }
            
            return "******";
        } catch (Exception e) {
            logger.warn("Error al enmascarar monto: {}", monto, e);
            return "******";
        }
    }

    /**
     * Enmascara un texto genérico, mostrando solo los últimos caracteres
     */
    public String enmascararTexto(String texto, int caracteresVisibles) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        
        if (texto.length() <= caracteresVisibles) {
            return "***";
        }
        
        int longitud = texto.length();
        String visible = texto.substring(longitud - caracteresVisibles);
        return "***" + visible;
    }

    /**
     * Enmascara una dirección, mostrando solo ciudad y provincia
     */
    public String enmascararDireccion(String direccion) {
        if (direccion == null || direccion.isEmpty()) {
            return direccion;
        }
        
        // Si la dirección es muy corta, enmascarar todo
        if (direccion.length() < 10) {
            return "***";
        }
        
        // Mostrar solo últimos 15 caracteres
        return "***" + direccion.substring(Math.max(0, direccion.length() - 15));
    }

    /**
     * Enmascara datos según el tipo de campo
     */
    public String enmascararPorTipo(String valor, String tipoCampo) {
        if (valor == null || valor.isEmpty()) {
            return valor;
        }
        
        String tipoLower = tipoCampo.toLowerCase();
        
        if (tipoLower.contains("cuit") || tipoLower.contains("cuil")) {
            return enmascararCuit(valor);
        } else if (tipoLower.contains("email") || tipoLower.contains("correo")) {
            return enmascararEmail(valor);
        } else if (tipoLower.contains("telefono") || tipoLower.contains("phone") || tipoLower.contains("celular")) {
            return enmascararTelefono(valor);
        } else if (tipoLower.contains("direccion") || tipoLower.contains("address")) {
            return enmascararDireccion(valor);
        } else if (tipoLower.contains("password") || tipoLower.contains("contraseña")) {
            return "******";
        } else {
            // Por defecto, enmascarar mostrando últimos 4 caracteres
            return enmascararTexto(valor, 4);
        }
    }

    /**
     * Verifica si un campo debe ser enmascarado
     */
    public boolean esCampoSensible(String nombreCampo) {
        if (nombreCampo == null) {
            return false;
        }
        
        String campoLower = nombreCampo.toLowerCase();
        return campoLower.contains("cuit") ||
               campoLower.contains("cuil") ||
               campoLower.contains("email") ||
               campoLower.contains("telefono") ||
               campoLower.contains("phone") ||
               campoLower.contains("password") ||
               campoLower.contains("contraseña") ||
               campoLower.contains("monto") ||
               campoLower.contains("precio") ||
               campoLower.contains("direccion") ||
               campoLower.contains("address");
    }
}

