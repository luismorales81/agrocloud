package com.agrocloud.controller;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.core.domain.Recordatorio;
import com.agrocloud.core.domain.User;
import com.agrocloud.cultivos.application.LaborService;
import com.agrocloud.cultivos.application.PlotService;
import com.agrocloud.core.application.RecordatorioService;
import com.agrocloud.porcinos.application.AlertasPorcinosService;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.application.CalendarioTareasRecurrentesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/calendario")
public class CalendarioController {

    @Autowired
    @Qualifier("laborServicioCultivos")
    private LaborService laborService;

    @Autowired
    @Qualifier("plotServicioCultivos")
    private PlotService plotService;

    @Autowired
    private RecordatorioService recordatorioService;

    @Autowired
    private AlertasPorcinosService alertasPorcinosService;

    @Autowired
    private UserService userService;

    @Autowired
    private CalendarioTareasRecurrentesService calendarioTareasRecurrentesService;

    /**
     * Helper method para obtener el usuario desde UserDetails
     */
    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    /**
     * Obtener todos los eventos del calendario (labores, fechas de cosecha y recordatorios)
     * para un rango de fechas especÃ­fico
     */
    @GetMapping("/eventos")
    public ResponseEntity<Map<String, Object>> getEventosCalendario(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user;
        try {
            user = obtenerUsuario(userDetails);
        } catch (Exception e) {
            System.err.println("Error al obtener usuario en calendario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error interno",
                    "mensaje", "No se pudo obtener el usuario.",
                    "labores", List.of(),
                    "cosechas", List.of(),
                    "recordatorios", List.of(),
                    "todos", List.of()));
        }
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        final LocalDate fechaInicioFinal = fechaInicio != null ? fechaInicio : LocalDate.now().withDayOfMonth(1);
        final LocalDate fechaFinFinal = fechaFin != null ? fechaFin : fechaInicioFinal.plusMonths(1).minusDays(1);

        Map<String, Object> eventos = new HashMap<>();
        List<Map<String, Object>> laboresCalendario = new ArrayList<>();
        List<Map<String, Object>> cosechasCalendario = new ArrayList<>();
        List<Map<String, Object>> recordatoriosCalendario = new ArrayList<>();
        List<Map<String, Object>> tareasRecurrentesCalendario = new ArrayList<>();

