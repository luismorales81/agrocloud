package com.agrocloud.controller;

import com.agrocloud.porcinos.domain.Destete;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.DesteteService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/porcinos/destetes")
public class DesteteController {

    @Autowired
    private DesteteService desteteService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @PostMapping
    public ResponseEntity<Destete> registrarDestete(
            @RequestBody Map<String, Object> requestData,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long partoId = Long.valueOf(requestData.get("partoId").toString());
        Destete desteteData = new Destete();
        // Mapear los datos del request al objeto Destete
        if (requestData.get("fechaDestete") != null) {
            desteteData.setFechaDestete(java.time.LocalDate.parse(requestData.get("fechaDestete").toString()));
        }
        if (requestData.get("cantidadDestetados") != null) {
            desteteData.setCantidadDestetados(Integer.valueOf(requestData.get("cantidadDestetados").toString()));
        }
        if (requestData.get("pesoPromedioDestete") != null) {
            desteteData.setPesoPromedioDestete(new java.math.BigDecimal(requestData.get("pesoPromedioDestete").toString()));
        }
        if (requestData.get("diasLactancia") != null) {
            desteteData.setDiasLactancia(Integer.valueOf(requestData.get("diasLactancia").toString()));
        }
        if (requestData.get("observaciones") != null) {
            desteteData.setObservaciones(requestData.get("observaciones").toString());
        }
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Destete destete = desteteService.registrarDestete(partoId, desteteData, user);
            return ResponseEntity.ok(destete);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al registrar destete: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/por-parto/{partoId}")
    public ResponseEntity<Destete> obtenerDestetePorParto(
            @PathVariable Long partoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Destete> destete = desteteService.obtenerDestetePorParto(partoId, user);
            return destete.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error al obtener destete: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Destete>> obtenerTodosLosDestetes(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            List<Destete> destetes = desteteService.obtenerTodosLosDestetes(user);
            return ResponseEntity.ok(destetes);
        } catch (Exception e) {
            System.err.println("Error al obtener destetes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Destete> obtenerDestete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Destete> destete = desteteService.obtenerDestetePorId(id, user);
            return destete.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error al obtener destete: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}







