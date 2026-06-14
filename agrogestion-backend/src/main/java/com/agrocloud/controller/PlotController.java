package com.agrocloud.controller;

import com.agrocloud.dto.SiembraRequest;
import com.agrocloud.dto.CosechaRequest;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.cultivos.domain.Cultivo;
import com.agrocloud.cultivos.application.PlotService;
import com.agrocloud.cultivos.application.SiembraService;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.cultivos.infrastructure.CultivoRepository;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.exception.BadRequestException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/lotes")
public class PlotController {

    @Autowired
    @Qualifier("plotServicioCultivos")
    private PlotService plotService;

    @Autowired
    @Qualifier("userServiceCore")
    private UserService userService;
    
    @Autowired
    @Qualifier("siembraServiceCultivos")
    private SiembraService siembraService;
    
    @Autowired
    @Qualifier("empresaContextServiceCore")
    private EmpresaContextService empresaContextService;
    
    @Autowired
    @Qualifier("cultivoRepositoryCultivos")
    private CultivoRepository cultivoRepository;

    @Autowired
    private com.agrocloud.core.application.UserService usuarioCoreService;

    @Autowired
    private EmpresaRepository empresaCoreRepository;

    private com.agrocloud.core.domain.Empresa obtenerEmpresaCore(User usuario) {
        Long empresaId = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuario.getId())
                .orElseThrow(() -> new RuntimeException("El usuario no pertenece a ninguna empresa"))
                .getId();
        return empresaCoreRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
    }

    // Obtener todos los lotes accesibles por el usuario
    @GetMapping
    public ResponseEntity<List<Plot>> getAllLotes(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("[PLOT_CONTROLLER] Iniciando getAllLotes para usuario: " + (userDetails != null ? userDetails.getUsername() : "null"));
            
            if (userDetails == null) {
                System.err.println("[PLOT_CONTROLLER] ERROR: UserDetails es null");
                return ResponseEntity.status(401).build();
            }
            
            User user = userService.findByEmailWithAllRelationsCombined(userDetails.getUsername());
            if (user == null) {
                System.err.println("[PLOT_CONTROLLER] ERROR: Usuario no encontrado: " + userDetails.getUsername());
                return ResponseEntity.status(404).build();
            }
            
            System.out.println("[PLOT_CONTROLLER] Usuario encontrado: " + user.getEmail());
            
            List<Plot> lotes = plotService.getLotesByUser(user);
            System.out.println("[PLOT_CONTROLLER] Lotes obtenidos: " + (lotes != null ? lotes.size() : "null"));
            
            return ResponseEntity.ok(lotes);
        } catch (Exception e) {
            System.err.println("[PLOT_CONTROLLER] ERROR en getAllLotes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /** Lotes del módulo cultivos (tabla cultivo_lotes). */
    @GetMapping("/cultivo")
    public ResponseEntity<List<com.agrocloud.cultivos.domain.Plot>> getLotesCultivo(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401).build();
            }
            User user = userService.findByEmailWithAllRelationsCombined(userDetails.getUsername());
            if (user == null) {
                return ResponseEntity.status(404).build();
            }
            return ResponseEntity.ok(plotService.getLotesCultivoByUser(user));
        } catch (Exception e) {
            System.err.println("[PLOT_CONTROLLER] ERROR en getLotesCultivo: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // Obtener lote por ID
    @GetMapping("/{id}")
    public ResponseEntity<Plot> getLoteById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelationsCombined(userDetails.getUsername());
            Optional<Plot> lote = plotService.getLoteById(id, user);
            
            return lote.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Crear nuevo lote
    @PostMapping
    public ResponseEntity<?> createLote(@RequestBody Plot lote, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelationsCombined(userDetails.getUsername());
            lote.setUser(user);
            Plot savedLote = plotService.saveLote(lote);
            return ResponseEntity.ok(savedLote);
        } catch (RuntimeException e) {
            System.err.println("[PLOT_CONTROLLER] ERROR al crear lote: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("[PLOT_CONTROLLER] ERROR inesperado al crear lote: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error interno del servidor"
            ));
        }
    }

    // Actualizar lote existente
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLote(@PathVariable Long id, @RequestBody Plot lote, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelationsCombined(userDetails.getUsername());
            Optional<Plot> existingLote = plotService.getLoteById(id, user);
            
            if (existingLote.isPresent()) {
                return plotService.updateLote(id, lote, user)
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            System.err.println("[PLOT_CONTROLLER] ERROR al actualizar lote: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("[PLOT_CONTROLLER] ERROR inesperado al actualizar lote: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error interno del servidor"
            ));
        }
    }

    // Eliminar lote lógicamente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLote(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelationsCombined(userDetails.getUsername());
            boolean deleted = plotService.deleteLote(id, user);
            
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Eliminar lote físicamente (solo administradores)
    @DeleteMapping("/{id}/fisico")
    public ResponseEntity<Void> deleteLoteFisicamente(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelationsCombined(userDetails.getUsername());
            boolean deleted = plotService.deleteLoteFisicamente(id, user);
            
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Obtener todos los lotes (incluyendo inactivos) - solo administradores
    @GetMapping("/todos")
    public ResponseEntity<List<Plot>> getAllLotesIncludingInactive(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelationsCombined(userDetails.getUsername());
            List<Plot> lotes = plotService.getAllLotesIncludingInactive(user);
            return ResponseEntity.ok(lotes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Buscar lotes por nombre
    @GetMapping("/buscar")
    public ResponseEntity<List<Plot>> searchLotes(@RequestParam String nombre, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelationsCombined(userDetails.getUsername());
            List<Plot> lotes = plotService.searchLotesByNombre(nombre, user);
            return ResponseEntity.ok(lotes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Endpoint para sembrar un lote
     * POST /api/v1/lotes/{id}/sembrar
     */
    @PostMapping("/{id}/sembrar")
    public ResponseEntity<?> sembrarLote(
            @PathVariable Long id,
            @Valid @RequestBody SiembraRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("[PLOT_CONTROLLER] Iniciando siembra para lote ID: " + id);
            
            if (userDetails == null) {
                System.err.println("[PLOT_CONTROLLER] ERROR: UserDetails es null");
                return ResponseEntity.status(401).build();
            }
            
            // Obtener usuario
            User usuario = userService.findByEmailWithRelations(userDetails.getUsername());
            com.agrocloud.core.domain.User usuarioCore = usuarioCoreService.findByEmailWithRelations(userDetails.getUsername());
            com.agrocloud.core.domain.Empresa empresa = obtenerEmpresaCore(usuario);
            
            com.agrocloud.cultivos.domain.Plot lote = siembraService.sembrarLote(id, request, usuarioCore, empresa);
            System.out.println("[PLOT_CONTROLLER] Lote sembrado exitosamente: " + lote.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lote sembrado exitosamente",
                "loteId", lote.getId(),
                "estado", lote.getEstado().toString(),
                "cultivoActual", lote.getCultivoActual() != null ? lote.getCultivoActual() : ""
            ));
        } catch (com.agrocloud.exception.ResourceNotFoundException e) {
            System.err.println("[PLOT_CONTROLLER] ERROR: " + e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (com.agrocloud.exception.BadRequestException e) {
            System.err.println("[PLOT_CONTROLLER] ERROR: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("[PLOT_CONTROLLER] ERROR inesperado en sembrarLote: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para obtener información del cultivo del lote para cosecha
     * GET /api/v1/lotes/{id}/info-cosecha
     */
    @GetMapping("/{id}/info-cosecha")
    public ResponseEntity<?> obtenerInfoCosechaLote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("[PLOT_CONTROLLER] Obteniendo info de cosecha para lote ID: " + id);
            
            if (userDetails == null) {
                System.err.println("[PLOT_CONTROLLER] ERROR: UserDetails es null");
                return ResponseEntity.status(401).build();
            }
            
            // Obtener usuario
            User usuario = userService.findByEmailWithRelations(userDetails.getUsername());
            
            // Obtener empresa principal del usuario
            Empresa empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("El usuario no pertenece a ninguna empresa"));
            
            // Obtener lote
            Plot lote = plotService.getLoteById(id, usuario)
                    .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + id));
            
            // Verificar que el lote pertenece a la empresa
            if (!lote.getCampo().getEmpresa().getId().equals(empresa.getId())) {
                throw new BadRequestException("El lote no pertenece a la empresa actual");
            }
            
            // Buscar el cultivo por nombre del lote
            Cultivo cultivo = null;
            if (lote.getCultivoActual() != null && !lote.getCultivoActual().isEmpty()) {
                List<Cultivo> cultivosEncontrados = cultivoRepository.findByNombreContaining(lote.getCultivoActual());
                // Filtrar por empresa si es necesario
                cultivo = cultivosEncontrados.stream()
                        .filter(c -> c.getEmpresa() != null && c.getEmpresa().getId().equals(empresa.getId()))
                        .findFirst()
                        .orElse(null);
            }
            
            // Preparar respuesta con información del lote y cultivo
            Map<String, Object> infoCosecha = new HashMap<>();
            infoCosecha.put("loteId", lote.getId());
            infoCosecha.put("nombreLote", lote.getNombre());
            infoCosecha.put("superficie", lote.getAreaHectareas());
            infoCosecha.put("cultivoActual", lote.getCultivoActual());
            infoCosecha.put("fechaSiembra", lote.getFechaSiembra());
            infoCosecha.put("rendimientoEsperadoLote", lote.getRendimientoEsperado());
            
            // Información del cultivo si existe
            if (cultivo != null) {
                infoCosecha.put("cultivoId", cultivo.getId());
                infoCosecha.put("variedadSemilla", cultivo.getVariedad());
                infoCosecha.put("rendimientoEsperadoCultivo", cultivo.getRendimientoEsperado());
                infoCosecha.put("unidadRendimiento", cultivo.getUnidadRendimiento());
                infoCosecha.put("cicloDias", cultivo.getCicloDias());
                infoCosecha.put("precioPorTonelada", cultivo.getPrecioPorTonelada());
            } else {
                // Si no se encuentra el cultivo, usar valores por defecto
                infoCosecha.put("variedadSemilla", "No especificada");
                infoCosecha.put("rendimientoEsperadoCultivo", null);
                infoCosecha.put("unidadRendimiento", "ton");
            }
            
            System.out.println("[PLOT_CONTROLLER] Info de cosecha obtenida exitosamente para lote: " + lote.getId());
            
            return ResponseEntity.ok(infoCosecha);
            
        } catch (ResourceNotFoundException e) {
            System.err.println("[PLOT_CONTROLLER] ERROR: " + e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (BadRequestException e) {
            System.err.println("[PLOT_CONTROLLER] ERROR: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("[PLOT_CONTROLLER] ERROR inesperado en obtenerInfoCosechaLote: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para cosechar un lote
     * POST /api/v1/lotes/{id}/cosechar
     */
    @PostMapping("/{id}/cosechar")
    public ResponseEntity<?> cosecharLote(
            @PathVariable Long id,
            @Valid @RequestBody CosechaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("[PLOT_CONTROLLER] Iniciando cosecha para lote ID: " + id);
            
            if (userDetails == null) {
                System.err.println("[PLOT_CONTROLLER] ERROR: UserDetails es null");
                return ResponseEntity.status(401).build();
            }
            
            // Obtener usuario
            User usuario = userService.findByEmailWithRelations(userDetails.getUsername());
            com.agrocloud.core.domain.User usuarioCore = usuarioCoreService.findByEmailWithRelations(userDetails.getUsername());
            com.agrocloud.core.domain.Empresa empresa = obtenerEmpresaCore(usuario);
            
            com.agrocloud.cultivos.domain.Plot lote = siembraService.cosecharLote(id, request, usuarioCore, empresa);
            System.out.println("[PLOT_CONTROLLER] Lote cosechado exitosamente: " + lote.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lote cosechado exitosamente",
                "loteId", lote.getId(),
                "estado", lote.getEstado().toString(),
                "cultivoActual", lote.getCultivoActual() != null ? lote.getCultivoActual() : ""
            ));
        } catch (com.agrocloud.exception.ResourceNotFoundException e) {
            System.err.println("[PLOT_CONTROLLER] ERROR: " + e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (com.agrocloud.exception.BadRequestException e) {
            System.err.println("[PLOT_CONTROLLER] ERROR: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("[PLOT_CONTROLLER] ERROR inesperado en cosecharLote: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para abandonar un cultivo
     * POST /api/v1/lotes/{id}/abandonar
     */
    @PostMapping("/{id}/abandonar")
    public ResponseEntity<?> abandonarCultivo(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("[PLOT_CONTROLLER] Abandonando cultivo para lote ID: " + id);
            
            if (userDetails == null) {
                return ResponseEntity.status(401).build();
            }
            
            User usuario = userService.findByEmailWithRelations(userDetails.getUsername());
            com.agrocloud.core.domain.User usuarioCore = usuarioCoreService.findByEmailWithRelations(userDetails.getUsername());
            com.agrocloud.core.domain.Empresa empresa = obtenerEmpresaCore(usuario);
            
            String motivo = request.getOrDefault("motivo", "Sin motivo especificado");
            
            com.agrocloud.cultivos.domain.Plot lote = siembraService.abandonarCultivo(id, motivo, usuarioCore, empresa);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cultivo abandonado exitosamente",
                "lote", lote
            ));
        } catch (com.agrocloud.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", e.getMessage()));
        } catch (com.agrocloud.exception.BadRequestException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("[PLOT_CONTROLLER] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * Endpoint para limpiar un cultivo
     * POST /api/v1/lotes/{id}/limpiar
     */
    @PostMapping("/{id}/limpiar")
    public ResponseEntity<?> limpiarCultivo(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("[PLOT_CONTROLLER] Limpiando cultivo para lote ID: " + id);
            
            if (userDetails == null) {
                return ResponseEntity.status(401).build();
            }
            
            User usuario = userService.findByEmailWithRelations(userDetails.getUsername());
            com.agrocloud.core.domain.User usuarioCore = usuarioCoreService.findByEmailWithRelations(userDetails.getUsername());
            com.agrocloud.core.domain.Empresa empresa = obtenerEmpresaCore(usuario);
            
            String motivo = request.getOrDefault("motivo", "Sin motivo especificado");
            
            com.agrocloud.cultivos.domain.Plot lote = siembraService.limpiarCultivo(id, motivo, usuarioCore, empresa);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cultivo limpiado exitosamente. El lote está ahora disponible",
                "lote", lote
            ));
        } catch (com.agrocloud.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", e.getMessage()));
        } catch (com.agrocloud.exception.BadRequestException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("[PLOT_CONTROLLER] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * Endpoint para convertir cultivo a forraje
     * POST /api/v1/lotes/{id}/convertir-forraje
     */
    @PostMapping("/{id}/convertir-forraje")
    public ResponseEntity<?> convertirAForraje(
            @PathVariable Long id,
            @Valid @RequestBody CosechaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("[PLOT_CONTROLLER] Convirtiendo a forraje para lote ID: " + id);
            
            if (userDetails == null) {
                return ResponseEntity.status(401).build();
            }
            
            User usuario = userService.findByEmailWithRelations(userDetails.getUsername());
            com.agrocloud.core.domain.User usuarioCore = usuarioCoreService.findByEmailWithRelations(userDetails.getUsername());
            com.agrocloud.core.domain.Empresa empresa = obtenerEmpresaCore(usuario);
            
            com.agrocloud.cultivos.domain.Plot lote = siembraService.convertirAForraje(id, request, usuarioCore, empresa);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cultivo convertido a forraje exitosamente",
                "lote", lote
            ));
        } catch (com.agrocloud.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", e.getMessage()));
        } catch (com.agrocloud.exception.BadRequestException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("[PLOT_CONTROLLER] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
