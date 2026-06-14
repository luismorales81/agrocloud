package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;

import com.agrocloud.porcinos.domain.ChequeoGestacion;
import com.agrocloud.porcinos.domain.Gestacion;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.Servicio;
import com.agrocloud.porcinos.infrastructure.ChequeoGestacionRepository;
import com.agrocloud.porcinos.infrastructure.GestacionRepository;
import com.agrocloud.porcinos.infrastructure.HistorialEstadoMadreRepository;
import com.agrocloud.porcinos.infrastructure.MadreRepository;
import com.agrocloud.porcinos.infrastructure.ServicioRepository;
import com.agrocloud.porcinos.domain.ParametrosProductivosPorcino;
import com.agrocloud.porcinos.infrastructure.ParametrosProductivosPorcinoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar chequeos de gestación
 * Incluye validaciones de rangos y automatismos según resultado
 */
@Service
public class ChequeoGestacionService {

    @Autowired
    private ChequeoGestacionRepository chequeoGestacionRepository;

    @Autowired
    private GestacionRepository gestacionRepository;

    @Autowired
    private MadreRepository madreRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private ParametrosProductivosPorcinoRepository parametrosProductivosRepository;


    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private MadreService madreService;

    /**
     * Registrar un chequeo de gestación con validaciones completas
     */
    @Transactional
    public ChequeoGestacion registrarChequeo(Long gestacionId, ChequeoGestacion chequeoData, User user) {
        Optional<Gestacion> gestacionOpt = gestacionRepository.findByIdAndActivoTrue(gestacionId);
        if (gestacionOpt.isEmpty()) {
            throw new IllegalArgumentException("Gestación no encontrada o inactiva");
        }

        Gestacion gestacion = gestacionOpt.get();
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty() || !gestacion.getEmpresa().getId().equals(empresaActiva.get().getId())) {
            throw new IllegalArgumentException("No tiene permisos para registrar chequeos en esta gestación");
        }

        // Validar que la gestación esté en curso
        if (gestacion.getEstado() != Gestacion.EstadoGestacion.EN_CURSO) {
            throw new IllegalArgumentException("Solo se pueden registrar chequeos en gestaciones en curso");
        }

        // Validar rango de fechas según parámetros productivos
        validarRangoChequeo(gestacion, chequeoData.getFecha(), empresaActiva.get());

        chequeoData.setGestacion(gestacion);
        chequeoData.setEmpresa(empresaActiva.get());
        chequeoData.setUsuario(user);
        chequeoData.setActivo(true);

        ChequeoGestacion chequeoGuardado = chequeoGestacionRepository.save(chequeoData);

        // AUTOMATISMOS según resultado
        if (chequeoData.getResultado() == ChequeoGestacion.ResultadoChequeo.POSITIVO) {
            // Cambiar estado de la madre a GESTANTE
            madreService.actualizarEstadoMadre(gestacion.getMadre(), Madre.EstadoMadre.GESTACION, 
                                 chequeoData.getFecha(), "Chequeo positivo de gestación");
            
            // Actualizar estado del servicio a PREÑEZ_CONFIRMADA
            if (gestacion.getServicio() != null) {
                Servicio servicio = gestacion.getServicio();
                servicio.setEstadoServicio(Servicio.EstadoServicio.PREÑEZ_CONFIRMADA);
                servicioRepository.save(servicio);
            }
        } else if (chequeoData.getResultado() == ChequeoGestacion.ResultadoChequeo.NEGATIVO) {
            // NOTA: Un chequeo negativo NO marca automáticamente como aborto
            // Un aborto es un evento específico que debe registrarse manualmente
            // Un chequeo negativo solo indica que no se detectó preñez en ese momento
            // Puede ser que:
            // - La gestación es muy temprana y aún no se detecta
            // - El método de chequeo no fue efectivo
            // - El servicio realmente falló (pero esto se confirma con múltiples chequeos o control de celo)
            
            // Solo actualizar observaciones de la gestación para registrar el chequeo negativo
            // NO cambiar el estado de la gestación ni del servicio automáticamente
            // El usuario debe decidir manualmente si registrar un aborto o esperar más chequeos
            
            String obsActual = gestacion.getObservaciones() != null ? gestacion.getObservaciones() : "";
            String obsNueva = String.format("Chequeo negativo el %s - Método: %s. %s",
                chequeoData.getFecha(),
                chequeoData.getMetodo(),
                chequeoData.getObservaciones() != null ? chequeoData.getObservaciones() : "");
            gestacion.setObservaciones(obsActual + (obsActual.isEmpty() ? "" : "\n") + obsNueva);
            gestacionRepository.save(gestacion);
            
            // NO cambiar estado del servicio automáticamente
            // NO cambiar estado de la gestación automáticamente
            // NO cambiar estado de la madre automáticamente
            // El usuario debe evaluar y decidir si registrar aborto manualmente
        }

        return chequeoGuardado;
    }

    /**
     * Validar que el chequeo esté dentro del rango configurado
     */
    private void validarRangoChequeo(Gestacion gestacion, LocalDate fechaChequeo, Empresa empresa) {
        Optional<ParametrosProductivosPorcino> parametrosOpt = 
            parametrosProductivosRepository.findByEmpresa(empresa);

        if (parametrosOpt.isPresent()) {
            ParametrosProductivosPorcino parametros = parametrosOpt.get();
            
            // Validar días de antelación para alertar ecografías
            if (parametros.getDiasAntelacionAlertarEcografias() != null) {
                // El chequeo debería estar cerca de la fecha probable de parto
                // Validar que no sea demasiado temprano ni demasiado tarde
                long diasDesdeInicio = java.time.temporal.ChronoUnit.DAYS
                    .between(gestacion.getFechaInicio(), fechaChequeo);
                
                // Rango típico: entre día 21 y día 90 de gestación
                if (diasDesdeInicio < 21 || diasDesdeInicio > 90) {
                    throw new IllegalArgumentException(
                        String.format("El chequeo debe realizarse entre los días 21 y 90 de gestación. " +
                                     "Días transcurridos: %d", diasDesdeInicio));
                }
            }
        }
    }


    public List<ChequeoGestacion> obtenerChequeosPorGestacion(Long gestacionId, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        Optional<Gestacion> gestacion = gestacionRepository.findByIdAndActivoTrue(gestacionId);
        if (gestacion.isEmpty() || !gestacion.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return List.of();
        }

        return chequeoGestacionRepository.findByGestacionAndActivoTrue(gestacion.get());
    }
}
