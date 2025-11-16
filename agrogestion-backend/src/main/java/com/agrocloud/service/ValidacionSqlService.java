package com.agrocloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Servicio para validar y prevenir inyección SQL
 * Valida parámetros antes de usarlos en consultas
 */
@Service
public class ValidacionSqlService {

    private static final Logger logger = LoggerFactory.getLogger(ValidacionSqlService.class);

    // Palabras clave SQL peligrosas
    private static final Set<String> PALABRAS_CLAVE_SQL_PELIGROSAS = new HashSet<>(Arrays.asList(
        "SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER", "EXEC", "EXECUTE",
        "UNION", "SCRIPT", "SCRIPT>", "<SCRIPT", "SCRIPT/", "SCRIPT\\", "SCRIPT%", "SCRIPT&",
        "SCRIPT'", "SCRIPT\"", "SCRIPT(", "SCRIPT)", "SCRIPT[", "SCRIPT]", "SCRIPT{", "SCRIPT}",
        "SCRIPT;", "SCRIPT:", "SCRIPT=", "SCRIPT?", "SCRIPT!", "SCRIPT@", "SCRIPT#", "SCRIPT$",
        "SCRIPT^", "SCRIPT*", "SCRIPT+", "SCRIPT|", "SCRIPT~", "SCRIPT`", "SCRIPT,", "SCRIPT.",
        "SCRIPT/", "SCRIPT\\", "SCRIPT-", "SCRIPT_", "SCRIPT ", "SCRIPT\t", "SCRIPT\n", "SCRIPT\r",
        "OR", "AND", "WHERE", "FROM", "INTO", "VALUES", "SET", "TABLE", "DATABASE", "SCHEMA",
        "TRUNCATE", "GRANT", "REVOKE", "COMMIT", "ROLLBACK", "TRANSACTION", "PROCEDURE", "FUNCTION",
        "TRIGGER", "VIEW", "INDEX", "CONSTRAINT", "PRIMARY", "FOREIGN", "KEY", "REFERENCES",
        "CASCADE", "RESTRICT", "NO ACTION", "SET NULL", "DEFAULT", "CHECK", "UNIQUE", "NOT NULL",
        "NULL", "TRUE", "FALSE", "LIKE", "ILIKE", "SIMILAR TO", "REGEXP", "RLIKE", "SOUNDS LIKE",
        "BETWEEN", "IN", "EXISTS", "ANY", "ALL", "SOME", "CASE", "WHEN", "THEN", "ELSE", "END",
        "IF", "ELSEIF", "ELSE IF", "END IF", "LOOP", "WHILE", "REPEAT", "UNTIL", "FOR", "FOREACH",
        "BREAK", "CONTINUE", "LEAVE", "ITERATE", "RETURN", "RETURNS", "DECLARE", "SET", "BEGIN",
        "END", "CALL", "PREPARE", "EXECUTE", "DEALLOCATE", "PREPARE", "EXECUTE", "DEALLOCATE",
        "PREPARE", "EXECUTE", "DEALLOCATE", "PREPARE", "EXECUTE", "DEALLOCATE", "PREPARE",
        "EXECUTE", "DEALLOCATE", "PREPARE", "EXECUTE", "DEALLOCATE", "PREPARE", "EXECUTE",
        "DEALLOCATE", "PREPARE", "EXECUTE", "DEALLOCATE", "PREPARE", "EXECUTE", "DEALLOCATE"
    ));

    // Caracteres peligrosos para SQL
    private static final Pattern PATRON_CARACTERES_PELIGROSOS = Pattern.compile(
        "[';\"\\\\]|(--)|(/\\*)|(\\*/)|(xp_)", Pattern.CASE_INSENSITIVE
    );

