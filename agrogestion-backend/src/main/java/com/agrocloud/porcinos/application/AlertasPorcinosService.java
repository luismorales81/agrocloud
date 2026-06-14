package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Recordatorio;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.infrastructure.RecordatorioRepository;

import com.agrocloud.porcinos.domain.Destete;
import com.agrocloud.porcinos.domain.Gestacion;
import com.agrocloud.porcinos.domain.Parto;
import com.agrocloud.porcinos.domain.Servicio;
import com.agrocloud.porcinos.infrastructure.DesteteRepository;
import com.agrocloud.porcinos.infrastructure.GestacionRepository;
import com.agrocloud.porcinos.infrastructure.MadreRepository;
import com.agrocloud.porcinos.infrastructure.PartoRepository;
import com.agrocloud.porcinos.infrastructure.ServicioRepository;
import com.agrocloud.porcinos.domain.ParametrosProductivosPorcino;
import com.agrocloud.porcinos.infrastructure.ParametrosProductivosPorcinoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Servicio para generar alertas y eventos del calendario del módulo Porcinos
 * Calcula automáticamente fechas importantes basadas en los parámetros configurados
 */
@Service
@Transactional(readOnly = true)
public class AlertasPorcinosService {

    @Autowired
    private GestacionRepository gestacionRepository;

    @Autowired
    private PartoRepository partoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    @SuppressWarnings("unused") // Reservado para futuras implementaciones
    private DesteteRepository desteteRepository;

    @Autowired
    @SuppressWarnings("unused") // Reservado para futuras implementaciones
    private MadreRepository madreRepository;

    @Autowired
    private ParametrosProductivosPorcinoRepository parametrosProductivosRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private RecordatorioRepository recordatorioRepository;

    /**
     * Obtener todos los eventos del calendario de porcinos para un rango de fechas
     */
    public List<Map<String, Object>> obtenerEventosCalendario(User user, LocalDate fechaInicio, LocalDate fechaFin) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Empresa empresa = empresaOpt.get();
        List<Map<String, Object>> eventos = new ArrayList<>();

        // Obtener parámetros productivos
        ParametrosProductivosPorcino parametros = parametrosProductivosRepository.findByEmpresa(empresa)
            .orElse(crearParametrosPorDefecto());

        // 1. Partos próximos (desde gestaciones activas)
        eventos.addAll(calcularPartosProximos(empresa, parametros, fechaInicio, fechaFin));

        // 2. Ecografías programadas (21 días después del servicio para confirmar preñez)
        eventos.addAll(calcularEcografiasProgramadas(empresa, parametros, fechaInicio, fechaFin));

        // 3. Destetes programados (desde partos activos)
        eventos.addAll(calcularDestetesProgramados(empresa, parametros, fechaInicio, fechaFin));

        // 4. Control de celo (21 días después del servicio)
        eventos.addAll(calcularControlesCelo(empresa, parametros, fechaInicio, fechaFin));

        // 6. Alertas de gestaciones vencidas
        eventos.addAll(calcularGestacionesVencidas(empresa, parametros, fechaInicio, fechaFin));

        // 7. Recordatorios automáticos del módulo porcinos (excluyendo destetes ya calculados)
        List<Map<String, Object>> eventosCalculados = new ArrayList<>(eventos);
        Set<String> destetesCalculados = new HashSet<>();
        for (Map<String, Object> evento : eventosCalculados) {
            if ("DESTETE".equals(evento.get("tipo")) && evento.get("partoId") != null) {
                String clave = evento.get("fecha") + "_" + evento.get("partoId");
                destetesCalculados.add(clave);
            }
        }
        eventos.addAll(convertirRecordatoriosAEventos(empresa, fechaInicio, fechaFin, destetesCalculados));

