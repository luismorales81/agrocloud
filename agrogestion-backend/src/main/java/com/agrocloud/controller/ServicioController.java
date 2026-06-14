package com.agrocloud.controller;

import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.Servicio;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.ServicioService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/porcinos/servicios")
public class ServicioController {

    @Autowired
    private UserService userService;

    @Autowired
    private ServicioService servicioService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Servicio>> getAllServicios(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Servicio> servicios = servicioService.obtenerServiciosPorEmpresa(user);
            return ResponseEntity.ok(servicios);
        } catch (Exception e) {
            System.err.println("Error al obtener servicios: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/pendientes-control")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Servicio>> getPendientesControl(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Servicio> servicios = servicioService.obtenerServiciosPendientesControl(user);
            return ResponseEntity.ok(servicios);
        } catch (Exception e) {
            System.err.println("Error al obtener servicios pendientes de control: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> crearServicio(@RequestBody Map<String, Object> servicioData, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no autenticado"));
            }

            // Convertir Map a entidad Servicio
            Servicio servicio = new Servicio();
            
            // Madre
            @SuppressWarnings("unchecked")
            Map<String, Object> madreMap = (Map<String, Object>) servicioData.get("madre");
            if (madreMap == null && servicioData.get("madreId") != null) {
                Madre madre = new Madre();
                madre.setId(Long.valueOf(servicioData.get("madreId").toString()));
                servicio.setMadre(madre);
            } else if (madreMap != null) {
                Madre madre = new Madre();
                madre.setId(Long.valueOf(madreMap.get("id").toString()));
                servicio.setMadre(madre);
            }

            // Tipo
            String tipoStr = servicioData.get("tipo").toString();
            servicio.setTipo(Servicio.TipoServicio.valueOf(tipoStr));

            // Fecha servicio
            String fechaServicioStr = servicioData.get("fechaServicio").toString();
            servicio.setFechaServicio(LocalDate.parse(fechaServicioStr));

            // Macho ID (opcional)
            if (servicioData.get("machoId") != null) {
                servicio.setMachoId(Long.valueOf(servicioData.get("machoId").toString()));
            }

            // Macho Nombre (opcional, para IA externa)
            if (servicioData.get("machoNombre") != null) {
                servicio.setMachoNombre(servicioData.get("machoNombre").toString());
            }

            // Origen semen (opcional, para IA)
            if (servicioData.get("origenSemen") != null) {
                String origenStr = servicioData.get("origenSemen").toString();
                servicio.setOrigenSemen(Servicio.OrigenSemen.valueOf(origenStr));
            }

            // Observaciones
            if (servicioData.get("observaciones") != null) {
                servicio.setObservaciones(servicioData.get("observaciones").toString());
            }

            // Calcular fecha control celo (fechaServicio + 21 dÃ­as)
            servicio.setFechaControlCelo(servicio.getFechaServicio().plusDays(21));

            Servicio servicioCreado = servicioService.crearServicio(servicio, user);
            return ResponseEntity.ok(servicioCreado);
        } catch (IllegalArgumentException e) {
            System.err.println("Error de validaciÃ³n al crear servicio: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error al crear servicio: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error interno al crear el servicio: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/control-celo")
    @Transactional
    public ResponseEntity<?> registrarControlCelo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> controlData,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no autenticado"));
            }

            // Extraer datos del control
            Boolean prenada = null;
            if (controlData.get("prenada") != null) {
                if (controlData.get("prenada") instanceof Boolean) {
                    prenada = (Boolean) controlData.get("prenada");
                } else {
                    prenada = Boolean.parseBoolean(controlData.get("prenada").toString());
                }
            }

            if (prenada == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Debe especificar si la madre esta prenada"));
            }

            String observaciones = controlData.get("observaciones") != null 
                ? controlData.get("observaciones").toString() 
                : null;

            Servicio servicioActualizado = servicioService.registrarControlCelo(id, prenada, observaciones, user);
            return ResponseEntity.ok(servicioActualizado);
        } catch (IllegalArgumentException e) {
            System.err.println("Error de validaciÃ³n al registrar control de celo: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error al registrar control de celo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error interno al registrar control de celo: " + e.getMessage()));
        }
    }

    /**
     * Obtener gestaciÃ³n asociada a un servicio (para registrar chequeo de preÃ±ez desde calendario).
     * GET /api/v1/porcinos/servicios/{id}/gestacion
     */
    @GetMapping("/{id}/gestacion")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Long>> getGestacionPorServicio(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            return servicioService.obtenerGestacionIdPorServicioId(id, user)
                    .map(gestacionId -> ResponseEntity.<Map<String, Long>>ok(Map.of("gestacionId", gestacionId)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error al obtener gestaciÃ³n por servicio: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}







