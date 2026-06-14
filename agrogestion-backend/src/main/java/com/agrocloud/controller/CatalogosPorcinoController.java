package com.agrocloud.controller;
import com.agrocloud.core.domain.User;

import com.agrocloud.core.application.port.LoteMinimoDTO;
import com.agrocloud.core.application.port.LoteParaPorcinosQuery;
import com.agrocloud.porcinos.domain.CausaMortalidadPorcino;
import com.agrocloud.porcinos.domain.CausaMomificado;
import com.agrocloud.porcinos.domain.CausaNacidoMuerto;
import com.agrocloud.porcinos.domain.EsquemaSanitarioPorcino;
import com.agrocloud.porcinos.domain.MotivoBajaPorcino;
import com.agrocloud.porcinos.domain.ProveedorGenetica;
import com.agrocloud.porcinos.domain.RazaPorcino;
import com.agrocloud.porcinos.domain.TipoEventoSanitario;
import com.agrocloud.porcinos.domain.TipoParto;
import com.agrocloud.porcinos.domain.TipoServicioPorcino;
import com.agrocloud.porcinos.domain.UbicacionInterna;
import com.agrocloud.porcinos.application.CatalogosPorcinoService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar catÃ¡logos configurables del mÃ³dulo porcino
 */
@RestController
@RequestMapping("/api/v1/porcinos/catalogos")
public class CatalogosPorcinoController {

    @Autowired
    private CatalogosPorcinoService catalogosService;

    @Autowired
    private UserService userService;

    @Autowired
    private LoteParaPorcinosQuery loteParaPorcinosQuery;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    private Long obtenerUserId(UserDetails userDetails) {
        User user = obtenerUsuario(userDetails);
        return user != null ? user.getId() : null;
    }

    // ============================================================================
    // LOTES PORCINOS (corrales) - solo lotes de uso porcino para recrÃ­a/ingreso
    // ============================================================================

