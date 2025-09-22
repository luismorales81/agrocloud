package com.agrocloud.service;

import com.agrocloud.model.dto.CosechaDTO;
import com.agrocloud.model.dto.ComparacionRendimientoDTO;
import com.agrocloud.model.entity.Cosecha;
import com.agrocloud.model.entity.Cultivo;
import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.CosechaRepository;
import com.agrocloud.repository.CultivoRepository;
import com.agrocloud.repository.PlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de cosechas
 */
@Service
@Transactional
public class CosechaService {

    private static final Logger logger = LoggerFactory.getLogger(CosechaService.class);

    @Autowired
    private CosechaRepository cosechaRepository;

    @Autowired
    private CultivoRepository cultivoRepository;

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private HistorialCosechaService historialCosechaService;

    /**
     * Crear una nueva cosecha
     */
    public CosechaDTO crearCosecha(CosechaDTO cosechaDTO, User usuario) {
        logger.info("Creando nueva cosecha para cultivo ID: {}", cosechaDTO.getCultivoId());

        // Verificar que el cultivo existe y pertenece al usuario
        Cultivo cultivo = cultivoRepository.findById(cosechaDTO.getCultivoId())
                .orElseThrow(() -> new RuntimeException("Cultivo no encontrado"));

        if (!cultivo.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tiene permisos para agregar cosechas a este cultivo");
        }

        // Buscar el lote
        Plot lote = plotRepository.findById(cosechaDTO.getLoteId())
                .orElseThrow(() -> new RuntimeException("Lote no encontrado"));

        // Crear la entidad Cosecha
        Cosecha cosecha = new Cosecha();
        cosecha.setCultivo(cultivo);
        cosecha.setLote(lote);
        cosecha.setFecha(cosechaDTO.getFecha());
        cosecha.setCantidadToneladas(cosechaDTO.getCantidadToneladas());
        cosecha.setPrecioPorTonelada(cosechaDTO.getPrecioPorTonelada());
        cosecha.setCostoTotal(cosechaDTO.getCostoTotal());
        cosecha.setObservaciones(cosechaDTO.getObservaciones());
        cosecha.setUsuario(usuario);

        // Guardar la cosecha
        Cosecha cosechaGuardada = cosechaRepository.save(cosecha);
        logger.info("Cosecha creada exitosamente con ID: {}", cosechaGuardada.getId());

        // Crear registro en el historial de cosechas
        try {
            historialCosechaService.crearHistorialCosecha(cosechaGuardada, usuario);
            logger.info("Historial de cosecha creado para cosecha ID: {}", cosechaGuardada.getId());
        } catch (Exception e) {
            logger.error("Error creando historial de cosecha: {}", e.getMessage());
            // No lanzar excepción para no afectar la creación de la cosecha
        }

        return convertirADTO(cosechaGuardada);
    }

    /**
     * Obtener todas las cosechas de un cultivo
     */
    @Transactional(readOnly = true)
    public List<CosechaDTO> obtenerCosechasPorCultivo(Long cultivoId, User usuario) {
        logger.info("Obteniendo cosechas para cultivo ID: {}", cultivoId);

        // Verificar que el cultivo existe y pertenece al usuario
        Cultivo cultivo = cultivoRepository.findById(cultivoId)
                .orElseThrow(() -> new RuntimeException("Cultivo no encontrado"));

        if (!cultivo.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tiene permisos para ver las cosechas de este cultivo");
        }

        List<Cosecha> cosechas = cosechaRepository.findByCultivoIdOrderByFechaDesc(cultivoId);
        return cosechas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todas las cosechas del usuario
     */
    @Transactional(readOnly = true)
    public List<CosechaDTO> obtenerCosechasPorUsuario(User usuario) {
        logger.info("Obteniendo todas las cosechas para usuario ID: {}", usuario.getId());
        logger.info("Usuario email: {}", usuario.getEmail());

        List<Cosecha> cosechas;
        
        // Si es administrador, obtener todas las cosechas
        if (esAdmin(usuario)) {
            logger.info("Usuario es ADMIN - obteniendo todas las cosechas");
            cosechas = cosechaRepository.findAll();
        } else {
            // Si no es admin, obtener solo las cosechas del usuario
            cosechas = cosechaRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId());
        }
        
        logger.info("Cosechas encontradas en BD: {}", cosechas.size());
        
        for (Cosecha cosecha : cosechas) {
            logger.info("Cosecha ID: {}, Cultivo ID: {}, Fecha: {}, Usuario ID: {}", 
                cosecha.getId(), cosecha.getCultivo().getId(), cosecha.getFecha(), cosecha.getUsuario().getId());
        }
        
        List<CosechaDTO> cosechasDTO = cosechas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
                
        logger.info("Cosechas DTO convertidas: {}", cosechasDTO.size());
        return cosechasDTO;
    }