        // Eliminar duplicados por partoId y fecha
        return eliminarDuplicadosPorPartoId(eventos);
    }

    private List<Map<String, Object>> calcularPartosProximos(Empresa empresa,
            ParametrosProductivosPorcino parametros, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Map<String, Object>> eventos = new ArrayList<>();
        List<Gestacion> gestacionesActivas = gestacionRepository.findByEmpresaAndActivas(empresa);

        for (Gestacion gestacion : gestacionesActivas) {
            LocalDate fechaParto = gestacion.getFechaProbableParto();

            if (fechaParto != null && !fechaParto.isBefore(fechaInicio) && !fechaParto.isAfter(fechaFin)) {
                Map<String, Object> evento = new HashMap<>();
                evento.put("id", "parto_" + gestacion.getId());
                evento.put("tipo", "PARTO");
                evento.put("titulo", "🐷 Parto - " + gestacion.getMadre().getIdentificacion());
                evento.put("descripcion", String.format("Parto esperado de madre %s. Gestación iniciada el %s",
                    gestacion.getMadre().getIdentificacion(),
                    gestacion.getFechaInicio()));
                evento.put("fecha", fechaParto.toString());
                evento.put("prioridad", calcularPrioridad(fechaParto, parametros.getDiasAntelacionAlertarPartos()));
                evento.put("gestacionId", gestacion.getId());
                evento.put("madreId", gestacion.getMadre().getId());
                evento.put("madreIdentificacion", gestacion.getMadre().getIdentificacion());
                evento.put("color", "#ec4899");
                eventos.add(evento);
            }
        }
        return eventos;
    }

    private List<Map<String, Object>> calcularEcografiasProgramadas(Empresa empresa,
            ParametrosProductivosPorcino parametros, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Map<String, Object>> eventos = new ArrayList<>();
        List<Servicio> serviciosPendientes = servicioRepository.findPendientesControlByEmpresa(empresa);

        int diasControlCelo = parametros.getDiasControlCelo() != null ? parametros.getDiasControlCelo() : 21;

        for (Servicio servicio : serviciosPendientes) {
            LocalDate fechaEcografia = servicio.getFechaServicio().plusDays(diasControlCelo);

            if (!fechaEcografia.isBefore(fechaInicio) && !fechaEcografia.isAfter(fechaFin)) {
                Map<String, Object> evento = new HashMap<>();
                evento.put("id", "ecografia_" + servicio.getId());
                evento.put("tipo", "ECOGRAFIA");
                evento.put("titulo", "🔬 Ecografía - " + servicio.getMadre().getIdentificacion());
                evento.put("descripcion", String.format("Control ecográfico de preñez. Servicio realizado el %s (%s)",
                    servicio.getFechaServicio(),
                    servicio.getTipo().name()));
                evento.put("fecha", fechaEcografia.toString());
                evento.put("prioridad", calcularPrioridad(fechaEcografia, parametros.getDiasAntelacionAlertarEcografias()));
                evento.put("servicioId", servicio.getId());
                evento.put("madreId", servicio.getMadre().getId());
                evento.put("madreIdentificacion", servicio.getMadre().getIdentificacion());
                evento.put("color", "#8b5cf6");
                eventos.add(evento);
            }
        }
        return eventos;
    }

    private List<Map<String, Object>> calcularDestetesProgramados(Empresa empresa,
            ParametrosProductivosPorcino parametros, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Map<String, Object>> eventos = new ArrayList<>();
        List<Parto> partosActivos = partoRepository.findByEmpresaAndActivoTrue(empresa);

        int diasLactancia = parametros.getDiasLactancia() != null ? parametros.getDiasLactancia() : 21;

        List<Recordatorio> recordatoriosDestetes = recordatorioRepository
            .findRecordatoriosPorcinosPorRango(fechaInicio, fechaFin);
        Set<Long> partosConRecordatorio = new HashSet<>();
        for (Recordatorio r : recordatoriosDestetes) {
            if (r.getPartoId() != null &&
                !Boolean.TRUE.equals(r.getCompletado()) &&
                Boolean.TRUE.equals(r.getActivo())) {
                partosConRecordatorio.add(r.getPartoId());
            }
        }

        for (Parto parto : partosActivos) {
            if (parto.getFechaFin() == null && parto.getFechaInicio() != null) {
                Optional<Destete> desteteExistente = desteteRepository.findByPartoAndActivoTrue(parto);
                if (desteteExistente.isPresent()) {
                    continue;
                }

                LocalDate fechaDestete = parto.getFechaInicio().toLocalDate().plusDays(diasLactancia);

                if (partosConRecordatorio.contains(parto.getId())) {
                    continue;
                }

                if (!fechaDestete.isBefore(fechaInicio) && !fechaDestete.isAfter(fechaFin)) {
                    Map<String, Object> evento = new HashMap<>();
                    evento.put("id", "destete_" + parto.getId());
                    evento.put("tipo", "DESTETE");
                    evento.put("titulo", "🍼 Destete - " + parto.getMadre().getIdentificacion());

                    LocalDate fechaParto = parto.getFechaInicio().toLocalDate();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("es-ES"));
                    String fechaPartoFormateada = fechaParto.format(formatter);

                    evento.put("descripcion", String.format("Destete programado. %d lechones nacidos vivos. Parto del %s",
                        parto.getNacidosVivos(),
                        fechaPartoFormateada));
                    evento.put("fecha", fechaDestete.toString());
                    evento.put("prioridad", calcularPrioridad(fechaDestete, parametros.getDiasAntelacionAlertarDestetes()));
                    evento.put("partoId", parto.getId());
                    evento.put("madreId", parto.getMadre().getId());
                    evento.put("madreIdentificacion", parto.getMadre().getIdentificacion());
                    evento.put("nacidosVivos", parto.getNacidosVivos());
                    evento.put("color", "#10b981");
                    eventos.add(evento);
                }
            }
        }
        return eventos;
    }

    private List<Map<String, Object>> calcularControlesCelo(Empresa empresa,
            ParametrosProductivosPorcino parametros, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Map<String, Object>> eventos = new ArrayList<>();
        List<Servicio> servicios = servicioRepository.findByEmpresaAndActivoTrue(empresa);

        int diasEntreCelos = parametros.getDiasEntreCelos() != null ? parametros.getDiasEntreCelos() : 21;

        for (Servicio servicio : servicios) {
            if (servicio.getEstadoServicio() == Servicio.EstadoServicio.PENDIENTE_CONTROL) {
                LocalDate fechaControlCelo = servicio.getFechaServicio().plusDays(diasEntreCelos);

                if (!fechaControlCelo.isBefore(fechaInicio) && !fechaControlCelo.isAfter(fechaFin)) {
                    Map<String, Object> evento = new HashMap<>();
                    evento.put("id", "celo_" + servicio.getId());
                    evento.put("tipo", "CONTROL_CELO");
                    evento.put("titulo", "👀 Control Celo - " + servicio.getMadre().getIdentificacion());
                    evento.put("descripcion", String.format("Control de retorno de celo. Si retorna, el servicio falló. Servicio del %s",
                        servicio.getFechaServicio()));
                    evento.put("fecha", fechaControlCelo.toString());
                    evento.put("prioridad", "MEDIA");
                    evento.put("servicioId", servicio.getId());
                    evento.put("madreId", servicio.getMadre().getId());
                    evento.put("madreIdentificacion", servicio.getMadre().getIdentificacion());
                    evento.put("color", "#6366f1");
                    eventos.add(evento);
                }
            }
        }
        return eventos;
    }

    private List<Map<String, Object>> calcularGestacionesVencidas(Empresa empresa,
            ParametrosProductivosPorcino parametros, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Map<String, Object>> eventos = new ArrayList<>();
        List<Gestacion> gestacionesActivas = gestacionRepository.findByEmpresaAndActivas(empresa);

        int tolerancia = parametros.getDiasToleranciaVencimientoGestacion() != null ?
            parametros.getDiasToleranciaVencimientoGestacion() : 5;
        LocalDate hoy = LocalDate.now();

        for (Gestacion gestacion : gestacionesActivas) {
            if (gestacion.getFechaProbableParto() != null) {
                LocalDate fechaVencimiento = gestacion.getFechaProbableParto().plusDays(tolerancia);

                if (hoy.isAfter(gestacion.getFechaProbableParto()) &&
                    !fechaVencimiento.isBefore(fechaInicio) && !fechaVencimiento.isAfter(fechaFin)) {

                    Map<String, Object> evento = new HashMap<>();
                    evento.put("id", "vencida_" + gestacion.getId());
                    evento.put("tipo", "GESTACION_VENCIDA");
                    evento.put("titulo", "⚠️ VENCIDA - " + gestacion.getMadre().getIdentificacion());
                    evento.put("descripcion", String.format("¡Gestación vencida sin parto! Fecha esperada: %s. Días de atraso: %d",
                        gestacion.getFechaProbableParto(),
                        ChronoUnit.DAYS.between(gestacion.getFechaProbableParto(), hoy)));
                    evento.put("fecha", gestacion.getFechaProbableParto().toString());
                    evento.put("prioridad", "CRITICA");
                    evento.put("gestacionId", gestacion.getId());
                    evento.put("madreId", gestacion.getMadre().getId());
                    evento.put("madreIdentificacion", gestacion.getMadre().getIdentificacion());
                    evento.put("color", "#ef4444");
                    eventos.add(evento);
                }
            }
        }
        return eventos;
    }

    private List<Map<String, Object>> convertirRecordatoriosAEventos(Empresa empresa,
            LocalDate fechaInicio, LocalDate fechaFin, Set<String> destetesCalculados) {
        List<Map<String, Object>> eventos = new ArrayList<>();

        List<Recordatorio> recordatorios = recordatorioRepository
            .findRecordatoriosPorcinosPorRango(fechaInicio, fechaFin);

        Set<String> recordatoriosIncluidos = new HashSet<>();

        for (Recordatorio recordatorio : recordatorios) {
            if (recordatorio.getCompletado() != null && recordatorio.getCompletado()) {
                continue;
            }
            if (recordatorio.getActivo() == null || !recordatorio.getActivo()) {
                continue;
            }

            if (recordatorio.getPartoId() != null) {
                String clave = recordatorio.getFecha().toString() + "_" + recordatorio.getPartoId();
                if (destetesCalculados != null && destetesCalculados.contains(clave)) {
                    continue;
                }
                if (recordatoriosIncluidos.contains(clave)) {
                    continue;
                }
                recordatoriosIncluidos.add(clave);
            }

            Map<String, Object> evento = new HashMap<>();
            evento.put("id", "recordatorio_" + recordatorio.getId());
            String tipoEvento = determinarTipoEventoDesdeRecordatorio(recordatorio);
            evento.put("tipo", tipoEvento);
            evento.put("titulo", recordatorio.getTitulo());

            String descripcion = recordatorio.getDescripcion();
            if (descripcion != null) {
                descripcion = normalizarFormatoFechaEnDescripcion(descripcion);
            }
            evento.put("descripcion", descripcion);

            evento.put("fecha", recordatorio.getFecha().toString());
            evento.put("recordatorioId", recordatorio.getId());
            evento.put("servicioId", recordatorio.getServicioId());
            evento.put("gestacionId", recordatorio.getGestacionId());
            evento.put("partoId", recordatorio.getPartoId());
            evento.put("madreId", recordatorio.getMadreId());

            if (recordatorio.getPartoId() != null) {
                Optional<Parto> partoOpt = partoRepository.findByIdAndActivoTrue(recordatorio.getPartoId());
                if (partoOpt.isPresent()) {
                    Parto parto = partoOpt.get();
                    evento.put("madreIdentificacion", parto.getMadre() != null ? parto.getMadre().getIdentificacion() : null);
                    evento.put("nacidosVivos", parto.getNacidosVivos());
                }
            }

            evento.put("completado", recordatorio.getCompletado() != null ? recordatorio.getCompletado() : false);
            evento.put("prioridad", calcularPrioridad(recordatorio.getFecha(), 7));
            evento.put("color", obtenerColorPorTipo(tipoEvento));

            eventos.add(evento);
        }
        return eventos;
    }

    private List<Map<String, Object>> eliminarDuplicadosPorPartoId(List<Map<String, Object>> eventos) {
        Map<String, Map<String, Object>> eventosUnicos = new LinkedHashMap<>();

        for (Map<String, Object> evento : eventos) {
            String tipo = (String) evento.get("tipo");
            Object partoId = evento.get("partoId");
            String fecha = (String) evento.get("fecha");

            if ("DESTETE".equals(tipo) && partoId != null && fecha != null) {
                String clave = fecha + "_" + partoId;
                if (!eventosUnicos.containsKey(clave)) {
                    eventosUnicos.put(clave, evento);
                }
            } else {
                String id = (String) evento.get("id");
                if (id != null) {
                    eventosUnicos.put(id, evento);
                } else {
                    eventosUnicos.put("evento_" + eventosUnicos.size(), evento);
                }
            }
        }
        return new ArrayList<>(eventosUnicos.values());
    }

    private String normalizarFormatoFechaEnDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isEmpty()) {
            return descripcion;
        }
        java.util.regex.Pattern patronFechaISO = java.util.regex.Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
        java.util.regex.Matcher matcher = patronFechaISO.matcher(descripcion);
        StringBuffer resultado = new StringBuffer();
        while (matcher.find()) {
            String año = matcher.group(1);
            String mes = matcher.group(2);
            String dia = matcher.group(3);
            String fechaFormateada = String.format("%s/%s/%s", dia, mes, año);
            matcher.appendReplacement(resultado, fechaFormateada);
        }
        matcher.appendTail(resultado);
        return resultado.toString();
    }

    private String determinarTipoEventoDesdeRecordatorio(Recordatorio recordatorio) {
        if (recordatorio.getServicioId() != null) {
            return "CONTROL_CELO";
        }
        if (recordatorio.getPartoId() != null) {
            return "DESTETE";
        }
        if (recordatorio.getGestacionId() != null) {
            String titulo = recordatorio.getTitulo() != null ? recordatorio.getTitulo().toLowerCase() : "";
            if (titulo.contains("maternidad")) {
                return "MATERNIDAD";
            }
            if (titulo.contains("parto")) {
                return "PARTO";
            }
            return "PARTO";
        }
        return "RECORDATORIO";
    }

    private String obtenerColorPorTipo(String tipo) {
        switch (tipo) {
            case "CONTROL_CELO": return "#3b82f6";
            case "DESTETE": return "#10b981";
            case "PARTO": return "#ec4899";
            default: return "#6b7280";
        }
    }

    private String calcularPrioridad(LocalDate fechaEvento, Integer diasAntelacion) {
        long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), fechaEvento);
        int umbral = diasAntelacion != null ? diasAntelacion : 7;

        if (diasRestantes < 0) return "CRITICA";
        if (diasRestantes <= 2) return "ALTA";
        if (diasRestantes <= umbral) return "MEDIA";
        return "BAJA";
    }

    private ParametrosProductivosPorcino crearParametrosPorDefecto() {
        ParametrosProductivosPorcino parametros = new ParametrosProductivosPorcino();
        parametros.setDiasPromedioGestacion(115);
        parametros.setDiasLactancia(21);
        parametros.setDiasToleranciaVencimientoGestacion(5);
        parametros.setDiasControlCelo(21);
        parametros.setDiasEntreCelos(21);
        parametros.setDiasAntelacionAlertarPartos(7);
        parametros.setDiasAntelacionAlertarEcografias(3);
        parametros.setDiasAntelacionAlertarDestetes(2);
        return parametros;
    }

    public Map<String, Integer> obtenerResumenAlertas(User user) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaOpt.isEmpty()) {
            return new HashMap<>();
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaFin = hoy.plusDays(7);

        List<Map<String, Object>> eventos = obtenerEventosCalendario(user, hoy, fechaFin);

        Map<String, Integer> resumen = new HashMap<>();
        resumen.put("total", eventos.size());
        resumen.put("partos", (int) eventos.stream().filter(e -> "PARTO".equals(e.get("tipo"))).count());
        resumen.put("ecografias", (int) eventos.stream().filter(e -> "ECOGRAFIA".equals(e.get("tipo"))).count());
        resumen.put("destetes", (int) eventos.stream().filter(e -> "DESTETE".equals(e.get("tipo"))).count());
        resumen.put("controlCelo", (int) eventos.stream().filter(e -> "CONTROL_CELO".equals(e.get("tipo"))).count());
        resumen.put("vencidas", (int) eventos.stream().filter(e -> "GESTACION_VENCIDA".equals(e.get("tipo"))).count());
        resumen.put("criticas", (int) eventos.stream().filter(e -> "CRITICA".equals(e.get("prioridad"))).count());
        resumen.put("altas", (int) eventos.stream().filter(e -> "ALTA".equals(e.get("prioridad"))).count());

        return resumen;
    }
}
