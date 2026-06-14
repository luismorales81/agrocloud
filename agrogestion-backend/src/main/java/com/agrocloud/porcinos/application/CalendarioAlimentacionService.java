package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.application.port.LoteParaPorcinosQuery;
import com.agrocloud.cultivos.infrastructure.CultivoRepository;
import com.agrocloud.porcinos.domain.ConsumoDiarioAutomatico;
import com.agrocloud.porcinos.domain.ConsumoDiarioDetalle;
import com.agrocloud.porcinos.domain.DiaAlimentacion;
import com.agrocloud.porcinos.infrastructure.ConsumoDiarioAutomaticoRepository;
import com.agrocloud.porcinos.infrastructure.ConsumoDiarioDetalleRepository;
import com.agrocloud.porcinos.infrastructure.DiaAlimentacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar el calendario de alimentación
 * Incluye confirmación de días, visualización de estados, y alertas
 */
@Service
public class CalendarioAlimentacionService {

    @Autowired
    private DiaAlimentacionRepository diaAlimentacionRepository;

    @Autowired
    private ConsumoDiarioAutomaticoRepository consumoDiarioRepository;

    @Autowired
    private ConsumoDiarioDetalleRepository consumoDetalleRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private UserService userService;

    @Autowired
    private LoteParaPorcinosQuery loteParaPorcinosQuery;

    @Autowired
    private CultivoRepository cultivoRepository;

    @Autowired
    @Lazy
    private ConsumoDiarioAutomaticoService consumoDiarioAutomaticoService;

    @Autowired
    private ConfiguracionPorcinoService configuracionPorcinoService;

    /** Clave en {@code porcinos_configuraciones_porcinos}: si es verdadero, no se puede confirmar el día con consumos aún estimados. */
    public static final String CLAVE_CONFIRMACION_CALENDARIO_SOLO_CON_RACION_REAL = "CONFIRMAR_CALENDARIO_SOLO_CON_REAL";