    /**
     * Obtener cosecha por ID
     */
    @Transactional(readOnly = true)
    public Optional<CosechaDTO> obtenerCosechaPorId(Long id, User usuario) {
        logger.info("Obteniendo cosecha ID: {}", id);

        Optional<Cosecha> cosecha = cosechaRepository.findById(id);
        if (cosecha.isPresent() && cosecha.get().getUsuario().getId().equals(usuario.getId())) {
            return Optional.of(convertirADTO(cosecha.get()));
        }
        return Optional.empty();
    }

    /**
     * Actualizar una cosecha existente
     */
    public CosechaDTO actualizarCosecha(Long id, CosechaDTO cosechaDTO, User usuario) {
        logger.info("Actualizando cosecha ID: {}", id);

        Cosecha cosecha = cosechaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cosecha no encontrada"));

        if (!cosecha.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tiene permisos para modificar esta cosecha");
        }

        // Actualizar campos
        cosecha.setFecha(cosechaDTO.getFecha());
        cosecha.setCantidadToneladas(cosechaDTO.getCantidadToneladas());
        cosecha.setPrecioPorTonelada(cosechaDTO.getPrecioPorTonelada());
        cosecha.setCostoTotal(cosechaDTO.getCostoTotal());
        cosecha.setObservaciones(cosechaDTO.getObservaciones());

        Cosecha cosechaActualizada = cosechaRepository.save(cosecha);
        logger.info("Cosecha actualizada exitosamente");

        return convertirADTO(cosechaActualizada);
    }

    /**
     * Eliminar una cosecha
     */
    public void eliminarCosecha(Long id, User usuario) {
        logger.info("Eliminando cosecha ID: {}", id);

        Cosecha cosecha = cosechaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cosecha no encontrada"));

        if (!cosecha.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tiene permisos para eliminar esta cosecha");
        }

        cosechaRepository.delete(cosecha);
        logger.info("Cosecha eliminada exitosamente");
    }

    /**
     * Obtener comparación de rendimientos para un cultivo
     */
    @Transactional(readOnly = true)
    public ComparacionRendimientoDTO obtenerComparacionRendimiento(Long cultivoId, User usuario) {
        logger.info("Obteniendo comparación de rendimientos para cultivo ID: {}", cultivoId);

        // Verificar que el cultivo existe y pertenece al usuario
        Cultivo cultivo = cultivoRepository.findById(cultivoId)
                .orElseThrow(() -> new RuntimeException("Cultivo no encontrado"));

        if (!cultivo.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tiene permisos para ver este cultivo");
        }

        // Crear DTO de comparación
        ComparacionRendimientoDTO comparacion = new ComparacionRendimientoDTO(
                cultivo.getId(),
                cultivo.getNombre(),
                cultivo.getVariedad(),
                cultivo.getRendimientoEsperado(),
                cultivo.getUnidadRendimiento()
        );

        // Obtener cosechas del cultivo
        List<Cosecha> cosechas = cosechaRepository.findByCultivoId(cultivoId);
        List<CosechaDTO> cosechasDTO = cosechas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());

        comparacion.setCosechas(cosechasDTO);
        comparacion.setTotalCosechas(cosechas.size());

