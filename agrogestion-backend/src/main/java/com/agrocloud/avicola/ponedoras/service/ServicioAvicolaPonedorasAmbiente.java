package com.agrocloud.avicola.ponedoras.service;

import com.agrocloud.avicola.ponedoras.model.dto.AvicolaPonedorasAmbienteDiarioRespuesta;
import com.agrocloud.avicola.ponedoras.model.dto.AvicolaPonedorasAmbienteDiarioSolicitud;
import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasAmbienteDiario;
import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasGalpon;
import com.agrocloud.avicola.ponedoras.repository.AvicolaPonedorasAmbienteDiarioRepository;
import com.agrocloud.avicola.ponedoras.repository.AvicolaPonedorasGalponRepository;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioAvicolaPonedorasAmbiente {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final AvicolaPonedorasGalponRepository galponRepository;
    private final AvicolaPonedorasAmbienteDiarioRepository ambienteRepository;

    public ServicioAvicolaPonedorasAmbiente(
            ServicioSeguridadContexto servicioSeguridadContexto,
            AvicolaPonedorasGalponRepository galponRepository,
            AvicolaPonedorasAmbienteDiarioRepository ambienteRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.galponRepository = galponRepository;
        this.ambienteRepository = ambienteRepository;
    }

    @Transactional(readOnly = true)
    public List<AvicolaPonedorasAmbienteDiarioRespuesta> listarAmbienteDiario(
            Long galponId, LocalDate desde, LocalDate hasta) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        requerirGalpon(galponId, empresaId);
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate ini = desde != null ? desde : fin.minusMonths(3);
        return ambienteRepository.listarPorGalponYFechas(galponId, empresaId, ini, fin).stream()
                .map(ServicioAvicolaPonedorasAmbiente::aRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaPonedorasAmbienteDiarioRespuesta guardarAmbienteDiario(
            Long galponId, AvicolaPonedorasAmbienteDiarioSolicitud solicitud) {
        if (solicitud.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        AvicolaPonedorasGalpon galpon = requerirGalpon(galponId, empresaId);
        AvicolaPonedorasAmbienteDiario registro = ambienteRepository
                .buscarPorGalponYFecha(galponId, empresaId, solicitud.getFecha())
                .orElseGet(() -> {
                    AvicolaPonedorasAmbienteDiario nuevo = new AvicolaPonedorasAmbienteDiario();
                    nuevo.setGalpon(galpon);
                    nuevo.setEmpresaId(empresaId);
                    nuevo.setFecha(solicitud.getFecha());
                    return nuevo;
                });
        registro.setTemperaturaDia(solicitud.getTemperaturaDia());
        registro.setHumedadDia(solicitud.getHumedadDia());
        return aRespuesta(ambienteRepository.save(registro));
    }

    private AvicolaPonedorasGalpon requerirGalpon(Long galponId, Long empresaId) {
        return galponRepository.buscarPorIdYEmpresaId(galponId, empresaId)
                .orElseThrow(() -> new IllegalStateException("Galpón no encontrado o no pertenece a la empresa"));
    }

    private static AvicolaPonedorasAmbienteDiarioRespuesta aRespuesta(AvicolaPonedorasAmbienteDiario a) {
        AvicolaPonedorasAmbienteDiarioRespuesta dto = new AvicolaPonedorasAmbienteDiarioRespuesta();
        dto.setId(a.getId());
        dto.setGalponId(a.getGalpon() != null ? a.getGalpon().getId() : null);
        dto.setEmpresaId(a.getEmpresaId());
        dto.setFecha(a.getFecha());
        dto.setTemperaturaDia(a.getTemperaturaDia());
        dto.setHumedadDia(a.getHumedadDia());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());
        return dto;
    }
}
