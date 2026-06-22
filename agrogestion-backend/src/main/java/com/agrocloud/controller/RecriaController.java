package com.agrocloud.controller;

import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.RecriaService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/porcinos/recria")
public class RecriaController {

    @Autowired
    private RecriaService recriaService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Recria>> getAllRecrias(
            @RequestParam(required = false) Boolean activas,
            @RequestParam(required = false) Boolean delPeriodoActivo,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Recria> recrias = recriaService.listarRecrias(user, activas, delPeriodoActivo);
            return ResponseEntity.ok(recrias);
        } catch (Exception e) {
            System.err.println("Error al obtener recrÃ­as: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Recria> getRecriaById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Optional<Recria> recria = recriaService.obtenerRecriaPorId(id, user);
            return recria.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error al obtener recrÃ­a: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/lote/{loteId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Recria>> getRecriasByLote(
            @PathVariable Long loteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // Por ahora retornamos todas las recrÃ­as activas del usuario
            // Se puede mejorar filtrando por lote especÃ­fico
            List<Recria> recrias = recriaService.obtenerRecriasActivas(user);
            return ResponseEntity.ok(recrias);
        } catch (Exception e) {
            System.err.println("Error al obtener recrÃ­as por lote: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> crearRecria(
            @RequestBody Recria recria,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no vÃ¡lido"));
            }
            
            Recria recriaCreada = recriaService.crearRecria(recria, user);
            return ResponseEntity.ok(recriaCreada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error al crear recrÃ­a: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al crear recrÃ­a"));
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> actualizarRecria(
            @PathVariable Long id,
            @RequestBody Recria recria,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no vÃ¡lido"));
            }
            
            Optional<Recria> recriaExistente = recriaService.obtenerRecriaPorId(id, user);
            if (recriaExistente.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Actualizar campos permitidos
            Recria recriaActualizada = recriaExistente.get();
            if (recria.getPesoPromedio() != null) {
                recriaActualizada.setPesoPromedio(recria.getPesoPromedio());
            }
            if (recria.getPesoIndividual() != null) {
                recriaActualizada.setPesoIndividual(recria.getPesoIndividual());
            }
            if (recria.getEtapa() != null) {
                recriaActualizada.setEtapa(recria.getEtapa());
            }
            if (recria.getObservaciones() != null) {
                recriaActualizada.setObservaciones(recria.getObservaciones());
            }
            
            // Guardar cambios (se puede crear mÃ©todo actualizarRecria en el servicio)
            return ResponseEntity.ok(recriaActualizada);
        } catch (Exception e) {
            System.err.println("Error al actualizar recrÃ­a: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al actualizar recrÃ­a"));
        }
    }

    @PostMapping("/{id}/peso")
    @Transactional
    public ResponseEntity<?> registrarPeso(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no vÃ¡lido"));
            }
            
            BigDecimal nuevoPeso = new BigDecimal(request.get("pesoPromedio").toString());
            LocalDate fechaPeso = request.get("fechaPeso") != null ? 
                LocalDate.parse(request.get("fechaPeso").toString()) : LocalDate.now();
            
            Recria recriaActualizada = recriaService.registrarPeso(id, nuevoPeso, fechaPeso, user);
            return ResponseEntity.ok(recriaActualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error al registrar peso: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al registrar peso"));
        }
    }

    @PostMapping("/{id}/egreso")
    @Transactional
    public ResponseEntity<?> registrarEgreso(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no vÃ¡lido"));
            }
            
            LocalDate fechaSalida = request.get("fechaSalida") != null ? 
                LocalDate.parse(request.get("fechaSalida").toString()) : LocalDate.now();
            Recria.DestinoRecria destino = Recria.DestinoRecria.valueOf(
                request.get("destino").toString().toUpperCase());
            String observaciones = request.get("observaciones") != null ? 
                request.get("observaciones").toString() : null;
            
            Recria recria = recriaService.cerrarRecria(id, fechaSalida, destino, user);
            if (observaciones != null) {
                recria.setObservaciones(observaciones);
            }
            
            return ResponseEntity.ok(recria);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error al registrar egreso: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al registrar egreso"));
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> eliminarRecria(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no vÃ¡lido"));
            }
            
            Optional<Recria> recria = recriaService.obtenerRecriaPorId(id, user);
            if (recria.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Soft delete
            recria.get().setActivo(false);
            return ResponseEntity.ok(Map.of("message", "RecrÃ­a eliminada correctamente"));
        } catch (Exception e) {
            System.err.println("Error al eliminar recrÃ­a: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al eliminar recrÃ­a"));
        }
    }
}
