package com.agrocloud.core.application;

import com.agrocloud.core.domain.Recordatorio;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.infrastructure.RecordatorioRepository;
import com.agrocloud.core.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RecordatorioService {

    @Autowired
    private RecordatorioRepository recordatorioRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtener todos los recordatorios activos de un usuario
     */
    public List<Recordatorio> getRecordatoriosByUsuario(Long usuarioId) {
        return recordatorioRepository.findByUsuarioIdAndActivoTrueOrderByFechaAsc(usuarioId);
    }

    /**
     * Obtener recordatorios de un usuario por rango de fechas
     */
    public List<Recordatorio> getRecordatoriosByUsuarioAndRango(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return recordatorioRepository.findByUsuarioIdAndRangoFechas(usuarioId, fechaInicio, fechaFin);
    }

    /**
     * Obtener recordatorios pendientes de un usuario
     */
    public List<Recordatorio> getRecordatoriosPendientes(Long usuarioId) {
        return recordatorioRepository.findPendientesByUsuarioId(usuarioId, LocalDate.now());
    }

    /**
     * Crear un nuevo recordatorio
     */
    public Recordatorio crearRecordatorio(Recordatorio recordatorio) {
        if (recordatorio.getUsuario() == null || recordatorio.getUsuario().getId() == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        
        User usuario = userRepository.findById(recordatorio.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        recordatorio.setUsuario(usuario);
        recordatorio.setActivo(true);
        recordatorio.setCompletado(false);
        
        return recordatorioRepository.save(recordatorio);
    }

    /**
     * Actualizar un recordatorio existente
     */
    public Recordatorio actualizarRecordatorio(Long id, Recordatorio recordatorioActualizado) {
        Recordatorio recordatorio = recordatorioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recordatorio no encontrado"));
        
        // Verificar que el recordatorio pertenece al usuario
        if (!recordatorio.getUsuario().getId().equals(recordatorioActualizado.getUsuario().getId())) {
            throw new IllegalArgumentException("No tiene permiso para modificar este recordatorio");
        }
        
        recordatorio.setTitulo(recordatorioActualizado.getTitulo());
        recordatorio.setDescripcion(recordatorioActualizado.getDescripcion());
        recordatorio.setFecha(recordatorioActualizado.getFecha());
        recordatorio.setTipo(recordatorioActualizado.getTipo());
        recordatorio.setLaborId(recordatorioActualizado.getLaborId());
        recordatorio.setLoteId(recordatorioActualizado.getLoteId());
        recordatorio.setServicioId(recordatorioActualizado.getServicioId());
        recordatorio.setGestacionId(recordatorioActualizado.getGestacionId());
        recordatorio.setPartoId(recordatorioActualizado.getPartoId());
        recordatorio.setMadreId(recordatorioActualizado.getMadreId());
        recordatorio.setLoteAvicolaHuevoId(recordatorioActualizado.getLoteAvicolaHuevoId());
        recordatorio.setCompletado(recordatorioActualizado.getCompletado());
        
        return recordatorioRepository.save(recordatorio);
    }

    /**
     * Marcar un recordatorio como completado
     */
    public Recordatorio marcarCompletado(Long id, Long usuarioId) {
        Recordatorio recordatorio = recordatorioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recordatorio no encontrado"));
        
        if (!recordatorio.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tiene permiso para modificar este recordatorio");
        }
        
        recordatorio.setCompletado(true);
        return recordatorioRepository.save(recordatorio);
    }

    /**
     * Eliminar (desactivar) un recordatorio
     */
    public void eliminarRecordatorio(Long id, Long usuarioId) {
        Recordatorio recordatorio = recordatorioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recordatorio no encontrado"));
        
        if (!recordatorio.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tiene permiso para eliminar este recordatorio");
        }
        
        recordatorio.setActivo(false);
        recordatorioRepository.save(recordatorio);
    }

    /**
     * Obtener recordatorio por ID
     */
    public Optional<Recordatorio> getRecordatorioById(Long id) {
        return recordatorioRepository.findById(id);
    }
}

