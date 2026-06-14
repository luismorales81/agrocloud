package com.agrocloud.cultivos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.cultivos.domain.EstadoLoteConfig;
import com.agrocloud.cultivos.domain.TareaPorEstadoConfig;
import com.agrocloud.cultivos.domain.TipoCultivo;
import com.agrocloud.cultivos.domain.TransicionEstadoConfig;

import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.cultivos.infrastructure.EstadoLoteConfigRepository;
import com.agrocloud.cultivos.infrastructure.TareaPorEstadoConfigRepository;
import com.agrocloud.cultivos.infrastructure.TipoCultivoRepository;
import com.agrocloud.cultivos.infrastructure.TransicionEstadoConfigRepository;
import com.agrocloud.cultivos.infrastructure.LaborRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Servicio para gestionar la configuración de estados y tareas por tipo de cultivo
 *
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Service("configuracionEstadosServiceCultivos")
@Transactional
public class ConfiguracionEstadosService {

    @Autowired
    private TipoCultivoRepository tipoCultivoRepository;

    @Autowired
    private EstadoLoteConfigRepository estadoLoteConfigRepository;

    @Autowired
    private TransicionEstadoConfigRepository transicionEstadoConfigRepository;

    @Autowired
    private TareaPorEstadoConfigRepository tareaPorEstadoConfigRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private LaborRepository laborRepository;

    // ============================================================================
    // TIPOS DE CULTIVO
    // ============================================================================

    /**
     * Obtener todos los tipos de cultivo (plantillas y personalizaciones)
     */
    @Transactional(readOnly = true)
    public List<TipoCultivo> obtenerTodosLosTiposCultivo() {
        return tipoCultivoRepository.findByActivoTrue();
    }

    /**
     * Obtener solo las plantillas globales
     */
    @Transactional(readOnly = true)
    public List<TipoCultivo> obtenerPlantillas() {
        return tipoCultivoRepository.findByEsPlantillaTrueAndActivoTrue();
    }

    /**
     * Obtener un tipo de cultivo por ID
     */
    @Transactional(readOnly = true)
    public Optional<TipoCultivo> obtenerTipoCultivoPorId(Long id) {
        return tipoCultivoRepository.findById(id);
    }