    @GetMapping("/lotes")
    @Transactional(readOnly = true)
    public ResponseEntity<List<LoteMinimoDTO>> listarLotesPorcinos(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null || user.getEmpresa() == null) return ResponseEntity.badRequest().build();
            Long empresaId = user.getEmpresa().getId();
            return ResponseEntity.ok(loteParaPorcinosQuery.listarLotesPorcinosPorEmpresa(empresaId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================================
    // RAZAS
    // ============================================================================

    @GetMapping("/razas")
    @Transactional(readOnly = true)
    public ResponseEntity<List<RazaPorcino>> obtenerRazas(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(catalogosService.obtenerRazas(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/razas/tipo/{tipo}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<RazaPorcino>> obtenerRazasPorTipo(
            @PathVariable String tipo,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            RazaPorcino.TipoRaza tipoRaza = RazaPorcino.TipoRaza.valueOf(tipo);
            return ResponseEntity.ok(catalogosService.obtenerRazasPorTipo(userId, tipoRaza));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/razas")
    public ResponseEntity<RazaPorcino> guardarRaza(
            @RequestBody RazaPorcino raza,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(catalogosService.guardarRaza(raza, userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/razas/{id}")
    public ResponseEntity<Void> eliminarRaza(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            catalogosService.eliminarRaza(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================================
    // TIPOS DE ALIMENTO (ELIMINADO - REDUNDANTE)
    // ============================================================================
    // NOTA: TipoAlimentoPorcino fue eliminado porque es redundante con:
    // - Recetas: InsumoCompuesto (tipo RACION) asociado a etapas mediante RecetaAlimentacionPorEtapa
    // - Balanceados comerciales: Insumo (tabla cultivo_insumos)
    // - Granos propios: Cultivo + InventarioGrano
    // TipoAlimentoPorcino solo existÃ­a como catÃ¡logo sin integraciÃ³n funcional
    // en el sistema de consumo actual (ConsumoDiarioAutomatico)

    // ============================================================================
    // TIPOS DE SERVICIO
    // ============================================================================

    @GetMapping("/tipos-servicio")
    @Transactional(readOnly = true)
    public ResponseEntity<List<TipoServicioPorcino>> obtenerTiposServicio(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(catalogosService.obtenerTiposServicio(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/tipos-servicio")
    public ResponseEntity<TipoServicioPorcino> guardarTipoServicio(
            @RequestBody TipoServicioPorcino tipoServicio,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(catalogosService.guardarTipoServicio(tipoServicio, userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/tipos-servicio/{id}")
    public ResponseEntity<Void> eliminarTipoServicio(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            catalogosService.eliminarTipoServicio(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================================
    // CAUSAS DE MORTALIDAD
    // ============================================================================

    @GetMapping("/causas-mortalidad")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CausaMortalidadPorcino>> obtenerCausasMortalidad(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(catalogosService.obtenerCausasMortalidad(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/causas-mortalidad")
    public ResponseEntity<CausaMortalidadPorcino> guardarCausaMortalidad(
            @RequestBody CausaMortalidadPorcino causa,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(catalogosService.guardarCausaMortalidad(causa, userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/causas-mortalidad/{id}")
    public ResponseEntity<Void> eliminarCausaMortalidad(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            catalogosService.eliminarCausaMortalidad(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================================
    // MOTIVOS DE BAJA
    // ============================================================================

    @GetMapping("/motivos-baja")
    @Transactional(readOnly = true)
    public ResponseEntity<List<MotivoBajaPorcino>> obtenerMotivosBaja(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(catalogosService.obtenerMotivosBaja(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/motivos-baja")
    public ResponseEntity<MotivoBajaPorcino> guardarMotivoBaja(
            @RequestBody MotivoBajaPorcino motivo,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(catalogosService.guardarMotivoBaja(motivo, userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/motivos-baja/{id}")
    public ResponseEntity<Void> eliminarMotivoBaja(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            catalogosService.eliminarMotivoBaja(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================================
    // ESQUEMAS SANITARIOS
    // ============================================================================

    @GetMapping("/esquemas-sanitarios")
    @Transactional(readOnly = true)
    public ResponseEntity<List<EsquemaSanitarioPorcino>> obtenerEsquemasSanitarios(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(catalogosService.obtenerEsquemasSanitarios(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/esquemas-sanitarios")
    public ResponseEntity<EsquemaSanitarioPorcino> guardarEsquemaSanitario(
            @RequestBody EsquemaSanitarioPorcino esquema,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(catalogosService.guardarEsquemaSanitario(esquema, userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/esquemas-sanitarios/{id}")
    public ResponseEntity<Void> eliminarEsquemaSanitario(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            catalogosService.eliminarEsquemaSanitario(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================================
    // TIPOS DE PARTO
    // ============================================================================

    @GetMapping("/tipos-parto")
    @Transactional(readOnly = true)
    public ResponseEntity<List<TipoParto>> obtenerTiposParto(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(catalogosService.obtenerTiposParto(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/tipos-parto")
    public ResponseEntity<TipoParto> guardarTipoParto(
            @RequestBody TipoParto tipoParto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(catalogosService.guardarTipoParto(tipoParto, userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/tipos-parto/{id}")
    public ResponseEntity<Void> eliminarTipoParto(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            catalogosService.eliminarTipoParto(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================================
    // TIPOS DE EVENTO SANITARIO
    // ============================================================================

    @GetMapping("/tipos-evento-sanitario")
    @Transactional(readOnly = true)
    public ResponseEntity<List<TipoEventoSanitario>> obtenerTiposEventoSanitario(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(catalogosService.obtenerTiposEventoSanitario(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tipos-evento-sanitario/categoria/{categoria}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<TipoEventoSanitario>> obtenerTiposEventoSanitarioPorCategoria(
            @PathVariable String categoria,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            TipoEventoSanitario.CategoriaEvento categoriaEnum = TipoEventoSanitario.CategoriaEvento.valueOf(categoria.toUpperCase());
            return ResponseEntity.ok(catalogosService.obtenerTiposEventoSanitarioPorCategoria(userId, categoriaEnum));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/tipos-evento-sanitario")
    public ResponseEntity<TipoEventoSanitario> guardarTipoEventoSanitario(
            @RequestBody TipoEventoSanitario tipo,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(catalogosService.guardarTipoEventoSanitario(tipo, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/tipos-evento-sanitario/{id}")
    public ResponseEntity<Void> eliminarTipoEventoSanitario(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            catalogosService.eliminarTipoEventoSanitario(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================================
    // UBICACIONES INTERNAS
    // ============================================================================

    @GetMapping("/ubicaciones-internas")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UbicacionInterna>> obtenerUbicacionesInternas(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) {
                System.err.println("Error: userId es null en obtenerUbicacionesInternas");
                return ResponseEntity.badRequest().build();
            }
            List<UbicacionInterna> ubicaciones = catalogosService.obtenerUbicacionesInternas(userId);
            return ResponseEntity.ok(ubicaciones);
        } catch (Exception e) {
            System.err.println("Error al obtener ubicaciones internas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ubicaciones-internas/nivel/{nivel}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UbicacionInterna>> obtenerUbicacionesPorNivel(
            @PathVariable String nivel,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) return ResponseEntity.badRequest().build();
            UbicacionInterna.NivelUbicacion nivelEnum = UbicacionInterna.NivelUbicacion.valueOf(nivel);
            return ResponseEntity.ok(catalogosService.obtenerUbicacionesPorNivel(userId, nivelEnum));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ubicaciones-internas/padre/{padreId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UbicacionInterna>> obtenerUbicacionesHijas(
            @PathVariable Long padreId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) {
                System.err.println("Error: userId es null en obtenerUbicacionesHijas");
                return ResponseEntity.badRequest().build();
            }
            List<UbicacionInterna> ubicaciones = catalogosService.obtenerUbicacionesHijas(padreId, userId);
            return ResponseEntity.ok(ubicaciones);
        } catch (IllegalArgumentException e) {
            System.err.println("Error de validaciÃ³n en obtenerUbicacionesHijas: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al obtener ubicaciones hijas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/ubicaciones-internas")
    public ResponseEntity<UbicacionInterna> guardarUbicacionInterna(
            @RequestBody UbicacionInterna ubicacion,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) {
                System.err.println("Error: userId es null en guardarUbicacionInterna");
                return ResponseEntity.badRequest().build();
            }
            UbicacionInterna guardada = catalogosService.guardarUbicacionInterna(ubicacion, userId);
            return ResponseEntity.ok(guardada);
        } catch (IllegalArgumentException e) {
            System.err.println("Error de validaciÃ³n en guardarUbicacionInterna: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al guardar ubicaciÃ³n interna: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/ubicaciones-internas/{id}")
    public ResponseEntity<Void> eliminarUbicacionInterna(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = obtenerUserId(userDetails);
            if (userId == null) {
                System.err.println("Error: userId es null en eliminarUbicacionInterna");
                return ResponseEntity.badRequest().build();
            }
            catalogosService.eliminarUbicacionInterna(id, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            System.err.println("Error de validaciÃ³n en eliminarUbicacionInterna: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al eliminar ubicaciÃ³n interna: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