        // Spec SDD: calendario = vista derivada de labores (y cosechas). Eventos = proyecciÃ³n de labores.
        try {
            List<Labor> labores = laborService.getLaboresCalendarioPorRango(user, fechaInicioFinal, fechaFinFinal);
            laboresCalendario = labores.stream()
                    .map(labor -> {
                        Map<String, Object> evento = new HashMap<>();
                        evento.put("id", "labor_" + labor.getId());
                        evento.put("tipo", "LABOR");
                        evento.put("titulo", labor.getTipoLabor() != null ? labor.getTipoLabor().toString() : "Labor");
                        evento.put("descripcion", labor.getDescripcion());
                        evento.put("fecha", labor.getFechaInicio().toString());
                        evento.put("fechaFin", labor.getFechaFin() != null ? labor.getFechaFin().toString() : null);
                        evento.put("estado", labor.getEstado() != null ? labor.getEstado().toString() : "PLANIFICADA");
                        evento.put("fechaRealizacion", labor.getFechaRealizacion() != null ? labor.getFechaRealizacion().toString() : null);
                        evento.put("overdue", labor.isVencida());
                        evento.put("laborId", labor.getId());
                        evento.put("loteId", labor.getLote() != null ? labor.getLote().getId() : null);
                        evento.put("loteNombre", labor.getLote() != null ? labor.getLote().getNombre() : null);
                        evento.put("responsable", labor.getResponsable());
                        evento.put("usuarioId", labor.getUsuario() != null ? labor.getUsuario().getId() : null);
                        evento.put("usuarioNombre", labor.getUsuario() != null ?
                                (labor.getUsuario().getFirstName() + " " + labor.getUsuario().getLastName()) : null);
                        return evento;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error al obtener labores para calendario: " + e.getMessage());
        }

        try {
            List<Plot> lotes = plotService.getLotesCultivoConCosechaEnRango(user, fechaInicioFinal, fechaFinFinal);
            cosechasCalendario = lotes.stream()
                    .map(lote -> {
                        Map<String, Object> evento = new HashMap<>();
                        evento.put("id", "cosecha_" + lote.getId());
                        evento.put("tipo", "COSECHA");
                        evento.put("titulo", "Cosecha: " + (lote.getCultivoActual() != null ? lote.getCultivoActual() : "Cultivo"));
                        evento.put("descripcion", "Fecha estimada de cosecha para " + lote.getNombre());
                        evento.put("fecha", lote.getFechaCosechaEsperada().toString());
                        evento.put("loteId", lote.getId());
                        evento.put("loteNombre", lote.getNombre());
                        evento.put("cultivo", lote.getCultivoActual());
                        evento.put("superficie", lote.getAreaHectareas());
                        return evento;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error al obtener cosechas para calendario: " + e.getMessage());
        }

        // Recordatorios: excluir los vinculados a labores (spec SDD: calendario deriva de labores, no de recordatorios de labor)
        try {
            List<Recordatorio> recordatorios = recordatorioService.getRecordatoriosByUsuarioAndRango(
                    user.getId(), fechaInicioFinal, fechaFinFinal);
            recordatoriosCalendario = recordatorios.stream()
                    .filter(r -> {
                        if (r.getLaborId() != null) return false;
                        boolean esPorcino = r.getPartoId() != null || r.getServicioId() != null || r.getGestacionId() != null;
                        boolean esCalendarioHuevos = r.getLoteAvicolaHuevoId() != null;
                        return !esPorcino && !esCalendarioHuevos;
                    })
                    .map(recordatorio -> {
                        Map<String, Object> evento = new HashMap<>();
                        evento.put("id", "recordatorio_" + recordatorio.getId());
                        evento.put("tipo", "RECORDATORIO");
                        evento.put("titulo", recordatorio.getTitulo());
                        evento.put("descripcion", recordatorio.getDescripcion());
                        evento.put("fecha", recordatorio.getFecha().toString());
                        evento.put("tipoRecordatorio", recordatorio.getTipo() != null ? recordatorio.getTipo().toString() : "GENERAL");
                        evento.put("completado", recordatorio.getCompletado());
                        evento.put("recordatorioId", recordatorio.getId());
                        evento.put("laborId", recordatorio.getLaborId());
                        evento.put("loteId", recordatorio.getLoteId());
                        evento.put("loteAvicolaHuevoId", recordatorio.getLoteAvicolaHuevoId());
                        return evento;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error al obtener recordatorios para calendario: " + e.getMessage());
        }

        try {
            tareasRecurrentesCalendario = calendarioTareasRecurrentesService.construirEventosEnRango(
                    user.getId(), fechaInicioFinal, fechaFinFinal);
        } catch (Exception e) {
            System.err.println("Error al obtener tareas recurrentes para calendario: " + e.getMessage());
        }

        List<Map<String, Object>> todosEventos = new ArrayList<>();
        todosEventos.addAll(laboresCalendario);
        todosEventos.addAll(cosechasCalendario);
        todosEventos.addAll(recordatoriosCalendario);
        todosEventos.addAll(tareasRecurrentesCalendario);

        eventos.put("labores", laboresCalendario);
        eventos.put("cosechas", cosechasCalendario);
        eventos.put("recordatorios", recordatoriosCalendario);
        eventos.put("tareasRecurrentes", tareasRecurrentesCalendario);
        eventos.put("todos", todosEventos);
        eventos.put("fechaInicio", fechaInicioFinal.toString());
        eventos.put("fechaFin", fechaFinFinal.toString());

        return ResponseEntity.ok(eventos);
    }

    /**
     * Calendario del mÃ³dulo avÃ­cola huevos: recordatorios por lote de postura y tareas recurrentes con Ã¡mbito huevos.
     */
    @GetMapping("/avicola-huevos")
    public ResponseEntity<Map<String, Object>> getEventosAvicolaHuevos(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = obtenerUsuario(userDetails);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        final LocalDate fechaInicioFinal = fechaInicio != null ? fechaInicio : LocalDate.now().withDayOfMonth(1);
        final LocalDate fechaFinFinal = fechaFin != null ? fechaFin : fechaInicioFinal.plusMonths(1).minusDays(1);

        List<Map<String, Object>> recordatoriosCalendario = new ArrayList<>();
        try {
            List<Recordatorio> recordatorios = recordatorioService.getRecordatoriosByUsuarioAndRango(
                    user.getId(), fechaInicioFinal, fechaFinFinal);
            recordatoriosCalendario = recordatorios.stream()
                    .filter(r -> r.getLoteAvicolaHuevoId() != null)
                    .map(recordatorio -> {
                        Map<String, Object> evento = new HashMap<>();
                        evento.put("id", "recordatorio_" + recordatorio.getId());
                        evento.put("tipo", "RECORDATORIO");
                        evento.put("titulo", recordatorio.getTitulo());
                        evento.put("descripcion", recordatorio.getDescripcion());
                        evento.put("fecha", recordatorio.getFecha().toString());
                        evento.put("tipoRecordatorio", recordatorio.getTipo() != null ? recordatorio.getTipo().toString() : "GENERAL");
                        evento.put("completado", recordatorio.getCompletado());
                        evento.put("recordatorioId", recordatorio.getId());
                        evento.put("laborId", recordatorio.getLaborId());
                        evento.put("loteId", recordatorio.getLoteId());
                        evento.put("loteAvicolaHuevoId", recordatorio.getLoteAvicolaHuevoId());
                        return evento;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error al obtener recordatorios calendario huevos: " + e.getMessage());
        }

        List<Map<String, Object>> tareasRecurrentesCalendario = new ArrayList<>();
        try {
            tareasRecurrentesCalendario = calendarioTareasRecurrentesService.construirEventosEnRangoCalendarioHuevos(
                    user.getId(), fechaInicioFinal, fechaFinFinal);
        } catch (Exception e) {
            System.err.println("Error al obtener tareas recurrentes huevos: " + e.getMessage());
        }

        List<Map<String, Object>> todosEventos = new ArrayList<>();
        todosEventos.addAll(recordatoriosCalendario);
        todosEventos.addAll(tareasRecurrentesCalendario);
        todosEventos.sort((a, b) -> {
            String fa = (String) a.get("fecha");
            String fb = (String) b.get("fecha");
            if (fa == null || fb == null) {
                return 0;
            }
            return fa.compareTo(fb);
        });

        Map<String, Object> eventos = new HashMap<>();
        eventos.put("recordatorios", recordatoriosCalendario);
        eventos.put("tareasRecurrentes", tareasRecurrentesCalendario);
        eventos.put("todos", todosEventos);
        eventos.put("fechaInicio", fechaInicioFinal.toString());
        eventos.put("fechaFin", fechaFinFinal.toString());
        return ResponseEntity.ok(eventos);
    }

    /**
     * Calendario del módulo feedlot: tareas recurrentes con ámbito FEEDLOT.
     */
    @GetMapping("/feedlot")
    public ResponseEntity<Map<String, Object>> getEventosFeedlot(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = obtenerUsuario(userDetails);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        final LocalDate fechaInicioFinal = fechaInicio != null ? fechaInicio : LocalDate.now().withDayOfMonth(1);
        final LocalDate fechaFinFinal = fechaFin != null ? fechaFin : fechaInicioFinal.plusMonths(1).minusDays(1);

        List<Map<String, Object>> tareasRecurrentesCalendario = new ArrayList<>();
        try {
            tareasRecurrentesCalendario = calendarioTareasRecurrentesService.construirEventosEnRangoCalendarioFeedlot(
                    user.getId(), fechaInicioFinal, fechaFinFinal);
        } catch (Exception e) {
            System.err.println("Error al obtener tareas recurrentes feedlot: " + e.getMessage());
        }

        List<Map<String, Object>> todosEventos = new ArrayList<>(tareasRecurrentesCalendario);
        todosEventos.sort((a, b) -> {
            String fa = (String) a.get("fecha");
            String fb = (String) b.get("fecha");
            if (fa == null || fb == null) {
                return 0;
            }
            return fa.compareTo(fb);
        });

        Map<String, Object> eventos = new HashMap<>();
        eventos.put("tareasRecurrentes", tareasRecurrentesCalendario);
        eventos.put("todos", todosEventos);
        eventos.put("fechaInicio", fechaInicioFinal.toString());
        eventos.put("fechaFin", fechaFinFinal.toString());
        return ResponseEntity.ok(eventos);
    }

    /**
     * Obtener eventos del calendario especÃ­ficos del mÃ³dulo Porcinos
     * Incluye: Partos, EcografÃ­as, Destetes, Control de Celo, Gestaciones Vencidas
     */
    @GetMapping("/porcinos")
    public ResponseEntity<Map<String, Object>> getEventosPorcinos(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            // Si no se proporcionan fechas, usar el mes actual
            final LocalDate fechaInicioFinal = fechaInicio != null ? fechaInicio : LocalDate.now().withDayOfMonth(1);
            final LocalDate fechaFinFinal = fechaFin != null ? fechaFin : fechaInicioFinal.plusMonths(1).minusDays(1);

            // Obtener eventos de porcinos (ya incluye recordatorios de porcinos convertidos)
            List<Map<String, Object>> eventosPorcinos = alertasPorcinosService.obtenerEventosCalendario(
                user, fechaInicioFinal, fechaFinFinal);

            // Crear un Set de IDs de recordatorios ya incluidos en eventosPorcinos para evitar duplicados
            Set<String> idsRecordatoriosIncluidos = eventosPorcinos.stream()
                    .filter(e -> e.get("recordatorioId") != null)
                    .map(e -> "recordatorio_" + e.get("recordatorioId"))
                    .collect(Collectors.toSet());

            // Obtener solo recordatorios generales que NO sean de porcinos (para evitar duplicados)
            // Los recordatorios de porcinos ya estÃ¡n incluidos en eventosPorcinos
            List<Recordatorio> recordatorios = recordatorioService.getRecordatoriosByUsuarioAndRango(
                    user.getId(), fechaInicioFinal, fechaFinFinal);
            List<Map<String, Object>> recordatoriosCalendario = recordatorios.stream()
                    .filter(r -> {
                        // Excluir recordatorios de porcinos (ya estÃ¡n en eventosPorcinos)
                        // Excluir recordatorios que ya fueron incluidos
                        if (idsRecordatoriosIncluidos.contains("recordatorio_" + r.getId())) {
                            return false;
                        }
                        
                        // Incluir solo recordatorios generales que NO sean de porcinos
                        String tipo = r.getTipo() != null ? r.getTipo().toString() : "";
                        boolean esPorcino = r.getPartoId() != null || r.getServicioId() != null || 
                                           r.getGestacionId() != null;
                        
                        // Solo incluir recordatorios GENERALES (no de porcinos)
                        return tipo.equals("GENERAL") && !esPorcino;
                    })
                    .map(recordatorio -> {
                        Map<String, Object> evento = new HashMap<>();
                        evento.put("id", "recordatorio_" + recordatorio.getId());
                        evento.put("tipo", "RECORDATORIO");
                        evento.put("titulo", "ðŸ“Œ " + recordatorio.getTitulo());
                        evento.put("descripcion", recordatorio.getDescripcion());
                        evento.put("fecha", recordatorio.getFecha().toString());
                        evento.put("tipoRecordatorio", recordatorio.getTipo() != null ? recordatorio.getTipo().toString() : "GENERAL");
                        evento.put("completado", recordatorio.getCompletado());
                        evento.put("recordatorioId", recordatorio.getId());
                        evento.put("prioridad", recordatorio.getCompletado() ? "COMPLETADO" : "MEDIA");
                        evento.put("color", recordatorio.getCompletado() ? "#6b7280" : "#ec4899");
                        return evento;
                    })
                    .collect(Collectors.toList());

            // Combinar todos los eventos y eliminar duplicados por ID
            Set<String> idsUnicos = new HashSet<>();
            List<Map<String, Object>> todosEventos = new ArrayList<>();
            
            // Agregar eventos de porcinos primero
            for (Map<String, Object> evento : eventosPorcinos) {
                String id = (String) evento.get("id");
                if (id != null && !idsUnicos.contains(id)) {
                    idsUnicos.add(id);
                    todosEventos.add(evento);
                }
            }
            
            // Agregar recordatorios generales (no duplicados)
            for (Map<String, Object> evento : recordatoriosCalendario) {
                String id = (String) evento.get("id");
                if (id != null && !idsUnicos.contains(id)) {
                    idsUnicos.add(id);
                    todosEventos.add(evento);
                }
            }

            // Ordenar por fecha
            todosEventos.sort((a, b) -> {
                String fechaA = (String) a.get("fecha");
                String fechaB = (String) b.get("fecha");
                return fechaA.compareTo(fechaB);
            });

            Map<String, Object> response = new HashMap<>();
            response.put("eventos", todosEventos);
            response.put("partos", eventosPorcinos.stream().filter(e -> "PARTO".equals(e.get("tipo"))).collect(Collectors.toList()));
            response.put("ecografias", eventosPorcinos.stream().filter(e -> "ECOGRAFIA".equals(e.get("tipo"))).collect(Collectors.toList()));
            response.put("destetes", eventosPorcinos.stream().filter(e -> "DESTETE".equals(e.get("tipo"))).collect(Collectors.toList()));
            response.put("controlCelo", eventosPorcinos.stream().filter(e -> "CONTROL_CELO".equals(e.get("tipo"))).collect(Collectors.toList()));
            response.put("vencidas", eventosPorcinos.stream().filter(e -> "GESTACION_VENCIDA".equals(e.get("tipo"))).collect(Collectors.toList()));
            response.put("recordatorios", recordatoriosCalendario);
            response.put("fechaInicio", fechaInicioFinal.toString());
            response.put("fechaFin", fechaFinFinal.toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error al obtener eventos de porcinos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener resumen de alertas de porcinos (para badges/contadores)
     */
    @GetMapping("/porcinos/resumen")
    public ResponseEntity<Map<String, Integer>> getResumenAlertasPorcinos(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Map<String, Integer> resumen = alertasPorcinosService.obtenerResumenAlertas(user);
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            System.err.println("Error al obtener resumen de alertas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