    /**
     * Crear un nuevo tipo de cultivo
     */
    @Transactional
    public TipoCultivo crearTipoCultivo(TipoCultivo tipoCultivo) {
        // Verificar que no exista otro con el mismo nombre
        if (tipoCultivoRepository.findByNombre(tipoCultivo.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un tipo de cultivo con el nombre: " + tipoCultivo.getNombre());
        }
        return tipoCultivoRepository.save(tipoCultivo);
    }

    /**
     * Actualizar un tipo de cultivo
     */
    @Transactional
    public Optional<TipoCultivo> actualizarTipoCultivo(Long id, TipoCultivo tipoCultivoData) {
        Optional<TipoCultivo> tipoCultivoOpt = tipoCultivoRepository.findById(id);
        if (tipoCultivoOpt.isPresent()) {
            TipoCultivo tipoCultivo = tipoCultivoOpt.get();

            // Verificar nombre único si cambió
            if (!tipoCultivo.getNombre().equals(tipoCultivoData.getNombre())) {
                if (tipoCultivoRepository.findByNombre(tipoCultivoData.getNombre()).isPresent()) {
                    throw new IllegalArgumentException("Ya existe un tipo de cultivo con el nombre: " + tipoCultivoData.getNombre());
                }
            }

            tipoCultivo.setNombre(tipoCultivoData.getNombre());
            tipoCultivo.setDescripcion(tipoCultivoData.getDescripcion());
            return Optional.of(tipoCultivoRepository.save(tipoCultivo));
        }
        return Optional.empty();
    }

    /**
     * Eliminar un tipo de cultivo (lógicamente)
     */
    @Transactional
    public boolean eliminarTipoCultivo(Long id) {
        Optional<TipoCultivo> tipoCultivoOpt = tipoCultivoRepository.findById(id);
        if (tipoCultivoOpt.isPresent()) {
            TipoCultivo tipoCultivo = tipoCultivoOpt.get();
            // No permitir eliminar plantillas
            if (tipoCultivo.getEsPlantilla()) {
                throw new IllegalArgumentException("No se pueden eliminar plantillas globales");
            }
            tipoCultivo.setActivo(false);
            tipoCultivoRepository.save(tipoCultivo);
            return true;
        }
        return false;
    }

    // ============================================================================
    // ESTADOS
    // ============================================================================

    /**
     * Obtener estados para un tipo de cultivo (resuelve plantilla o personalización)
     */
    @Transactional(readOnly = true)
    public List<EstadoLoteConfig> obtenerEstadosPorTipoCultivo(Long tipoCultivoId, Long empresaId) {
        // Primero intentar obtener personalización de la empresa
        if (empresaId != null) {
            List<EstadoLoteConfig> personalizados = estadoLoteConfigRepository
                .findByTipoCultivoIdAndEmpresaIdAndActivoTrueOrderByOrdenAsc(tipoCultivoId, empresaId);
            if (!personalizados.isEmpty()) {
                return personalizados;
            }
        }

        // Si no hay personalización, usar plantilla global
        return estadoLoteConfigRepository.findPlantillasByTipoCultivoId(tipoCultivoId);
    }

    /**
     * Obtener un estado por ID
     */
    @Transactional(readOnly = true)
    public Optional<EstadoLoteConfig> obtenerEstadoPorId(Long id) {
        return estadoLoteConfigRepository.findById(id);
    }

    /**
     * Crear un nuevo estado
     */
    @Transactional
    public EstadoLoteConfig crearEstado(EstadoLoteConfig estado, Long tipoCultivoId, Long empresaId) {
        // Validar que no exista otro estado con el mismo nombre para este tipo de cultivo y empresa
        Optional<EstadoLoteConfig> existente = estadoLoteConfigRepository
            .findByNombreAndTipoCultivoIdAndEmpresaId(estado.getNombre(), tipoCultivoId, empresaId);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un estado con el nombre: " + estado.getNombre());
        }

        // Asignar relaciones
        if (tipoCultivoId != null) {
            TipoCultivo tipoCultivo = tipoCultivoRepository.findById(tipoCultivoId)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de cultivo no encontrado"));
            estado.setTipoCultivo(tipoCultivo);
        }

        if (empresaId != null) {
            Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
            estado.setEmpresa(empresa);
        }

        return estadoLoteConfigRepository.save(estado);
    }

    /**
     * Actualizar un estado
     */
    @Transactional
    public Optional<EstadoLoteConfig> actualizarEstado(Long id, EstadoLoteConfig estadoData) {
        Optional<EstadoLoteConfig> estadoOpt = estadoLoteConfigRepository.findById(id);
        if (estadoOpt.isPresent()) {
            EstadoLoteConfig estado = estadoOpt.get();

            // Validar nombre único si cambió
            if (!estado.getNombre().equals(estadoData.getNombre())) {
                Optional<EstadoLoteConfig> existente = estadoLoteConfigRepository
                    .findByNombreAndTipoCultivoIdAndEmpresaId(
                        estadoData.getNombre(),
                        estado.getTipoCultivoId(),
                        estado.getEmpresaId()
                    );
                if (existente.isPresent() && !existente.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Ya existe un estado con el nombre: " + estadoData.getNombre());
                }
            }

            estado.setNombre(estadoData.getNombre());
            estado.setDescripcion(estadoData.getDescripcion());
            estado.setColor(estadoData.getColor());
            estado.setIcono(estadoData.getIcono());
            estado.setEsEstadoInicial(estadoData.getEsEstadoInicial());
            estado.setEsEstadoFinal(estadoData.getEsEstadoFinal());

            return Optional.of(estadoLoteConfigRepository.save(estado));
        }
        return Optional.empty();
    }