        if (!cosechas.isEmpty()) {
            // Calcular estadísticas
            BigDecimal rendimientoPromedio = cultivo.getRendimientoRealPromedio();
            BigDecimal diferenciaAbsoluta = cultivo.getDiferenciaRendimiento();
            BigDecimal diferenciaPorcentual = cultivo.getPorcentajeDiferencia();

            // Calcular superficie total y rendimiento total
            BigDecimal superficieTotal = cosechas.stream()
                    .map(Cosecha::getCantidadToneladas)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal rendimientoTotal = cosechas.stream()
                    .map(Cosecha::getRendimientoTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            comparacion.setRendimientoRealPromedio(rendimientoPromedio);
            comparacion.setDiferenciaAbsoluta(diferenciaAbsoluta);
            comparacion.setDiferenciaPorcentual(diferenciaPorcentual);
            comparacion.setSuperficieTotalCosechada(superficieTotal);
            comparacion.setRendimientoTotalObtenido(rendimientoTotal);
        }

        return comparacion;
    }

    /**
     * Obtener estadísticas de cosechas por usuario
     */
    @Transactional(readOnly = true)
    public Object[] obtenerEstadisticasPorUsuario(User usuario) {
        logger.info("Obteniendo estadísticas de cosechas para usuario ID: {}", usuario.getId());
        return cosechaRepository.obtenerEstadisticasPorUsuario(usuario.getId());
    }

    /**
     * Obtener cosechas por rango de fechas
     */
    @Transactional(readOnly = true)
    public List<CosechaDTO> obtenerCosechasPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin, User usuario) {
        logger.info("Obteniendo cosechas entre {} y {} para usuario ID: {}", fechaInicio, fechaFin, usuario.getId());

        List<Cosecha> cosechas = cosechaRepository.findByUsuarioIdAndFechaBetween(usuario.getId(), fechaInicio, fechaFin);
        return cosechas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Convertir entidad Cosecha a DTO
     */
    private CosechaDTO convertirADTO(Cosecha cosecha) {
        CosechaDTO dto = new CosechaDTO();
        dto.setId(cosecha.getId());
        dto.setCultivoId(cosecha.getCultivo().getId());
        dto.setCultivoNombre(cosecha.getCultivo().getNombre());
        dto.setFecha(cosecha.getFecha());
        dto.setLoteId(cosecha.getLote() != null ? cosecha.getLote().getId() : null);
        dto.setLoteNombre(cosecha.getLote() != null ? cosecha.getLote().getNombre() : null);
        dto.setCantidadToneladas(cosecha.getCantidadToneladas());
        dto.setPrecioPorTonelada(cosecha.getPrecioPorTonelada());
        dto.setCostoTotal(cosecha.getCostoTotal());
        dto.setObservaciones(cosecha.getObservaciones());
        dto.setUsuarioId(cosecha.getUsuario().getId());
        dto.setUsuarioNombre(cosecha.getUsuario().getFirstName() + " " + cosecha.getUsuario().getLastName());
        dto.setCreatedAt(cosecha.getCreatedAt());
        dto.setUpdatedAt(cosecha.getUpdatedAt());

        // La empresa se obtiene a través del usuario
        if (cosecha.getUsuario() != null && !cosecha.getUsuario().getUserCompanyRoles().isEmpty()) {
            Empresa empresa = cosecha.getUsuario().getUserCompanyRoles().get(0).getEmpresa();
            dto.setEmpresaId(empresa.getId());
            dto.setEmpresaNombre(empresa.getNombre());
        }

        return dto;
    }

    /**
     * Verificar si un usuario es administrador
     */
    private boolean esAdmin(User usuario) {
        if (usuario == null) {
            logger.warn("Usuario es null");
            return false;
        }
        
        if (usuario.getRoles() == null) {
            logger.warn("Roles del usuario son null para: {}", usuario.getUsername());
            return false;
        }
        
        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getNombre()) || "ADMINISTRADOR".equals(role.getNombre()) || "SUPERADMIN".equals(role.getNombre()));
        
        logger.info("Usuario {} es admin: {}", usuario.getUsername(), esAdmin);
        
        return esAdmin;
    }
}
