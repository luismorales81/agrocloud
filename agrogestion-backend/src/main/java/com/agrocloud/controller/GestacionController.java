package com.agrocloud.controller;

import com.agrocloud.porcinos.domain.Gestacion;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.GestacionService;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/porcinos/gestacion")
public class GestacionController {

    @Autowired
    private GestacionService gestacionService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Gestacion>> getAllGestaciones(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            List<Gestacion> gestaciones = gestacionService.obtenerGestacionesActivas(user);
            return ResponseEntity.ok(gestaciones);
        } catch (Exception e) {
            System.err.println("Error al obtener gestaciones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/activas")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Gestacion>> getGestacionesActivas(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            List<Gestacion> gestaciones = gestacionService.obtenerGestacionesActivas(user);
            return ResponseEntity.ok(gestaciones);
        } catch (Exception e) {
            System.err.println("Error al obtener gestaciones activas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/proximos-partos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Gestacion>> getProximosPartos(
            @RequestParam(required = false, defaultValue = "7") int dias,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            List<Gestacion> gestaciones = gestacionService.obtenerProximosPartos(user, dias);
            return ResponseEntity.ok(gestaciones);
        } catch (Exception e) {
            System.err.println("Error al obtener prÃ³ximos partos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Gestacion> getGestacionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            Optional<Gestacion> gestacion = gestacionService.obtenerGestacionPorId(id, user);
            return gestacion.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error al obtener gestaciÃ³n: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Gestacion> crearGestacion(
            @RequestBody Gestacion gestacionData,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            Gestacion gestacion = gestacionService.crearGestacion(gestacionData, user);
            return ResponseEntity.ok(gestacion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al crear gestaciÃ³n: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/aborto")
    public ResponseEntity<Gestacion> registrarAborto(
            @PathVariable Long id,
            @RequestBody Map<String, Object> abortoData,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            LocalDate fechaAborto = LocalDate.parse((String) abortoData.get("fechaAborto"));
            String causaAborto = (String) abortoData.get("causaAborto");
            Gestacion gestacion = gestacionService.registrarAborto(id, fechaAborto, causaAborto, user);
            return ResponseEntity.ok(gestacion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al registrar aborto: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<Gestacion> finalizarGestacion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            Gestacion gestacion = gestacionService.finalizarGestacion(id, user);
            return ResponseEntity.ok(gestacion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al finalizar gestaciÃ³n: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