    /**
     * Reordenar estados
     */
    @Transactional
    public void reordenarEstados(List<Long> estadoIds) {
        for (int i = 0; i < estadoIds.size(); i++) {
            Optional<EstadoLoteConfig> estadoOpt = estadoLoteConfigRepository.findById(estadoIds.get(i));
            if (estadoOpt.isPresent()) {
                EstadoLoteConfig estado = estadoOpt.get();
                estado.setOrden(i + 1);
                estadoLoteConfigRepository.save(estado);
            }
        }
    }

    /**
     * Eliminar un estado (lógicamente)
     */
    @Transactional
    public boolean eliminarEstado(Long id) {
        Optional<EstadoLoteConfig> estadoOpt = estadoLoteConfigRepository.findById(id);
        if (estadoOpt.isPresent()) {
            EstadoLoteConfig estado = estadoOpt.get();

            // Verificar si está en uso
            if (estadoLoteConfigRepository.isEstadoEnUso(id)) {
                throw new IllegalArgumentException("No se puede eliminar un estado que está en uso");
            }

            estado.setActivo(false);
            estadoLoteConfigRepository.save(estado);
            return true;
        }
        return false;
    }

    /**
     * Copiar plantilla a personalización de empresa
     */
    @Transactional
    public void copiarPlantillaAEmpresa(Long tipoCultivoId, Long empresaId) {
        // Obtener plantilla
        List<EstadoLoteConfig> plantilla = estadoLoteConfigRepository.findPlantillasByTipoCultivoId(tipoCultivoId);

        if (plantilla.isEmpty()) {
            throw new IllegalArgumentException("No existe plantilla para el tipo de cultivo especificado");
        }

        TipoCultivo tipoCultivo = tipoCultivoRepository.findById(tipoCultivoId)
            .orElseThrow(() -> new IllegalArgumentException("Tipo de cultivo no encontrado"));

        Empresa empresa = empresaRepository.findById(empresaId)
            .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        // Verificar si ya existe personalización
        List<EstadoLoteConfig> existentes = estadoLoteConfigRepository
            .findByTipoCultivoIdAndEmpresaIdAndActivoTrueOrderByOrdenAsc(tipoCultivoId, empresaId);
        if (!existentes.isEmpty()) {
            throw new IllegalArgumentException("Ya existe una personalización para este tipo de cultivo y empresa");
        }

        // Copiar estados
        Map<Long, EstadoLoteConfig> estadosCreados = new HashMap<>();
        for (EstadoLoteConfig estadoPlantilla : plantilla) {
            EstadoLoteConfig nuevoEstado = new EstadoLoteConfig();
            nuevoEstado.setTipoCultivo(tipoCultivo);
            nuevoEstado.setEmpresa(empresa);
            nuevoEstado.setNombre(estadoPlantilla.getNombre());
            nuevoEstado.setDescripcion(estadoPlantilla.getDescripcion());
            nuevoEstado.setColor(estadoPlantilla.getColor());
            nuevoEstado.setIcono(estadoPlantilla.getIcono());
            nuevoEstado.setOrden(estadoPlantilla.getOrden());
            nuevoEstado.setEsEstadoInicial(estadoPlantilla.getEsEstadoInicial());
            nuevoEstado.setEsEstadoFinal(estadoPlantilla.getEsEstadoFinal());
            nuevoEstado.setActivo(true);

            EstadoLoteConfig estadoGuardado = estadoLoteConfigRepository.save(nuevoEstado);
            estadosCreados.put(estadoPlantilla.getId(), estadoGuardado);
        }

        // Copiar transiciones
        List<TransicionEstadoConfig> transicionesPlantilla = transicionEstadoConfigRepository
            .findPlantillasByTipoCultivoId(tipoCultivoId);

        for (TransicionEstadoConfig transicionPlantilla : transicionesPlantilla) {
            EstadoLoteConfig origenNuevo = estadosCreados.get(transicionPlantilla.getEstadoOrigen().getId());
            EstadoLoteConfig destinoNuevo = estadosCreados.get(transicionPlantilla.getEstadoDestino().getId());

            if (origenNuevo != null && destinoNuevo != null) {
                TransicionEstadoConfig nuevaTransicion = new TransicionEstadoConfig();
                nuevaTransicion.setTipoCultivo(tipoCultivo);
                nuevaTransicion.setEmpresa(empresa);
                nuevaTransicion.setEstadoOrigen(origenNuevo);
                nuevaTransicion.setEstadoDestino(destinoNuevo);
                nuevaTransicion.setRequiereMotivo(transicionPlantilla.getRequiereMotivo());
                nuevaTransicion.setActivo(true);
                transicionEstadoConfigRepository.save(nuevaTransicion);
            }
        }

        // Copiar tareas
        for (EstadoLoteConfig estadoPlantilla : plantilla) {
            List<TareaPorEstadoConfig> tareasPlantilla = tareaPorEstadoConfigRepository
                .findByEstadoIdAndActivoTrueOrderByOrdenAsc(estadoPlantilla.getId());

            EstadoLoteConfig estadoNuevo = estadosCreados.get(estadoPlantilla.getId());

            for (TareaPorEstadoConfig tareaPlantilla : tareasPlantilla) {
                TareaPorEstadoConfig nuevaTarea = new TareaPorEstadoConfig();
                nuevaTarea.setTipoCultivo(tipoCultivo);
                nuevaTarea.setEmpresa(empresa);
                nuevaTarea.setEstado(estadoNuevo);
                nuevaTarea.setTipoLabor(tareaPlantilla.getTipoLabor());
                nuevaTarea.setNombreTarea(tareaPlantilla.getNombreTarea());
                nuevaTarea.setDescripcion(tareaPlantilla.getDescripcion());
                nuevaTarea.setEsObligatoria(tareaPlantilla.getEsObligatoria());
                nuevaTarea.setOrden(tareaPlantilla.getOrden());
                nuevaTarea.setActivo(true);
                tareaPorEstadoConfigRepository.save(nuevaTarea);
            }
        }
    }

