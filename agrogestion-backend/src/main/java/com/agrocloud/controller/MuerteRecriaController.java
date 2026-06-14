package com.agrocloud.controller;

import com.agrocloud.porcinos.domain.MuerteRecria;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.MuerteRecriaService;
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
@RequestMapping("/api/v1/porcinos/recria")
public class MuerteRecriaController {

    @Autowired
    private MuerteRecriaService muerteRecriaService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @PostMapping("/{recriaId}/muertes")
    public ResponseEntity<MuerteRecria> registrarMuerte(
            @PathVariable Long recriaId,
            @RequestBody MuerteRecria muerteData,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            MuerteRecria muerte = muerteRecriaService.registrarMuerte(recriaId, muerteData, user);
            return ResponseEntity.ok(muerte);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.err.println("Error al registrar muerte en recrÃ­a: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{recriaId}/muertes")
    public ResponseEntity<List<MuerteRecria>> obtenerMuertesPorRecria(
            @PathVariable Long recriaId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            List<MuerteRecria> muertes = muerteRecriaService.obtenerMuertesPorRecria(recriaId, user);
            return ResponseEntity.ok(muertes);
        } catch (Exception e) {
            System.err.println("Error al obtener muertes de recrÃ­a: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/muertes")
    public ResponseEntity<List<MuerteRecria>> obtenerTodasLasMuertes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            List<MuerteRecria> muertes = muerteRecriaService.obtenerTodasLasMuertes(user, fechaDesde, fechaHasta);
            return ResponseEntity.ok(muertes);
        } catch (Exception e) {
            System.err.println("Error al obtener muertes de recrÃ­a: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}







