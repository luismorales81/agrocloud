package com.agrocloud.controller;

import com.agrocloud.porcinos.domain.MadreMuerte;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.MadreMuerteService;
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
@RequestMapping("/api/v1/porcinos/madres")
public class MadreMuerteController {

    @Autowired
    private MadreMuerteService madreMuerteService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @PostMapping("/{madreId}/muerte")
    public ResponseEntity<MadreMuerte> registrarMuerte(
            @PathVariable Long madreId,
            @RequestBody MadreMuerte muerteData,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            MadreMuerte muerte = madreMuerteService.registrarMuerte(madreId, muerteData, user);
            return ResponseEntity.ok(muerte);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al registrar muerte de madre: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{madreId}/muertes")
    public ResponseEntity<List<MadreMuerte>> obtenerMuertesPorMadre(
            @PathVariable Long madreId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            List<MadreMuerte> muertes = madreMuerteService.obtenerMuertesPorMadre(madreId, user);
            return ResponseEntity.ok(muertes);
        } catch (Exception e) {
            System.err.println("Error al obtener muertes de madre: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/muertes")
    public ResponseEntity<List<MadreMuerte>> obtenerTodasLasMuertes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String causa,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            List<MadreMuerte> muertes;
            if (causa != null && !causa.isEmpty()) {
                try {
                    MadreMuerte.CausaMuerteMadre causaEnum = MadreMuerte.CausaMuerteMadre.valueOf(causa.toUpperCase());
                    muertes = madreMuerteService.obtenerMuertesPorCausa(user, causaEnum);
                } catch (IllegalArgumentException e) {
                    muertes = madreMuerteService.obtenerTodasLasMuertes(user, fechaDesde, fechaHasta);
                }
            } else {
                muertes = madreMuerteService.obtenerTodasLasMuertes(user, fechaDesde, fechaHasta);
            }

            return ResponseEntity.ok(muertes);
        } catch (Exception e) {
            System.err.println("Error al obtener muertes de madres: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}