    // ============================================================================
    // TRANSICIONES
    // ============================================================================

    /**
     * Obtener transiciones para un tipo de cultivo (resuelve plantilla o personalización)
     */
    @Transactional(readOnly = true)
    public List<TransicionEstadoConfig> obtenerTransicionesPorTipoCultivo(Long tipoCultivoId, Long empresaId) {
        if (empresaId != null) {
            List<TransicionEstadoConfig> personalizadas = transicionEstadoConfigRepository
                .findByTipoCultivoIdAndEmpresaIdAndActivoTrue(tipoCultivoId, empresaId);
            if (!personalizadas.isEmpty()) {
                return personalizadas;
            }
        }
        return transicionEstadoConfigRepository.findPlantillasByTipoCultivoId(tipoCultivoId);
    }

    /**
     * Obtener transiciones válidas desde un estado
     */
    @Transactional(readOnly = true)
    public List<TransicionEstadoConfig> obtenerTransicionesValidasDesdeEstado(Long estadoOrigenId, Long empresaId) {
        return transicionEstadoConfigRepository.findTransicionesValidasDesdeEstado(estadoOrigenId, empresaId);
    }

    /**
     * Crear una nueva transición
     */
    @Transactional
    public TransicionEstadoConfig crearTransicion(TransicionEstadoConfig transicion, Long tipoCultivoId, Long empresaId) {
        // Obtener los IDs de los estados
        Long estadoOrigenId = transicion.getEstadoOrigen() != null
            ? transicion.getEstadoOrigen().getId()
            : transicion.getEstadoOrigenId();
        Long estadoDestinoId = transicion.getEstadoDestino() != null
            ? transicion.getEstadoDestino().getId()
            : transicion.getEstadoDestinoId();

        if (estadoOrigenId == null || estadoDestinoId == null) {
            throw new IllegalArgumentException("Los estados origen y destino son obligatorios");
        }

        // Validar que no exista ya esta transición
        Optional<TransicionEstadoConfig> existente = transicionEstadoConfigRepository
            .findByEstadoOrigenIdAndEstadoDestinoIdAndEmpresaId(
                estadoOrigenId,
                estadoDestinoId,
                empresaId
            );
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe esta transición");
        }

