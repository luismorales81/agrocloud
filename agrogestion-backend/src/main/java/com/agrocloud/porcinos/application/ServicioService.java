package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Recordatorio;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.application.RecordatorioService;

import com.agrocloud.porcinos.domain.Gestacion;
import com.agrocloud.porcinos.domain.HistorialEstadoMadre;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.Padrillo;
import com.agrocloud.porcinos.domain.Servicio;
import com.agrocloud.porcinos.infrastructure.GestacionRepository;
import com.agrocloud.porcinos.infrastructure.HistorialEstadoMadreRepository;
import com.agrocloud.porcinos.infrastructure.MadreRepository;
import com.agrocloud.porcinos.infrastructure.PadrilloRepository;
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
 * Servicio para gestionar servicios reproductivos (monta natural e inseminación artificial)
 * Incluye validaciones completas y automatismos según reglas productivas
 */
@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private MadreRepository madreRepository;

    @Autowired
    private PadrilloRepository padrilloRepository;

    @Autowired
    private GestacionRepository gestacionRepository;

    @Autowired
    private GestacionService gestacionService;

    @Autowired
    private HistorialEstadoMadreRepository historialEstadoMadreRepository;

    @Autowired
    private ParametrosProductivosPorcinoRepository parametrosProductivosRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private RecordatorioService recordatorioService;

    @Autowired
    private MadreService madreService;

    /**
     * Crear un nuevo servicio reproductivo con validaciones completas
     */
    @Transactional
    public Servicio crearServicio(Servicio servicioData, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        // Validar y obtener la madre
        Optional<Madre> madreOpt = madreRepository.findByIdAndActivoTrue(servicioData.getMadre().getId());
        if (madreOpt.isEmpty()) {
            throw new IllegalArgumentException("La madre no existe o está inactiva");
        }
        Madre madre = madreOpt.get();

        // Validar empresa de la madre
        if (!madre.getEmpresa().getId().equals(empresaActiva.get().getId())) {
            throw new IllegalArgumentException("La madre no pertenece a la empresa activa");
        }

        // VALIDACIONES DE MADRE
        validarMadreParaServicio(madre);

        // VALIDACIONES DE PADRILLO/MACHO según tipo de servicio
        if (servicioData.getTipo() == Servicio.TipoServicio.MONTA_NATURAL) {
            if (servicioData.getMachoId() == null) {
                throw new IllegalArgumentException("Debe especificar el padrillo para monta natural");
            }
            validarPadrilloParaServicio(servicioData.getMachoId(), empresaActiva.get());
        } else if (servicioData.getTipo() == Servicio.TipoServicio.IA) {
            // Para IA, validar según origen del semen
            if (servicioData.getOrigenSemen() == Servicio.OrigenSemen.INTERNO) {
                if (servicioData.getMachoId() == null) {
                    throw new IllegalArgumentException("Debe especificar el padrillo para inseminación artificial interna");
                }
                validarPadrilloParaServicio(servicioData.getMachoId(), empresaActiva.get());
            } else if (servicioData.getOrigenSemen() == Servicio.OrigenSemen.EXTERNO) {
                if (servicioData.getMachoNombre() == null || servicioData.getMachoNombre().trim().isEmpty()) {
                    throw new IllegalArgumentException("Debe especificar la información del macho externo para inseminación artificial externa");
                }
            }
        }

        // Validar que no haya servicios activos simultáneos
        Optional<Servicio> servicioActivo = servicioRepository.findPendienteControlByMadre(madre);
        if (servicioActivo.isPresent()) {
            throw new IllegalArgumentException("La madre ya tiene un servicio activo pendiente de control");
        }

        // Validar ventana válida de servicio (usando parámetros productivos)
        validarVentanaValidaServicio(madre, servicioData.getFechaServicio(), empresaActiva.get());

        // Obtener parámetros productivos para calcular días de gestación
        Integer diasGestacion = obtenerDiasGestacion(empresaActiva.get());

        // Crear el servicio
        servicioData.setMadre(madre);
        servicioData.setEmpresa(empresaActiva.get());
        servicioData.setUsuario(user);
        servicioData.setEstadoServicio(Servicio.EstadoServicio.PENDIENTE_CONTROL);
        servicioData.setActivo(true);

        // Si no tiene fecha de control de celo, usar la fecha del servicio
        if (servicioData.getFechaControlCelo() == null) {
            servicioData.setFechaControlCelo(servicioData.getFechaServicio());
        }

        // Calcular número de intento: máximo de intentos previos de esta madre + 1
        // Así, si el control falla y se carga un nuevo servicio, será Intento #2, #3, etc.
        int siguienteIntento = calcularSiguienteNumeroIntento(madre);
        servicioData.setNumeroIntento(siguienteIntento);

        Servicio servicioGuardado = servicioRepository.save(servicioData);

        // AUTOMATISMO: Actualizar estado de la madre a EN_SERVICIO
        madreService.actualizarEstadoMadre(madre, Madre.EstadoMadre.ADULTA, servicioData.getFechaServicio(), 
                              "Servicio registrado: " + servicioData.getTipo().name());

        // AUTOMATISMO: Crear gestación automáticamente con estado PENDIENTE_CONTRASTAR
        crearGestacionAutomatica(servicioGuardado, diasGestacion, user);

        // AUTOMATISMO: Crear recordatorio para Control de Celo
        crearRecordatorioControlCelo(servicioGuardado, user, empresaActiva.get());

        return servicioGuardado;
    }

    /**
     * Validar que la madre puede recibir un servicio
     */
    private void validarMadreParaServicio(Madre madre) {
        // La madre debe estar activa
        if (!madre.getActivo()) {
            throw new IllegalArgumentException("La madre no está activa");
        }

        // La madre no debe estar gestante
        if (madre.getEstadoActual() == Madre.EstadoMadre.GESTACION) {
            throw new IllegalArgumentException("La madre está gestante, no puede recibir servicio");
        }

        // La madre no debe estar en lactancia
        if (madre.getEstadoActual() == Madre.EstadoMadre.LACTANCIA) {
            throw new IllegalArgumentException("La madre está en lactancia, debe destetar primero");
        }

        // Verificar que no esté muerta (buscar en muertes)
        // Esto se validará en el método de muerte de madre

        // Verificar que no esté dada de baja
        // El estado DESCARTE indica baja
        if (madre.getEstadoActual() == Madre.EstadoMadre.DESCARTE) {
            throw new IllegalArgumentException("La madre está dada de baja");
        }
    }

    /**
     * Validar que el padrillo puede realizar un servicio
     */
    private void validarPadrilloParaServicio(Long padrilloId, Empresa empresa) {
        Optional<Padrillo> padrilloOpt = padrilloRepository.findByIdAndActivoTrue(padrilloId);
        if (padrilloOpt.isEmpty()) {
            throw new IllegalArgumentException("El padrillo no existe o está inactivo");
        }

        Padrillo padrillo = padrilloOpt.get();
        if (!padrillo.getEmpresa().getId().equals(empresa.getId())) {
            throw new IllegalArgumentException("El padrillo no pertenece a la empresa activa");
        }

        // Verificar que no esté muerto (esto se validará cuando se registre la muerte)
        // Por ahora solo validamos que esté activo
    }

    /**
     * Validar ventana válida de servicio según parámetros productivos
     */
    private void validarVentanaValidaServicio(Madre madre, LocalDate fechaServicio, Empresa empresa) {
        Optional<ParametrosProductivosPorcino> parametrosOpt = 
            parametrosProductivosRepository.findByEmpresa(empresa);

        if (parametrosOpt.isPresent()) {
            ParametrosProductivosPorcino parametros = parametrosOpt.get();
            
            // Validar tiempo mínimo entre servicios (si existe configuración)
            if (parametros.getTiempoEsperaEntreServiciosHoras() != null) {
                // Buscar último servicio de la madre
                List<Servicio> serviciosAnteriores = servicioRepository.findByMadreAndActivoTrue(madre);
                if (!serviciosAnteriores.isEmpty()) {
                    Servicio ultimoServicio = serviciosAnteriores.stream()
                        .max((s1, s2) -> s1.getFechaServicio().compareTo(s2.getFechaServicio()))
                        .orElse(null);
                    
                    if (ultimoServicio != null) {
                        long horasEntreServicios = java.time.temporal.ChronoUnit.HOURS
                            .between(ultimoServicio.getFechaServicio().atStartOfDay(), 
                                    fechaServicio.atStartOfDay());
                        
                        if (horasEntreServicios < parametros.getTiempoEsperaEntreServiciosHoras()) {
                            throw new IllegalArgumentException(
                                String.format("Debe esperar al menos %d horas entre servicios",
                                    parametros.getTiempoEsperaEntreServiciosHoras()));
                        }
                    }
                }
            }
        }
    }

    /**
     * Obtener días de gestación desde parámetros productivos
     */
    private Integer obtenerDiasGestacion(Empresa empresa) {
        Optional<ParametrosProductivosPorcino> parametrosOpt = 
            parametrosProductivosRepository.findByEmpresa(empresa);
        
        if (parametrosOpt.isPresent() && parametrosOpt.get().getDiasPromedioGestacion() != null) {
            return parametrosOpt.get().getDiasPromedioGestacion();
        }
        
        // Valor por defecto si no está configurado
        return 115;
    }

    /**
     * Crear gestación automáticamente después de un servicio
     */
    private void crearGestacionAutomatica(Servicio servicio, Integer diasGestacion, User user) {
        // Verificar si ya existe una gestación para este servicio
        Optional<Gestacion> gestacionExistente = gestacionRepository.findByServicioAndActivoTrue(servicio);
        if (gestacionExistente.isPresent()) {
            // Ya existe una gestación, no crear otra
            return;
        }

        // Verificar que la madre no tenga otra gestación activa
        Optional<Gestacion> gestacionActivaMadre = gestacionRepository.findActivaByMadre(servicio.getMadre());
        if (gestacionActivaMadre.isPresent()) {
            // Ya hay una gestación activa, no crear otra
            return;
        }

        Gestacion gestacion = new Gestacion();
        gestacion.setMadre(servicio.getMadre());
        gestacion.setServicio(servicio);
        gestacion.setFechaInicio(servicio.getFechaServicio());
        gestacion.setFechaProbableParto(servicio.getFechaServicio().plusDays(diasGestacion));
        gestacion.setEstado(Gestacion.EstadoGestacion.EN_CURSO);
        gestacion.setEmpresa(servicio.getEmpresa());
        gestacion.setUsuario(user);
        gestacion.setActivo(true);

        try {
            Gestacion gestacionGuardada = gestacionRepository.save(gestacion);

            // AUTOMATISMO: Crear recordatorios para Parto
            crearRecordatoriosGestacion(gestacionGuardada, user, servicio.getEmpresa());
        } catch (Exception e) {
            // Log error pero no fallar el proceso principal
            System.err.println("Error creando gestación automática: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crear recordatorios automáticos para gestación (Parto Esperado)
     * Duplicado de GestacionService para evitar dependencia circular
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
     * Crear recordatorio automático para Control de Celo
     */
    private void crearRecordatorioControlCelo(Servicio servicio, User user, Empresa empresa) {
        try {
            Optional<ParametrosProductivosPorcino> parametrosOpt = 
                parametrosProductivosRepository.findByEmpresa(empresa);
            
            int diasControlCelo = parametrosOpt
                .map(p -> p.getDiasControlCelo() != null ? p.getDiasControlCelo() : 21)
                .orElse(21);

            LocalDate fechaControlCelo = servicio.getFechaServicio().plusDays(diasControlCelo);

            Recordatorio recordatorio = new Recordatorio();
            recordatorio.setTitulo("Control de Celo - " + servicio.getMadre().getIdentificacion());
            recordatorio.setDescripcion(String.format(
                "Realizar control de celo para confirmar preñez. Servicio del %s",
                servicio.getFechaServicio()));
            recordatorio.setFecha(fechaControlCelo);
            recordatorio.setTipo(Recordatorio.TipoRecordatorio.REPRODUCCION);
            recordatorio.setUsuario(user);
            recordatorio.setServicioId(servicio.getId());
            recordatorio.setMadreId(servicio.getMadre().getId());
            recordatorio.setActivo(true);
            recordatorio.setCompletado(false);

            recordatorioService.crearRecordatorio(recordatorio);
        } catch (Exception e) {
            // Log error pero no fallar el proceso principal
            System.err.println("Error creando recordatorio de control de celo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Registrar control de celo de un servicio
     * Actualiza el estado del servicio y crea gestación si está preñada
     */
    @Transactional
    public Servicio registrarControlCelo(Long servicioId, boolean preñada, String observaciones, User user) {
        try {
            System.out.println("=== INICIO registrarControlCelo ===");
            System.out.println("servicioId: " + servicioId);
            System.out.println("preñada: " + preñada);
            
            Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
            if (empresaActiva.isEmpty()) {
                throw new IllegalArgumentException("Usuario no tiene empresa activa");
            }
            System.out.println("Empresa activa obtenida: " + empresaActiva.get().getId());

            // Obtener el servicio con relaciones cargadas
            Optional<Servicio> servicioOpt = servicioRepository.findByIdWithRelations(servicioId);
            if (servicioOpt.isEmpty()) {
                throw new IllegalArgumentException("Servicio no encontrado");
            }

            Servicio servicio = servicioOpt.get();
            System.out.println("Servicio obtenido: " + servicio.getId());
            
            // Validar que el servicio esté activo
            if (!servicio.getActivo()) {
                throw new IllegalArgumentException("El servicio no está activo");
            }

            // Validar empresa (cargar empresa si es necesario)
            if (servicio.getEmpresa() == null || !servicio.getEmpresa().getId().equals(empresaActiva.get().getId())) {
                throw new IllegalArgumentException("El servicio no pertenece a la empresa activa");
            }

            // Validar que el servicio esté pendiente de control
            if (servicio.getEstadoServicio() != Servicio.EstadoServicio.PENDIENTE_CONTROL) {
                String estadoActual = servicio.getEstadoServicio().name();
                String mensaje = String.format(
                    "No se puede registrar un control de celo para este servicio. " +
                    "El servicio ID %d ya fue procesado y tiene estado: %s. " +
                    "Solo se pueden registrar controles de celo para servicios con estado PENDIENTE_CONTROL.",
                    servicioId, estadoActual
                );
                throw new IllegalArgumentException(mensaje);
            }

            // Calcular días desde el servicio para validación
            long diasDesdeServicio = java.time.temporal.ChronoUnit.DAYS
                .between(servicio.getFechaServicio(), LocalDate.now());
            
            System.out.println("Días desde servicio: " + diasDesdeServicio);
            
            // Obtener parámetros para validar ventana de control
            Optional<ParametrosProductivosPorcino> parametrosOpt = 
                parametrosProductivosRepository.findByEmpresa(empresaActiva.get());
            
            int diasControlCelo = parametrosOpt
                .map(p -> p.getDiasControlCelo() != null ? p.getDiasControlCelo() : 21)
                .orElse(21);
            
            // Advertir si el control es muy tardío (más de 60 días desde servicio)
            if (diasDesdeServicio > 60) {
                System.out.println("⚠️ ADVERTENCIA: Control de celo muy tardío (" + diasDesdeServicio + " días desde servicio)");
            }

            // Actualizar observaciones si se proporcionan
            if (observaciones != null && !observaciones.trim().isEmpty()) {
                String obsActual = servicio.getObservaciones() != null ? servicio.getObservaciones() : "";
                String obsNueva = "Control de celo (" + diasDesdeServicio + " días desde servicio): " + observaciones;
                servicio.setObservaciones(obsActual + (obsActual.isEmpty() ? "" : "\n") + obsNueva);
            }

            // Actualizar estado del servicio según resultado
            if (preñada) {
                servicio.setEstadoServicio(Servicio.EstadoServicio.PREÑEZ_CONFIRMADA);
                
                // Verificar si ya existe una gestación para este servicio
                Optional<Gestacion> gestacionExistente = gestacionRepository.findByServicioAndActivoTrue(servicio);

                if (gestacionExistente.isEmpty()) {
                    // Crear gestación automáticamente si no existe
                    try {
                        Integer diasGestacion = obtenerDiasGestacion(empresaActiva.get());
                        crearGestacionAutomatica(servicio, diasGestacion, user);
                    } catch (Exception e) {
                        System.err.println("Error al crear gestación en control de celo: " + e.getMessage());
                        e.printStackTrace();
                        // Continuar aunque falle la creación de gestación
                    }
                } else {
                    // Si ya existe, verificar su estado
                    Gestacion gestacion = gestacionExistente.get();
                    System.out.println("Gestación existente encontrada. Estado: " + gestacion.getEstado());
                    System.out.println("Fecha probable parto: " + gestacion.getFechaProbableParto());
                    
                    // Si la gestación ya finalizó (fecha probable de parto pasada), no cambiar el estado
                    if (gestacion.getFechaProbableParto() != null && 
                        gestacion.getFechaProbableParto().isBefore(LocalDate.now())) {
                        System.out.println("⚠️ ADVERTENCIA: La gestación ya debería haber finalizado (fecha probable parto: " + 
                            gestacion.getFechaProbableParto() + ")");
                        // No cambiar el estado si ya finalizó
                    } else if (gestacion.getEstado() != Gestacion.EstadoGestacion.EN_CURSO) {
                        // Solo actualizar si no está en curso y aún no ha pasado la fecha probable de parto
                        gestacion.setEstado(Gestacion.EstadoGestacion.EN_CURSO);
                        try {
                            gestacionRepository.save(gestacion);
                            System.out.println("Gestación actualizada a EN_CURSO");
                        } catch (Exception e) {
                            System.err.println("Error al actualizar gestación: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                
                // Actualizar estado de la madre a GESTACION solo si no está en LACTANCIA
                try {
                    Madre madre = servicio.getMadre();
                    if (madre.getEstadoActual() == Madre.EstadoMadre.LACTANCIA) {
                        System.out.println("⚠️ ADVERTENCIA: La madre está en LACTANCIA. No se cambiará el estado.");
                    } else {
                        madreService.actualizarEstadoMadre(madre, Madre.EstadoMadre.GESTACION, 
                                             LocalDate.now(), 
                                             "Preñez confirmada por control de celo (tardío: " + diasDesdeServicio + " días)");
                    }
                } catch (Exception e) {
                    System.err.println("Error al actualizar estado de la madre: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                // Servicio fallido
                servicio.setEstadoServicio(Servicio.EstadoServicio.FALLIDO);
                
                // Cerrar gestación si existe (marcar como aborto)
                Optional<Gestacion> gestacionExistenteOpt = gestacionRepository.findByServicioAndActivoTrue(servicio);
                if (gestacionExistenteOpt.isPresent()) {
                    Gestacion gestacion = gestacionExistenteOpt.get();
                    if (gestacion.getEstado() == Gestacion.EstadoGestacion.EN_CURSO) {
                        gestacion.setEstado(Gestacion.EstadoGestacion.ABORTO);
                        gestacion.setFechaAborto(LocalDate.now());
                        gestacion.setCausaAborto("Control de celo negativo - servicio fallido");
                        try {
                            gestacionRepository.save(gestacion);
                        } catch (Exception e) {
                            System.err.println("Error al cerrar gestación: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

                // Devolver estado de la madre a ADULTA
                try {
                    Madre madre = servicio.getMadre();
                    madreService.actualizarEstadoMadre(madre, Madre.EstadoMadre.ADULTA, 
                                         LocalDate.now(), 
                                         "Control de celo negativo - servicio fallido");
                } catch (Exception e) {
                    System.err.println("Error al actualizar estado de la madre: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("Guardando servicio actualizado...");
            Servicio servicioGuardado = servicioRepository.save(servicio);
            System.out.println("=== FIN registrarControlCelo (éxito) ===");
            return servicioGuardado;
        } catch (IllegalArgumentException e) {
            System.err.println("Error de validación en registrarControlCelo: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("Error inesperado en registrarControlCelo: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al registrar control de celo: " + e.getMessage(), e);
        }
    }

    /**
     * Calcula el siguiente número de intento para un nuevo servicio de la madre.
     * Toma el máximo numeroIntento de los servicios previos (fallidos o exitosos) de esa madre y suma 1.
     * Así, si el control de celo falla y se carga un nuevo servicio, será Intento #2, #3, etc.
     */
    private int calcularSiguienteNumeroIntento(Madre madre) {
        List<Servicio> serviciosPrevios = servicioRepository.findByMadreAndActivoTrue(madre);
        int maxIntento = serviciosPrevios.stream()
            .mapToInt(s -> s.getNumeroIntento() != null ? s.getNumeroIntento() : 1)
            .max()
            .orElse(0);
        return maxIntento + 1;
    }

    /**
     * Obtener servicios por empresa
     */
    public List<Servicio> obtenerServiciosPorEmpresa(User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }
        return servicioRepository.findByEmpresaAndActivoTrue(empresaActiva.get());
    }

    /**
     * Obtener servicios pendientes de control
     */
    public List<Servicio> obtenerServiciosPendientesControl(User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }
        return servicioRepository.findPendientesControlByEmpresa(empresaActiva.get());
    }

    /**
     * Obtener servicio por ID
     */
    public Optional<Servicio> obtenerServicioPorId(Long id, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return Optional.empty();
        }

        Optional<Servicio> servicio = servicioRepository.findByIdAndActivoTrue(id);
        if (servicio.isPresent() && servicio.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return servicio;
        }
        return Optional.empty();
    }

    /**
     * Marcar servicio como fallido
     */
    @Transactional
    public Servicio marcarServicioComoFallido(Long servicioId, User user) {
        Optional<Servicio> servicioOpt = obtenerServicioPorId(servicioId, user);
        if (servicioOpt.isEmpty()) {
            throw new IllegalArgumentException("Servicio no encontrado");
        }

        Servicio servicio = servicioOpt.get();
        servicio.setEstadoServicio(Servicio.EstadoServicio.FALLIDO);

        // Cerrar gestación asociada si existe
        Optional<Gestacion> gestacionOpt = gestacionRepository.findActivaByMadre(servicio.getMadre());
        if (gestacionOpt.isPresent()) {
            Gestacion gestacion = gestacionOpt.get();
            if (gestacion.getServicio() != null && gestacion.getServicio().getId().equals(servicioId)) {
                gestacion.setEstado(Gestacion.EstadoGestacion.ABORTO);
                gestacion.setCausaAborto("Servicio fallido");
                gestacionRepository.save(gestacion);
            }
        }

        // Devolver estado de la madre a VACÍA o ADULTA
        madreService.actualizarEstadoMadre(servicio.getMadre(), Madre.EstadoMadre.ADULTA, 
                              LocalDate.now(), "Servicio fallido");

        return servicioRepository.save(servicio);
    }

    /**
     * Obtener ID de gestación asociada a un servicio (para registro de chequeo desde calendario).
     * Devuelve vacío si el servicio no tiene gestación activa.
     */
    @Transactional(readOnly = true)
    public Optional<Long> obtenerGestacionIdPorServicioId(Long servicioId, User user) {
        Optional<Servicio> servicioOpt = obtenerServicioPorId(servicioId, user);
        if (servicioOpt.isEmpty()) {
            return Optional.empty();
        }
        return gestacionRepository.findByServicioAndActivoTrue(servicioOpt.get())
                .map(Gestacion::getId);
    }
}





