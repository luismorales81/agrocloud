package com.agrocloud.controller;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.dto.CalcularPreparacionRecetaResponse;
import com.agrocloud.cultivos.domain.Cultivo;
import com.agrocloud.core.inventory.domain.ComponenteInsumoCompuesto;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import com.agrocloud.porcinos.domain.RecetaAlimentacionPorEtapa;
import com.agrocloud.core.inventory.application.InsumoCompuestoService;
import com.agrocloud.core.inventory.application.PreparacionRecetaService;
import org.springframework.beans.factory.annotation.Qualifier;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/insumos-compuestos")
public class InsumoCompuestoController {

    @Autowired
    @Qualifier("insumoCompuestoServiceInventario")
    private InsumoCompuestoService insumoCompuestoService;

    @Autowired
    @Qualifier("preparacionRecetaServiceInventario")
    private PreparacionRecetaService preparacionRecetaService;

    @Autowired
    @Qualifier("userServiceCore")
    private UserService userService;

    /**
     * Obtener todos los insumos compuestos de la empresa
     */
    @GetMapping
    public ResponseEntity<List<InsumoCompuesto>> obtenerInsumosCompuestos(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            List<InsumoCompuesto> insumos = insumoCompuestoService.obtenerInsumosCompuestos(user);
            return ResponseEntity.ok(insumos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener un insumo compuesto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<InsumoCompuesto> obtenerInsumoCompuestoPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            Optional<InsumoCompuesto> insumo = insumoCompuestoService.obtenerInsumoCompuestoPorId(id, user);
            return insumo.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crear o actualizar un insumo compuesto
     */
    @PostMapping
    public ResponseEntity<?> guardarInsumoCompuesto(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            
            // Construir InsumoCompuesto desde el request
            InsumoCompuesto insumoCompuesto = new InsumoCompuesto();
            if (request.get("id") != null) {
                insumoCompuesto.setId(Long.valueOf(request.get("id").toString()));
            }
            insumoCompuesto.setNombre((String) request.get("nombre"));
            insumoCompuesto.setDescripcion((String) request.get("descripcion"));
            if (request.get("tipo") != null) {
                insumoCompuesto.setTipo(InsumoCompuesto.TipoInsumoCompuesto.valueOf(
                    request.get("tipo").toString()));
            }
            insumoCompuesto.setUnidadMedida((String) request.getOrDefault("unidadMedida", "kg"));
            if (request.get("rendimiento") != null) {
                insumoCompuesto.setRendimiento(new BigDecimal(request.get("rendimiento").toString()));
            }
            if (request.get("stockMinimo") != null) {
                insumoCompuesto.setStockMinimo(new BigDecimal(request.get("stockMinimo").toString()));
            }

            // Construir componentes
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> componentesData = (List<Map<String, Object>>) request.get("componentes");
            List<ComponenteInsumoCompuesto> componentes = construirComponentes(componentesData);

            InsumoCompuesto guardado = insumoCompuestoService.guardarInsumoCompuesto(
                insumoCompuesto, componentes, user);
            
            return ResponseEntity.ok(guardado);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al guardar el insumo compuesto: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Eliminar (desactivar) un insumo compuesto
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarInsumoCompuesto(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            insumoCompuestoService.eliminarInsumoCompuesto(id, user);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Recalcular costo unitario de un insumo compuesto
     */
    @PostMapping("/{id}/recalcular-costo")
    public ResponseEntity<?> recalcularCosto(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            Optional<InsumoCompuesto> insumoOpt = insumoCompuestoService.obtenerInsumoCompuestoPorId(id, user);
            if (insumoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            insumoCompuestoService.recalcularCostoUnitario(insumoOpt.get());
            return ResponseEntity.ok(insumoOpt.get());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Asociar receta a una etapa
     */
    @PostMapping("/{id}/asociar-etapa")
    public ResponseEntity<?> asociarRecetaAEtapa(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            
            RecetaAlimentacionPorEtapa.EtapaAlimentacion etapa = 
                RecetaAlimentacionPorEtapa.EtapaAlimentacion.valueOf(
                    request.get("etapa").toString());
            BigDecimal cantidadDiaria = new BigDecimal(request.get("cantidadDiariaPorAnimal").toString());

            RecetaAlimentacionPorEtapa receta = insumoCompuestoService.asociarRecetaAEtapa(
                id, etapa, cantidadDiaria, user);
            
            return ResponseEntity.ok(receta);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al asociar receta: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Obtener recetas por etapa
     */
    @GetMapping("/etapas/{etapa}")
    public ResponseEntity<List<RecetaAlimentacionPorEtapa>> obtenerRecetasPorEtapa(
            @PathVariable String etapa,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            RecetaAlimentacionPorEtapa.EtapaAlimentacion etapaEnum = 
                RecetaAlimentacionPorEtapa.EtapaAlimentacion.valueOf(etapa.toUpperCase());
            List<RecetaAlimentacionPorEtapa> recetas = 
                insumoCompuestoService.obtenerRecetasPorEtapa(etapaEnum, user);
            return ResponseEntity.ok(recetas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener receta por defecto para una etapa
     */
    @GetMapping("/etapas/{etapa}/por-defecto")
    public ResponseEntity<RecetaAlimentacionPorEtapa> obtenerRecetaPorDefecto(
            @PathVariable String etapa,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            RecetaAlimentacionPorEtapa.EtapaAlimentacion etapaEnum = 
                RecetaAlimentacionPorEtapa.EtapaAlimentacion.valueOf(etapa.toUpperCase());
            Optional<RecetaAlimentacionPorEtapa> receta = 
                insumoCompuestoService.obtenerRecetaPorDefecto(etapaEnum, user);
            return receta.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Calcular preparaciÃ³n de receta sin descontar del inventario
     * Muestra cuÃ¡nto se necesita de cada componente por kg de receta,
     * el stock disponible, y el mÃ¡ximo preparable
     */
    @GetMapping("/{id}/calcular-preparacion")
    public ResponseEntity<?> calcularPreparacion(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal cantidad,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            
            CalcularPreparacionRecetaResponse response = preparacionRecetaService.calcularPreparacionReceta(
                id, cantidad, user);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al calcular preparaciÃ³n: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Preparar una receta (descontar ingredientes del inventario)
     * Este endpoint permite preparar una cantidad especÃ­fica de receta,
     * descontando automÃ¡ticamente los ingredientes del inventario compartido
     */
    @PostMapping("/{id}/preparar")
    public ResponseEntity<?> prepararReceta(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            
            BigDecimal cantidadPreparada = new BigDecimal(request.get("cantidadPreparada").toString());
            java.time.LocalDate fechaPreparacion = request.get("fechaPreparacion") != null ?
                java.time.LocalDate.parse(request.get("fechaPreparacion").toString()) : null;

            InsumoCompuesto receta = preparacionRecetaService.prepararReceta(
                id, cantidadPreparada, fechaPreparacion, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", String.format("Receta '%s' preparada exitosamente. Cantidad: %s %s",
                receta.getNombre(), cantidadPreparada, receta.getUnidadMedida()));
            response.put("receta", receta);
            response.put("cantidadPreparada", cantidadPreparada);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al preparar receta: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Helper para construir componentes desde el request
     */
    private List<ComponenteInsumoCompuesto> construirComponentes(List<Map<String, Object>> componentesData) {
        return componentesData.stream().map(data -> {
            ComponenteInsumoCompuesto componente = new ComponenteInsumoCompuesto();
            
            // Determinar tipo y origen - crear objetos temporales con IDs para que el servicio los resuelva
            String tipoComponente = (String) data.get("tipoComponente");
            if ("INSUMO".equals(tipoComponente) && data.get("insumoId") != null) {
                componente.setTipoComponente(ComponenteInsumoCompuesto.TipoComponente.INSUMO);
                Insumo insumo = new Insumo();
                insumo.setId(Long.valueOf(data.get("insumoId").toString()));
                componente.setInsumo(insumo);
            } else if ("GRANO_PROPIO".equals(tipoComponente) && data.get("cultivoId") != null) {
                componente.setTipoComponente(ComponenteInsumoCompuesto.TipoComponente.GRANO_PROPIO);
                Cultivo cultivo = new Cultivo();
                cultivo.setId(Long.valueOf(data.get("cultivoId").toString()));
                componente.setCultivo(cultivo);
            } else if ("INSUMO_COMPUESTO".equals(tipoComponente) && data.get("insumoCompuestoId") != null) {
                componente.setTipoComponente(ComponenteInsumoCompuesto.TipoComponente.INSUMO_COMPUESTO);
                InsumoCompuesto insumoComp = new InsumoCompuesto();
                insumoComp.setId(Long.valueOf(data.get("insumoCompuestoId").toString()));
                componente.setInsumoCompuestoPadre(insumoComp);
            }

            if (data.get("porcentaje") != null) {
                componente.setPorcentaje(new BigDecimal(data.get("porcentaje").toString()));
            }
            if (data.get("cantidadFija") != null) {
                componente.setCantidadFija(new BigDecimal(data.get("cantidadFija").toString()));
            }
            if (data.get("unidadMedida") != null) {
                componente.setUnidadMedida((String) data.get("unidadMedida"));
            }
            if (data.get("ordenMezcla") != null) {
                componente.setOrdenMezcla(Integer.valueOf(data.get("ordenMezcla").toString()));
            }
            if (data.get("observaciones") != null) {
                componente.setObservaciones((String) data.get("observaciones"));
            }

            return componente;
        }).toList();
    }
}





