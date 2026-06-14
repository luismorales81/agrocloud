package com.agrocloud.controller;

import com.agrocloud.porcinos.domain.Parto;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.PartoService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/porcinos/partos")
public class PartoController {

    @Autowired
    private UserService userService;

    @Autowired
    private PartoService partoService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Parto>> getAllPartos(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Parto> partos = partoService.obtenerPartosPorEmpresa(user);
            return ResponseEntity.ok(partos);
        } catch (Exception e) {
            System.err.println("Error al obtener partos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/proximos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Parto>> getPartosProximos(@RequestParam(required = false, defaultValue = "7") int dias, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // Por ahora devolvemos todos los partos, pero se puede filtrar por fecha
            List<Parto> partos = partoService.obtenerPartosPorEmpresa(user);
            return ResponseEntity.ok(partos);
        } catch (Exception e) {
            System.err.println("Error al obtener partos prÃ³ximos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/madre/{madreId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Parto>> getPartosPorMadre(@PathVariable Long madreId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Parto> partos = partoService.obtenerPartosPorMadre(madreId, user);
            return ResponseEntity.ok(partos);
        } catch (Exception e) {
            System.err.println("Error al obtener partos por madre: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Parto> getPartoById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            java.util.Optional<Parto> parto = partoService.obtenerPartoPorId(id, user);
            return parto.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error al obtener parto: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> crearParto(@RequestBody java.util.Map<String, Object> partoData, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Usuario no autenticado"));
            }

            // Extraer madreId del request
            Long madreId = null;
            if (partoData.get("madreId") != null) {
                madreId = Long.valueOf(partoData.get("madreId").toString());
            } else if (partoData.get("madre") != null) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> madreMap = (java.util.Map<String, Object>) partoData.get("madre");
                if (madreMap.get("id") != null) {
                    madreId = Long.valueOf(madreMap.get("id").toString());
                }
            }

            if (madreId == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Debe especificar la madre"));
            }

            // Crear objeto Parto desde partoData
            Parto parto = new Parto();
            
            if (partoData.get("nacidosVivos") != null) {
                parto.setNacidosVivos(Integer.valueOf(partoData.get("nacidosVivos").toString()));
            }
            if (partoData.get("nacidosMuertos") != null) {
                parto.setNacidosMuertos(Integer.valueOf(partoData.get("nacidosMuertos").toString()));
            }
            if (partoData.get("momias") != null) {
                parto.setMomias(Integer.valueOf(partoData.get("momias").toString()));
            }
            if (partoData.get("pesoPromedioNacimiento") != null) {
                parto.setPesoPromedioNacimiento(new java.math.BigDecimal(partoData.get("pesoPromedioNacimiento").toString()));
            }
            if (partoData.get("observaciones") != null) {
                parto.setObservaciones(partoData.get("observaciones").toString());
            }
            if (partoData.get("intervenciones") != null) {
                parto.setIntervenciones(partoData.get("intervenciones").toString());
            }

            Parto partoCreado = partoService.registrarParto(madreId, parto, user);
            return ResponseEntity.ok(partoCreado);
        } catch (IllegalArgumentException e) {
            System.err.println("Error de validaciÃ³n al crear parto: " + e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error al crear parto: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(java.util.Map.of("error", "Error interno al crear el parto: " + e.getMessage()));
        }
    }
}







