package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Recordatorio;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.application.RecordatorioService;

import com.agrocloud.porcinos.domain.Gestacion;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.Parto;
import com.agrocloud.porcinos.infrastructure.GestacionRepository;
import com.agrocloud.porcinos.infrastructure.MadreRepository;
import com.agrocloud.porcinos.infrastructure.PartoRepository;
import com.agrocloud.porcinos.infrastructure.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class GestacionService {

    @Autowired
    private GestacionRepository gestacionRepository;

    @Autowired
    private MadreRepository madreRepository;

    @Autowired
    @SuppressWarnings("unused") // Reservado para futuras implementaciones
    private ServicioRepository servicioRepository;

    @Autowired
    private PartoRepository partoRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private com.agrocloud.porcinos.infrastructure.ParametrosProductivosPorcinoRepository parametrosProductivosRepository;

    @Autowired
    private RecordatorioService recordatorioService;

    @Autowired
    private MadreService madreService;

    // Valores por defecto (usados solo si no hay configuración)
    private static final int DIAS_GESTACION_DEFAULT = 115;
    private static final int TOLERANCIA_DIAS_VENCIMIENTO_DEFAULT = 5;

    @Transactional
    public Gestacion crearGestacion(Gestacion gestacionData, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }

        Optional<Madre> madre = madreRepository.findByIdAndActivoTrue(gestacionData.getMadre().getId());
        if (madre.isEmpty()) {
            throw new RuntimeException("Madre no encontrada o inactiva");
        }

        // Verificar que no haya una gestación activa
        Optional<Gestacion> gestacionActiva = gestacionRepository.findActivaByMadre(madre.get());
        if (gestacionActiva.isPresent()) {
            throw new RuntimeException("La madre ya tiene una gestación activa");
        }

        // Calcular fecha probable de parto si no se proporciona
        if (gestacionData.getFechaProbableParto() == null && gestacionData.getFechaInicio() != null) {
            int diasGestacion = obtenerDiasGestacion(empresaActiva.get());
            gestacionData.setFechaProbableParto(gestacionData.getFechaInicio().plusDays(diasGestacion));
        }

        gestacionData.setMadre(madre.get());
        gestacionData.setEmpresa(empresaActiva.get());
        gestacionData.setUsuario(user);
        gestacionData.setEstado(Gestacion.EstadoGestacion.EN_CURSO);
        gestacionData.setActivo(true);

        // Actualizar estado de la madre
        madre.get().setEstadoActual(Madre.EstadoMadre.GESTACION);
        madreRepository.save(madre.get());

        Gestacion gestacionGuardada = gestacionRepository.save(gestacionData);

        // AUTOMATISMO: Crear recordatorio para Parto Esperado
        crearRecordatoriosGestacion(gestacionGuardada, user, empresaActiva.get());

        return gestacionGuardada;
    }

    @Transactional
    public Gestacion registrarAborto(Long gestacionId, LocalDate fechaAborto, String causaAborto, User user) {
        Optional<Gestacion> gestacionOpt = gestacionRepository.findByIdAndActivoTrue(gestacionId);
        if (gestacionOpt.isEmpty()) {
            throw new RuntimeException("Gestación no encontrada o inactiva");
        }

        Gestacion gestacion = gestacionOpt.get();
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty() || !gestacion.getEmpresa().getId().equals(empresaActiva.get().getId())) {
            throw new RuntimeException("No tiene permisos para registrar aborto en esta gestación");
        }

        gestacion.setEstado(Gestacion.EstadoGestacion.ABORTO);
        gestacion.setFechaAborto(fechaAborto);
        gestacion.setCausaAborto(causaAborto);

        // Actualizar estado de la madre usando método centralizado
        Madre madre = gestacion.getMadre();
        LocalDate fechaActualizacion = fechaAborto != null ? fechaAborto : LocalDate.now();
        madreService.actualizarEstadoMadre(madre, Madre.EstadoMadre.ADULTA, 
                             fechaActualizacion, 
                             "Aborto registrado" + (causaAborto != null ? ": " + causaAborto : ""));

        return gestacionRepository.save(gestacion);
    }

    @Transactional
    public Gestacion finalizarGestacion(Long gestacionId, User user) {
        Optional<Gestacion> gestacionOpt = gestacionRepository.findByIdAndActivoTrue(gestacionId);
        if (gestacionOpt.isEmpty()) {
            throw new RuntimeException("Gestación no encontrada o inactiva");
        }

        Gestacion gestacion = gestacionOpt.get();
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty() || !gestacion.getEmpresa().getId().equals(empresaActiva.get().getId())) {
            throw new RuntimeException("No tiene permisos para finalizar esta gestación");
        }

        gestacion.setEstado(Gestacion.EstadoGestacion.FINALIZADA);
        
        // VALIDACIÓN: Verificar si existe un parto para esta gestación
        // Si no hay parto, la madre debería volver a ADULTA
        // Si hay parto, el estado de la madre ya debería estar en LACTANCIA (actualizado por PartoService)
        Optional<Parto> partoOpt = partoRepository.findByMadreAndActivoTrue(gestacion.getMadre())
            .stream()
            .filter(p -> p.getFechaInicio() != null && 
                        p.getFechaInicio().toLocalDate().isAfter(gestacion.getFechaInicio()) &&
                        p.getFechaInicio().toLocalDate().isBefore(gestacion.getFechaProbableParto().plusDays(30)))
            .findFirst();
        
        if (partoOpt.isEmpty()) {
            // No hay parto registrado, actualizar estado de la madre a ADULTA
            Madre madre = gestacion.getMadre();
            LocalDate fechaActualizacion = gestacion.getFechaProbableParto() != null ? 
                gestacion.getFechaProbableParto() : LocalDate.now();
            madreService.actualizarEstadoMadre(madre, Madre.EstadoMadre.ADULTA, 
                                 fechaActualizacion, 
                                 "Gestación finalizada manualmente sin parto registrado");
        }
        // Si hay parto, el estado ya está correcto (LACTANCIA) y no se modifica
        
        return gestacionRepository.save(gestacion);
    }

    @Transactional(readOnly = true)
    public List<Gestacion> obtenerGestacionesActivas(User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }
        return gestacionRepository.findByEmpresaAndActivas(empresaActiva.get());
    }

    @Transactional(readOnly = true)
    public List<Gestacion> obtenerProximosPartos(User user, int dias) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }
        LocalDate fechaDesde = LocalDate.now();
        LocalDate fechaHasta = LocalDate.now().plusDays(dias);
        return gestacionRepository.findProximasPartos(empresaActiva.get(), fechaDesde, fechaHasta);
    }

    @Transactional(readOnly = true)
    public Optional<Gestacion> obtenerGestacionPorId(Long id, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return Optional.empty();
        }
        Optional<Gestacion> gestacion = gestacionRepository.findByIdAndActivoTrue(id);
        if (gestacion.isPresent() && gestacion.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return gestacion;
        }
        return Optional.empty();
    }

    /**
     * Verificar y alertar sobre gestaciones vencidas sin parto
     * Este método debe ejecutarse periódicamente (ej: scheduler)
     */
    @Transactional
    public List<Gestacion> verificarGestacionesVencidas(User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        List<Gestacion> gestacionesActivas = gestacionRepository.findByEmpresaAndActivas(empresaActiva.get());
        LocalDate hoy = LocalDate.now();
        List<Gestacion> gestacionesVencidas = new java.util.ArrayList<>();
        int toleranciaDias = obtenerToleranciaVencimiento(empresaActiva.get());

        for (Gestacion gestacion : gestacionesActivas) {
            LocalDate fechaVencimiento = gestacion.getFechaProbableParto().plusDays(toleranciaDias);
            
            if (hoy.isAfter(fechaVencimiento)) {
                // Generar alerta: Gestación vencida sin parto
                // En un sistema completo, esto podría crear una notificación o alerta
                gestacionesVencidas.add(gestacion);
                
                // Agregar observación a la gestación
                String observacion = gestacion.getObservaciones() != null ? gestacion.getObservaciones() : "";
                observacion += "\n⚠️ ALERTA: Gestación vencida sin parto registrado (fecha probable: " + 
                               gestacion.getFechaProbableParto() + ")";
                gestacion.setObservaciones(observacion);
                gestacionRepository.save(gestacion);
            }
        }

        return gestacionesVencidas;
    }

    /**
     * Obtener días de gestación desde parámetros productivos
     */
    private int obtenerDiasGestacion(Empresa empresa) {
        return parametrosProductivosRepository.findByEmpresa(empresa)
            .map(p -> p.getDiasPromedioGestacion() != null ? p.getDiasPromedioGestacion() : DIAS_GESTACION_DEFAULT)
            .orElse(DIAS_GESTACION_DEFAULT);
    }

    /**
     * Crear recordatorios automáticos para gestación (Parto Esperado)
     */
    private void crearRecordatoriosGestacion(Gestacion gestacion, User user, Empresa empresa) {
        try {
            if (gestacion.getFechaProbableParto() != null) {
                // Crear recordatorio para Parto Esperado
                Recordatorio recordatorioParto = new Recordatorio();
                recordatorioParto.setTitulo("Parto Esperado - " + gestacion.getMadre().getIdentificacion());
                recordatorioParto.setDescripcion("Fecha probable de parto. Estar preparado para el parto");
                recordatorioParto.setFecha(gestacion.getFechaProbableParto());
                recordatorioParto.setTipo(Recordatorio.TipoRecordatorio.PARTO);
                recordatorioParto.setUsuario(user);
                recordatorioParto.setGestacionId(gestacion.getId());
                recordatorioParto.setMadreId(gestacion.getMadre().getId());
                recordatorioParto.setActivo(true);
                recordatorioParto.setCompletado(false);
                
                recordatorioService.crearRecordatorio(recordatorioParto);
            }
        } catch (Exception e) {
            // Log error pero no fallar el proceso principal
            System.err.println("Error creando recordatorios de gestación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtener tolerancia de días de vencimiento desde parámetros productivos
     */
    private int obtenerToleranciaVencimiento(Empresa empresa) {
        return parametrosProductivosRepository.findByEmpresa(empresa)
            .map(p -> p.getDiasToleranciaVencimientoGestacion() != null ? p.getDiasToleranciaVencimientoGestacion() : TOLERANCIA_DIAS_VENCIMIENTO_DEFAULT)
            .orElse(TOLERANCIA_DIAS_VENCIMIENTO_DEFAULT);
    }
}