    // Patrón para detectar intentos de inyección SQL común
    private static final Pattern PATRON_INYECCION_SQL = Pattern.compile(
        "(?i)(union\\s+select|insert\\s+into|delete\\s+from|update\\s+set|drop\\s+table|exec\\s*\\(|execute\\s*\\(|script\\s*>|'\\s*or\\s*'|'\\s*and\\s*'|'\\s*;\\s*--|'\\s*;\\s*/\\*|'\\s*;\\s*\\*/)",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Valida que un parámetro no contenga código SQL malicioso
     * 
     * @param parametro El parámetro a validar
     * @param nombreCampo Nombre del campo para logging
     * @return true si es seguro, false si contiene código peligroso
     */
    public boolean validarParametroSeguro(String parametro, String nombreCampo) {
        if (parametro == null) {
            return true;
        }

        String parametroUpper = parametro.toUpperCase().trim();

        // Verificar palabras clave SQL peligrosas
        for (String palabraClave : PALABRAS_CLAVE_SQL_PELIGROSAS) {
            if (parametroUpper.contains(palabraClave)) {
                logger.warn("⚠️ Intento de inyección SQL detectado en campo '{}': contiene palabra clave '{}'", 
                    nombreCampo, palabraClave);
                return false;
            }
        }

        // Verificar caracteres peligrosos
        if (PATRON_CARACTERES_PELIGROSOS.matcher(parametro).find()) {
            logger.warn("⚠️ Intento de inyección SQL detectado en campo '{}': contiene caracteres peligrosos", 
                nombreCampo);
            return false;
        }

        // Verificar patrones de inyección SQL comunes
        if (PATRON_INYECCION_SQL.matcher(parametro).find()) {
            logger.warn("⚠️ Intento de inyección SQL detectado en campo '{}': patrón sospechoso detectado", 
                nombreCampo);
            return false;
        }

        return true;
    }

    /**
     * Valida un CUIT argentino
     * Formato válido: XX-XXXXXXXX-X
     */
    public boolean validarFormatoCuit(String cuit) {
        if (cuit == null || cuit.isEmpty()) {
            return false;
        }

        // Patrón para CUIT argentino: XX-XXXXXXXX-X
        Pattern patronCuit = Pattern.compile("^\\d{2}-\\d{8}-\\d{1}$");
        
        if (!patronCuit.matcher(cuit).matches()) {
            logger.warn("⚠️ Formato de CUIT inválido: {}", cuit);
            return false;
        }

        // Validar que no contenga código SQL
        return validarParametroSeguro(cuit, "cuit");
    }

    /**
     * Valida un email
     */
    public boolean validarFormatoEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        // Patrón básico para email
        Pattern patronEmail = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        
        if (!patronEmail.matcher(email).matches()) {
            logger.warn("⚠️ Formato de email inválido: {}", email);
            return false;
        }

        // Validar que no contenga código SQL
        return validarParametroSeguro(email, "email");
    }

    /**
     * Valida un ID numérico
     */
    public boolean validarId(Long id) {
        if (id == null) {
            return false;
        }

        // Verificar que sea positivo
        if (id <= 0) {
            logger.warn("⚠️ ID inválido: debe ser mayor que 0");
            return false;
        }

        // Verificar que no sea excesivamente grande (posible overflow)
        if (id > Long.MAX_VALUE / 2) {
            logger.warn("⚠️ ID sospechosamente grande: {}", id);
            return false;
        }

        return true;
    }

    /**
     * Sanitiza un parámetro removiendo caracteres peligrosos
     * NOTA: Este método solo debe usarse como medida adicional.
     * La mejor práctica es usar parámetros preparados (PreparedStatement).
     */
    public String sanitizarParametro(String parametro) {
        if (parametro == null) {
            return null;
        }

        // Remover caracteres peligrosos
        String sanitizado = parametro
            .replace("'", "")
            .replace("\"", "")
            .replace(";", "")
            .replace("--", "")
            .replace("/*", "")
            .replace("*/", "")
            .replace("\\", "");

        return sanitizado.trim();
    }

    /**
     * Valida múltiples parámetros a la vez
     */
    public boolean validarParametrosSeguros(String... parametros) {
        for (int i = 0; i < parametros.length; i++) {
            if (!validarParametroSeguro(parametros[i], "parametro_" + i)) {
                return false;
            }
        }
        return true;
    }
}

