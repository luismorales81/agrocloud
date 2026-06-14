package com.agrocloud.controller;

import com.agrocloud.porcinos.domain.ConfiguracionPorcino;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.ConfiguracionPorcinoService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/porcinos/configuraciones")
public class ConfiguracionPorcinoController {

    @Autowired
    private ConfiguracionPorcinoService configuracionService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<ConfiguracionPorcino>> obtenerTodas(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            List<ConfiguracionPorcino> configs = configuracionService.obtenerTodas(user);
            return ResponseEntity.ok(configs);
        } catch (Exception e) {
            System.err.println("Error al obtener configuraciones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/categoria/{categoria}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ConfiguracionPorcino>> obtenerPorCategoria(
            @PathVariable String categoria,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            List<ConfiguracionPorcino> configs = configuracionService.obtenerPorCategoria(categoria, user);
            return ResponseEntity.ok(configs);
        } catch (Exception e) {
            System.err.println("Error al obtener configuraciones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{clave}")
    @Transactional(readOnly = true)
    public ResponseEntity<ConfiguracionPorcino> obtenerPorClave(
            @PathVariable String clave,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            return configuracionService.obtenerConfiguracion(clave, user)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error al obtener configuraciÃ³n: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<ConfiguracionPorcino> guardarConfiguracion(
            @RequestBody Map<String, Object> configData,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            String clave = (String) configData.get("clave");
            String valor = (String) configData.get("valor");
            String tipoStr = (String) configData.get("tipo");
            String categoria = (String) configData.getOrDefault("categoria", "GENERAL");
            String descripcion = (String) configData.getOrDefault("descripcion", "");

            ConfiguracionPorcino.TipoConfig tipo = ConfiguracionPorcino.TipoConfig.valueOf(tipoStr);

            ConfiguracionPorcino config = configuracionService.guardarConfiguracion(
                clave, valor, tipo, categoria, descripcion, user);
            return ResponseEntity.ok(config);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al guardar configuraciÃ³n: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}







