package com.agrocloud.controller;

import com.agrocloud.core.domain.Recordatorio;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.application.RecordatorioService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recordatorios")
public class RecordatorioController {

    @Autowired
    private RecordatorioService recordatorioService;

    @Autowired
    private UserService userService;

    /**
     * Helper method para obtener el usuario desde UserDetails
     */
    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    /**
     * Obtener todos los recordatorios del usuario autenticado
     */
    @GetMapping
    public ResponseEntity<List<Recordatorio>> getRecordatorios(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Recordatorio> recordatorios = recordatorioService.getRecordatoriosByUsuario(user.getId());
            return ResponseEntity.ok(recordatorios);
        } catch (Exception e) {
            System.err.println("Error al obtener recordatorios: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener recordatorios por rango de fechas
     */
    @GetMapping("/rango")
    public ResponseEntity<List<Recordatorio>> getRecordatoriosPorRango(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Recordatorio> recordatorios = recordatorioService.getRecordatoriosByUsuarioAndRango(
                    user.getId(), fechaInicio, fechaFin);
            return ResponseEntity.ok(recordatorios);
        } catch (Exception e) {
            System.err.println("Error al obtener recordatorios por rango: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener recordatorios pendientes
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<Recordatorio>> getRecordatoriosPendientes(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Recordatorio> recordatorios = recordatorioService.getRecordatoriosPendientes(user.getId());
            return ResponseEntity.ok(recordatorios);
        } catch (Exception e) {
            System.err.println("Error al obtener recordatorios pendientes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crear un nuevo recordatorio
     */
    @PostMapping
    public ResponseEntity<Recordatorio> crearRecordatorio(
            @RequestBody Recordatorio recordatorio,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            recordatorio.setUsuario(user);
            Recordatorio nuevoRecordatorio = recordatorioService.crearRecordatorio(recordatorio);
            return ResponseEntity.ok(nuevoRecordatorio);
        } catch (Exception e) {
            System.err.println("Error al crear recordatorio: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualizar un recordatorio existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<Recordatorio> actualizarRecordatorio(
            @PathVariable Long id,
            @RequestBody Recordatorio recordatorio,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            recordatorio.setUsuario(user);
            Recordatorio recordatorioActualizado = recordatorioService.actualizarRecordatorio(id, recordatorio);
            return ResponseEntity.ok(recordatorioActualizado);
        } catch (Exception e) {
            System.err.println("Error al actualizar recordatorio: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Marcar un recordatorio como completado
     */
    @PatchMapping("/{id}/completar")
    public ResponseEntity<Recordatorio> marcarCompletado(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Recordatorio recordatorio = recordatorioService.marcarCompletado(id, user.getId());
            return ResponseEntity.ok(recordatorio);
        } catch (Exception e) {
            System.err.println("Error al marcar recordatorio como completado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Eliminar un recordatorio
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRecordatorio(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            recordatorioService.eliminarRecordatorio(id, user.getId());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("Error al eliminar recordatorio: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener un recordatorio por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Recordatorio> getRecordatorioById(@PathVariable Long id) {
        try {
            Optional<Recordatorio> recordatorio = recordatorioService.getRecordatorioById(id);
            return recordatorio.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error al obtener recordatorio: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

