package com.agrocloud.controller;

import com.agrocloud.porcinos.domain.MuerteLechon;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.MuerteLechonService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/porcinos/partos")
public class MuerteLechonController {

    @Autowired
    private MuerteLechonService muerteLechonService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @PostMapping("/{partoId}/muertes")
    public ResponseEntity<MuerteLechon> registrarMuerte(
            @PathVariable Long partoId,
            @RequestBody MuerteLechon muerteData,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            MuerteLechon muerte = muerteLechonService.registrarMuerte(partoId, muerteData, user);
            return ResponseEntity.ok(muerte);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.err.println("Error al registrar muerte de lechÃ³n: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{partoId}/muertes")
    public ResponseEntity<List<MuerteLechon>> obtenerMuertesPorParto(
            @PathVariable Long partoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            List<MuerteLechon> muertes = muerteLechonService.obtenerMuertesPorParto(partoId, user);
            return ResponseEntity.ok(muertes);
        } catch (Exception e) {
            System.err.println("Error al obtener muertes de lechones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/lechones/muertes")
    public ResponseEntity<List<MuerteLechon>> obtenerTodasLasMuertes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Long madreId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            List<MuerteLechon> muertes;
            if (madreId != null) {
                muertes = muerteLechonService.obtenerMuertesPorMadre(madreId, user);
            } else {
                muertes = muerteLechonService.obtenerTodasLasMuertes(user, fechaDesde, fechaHasta);
            }

            return ResponseEntity.ok(muertes);
        } catch (Exception e) {
            System.err.println("Error al obtener muertes de lechones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}







