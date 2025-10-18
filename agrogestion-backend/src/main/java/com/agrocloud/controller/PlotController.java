package com.agrocloud.controller;

import com.agrocloud.dto.SiembraRequest;
import com.agrocloud.dto.CosechaRequest;
import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.Cultivo;
import com.agrocloud.service.PlotService;
import com.agrocloud.service.UserService;
import com.agrocloud.service.SiembraService;
import com.agrocloud.service.EmpresaContextService;
import com.agrocloud.repository.CultivoRepository;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.exception.BadRequestException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
@CrossOrigin(origins = "*")
public class PlotController {

    @Autowired
    private PlotService plotService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private SiembraService siembraService;
    
    @Autowired
    private EmpresaContextService empresaContextService;
    
    @Autowired
    private CultivoRepository cultivoRepository;

    // Obtener todos los lotes accesibles por el usuario
    @GetMapping
    public ResponseEntity<List<Plot>> getAllLotes(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("[PLOT_CONTROLLER] Iniciando getAllLotes para usuario: " + (userDetails != null ? userDetails.getUsername() : "null"));
            
            if (userDetails == null) {
                System.err.println("[PLOT_CONTROLLER] ERROR: UserDetails es null");
                return ResponseEntity.status(401).build();
            }
            
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
            if (user == null) {
                System.err.println("[PLOT_CONTROLLER] ERROR: Usuario no encontrado: " + userDetails.getUsername());
                return ResponseEntity.status(404).build();
            }
            
            System.out.println("[PLOT_CONTROLLER] Usuario encontrado: " + user.getEmail() + ", roles: " + user.getRoles().size());
            
            List<Plot> lotes = plotService.getLotesByUser(user);
            System.out.println("[PLOT_CONTROLLER] Lotes obtenidos: " + (lotes != null ? lotes.size() : "null"));
            
            return ResponseEntity.ok(lotes);
        } catch (Exception e) {
            System.err.println("[PLOT_CONTROLLER] ERROR en getAllLotes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // Obtener lote por ID
    @GetMapping("/{id}")
    public ResponseEntity<Plot> getLoteById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
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
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
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
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
            Optional<Plot> existingLote = plotService.getLoteById(id, user);
            
            if (existingLote.isPresent()) {
                lote.setId(id);
                lote.setUser(user);
                Plot updatedLote = plotService.saveLote(lote);
                return ResponseEntity.ok(updatedLote);
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
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
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
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
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
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
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
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
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
            
            // Obtener empresa principal del usuario
            Empresa empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("El usuario no pertenece a ninguna empresa"));
            
            Plot lote = siembraService.sembrarLote(id, request, usuario, empresa);
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
            
            // Obtener empresa principal del usuario
            Empresa empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("El usuario no pertenece a ninguna empresa"));
            
            Plot lote = siembraService.cosecharLote(id, request, usuario, empresa);
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
            Empresa empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("El usuario no pertenece a ninguna empresa"));
            
            String motivo = request.getOrDefault("motivo", "Sin motivo especificado");
            
            Plot lote = siembraService.abandonarCultivo(id, motivo, usuario, empresa);
            
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
            Empresa empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("El usuario no pertenece a ninguna empresa"));
            
            String motivo = request.getOrDefault("motivo", "Sin motivo especificado");
            
            Plot lote = siembraService.limpiarCultivo(id, motivo, usuario, empresa);
            
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
            Empresa empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("El usuario no pertenece a ninguna empresa"));
            
            Plot lote = siembraService.convertirAForraje(id, request, usuario, empresa);
            
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