        // Validar que no sea una transición circular (estado origen = estado destino)
        if (estadoOrigenId.equals(estadoDestinoId)) {
            throw new IllegalArgumentException("No se puede crear una transición de un estado a sí mismo");
        }

        // Cargar las entidades de estado
        EstadoLoteConfig estadoOrigen = estadoLoteConfigRepository.findById(estadoOrigenId)
            .orElseThrow(() -> new IllegalArgumentException("Estado origen no encontrado"));
        EstadoLoteConfig estadoDestino = estadoLoteConfigRepository.findById(estadoDestinoId)
            .orElseThrow(() -> new IllegalArgumentException("Estado destino no encontrado"));

        // Asignar relaciones
        if (tipoCultivoId != null) {
            TipoCultivo tipoCultivo = tipoCultivoRepository.findById(tipoCultivoId)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de cultivo no encontrado"));
            transicion.setTipoCultivo(tipoCultivo);
        }

        if (empresaId != null) {
            Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
            transicion.setEmpresa(empresa);
        }

        transicion.setEstadoOrigen(estadoOrigen);
        transicion.setEstadoDestino(estadoDestino);

        return transicionEstadoConfigRepository.save(transicion);
    }

    /**
     * Eliminar una transición
     */
    @Transactional
    public boolean eliminarTransicion(Long id) {
        Optional<TransicionEstadoConfig> transicionOpt = transicionEstadoConfigRepository.findById(id);
        if (transicionOpt.isPresent()) {
            TransicionEstadoConfig transicion = transicionOpt.get();
            transicion.setActivo(false);
            transicionEstadoConfigRepository.save(transicion);
            return true;
        }
        return false;
    }

    // ============================================================================
    // TAREAS
    // ============================================================================

    /**
     * Obtener tareas disponibles para un estado (resuelve plantilla o personalización)
     */
    @Transactional(readOnly = true)
    public List<TareaPorEstadoConfig> obtenerTareasPorEstado(Long estadoId, Long empresaId) {
        return tareaPorEstadoConfigRepository.findTareasDisponiblesPorEstado(estadoId, empresaId);
    }

    /**
     * Crear una nueva tarea
     */
    @Transactional
    public TareaPorEstadoConfig crearTarea(TareaPorEstadoConfig tarea, Long tipoCultivoId, Long empresaId) {
        // Obtener el ID del estado
        Long estadoId = tarea.getEstado() != null
            ? tarea.getEstado().getId()
            : tarea.getEstadoId();

        if (estadoId == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }

        // Validar que no exista ya esta tarea para este estado
        Optional<TareaPorEstadoConfig> existente = tareaPorEstadoConfigRepository
            .findByEstadoIdAndTipoLaborAndActivoTrue(estadoId, tarea.getTipoLabor());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe una tarea de este tipo para este estado");
        }

        // Cargar la entidad de estado
        EstadoLoteConfig estado = estadoLoteConfigRepository.findById(estadoId)
            .orElseThrow(() -> new IllegalArgumentException("Estado no encontrado"));

        // Asignar relaciones
        if (tipoCultivoId != null) {
            TipoCultivo tipoCultivo = tipoCultivoRepository.findById(tipoCultivoId)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de cultivo no encontrado"));
            tarea.setTipoCultivo(tipoCultivo);
        }

        if (empresaId != null) {
            Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
            tarea.setEmpresa(empresa);
        }

        tarea.setEstado(estado);

        return tareaPorEstadoConfigRepository.save(tarea);
    }

    /**
     * Actualizar una tarea
     */
    @Transactional
    public Optional<TareaPorEstadoConfig> actualizarTarea(Long id, TareaPorEstadoConfig tareaData) {
        Optional<TareaPorEstadoConfig> tareaOpt = tareaPorEstadoConfigRepository.findById(id);
        if (tareaOpt.isPresent()) {
            TareaPorEstadoConfig tarea = tareaOpt.get();

            // Validar tipo de labor único si cambió
            if (!tarea.getTipoLabor().equals(tareaData.getTipoLabor())) {
                Optional<TareaPorEstadoConfig> existente = tareaPorEstadoConfigRepository
                    .findByEstadoIdAndTipoLaborAndActivoTrue(tarea.getEstadoId(), tareaData.getTipoLabor());
                if (existente.isPresent() && !existente.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Ya existe una tarea de este tipo para este estado");
                }
            }

            tarea.setTipoLabor(tareaData.getTipoLabor());
            tarea.setNombreTarea(tareaData.getNombreTarea());
            tarea.setDescripcion(tareaData.getDescripcion());
            tarea.setEsObligatoria(tareaData.getEsObligatoria());
            tarea.setOrden(tareaData.getOrden());

            return Optional.of(tareaPorEstadoConfigRepository.save(tarea));
        }
        return Optional.empty();
    }

    /**
     * Reordenar tareas de un estado
     */
    @Transactional
    public void reordenarTareas(Long estadoId, List<Long> tareaIds) {
        for (int i = 0; i < tareaIds.size(); i++) {
            Optional<TareaPorEstadoConfig> tareaOpt = tareaPorEstadoConfigRepository.findById(tareaIds.get(i));
            if (tareaOpt.isPresent()) {
                TareaPorEstadoConfig tarea = tareaOpt.get();
                if (tarea.getEstadoId().equals(estadoId)) {
                    tarea.setOrden(i + 1);
                    tareaPorEstadoConfigRepository.save(tarea);
                }
            }
        }
    }

    /**
     * Eliminar una tarea con validación de labores asociadas
     *
     * @param id ID de la tarea a eliminar
     * @return Respuesta con información sobre la eliminación y labores afectadas
     */
    @Transactional
    public Map<String, Object> eliminarTareaConValidacion(Long id) {
        Optional<TareaPorEstadoConfig> tareaOpt = tareaPorEstadoConfigRepository.findById(id);
        if (tareaOpt.isEmpty()) {
            return Map.of(
                "eliminada", false,
                "mensaje", "Tarea no encontrada"
            );
        }

        TareaPorEstadoConfig tarea = tareaOpt.get();
        Map<String, Object> respuesta = new HashMap<>();

        // Convertir el tipo de labor de String a enum
        try {
            Labor.TipoLabor tipoLaborEnum = Labor.TipoLabor.valueOf(tarea.getTipoLabor());

            // Contar labores asociadas
            Long totalLabores = laborRepository.countByTipoLaborAndActivoTrue(tipoLaborEnum);
            Long laboresPlanificadas = laborRepository.countByTipoLaborAndEstadoAndActivoTrue(
                tipoLaborEnum, Labor.EstadoLabor.PLANIFICADA);
            Long laboresEnProgreso = laborRepository.countByTipoLaborAndEstadoAndActivoTrue(
                tipoLaborEnum, Labor.EstadoLabor.EN_PROGRESO);
            Long laboresCompletadas = laborRepository.countByTipoLaborAndEstadoAndActivoTrue(
                tipoLaborEnum, Labor.EstadoLabor.COMPLETADA);

            respuesta.put("tieneLaboresAsociadas", totalLabores > 0);
            respuesta.put("totalLabores", totalLabores);
            respuesta.put("laboresPlanificadas", laboresPlanificadas);
            respuesta.put("laboresEnProgreso", laboresEnProgreso);
            respuesta.put("laboresCompletadas", laboresCompletadas);

            // Generar advertencia si hay labores asociadas
            if (totalLabores > 0) {
                StringBuilder advertencia = new StringBuilder();
                advertencia.append("Esta tarea tiene ").append(totalLabores).append(" labor(es) asociada(s): ");

                if (laboresCompletadas > 0) {
                    advertencia.append(laboresCompletadas).append(" completada(s), ");
                }
                if (laboresEnProgreso > 0) {
                    advertencia.append(laboresEnProgreso).append(" en progreso, ");
                }
                if (laboresPlanificadas > 0) {
                    advertencia.append(laboresPlanificadas).append(" planificada(s). ");
                }

                advertencia.append("Las labores existentes NO se eliminarán, solo se desactivará esta tarea del plan. ");
                advertencia.append("Esto significa que no podrá crear nuevas labores de este tipo para este estado, ");
                advertencia.append("pero las labores históricas se mantendrán intactas.");

                respuesta.put("advertencia", advertencia.toString());
            }
        } catch (IllegalArgumentException e) {
            // Si no se puede convertir el tipo de labor, asumir que no hay labores asociadas
            respuesta.put("tieneLaboresAsociadas", false);
            respuesta.put("totalLabores", 0L);
            respuesta.put("laboresPlanificadas", 0L);
            respuesta.put("laboresEnProgreso", 0L);
            respuesta.put("laboresCompletadas", 0L);
        }

        // Realizar eliminación lógica (SEGURA - no afecta labores existentes)
        tarea.setActivo(false);
        tareaPorEstadoConfigRepository.save(tarea);

        respuesta.put("eliminada", true);
        if (respuesta.get("tieneLaboresAsociadas") != null &&
            (Boolean) respuesta.get("tieneLaboresAsociadas")) {
            respuesta.put("mensaje", "Tarea eliminada exitosamente (eliminación lógica). Ver advertencia para más detalles.");
        } else {
            respuesta.put("mensaje", "Tarea eliminada exitosamente (eliminación lógica).");
        }

        return respuesta;
    }

    /**
     * Eliminar una tarea (método legacy - mantiene compatibilidad)
     */
    @Transactional
    public boolean eliminarTarea(Long id) {
        Map<String, Object> respuesta = eliminarTareaConValidacion(id);
        return (Boolean) respuesta.get("eliminada");
    }

    // ============================================================================
    // MÉTODOS DE UTILIDAD
    // ============================================================================

    /**
     * Obtener la empresa del usuario actual
     */
    @Transactional(readOnly = true)
    public Optional<Long> obtenerEmpresaIdDelUsuario(User user) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        return empresa.map(Empresa::getId);
    }

    /**
     * Validar si una transición es válida
     */
    @Transactional(readOnly = true)
    public boolean validarTransicion(Long estadoOrigenId, Long estadoDestinoId, Long empresaId) {
        Optional<TransicionEstadoConfig> transicion = transicionEstadoConfigRepository
            .findByEstadoOrigenIdAndEstadoDestinoIdAndEmpresaId(estadoOrigenId, estadoDestinoId, empresaId);
        return transicion.isPresent() && transicion.get().getActivo();
    }

    /**
     * Validar si una tarea es permitida en un estado
     */
    @Transactional(readOnly = true)
    public boolean validarTareaParaEstado(Long estadoId, String tipoLabor, Long empresaId) {
        List<TareaPorEstadoConfig> tareas = tareaPorEstadoConfigRepository
            .findTareasDisponiblesPorEstado(estadoId, empresaId);
        return tareas.stream()
            .anyMatch(t -> t.getTipoLabor().equals(tipoLabor) && t.getActivo());
    }
}
