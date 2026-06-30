package com.agrocloud.lecheria.service;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.lecheria.model.dto.LecheriaImportControlLecheroRespuesta;
import com.agrocloud.lecheria.model.dto.LecheriaRegistroOrdeneSolicitud;
import com.agrocloud.lecheria.model.entity.LecheriaAnimal;
import com.agrocloud.lecheria.model.entity.LecheriaImportControlLechero;
import com.agrocloud.lecheria.model.enums.LecheriaTurnoOrdene;
import com.agrocloud.lecheria.repository.LecheriaAnimalRepository;
import com.agrocloud.lecheria.repository.LecheriaImportControlLecheroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioLecheriaImportacion {

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final LecheriaAnimalRepository animalRepository;
    private final LecheriaImportControlLecheroRepository importRepository;
    private final ServicioLecheriaOrdene servicioOrdene;

    public ServicioLecheriaImportacion(
            ServicioSeguridadContexto servicioSeguridadContexto,
            LecheriaAnimalRepository animalRepository,
            LecheriaImportControlLecheroRepository importRepository,
            ServicioLecheriaOrdene servicioOrdene) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.animalRepository = animalRepository;
        this.importRepository = importRepository;
        this.servicioOrdene = servicioOrdene;
    }

    @Transactional
    public LecheriaImportControlLecheroRespuesta importarControlLechero(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo CSV es obligatorio");
        }
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();

        int procesadas = 0;
        int errores = 0;
        List<String> detalleErrores = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            boolean primera = true;
            while ((linea = reader.readLine()) != null) {
                if (primera) {
                    primera = false;
                    if (linea.toLowerCase().contains("identificacion")) continue;
                }
                if (linea.isBlank()) continue;
                String[] cols = linea.split(";", -1);
                if (cols.length < 4) cols = linea.split(",", -1);
                try {
                    procesarFila(empresaId, cols);
                    procesadas++;
                } catch (Exception ex) {
                    errores++;
                    detalleErrores.add("Línea: " + linea + " -> " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error leyendo CSV: " + e.getMessage());
        }

        LecheriaImportControlLechero reg = new LecheriaImportControlLechero();
        reg.setEmpresaId(empresaId);
        reg.setNombreArchivo(archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "import.csv");
        reg.setFilasProcesadas(procesadas);
        reg.setFilasError(errores);
        reg.setUsuarioId(usuarioId);
        if (!detalleErrores.isEmpty()) {
            reg.setDetalleErrores(String.join("\n", detalleErrores));
        }
        reg = importRepository.save(reg);
        return aImportRespuesta(reg);
    }

    private void procesarFila(Long empresaId, String[] cols) {
        String identificacion = cols[0].trim();
        LocalDate fecha = LocalDate.parse(cols[1].trim(), FMT_FECHA);
        LecheriaTurnoOrdene turno = LecheriaTurnoOrdene.valueOf(cols[2].trim().toUpperCase());
        BigDecimal litros = new BigDecimal(cols[3].trim().replace(',', '.'));

        LecheriaAnimal animal = animalRepository.listarPorEmpresaId(empresaId).stream()
                .filter(a -> a.getIdentificacion().equalsIgnoreCase(identificacion))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Animal no encontrado: " + identificacion));

        LecheriaRegistroOrdeneSolicitud solicitud = new LecheriaRegistroOrdeneSolicitud();
        solicitud.setFecha(fecha);
        solicitud.setTurno(turno);
        solicitud.setLitros(litros);
        if (cols.length > 4 && !cols[4].isBlank()) solicitud.setRcs(Long.parseLong(cols[4].trim()));
        servicioOrdene.registrarOrdene(animal.getId(), solicitud);
    }

    private LecheriaImportControlLecheroRespuesta aImportRespuesta(LecheriaImportControlLechero reg) {
        LecheriaImportControlLecheroRespuesta dto = new LecheriaImportControlLecheroRespuesta();
        dto.setId(reg.getId());
        dto.setNombreArchivo(reg.getNombreArchivo());
        dto.setFilasProcesadas(reg.getFilasProcesadas());
        dto.setFilasError(reg.getFilasError());
        dto.setDetalleErrores(reg.getDetalleErrores());
        dto.setCreatedAt(reg.getCreatedAt());
        return dto;
    }
}
