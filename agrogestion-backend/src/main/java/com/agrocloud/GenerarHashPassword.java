package com.agrocloud;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad local para generar hash BCrypt. No almacenar contraseñas en el código fuente.
 * Uso: variable de entorno PASSWORD_TO_HASH, o primer argumento en línea de comandos.
 */
public class GenerarHashPassword {

    public static void main(String[] args) {
        String texto = System.getenv("PASSWORD_TO_HASH");
        if ((texto == null || texto.isBlank()) && args.length > 0) {
            texto = args[0];
        }
        if (texto == null || texto.isBlank()) {
            System.err.println("Indique la contraseña sin dejarla en el repositorio:");
            System.err.println("  PASSWORD_TO_HASH='su_texto' mvn -q exec:java -Dexec.mainClass=com.agrocloud.GenerarHashPassword");
            System.err.println("  o: java ... GenerarHashPassword \"su_texto\"");
            System.exit(1);
            return;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(texto);
        System.out.println("Hash BCrypt (no imprime la contraseña en claro):");
        System.out.println(hash);
    }
}
