package com.agrocloud.controller;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.cultivos.domain.EstadoLoteConfig;
import com.agrocloud.cultivos.domain.TareaPorEstadoConfig;
import com.agrocloud.cultivos.domain.TipoCultivo;
import com.agrocloud.cultivos.domain.TransicionEstadoConfig;

import com.agrocloud.dto.ValidacionConfiguracionEstadosDTO;
import com.agrocloud.dto.TipoCultivoResumenDTO;
import com.agrocloud.cultivos.application.ConfiguracionEstadosService;
import com.agrocloud.cultivos.application.ImportacionConfiguracionEstadosService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para gestionar la configuraciÃ³n de estados y tareas por tipo de cultivo
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/configuracion-estados")
public class ConfiguracionEstadosController {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionEstadosController.class);

    @Autowired
    private ConfiguracionEstadosService configuracionEstadosService;

    @Autowired
    private ImportacionConfiguracionEstadosService importacionConfiguracionEstadosService;

    @Autowired
    private UserService userService;

    // ============================================================================
    // IMPORTACIÃ“N / EXPORTACIÃ“N EXCEL
    // ============================================================================

    /**
     * Descargar archivo Excel de ejemplo para importar configuraciÃ³n
     */
    @GetMapping("/plantilla-excel")
    public ResponseEntity<ByteArrayResource> descargarPlantillaExcel() {
        try {
            byte[] contenido = importacionConfiguracionEstadosService.generarPlantillaEjemplo();
            ByteArrayResource recurso = new ByteArrayResource(contenido);
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"plantilla_configuracion_estados.xlsx\"")
                .body(recurso);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Importar configuraciÃ³n desde archivo Excel
     *
     * @param archivo Archivo Excel (.xlsx)
     * @param nombreTipoCultivo Nombre del tipo de cultivo (para crear nuevo)
     * @param tipoCultivoId Si se proporciona, reemplaza la configuraciÃ³n existente
     * @param empresaId Si se proporciona, crea personalizaciÃ³n por empresa
     */
    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importarDesdeExcel(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(required = false) String nombreTipoCultivo,
            @RequestParam(required = false) Long tipoCultivoId,
            @RequestParam(required = false) Long empresaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long empresaIdFinal = empresaId;
            if (empresaIdFinal == null && userDetails != null) {
                User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
                Optional<Long> empresaIdOpt = configuracionEstadosService.obtenerEmpresaIdDelUsuario(user);
                empresaIdFinal = empresaIdOpt.orElse(null);
            }

            ImportacionConfiguracionEstadosService.ResultadoImportacion resultado =
                importacionConfiguracionEstadosService.importarDesdeExcel(
                    archivo, nombreTipoCultivo, tipoCultivoId, empresaIdFinal);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("exito", resultado.isExito());
            respuesta.put("tipoCultivoId", resultado.getTipoCultivoId());
            respuesta.put("tipoCultivoNombre", resultado.getTipoCultivoNombre());
            respuesta.put("estadosCreados", resultado.getEstadosCreados());
            respuesta.put("transicionesCreadas", resultado.getTransicionesCreadas());
            respuesta.put("tareasCreadas", resultado.getTareasCreadas());
            respuesta.put("errores", resultado.getErrores());

            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================================
    // TIPOS DE CULTIVO
    // ============================================================================

    /**
     * Obtener todos los tipos de cultivo
     */
    @GetMapping("/tipos-cultivo")
    public ResponseEntity<?> obtenerTiposCultivo() {
        try {
            List<TipoCultivoResumenDTO> tipos = configuracionEstadosService.obtenerTodosLosTiposCultivo().stream()
                .map(TipoCultivoResumenDTO::desde)
                .toList();
            return ResponseEntity.ok(tipos);
        } catch (Exception e) {
            logger.error("Error al listar tipos de cultivo", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "No se pudieron cargar los tipos de cultivo: " + e.getMessage()));
        }
    }

    /**
     * Obtener solo las plantillas globales
     */
    @GetMapping("/tipos-cultivo/plantillas")
    public ResponseEntity<?> obtenerPlantillas() {
        try {
            List<TipoCultivoResumenDTO> plantillas = configuracionEstadosService.obtenerPlantillas().stream()
                .map(TipoCultivoResumenDTO::desde)
                .toList();
            return ResponseEntity.ok(plantillas);
        } catch (Exception e) {
            logger.error("Error al listar plantillas de tipos de cultivo", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "No se pudieron cargar las plantillas: " + e.getMessage()));
        }
    }

    /**
     * Obtener un tipo de cultivo por ID
     */
    @GetMapping("/tipos-cultivo/{id}")
    public ResponseEntity<?> obtenerTipoCultivoPorId(@PathVariable Long id) {
        try {
            Optional<TipoCultivo> tipo = configuracionEstadosService.obtenerTipoCultivoPorId(id);
            return tipo.map(t -> ResponseEntity.ok(TipoCultivoResumenDTO.desde(t)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error al obtener tipo de cultivo id={}", id, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "No se pudo cargar el tipo de cultivo: " + e.getMessage()));
        }
    }

    /**
     * Crear un nuevo tipo de cultivo
     */
    @PostMapping("/tipos-cultivo")
    public ResponseEntity<?> crearTipoCultivo(@RequestBody TipoCultivo tipoCultivo, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            TipoCultivo creado = configuracionEstadosService.crearTipoCultivo(tipoCultivo);
            return ResponseEntity.ok(TipoCultivoResumenDTO.desde(creado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error al crear tipo de cultivo", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "No se pudo crear el tipo de cultivo: " + e.getMessage()));
        }
    }

    /**
     * Actualizar un tipo de cultivo
     */
    @PutMapping("/tipos-cultivo/{id}")
    public ResponseEntity<?> actualizarTipoCultivo(@PathVariable Long id, @RequestBody TipoCultivo tipoCultivo) {
        try {
            Optional<TipoCultivo> actualizado = configuracionEstadosService.actualizarTipoCultivo(id, tipoCultivo);
            if (actualizado.isPresent()) {
                return ResponseEntity.ok(actualizado.get());
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Eliminar un tipo de cultivo
     */
    @DeleteMapping("/tipos-cultivo/{id}")
    public ResponseEntity<?> eliminarTipoCultivo(@PathVariable Long id) {
        try {
            boolean eliminado = configuracionEstadosService.eliminarTipoCultivo(id);
            if (eliminado) {
                return ResponseEntity.ok(Map.of("mensaje", "Tipo de cultivo eliminado exitosamente"));
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================================
    // ESTADOS
    // ============================================================================

    /**
     * Obtener estados para un tipo de cultivo (resuelve plantilla o personalizaciÃ³n)
     */
    @GetMapping("/estados")
    public ResponseEntity<List<EstadoLoteConfig>> obtenerEstados(
            @RequestParam Long tipoCultivoId,
            @RequestParam(required = false) Long empresaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Si no se proporciona empresaId, obtenerlo del usuario
            if (empresaId == null && userDetails != null) {
                User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
                Optional<Long> empresaIdOpt = configuracionEstadosService.obtenerEmpresaIdDelUsuario(user);
                empresaId = empresaIdOpt.orElse(null);
            }

            List<EstadoLoteConfig> estados = configuracionEstadosService.obtenerEstadosPorTipoCultivo(tipoCultivoId, empresaId);
            return ResponseEntity.ok(estados);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener un estado por ID
     */
    @GetMapping("/estados/{id}")
    public ResponseEntity<EstadoLoteConfig> obtenerEstadoPorId(@PathVariable Long id) {
        try {
            Optional<EstadoLoteConfig> estado = configuracionEstadosService.obtenerEstadoPorId(id);
            return estado.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crear un nuevo estado
     */
    @PostMapping("/estados")
    public ResponseEntity<?> crearEstado(
            @RequestBody EstadoLoteConfig estado,
            @RequestParam Long tipoCultivoId,
            @RequestParam(required = false) Long empresaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Si no se proporciona empresaId, obtenerlo del usuario
            if (empresaId == null && userDetails != null) {
                User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
                Optional<Long> empresaIdOpt = configuracionEstadosService.obtenerEmpresaIdDelUsuario(user);
                empresaId = empresaIdOpt.orElse(null);
            }

            EstadoLoteConfig creado = configuracionEstadosService.crearEstado(estado, tipoCultivoId, empresaId);
            return ResponseEntity.ok(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualizar un estado
     */
    @PutMapping("/estados/{id}")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestBody EstadoLoteConfig estado) {
        try {
            Optional<EstadoLoteConfig> actualizado = configuracionEstadosService.actualizarEstado(id, estado);
            if (actualizado.isPresent()) {
                return ResponseEntity.ok(actualizado.get());
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Reordenar estados
     */
    @PutMapping("/estados/reordenar")
    public ResponseEntity<?> reordenarEstados(@RequestBody List<Long> estadoIds) {
        try {
            configuracionEstadosService.reordenarEstados(estadoIds);
            return ResponseEntity.ok(Map.of("mensaje", "Estados reordenados exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Eliminar un estado
     */
    @DeleteMapping("/estados/{id}")
    public ResponseEntity<?> eliminarEstado(@PathVariable Long id) {
        try {
            boolean eliminado = configuracionEstadosService.eliminarEstado(id);
            if (eliminado) {
                return ResponseEntity.ok(Map.of("mensaje", "Estado eliminado exitosamente"));
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Copiar plantilla a personalizaciÃ³n de empresa
     */
    @PostMapping("/estados/copiar-plantilla")
    public ResponseEntity<?> copiarPlantillaAEmpresa(
            @RequestParam Long tipoCultivoId,
            @RequestParam(required = false) Long empresaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Si no se proporciona empresaId, obtenerlo del usuario
            if (empresaId == null && userDetails != null) {
                User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
                Optional<Long> empresaIdOpt = configuracionEstadosService.obtenerEmpresaIdDelUsuario(user);
                empresaId = empresaIdOpt.orElseThrow(() -> new IllegalArgumentException("No se pudo determinar la empresa del usuario"));
            }

            configuracionEstadosService.copiarPlantillaAEmpresa(tipoCultivoId, empresaId);
            return ResponseEntity.ok(Map.of("mensaje", "Plantilla copiada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================================
    // TRANSICIONES
    // ============================================================================

    /**
     * Obtener transiciones para un tipo de cultivo
     */
    @GetMapping("/transiciones")
    public ResponseEntity<List<TransicionEstadoConfig>> obtenerTransiciones(
            @RequestParam Long tipoCultivoId,
            @RequestParam(required = false) Long empresaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Si no se proporciona empresaId, obtenerlo del usuario
            if (empresaId == null && userDetails != null) {
                User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
                Optional<Long> empresaIdOpt = configuracionEstadosService.obtenerEmpresaIdDelUsuario(user);
                empresaId = empresaIdOpt.orElse(null);
            }

            List<TransicionEstadoConfig> transiciones = configuracionEstadosService.obtenerTransicionesPorTipoCultivo(tipoCultivoId, empresaId);
            return ResponseEntity.ok(transiciones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener transiciones vÃ¡lidas desde un estado
     */
    @GetMapping("/transiciones/desde-estado/{estadoOrigenId}")
    public ResponseEntity<List<TransicionEstadoConfig>> obtenerTransicionesDesdeEstado(
            @PathVariable Long estadoOrigenId,
            @RequestParam(required = false) Long empresaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Si no se proporciona empresaId, obtenerlo del usuario
            if (empresaId == null && userDetails != null) {
                User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
                Optional<Long> empresaIdOpt = configuracionEstadosService.obtenerEmpresaIdDelUsuario(user);
                empresaId = empresaIdOpt.orElse(null);
            }

            List<TransicionEstadoConfig> transiciones = configuracionEstadosService.obtenerTransicionesValidasDesdeEstado(estadoOrigenId, empresaId);
            return ResponseEntity.ok(transiciones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crear una nueva transiciÃ³n
     */
    @PostMapping("/transiciones")
    public ResponseEntity<?> crearTransicion(
            @RequestBody TransicionEstadoConfig transicion,
            @RequestParam Long tipoCultivoId,
            @RequestParam(required = false) Long empresaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Si no se proporciona empresaId, obtenerlo del usuario
            if (empresaId == null && userDetails != null) {
                User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
                Optional<Long> empresaIdOpt = configuracionEstadosService.obtenerEmpresaIdDelUsuario(user);
                empresaId = empresaIdOpt.orElse(null);
            }

            TransicionEstadoConfig creada = configuracionEstadosService.crearTransicion(transicion, tipoCultivoId, empresaId);
            return ResponseEntity.ok(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Eliminar una transición
     */
    @DeleteMapping("/transiciones/{id}")
    public ResponseEntity<?> eliminarTransicion(@PathVariable Long id) {
        try {
            boolean eliminada = configuracionEstadosService.eliminarTransicion(id);
            if (eliminada) {
                return ResponseEntity.ok(Map.of("mensaje", "Transición eliminada exitosamente"));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualizar una transición existente.
     */
    @PutMapping("/transiciones/{id}")
    public ResponseEntity<?> actualizarTransicion(
            @PathVariable Long id,
            @RequestBody TransicionEstadoConfig transicion) {
        try {
            Optional<TransicionEstadoConfig> actualizada = configuracionEstadosService.actualizarTransicion(id, transicion);
            return actualizada.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Validar si una transición es válida
     */
    @GetMapping("/transiciones/validar")
    public ResponseEntity<Map<String, Object>> validarTransicion(
            @RequestParam Long estadoOrigenId,
            @RequestParam Long estadoDestinoId,
            @RequestParam(required = false) Long empresaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Si no se proporciona empresaId, obtenerlo del usuario
            if (empresaId == null && userDetails != null) {
                User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
                Optional<Long> empresaIdOpt = configuracionEstadosService.obtenerEmpresaIdDelUsuario(user);
                empresaId = empresaIdOpt.orElse(null);
            }

            boolean esValida = configuracionEstadosService.validarTransicion(estadoOrigenId, estadoDestinoId, empresaId);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("esValida", esValida);
            respuesta.put("mensaje", esValida ? "TransiciÃ³n vÃ¡lida" : "TransiciÃ³n no permitida");
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================================
    // TAREAS
    // ============================================================================

    /**
     * Obtener tareas disponibles para un estado
     */
    @GetMapping("/tareas")
    public ResponseEntity<List<TareaPorEstadoConfig>> obtenerTareas(
            @RequestParam Long estadoId,
            @RequestParam(required = false) Long empresaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Si no se proporciona empresaId, obtenerlo del usuario
            if (empresaId == null && userDetails != null) {
                User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
                Optional<Long> empresaIdOpt = configuracionEstadosService.obtenerEmpresaIdDelUsuario(user);
                empresaId = empresaIdOpt.orElse(null);
            }

            List<TareaPorEstadoConfig> tareas = configuracionEstadosService.obtenerTareasPorEstado(estadoId, empresaId);
            return ResponseEntity.ok(tareas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crear una nueva tarea
     */
    @PostMapping("/tareas")
    public ResponseEntity<?> crearTarea(
            @RequestBody TareaPorEstadoConfig tarea,
            @RequestParam Long tipoCultivoId,
            @RequestParam(required = false) Long empresaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Si no se proporciona empresaId, obtenerlo del usuario
            if (empresaId == null && userDetails != null) {
                User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
                Optional<Long> empresaIdOpt = configuracionEstadosService.obtenerEmpresaIdDelUsuario(user);
                empresaId = empresaIdOpt.orElse(null);
            }

            TareaPorEstadoConfig creada = configuracionEstadosService.crearTarea(tarea, tipoCultivoId, empresaId);
            return ResponseEntity.ok(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualizar una tarea
     */
    @PutMapping("/tareas/{id}")
    public ResponseEntity<?> actualizarTarea(@PathVariable Long id, @RequestBody TareaPorEstadoConfig tarea) {
        try {
            Optional<TareaPorEstadoConfig> actualizada = configuracionEstadosService.actualizarTarea(id, tarea);
            if (actualizada.isPresent()) {
                return ResponseEntity.ok(actualizada.get());
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Reordenar tareas de un estado
     */
    @PutMapping("/tareas/reordenar")
    public ResponseEntity<?> reordenarTareas(@RequestParam Long estadoId, @RequestBody List<Long> tareaIds) {
        try {
            configuracionEstadosService.reordenarTareas(estadoId, tareaIds);
            return ResponseEntity.ok(Map.of("mensaje", "Tareas reordenadas exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Eliminar una tarea con validaciÃ³n de labores asociadas
     */
    @DeleteMapping("/tareas/{id}")
    public ResponseEntity<?> eliminarTarea(@PathVariable Long id) {
        try {
            Map<String, Object> respuesta = configuracionEstadosService.eliminarTareaConValidacion(id);
            if ((Boolean) respuesta.get("eliminada")) {
                return ResponseEntity.ok(respuesta);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al eliminar la tarea: " + e.getMessage()));
        }
    }

    /**
     * Validar si una tarea es permitida en un estado
     */
    @GetMapping("/tareas/validar")
    public ResponseEntity<Map<String, Object>> validarTarea(
            @RequestParam Long estadoId,
            @RequestParam String tipoLabor,
            @RequestParam(required = false) Long empresaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Si no se proporciona empresaId, obtenerlo del usuario
            if (empresaId == null && userDetails != null) {
                User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
                Optional<Long> empresaIdOpt = configuracionEstadosService.obtenerEmpresaIdDelUsuario(user);
                empresaId = empresaIdOpt.orElse(null);
            }

            boolean esPermitida = configuracionEstadosService.validarTareaParaEstado(estadoId, tipoLabor, empresaId);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("esPermitida", esPermitida);
            respuesta.put("mensaje", esPermitida ? "Tarea permitida en este estado" : "Tarea no permitida en este estado");
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Validación completa de coherencia (estados, transiciones, tareas).
     */
    @GetMapping("/validacion-completa")
    public ResponseEntity<ValidacionConfiguracionEstadosDTO> validarConfiguracionCompleta(
            @RequestParam Long tipoCultivoId,
            @RequestParam(required = false) Long empresaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (empresaId == null && userDetails != null) {
                User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
                Optional<Long> empresaIdOpt = configuracionEstadosService.obtenerEmpresaIdDelUsuario(user);
                empresaId = empresaIdOpt.orElse(null);
            }
            return ResponseEntity.ok(
                configuracionEstadosService.validarConfiguracionCompleta(tipoCultivoId, empresaId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}