    /**
     * Obtener calendario mensual con estados
     */
    @Transactional(readOnly = true)
    public Map<LocalDate, DiaAlimentacionDTO> obtenerCalendarioMensual(int año, int mes, Long userId) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresaOpt.isEmpty()) {
            return Map.of();
        }

        LocalDate fechaInicio = LocalDate.of(año, mes, 1);
        LocalDate fechaFin = fechaInicio.withDayOfMonth(fechaInicio.lengthOfMonth());

        List<DiaAlimentacion> dias = diaAlimentacionRepository
            .findByEmpresaAndFechaBetweenOrderByFechaDesc(empresaOpt.get(), fechaInicio, fechaFin);

        return dias.stream()
            .collect(Collectors.toMap(
                DiaAlimentacion::getFecha,
                dia -> {
                    DiaAlimentacionDTO dto = convertirADTO(dia);
                    enriquecerConteosTipoConsumoDesdeRepositorio(dto, dia);
                    return dto;
                }
            ));
    }

    /**
     * Obtener detalle completo de un día
     */
    @Transactional(readOnly = true)
    public DiaAlimentacionDetalleDTO obtenerDetalleDia(LocalDate fecha, Long userId) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresaOpt.isEmpty()) {
            return null;
        }

        Optional<DiaAlimentacion> diaOpt = diaAlimentacionRepository
            .findByEmpresaAndFecha(empresaOpt.get(), fecha);

        if (diaOpt.isEmpty()) {
            return null;
        }

        DiaAlimentacion dia = diaOpt.get();
        
        // Cargar consumos del día
        List<ConsumoDiarioAutomatico> consumos = consumoDiarioRepository
            .findByDiaAlimentacionOrderByEtapaAlimentacionAsc(dia);

        // Para cada consumo, cargar detalles y poblar nombres
        List<ConsumoDiarioDTO> consumosDTO = new ArrayList<>();
        List<AlertaStockDTO> alertas = new ArrayList<>();

        for (ConsumoDiarioAutomatico consumo : consumos) {
            // Poblar nombres transitorios
            if (consumo.getRecria() != null && consumo.getRecria().getLoteId() != null) {
                consumo.setRecriaNombre(loteParaPorcinosQuery.obtenerPorId(consumo.getRecria().getLoteId()).map(dto -> dto.nombre()).orElse(null));
            }
            if (consumo.getLoteId() != null) {
                consumo.setLoteNombre(loteParaPorcinosQuery.obtenerPorId(consumo.getLoteId()).map(dto -> dto.nombre()).orElse(null));
            }
            if (consumo.getReceta() != null) {
                consumo.setRecetaNombre(consumo.getReceta().getNombre());
            }

            // Cargar detalles del consumo
            List<ConsumoDiarioDetalle> detalles = consumoDetalleRepository
                .findByConsumoDiarioOrderByTipoComponenteAsc(consumo);

            // Poblar nombres de detalles
            for (ConsumoDiarioDetalle detalle : detalles) {
                if (detalle.getInsumo() != null) {
                    detalle.setNombreComponente(detalle.getInsumo().getNombre());
                    detalle.setUnidadMedida(detalle.getInsumo().getUnidadMedida());
                } else if (detalle.getCultivoId() != null) {
                    detalle.setNombreComponente(cultivoRepository.findById(detalle.getCultivoId()).map(c -> c.getNombre()).orElse(null));
                    detalle.setUnidadMedida("kg");
                } else if (detalle.getInsumoCompuesto() != null) {
                    detalle.setNombreComponente(detalle.getInsumoCompuesto().getNombre());
                    detalle.setUnidadMedida(detalle.getInsumoCompuesto().getUnidadMedida());
                }

                // Si tiene déficit, crear alerta
                if (detalle.getTieneDeficit() && detalle.getDeficit() != null) {
                    AlertaStockDTO alerta = new AlertaStockDTO();
                    alerta.setFecha(dia.getFecha());
                    alerta.setInsumoNombre(detalle.getNombreComponente());
                    alerta.setDeficit(detalle.getDeficit());
                    alerta.setCantidadRequerida(detalle.getCantidadRequerida());
                    alerta.setCantidadDisponible(detalle.getCantidadDisponible());
                    alerta.setPorcentajeCobertura(detalle.getPorcentajeCobertura());
                    alerta.setUnidadMedida(detalle.getUnidadMedida());
                    alerta.setDiaAlimentacionId(dia.getId());
                    alertas.add(alerta);
                }
            }

            ConsumoDiarioDTO consumoDTO = convertirConsumoADTO(consumo, detalles);
            consumosDTO.add(consumoDTO);
        }

        // Construir DTO completo
        DiaAlimentacionDetalleDTO detalleDTO = new DiaAlimentacionDetalleDTO();
        DiaAlimentacionDTO diaDto = convertirADTO(dia);
        enriquecerConteosTipoConsumoDesdeEntidades(diaDto, dia, consumos);
        detalleDTO.setDia(diaDto);
        detalleDTO.setConsumos(consumosDTO);
        detalleDTO.setAlertas(alertas);

        return detalleDTO;
    }

    /**
     * Para días pendientes: total de consumos del día y cuántos siguen solo en modo estimado (sin kg real).
     */
    private void enriquecerConteosTipoConsumoDesdeEntidades(
            DiaAlimentacionDTO dto,
            DiaAlimentacion dia,
            List<ConsumoDiarioAutomatico> consumos) {
        if (dia.getEstado() != DiaAlimentacion.EstadoDia.PENDIENTE || consumos == null) {
            return;
        }
        int total = consumos.size();
        int estimados = (int) consumos.stream()
            .filter(c -> c.getTipoRegistroConsumo() == null
                || c.getTipoRegistroConsumo() == ConsumoDiarioAutomatico.TipoRegistroConsumo.ESTIMADO)
            .count();
        dto.setConsumosDelDia(total);
        dto.setConsumosSoloEstimados(estimados);
    }

    private void enriquecerConteosTipoConsumoDesdeRepositorio(DiaAlimentacionDTO dto, DiaAlimentacion dia) {
        if (dia.getEstado() != DiaAlimentacion.EstadoDia.PENDIENTE) {
            return;
        }
        int total = (int) consumoDiarioRepository.countByDiaAlimentacion(dia);
        int estimados = (int) consumoDiarioRepository.countByDiaAlimentacionAndTipoRegistroConsumo(
            dia, ConsumoDiarioAutomatico.TipoRegistroConsumo.ESTIMADO);
        dto.setConsumosDelDia(total);
        dto.setConsumosSoloEstimados(estimados);
    }

    /**
     * Confirmar día de alimentación y descontar stock.
     * Primero descuenta el stock en inventario, luego marca el día como CONFIRMADO.
     */
    @Transactional
    public DiaAlimentacionDTO confirmarDia(LocalDate fecha, String observaciones, Long userId) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresaOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        Optional<DiaAlimentacion> diaOpt = diaAlimentacionRepository
            .findByEmpresaAndFecha(empresaOpt.get(), fecha);

        if (diaOpt.isEmpty()) {
            throw new IllegalArgumentException("No existe día de alimentación para la fecha: " + fecha);
        }

        DiaAlimentacion dia = diaOpt.get();

        // REGLA: No recalcular días ya confirmados
        if (dia.estaCerrado()) {
            throw new IllegalStateException("El día ya está confirmado y no se puede modificar. Estado: " + dia.getEstado());
        }

        // Validar que el día existe y está en estado PENDIENTE
        if (dia.getEstado() != DiaAlimentacion.EstadoDia.PENDIENTE) {
            throw new IllegalStateException("El día no está en estado PENDIENTE. Estado actual: " + dia.getEstado());
        }

        // Obtener usuario
        Optional<User> usuarioOpt = userService.findById(userId);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        User usuario = usuarioOpt.get();

        boolean exigirKgReal = configuracionPorcinoService.obtenerValorBoolean(
            CLAVE_CONFIRMACION_CALENDARIO_SOLO_CON_RACION_REAL, false, usuario);
        if (exigirKgReal) {
            List<ConsumoDiarioAutomatico> consumosDelDia = consumoDiarioRepository.findByDiaAlimentacion(dia);
            long lineasSoloEstimadas = consumosDelDia.stream()
                .filter(c -> c.getTipoRegistroConsumo() == null
                    || c.getTipoRegistroConsumo() == ConsumoDiarioAutomatico.TipoRegistroConsumo.ESTIMADO)
                .count();
            if (lineasSoloEstimadas > 0) {
                throw new IllegalStateException(
                    "La empresa tiene activada la opción de confirmar el calendario solo con kg reales cargados. "
                        + "Aún hay " + lineasSoloEstimadas + " consumo(s) en modo estimado. Registre el kg real o desactive la clave "
                        + CLAVE_CONFIRMACION_CALENDARIO_SOLO_CON_RACION_REAL + " en configuraciones porcinos.");
            }
        }

        // 1. Descontar stock en inventario (antes de confirmar, para que si falla no se confirme)
        consumoDiarioAutomaticoService.descontarStockAlConfirmar(dia, usuario);

        // 2. Confirmar día (cambiar estado a CONFIRMADO)
        dia.confirmar(usuario, observaciones);

        DiaAlimentacion diaGuardado = diaAlimentacionRepository.save(dia);

        return convertirADTO(diaGuardado);
    }

    /**
     * Registra kg reales de ración para un consumo del día (recría o madre), recalcula detalles y alertas.
     */
    @Transactional
    public ConsumoDiarioDTO registrarCantidadRecetaReal(Long consumoDiarioId, java.math.BigDecimal cantidadRecetaKg, Long userId) {
        ConsumoDiarioAutomatico consumo = consumoDiarioAutomaticoService.registrarCantidadRecetaReal(
            consumoDiarioId, cantidadRecetaKg, userId);
        if (consumo.getRecria() != null && consumo.getRecria().getLoteId() != null) {
            consumo.setRecriaNombre(loteParaPorcinosQuery.obtenerPorId(consumo.getRecria().getLoteId()).map(dto -> dto.nombre()).orElse(null));
        }
        if (consumo.getLoteId() != null) {
            consumo.setLoteNombre(loteParaPorcinosQuery.obtenerPorId(consumo.getLoteId()).map(dto -> dto.nombre()).orElse(null));
        }
        if (consumo.getReceta() != null) {
            consumo.setRecetaNombre(consumo.getReceta().getNombre());
        }
        List<ConsumoDiarioDetalle> detalles = consumoDetalleRepository.findByConsumoDiarioOrderByTipoComponenteAsc(consumo);
        for (ConsumoDiarioDetalle detalle : detalles) {
            if (detalle.getInsumo() != null) {
                detalle.setNombreComponente(detalle.getInsumo().getNombre());
                detalle.setUnidadMedida(detalle.getInsumo().getUnidadMedida());
            } else if (detalle.getCultivoId() != null) {
                detalle.setNombreComponente(cultivoRepository.findById(detalle.getCultivoId()).map(c -> c.getNombre()).orElse(null));
                detalle.setUnidadMedida("kg");
            } else if (detalle.getInsumoCompuesto() != null) {
                detalle.setNombreComponente(detalle.getInsumoCompuesto().getNombre());
                detalle.setUnidadMedida(detalle.getInsumoCompuesto().getUnidadMedida());
            }
        }
        return convertirConsumoADTO(consumo, detalles);
    }

    /**
     * Obtener alertas de stock insuficiente
     */
    @Transactional(readOnly = true)
    public List<AlertaStockDTO> obtenerAlertasStock(LocalDate fechaDesde, LocalDate fechaHasta, Long userId) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresaOpt.isEmpty()) {
            return List.of();
        }

        if (fechaDesde == null) {
            fechaDesde = LocalDate.now().minusMonths(1);
        }
        if (fechaHasta == null) {
            fechaHasta = LocalDate.now();
        }

        List<DiaAlimentacion> diasConAlertas = diaAlimentacionRepository
            .findByEmpresaAndTieneAlertasStockInsuficienteTrueOrderByFechaDesc(empresaOpt.get());

        List<AlertaStockDTO> alertas = new ArrayList<>();

        for (DiaAlimentacion dia : diasConAlertas) {
            if (dia.getFecha().isBefore(fechaDesde) || dia.getFecha().isAfter(fechaHasta)) {
                continue;
            }

            List<ConsumoDiarioDetalle> detallesConDeficit = consumoDetalleRepository
                .findByDiaAlimentacionAndTieneDeficitTrue(dia);

            for (ConsumoDiarioDetalle detalle : detallesConDeficit) {
                AlertaStockDTO alerta = new AlertaStockDTO();
                alerta.setFecha(dia.getFecha());
                alerta.setInsumoNombre(detalle.getNombreComponente());
                alerta.setDeficit(detalle.getDeficit());
                alerta.setCantidadRequerida(detalle.getCantidadRequerida());
                alerta.setCantidadDisponible(detalle.getCantidadDisponible());
                alerta.setPorcentajeCobertura(detalle.getPorcentajeCobertura());
                alerta.setUnidadMedida(detalle.getUnidadMedida());
                alerta.setDiaAlimentacionId(dia.getId());
                alertas.add(alerta);
            }
        }

        return alertas;
    }

    // Métodos auxiliares de conversión a DTO
    private DiaAlimentacionDTO convertirADTO(DiaAlimentacion dia) {
        DiaAlimentacionDTO dto = new DiaAlimentacionDTO();
        dto.setId(dia.getId());
        dto.setFecha(dia.getFecha());
        dto.setEstado(dia.getEstado().name());
        dto.setTotalLotesAtendidos(dia.getTotalLotesAtendidos());
        dto.setTotalAnimalesAtendidos(dia.getTotalAnimalesAtendidos());
        dto.setTotalRecetasUsadas(dia.getTotalRecetasUsadas());
        dto.setTotalInsumosConsumidos(dia.getTotalInsumosConsumidos());
        dto.setTieneAlertasStockInsuficiente(dia.getTieneAlertasStockInsuficiente());
        dto.setCantidadAlertas(dia.getCantidadAlertas());
        if (dia.getConfirmadoPor() != null) {
            dto.setConfirmadoPorId(dia.getConfirmadoPor().getId());
            String nombreCompleto = "";
            if (dia.getConfirmadoPor().getFirstName() != null) {
                nombreCompleto += dia.getConfirmadoPor().getFirstName();
            }
            if (dia.getConfirmadoPor().getLastName() != null) {
                if (!nombreCompleto.isEmpty()) nombreCompleto += " ";
                nombreCompleto += dia.getConfirmadoPor().getLastName();
            }
            if (nombreCompleto.isEmpty()) {
                nombreCompleto = dia.getConfirmadoPor().getEmail() != null ? 
                    dia.getConfirmadoPor().getEmail() : dia.getConfirmadoPor().getActualUsername();
            }
            dto.setConfirmadoPorNombre(nombreCompleto);
        }
        dto.setFechaConfirmacion(dia.getFechaConfirmacion());
        dto.setObservacionesConfirmacion(dia.getObservacionesConfirmacion());
        return dto;
    }

    private ConsumoDiarioDTO convertirConsumoADTO(ConsumoDiarioAutomatico consumo, List<ConsumoDiarioDetalle> detalles) {
        ConsumoDiarioDTO dto = new ConsumoDiarioDTO();
        dto.setId(consumo.getId());
        dto.setEtapaAlimentacion(consumo.getEtapaAlimentacion());
        dto.setCantidadAnimales(consumo.getCantidadAnimales());
        dto.setRecetaId(consumo.getReceta().getId());
        dto.setRecetaNombre(consumo.getRecetaNombre());
        dto.setCantidadRecetaTotal(consumo.getCantidadRecetaTotal());
        dto.setCantidadRecetaReal(consumo.getCantidadRecetaReal());
        dto.setCantidadRecetaEfectiva(consumo.getCantidadRecetaEfectiva());
        dto.setTipoRegistroConsumo(
            consumo.getTipoRegistroConsumo() != null ? consumo.getTipoRegistroConsumo().name() : "ESTIMADO");
        dto.setCantidadDiariaPorAnimal(consumo.getCantidadDiariaPorAnimal());
        if (consumo.getRecria() != null) {
            dto.setRecriaId(consumo.getRecria().getId());
            dto.setRecriaNombre(consumo.getRecriaNombre());
        }
        if (consumo.getMadre() != null) {
            dto.setMadreId(consumo.getMadre().getId());
            dto.setMadreNombre(consumo.getMadreNombre());
        }
        
        List<ConsumoDetalleDTO> detallesDTO = detalles.stream()
            .map(this::convertirDetalleADTO)
            .collect(Collectors.toList());
        dto.setDetalles(detallesDTO);
        
        return dto;
    }

    private ConsumoDetalleDTO convertirDetalleADTO(ConsumoDiarioDetalle detalle) {
        ConsumoDetalleDTO dto = new ConsumoDetalleDTO();
        dto.setId(detalle.getId());
        dto.setTipoComponente(detalle.getTipoComponente().name());
        dto.setNombreComponente(detalle.getNombreComponente());
        dto.setCantidadRequerida(detalle.getCantidadRequerida());
        dto.setCantidadDisponible(detalle.getCantidadDisponible());
        dto.setCantidadDescontada(detalle.getCantidadDescontada());
        dto.setStockResultante(detalle.getStockResultante());
        dto.setDeficit(detalle.getDeficit());
        dto.setTieneDeficit(detalle.getTieneDeficit());
        dto.setPorcentajeCobertura(detalle.getPorcentajeCobertura());
        dto.setUnidadMedida(detalle.getUnidadMedida());
        return dto;
    }

    // DTOs internos (simplificados - se crearán en archivo separado)
    public static class DiaAlimentacionDTO {
        private Long id;
        private LocalDate fecha;
        private String estado;
        private Integer totalLotesAtendidos;
        private Integer totalAnimalesAtendidos;
        private Integer totalRecetasUsadas;
        private Integer totalInsumosConsumidos;
        private Boolean tieneAlertasStockInsuficiente;
        private Integer cantidadAlertas;
        private Long confirmadoPorId;
        private String confirmadoPorNombre;
        private java.time.LocalDateTime fechaConfirmacion;
        private String observacionesConfirmacion;
        /** Solo días PENDIENTE: cantidad de líneas de consumo del día. */
        private Integer consumosDelDia;
        /** Solo PENDIENTE: cuántas siguen sin kg real cargado (tipo ESTIMADO). */
        private Integer consumosSoloEstimados;
        // Getters y setters...
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
        public Integer getTotalLotesAtendidos() { return totalLotesAtendidos; }
        public void setTotalLotesAtendidos(Integer totalLotesAtendidos) { this.totalLotesAtendidos = totalLotesAtendidos; }
        public Integer getTotalAnimalesAtendidos() { return totalAnimalesAtendidos; }
        public void setTotalAnimalesAtendidos(Integer totalAnimalesAtendidos) { this.totalAnimalesAtendidos = totalAnimalesAtendidos; }
        public Integer getTotalRecetasUsadas() { return totalRecetasUsadas; }
        public void setTotalRecetasUsadas(Integer totalRecetasUsadas) { this.totalRecetasUsadas = totalRecetasUsadas; }
        public Integer getTotalInsumosConsumidos() { return totalInsumosConsumidos; }
        public void setTotalInsumosConsumidos(Integer totalInsumosConsumidos) { this.totalInsumosConsumidos = totalInsumosConsumidos; }
        public Boolean getTieneAlertasStockInsuficiente() { return tieneAlertasStockInsuficiente; }
        public void setTieneAlertasStockInsuficiente(Boolean tieneAlertasStockInsuficiente) { this.tieneAlertasStockInsuficiente = tieneAlertasStockInsuficiente; }
        public Integer getCantidadAlertas() { return cantidadAlertas; }
        public void setCantidadAlertas(Integer cantidadAlertas) { this.cantidadAlertas = cantidadAlertas; }
        public Long getConfirmadoPorId() { return confirmadoPorId; }
        public void setConfirmadoPorId(Long confirmadoPorId) { this.confirmadoPorId = confirmadoPorId; }
        public String getConfirmadoPorNombre() { return confirmadoPorNombre; }
        public void setConfirmadoPorNombre(String confirmadoPorNombre) { this.confirmadoPorNombre = confirmadoPorNombre; }
        public java.time.LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }
        public void setFechaConfirmacion(java.time.LocalDateTime fechaConfirmacion) { this.fechaConfirmacion = fechaConfirmacion; }
        public String getObservacionesConfirmacion() { return observacionesConfirmacion; }
        public void setObservacionesConfirmacion(String observacionesConfirmacion) { this.observacionesConfirmacion = observacionesConfirmacion; }
        public Integer getConsumosDelDia() { return consumosDelDia; }
        public void setConsumosDelDia(Integer consumosDelDia) { this.consumosDelDia = consumosDelDia; }
        public Integer getConsumosSoloEstimados() { return consumosSoloEstimados; }
        public void setConsumosSoloEstimados(Integer consumosSoloEstimados) { this.consumosSoloEstimados = consumosSoloEstimados; }
    }

    public static class DiaAlimentacionDetalleDTO {
        private DiaAlimentacionDTO dia;
        private List<ConsumoDiarioDTO> consumos;
        private List<AlertaStockDTO> alertas;
        // Getters y setters...
        public DiaAlimentacionDTO getDia() { return dia; }
        public void setDia(DiaAlimentacionDTO dia) { this.dia = dia; }
        public List<ConsumoDiarioDTO> getConsumos() { return consumos; }
        public void setConsumos(List<ConsumoDiarioDTO> consumos) { this.consumos = consumos; }
        public List<AlertaStockDTO> getAlertas() { return alertas; }
        public void setAlertas(List<AlertaStockDTO> alertas) { this.alertas = alertas; }
    }

    public static class ConsumoDiarioDTO {
        private Long id;
        private String etapaAlimentacion;
        private Integer cantidadAnimales;
        private Long recriaId;
        private String recriaNombre;
        private Long madreId;
        private String madreNombre;
        private Long recetaId;
        private String recetaNombre;
        private java.math.BigDecimal cantidadRecetaTotal;
        /** kg informados por el operario; null si solo hay proyección. */
        private java.math.BigDecimal cantidadRecetaReal;
        /** kg usados para detalle y stock: real si hay; si no, proyección. */
        private java.math.BigDecimal cantidadRecetaEfectiva;
        private String tipoRegistroConsumo;
        private java.math.BigDecimal cantidadDiariaPorAnimal;
        private List<ConsumoDetalleDTO> detalles;
        // Getters y setters...
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEtapaAlimentacion() { return etapaAlimentacion; }
        public void setEtapaAlimentacion(String etapaAlimentacion) { this.etapaAlimentacion = etapaAlimentacion; }
        public Integer getCantidadAnimales() { return cantidadAnimales; }
        public void setCantidadAnimales(Integer cantidadAnimales) { this.cantidadAnimales = cantidadAnimales; }
        public Long getRecriaId() { return recriaId; }
        public void setRecriaId(Long recriaId) { this.recriaId = recriaId; }
        public String getRecriaNombre() { return recriaNombre; }
        public void setRecriaNombre(String recriaNombre) { this.recriaNombre = recriaNombre; }
        public Long getMadreId() { return madreId; }
        public void setMadreId(Long madreId) { this.madreId = madreId; }
        public String getMadreNombre() { return madreNombre; }
        public void setMadreNombre(String madreNombre) { this.madreNombre = madreNombre; }
        public Long getRecetaId() { return recetaId; }
        public void setRecetaId(Long recetaId) { this.recetaId = recetaId; }
        public String getRecetaNombre() { return recetaNombre; }
        public void setRecetaNombre(String recetaNombre) { this.recetaNombre = recetaNombre; }
        public java.math.BigDecimal getCantidadRecetaTotal() { return cantidadRecetaTotal; }
        public void setCantidadRecetaTotal(java.math.BigDecimal cantidadRecetaTotal) { this.cantidadRecetaTotal = cantidadRecetaTotal; }
        public java.math.BigDecimal getCantidadRecetaReal() { return cantidadRecetaReal; }
        public void setCantidadRecetaReal(java.math.BigDecimal cantidadRecetaReal) { this.cantidadRecetaReal = cantidadRecetaReal; }
        public java.math.BigDecimal getCantidadRecetaEfectiva() { return cantidadRecetaEfectiva; }
        public void setCantidadRecetaEfectiva(java.math.BigDecimal cantidadRecetaEfectiva) { this.cantidadRecetaEfectiva = cantidadRecetaEfectiva; }
        public String getTipoRegistroConsumo() { return tipoRegistroConsumo; }
        public void setTipoRegistroConsumo(String tipoRegistroConsumo) { this.tipoRegistroConsumo = tipoRegistroConsumo; }
        public java.math.BigDecimal getCantidadDiariaPorAnimal() { return cantidadDiariaPorAnimal; }
        public void setCantidadDiariaPorAnimal(java.math.BigDecimal cantidadDiariaPorAnimal) { this.cantidadDiariaPorAnimal = cantidadDiariaPorAnimal; }
        public List<ConsumoDetalleDTO> getDetalles() { return detalles; }
        public void setDetalles(List<ConsumoDetalleDTO> detalles) { this.detalles = detalles; }
    }

    public static class ConsumoDetalleDTO {
        private Long id;
        private String tipoComponente;
        private String nombreComponente;
        private java.math.BigDecimal cantidadRequerida;
        private java.math.BigDecimal cantidadDisponible;
        private java.math.BigDecimal cantidadDescontada;
        private java.math.BigDecimal stockResultante;
        private java.math.BigDecimal deficit;
        private Boolean tieneDeficit;
        private java.math.BigDecimal porcentajeCobertura;
        private String unidadMedida;
        // Getters y setters...
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTipoComponente() { return tipoComponente; }
        public void setTipoComponente(String tipoComponente) { this.tipoComponente = tipoComponente; }
        public String getNombreComponente() { return nombreComponente; }
        public void setNombreComponente(String nombreComponente) { this.nombreComponente = nombreComponente; }
        public java.math.BigDecimal getCantidadRequerida() { return cantidadRequerida; }
        public void setCantidadRequerida(java.math.BigDecimal cantidadRequerida) { this.cantidadRequerida = cantidadRequerida; }
        public java.math.BigDecimal getCantidadDisponible() { return cantidadDisponible; }
        public void setCantidadDisponible(java.math.BigDecimal cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
        public java.math.BigDecimal getCantidadDescontada() { return cantidadDescontada; }
        public void setCantidadDescontada(java.math.BigDecimal cantidadDescontada) { this.cantidadDescontada = cantidadDescontada; }
        public java.math.BigDecimal getStockResultante() { return stockResultante; }
        public void setStockResultante(java.math.BigDecimal stockResultante) { this.stockResultante = stockResultante; }
        public java.math.BigDecimal getDeficit() { return deficit; }
        public void setDeficit(java.math.BigDecimal deficit) { this.deficit = deficit; }
        public Boolean getTieneDeficit() { return tieneDeficit; }
        public void setTieneDeficit(Boolean tieneDeficit) { this.tieneDeficit = tieneDeficit; }
        public java.math.BigDecimal getPorcentajeCobertura() { return porcentajeCobertura; }
        public void setPorcentajeCobertura(java.math.BigDecimal porcentajeCobertura) { this.porcentajeCobertura = porcentajeCobertura; }
        public String getUnidadMedida() { return unidadMedida; }
        public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    }

    public static class AlertaStockDTO {
        private LocalDate fecha;
        private String insumoNombre;
        private java.math.BigDecimal deficit;
        private java.math.BigDecimal cantidadRequerida;
        private java.math.BigDecimal cantidadDisponible;
        private java.math.BigDecimal porcentajeCobertura;
        private String unidadMedida;
        private Long diaAlimentacionId;
        // Getters y setters...
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        public String getInsumoNombre() { return insumoNombre; }
        public void setInsumoNombre(String insumoNombre) { this.insumoNombre = insumoNombre; }
        public java.math.BigDecimal getDeficit() { return deficit; }
        public void setDeficit(java.math.BigDecimal deficit) { this.deficit = deficit; }
        public java.math.BigDecimal getCantidadRequerida() { return cantidadRequerida; }
        public void setCantidadRequerida(java.math.BigDecimal cantidadRequerida) { this.cantidadRequerida = cantidadRequerida; }
        public java.math.BigDecimal getCantidadDisponible() { return cantidadDisponible; }
        public void setCantidadDisponible(java.math.BigDecimal cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
        public java.math.BigDecimal getPorcentajeCobertura() { return porcentajeCobertura; }
        public void setPorcentajeCobertura(java.math.BigDecimal porcentajeCobertura) { this.porcentajeCobertura = porcentajeCobertura; }
        public String getUnidadMedida() { return unidadMedida; }
        public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
        public Long getDiaAlimentacionId() { return diaAlimentacionId; }
        public void setDiaAlimentacionId(Long diaAlimentacionId) { this.diaAlimentacionId = diaAlimentacionId; }
    }
}
