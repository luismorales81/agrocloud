package com.agrocloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Carga {@code .env} y {@code .env.local} al proceso (system properties) sin pisar variables ya definidas.
 */
public final class CargadorVariablesEntornoLocal {

    private static final Logger log = LoggerFactory.getLogger(CargadorVariablesEntornoLocal.class);

    private CargadorVariablesEntornoLocal() {
    }

    public static void cargar() {
        Path directorioTrabajo = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        List<Path> candidatos = new ArrayList<>();
        candidatos.add(directorioTrabajo.resolve(".env"));
        candidatos.add(directorioTrabajo.resolve(".env.local"));

        String nombreDirectorio = directorioTrabajo.getFileName() != null
                ? directorioTrabajo.getFileName().toString()
                : "";
        Path padre = directorioTrabajo.getParent();
        if ("agrogestion-backend".equals(nombreDirectorio) && padre != null) {
            candidatos.add(padre.resolve(".env"));
            candidatos.add(padre.resolve(".env.local"));
            candidatos.add(padre.resolve("agrogestion-frontend").resolve(".env.local"));
        } else {
            candidatos.add(directorioTrabajo.resolve("agrogestion-frontend").resolve(".env.local"));
        }

        int archivosCargados = 0;
        for (Path ruta : candidatos) {
            if (Files.isRegularFile(ruta)) {
                cargarArchivo(ruta);
                archivosCargados++;
            }
        }
        if (archivosCargados == 0) {
            log.info("No se encontró .env ni .env.local; se usan solo variables del sistema");
        }
    }

    private static void cargarArchivo(Path ruta) {
        try {
            int definidas = 0;
            for (String linea : Files.readAllLines(ruta, StandardCharsets.UTF_8)) {
                String recortada = linea.trim();
                if (recortada.isEmpty() || recortada.startsWith("#") || !recortada.contains("=")) {
                    continue;
                }
                int separador = recortada.indexOf('=');
                String nombre = recortada.substring(0, separador).trim();
                String valor = recortada.substring(separador + 1).trim();
                if (nombre.isEmpty() || valor.isEmpty()) {
                    continue;
                }
                if (System.getenv(nombre) != null && !System.getenv(nombre).isBlank()) {
                    continue;
                }
                if (System.getProperty(nombre) != null && !System.getProperty(nombre).isBlank()) {
                    continue;
                }
                System.setProperty(nombre, valor);
                definidas++;
            }
            log.info("Variables locales cargadas desde {} ({})", ruta.toAbsolutePath(), definidas);
        } catch (Exception e) {
            log.warn("No se pudo leer {}: {}", ruta.toAbsolutePath(), e.getMessage());
        }
    }
}
